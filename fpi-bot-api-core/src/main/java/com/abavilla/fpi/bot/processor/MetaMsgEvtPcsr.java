/******************************************************************************
 * FPI Application - Abavilla                                                 *
 * Copyright (C) 2022  Vince Jerald Villamora                                 *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.     *
 ******************************************************************************/

package com.abavilla.fpi.bot.processor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.abavilla.fpi.bot.entity.meta.MetaMsgEvt;
import com.abavilla.fpi.bot.mapper.meta.MetaMsgEvtMapper;
import com.abavilla.fpi.bot.repo.MetaMsgEvtRepo;
import com.abavilla.fpi.fw.dto.impl.RespDto;
import com.abavilla.fpi.fw.exceptions.ApiSvcEx;
import com.abavilla.fpi.fw.util.DateUtil;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.load.ext.rest.LoadQueryApi;
import com.abavilla.fpi.login.ext.dto.LoginDto;
import com.abavilla.fpi.login.ext.dto.SessionDto;
import com.abavilla.fpi.login.ext.dto.WebhookLoginDto;
import com.abavilla.fpi.login.ext.rest.TrustedLoginApi;
import com.abavilla.fpi.meta.ext.codec.MetaMsgEvtCodec;
import com.abavilla.fpi.meta.ext.dto.msgr.ext.MetaMsgEvtDto;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.abavilla.fpi.msgr.ext.rest.MsgrReqApi;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MetaMsgEvtPcsr {

  @ConfigProperty(name = "fpi.app-to-app.auth.username")
  String fpiSystemId;

  @RestClient
  TrustedLoginApi loginApi;

  @RestClient
  LoadQueryApi loadApi;

  @RestClient
  MsgrReqApi msgrApi;

  @Inject
  MetaMsgEvtMapper metaMsgEvtMapper;

  @Inject
  MetaMsgEvtRepo metaMsgEvtRepo;

  @ConsumeEvent(value = "meta-msg-evt", codec = MetaMsgEvtCodec.class)
  public Uni<Void> process(MetaMsgEvtDto evt) {
    Log.info("Received event: " + evt);
    if (StringUtils.isNotBlank(evt.getContent())) {
      return msgrApi.toggleTyping(evt.getSender(), true).chain(() -> {
        Log.info("Processing event: " + evt.getMetaMsgId());
        MetaMsgEvt metaMsgEvt = metaMsgEvtMapper.mapToEntity(evt);
        metaMsgEvt.setDateCreated(DateUtil.now());
        metaMsgEvt.setDateUpdated(DateUtil.now());
        return metaMsgEvtRepo.persist(metaMsgEvt).chain(() -> {
          Log.info("Logged to db: " + metaMsgEvt.getMetaMsgId());
          // verify if person is registered
          var metaId = evt.getSender();
          WebhookLoginDto login = new WebhookLoginDto();
          login.setUsername(metaId);
          Log.info("Authenticating user: " + login.getUsername());
          return loginApi.webhookAuthenticate(login)
            // process load
            .chain(session -> processLoadQuery(login, session, evt))
            // login failures/query exceptions
            .onFailure(ApiSvcEx.class).recoverWithUni(ex -> handleApiEx(evt, ex));
        })
        .onFailure(ex -> ex instanceof MongoWriteException wEx &&
          wEx.getError().getCategory().equals(ErrorCategory.DUPLICATE_KEY))
        .recoverWithItem(() -> {
          Log.warn("Received duplicate mid: " + evt.getMetaMsgId());
          return null;
        });
      })
      .chain(() -> msgrApi.toggleTyping(evt.getSender(), false)).replaceWithVoid()
      .onFailure().invoke(this::handleMsgEx);
    }
    return Uni.createFrom().voidItem();
  }

  private Uni<Void> processLoadQuery(LoginDto login, RespDto<SessionDto> session, MetaMsgEvtDto evt) {
    Log.info("Authenticated: " + login.getUsername());
    if (StringUtils.equals(session.getStatus(),
      SessionDto.SessionStatus.ESTABLISHED.toString())) {
      var query = new QueryDto();
      query.setQuery(evt.getContent());
      return loadApi.query(query, "Bearer " + session.getResp().getAccessToken()).chain(resp -> {
        Log.info("Query received, response is " + resp);
        return sendMsgrMsg(evt, "Working on your request, status is '%s'".formatted(resp.getStatus()));
      });
    }
    return sendMsgrMsg(evt, session.getStatus());
  }

  private void handleMsgEx(Throwable sendMsgEx) {
    Log.error("Message sending failed: " + sendMsgEx.getMessage(), sendMsgEx);
  }

  private Uni<Void> handleApiEx(MetaMsgEvtDto evt, Throwable ex) {
    Log.error("Error while processing evt: " + evt.getMetaMsgId(), ex);
    var apiSvcEx = (ApiSvcEx) ex;
    Uni<Void> handleAction;

    if (!HttpResponseStatus.INTERNAL_SERVER_ERROR.equals(apiSvcEx.getHttpResponseStatus())) {
      var jsonResponse = apiSvcEx.getJsonResponse(RespDto.class);
      if (jsonResponse != null && StringUtils.isNotBlank(jsonResponse.getError())) {
        handleAction = sendMsgrMsg(evt, jsonResponse.getError());
      } else {
        handleAction = sendMsgrMsg(evt, "Error occurred, please try again");
      }
    } else {
      handleAction = sendMsgrMsg(evt, "Error occurred, please try again");
    }

    return handleAction.chain(() -> msgrApi.toggleTyping(
      evt.getSender(), false).replaceWithVoid());
  }

  private Uni<Void> sendMsgrMsg(MetaMsgEvtDto evt, String msg) {
    Log.info("Sending msgr msg: " + msg + " event: " + evt.getMetaMsgId());
    var msgReq = new MsgrMsgReqDto();
    msgReq.setRecipient(evt.getSender());
    msgReq.setContent(msg);
    return msgrApi.sendMsg(msgReq, fpiSystemId).replaceWithVoid();
  }

}
