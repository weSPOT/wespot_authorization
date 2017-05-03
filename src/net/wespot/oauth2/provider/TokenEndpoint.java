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

import net.wespot.utils.Utils;

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
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID);
        String clientSecret = request.getParameter(OAuth.OAUTH_CLIENT_SECRET);
        String code = request.getParameter(OAuth.OAUTH_CODE);
        String grantType = request.getParameter(OAuth.OAUTH_GRANT_TYPE);

        if (Utils.isEmpty(clientId) || Utils.isEmpty(clientSecret) || Utils.isEmpty(code) || Utils.isEmpty(grantType)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing field!").build();
        }

        hashMap.put(OAuth.OAUTH_CLIENT_ID, clientId);
        hashMap.put(OAuth.OAUTH_CLIENT_SECRET, clientSecret);
        hashMap.put(OAuth.OAUTH_CODE, code);
        hashMap.put(OAuth.OAUTH_GRANT_TYPE, grantType);

        return authorize(hashMap);
    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeGet(@Context HttpServletRequest request) throws OAuthSystemException {
        return authorize(request);
    }

    private Response authorize(HashMap<String, String> hashMap) throws OAuthSystemException {
        String clientId = hashMap.get(OAuth.OAUTH_CLIENT_ID);

        ApplicationRegistry application = ObjectifyService.ofy().load().key(Key.create(ApplicationRegistry.class, clientId)).now();
        if (application == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("client_id " + clientId + " is not a valid client id!")
                    .build();
        }

        if (!application.getClientSecret().equals(hashMap.get(OAuth.OAUTH_CLIENT_SECRET))) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("client_secret does not match client_id")
                    .build();
        }

        String accessToken = new MD5Generator().generateValue();

        System.out.println("access token is " + accessToken);
        OAuthResponse response = OAuthASResponse
                .tokenResponse(HttpServletResponse.SC_OK)
                .setAccessToken(accessToken)
                .setExpiresIn("3600")
                .buildJSONMessage();

        CodeToAccount code = ObjectifyService.ofy().load().key(Key.create(CodeToAccount.class, hashMap.get(OAuth.OAUTH_CODE))).now();
        if (code != null) {
            Account account = null;
            System.out.println(code.getAccount().isLoaded());
            if (!code.getAccount().isLoaded()) {
                account = ObjectifyService.ofy().load().key(code.getAccount().getKey()).now();
            }
            AccessToken at = new AccessToken(accessToken, account);
            ObjectifyService.ofy().save().entity(at).now();
        }

        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

}
