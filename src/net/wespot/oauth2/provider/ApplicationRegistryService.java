package net.wespot.oauth2.provider;

import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.ApplicationRegistry;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import net.wespot.utils.DbUtils;
import net.wespot.utils.ErrorResponse;
import net.wespot.utils.SuccessResponse;

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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/createApplication")
    public Response createApplication(String application) throws JSONException {
        try {
            final JSONObject applicationJson = new JSONObject(application);
            final String clientId = applicationJson.getString("clientId");

            if (DbUtils.getApplication(clientId) != null) {
                return new ErrorResponse("Client id is already taken").build();
            }

            final ApplicationRegistry app = new ApplicationRegistry();
            app.setRedirectUri(applicationJson.getString("redirectUri"));
            app.setClientSecret(applicationJson.getString("sharedSecret"));
            app.setClientId(clientId);
            app.setIdentifier(clientId);
            app.setApplicationName(applicationJson.getString("appName"));

            ObjectifyService.ofy().save().entity(app).now();

            return new SuccessResponse("application", applicationJson).build();
        } catch (JSONException e) {
            return new ErrorResponse("Invalid application JSON").build();
        }
    }
}
