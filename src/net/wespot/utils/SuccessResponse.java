package net.wespot.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public final class SuccessResponse extends JsonResponse {
    private static final Status RESPONSE_STATUS = Status.OK;

    public SuccessResponse() throws JSONException {
        this.responseStatus = RESPONSE_STATUS;
        this.json = new JSONObject().put("success", true);
    }

    public SuccessResponse(String key, Object value) throws JSONException {
        this.responseStatus = RESPONSE_STATUS;
        this.json = new JSONObject().put(key, value);
    }
}
