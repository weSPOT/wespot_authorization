package net.wespot.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import javax.ws.rs.core.Response.Status;

public final class ErrorResponse extends JsonResponse {
    public ErrorResponse(String errorDescription) throws JSONException {
        this.responseStatus = Status.BAD_REQUEST;
        this.json = new JSONObject().put("error_description", errorDescription);
    }
}
