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

package com.abavilla.fpi.bot.mapper.m360;

import java.time.LocalDateTime;

import com.abavilla.fpi.bot.dto.MOEvtDto;
import com.abavilla.fpi.bot.entity.m360.MOEvt;
import com.abavilla.fpi.bot.util.BotConst;
import com.abavilla.fpi.fw.mapper.IDtoToEntityMapper;
import com.abavilla.fpi.fw.util.DateUtil;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MOEvtMapper extends IDtoToEntityMapper<MOEvtDto, MOEvt> {

  default LocalDateTime parseTimestamp(String timestamp) {
    return DateUtil.modLdtToUtc(DateUtil.parseStrDateToLdt(timestamp, BotConst.M360_TIMESTAMP_FORMAT));
  }

}
