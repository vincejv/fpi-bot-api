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
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.load.ext.rest.LoadQueryApi;
import com.abavilla.fpi.login.ext.dto.SessionDto;
import com.abavilla.fpi.login.ext.dto.WebhookLoginDto;
import com.abavilla.fpi.login.ext.rest.TrustedLoginApi;
import com.abavilla.fpi.meta.config.codec.MetaMsgEvtCodec;
import com.abavilla.fpi.meta.dto.msgr.ext.MetaMsgEvtDto;
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
    if (StringUtils.isNotBlank(evt.getContent())) {
      // verify if person is registered
      var metaId = evt.getSender();
      WebhookLoginDto login = new WebhookLoginDto();
      login.setUsername(metaId);
      return loginApi.webhookAuthenticate(login).chain(session -> {
          if (StringUtils.equals(session.getStatus(),
            SessionDto.SessionStatus.ESTABLISHED.toString())) {
            var query = new QueryDto();
            query.setQuery(evt.getContent());
            return loadApi.query(query).chain(resp -> {
              return sendMsgrMsg(evt, "Received your query, current status is " + resp.getStatus());
            });
          }
          return sendUnauthorizedMsg(evt, session.getStatus());
        })
        // login failures
        .onFailure().recoverWithUni(throwable -> sendUnauthorizedMsg(evt, throwable.getMessage()))
        .replaceWithVoid();
    }

    return Uni.createFrom().voidItem();
  }

  private Uni<Void> sendUnauthorizedMsg(MetaMsgEvtDto evt, String msg) {
    return sendMsgrMsg(evt, "Unauthorized user" + msg);
  }

  private Uni<Void> sendMsgrMsg(MetaMsgEvtDto evt, String msg) {
    return metaMsgrSvc.sendMsg(msg, evt.getSender()).replaceWithVoid();
  }

}
