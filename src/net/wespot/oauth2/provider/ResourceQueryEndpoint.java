package net.wespot.oauth2.provider;

import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.AccessToken;
import net.wespot.db.Account;
import org.codehaus.jettison.json.JSONException;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.ParameterStyle;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import net.wespot.utils.DbUtils;
import net.wespot.utils.ErrorResponse;

/**
 * ****************************************************************************
 * Copyright (C) 2013 Open Universiteit Nederland
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
 * Contributors: Stefaan Ternier
 * ****************************************************************************
 */
@Path("/resource_query")
public class ResourceQueryEndpoint {

    static {
        ObjectifyService.register(AccessToken.class);
        ObjectifyService.register(Account.class);
    }

    @GET
    @Produces("application/json")
    public Response get(@Context HttpServletRequest request) throws OAuthSystemException, JSONException {
        try {
            // Extract access token from request via OAuthAccessResourceRequest
            String accessToken = new OAuthAccessResourceRequest(request, ParameterStyle.QUERY)
                    .getAccessToken();

            AccessToken at = DbUtils.getAccessToken(accessToken);
            Account account = ObjectifyService.ofy().load().ref(at.getAccount()).now();
            return Response.ok(account.toJson()).build();
        } catch (OAuthProblemException|NullPointerException e) {
            return new ErrorResponse("Invalid access token").build();
        }
    }

}
