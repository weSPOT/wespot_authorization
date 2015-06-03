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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        System.out.println("before fetching ");
        HashMap<String, String> hashMap = new HashMap<String, String>();
        OAuthTokenRequest oauthRequest = null;
        try {
            System.out.println("before fetching ");

            oauthRequest = new OAuthTokenRequest(request);
            System.out.println("before fetching "+oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID));
            hashMap.put(OAuth.OAUTH_CLIENT_ID, oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID));
            hashMap.put(OAuth.OAUTH_CODE, oauthRequest.getParam(OAuth.OAUTH_CODE));
            hashMap.put(OAuth.OAUTH_GRANT_TYPE, oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE));
            hashMap.put(OAuth.OAUTH_CLIENT_SECRET, oauthRequest.getParam(OAuth.OAUTH_CLIENT_SECRET));
            System.out.println("hashmap "+hashMap.toString());
            System.out.println("OAUTH_CLIENT_ID "+oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID));
        } catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }

        return authorize(hashMap);

    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeGet(@Context javax.servlet.http.HttpServletRequest request) throws OAuthSystemException {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(OAuth.OAUTH_CLIENT_ID, request.getParameter(OAuth.OAUTH_CLIENT_ID));
        hashMap.put(OAuth.OAUTH_CODE, request.getParameter(OAuth.OAUTH_CODE));
        hashMap.put(OAuth.OAUTH_GRANT_TYPE, request.getParameter(OAuth.OAUTH_GRANT_TYPE));
        hashMap.put(OAuth.OAUTH_CLIENT_SECRET, request.getParameter(OAuth.OAUTH_CLIENT_SECRET));

        Set<Map.Entry> set = request.getParameterMap().entrySet();
        for (Map.Entry entry : set) {
            System.out.println("entry " + entry.getKey() + " " + entry.getValue());
        }
        return authorize( hashMap);
    }


    private Response authorize(HashMap<String, String> hashMap)  throws OAuthSystemException{
        String clientId = hashMap.get(OAuth.OAUTH_CLIENT_ID);
        String sharedSecret = "";
        if (clientId != null) {

            ApplicationRegistry application = ObjectifyService.ofy().load().key(Key.create(ApplicationRegistry.class, hashMap.get(OAuth.OAUTH_CLIENT_ID))).now();
            if (application == null) {
                final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

                throw new WebApplicationException(
                        responseBuilder.entity("client_id " + hashMap.get(OAuth.OAUTH_CLIENT_ID) + " is not a valid client id!!!").build());

            } else {
                sharedSecret = application.getClientSecret();
            }

        } else {
            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

            throw new WebApplicationException(
                    responseBuilder.entity("OAuth client id needs to be provided by client!!!").build());
        }

//        if (!clientId.equals(hashMap.get(OAuth.OAUTH_CLIENT_ID))) {
//            OAuthResponse response =
//                    OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
//                            .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
//                            .buildJSONMessage();
//            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
//        }

        if (!sharedSecret.equals(hashMap.get(OAuth.OAUTH_CLIENT_SECRET))) {
            OAuthResponse response =
                    OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_REQUEST).setErrorDescription("shared secret does not match")
                            .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }


        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        String accessToken = oauthIssuerImpl.accessToken();
        System.out.println("access token is " + accessToken);
        OAuthResponse response = OAuthASResponse
                .tokenResponse(HttpServletResponse.SC_OK)
                .setAccessToken(accessToken)
                .setExpiresIn("3600")
                .buildJSONMessage();

        CodeToAccount code = ObjectifyService.ofy().load().key(Key.create(CodeToAccount.class, hashMap.get(OAuth.OAUTH_CODE))).now();
        if (code != null) {
            Account account = null;
            if (!code.getAccount().isLoaded()) {
                account = ObjectifyService.ofy().load().key(code.getAccount().getKey()).now();
            }
            AccessToken at = new AccessToken(accessToken, account);
            ObjectifyService.ofy().save().entity(at).now();
        }


        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

}
