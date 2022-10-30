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

import com.abavilla.fpi.bot.service.MetaMsgrSvc;
import com.abavilla.fpi.fw.dto.impl.RespDto;
import com.abavilla.fpi.fw.exceptions.ApiSvcEx;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.load.ext.rest.LoadQueryApi;
import com.abavilla.fpi.login.ext.dto.SessionDto;
import com.abavilla.fpi.login.ext.dto.WebhookLoginDto;
import com.abavilla.fpi.login.ext.rest.TrustedLoginApi;
import com.abavilla.fpi.meta.ext.codec.MetaMsgEvtCodec;
import com.abavilla.fpi.meta.ext.dto.msgr.ext.MetaMsgEvtDto;
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
  MetaMsgrSvc metaMsgrSvc;

  @ConsumeEvent(value = "meta-msg-evt", codec = MetaMsgEvtCodec.class)
  public Uni<Void> process(MetaMsgEvtDto evt) {
    Log.info("Processing event: " + evt);
    if (StringUtils.isNotBlank(evt.getContent())) {
      // verify if person is registered
      var metaId = evt.getSender();
      WebhookLoginDto login = new WebhookLoginDto();
      login.setUsername(metaId);
      Log.info("Authenticating user: " + login.getUsername());
      return loginApi.webhookAuthenticate(login).chain(session -> {
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
        })
        // login failures
        .onFailure(ApiSvcEx.class).recoverWithUni(apiSvcEx -> {
          Log.error("Error while processing evt: " + evt, apiSvcEx);
          return sendUnauthorizedMsg(evt,
            ((ApiSvcEx) apiSvcEx).getJsonResponse(RespDto.class).getError());
        })
        .onFailure().recoverWithItem(sendMsgEx -> {
          Log.info("Message sending failed: " + sendMsgEx.getMessage(), sendMsgEx);
          return null;
        })
        .replaceWithVoid();
    }

    return Uni.createFrom().voidItem();
  }

  private Uni<Void> sendUnauthorizedMsg(MetaMsgEvtDto evt, String msg) {
    return sendMsgrMsg(evt, "Unauthorized user: " + msg);
  }

  private Uni<Void> sendMsgrMsg(MetaMsgEvtDto evt, String msg) {
    Log.info("Sending msgr msg: " + msg + " event: " + evt);
    return metaMsgrSvc.sendMsg(msg, evt.getSender()).replaceWithVoid();
  }

}
