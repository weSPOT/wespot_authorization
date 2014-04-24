package net.wespot.oauth2.provider;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.AccessToken;
import net.wespot.db.Account;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ParameterStyle;
import org.apache.amber.oauth2.common.utils.OAuthUtils;

import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.amber.oauth2.rs.response.OAuthRSResponse;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
    @Produces("text/html")
    public Response get(@Context javax.servlet.http.HttpServletRequest request) throws OAuthSystemException {
        try {

            // Make the OAuth Request out of this request
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request,
                    ParameterStyle.QUERY);

            String accessToken = oauthRequest.getAccessToken();
            OAuthResponse oauthResponse = OAuthRSResponse
                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setRealm(TestContent.RESOURCE_SERVER_NAME)
                    .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
                    .buildHeaderMessage();

            //return Response.status(Response.Status.UNAUTHORIZED).build();
            System.out.println("query accesstoken "+accessToken);
           AccessToken at = ObjectifyService.ofy().load().key(Key.create(AccessToken.class, accessToken)).now();

            System.out.println(at.getAccount());

            Account account = ObjectifyService.ofy().load().ref(at.getAccount()).now();
           return Response.status(Response.Status.OK).entity(account.toJson()).build();


        } catch (OAuthProblemException e) {
            String errorCode = e.getError();
            if (OAuthUtils.isEmpty(errorCode)) {

                // Return the OAuth error message
                OAuthResponse oauthResponse = OAuthRSResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setRealm(TestContent.RESOURCE_SERVER_NAME)
                        .buildHeaderMessage();

                // If no error code then return a standard 401 Unauthorized response
//                return Response.status(Response.Status.UNAUTHORIZED)
//                        .header(OAuth.HeaderType.WWW_AUTHENTICATE,
//                                oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
//                        .build();
                return null;
            }

            OAuthResponse oauthResponse = OAuthRSResponse
                    .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setRealm(TestContent.RESOURCE_SERVER_NAME)
                    .setError(e.getError())
                    .setErrorDescription(e.getDescription())
                    .setErrorUri(e.getUri())
                    .buildHeaderMessage();

//            return Response.status(Response.Status.BAD_REQUEST)
//                    .header(OAuth.HeaderType.WWW_AUTHENTICATE,
//                            oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
//                    .build();
            return null;

        }

    }

}
