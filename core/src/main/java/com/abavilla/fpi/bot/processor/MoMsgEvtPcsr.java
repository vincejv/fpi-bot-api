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

import java.time.temporal.ChronoUnit;

import com.abavilla.fpi.bot.codec.MoEvtDtoCodec;
import com.abavilla.fpi.bot.dto.MOEvtDto;
import com.abavilla.fpi.bot.entity.enums.QueryEvtType;
import com.abavilla.fpi.bot.entity.m360.MOEvt;
import com.abavilla.fpi.bot.mapper.m360.MOEvtMapper;
import com.abavilla.fpi.bot.repo.MOEvtRepo;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.dto.impl.RespDto;
import com.abavilla.fpi.fw.util.DateUtil;
import com.abavilla.fpi.load.ext.dto.QueryDto;
import com.abavilla.fpi.login.ext.dto.SessionDto;
import com.abavilla.fpi.login.ext.dto.UserDto;
import com.abavilla.fpi.login.ext.dto.WebhookLoginDto;
import com.abavilla.fpi.login.ext.entity.ServiceStatus;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.abavilla.fpi.sms.ext.dto.MsgReqDto;
import com.abavilla.fpi.sms.ext.rest.SmsApi;
import com.abavilla.fpi.telco.ext.enums.BotSource;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MoMsgEvtPcsr extends EvtPcsr<MOEvtDto, SmsApi, MOEvtRepo, MOEvt> {

  @Inject
  MOEvtMapper moEvtMapper;

  @ConfigProperty(name = "fpi.webhook.mo.max-delay-sec", defaultValue = "300")
  Integer maxSmsDelay;

  @ConsumeEvent(value = "mo-msg-evt", codec = MoEvtDtoCodec.class)
  public Uni<Void> process(MOEvtDto evt) {
    Log.info("Received MO Msg event: " + evt);
    return processEvent(evt);
  }

  @Override
  protected boolean preProcessEvt(MOEvtDto evt) {
    var moUtcTimestamp = DateUtil.modLdtToUtc(
      DateUtil.parseStrDateToLdt(evt.getTimestamp(), BotConst.M360_TIMESTAMP_FORMAT));
    var now = DateUtil.now();
    var delay = Math.abs(ChronoUnit.SECONDS.between(now, moUtcTimestamp));
    if (delay <= maxSmsDelay) {
      return true;
    } else {
      Log.warn("Dropping MO evt, due to exceeding delay threshold" + evt);
      return false;
    }
  }

  @Override
  protected Uni<Void> postProcessEvt(WebhookLoginDto login, RespDto<SessionDto> session, MOEvtDto evt, QueryDto query,
                                     Uni<Void> queryEvt) {
    var queryType = determineQueryType(query.getQuery());
    return switch (queryType) {
      case KEYW_SUBS -> userApi.getByMobile(login.getUsername()).chain(usrByMobile -> {
        var usrId = usrByMobile.getResp().getId();
        var usr = new UserDto();
        usr.setSvcStatus(ServiceStatus.OPT_IN);
        return userApi.patchById(usrId, usr)
          .chain(() -> sendResponse(evt, session.getResp().getUsername(),
            """
              Thank you for subscribing to FPI Service, with this subscription you agree to our privacy policy at https://florenz.abavilla.com/privacy-policy.

              To opt-out of our service send "STOP" to 225642222"""));
      });
      case KEYW_STOP -> userApi.getByMobile(login.getUsername()).chain(usrByMobile -> {
        var usrId = usrByMobile.getResp().getId();
        var usr = new UserDto();
        usr.setSvcStatus(ServiceStatus.OPT_OUT);
        return userApi.patchById(usrId, usr).chain(() -> sendResponse(evt, session.getResp().getUsername(),
          """
            We are sad to see you go, you will no longer receive any messages from FPI.

            To opt-in with our service again send "SUBSCRIBE" to 225642222"""));
      });
      case KEYW_STATUS -> userApi.getByMobile(login.getUsername()).chain(usrByMobile ->
        sendResponse(evt, session.getResp().getUsername(),
        """
          Your current service status to FPI is: %s

          To opt-in with our service send "SUBSCRIBE", to opt-out, "STOP" to 225642222"""
          .formatted(usrByMobile.getResp().getSvcStatus())));
      default -> queryEvt; // if not a SUB or STOP command, retain load query assumption
    };
  }

  private QueryEvtType determineQueryType(String query) {
    var tokens = StringUtils.split(query.toUpperCase());
    if (tokens.length == 1) {
      return QueryEvtType.fromValue(tokens[0]);
    }
    return QueryEvtType.LOAD_QUERY;
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
