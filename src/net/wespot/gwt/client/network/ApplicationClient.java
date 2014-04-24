package net.wespot.gwt.client.network;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

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
public class ApplicationClient extends GenericClient{

    private static ApplicationClient instance;

    private ApplicationClient() {
    }
    public static ApplicationClient getInstance() {
        if (instance == null) instance = new ApplicationClient();
        return instance;
    }

    public String getUrl() {
        return super.getUrl() + "apps";
    }

    public void createApp(String appName, String clientId, String sharedSecret, String redirectUri, JsonCallback jsonCallback) {
        JSONObject account = new JSONObject();
        account.put("appName", new JSONString(appName));
        account.put("clientId", new JSONString(clientId));
        account.put("sharedSecret", new JSONString(sharedSecret));
        account.put("redirectUri", new JSONString(redirectUri));
        invokeJsonPOST("/createApplication", account, jsonCallback);

    }
}
