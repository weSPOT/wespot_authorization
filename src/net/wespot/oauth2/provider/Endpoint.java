package net.wespot.oauth2.provider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import java.net.URISyntaxException;
import org.codehaus.jettison.json.JSONException;

interface Endpoint {
    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response authorizeGet(@Context HttpServletRequest request)
          throws URISyntaxException, OAuthSystemException, JSONException;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response authorize(@Context HttpServletRequest request)
          throws URISyntaxException, OAuthSystemException, JSONException;
}
