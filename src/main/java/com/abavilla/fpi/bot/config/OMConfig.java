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

import javax.inject.Singleton;

import com.abavilla.fpi.fw.config.IObjectMapperConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contains {@link ObjectMapper} configuration for JSON serialization and deserialization.
 *
 * @author <a href="mailto:vincevillamora@gmail.com">Vince Villamora</a>
 */
@Singleton
public class OMConfig implements IObjectMapperConfig {
  @Override
  public void customizeMapper(ObjectMapper mapper) {
    // no customizations
  }
}
