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

import com.abavilla.fpi.bot.codec.MoEvtDtoCodec;
import com.abavilla.fpi.bot.dto.MOEvtDto;
import com.abavilla.fpi.bot.entity.m360.MOEvt;
import com.abavilla.fpi.bot.mapper.m360.MOEvtMapper;
import com.abavilla.fpi.bot.repo.MOEvtRepo;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.abavilla.fpi.sms.ext.dto.MsgReqDto;
import com.abavilla.fpi.sms.ext.rest.SmsApi;
import com.abavilla.fpi.telco.ext.enums.BotSource;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class MoMsgEvtPcsr extends EvtPcsr<MOEvtDto, SmsApi, MOEvtRepo, MOEvt> {

  @Inject
  MOEvtMapper moEvtMapper;

  @ConsumeEvent(value = "mo-msg-evt", codec = MoEvtDtoCodec.class)
  public Uni<Void> process(MOEvtDto evt) {
    Log.info("Received MO Msg event: " + evt);
    return processEvent(evt);
  }

  @Override
  public BotSource getBotSource() {
    return BotSource.SMS;
  }

  @Override
  protected Uni<Void> sendMsgrMsg(MsgrMsgReqDto msgReq, String fpiUser) {
    var mappedMsgReq = new MsgReqDto();
    mappedMsgReq.setContent(msgReq.getContent());
    mappedMsgReq.setMobileNumber(msgReq.getRecipient());
    return msgrApi.sendSms(mappedMsgReq).replaceWithVoid();
  }

  @Override
  protected Uni<Void> toggleTyping(String recipient, boolean isTyping) {
    // SMS Api doesn't have an API for typing
    return Uni.createFrom().voidItem();
  }

  @Override
  protected MOEvt mapToEntity(MOEvtDto evt) {
    return moEvtMapper.mapToEntity(evt);
  }

  @Override
  protected String getContentFromEvt(MOEvtDto evt) {
    return evt.getMessage();
  }

  @Override
  protected String getEventIdForLogging(MOEvtDto evt) {
    return evt.getTransactionId();
  }

  @Override
  protected QueryDto createLoadQueryFromEvt(MOEvtDto evt) {
    var queryDto = new QueryDto();
    queryDto.setQuery(evt.getMessage());
    queryDto.setBotSource(getBotSource().toString());
    return queryDto;
  }

  @Override
  protected MsgrMsgReqDto createMsgReqFromEvt(MOEvtDto evt) {
    var msgrMsgReqDto = new MsgrMsgReqDto();
    msgrMsgReqDto.setContent(evt.getMessage());
    msgrMsgReqDto.setRecipient(evt.getMobileNo());
    msgrMsgReqDto.setReplyTo(evt.getTransactionId());
    return msgrMsgReqDto;
  }

  @Override
  protected String getSenderFromEvt(MOEvtDto evt) {
    return evt.getMobileNo();
  }

  @Override
  protected Uni<String> getFriendlyUserName(MOEvtDto evt) {
    return Uni.createFrom().item(getSenderFromEvt(evt));
  }
}
