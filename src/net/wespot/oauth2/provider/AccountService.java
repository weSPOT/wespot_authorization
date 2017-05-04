package net.wespot.oauth2.provider;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.AccessToken;
import net.wespot.db.Account;
import net.wespot.db.AccountReset;
import net.wespot.db.CodeToAccount;
import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuer;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Date;

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
@Path("/account")
public class AccountService {
    static {
        ObjectifyService.register(Account.class);
        ObjectifyService.register(AccountReset.class);
        ObjectifyService.register(CodeToAccount.class);
        ObjectifyService.register(AccessToken.class);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Path("/accountExists/{username}")
    public String accountExists(@PathParam("username") String username) {
        try {
            JSONObject result = new JSONObject();
            Account account = DbUtils.getAccount(username);
            result.put("accountExists", account != null);
            return result.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Path("/createAccount")
    public String createAccount(@DefaultValue("application/json") @HeaderParam("Content-Type") String contentType,
                                @DefaultValue("application/json") @HeaderParam("Accept") String accept,
                                String account) {

        try {
            JSONObject accountJson = new JSONObject(account);

            createAccountStatic(accountJson.getString("username"),
                    accountJson.getString("password"),
                    accountJson.getString("firstName"),
                    accountJson.getString("lastName"),
                    accountJson.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "{}";
    }

    public static Account createAccountStatic(String username, String password, String firstName, String lastName, String email) {
        Account accountOfi = new Account(username, username);
        accountOfi.setPasswordHash(hash(password));
        accountOfi.setName(firstName + " " + lastName);
        accountOfi.setFamilyName(lastName);
        accountOfi.setGivenName(firstName);
        accountOfi.setEmail(email);

        ObjectifyService.ofy().save().entity(accountOfi);
        return accountOfi;
    }

    public static AccountReset resetAccount(Account accountOfi) {
        try {
            String code = new MD5Generator().generateValue();

            AccountReset resetEntity = new AccountReset(accountOfi.getIdentifier(), code);
            ObjectifyService.ofy().save().entity(resetEntity).now();
            return resetEntity;
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request) {

        try {
            JSONObject result = new JSONObject();
            result.put("type", "AuthResponse");
            result.put("logout" , true);

            String token = Utils.getTokenFromCookies(request.getCookies());
            if (token != null) {
                ObjectifyService.ofy().delete().key(Key.create(AccessToken.class, token)).now();
                result.put("accessTokenDeleted" , token);
            }

            return Response.ok(result.toString(), MediaType.APPLICATION_JSON)
                    .header(
                        "Set-Cookie",
                        "net.wespot.authToken=deleted;Domain=.wespot-arlearn.appspot.com;Path=/;Expires=Thu, 01-Jan-1970 00:00:01 GMT"
                    )
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Path("/authenticate")
    public Response authenticate(@DefaultValue("application/json") @HeaderParam("Content-Type") String contentType,
                                 @DefaultValue("application/json") @HeaderParam("Accept") String accept,
                                 String account) {

        try {
            JSONObject result = new JSONObject();
            result.put("type", "AuthResponse");

            JSONObject accountJson = new JSONObject(account);
            Account accountOfi = DbUtils.getAccount(accountJson.getString("username"));
            if (accountOfi == null) {
                result.put("error", "username does not exist ");
                result.put("userName", false);

                return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
            }
            String password = accountJson.getString("password");
            if (password == null || !hash(password).equals(accountOfi.getPasswordHash())) {
                result.put("error", "password incorrect");
                result.put("password", false);

                return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
            }

            String accessToken = new MD5Generator().generateValue();
            AccessToken at = new AccessToken(accessToken, accountOfi);
            ObjectifyService.ofy().save().entity(at).now();

            result.put("token", accessToken);
            return Response.ok(result.toString(), MediaType.APPLICATION_JSON)
                .cookie(new NewCookie("net.wespot.authToken", accessToken, "/", "wespot-arlearn.appspot.com", "OAuth token cookie", 3600, false))
                .expires(new Date(System.currentTimeMillis() + 3600))
                .build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @POST
    @Path("/authenticateFw")
    public Response authenticateFw(@DefaultValue("application/json") @HeaderParam("Content-Type") String contentType,
                                 @Context final HttpServletResponse response,
                                 @Context final HttpServletRequest request,
                                 @FormParam("school") Long school,
                                 @FormParam("username") String username,
                                 @FormParam("password") String password,
                                 @FormParam("originalPage") String originalPage,
                                 @Context ServletContext servletContext) throws Exception {

        try {
            Account accountOfi = DbUtils.getAccount(username);

            if (accountOfi == null) {
                URI location = new URI("../" + originalPage + "?incorrectUsername");
                return Response.temporaryRedirect(location).build();
            }

            if (Utils.isEmpty(password) || !hash(password).equals(accountOfi.getPasswordHash())) {
                URI location = new URI("../" + originalPage + "?incorrectPassword");
                return Response.temporaryRedirect(location).build();
            }

            OAuthIssuer oauthIssuer = new OAuthIssuerImpl(new MD5Generator());

            AccessToken at = new AccessToken(oauthIssuer.accessToken(), accountOfi);
            ObjectifyService.ofy().save().entity(at).now();

            String code = oauthIssuer.authorizationCode();
            CodeToAccount cta = new CodeToAccount(code, accountOfi);
            ObjectifyService.ofy().save().entity(cta).now();

            URI location = new URI("http://streetlearn.appspot.com/oauth/wespot?code=" + code);

            return Response.temporaryRedirect(location).build();
        } catch (OAuthSystemException|NullPointerException e) {
            e.printStackTrace();
        }

        return Response.ok("<html></html>").build();
    }

    @POST
    @Path("/authenticateFwAndroid")
    public Response authenticateFwAndroid(@DefaultValue("application/json") @HeaderParam("Content-Type") String contentType,
                                 @Context final HttpServletResponse response,
                                 @Context final HttpServletRequest request,
                                 @FormParam("school") Long school,
                                 @FormParam("username") String username,
                                 @FormParam("password") String password,
                                 @FormParam("originalPage") String originalPage,
                                 @Context ServletContext servletContext) throws Exception {

        if (!Utils.isEmpty(username) && school != null && school != 0) {
            username = school + "_" + username;
        }

        return authenticateFw(contentType, response, request, school, username, password, originalPage, servletContext);
    }


    public static String hash(String pw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] mdbytes = md.digest(pw.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void resetPassword(String resetId, String password) {
        AccountReset ar = DbUtils.getAccountReset(resetId);
        Account account = DbUtils.getAccount(ar.getIdentifier());
        account.setPasswordHash(hash(password));
        ObjectifyService.ofy().save().entity(account);
        ObjectifyService.ofy().delete().entity(ar);
    }
}
