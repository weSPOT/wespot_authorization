package net.wespot.oauth2.provider;

import com.google.gwt.user.client.Window;
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

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
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public String createAccount() {
        Account account = new Account("someId2","iemand2") ;
        account.setName("Stefaan weSPOT Ternier");
        account.setGivenName("Stefaan");
        account.setFamilyName("Ternier");
        account.setPictureUrl("https://lh3.googleusercontent.com/-rRb8mSKLrNY/AAAAAAAAAAI/AAAAAAAAEQY/Y8BKx96IyHQ/photo.jpg");

        ObjectifyService.ofy().save().entity(account).now();



        return "";
    }
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Path("/accountExists/{account}")
    public String accountExists(@PathParam("account")String account)  {
        JSONObject result = null;
        try {
            result = new JSONObject();
            Account accountOfi = ObjectifyService.ofy().load().key(Key.create(Account.class, account)).now();
            result.put("accountExists", accountOfi!= null);
            return result.toString() ;
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

        ObjectifyService.register(Account.class);

        try {
            JSONObject accountJson = new JSONObject(account);
            Account accountOfi = new Account(accountJson.getString("username"),accountJson.getString("username")) ;
            accountOfi.setPasswordHash(hash(accountJson.getString("password")));

            accountOfi.setName(accountJson.getString("firstname")+" "+accountJson.getString("familyName"));
            accountOfi.setFamilyName(accountJson.getString("familyName"));
            accountOfi.setGivenName(accountJson.getString("firstname"));
            if (accountJson.has("pictureUrl")) accountOfi.setPictureUrl(accountJson.getString("pictureUrl"));
            accountOfi.setEmail(accountJson.getString("email"));


            ObjectifyService.ofy().save().entity(accountOfi);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return "{}";
    }

    public static Account createAccountStatic (String username, String password, String firstName, String lastName, String email){

        ObjectifyService.register(Account.class);

            Account accountOfi = new Account(username,username) ;
            accountOfi.setPasswordHash(hash(password));

            accountOfi.setName(firstName+" "+lastName);
            accountOfi.setFamilyName(lastName);
            accountOfi.setGivenName(firstName);
//            if (accountJson.has("pictureUrl")) accountOfi.setPictureUrl(accountJson.getString("pictureUrl"));
            accountOfi.setEmail(email);


            ObjectifyService.ofy().save().entity(accountOfi);
        return accountOfi;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Path("/resetPassword/{email}/{username}")
    public String resetPassword(@DefaultValue("application/json") @HeaderParam("Content-Type") String contentType,
                                @DefaultValue("application/json") @HeaderParam("Accept") String accept,
                                @PathParam("username") String username,
                                @PathParam("email") String email) throws OAuthSystemException {
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        if (username != null && !"".equals(username.trim())) {
            Account accountOfi = ObjectifyService.ofy().load().key(Key.create(Account.class, username)).now();


            AccountReset resetEntity = new AccountReset(accountOfi.getIdentifier(),oauthIssuerImpl.authorizationCode());
            ObjectifyService.ofy().save().entity(resetEntity).now();
        }

        return "{}";
    }

    public static Account resetAccountGetAccount(String username, String email) {

        if (username != null && !"".equals(username.trim())) {
            Account accountOfi = ObjectifyService.ofy().load().key(Key.create(Account.class, username)).now();
            return accountOfi;

        }
//        if (email!= null && !"".equals(email.trim())) {
//            List<Account> accounts = ObjectifyService.ofy().load().type(Account.class).filter("email", email.trim()).list();
//            if (!accounts.isEmpty()) {
//
//            }
//
//        }
        return null;
    }

    public static AccountReset resetAccount(Account accountOfi) {
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        AccountReset resetEntity = null;
        try {
            resetEntity = new AccountReset(accountOfi.getIdentifier(),oauthIssuerImpl.authorizationCode());
            ObjectifyService.ofy().save().entity(resetEntity).now();
            return resetEntity;
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
        return null;
    }
    @GET
    @Path("/logout")
    public Response logout( @Context HttpServletRequest request) { //@PathParam("accessToken") String accessToken,

        try {
            JSONObject result = new JSONObject();
            result.put("type", "AuthResponse");
            result.put("logout" , true);

            if (request.getCookies() != null) {
                String token = null;
                for (Cookie cookie : request.getCookies()) {
                    if ("net.wespot.authToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
                System.out.println("cookie set ? " + token);
                if (token != null) {
                    ObjectifyService.ofy().delete().key(Key.create(AccessToken.class, token)).now();
                    result.put("accessTokenDeleted" , token);
                }
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
                               String account)  {

        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        try {
            JSONObject accountJson = new JSONObject(account);
            Account accountOfi = ObjectifyService.ofy().load().key(Key.create(Account.class, accountJson.getString("username"))).now();
            if (accountOfi == null) {
                JSONObject result = new JSONObject();
                try {
                    result.put("type", "AuthResponse");
                    result.put("error" , "username does not exist ");
                    result.put("userName" , false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
            }
            String password =accountJson.getString("password");
            if (password == null || !hash(accountJson.getString("password")).equals(accountOfi.getPasswordHash())) {
                JSONObject result = new JSONObject();
                try {
                    result.put("type", "AuthResponse");
                    result.put("error" , "password incorrect");
                    result.put("password" , false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
            }
            AccessToken at = new AccessToken(oauthIssuerImpl.accessToken(), accountOfi);
            ObjectifyService.ofy().save().entity(at).now();

            JSONObject result = new JSONObject();
            result.put("type", "AuthResponse");
            result.put("token" , at.getIdentifier());
            return Response.ok(result.toString(), MediaType.APPLICATION_JSON)
                                		.cookie(new NewCookie("net.wespot.authToken", at.getIdentifier(), "/", "wespot-arlearn.appspot.com", "OAuth token cookie", 3600, false))
                                		.expires(new Date(System.currentTimeMillis() + 3600))
                                		.build();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            //TODO for testing only ... remove this
            JSONObject result = new JSONObject();
            try {
                result.put("type", "AuthResponse");

                result.put("token" , "token_12345");
                result.put("error" , "Could not recover from error");
                result.put("password" , false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
        }

        return null;
    }

    @POST
    @Path("/authenticateFw")
    public String authenticateFw(@DefaultValue("application/json") @HeaderParam("Content-Type") String contentType,
                                 @Context final HttpServletResponse response,
                                 @Context final HttpServletRequest request,
                                 @FormParam("school") Long school,
                                 @FormParam("username") String username,
                                 @FormParam("password") String password,
                                 @FormParam("originalPage") String originalPage,
                                 @Context ServletContext servletContext)  throws Exception{



        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        try {

            Account accountOfi = null;
            if (username != null && !"".equals(username)) {


                accountOfi= ObjectifyService.ofy().load().key(Key.create(Account.class, username)).now();
            }
            if (accountOfi == null) {
                    java.net.URI location = new java.net.URI("../"+originalPage+"?incorrectUsername");
                    throw new WebApplicationException(Response.temporaryRedirect(location).build());
            }

            if (password == null || "".equals(password)|| !hash(password).equals(accountOfi.getPasswordHash())) {
                    java.net.URI location = new java.net.URI("../"+originalPage+"?incorrectPassword");
                    throw new WebApplicationException(Response.temporaryRedirect(location).build());
            }
            AccessToken at = new AccessToken(oauthIssuerImpl.accessToken(), accountOfi);
            ObjectifyService.ofy().save().entity(at).now();


            String redirect_uri = "http://streetlearn.appspot.com/oauth/wespot";
            String clientId = "wespotstreetlearnid";


            String code = oauthIssuerImpl.authorizationCode();
            CodeToAccount cta = new CodeToAccount(code, accountOfi);
            ObjectifyService.ofy().save().entity(cta).now();

//            java.net.URI location = new java.net.URI("https://wespot-arlearn.appspot.com/oauth/auth?redirect_uri=http://ar-learn.appspot.com/oauth/wespot&response_type=code&client_id=wespotarlearnid&approval_prompt=force&scope=profile+email");

//            java.net.URI location = new java.net.URI("/auth?redirect_uri="+ redirect_uri+
//                    "&client_id="+clientId+
//                    "&response_type=code"+
//                    "&scope=profile+email");
            java.net.URI location = new java.net.URI("http://streetlearn.appspot.com/oauth/wespot?code="+code);

            throw new WebApplicationException(Response.temporaryRedirect(location).build());
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            //TODO for testing only ... remove this
            JSONObject result = new JSONObject();
            try {
                result.put("type", "AuthResponse");

                result.put("token" , "token_12345");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result.toString();
        }
//        response.sendRedirect("http://www.google.com");
//        RequestDispatcher dispatcher =  servletContext.getRequestDispatcher("http://www.google.com");
//        dispatcher.forward(request, response);

//        try {
//
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

        return "<html></html>";

    }

    @POST
    @Path("/authenticateFwAndroid")
    public String authenticateFwAndroid(@DefaultValue("application/json") @HeaderParam("Content-Type") String contentType,
                                 @Context final HttpServletResponse response,
                                 @Context final HttpServletRequest request,
                                 @FormParam("school") Long school,
                                 @FormParam("username") String username,
                                 @FormParam("password") String password,
                                 @FormParam("originalPage") String originalPage,
                                 @Context ServletContext servletContext)  throws Exception{



        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        try {

            Account accountOfi = null;
            if (username != null && !"".equals(username)) {
                if (school != 0) {
                    username = school+ "_"+username;
                }

                accountOfi= ObjectifyService.ofy().load().key(Key.create(Account.class, username)).now();
            }
            if (accountOfi == null) {
                java.net.URI location = new java.net.URI("../"+originalPage+"?incorrectUsername");
                throw new WebApplicationException(Response.temporaryRedirect(location).build());
            }

            if (password == null || "".equals(password)|| !hash(password).equals(accountOfi.getPasswordHash())) {
                java.net.URI location = new java.net.URI("../"+originalPage+"?incorrectPassword");
                throw new WebApplicationException(Response.temporaryRedirect(location).build());
            }
            AccessToken at = new AccessToken(oauthIssuerImpl.accessToken(), accountOfi);
            ObjectifyService.ofy().save().entity(at).now();


            String redirect_uri = "http://streetlearn.appspot.com/oauth/wespot";
            String clientId = "wespotstreetlearnid";


            String code = oauthIssuerImpl.authorizationCode();
            CodeToAccount cta = new CodeToAccount(code, accountOfi);
            ObjectifyService.ofy().save().entity(cta).now();

//            java.net.URI location = new java.net.URI("https://wespot-arlearn.appspot.com/oauth/auth?redirect_uri=http://ar-learn.appspot.com/oauth/wespot&response_type=code&client_id=wespotarlearnid&approval_prompt=force&scope=profile+email");

//            java.net.URI location = new java.net.URI("/auth?redirect_uri="+ redirect_uri+
//                    "&client_id="+clientId+
//                    "&response_type=code"+
//                    "&scope=profile+email");
            java.net.URI location = new java.net.URI("http://streetlearn.appspot.com/oauth/wespot?code="+code);

            throw new WebApplicationException(Response.temporaryRedirect(location).build());
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            //TODO for testing only ... remove this
            JSONObject result = new JSONObject();
            try {
                result.put("type", "AuthResponse");

                result.put("token" , "token_12345");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result.toString();
        }
//        response.sendRedirect("http://www.google.com");
//        RequestDispatcher dispatcher =  servletContext.getRequestDispatcher("http://www.google.com");
//        dispatcher.forward(request, response);

//        try {
//
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

        return "<html></html>";

    }


    public static String hash(String pw){
        try{
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


    public static AccountReset getAccountReset(String id) {
        return ObjectifyService.ofy().load().key(Key.create(AccountReset.class, id)).now();

    }

    public static void resetPassword(String resetId, String password) {
        AccountReset ar = getAccountReset(resetId);
        Account account = ObjectifyService.ofy().load().key(Key.create(Account.class, ar.getIdentifier())).now();
        account.setPasswordHash(hash(password));
        ObjectifyService.ofy().save().entity(account);
        ObjectifyService.ofy().delete().entity(ar);
    }
}
