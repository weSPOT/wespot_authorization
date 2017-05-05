package net.wespot.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import javax.ws.rs.core.Response.Status;

public final class ErrorResponse extends JsonResponse {
    private static final Status RESPONSE_STATUS = Status.BAD_REQUEST;

    public ErrorResponse(String errorDescription) throws JSONException {
        this.responseStatus = RESPONSE_STATUS;
        this.json = new JSONObject().put("error_description", errorDescription);
    }

    public ErrorResponse(String errorTitle, String errorDescription) throws JSONException {
        this.responseStatus = RESPONSE_STATUS;
        this.json = new JSONObject()
                .put("error_title", errorTitle)
                .put("error_description", errorDescription);
    }
}
