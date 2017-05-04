package net.wespot.oauth2.provider;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.AccessToken;
import net.wespot.db.Account;
import net.wespot.db.ApplicationRegistry;
import net.wespot.db.CodeToAccount;
import org.apache.amber.oauth2.common.OAuth;

import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;

import java.net.URI;
import java.net.URISyntaxException;

import net.wespot.utils.Utils;
import net.wespot.utils.DbUtils;

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
public class AuthEndpoint {

    static {
        ObjectifyService.register(AccessToken.class);
        ObjectifyService.register(CodeToAccount.class);
        ObjectifyService.register(Account.class);
        ObjectifyService.register(ApplicationRegistry.class);
    }

    @POST
    public Response authorizePOST(@Context HttpServletRequest request)
            throws URISyntaxException, OAuthSystemException {
        return authorize(request);
    }

    @GET
    public Response authorize(@Context HttpServletRequest request)
            throws URISyntaxException, OAuthSystemException {
        try {
            final OAuthResponse loginPageResponse = OAuthASResponse
                    .authorizationResponse(request, HttpServletResponse.SC_FOUND)
                    .location("../Login.html")
                    .setParam(OAuth.OAUTH_REDIRECT_URI, request.getParameter(OAuth.OAUTH_REDIRECT_URI))
                    .setParam(OAuth.OAUTH_CLIENT_ID, request.getParameter(OAuth.OAUTH_CLIENT_ID))
                    .setParam(OAuth.OAUTH_RESPONSE_TYPE, request.getParameter(OAuth.OAUTH_RESPONSE_TYPE))
                    .setParam(OAuth.OAUTH_SCOPE, request.getParameter(OAuth.OAUTH_SCOPE))
                    .buildQueryMessage();
            URI loginPageUrl = new URI(loginPageResponse.getLocationUri());
            Response invalidCookieResponse = Response.status(loginPageResponse.getResponseStatus()).location(loginPageUrl).build();

            String token = Utils.getTokenFromCookies(request.getCookies());

            if (token == null) return invalidCookieResponse;

            AccessToken accessToken = DbUtils.getAccessToken(token);
            if (!accessToken.getAccount().isLoaded()) {
                ObjectifyService.ofy().load().key(accessToken.getAccount().getKey()).now();
            }
            if (accessToken.getAccount().getValue().getIdentifier() == null) {
                return invalidCookieResponse;
            }

            String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID);

            if (clientId != null) {
                ApplicationRegistry application = DbUtils.getApplication(clientId);
                if (application == null) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("client_id " + clientId + " is not a valid client id!")
                            .build();
                }
            } else {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("OAuth client id needs to be provided by client!")
                        .build();
            }

            OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);

            // Build response according to response_type
            String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
                    .authorizationResponse(request, HttpServletResponse.SC_FOUND);

            String code = new MD5Generator().generateValue();

            if (responseType.equals(ResponseType.CODE.toString())) {
                  builder.setCode(code);

                  CodeToAccount cta = new CodeToAccount(code, accessToken.getAccount().getValue());
                  ObjectifyService.ofy().save().entity(cta).now();
            }

            if (responseType.equals(ResponseType.TOKEN.toString())) {
                  builder.setAccessToken(code);
                  builder.setExpiresIn("3600");
            }

            String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
            final OAuthResponse response = builder.location(redirectURI).buildQueryMessage();
            URI url = new URI(response.getLocationUri());

            return Response.status(response.getResponseStatus()).location(url).build();

            //TODO: check if redirectURI exists.

        } catch (OAuthProblemException e) {
            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

            String redirectUri = e.getRedirectUri();

            if (Utils.isEmpty(redirectUri)) {
                return responseBuilder.entity("OAuth callback url needs to be provided by client!").build();
            }

            final OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .error(e)
                    .location(redirectUri)
                    .buildQueryMessage();
            final URI location = new URI(response.getLocationUri());
            return responseBuilder.location(location).build();
        }
    }
}
