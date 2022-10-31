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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.abavilla.fpi.bot.config.MetaApiKeyConfig;
import com.abavilla.fpi.bot.entity.meta.MetaMsgEvt;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.exceptions.FPISvcEx;
import com.abavilla.fpi.fw.service.AbsSvc;
import com.abavilla.fpi.meta.ext.codec.MetaMsgEvtCodec;
import com.abavilla.fpi.meta.ext.dto.MetaHookEvtDto;
import com.abavilla.fpi.meta.ext.dto.msgr.ext.MetaMsgEvtDto;
import com.abavilla.fpi.meta.ext.mapper.MetaHookEvtMapper;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class MetaMsgEvtSvc extends AbsSvc<MetaHookEvtDto, MetaMsgEvt> {

  @Inject
  MetaApiKeyConfig metaApiKeyConfig;

  @Inject
  EventBus bus;

  @Inject
  MetaHookEvtMapper metaHookEvtMapper;

  public Uni<Void> processWebhook(MetaHookEvtDto event) {
    List<MetaMsgEvtDto> metaMsgEvtDtos = metaHookEvtMapper.hookToDtoList(event);
    for (MetaMsgEvtDto dto : metaMsgEvtDtos) {
      bus.send("meta-msg-evt", dto,
        new DeliveryOptions().setCodecName(MetaMsgEvtCodec.class.getName()));
      Log.info("Sent to event bus for processing" + dto.getMetaMsgId());
    }
    return Uni.createFrom().voidItem();
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
