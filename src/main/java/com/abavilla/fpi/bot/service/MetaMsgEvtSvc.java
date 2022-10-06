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
import com.abavilla.fpi.bot.mapper.meta.MetaMsgEvtMapper;
import com.abavilla.fpi.bot.repo.MetaMsgEvtRepo;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.exceptions.FPISvcEx;
import com.abavilla.fpi.fw.service.AbsRepoSvc;
import com.abavilla.fpi.fw.util.DateUtil;
import com.abavilla.fpi.meta.config.codec.MetaMsgEvtCodec;
import com.abavilla.fpi.meta.dto.MetaHookEvtDto;
import com.abavilla.fpi.meta.dto.msgr.MetaMsgEvtDto;
import com.abavilla.fpi.meta.mapper.MetaHookEvtMapper;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class MetaMsgEvtSvc extends AbsRepoSvc<MetaHookEvtDto, MetaMsgEvt, MetaMsgEvtRepo> {

  @Inject
  MetaApiKeyConfig metaApiKeyConfig;

  @Inject
  EventBus bus;

  @Inject
  MetaHookEvtMapper metaHookEvtMapper;

  @Inject
  MetaMsgEvtMapper metaMsgEvtMapper;

  public Uni<Void> processWebhook(MetaHookEvtDto event) {
    List<MetaMsgEvtDto> metaMsgEvtDtos = metaHookEvtMapper.hookToDtoList(event);
    List<MetaMsgEvt> entities = metaMsgEvtDtos.stream()
        .map(dto -> {
          bus.send("meta-msg-evt", dto,
              new DeliveryOptions().setCodecName(MetaMsgEvtCodec.class.getName()));
          Log.info("Sent to event bus " + dto.getMetaMsgId());
          MetaMsgEvt metaMsgEvt = metaMsgEvtMapper.mapToEntity(dto);
          metaMsgEvt.setDateCreated(DateUtil.now());
          metaMsgEvt.setDateUpdated(DateUtil.now());
          return metaMsgEvt;
        }).toList();

    return repo.persist(entities).replaceWithVoid();

//    for (EntryDto entryDto : event.getEntry()) {
//      for (MessagingDto messagingDto : entryDto.getMessaging()) {
//        var login = new WebhookLoginDto();
//        login.setUsername(messagingDto.getSender().getId());
//        var recipient = String.format("{'id':'%s'}", login.getUsername());
//        loginApi.webhookAuthenticate(login).chain(resp->{
//          if (resp.getStatusInfo().getStatusCode() ==
//              Response.Status.OK.getStatusCode()) {
//
//          } else {
//            Log.warn("Webhook login status: " + resp.getStatusInfo().getStatusCode());
//          }
//          return metaGraphApi.getProfile( login.getUsername(), "name", pageAccessToken)
//              .onFailure().retry().withBackOff(
//                  Duration.ofSeconds(3)).withJitter(0.2)
//              .atMost(5)
//              .onFailure().recoverWithItem(t-> RestResponse.ok("Unable to retrieve name"))
//              .chain(profileGet ->
//                  metaGraphApi.sendMsgrMsg(pageId, recipient, "RESPONSE",
//                          String.format("{'text':'Pending registration for %s'}", profileGet.getEntity()),
//                          pageAccessToken).onFailure().retry().withBackOff(
//                          Duration.ofSeconds(3)).withJitter(0.2)
//                      .atMost(5));
//        }).subscribe().with(log-> Log.info("Sent message:: " + log.getEntity()));
//      }
//    }
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
