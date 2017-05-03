package net.wespot.oauth2.provider;

import com.googlecode.objectify.ObjectifyService;
import net.wespot.db.ApplicationRegistry;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Path("/createApplication")
    public String createApplication(@DefaultValue("application/json") @HeaderParam("Content-Type") String contentType,
                                    @DefaultValue("application/json") @HeaderParam("Accept") String accept,
                                    String application) {

        try {
            JSONObject applicationJson = new JSONObject(application);

            ApplicationRegistry app = new ApplicationRegistry();
            app.setRedirectUri(applicationJson.getString("redirectUri"));
            app.setClientSecret(applicationJson.getString("sharedSecret"));
            app.setClientId(applicationJson.getString("clientId"));
            app.setIdentifier(applicationJson.getString("clientId"));
            app.setApplicationName(applicationJson.getString("appName"));

            ObjectifyService.ofy().save().entity(app).now();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return "{}";
    }
}
