package net.wespot.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public final class ErrorJson {
    private JSONObject errorJson;

    public ErrorJson(String errorDescription) throws JSONException {
        this.errorJson = new JSONObject().put("error_description", errorDescription);
    }

    public ErrorJson(String errorTitle, String errorDescription) throws JSONException {
        this.errorJson = new JSONObject()
                .put("error_title", errorTitle)
                .put("error_description", errorDescription);
    }

    public String getJson() {
        return this.errorJson.toString();
    }
}
