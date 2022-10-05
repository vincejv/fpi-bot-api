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

package com.abavilla.fpi.bot.controller;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.abavilla.fpi.bot.entity.meta.MetaMsgEvt;
import com.abavilla.fpi.bot.service.MetaMsgEvtSvc;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.controller.AbsBaseResource;
import com.abavilla.fpi.fw.exceptions.FPISvcEx;
import com.abavilla.fpi.fw.util.MapperUtil;
import com.abavilla.fpi.fw.util.SigUtil;
import com.abavilla.fpi.meta.dto.MetaHookEvtDto;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestHeader;

@Path("/fpi/webhook")
public class WebhookResource extends AbsBaseResource<MetaHookEvtDto, MetaMsgEvt, MetaMsgEvtSvc> {

  /**
   * Facebook app secret, used in HMAC signature checking
   */
  @ConfigProperty(name = "com.meta.facebook.app-secret")
  String webhookSecret;

  @Path("msgr")
  @POST
  public Uni<Void> receiveEvent(
      @RestHeader("X-Hub-Signature-256") String signatureHdr,
      String body) {
    var signature = StringUtils.removeStart(signatureHdr, BotConst.HTTP_HDR_SHA256);
    if (StringUtils.isNotBlank(signatureHdr) && SigUtil.validateSignature(body, webhookSecret, signature)) {
      return service.processWebhook(MapperUtil.readJson(body, MetaHookEvtDto.class));
    } else {
      Log.warn("Signature check failed: payload: " + body + " signature: " + signatureHdr);
      throw new FPISvcEx(StringUtils.EMPTY, Response.Status.FORBIDDEN.getStatusCode());
    }
  }
}
