/*************************************************************************
 * Copyright (C) 2022 Vince Jerald Villamora @ https://vincejv.com       *
 *                                                                       *
 * Licensed under the Apache License, Version 2.0 (the "License");       *
 * you may not use this file except in compliance with the License.      *
 * You may obtain a copy of the License at                               *
 *                                                                       *
 *   http://www.apache.org/licenses/LICENSE-2.0                          *
 *                                                                       *
 * Unless required by applicable law or agreed to in writing, software   *
 * distributed under the License is distributed on an "AS IS" BASIS,     *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or       *
 * implied. See the License for the specific language governing          *
 * permissions and limitations under the License.                        *
 *************************************************************************/

package com.abavilla.fpi.bot.processor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.abavilla.fpi.fw.dto.impl.RespDto;
import com.abavilla.fpi.fw.entity.AbsItem;
import com.abavilla.fpi.fw.exceptions.ApiSvcEx;
import com.abavilla.fpi.fw.repo.AbsMongoRepo;
import com.abavilla.fpi.fw.rest.IApi;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.load.ext.rest.LoadQueryApi;
import com.abavilla.fpi.login.ext.dto.SessionDto;
import com.abavilla.fpi.login.ext.dto.WebhookLoginDto;
import com.abavilla.fpi.login.ext.rest.TrustedLoginApi;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public abstract class EvtPcsr<E, A extends IApi, R extends AbsMongoRepo<I>, I extends AbsItem>
  implements IPcsr<E> {

  @RestClient
  protected TrustedLoginApi loginApi;

  @RestClient
  protected LoadQueryApi loadApi;

  @Inject
  protected R evtLog;

  @RestClient
  protected A msgrApi;

  public Uni<Void> processEvent(E evt) {
    if (StringUtils.isNotBlank(getContentFromEvt(evt))) {
      return toggleTyping(getSenderFromEvt(evt), true).chain(() -> {
          Log.info("Processing event: " + getEventIdForLogging(evt));
          I evtEntity = mapToEntity(evt);
          return evtLog.persist(evtEntity).chain(() -> {
              Log.info("Logged to db: " + getEventIdForLogging(evt));
              // verify if person is registered
              var login = new WebhookLoginDto();
              login.setUsername(getSenderFromEvt(evt));
              login.setBotSource(getBotSource().toString());
              Log.info("Authenticating user: " + login);
              return loginApi.webhookAuthenticate(login)
                // process load
                .chain(session -> processLoadQuery(login, session, evt)
                  //load query exceptions
                  .onFailure(ApiSvcEx.class).recoverWithUni(ex ->
                    handleApiEx(evt, session.getResp().getUsername(), ex)))
                // login failures
                .onFailure(ApiSvcEx.class).recoverWithUni(ex -> handleApiEx(evt, ex));
            })
            .onFailure(ex -> ex instanceof MongoWriteException wEx &&
              wEx.getError().getCategory().equals(ErrorCategory.DUPLICATE_KEY))
            .recoverWithItem(() -> {
              Log.warn("Received duplicate event :: " + getEventIdForLogging(evt));
              return null;
            });
        })
        .chain(() -> toggleTyping(getSenderFromEvt(evt), false)).replaceWithVoid()
        .onFailure().invoke(this::handleMsgEx);
    } else {
      Log.warn("Skipping unsupported event");
    }
    return Uni.createFrom().voidItem();
  }

  protected Uni<Void> processLoadQuery(WebhookLoginDto login, RespDto<SessionDto> session, E evt) {
    Log.info("Authenticated: " + login.getUsername());
    if (StringUtils.equals(session.getStatus(),
      SessionDto.SessionStatus.ESTABLISHED.toString())) {
      var query = createLoadQueryFromEvt(evt);
      return loadApi.query(query, "Bearer " + session.getResp().getAccessToken()).chain(resp -> {
        Log.info("Query received, response is " + resp);
        return sendResponse(evt, session.getResp().getUsername(),
          "Processing your request, status is %s".formatted(resp.getStatus()));
      });
    }
    return sendResponse(evt, session.getResp().getUsername(), session.getStatus());
  }

  protected void handleMsgEx(Throwable sendMsgEx) {
    Log.error("Message sending failed: " + sendMsgEx.getMessage(), sendMsgEx);
  }

  protected Uni<Void> handleApiEx(E evt, Throwable ex) {
    return handleApiEx(evt, null, ex);
  }

  protected Uni<Void> handleApiEx(E evt, String fpiUser, Throwable ex) {
    Log.error("Error while processing evt: " + getEventIdForLogging(evt), ex);
    var apiSvcEx = (ApiSvcEx) ex;
    Uni<Void> handleAction;

    if (!HttpResponseStatus.INTERNAL_SERVER_ERROR.equals(apiSvcEx.getHttpResponseStatus())) {
      var jsonResponse = apiSvcEx.getJsonResponse(RespDto.class);
      if (jsonResponse != null && StringUtils.isNotBlank(jsonResponse.getError())) {
        handleAction = sendResponse(evt, fpiUser, jsonResponse.getError());
      } else {
        handleAction = sendResponse(evt, fpiUser, "Error occurred, please try again");
      }
    } else {
      handleAction = sendResponse(evt, fpiUser, "Error occurred, please try again");
    }

    return handleAction.chain(() -> toggleTyping(
      getSenderFromEvt(evt), false).replaceWithVoid());
  }

  private Uni<Void> sendResponse(E evt, String fpiUser, String msg) {
    Log.info("Sending msgr msg: " + msg + " event: " + getEventIdForLogging(evt));
    var msgReq = createMsgReqFromEvt(evt);
    msgReq.setContent(msg);
    return toggleTyping(msgReq.getRecipient(), true)
      .chain(() -> sendMsgrMsg(msgReq, fpiUser));
  }

  protected abstract Uni<Void> sendMsgrMsg(MsgrMsgReqDto msgReq, String fpiUser);

  protected abstract Uni<Void> toggleTyping(String recipient, boolean isTyping);

  protected abstract I mapToEntity(E evt);

  protected abstract String getContentFromEvt(E evt);

  protected abstract String getEventIdForLogging(E evt);

  protected abstract QueryDto createLoadQueryFromEvt(E evt);

  protected abstract MsgrMsgReqDto createMsgReqFromEvt(E evt);

  protected abstract String getSenderFromEvt(E evt);
}
