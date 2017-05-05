package net.wespot.utils;

import org.codehaus.jettison.json.JSONObject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

abstract class JsonResponse {
    protected JSONObject json;
    protected Status responseStatus;

    public String getJson() {
        return this.json.toString();
    }

    public Response build() {
        return Response.status(this.responseStatus).entity(getJson()).build();
    }
}
