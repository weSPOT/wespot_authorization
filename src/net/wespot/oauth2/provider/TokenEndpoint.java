package net.wespot.oauth2.provider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.AccessToken;
import net.wespot.db.Account;
import net.wespot.db.ApplicationRegistry;
import net.wespot.db.CodeToAccount;
import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import net.wespot.utils.Utils;
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
@Path("/token")
public class TokenEndpoint implements Endpoint {
    static {
        ObjectifyService.register(AccessToken.class);
        ObjectifyService.register(CodeToAccount.class);
        ObjectifyService.register(ApplicationRegistry.class);
        ObjectifyService.register(Account.class);
    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeGet(@Context HttpServletRequest request) throws OAuthSystemException, JSONException {
        return authorize(request);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException, JSONException {
        final String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID);
        final String clientSecret = request.getParameter(OAuth.OAUTH_CLIENT_SECRET);
        final String code = request.getParameter(OAuth.OAUTH_CODE);

        final ResponseBuilder badRequest = Response.status(Status.BAD_REQUEST);

        if (Utils.isEmpty(clientId) || Utils.isEmpty(clientSecret) || Utils.isEmpty(code)) {
            return new ErrorResponse("Missing field!").build();
        }

        final ApplicationRegistry application = DbUtils.getApplication(clientId);
        if (application == null) {
            return new ErrorResponse("client_id " + clientId + " is not a valid client id!").build();
        }

        if (!application.getClientSecret().equals(clientSecret)) {
            return new ErrorResponse("client_secret does not match client_id").build();
        }

        final CodeToAccount accountCode = DbUtils.getCodeToAccount(code);
        Account account = null;
        if (!accountCode.getAccount().isLoaded()) {
            account = ObjectifyService.ofy().load().key(accountCode.getAccount().getKey()).now();
        }

        final String accessToken = new MD5Generator().generateValue();
        final AccessToken at = new AccessToken(accessToken, account);
        ObjectifyService.ofy().save().entity(at).now();

        final JSONObject result = new JSONObject()
                .put("access_token", accessToken)
                .put("expires_in", 3600);

        return Response.ok(result.toString()).build();
    }
}
