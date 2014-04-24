package net.wespot.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourcePasswordField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.*;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.form.validator.CustomValidator;
import com.smartgwt.client.widgets.form.validator.LengthRangeValidator;
import com.smartgwt.client.widgets.form.validator.MatchesFieldValidator;
import com.smartgwt.client.widgets.form.validator.RegExpValidator;

import com.google.gwt.core.client.EntryPoint;
import net.wespot.gwt.client.network.AccountClient;
import net.wespot.gwt.client.network.JsonCallback;

import java.util.HashMap;

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
public class Account  {
    private HashMap<String, Boolean> accountExistsMap = new HashMap<String, Boolean>();
    public void loadPage() {
        final DynamicForm form = new DynamicForm();

        form.setUseAllDataSourceFields(true);

        HeaderItem header = new HeaderItem();
        header.setDefaultValue("Registration Form");
        LengthRangeValidator lengthRangeValidator = new LengthRangeValidator();
        lengthRangeValidator.setMin(3);
        lengthRangeValidator.setErrorMessage("Value should be longer then 3 characters");

        final TextItem userName = new TextItem();
        userName.setName("username");
        userName.setTitle("Choose a username");
        userName.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                final String username = (String) event.getValue();
                AccountClient.getInstance().accountExists(form.getValueAsString("username"), new JsonCallback(){
                    public void onJsonReceived(JSONValue jsonValue) {
//                        SC.showPrompt(jsonValue.toString());
                        if (jsonValue.isObject()!=null) {
                            accountExistsMap.put(username, jsonValue.isObject().get("accountExists").isBoolean().booleanValue());
                            userName.validate();
                        }
                    }
            });
         }
        });
        CustomValidator accountDoesNotExist = new CustomValidator() {
            @Override
            protected boolean condition(Object value) {
                if (accountExistsMap.containsKey(form.getValueAsString("username"))) {
                    return !accountExistsMap.get(form.getValueAsString("username"));
                }
                return false;
            }
        };
        accountDoesNotExist.setErrorMessage("Name exists, choose another username");

        CustomValidator notNullValidator = new CustomValidator() {
            @Override
            protected boolean condition(Object value) {

                return value != null;
            }
        };
        notNullValidator.setErrorMessage("Value must not be empty");


        userName.setValidators(accountDoesNotExist, lengthRangeValidator, notNullValidator);


        TextItem name = new TextItem();
        name.setName("firstname");
        name.setTitle("First Name");
        name.setValidators(notNullValidator, lengthRangeValidator);

        TextItem familyName = new TextItem();
        familyName.setName("familyName");
        familyName.setTitle("Last Name");
        familyName.setValidators(notNullValidator,lengthRangeValidator);

        TextItem email = new TextItem();
        email.setName("email");
        email.setTitle("E-mail");
        RegExpValidator emailValidator = new RegExpValidator();
        emailValidator.setErrorMessage("Invalid email address");
        emailValidator.setExpression("^([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}$");

        email.setValidators(notNullValidator,emailValidator);

        TextItem pictureUrl = new TextItem();
        pictureUrl.setName("pictureUrl");
        pictureUrl.setTitle("Picture url (optional)");



        PasswordItem passwordItem = new PasswordItem();
        passwordItem.setName("password");
        passwordItem.setValidators(notNullValidator);

        PasswordItem passwordItem2 = new PasswordItem();
        passwordItem2.setName("password2");
        passwordItem2.setTitle("Password Again");
        passwordItem2.setRequired(true);
        passwordItem2.setLength(20);



        MatchesFieldValidator matchesValidator = new MatchesFieldValidator();
        matchesValidator.setOtherField("password");
        matchesValidator.setErrorMessage("Passwords do not match");
        passwordItem2.setValidators(matchesValidator);

        CheckboxItem acceptItem = new CheckboxItem();
        acceptItem.setName("acceptTerms");
        acceptItem.setTitle("I accept the terms of use.");
        acceptItem.setDefaultValue(false);
        CustomValidator isTrueValidator = new CustomValidator() {

            @Override
            protected boolean condition(Object value) {
                if (new Boolean(true).equals(value)) return true;
                return false;
            }

        };
        isTrueValidator.setErrorMessage("You must accept the terms of use to continue");
        acceptItem.setValidators(isTrueValidator);

        acceptItem.setWidth(150);

        ButtonItem validateItem = new ButtonItem();
        validateItem.setTitle("Validate");
        validateItem.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (form.validate(false)) {
                    AccountClient.getInstance().createAccount(
                            form.getValueAsString("username"),
                            form.getValueAsString("password"),
                            form.getValueAsString("firstname"),
                            form.getValueAsString("familyName"),
                            form.getValueAsString("email"),
                            form.getValueAsString("pictureUrl"),
                            new JsonCallback(){
                                public void onJsonReceived(JSONValue jsonValue) {
                                    if (jsonValue.isObject()!=null) {
                                        Window.open("Login.html", "_self", "");

                                    }
                                }
                            });
                }
            }
        });

        form.setFields(header, userName, name, familyName, email, pictureUrl,passwordItem, passwordItem2, acceptItem, validateItem);

        RootPanel.get("new-account").add(form);

    }
}
