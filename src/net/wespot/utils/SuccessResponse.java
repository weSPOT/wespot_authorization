package net.wespot.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import javax.ws.rs.core.Response.Status;

/**
 * ****************************************************************************
 * Copyright (C) 2013-2017 Open Universiteit Nederland
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
 * Contributors: Rafael Klaessen
 * ****************************************************************************
 */
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
