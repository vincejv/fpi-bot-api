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
import com.abavilla.fpi.bot.rest.MetaGraphApi;
import com.abavilla.fpi.meta.dto.msgr.MsgDtlDto;
import com.abavilla.fpi.meta.dto.msgr.ProfileDto;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MetaMsgrSvc {

  @RestClient
  MetaGraphApi metaGraphApi;

  @Inject
  MetaApiKeyConfig metaApiKeyConfig;

  public Uni<Void> sendMsg(String msg, String recipientId) {
    ProfileDto recipient = new ProfileDto();
    recipient.setId(recipientId);
    MsgDtlDto msgDtl = new MsgDtlDto();
    msgDtl.setText(msg);

    return metaGraphApi.sendMsgrMsg(metaApiKeyConfig.getPageId(),
        recipient.toString(),
        "RESPONSE",
        msgDtl.toString(),
        metaApiKeyConfig.getPageAccessToken()).replaceWithVoid();
  }

}
