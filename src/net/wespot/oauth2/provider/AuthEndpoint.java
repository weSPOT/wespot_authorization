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
    @Path("/password")
    public Response credentials(@Context HttpServletRequest request,
                                @FormParam("account") String account,
                                @FormParam("pwd") String pwd)
            throws URISyntaxException, OAuthSystemException {
        String concat = account + ":" + pwd;
        MD5Generator md5Generator = new MD5Generator();
        String md5 = md5Generator.generateValue(concat);
        System.out.println("accoun " + concat);
        System.out.println("md5 is " + md5);

        if ("pw".equals("pw")) {
            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
                    .authorizationResponse(request, HttpServletResponse.SC_FOUND);
            final OAuthResponse response = builder.location("/grant.html").buildQueryMessage();

            return Response.status(response.getResponseStatus()).location(new URI("../grant.html")).build();

        }
        return null;
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
            OAuthAuthzRequest oauthRequest = null;

            OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
            String token = null;
            System.out.println("cookie set ? " + request.getCookies());
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("net.wespot.authToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
                System.out.println("cookie set ? " + token);


                if (request.getParameter(OAuth.OAUTH_CLIENT_ID) != null) {
                    ApplicationRegistry application = ObjectifyService.ofy().load().key(Key.create(ApplicationRegistry.class, request.getParameter(OAuth.OAUTH_CLIENT_ID))).now();
                    if (application == null) {
                        final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

                        throw new WebApplicationException(
                                responseBuilder.entity("client_id " + request.getParameter(OAuth.OAUTH_CLIENT_ID) + " is not a valid client id!").build());

                    }

                } else {
                    final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

                    throw new WebApplicationException(
                            responseBuilder.entity("OAuth client id needs to be provided by client!!!").build());
                }
                if (token != null) {
                    System.out.println("Token exists");
                    AccessToken accessToken = ObjectifyService.ofy().load().key(Key.create(AccessToken.class, token)).now();
                    if (!accessToken.getAccount().isLoaded()) {
                        ObjectifyService.ofy().load().key(accessToken.getAccount().getKey()).now();
                    }
                    System.out.println("accessToken " + accessToken.getAccount().getValue().getIdentifier());
                    if (accessToken.getAccount().getValue().getIdentifier() != null) {
                        oauthRequest = new OAuthAuthzRequest(request);

                        //build response according to response_type

                        String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

                        OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
                                .authorizationResponse(request, HttpServletResponse.SC_FOUND);

                        if (responseType.equals(ResponseType.CODE.toString())) {
                            String code = oauthIssuerImpl.authorizationCode();
                            builder.setCode(code);

                            CodeToAccount cta = new CodeToAccount(code, accessToken.getAccount().getValue());
                            ObjectifyService.ofy().save().entity(cta).now();
                            System.out.println("code will be sent" + code);
                        }
                        if (responseType.equals(ResponseType.TOKEN.toString())) {
                            builder.setAccessToken(oauthIssuerImpl.accessToken());
                            builder.setExpiresIn("3600");
                        }


                        String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
                        System.out.println("redirect url" + redirectURI);
                        final OAuthResponse response = builder.location(redirectURI).buildQueryMessage();
                        URI url = new URI(response.getLocationUri());

                        return Response.status(response.getResponseStatus()).location(url).build();
                    }
                }
            }
            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
                    .authorizationResponse(request, HttpServletResponse.SC_FOUND);
            final OAuthResponse response = builder.location("../Login.html")
                    .setParam(OAuth.OAUTH_REDIRECT_URI, request.getParameter(OAuth.OAUTH_REDIRECT_URI))
                    .setParam(OAuth.OAUTH_CLIENT_ID, request.getParameter(OAuth.OAUTH_CLIENT_ID))
                    .setParam(OAuth.OAUTH_RESPONSE_TYPE, request.getParameter(OAuth.OAUTH_RESPONSE_TYPE))
                    .setParam(OAuth.OAUTH_SCOPE, request.getParameter(OAuth.OAUTH_SCOPE))
                    .buildQueryMessage();
            URI url = new URI(response.getLocationUri());
            System.out.println("redirecting to login page" + url);
            return Response.status(response.getResponseStatus()).location(url).build();

            //TODO: check if redirectURI exists.


        } catch (OAuthProblemException e) {

            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

            String redirectUri = e.getRedirectUri();

            if (OAuthUtils.isEmpty(redirectUri)) {
                throw new WebApplicationException(
                        responseBuilder.entity("OAuth callback url needs to be provided by client!!!").build());
            }
            final OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .error(e)
                    .location(redirectUri).buildQueryMessage();
            final URI location = new URI(response.getLocationUri());
            return responseBuilder.location(location).build();
        }
    }
}
