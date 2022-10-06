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

import com.abavilla.fpi.bot.entity.meta.MetaMsgEvt;
import com.abavilla.fpi.bot.mapper.meta.MetaMsgEvtMapper;
import com.abavilla.fpi.bot.repo.MetaMsgEvtRepo;
import com.abavilla.fpi.bot.rest.LoginApi;
import com.abavilla.fpi.bot.rest.MetaGraphApi;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.exceptions.FPISvcEx;
import com.abavilla.fpi.fw.service.AbsRepoSvc;
import com.abavilla.fpi.meta.dto.MetaHookEvtDto;
import com.abavilla.fpi.meta.dto.msgr.MetaMsgEvtDto;
import com.abavilla.fpi.meta.mapper.MetaHookEvtMapper;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MetaMsgEvtSvc extends AbsRepoSvc<MetaHookEvtDto, MetaMsgEvt, MetaMsgEvtRepo> {

  /**
   * API Key for verification handshake between Meta and FPI
   */
  @ConfigProperty(name = "com.meta.facebook.verify-token")
  String authorizedToken;

  @ConfigProperty(name = "com.meta.facebook.page-access-token")
  String pageAccessToken;

  @ConfigProperty(name = "com.meta.facebook.page-id")
  String pageId;

  @RestClient
  LoginApi loginApi;

  @RestClient
  MetaGraphApi metaGraphApi;

  @Inject
  MetaHookEvtMapper metaHookEvtMapper;

  @Inject
  MetaMsgEvtMapper metaMsgEvtMapper;

  public Uni<Void> processWebhook(MetaHookEvtDto event) {
    List<MetaMsgEvtDto> metaMsgEvtDtos = metaHookEvtMapper.hookToDtoList(event);
    List<MetaMsgEvt> entities = metaMsgEvtDtos.stream()
        .map(dto -> metaMsgEvtMapper.mapToEntity(dto)).toList();
    repo.persist(entities).subscribe().with(ignored->{});

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
        StringUtils.equals(verifyToken, authorizedToken)) {
      // authorized
      return Uni.createFrom().item(challenge);
    } else {
      throw new FPISvcEx("Unauthorized webhook access",
          Response.Status.FORBIDDEN.getStatusCode());
    }
  }

}
