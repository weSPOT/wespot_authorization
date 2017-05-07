package net.wespot.oauth2.provider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import java.net.URISyntaxException;
import org.codehaus.jettison.json.JSONException;

/**
 * ****************************************************************************
 * Copyright (C) 2013-2017 Open Universiteit Nederland
 * <p/>
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Contributors: Rafael Klaessen
 * ****************************************************************************
 */
interface Endpoint {
    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response authorizeGet(@Context HttpServletRequest request)
          throws URISyntaxException, OAuthSystemException, JSONException;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response authorize(@Context HttpServletRequest request)
          throws URISyntaxException, OAuthSystemException, JSONException;
}
