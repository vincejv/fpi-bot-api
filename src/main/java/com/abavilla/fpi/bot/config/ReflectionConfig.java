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

package com.abavilla.fpi.bot.config;

import com.abavilla.fpi.fw.config.BaseReflectionConfig;
import com.abavilla.fpi.meta.dto.MetaHookEvtDto;
import com.abavilla.fpi.meta.dto.ProfileReqReply;
import com.abavilla.fpi.meta.dto.msgr.EntryDto;
import com.abavilla.fpi.meta.dto.msgr.MessagingDto;
import com.abavilla.fpi.meta.dto.msgr.MsgAttchmtDto;
import com.abavilla.fpi.meta.dto.msgr.MsgDtlDto;
import com.abavilla.fpi.meta.dto.msgr.MsgrReqReply;
import com.abavilla.fpi.meta.dto.msgr.ProfileDto;
import com.abavilla.fpi.meta.dto.msgr.QuickReplyDto;
import com.abavilla.fpi.meta.dto.msgr.ReferralDto;
import com.abavilla.fpi.meta.dto.msgr.ext.MetaMsgEvtAttchmtDto;
import com.abavilla.fpi.meta.dto.msgr.ext.MetaMsgEvtDto;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Classes to register for reflection for Quarkus native image.
 *
 * @author <a href="mailto:vincevillamora@gmail.com">Vince Villamora</a>
 */
@RegisterForReflection(targets = {
  MetaMsgEvtAttchmtDto.class,
  MetaMsgEvtDto.class,
  EntryDto.class,
  MessagingDto.class,
  MsgAttchmtDto.class,
  MsgDtlDto.class,
  MsgrReqReply.class,
  ProfileDto.class,
  QuickReplyDto.class,
  ReferralDto.class,
  MetaHookEvtDto.class,
  ProfileReqReply.class,
})
public class ReflectionConfig extends BaseReflectionConfig {
}
