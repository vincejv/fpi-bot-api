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
import com.abavilla.fpi.bot.service.MetaMsgrApiSvc;
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
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MetaMsgEvtPcsr {

  @RestClient
  TrustedLoginApi loginApi;

  @RestClient
  LoadQueryApi loadApi;

  @Inject
  MetaMsgrApiSvc metaMsgrSvc;

  @Inject
  MetaMsgEvtMapper metaMsgEvtMapper;

  @Inject
  MetaMsgEvtRepo metaMsgEvtRepo;

  @ConsumeEvent(value = "meta-msg-evt", codec = MetaMsgEvtCodec.class)
  public Uni<Void> process(MetaMsgEvtDto evt) {
    Log.info("Received event: " + evt);
    if (StringUtils.isNotBlank(evt.getContent())) {
      return metaMsgrSvc.sendTypingIndicator(evt.getSender()).chain(() -> {
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
            // login failures
            .onFailure(ApiSvcEx.class).recoverWithUni(ex -> handleApiEx(evt, ex))
            // failures to send messenger
            .onFailure().recoverWithItem(this::handleMsgEx)
            .replaceWithVoid();
        })
        .onFailure(ex -> ex instanceof MongoWriteException wEx &&
          wEx.getError().getCategory().equals(ErrorCategory.DUPLICATE_KEY))
        .recoverWithUni(throwable -> {
          Log.warn("Received duplicate mid: " + evt.getMetaMsgId());
          return Uni.createFrom().voidItem();
        });
      }).onFailure().recoverWithItem(this::handleMsgEx);
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
        return sendMsgrMsg(evt, "Received your query, current status is " + resp.getStatus());
      });
    }
    return sendMsgrMsg(evt, session.getStatus());
  }

  private Void handleMsgEx(Throwable sendMsgEx) {
    Log.error("Message sending failed: " + sendMsgEx.getMessage(), sendMsgEx);
    return null;
  }

  private Uni<Void> handleApiEx(MetaMsgEvtDto evt, Throwable ex) {
    Log.error("Error while processing evt: " + evt.getMetaMsgId(), ex);
    var apiSvcEx = (ApiSvcEx) ex;
    if (!HttpResponseStatus.INTERNAL_SERVER_ERROR.equals(apiSvcEx.getHttpResponseStatus())) {
      return sendMsgrMsg(evt,
        apiSvcEx.getJsonResponse(RespDto.class).getError());
    } else {
      return sendMsgrMsg(evt, "Error occurred, please try again");
    }
  }

  private Uni<Void> sendMsgrMsg(MetaMsgEvtDto evt, String msg) {
    Log.info("Sending msgr msg: " + msg + " event: " + evt.getMetaMsgId());
    return metaMsgrSvc.sendMsg(msg, evt.getSender()).replaceWithVoid();
  }

}
