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

package com.abavilla.fpi.bot.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.abavilla.fpi.bot.dto.auth.LoginDto;
import com.abavilla.fpi.bot.dto.auth.SessionDto;
import com.abavilla.fpi.bot.dto.auth.WebhookLoginDto;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient(configKey = "login-api")
public interface LoginApi {

  /**
   * Obtain a session token from authentication service.
   * @param login {@link LoginDto} object
   *
   * @return {@link SessionDto} object containing the session info
   */
  @POST
  Uni<SessionDto> authenticate(LoginDto login);

  /**
   * Perform a trusted login
   * @param login Login credentials
   * @return
   */
  @POST
  @Path("trusted")
  Uni<RestResponse<Void>> webhookAuthenticate(WebhookLoginDto login);
}
