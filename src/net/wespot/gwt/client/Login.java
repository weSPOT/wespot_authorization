package net.wespot.gwt.client;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.*;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.form.validator.CustomValidator;
import com.smartgwt.client.widgets.form.validator.MatchesFieldValidator;
import com.smartgwt.client.widgets.form.validator.Validator;
import net.wespot.gwt.client.network.AccountClient;
import net.wespot.gwt.client.network.JsonCallback;

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
public class Login {
    public static final String AUTH_COOKIE = "net.wespot.authToken";

    public void loadPage() {
        final DynamicForm form = new DynamicForm();

        form.setUseAllDataSourceFields(true);

        HeaderItem header = new HeaderItem();
        header.setDefaultValue("Login");
        TextItem userName = new TextItem();
        userName.setName("username");
        userName.setTitle("Username");

        final PasswordItem passwordItem = new PasswordItem();

        passwordItem.setName("password");
        passwordItem.setTitle("Password");


        ButtonItem submit = new ButtonItem();
        submit.setTitle("Submit");
        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {

                    AccountClient.getInstance().authenticate(form.getValueAsString("username"), form.getValueAsString("password"), new JsonCallback() {
                        public void onJsonReceived(JSONValue jsonValue) {
                            if (jsonValue.isObject() != null) {
                                if (jsonValue.isObject().containsKey("userName")) {
                                    SC.ask("result", "username incorrect", new BooleanCallback() {
                                        @Override
                                        public void execute(Boolean value) {

                                        }
                                    });
//                                    SC.showPrompt("username incorrect");
                                }

                                if (jsonValue.isObject().containsKey("password")) {
                                    SC.ask("result", "password incorrect", new BooleanCallback() {
                                        @Override
                                        public void execute(Boolean value) {

                                        }
                                    });
                                }
                                if (jsonValue.isObject().containsKey("token")) {

                                    Cookies.setCookie(AUTH_COOKIE, jsonValue.isObject().get("token").isString().stringValue());
                                    if (Window.Location.getParameter("redirect_uri")!= null) {
//                                        SC.ask("result", "about to open "+"/oauth/auth?redirect_uri="+Window.Location.getParameter("redirect_uri")+
//                                                "&client_id="+Window.Location.getParameter("client_id")+
//                                                "&response_type="+Window.Location.getParameter("response_type")+
//                                                "&scope="+Window.Location.getParameter("scope"), new BooleanCallback() {
//                                            @Override
//                                            public void execute(Boolean value) {
//
//                                            }
//                                        });
//                                        Window.open("/oauth/auth?redirect_uri="+Window.Location.getParameter("redirect_uri")+
//                                                "&client_id="+Window.Location.getParameter("client_id")+
//                                                "&response_type="+Window.Location.getParameter("response_type")+
//                                                "&scope="+Window.Location.getParameter("scope"), "_self", "");

                                        Window.Location.replace("/oauth/auth?redirect_uri="+Window.Location.getParameter("redirect_uri")+
                                                "&client_id="+Window.Location.getParameter("client_id")+
                                                "&response_type="+Window.Location.getParameter("response_type")+
                                                "&scope="+Window.Location.getParameter("scope"));
                                    } else {

                                        SC.say("Login Successful");
                                    }
                                }
                            }
                        };

                    });
//                final String oldValue = form.getValueAsString("password");
//                CustomValidator matchesValidator = new CustomValidator(){
//                    @Override
//                    protected boolean condition(Object value) {
//                        return !oldValue.equals(form.getValue("password"));
//
//                    }
//                };
//                matchesValidator.setErrorMessage("Username/password incorrect");
//                passwordItem.setValidators(matchesValidator);
//                form.validate();

            }
        });

        form.setFields(header, userName, passwordItem, submit);

        RootPanel.get("login").add(form);

    }
}
