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

package com.abavilla.fpi.bot.service;

import java.util.List;

import com.abavilla.fpi.bot.codec.MoEvtDtoCodec;
import com.abavilla.fpi.bot.codec.TGUpdateEvtCodec;
import com.abavilla.fpi.bot.codec.ViberUpdateEvtCodec;
import com.abavilla.fpi.bot.config.MetaApiKeyConfig;
import com.abavilla.fpi.bot.dto.MOEvtDto;
import com.abavilla.fpi.bot.entity.meta.MetaMsgEvt;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.exceptions.FPISvcEx;
import com.abavilla.fpi.fw.service.AbsSvc;
import com.abavilla.fpi.meta.ext.codec.MetaMsgEvtCodec;
import com.abavilla.fpi.meta.ext.dto.MetaHookEvtDto;
import com.abavilla.fpi.meta.ext.dto.msgr.ext.MetaMsgEvtDto;
import com.abavilla.fpi.meta.ext.mapper.MetaHookEvtMapper;
import com.abavilla.fpi.telco.ext.enums.Telco;
import com.abavilla.fpi.viber.ext.dto.ViberUpdate;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.pengrad.telegrambot.model.Update;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class WebhookMsgEvtSvc extends AbsSvc<MetaHookEvtDto, MetaMsgEvt> {

  @Inject
  MetaApiKeyConfig metaApiKeyConfig;

  @Inject
  EventBus bus;

  @Inject
  MetaHookEvtMapper metaHookEvtMapper;

  @Inject
  PhoneNumberUtil phoneNumberUtil;

  public void processWebhook(MetaHookEvtDto event) {
    List<MetaMsgEvtDto> metaMsgEvtDtos = metaHookEvtMapper.hookToDtoList(event);
    for (MetaMsgEvtDto dto : metaMsgEvtDtos) {
      bus.send("meta-msg-evt", dto,
        new DeliveryOptions().setCodecName(MetaMsgEvtCodec.class.getName()));
      Log.info("Sent to meta event bus for processing: " + dto.getMetaMsgId());
    }
  }

  public void processWebhook(Update event) {
    bus.send("telegram-msg-evt", event,
      new DeliveryOptions().setCodecName(TGUpdateEvtCodec.class.getName()));
    Log.info("Sent to telegram event bus for processing: " + event.updateId());
  }

  public void processWebhook(ViberUpdate event) {
    bus.send("viber-msg-evt", event,
      new DeliveryOptions().setCodecName(ViberUpdateEvtCodec.class.getName()));
    Log.info("Sent to viber event bus for processing: " + event.getMessageToken());
  }

  public void processWebhook(String transactionId, String mobileNo, String message,
    String timestamp, String acSource, Integer msgSegment, Integer telco
  ) {
    var moEvt = new MOEvtDto();
    moEvt.setTransactionId(transactionId);
    moEvt.setMobileNo(convertToEi64(mobileNo));
    moEvt.setMessage(message);
    moEvt.setAcSource(acSource);
    moEvt.setMsgSegment(msgSegment);
    moEvt.setTimestamp(timestamp);
    moEvt.setTelco(Telco.fromId(telco));
    bus.send("mo-msg-evt", moEvt,
      new DeliveryOptions().setCodecName(MoEvtDtoCodec.class.getName()));
    Log.info("Sent to MO event bus for processing: " + moEvt.getTransactionId());
  }

  @SneakyThrows
  private String convertToEi64(String mobile) {
    var number = phoneNumberUtil.parse(mobile, BotConst.PH_REGION_CODE);
    return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
  }


  /**
   * Verify webhook access to meta.
   *
   * @param mode Webhook Mode
   * @param verifyToken Token to verify authenticity of request
   * @param challenge Challenge request
   * @return {@link String} Challenge response
   */
  public Uni<String> verifyWebhook(
      String mode, String verifyToken, String challenge) {
    if (StringUtils.equals(mode, BotConst.META_HUB_MODE_SUBSCRIBE) &&
        StringUtils.equals(verifyToken, metaApiKeyConfig.getAuthorizedToken())) {
      // authorized
      return Uni.createFrom().item(challenge);
    } else {
      throw new FPISvcEx("Unauthorized webhook access",
          Response.Status.FORBIDDEN.getStatusCode());
    }
  }

}
