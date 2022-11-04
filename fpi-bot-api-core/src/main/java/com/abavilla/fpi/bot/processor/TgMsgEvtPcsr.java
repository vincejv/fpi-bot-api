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

import com.abavilla.fpi.bot.codec.TGUpdateEvtCodec;
import com.abavilla.fpi.bot.entity.telegram.TelegramEvt;
import com.abavilla.fpi.bot.ext.entity.enums.BotSource;
import com.abavilla.fpi.bot.repo.TgEvtRepo;
import com.abavilla.fpi.fw.dto.impl.RespDto;
import com.abavilla.fpi.fw.exceptions.ApiSvcEx;
import com.abavilla.fpi.fw.util.DateUtil;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.load.ext.rest.LoadQueryApi;
import com.abavilla.fpi.login.ext.dto.SessionDto;
import com.abavilla.fpi.login.ext.dto.WebhookLoginDto;
import com.abavilla.fpi.login.ext.rest.TrustedLoginApi;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.abavilla.fpi.msgr.ext.rest.TelegramReqApi;
import com.pengrad.telegrambot.model.Update;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class TgMsgEvtPcsr {

  @RestClient
  TrustedLoginApi loginApi;

  @RestClient
  LoadQueryApi loadApi;

  @RestClient
  TelegramReqApi telegramApi;

  @Inject
  TgEvtRepo repo;

  @ConsumeEvent(value = "telegram-msg-evt", codec = TGUpdateEvtCodec.class)
  public Uni<Void> process(Update evt) {
    Log.info("Received telegram update event: " + evt);
    var log = mapToEntity(evt);
    return repo.persist(log).chain(savedLog -> {
      Log.info("Logged to db: " + savedLog);
      return telegramApi.toggleTyping(String.valueOf(savedLog.getSenderId())).chain(() -> {
        var login = new WebhookLoginDto();
        login.setUsername(String.valueOf(savedLog.getSenderId()));
        login.setBotSource(BotSource.TELEGRAM.getValue());
        Log.info("Authenticating user: " + login);
        return loginApi.webhookAuthenticate(login)
          // process load
          .chain(session -> processLoadQuery(login, session, evt)
            .onFailure().recoverWithUni(ex -> handleApiEx(evt, session.getResp().getUsername(), ex)))
          // login failures/query exceptions
          .onFailure().recoverWithUni(ex -> handleApiEx(evt, ex));
        });
      }
    );
  }

  private Uni<Void> processLoadQuery(WebhookLoginDto login, RespDto<SessionDto> session, Update evt) {
    Log.info("Authenticated: " + login.getUsername());
    if (StringUtils.equals(session.getStatus(),
      SessionDto.SessionStatus.ESTABLISHED.toString())) {
      var query = new QueryDto();
      query.setQuery(evt.message().text());
      query.setBotSource(login.getBotSource());
      return loadApi.query(query, "Bearer " + session.getResp().getAccessToken()).chain(resp -> {
        Log.info("Query received, response is " + resp);
        return sendMsgrMsg(evt, session.getResp().getUsername(),
          "Working on your request, status is '%s'".formatted(resp.getStatus()));
      });
    }
    return sendMsgrMsg(evt, session.getResp().getUsername(), session.getStatus());
  }

  private Uni<Void> handleApiEx(Update evt, Throwable ex) {
    return handleApiEx(evt, null, ex);
  }

  private Uni<Void> handleApiEx(Update tgUpdate, String fpiUser, Throwable ex) {
    Log.error("Error while processing evt: " + tgUpdate.updateId(), ex);
    var apiSvcEx = (ApiSvcEx) ex;
    Uni<Void> handleAction;

    if (!HttpResponseStatus.INTERNAL_SERVER_ERROR.equals(apiSvcEx.getHttpResponseStatus())) {
      var jsonResponse = apiSvcEx.getJsonResponse(RespDto.class);
      if (jsonResponse != null && StringUtils.isNotBlank(jsonResponse.getError())) {
        handleAction = sendMsgrMsg(tgUpdate, fpiUser, jsonResponse.getError());
      } else {
        handleAction = sendMsgrMsg(tgUpdate, fpiUser, "Error occurred, please try again");
      }
    } else {
      handleAction = sendMsgrMsg(tgUpdate, fpiUser, "Error occurred, please try again");
    }

    return handleAction;
  }

  private Uni<Void> sendMsgrMsg(Update evt, String fpiUser, String msg) {
    Log.info("Sending telegram msg: " + msg + " event: " + evt.updateId());
    var msgReq = new MsgrMsgReqDto();
    msgReq.setRecipient(String.valueOf(evt.message().chat().id()));
    msgReq.setContent(msg);
    msgReq.setReplyTo(String.valueOf(evt.message().messageId()));
    return telegramApi.toggleTyping(msgReq.getRecipient())
      .chain(() -> telegramApi.sendMsg(msgReq, fpiUser).replaceWithVoid());
  }

  private TelegramEvt mapToEntity(Update upd) {
    var entity = new TelegramEvt();
    entity.setSenderId(upd.message().from().id());
    entity.setUpdateId(upd.updateId());
    entity.setMessageId(upd.message().messageId());
    entity.setContent(upd.message().text());

    if (upd.message().replyToMessage() != null) {
      entity.setReplyTo(upd.message().replyToMessage().messageId());
    }

    entity.setTimestamp(DateUtil.fromEpoch(upd.message().date() * 1000L));
    entity.setDateCreated(DateUtil.now());
    entity.setDateUpdated(DateUtil.now());
    return entity;
  }

}
