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

import com.abavilla.fpi.bot.codec.TGUpdateEvtCodec;
import com.abavilla.fpi.bot.entity.telegram.TelegramEvt;
import com.abavilla.fpi.bot.repo.TgEvtRepo;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.util.DateUtil;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.abavilla.fpi.msgr.ext.rest.TelegramReqApi;
import com.abavilla.fpi.telco.ext.enums.BotSource;
import com.pengrad.telegrambot.model.Update;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class TgMsgEvtPcsr extends EvtPcsr<Update, TelegramReqApi, TgEvtRepo, TelegramEvt> {

  @ConsumeEvent(value = "telegram-msg-evt", codec = TGUpdateEvtCodec.class)
  public Uni<Void> process(Update evt) {
    Log.info("Received telegram update event: " + evt);
    return processEvent(evt);
  }

  @Override
  public BotSource getBotSource() {
    return BotSource.TELEGRAM;
  }

  @Override
  public Uni<Void> toggleTyping(String recipient, boolean isTyping) {
    if (isTyping) {
      return msgrApi.toggleTyping(recipient).replaceWithVoid();
    }
    return Uni.createFrom().voidItem();
  }

  @Override
  public QueryDto createLoadQueryFromEvt(Update evt) {
    var query = new QueryDto();
    query.setQuery(evt.message().text());
    query.setBotSource(getBotSource().toString());
    return query;
  }

  @Override
  public MsgrMsgReqDto createMsgReqFromEvt(Update evt) {
    var msgReq = new MsgrMsgReqDto();
    msgReq.setRecipient(getSenderFromEvt(evt));
    msgReq.setReplyTo(String.valueOf(evt.message().messageId()));
    return msgReq;
  }

  @Override
  public String getSenderFromEvt(Update evt) {
    return String.valueOf(evt.message().from().id());
  }

  @Override
  protected Uni<String> getFriendlyUserName(Update evt) {
    return Uni.createFrom().item(() -> {
      String fname = evt.message().from().firstName();
      String lname = evt.message().from().lastName();
      String friendlyName = StringUtils.EMPTY;
      if (StringUtils.isNotBlank(fname) && !StringUtils.equals(fname, BotConst.NULL_STR)) {
        friendlyName += fname;
      }
      if (StringUtils.isNotBlank(lname) && !StringUtils.equals(lname, BotConst.NULL_STR)) {
        friendlyName += StringUtils.SPACE + lname;
      }
      if (StringUtils.isBlank(friendlyName)) {
        friendlyName = evt.message().from().username();
      }
      return friendlyName;
    });
  }

  @Override
  protected Uni<Void> sendMsgrMsg(MsgrMsgReqDto msgReq, String fpiUser) {
    return msgrApi.sendMsg(msgReq, fpiUser).replaceWithVoid();
  }

  @Override
  protected TelegramEvt mapToEntity(Update upd) {
    var entity = new TelegramEvt();
    entity.setSenderId(upd.message().from().id());
    entity.setUpdateId(upd.updateId());
    entity.setMessageId(upd.message().messageId());
    entity.setContent(upd.message().text());

    if (upd.message().replyToMessage() != null) {
      entity.setReplyTo(upd.message().replyToMessage().messageId());
    }

    entity.setTimestamp(DateUtil.fromEpoch(upd.message().date() * 1000L));
    entity.setDateCreated(DateUtil.now());
    entity.setDateUpdated(DateUtil.now());
    return entity;
  }

  @Override
  protected String getContentFromEvt(Update evt) {
    return evt.message().text();
  }

  @Override
  protected String getEventIdForLogging(Update evt) {
    return "updateId: %d, senderId: %d, messageId: %d"
      .formatted(evt.updateId(), evt.message().from().id(), evt.message().messageId());
  }

}
