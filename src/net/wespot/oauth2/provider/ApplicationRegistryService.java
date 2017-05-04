package net.wespot.oauth2.provider;

import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.ApplicationRegistry;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.wespot.utils.ErrorJson;
import net.wespot.utils.SuccessJson;

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
@Path("/apps")
public class ApplicationRegistryService {
    static {
        ObjectifyService.register(ApplicationRegistry.class);
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/createApplication")
    public Response createApplication(String application) throws JSONException {
        try {
            JSONObject applicationJson = new JSONObject(application);

            ApplicationRegistry app = new ApplicationRegistry();
            app.setRedirectUri(applicationJson.getString("redirectUri"));
            app.setClientSecret(applicationJson.getString("sharedSecret"));
            app.setClientId(applicationJson.getString("clientId"));
            app.setIdentifier(applicationJson.getString("clientId"));
            app.setApplicationName(applicationJson.getString("appName"));

            ObjectifyService.ofy().save().entity(app).now();

            SuccessJson successJson = new SuccessJson("application", applicationJson);
            return Response.ok(successJson.getJson()).build();
        } catch (JSONException e) {
            ErrorJson errorJson = new ErrorJson("Invalid application JSON");
            return Response.status(Status.BAD_REQUEST).entity(errorJson.getJson()).build();
        }
    }
}
