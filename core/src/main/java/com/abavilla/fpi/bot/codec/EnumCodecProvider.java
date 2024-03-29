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

package com.abavilla.fpi.bot.codec;

import com.abavilla.fpi.fw.codec.IEnumCodecProvider;
import com.abavilla.fpi.telco.ext.codec.BotSourceCodec;
import com.abavilla.fpi.telco.ext.codec.TelcoCodec;
import com.abavilla.fpi.telco.ext.enums.BotSource;
import com.abavilla.fpi.telco.ext.enums.Telco;
import com.abavilla.fpi.viber.ext.codec.MessageTypeCodec;
import com.abavilla.fpi.viber.ext.dto.Message;
import org.bson.codecs.Codec;

/**
 * MongoDB Codec registry, contains all the codec for classes that doesn't
 * work by default with default POJO codec for MongoDb driver.
 *
 * @author <a href="mailto:vincevillamora@gmail.com">Vince Villamora</a>
 */
public class EnumCodecProvider implements IEnumCodecProvider {

  @SuppressWarnings("unchecked")
  @Override
  public <T> Codec<T> getCodecProvider(Class<T> tClass) {
    if (tClass == BotSource.class) {
      return (Codec<T>) new BotSourceCodec();
    } else if (tClass == Message.Type.class) {
      return (Codec<T>) new MessageTypeCodec();
    } else if (tClass == Telco.class) {
      return (Codec<T>) new TelcoCodec();
    }
    return null;
  }
}
