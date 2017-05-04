package net.wespot.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public final class SuccessJson {
    private JSONObject successJson;

    public SuccessJson() throws JSONException {
        this.successJson = new JSONObject().put("success", true);
    }

    public SuccessJson(String name, Object value) throws JSONException {
        this.successJson = new JSONObject()
                .put("success", true)
                .put(name, value);
    }

    public String getJson() {
        return this.successJson.toString();
    }
}
