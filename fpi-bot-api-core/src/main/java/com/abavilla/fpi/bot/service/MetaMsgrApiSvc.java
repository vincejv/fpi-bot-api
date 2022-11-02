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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.abavilla.fpi.bot.config.MetaApiKeyConfig;
import com.abavilla.fpi.bot.entity.enums.SenderAction;
import com.abavilla.fpi.bot.rest.MetaGraphApi;
import com.abavilla.fpi.fw.exceptions.FPISvcEx;
import com.abavilla.fpi.fw.rest.AbsApiSvc;
import com.abavilla.fpi.meta.ext.dto.msgr.MsgDtlDto;
import com.abavilla.fpi.meta.ext.dto.msgr.MsgrReqReply;
import com.abavilla.fpi.meta.ext.dto.msgr.ProfileDto;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class MetaMsgrApiSvc extends AbsApiSvc<MetaGraphApi> {

  @Inject
  MetaApiKeyConfig metaApiKeyConfig;

  public Uni<MsgrReqReply> sendMsg(String msg, String recipientId) {
    var recipient = new ProfileDto();
    recipient.setId(recipientId);
    var msgDtl = new MsgDtlDto();
    msgDtl.setText(msg);

    return client.sendMsgrMsg(
        metaApiKeyConfig.getPageId(),
        recipient.toJsonStr(),
        "RESPONSE",
        msgDtl.toJsonStr(),
        metaApiKeyConfig.getPageAccessToken()
    ).map(this::mapResponse);
  }

  public Uni<MsgrReqReply> sendTypingIndicator(String recipientId, boolean isTyping) {
    var recipient = new ProfileDto();
    recipient.setId(recipientId);
    return client.sendTypingIndicator(
      metaApiKeyConfig.getPageId(),
      recipient.toJsonStr(),
      isTyping ? SenderAction.TYPING_ON.getValue() : SenderAction.TYPING_OFF.getValue(),
      "RESPONSE",
      metaApiKeyConfig.getPageAccessToken()
    ).map(this::mapResponse);
  }

  private MsgrReqReply mapResponse(RestResponse<MsgrReqReply> resp) {
    if (resp.getStatus() == RestResponse.StatusCode.OK) {
      return resp.getEntity();
    } else {
      throw new FPISvcEx("Unable to send messenger reply");
    }
  }

}
