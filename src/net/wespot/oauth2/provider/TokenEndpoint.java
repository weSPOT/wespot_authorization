package net.wespot.oauth2.provider;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.AccessToken;
import net.wespot.db.Account;
import net.wespot.db.ApplicationRegistry;
import net.wespot.db.CodeToAccount;
import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuer;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.as.request.OAuthTokenRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;
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
public class TokenEndpoint {
    static {
        ObjectifyService.register(AccessToken.class);
        ObjectifyService.register(CodeToAccount.class);
        ObjectifyService.register(ApplicationRegistry.class);
        ObjectifyService.register(Account.class);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context javax.servlet.http.HttpServletRequest request) throws OAuthSystemException {

        OAuthTokenRequest oauthRequest = null;

        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthTokenRequest(request);

            //check if clientid is valid
            if (!TestContent.CLIENT_ID.equals(oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                                .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            //do checking for different grant types
            if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.AUTHORIZATION_CODE.toString())) {
                if (!TestContent.AUTHORIZATION_CODE.equals(oauthRequest.getParam(OAuth.OAUTH_CODE))) {
                    OAuthResponse response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("invalid authorization code")
                            .buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.PASSWORD.toString())) {
                if (!TestContent.PASSWORD.equals(oauthRequest.getPassword())
                        || !TestContent.USERNAME.equals(oauthRequest.getUsername())) {
                    OAuthResponse response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("invalid username or password")
                            .buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.REFRESH_TOKEN.toString())) {
                OAuthResponse response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("invalid username or password")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            Account account = ObjectifyService.ofy().load().key(Key.create(Account.class, "someId2")).now();
//            AccessToken at = new AccessToken(oauthIssuerImpl.accessToken(), account);
            String accessToken =oauthIssuerImpl.accessToken();
//            ObjectifyService.ofy().save().entity(at).now();
            OAuthResponse response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(accessToken)
                    .setExpiresIn("3600")
                    .buildJSONMessage();
            CodeToAccount cta = new CodeToAccount(accessToken, account);
            ObjectifyService.ofy().save().entity(cta).now();

            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        } catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }
    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeGet(@Context javax.servlet.http.HttpServletRequest request) throws OAuthSystemException {
//        OAuthTokenRequest oauthRequest = null;
//        try {

//        oauthRequest = new OAuthTokenRequest(request);

        String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID);
            String sharedSecret = "";
        if (clientId != null) {
            ApplicationRegistry application = ObjectifyService.ofy().load().key(Key.create(ApplicationRegistry.class, request.getParameter(OAuth.OAUTH_CLIENT_ID))).now();
            if (application == null) {
                final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

                throw new WebApplicationException(
                        responseBuilder.entity("client_id "+request.getParameter(OAuth.OAUTH_CLIENT_ID)+" is not a valid client id!!!").build());

            } else {
                sharedSecret = application.getClientSecret();
            }

        } else {
            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

            throw new WebApplicationException(
                    responseBuilder.entity("OAuth client id needs to be provided by client!!!").build());
        }

        if (!clientId.equals(request.getParameter(OAuth.OAUTH_CLIENT_ID))) {
            OAuthResponse response =
                    OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                            .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }

        if (!sharedSecret.equals(request.getParameter(OAuth.OAUTH_CLIENT_SECRET))) {
            OAuthResponse response =
                    OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_REQUEST).setErrorDescription("shared secret does not match")
                            .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }



        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        String accessToken = oauthIssuerImpl.accessToken();
        System.out.println("access token is "+accessToken);
        OAuthResponse response = OAuthASResponse
                .tokenResponse(HttpServletResponse.SC_OK)
                .setAccessToken(accessToken)
                .setExpiresIn("3600")
                .buildJSONMessage();

        CodeToAccount code = ObjectifyService.ofy().load().key(`CodeToAccount.class, request.getParameter("code"))).now();
        if (code != null) {
            Account account = null;
            if (!code.getAccount().isLoaded()) {
                account = ObjectifyService.ofy().load().key(code.getAccount().getKey()).now();
            }
            AccessToken at = new AccessToken(accessToken, account);
            ObjectifyService.ofy().save().entity(at).now();
        }



        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
//        } catch (OAuthProblemException e) {
//            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
//                    .buildJSONMessage();
//            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
//        }
    }

}
