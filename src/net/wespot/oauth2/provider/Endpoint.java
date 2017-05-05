package net.wespot.oauth2.provider;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import java.net.URISyntaxException;
import org.codehaus.jettison.json.JSONException;

interface Endpoint {
    @GET
    Response authorizeGet(@Context HttpServletRequest request)
          throws URISyntaxException, OAuthSystemException, JSONException;

    @POST
    Response authorize(@Context HttpServletRequest request)
          throws URISyntaxException, OAuthSystemException, JSONException;
}