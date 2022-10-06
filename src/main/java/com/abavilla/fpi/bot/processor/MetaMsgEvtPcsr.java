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

import com.abavilla.fpi.bot.service.MetaMsgrSvc;
import com.abavilla.fpi.meta.config.codec.MetaMsgEvtCodec;
import com.abavilla.fpi.meta.dto.msgr.MetaMsgEvtDto;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;

@ApplicationScoped
public class MetaMsgEvtPcsr {

  @Inject
  MetaMsgrSvc metaMsgrSvc;

  @ConsumeEvent(value = "meta-msg-evt", codec = MetaMsgEvtCodec.class, blocking = true)
  public void process(MetaMsgEvtDto evt) {
    Log.info("Echoing: " + evt);
    metaMsgrSvc.sendMsg(evt.getContent(), evt.getRecipient()).await().indefinitely();
    Log.info("Sent: " + evt.getMetaMsgId());
  }

}
