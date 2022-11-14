/*************************************************************************
 * FPI Application - Abavilla                                            *
 * Copyright (C) 2022  Vince Jerald Villamora                            *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.*
 *************************************************************************/

package com.abavilla.fpi.bot.mapper.viber;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.abavilla.fpi.bot.entity.viber.ViberEvt;
import com.abavilla.fpi.fw.mapper.IDtoToEntityMapper;
import com.abavilla.fpi.fw.util.DateUtil;
import com.abavilla.fpi.msgr.ext.dto.MsgrMsgReqDto;
import com.abavilla.fpi.viber.ext.dto.ViberUpdate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ViberEvtMapper extends IDtoToEntityMapper<ViberUpdate, ViberEvt> {

  @Mapping(target = "content", source = "message.text")
  @Mapping(target = "recipient", source = "sender.id")
  @Mapping(target = "replyTo", source = "message.trackingData")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateUpdated", ignore = true)
  MsgrMsgReqDto createMsgReqFromUpdate(ViberUpdate update);

  default LocalDateTime timestampToLdt(Long timestamp) {
    if (timestamp == null) {
      return null;
    }
    return DateUtil.fromEpoch(timestamp);
  }

  default Long ldtToTimestamp(LocalDateTime timestamp) {
    if (timestamp == null) {
      return null;
    }
    return timestamp.toEpochSecond(ZoneOffset.UTC);
  }

}
