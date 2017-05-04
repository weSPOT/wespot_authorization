package net.wespot.oauth2.provider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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

import java.util.HashMap;

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
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException, JSONException {
        HashMap<String, String> requestData = new HashMap<String, String>();

        requestData.put(OAuth.OAUTH_CLIENT_ID, request.getParameter(OAuth.OAUTH_CLIENT_ID));
        requestData.put(OAuth.OAUTH_CLIENT_SECRET, request.getParameter(OAuth.OAUTH_CLIENT_SECRET));
        requestData.put(OAuth.OAUTH_CODE, request.getParameter(OAuth.OAUTH_CODE));
        requestData.put(OAuth.OAUTH_GRANT_TYPE, request.getParameter(OAuth.OAUTH_GRANT_TYPE));

        if (Utils.hasEmpty(requestData.keySet())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing field!").build();
        }

        return authorize(requestData);
    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeGet(@Context HttpServletRequest request) throws OAuthSystemException, JSONException {
        return authorize(request);
    }

    private Response authorize(HashMap<String, String> hashMap) throws OAuthSystemException, JSONException {
        String clientId = hashMap.get(OAuth.OAUTH_CLIENT_ID);

        ApplicationRegistry application = DbUtils.getApplication(clientId);
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
        CodeToAccount code = DbUtils.getCodeToAccount(hashMap.get(OAuth.OAUTH_CODE));
        if (code != null) {
            Account account = null;
            if (!code.getAccount().isLoaded()) {
                account = ObjectifyService.ofy().load().key(code.getAccount().getKey()).now();
            }
            AccessToken at = new AccessToken(accessToken, account);
            ObjectifyService.ofy().save().entity(at).now();
        }

        JSONObject result = new JSONObject()
                .put("access_token", accessToken)
                .put("expires_in", 3600);

        return Response.ok(result.toString()).build();
    }
}
