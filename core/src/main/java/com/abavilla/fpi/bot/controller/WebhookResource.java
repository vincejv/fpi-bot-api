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

import com.abavilla.fpi.bot.entity.meta.MetaMsgEvt;
import com.abavilla.fpi.bot.service.WebhookMsgEvtSvc;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.controller.AbsBaseResource;
import com.abavilla.fpi.fw.dto.IDto;
import com.abavilla.fpi.fw.dto.impl.RespDto;
import com.abavilla.fpi.fw.exceptions.FPISvcEx;
import com.abavilla.fpi.fw.util.DateUtil;
import com.abavilla.fpi.fw.util.MapperUtil;
import com.abavilla.fpi.fw.util.SigUtil;
import com.abavilla.fpi.meta.ext.dto.MetaHookEvtDto;
import com.abavilla.fpi.viber.ext.dto.ViberUpdate;
import com.pengrad.telegrambot.BotUtils;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Path("/fpi/webhook")
public class WebhookResource extends AbsBaseResource<MetaHookEvtDto, MetaMsgEvt, WebhookMsgEvtSvc> {

  /**
   * Facebook app secret, used in HMAC signature checking
   */
  @ConfigProperty(name = "com.meta.facebook.app-secret")
  String metaAppSecret;

  @ConfigProperty(name = "com.viber.auth-token")
  String viberAuthToken;

  @ConfigProperty(name = "fpi.webhook.mo.api-key")
  String moWebhookApiKey;

  @Path("msgr")
  @POST
  public Uni<Void> receiveEventFromMsgr(
      @RestHeader("X-Hub-Signature-256") String signatureHdr,
      String body) {
    var signature = StringUtils.removeStart(signatureHdr, BotConst.HTTP_HDR_SHA256);
    if (StringUtils.isNotBlank(signatureHdr) && SigUtil.validateSignature(body, metaAppSecret, signature)) {
      service.processWebhook(MapperUtil.readJson(body, MetaHookEvtDto.class));
      return Uni.createFrom().voidItem();
    } else {
      Log.warn("Signature check failed for META: payload: " + body + " meta signature: " + signatureHdr);
      throw new FPISvcEx(StringUtils.EMPTY, Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Path("msgr")
  @GET
  public Uni<String> verifyMsgr(
      @QueryParam("hub.mode") String mode,
      @QueryParam("hub.verify_token") String verifyToken,
      @QueryParam("hub.challenge") String challenge) {
    return service.verifyWebhook(mode, verifyToken, challenge);
  }

  @POST
  @Path("telegram")
  public Uni<Void> receiveEventFromTelegram(
    String evtJsonStr, @HeaderParam("X-Telegram-Bot-Api-Secret-Token") String secretToken
  ) {
    if (StringUtils.equals(secretToken, secretToken)) {
      var evt = BotUtils.parseUpdate(evtJsonStr);
      service.processWebhook(evt);
      return Uni.createFrom().voidItem();
    }
    throw new FPISvcEx("Unauthorized secret token", RestResponse.StatusCode.FORBIDDEN);
  }

  @Path("viber")
  @POST
  public Uni<RespDto<IDto>> receiveEventFromViber(
    @RestHeader("X-Viber-Content-Signature") String signature,
    String body) {
    if (StringUtils.isNotBlank(signature) && SigUtil.validateSignature(body, viberAuthToken, signature)) {
      service.processWebhook(MapperUtil.readJson(body, ViberUpdate.class));
      return Uni.createFrom().item(() -> {
        var resp = new RespDto<>();
        resp.setTimestamp(DateUtil.nowAsStr());
        resp.setStatus("received");
        return resp;
      });
    }
    Log.warn("Signature check failed for Viber: payload: " + body + " Viber signature: " + signature);
    throw new FPISvcEx("Signature check failed: payload: " + body + " signature: " + signature, Response.Status.FORBIDDEN.getStatusCode());
  }

  @Path("sms/mo/{key}")
  @GET
  public Uni<RespDto<IDto>> receiveEventFromMOWebhook(
    @PathParam("key") String appKey, @QueryParam("transid") String transactionId,
    @QueryParam("msisdn") String mobileNo, @QueryParam("message") String message,
    @QueryParam("timestamp") String timestamp, @QueryParam("from") String acSource,
    @QueryParam("msgcount") Integer msgSegment, @QueryParam("telco_id") Integer telco) {
    if (StringUtils.equals(moWebhookApiKey, appKey)) {
      service.processWebhook(transactionId, mobileNo, message, timestamp, acSource, msgSegment, telco);
      return Uni.createFrom().item(() -> {
        var resp = new RespDto<>();
        resp.setTimestamp(DateUtil.nowAsStr());
        resp.setStatus("received mo event");
        return resp;
      });
    }
    Log.warn("Restricted MO Webhook key: " + appKey);
    throw new FPISvcEx("Restricted MO Webhook key", Response.Status.FORBIDDEN.getStatusCode());
  }

  /**
   * {@inheritDoc}
   */
  @ServerExceptionMapper
  @Override
  public RestResponse<RespDto<IDto>> mapException(FPISvcEx x) {
    return super.mapException(x);
  }

}
