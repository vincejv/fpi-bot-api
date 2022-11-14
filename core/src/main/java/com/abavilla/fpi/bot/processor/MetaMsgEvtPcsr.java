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

import com.abavilla.fpi.bot.entity.meta.MetaMsgEvt;
import com.abavilla.fpi.bot.mapper.meta.MetaMsgEvtMapper;
import com.abavilla.fpi.bot.repo.MetaMsgEvtRepo;
import com.abavilla.fpi.fw.util.DateUtil;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.meta.ext.codec.MetaMsgEvtCodec;
import com.abavilla.fpi.meta.ext.dto.msgr.ext.MetaMsgEvtDto;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.abavilla.fpi.msgr.ext.rest.MsgrReqApi;
import com.abavilla.fpi.telco.ext.enums.BotSource;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class MetaMsgEvtPcsr extends EvtPcsr<MetaMsgEvtDto, MsgrReqApi, MetaMsgEvtRepo, MetaMsgEvt> {

  @Inject
  MetaMsgEvtMapper metaMsgEvtMapper;

  @ConsumeEvent(value = "meta-msg-evt", codec = MetaMsgEvtCodec.class)
  public Uni<Void> process(MetaMsgEvtDto evt) {
    Log.info("Received meta msgr event: " + evt);
    return processEvent(evt);
  }

  @Override
  protected MetaMsgEvt mapToEntity(MetaMsgEvtDto evt) {
    var metaMsgEvt = metaMsgEvtMapper.mapToEntity(evt);
    metaMsgEvt.setDateCreated(DateUtil.now());
    metaMsgEvt.setDateUpdated(DateUtil.now());
    return metaMsgEvt;
  }

  @Override
  protected String getContentFromEvt(MetaMsgEvtDto evt) {
    return evt.getContent();
  }

  @Override
  protected String getEventIdForLogging(MetaMsgEvtDto evt) {
    return evt.getMetaMsgId();
  }

  @Override
  public BotSource getBotSource() {
    return BotSource.FB_MSGR;
  }

  @Override
  public Uni<Void> toggleTyping(String recipient, boolean isTyping) {
    return msgrApi.toggleTyping(recipient, isTyping).replaceWithVoid();
  }

  @Override
  public QueryDto createLoadQueryFromEvt(MetaMsgEvtDto evt) {
    var query = new QueryDto();
    query.setQuery(evt.getContent());
    query.setBotSource(getBotSource().toString());
    return query;
  }

  @Override
  public MsgrMsgReqDto createMsgReqFromEvt(MetaMsgEvtDto evt) {
    var msgReq = new MsgrMsgReqDto();
    msgReq.setRecipient(getSenderFromEvt(evt));
    msgReq.setReplyTo(evt.getMetaMsgId());
    return msgReq;
  }

  @Override
  public String getSenderFromEvt(MetaMsgEvtDto evt) {
    return evt.getSender();
  }

  @Override
  protected Uni<Void> sendMsgrMsg(MsgrMsgReqDto msgReq, String fpiUser) {
    return msgrApi.sendMsg(msgReq, fpiUser).replaceWithVoid();
  }

}
