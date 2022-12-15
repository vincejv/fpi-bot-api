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

import com.abavilla.fpi.bot.codec.ViberUpdateEvtCodec;
import com.abavilla.fpi.bot.entity.viber.ViberEvt;
import com.abavilla.fpi.bot.mapper.viber.ViberEvtMapper;
import com.abavilla.fpi.bot.repo.ViberEvtRepo;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.abavilla.fpi.msgr.ext.rest.ViberReqApi;
import com.abavilla.fpi.telco.ext.enums.BotSource;
import com.abavilla.fpi.viber.ext.dto.ViberUpdate;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class ViberMsgEvtPcsr extends EvtPcsr<ViberUpdate, ViberReqApi, ViberEvtRepo, ViberEvt> {

  @Inject
  ViberEvtMapper mapper;

  @ConsumeEvent(value = "viber-msg-evt", codec = ViberUpdateEvtCodec.class)
  public Uni<Void> process(ViberUpdate evt) {
    Log.info("Received viber update event: " + evt);
    return processEvent(evt);
  }

  @Override
  protected Uni<Void> sendMsgrMsg(MsgrMsgReqDto msgReq, String fpiUser) {
    return msgrApi.sendMsg(msgReq, fpiUser).replaceWithVoid();
  }

  @Override
  protected Uni<Void> toggleTyping(String recipient, boolean isTyping) {
    // viber doesn't have an API for typing
    return Uni.createFrom().voidItem();
  }

  @Override
  protected ViberEvt mapToEntity(ViberUpdate evt) {
    return mapper.mapToEntity(evt);
  }

  @Override
  protected String getContentFromEvt(ViberUpdate evt) {
    return evt.getMessage().getText();
  }

  @Override
  protected String getEventIdForLogging(ViberUpdate evt) {
    return String.valueOf(evt.getMessageToken());
  }

  @Override
  protected QueryDto createLoadQueryFromEvt(ViberUpdate evt) {
    var query = new QueryDto();
    query.setQuery(evt.getMessage().getText());
    query.setBotSource(getBotSource().toString());
    return query;
  }

  @Override
  protected MsgrMsgReqDto createMsgReqFromEvt(ViberUpdate evt) {
    return mapper.createMsgReqFromUpdate(evt);
  }

  @Override
  protected String getSenderFromEvt(ViberUpdate evt) {
    return evt.getSender() == null ? evt.getUserId() : evt.getSender().getId();
  }

  @Override
  protected Uni<String> getFriendlyUserName(ViberUpdate evt) {
    return msgrApi.getUserDtls(getSenderFromEvt(evt), StringUtils.EMPTY)
      .map(resp -> resp.getResp().getUser().getName());
  }

  @Override
  public BotSource getBotSource() {
    return BotSource.VIBER;
  }
}
