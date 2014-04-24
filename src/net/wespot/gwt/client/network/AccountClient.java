package net.wespot.gwt.client.network;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import java.io.FileInputStream;
import java.security.MessageDigest;

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
public class AccountClient extends GenericClient{

    private static AccountClient instance;

    private AccountClient() {
    }
    public static AccountClient getInstance() {
        if (instance == null) instance = new AccountClient();
        return instance;
    }

    public String getUrl() {
        return super.getUrl() + "account";
    }

    public void createAccount(String username, String password, String firstname, String familyName, String email, String pictureUrl, JsonCallback jsonCallback) {
        JSONObject account = new JSONObject();
        account.put("username", new JSONString(username));
        account.put("password", new JSONString(password));
        account.put("firstname", new JSONString(firstname));
        account.put("familyName", new JSONString(familyName));
        account.put("email", new JSONString(email));
        if (pictureUrl != null) account.put("pictureUrl", new JSONString(pictureUrl));

        invokeJsonPOST("/createAccount", account, jsonCallback);

    }

    public void authenticate(String username, String password, JsonCallback jsonCallback) {
        JSONObject account = new JSONObject();
        account.put("username", new JSONString(username));
        account.put("password", new JSONString(password));
        invokeJsonPOST("/authenticate", account, jsonCallback);

    }


    public void accountExists(String username, JsonCallback jsonCallback) {
        invokeJsonGET("/accountExists/"+username,jsonCallback);
    }
}
