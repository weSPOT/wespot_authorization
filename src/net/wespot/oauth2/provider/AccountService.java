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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Date;

import net.wespot.utils.Utils;
import net.wespot.utils.DbUtils;
import net.wespot.utils.ErrorResponse;
import net.wespot.utils.SuccessResponse;

/**
 * ****************************************************************************
 * Copyright (C) 2013-2017 Open Universiteit Nederland
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
 * Contributors: Stefaan Ternier, Rafael Klaessen
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/accountExists/{username}")
    public String accountExists(@PathParam("username") String username) throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("accountExists", DbUtils.getAccount(username) != null);
        return result.toString();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/createAccount")
    public Response createAccount(String account) throws JSONException {
        try {
            final JSONObject accountJson = new JSONObject(account);
            final String username = accountJson.getString("username");

            if (DbUtils.getAccount(username) != null) {
                return new ErrorResponse("Username taken").build();
            }

            final String email = accountJson.getString("email");

            if (!Utils.isValidEmail(email)) {
                return new ErrorResponse("Invalid email address").build();
            }

            createAccountStatic(username,
                    accountJson.getString("password"),
                    accountJson.getString("firstName"),
                    accountJson.getString("lastName"),
                    email);

            return new SuccessResponse("account", accountJson).build();
        } catch (JSONException e) {
            return new ErrorResponse("Invalid account JSON").build();
        }
    }

    public static Account createAccountStatic(String username, String password, String firstName, String lastName, String email) {
        final Account account = new Account(username, username);
        account.setPasswordHash(hash(password));
        account.setName(firstName + " " + lastName);
        account.setFamilyName(lastName);
        account.setGivenName(firstName);
        account.setEmail(email);

        ObjectifyService.ofy().save().entity(account);
        return account;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request) throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("type", "AuthResponse");
        result.put("logout" , true);

        final String token = Utils.getTokenFromCookies(request.getCookies());
        if (token != null) {
            ObjectifyService.ofy().delete().key(Key.create(AccessToken.class, token)).now();
            result.put("accessTokenDeleted" , token);
        }

        return Response.ok(result.toString())
                .header(
                    "Set-Cookie",
                    "net.wespot.authToken=deleted;Domain=.wespot-arlearn.appspot.com;Path=/;Expires=Thu, 01-Jan-1970 00:00:01 GMT"
                )
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/authenticate")
    public Response authenticate(String account) throws JSONException, OAuthSystemException {
        final JSONObject result = new JSONObject();
        result.put("type", "AuthResponse");

        final JSONObject accountJson = new JSONObject(account);
        final Account accountOfi = DbUtils.getAccount(accountJson.getString("username"));
        if (accountOfi == null) {
            result.put("error", "username does not exist ");
            result.put("userName", false);

            return Response.ok(result.toString()).build();
        }
        final String password = accountJson.getString("password");
        if (password == null || !hash(password).equals(accountOfi.getPasswordHash())) {
            result.put("error", "password incorrect");
            result.put("password", false);

            return Response.ok(result.toString()).build();
        }

        final String accessToken = new MD5Generator().generateValue();
        final AccessToken at = new AccessToken(accessToken, accountOfi);
        ObjectifyService.ofy().save().entity(at).now();

        result.put("token", accessToken);
        return Response.ok(result.toString())
                .cookie(new NewCookie("net.wespot.authToken", accessToken, "/", "wespot-arlearn.appspot.com", "OAuth token cookie", 3600, false))
                .expires(new Date(System.currentTimeMillis() + 3600))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/authenticateFw")
    public Response authenticateFw(@FormParam("username") String username,
                                   @FormParam("password") String password,
                                   @FormParam("originalPage") String originalPage) throws URISyntaxException {

        try {
            final Account account = DbUtils.getAccount(username);

            if (account == null) {
                final URI location = new URI("../" + originalPage + "?incorrectUsername");
                return Response.temporaryRedirect(location).build();
            }

            if (Utils.isEmpty(password) || !hash(password).equals(account.getPasswordHash())) {
                final URI location = new URI("../" + originalPage + "?incorrectPassword");
                return Response.temporaryRedirect(location).build();
            }

            final OAuthIssuer oauthIssuer = new OAuthIssuerImpl(new MD5Generator());

            final AccessToken at = new AccessToken(oauthIssuer.accessToken(), account);
            ObjectifyService.ofy().save().entity(at).now();

            final String code = oauthIssuer.authorizationCode();
            final CodeToAccount cta = new CodeToAccount(code, account);
            ObjectifyService.ofy().save().entity(cta).now();

            final URI location = new URI("http://streetlearn.appspot.com/oauth/wespot?code=" + code);
            return Response.temporaryRedirect(location).build();
        } catch (OAuthSystemException|NullPointerException e) {
            final URI location = new URI("../" + originalPage + "?incorrectData");
            return Response.temporaryRedirect(location).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/authenticateFwAndroid")
    public Response authenticateFwAndroid(@FormParam("school") Long school,
                                          @FormParam("username") String username,
                                          @FormParam("password") String password,
                                          @FormParam("originalPage") String originalPage) throws URISyntaxException {

        if (!Utils.isEmpty(username) && school != null && school != 0) {
            username = school + "_" + username;
        }

        return authenticateFw(username, password, originalPage);
    }


    public static String hash(String pw) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] mdbytes = md.digest(pw.getBytes());
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static AccountReset resetAccount(Account account) throws OAuthSystemException {
        final String code = new MD5Generator().generateValue();

        final AccountReset resetEntity = new AccountReset(account.getIdentifier(), code);
        ObjectifyService.ofy().save().entity(resetEntity).now();
        return resetEntity;
    }

    public static void resetPassword(String resetId, String password) {
        final AccountReset ar = DbUtils.getAccountReset(resetId);
        final Account account = DbUtils.getAccount(ar.getIdentifier());
        account.setPasswordHash(hash(password));
        ObjectifyService.ofy().save().entity(account);
        ObjectifyService.ofy().delete().entity(ar);
    }
}
