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

package com.abavilla.fpi.bot.util;

public final class BotConst {

  public static final String META_HUB_MODE_SUBSCRIBE = "subscribe";

  /**
   * SHA-256 HMAC algorithm from http header
   */
  public static final String HTTP_HDR_SHA256 = "sha256=";

  public static final String M360_TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

  public static final String PH_REGION_CODE = "PH";

  public static final String NULL_STR = "null";

  private BotConst() {}
}
