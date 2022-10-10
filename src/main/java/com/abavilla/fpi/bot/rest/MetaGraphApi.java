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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.abavilla.fpi.meta.dto.ProfileReqReply;
import com.abavilla.fpi.meta.dto.msgr.MsgrReqReply;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient(configKey = "meta-graph-api")
@Produces(MediaType.APPLICATION_JSON)
public interface MetaGraphApi {

  @POST
  @Path("v15.0/{pageId}/messages")
  Uni<RestResponse<MsgrReqReply>> sendMsgrMsg(
      @PathParam("pageId") String pageId,
      @QueryParam("recipient") String recipient,
      @QueryParam("messaging_type") String type,
      @QueryParam("message") String messageNode,
      @QueryParam("access_token") String token
  );

  @GET
  @Path("{profileId}")
  Uni<RestResponse<ProfileReqReply>> getProfile(
      @PathParam("profileId") String profileId,
      @QueryParam("fields") String fields,
      @QueryParam("access_token") String token
  );
}
