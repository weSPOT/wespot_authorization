package net.wespot.oauth2.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.AccessToken;
import net.wespot.db.Account;
import net.wespot.db.ApplicationRegistry;
import net.wespot.db.CodeToAccount;
import org.apache.amber.oauth2.common.OAuth;

import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.as.response.OAuthASResponse.OAuthAuthorizationResponseBuilder;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.codehaus.jettison.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;

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
@Path("/auth")
public class AuthEndpoint implements Endpoint {

    static {
        ObjectifyService.register(AccessToken.class);
        ObjectifyService.register(CodeToAccount.class);
        ObjectifyService.register(Account.class);
        ObjectifyService.register(ApplicationRegistry.class);
    }

    @GET
    public Response authorizeGet(@Context HttpServletRequest request)
            throws URISyntaxException, OAuthSystemException, JSONException {
        return authorize(request);
    }

    @POST
    public Response authorize(@Context HttpServletRequest request)
            throws URISyntaxException, OAuthSystemException, JSONException {

        final String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID);
        final String responseType = request.getParameter(OAuth.OAUTH_RESPONSE_TYPE);
        final String redirectUri = request.getParameter(OAuth.OAUTH_REDIRECT_URI);

        final OAuthResponse loginPageResponse = OAuthASResponse
                .authorizationResponse(request, HttpServletResponse.SC_FOUND)
                .location("../Login.html")
                .setParam(OAuth.OAUTH_REDIRECT_URI, redirectUri)
                .setParam(OAuth.OAUTH_CLIENT_ID, clientId)
                .setParam(OAuth.OAUTH_RESPONSE_TYPE, responseType)
                .buildQueryMessage();
        final URI loginPageUrl = new URI(loginPageResponse.getLocationUri());
        final Response invalidCookieResponse = Response.status(loginPageResponse.getResponseStatus()).location(loginPageUrl).build();

        final String token = Utils.getTokenFromCookies(request.getCookies());
        if (token == null) return invalidCookieResponse;

        final AccessToken accessToken = DbUtils.getAccessToken(token);
        if (!accessToken.getAccount().isLoaded()) {
            ObjectifyService.ofy().load().key(accessToken.getAccount().getKey()).now();
        }
        if (accessToken.getAccount().getValue().getIdentifier() == null) {
            return invalidCookieResponse;
        }

        if (clientId == null) {
            return new ErrorResponse("OAuth client id needs to be provided by client!").build();
        } else if (clientId != null && DbUtils.getApplication(clientId) == null) {
            return new ErrorResponse("client_id " + clientId + " is not a valid client id!").build();
        }

        if (redirectUri == null || !Utils.validUri(redirectUri)) {
            return new ErrorResponse("Valid redirect uri needs to be provided by client!").build();
        }

        if (responseType == null || (!responseType.equals(ResponseType.TOKEN.toString()) && !responseType.equals(ResponseType.CODE.toString()))) {
            return new ErrorResponse("Invalid response type").build();
        }

        // Build response according to response_type
        final OAuthAuthorizationResponseBuilder builder = OAuthASResponse
                .authorizationResponse(request, HttpServletResponse.SC_FOUND);

        final String code = new MD5Generator().generateValue();

        if (responseType.equals(ResponseType.CODE.toString())) {
            builder.setCode(code);

            final CodeToAccount cta = new CodeToAccount(code, accessToken.getAccount().getValue());
            ObjectifyService.ofy().save().entity(cta).now();
        } else {
            builder.setAccessToken(code);
            builder.setExpiresIn("3600");
        }

        final OAuthResponse response = builder.location(redirectUri).buildQueryMessage();
        final URI url = new URI(response.getLocationUri());

        return Response.status(response.getResponseStatus()).location(url).build();
    }
}
