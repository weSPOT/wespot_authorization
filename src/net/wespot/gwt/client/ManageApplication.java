package net.wespot.gwt.client;

import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.*;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.form.validator.CustomValidator;
import com.smartgwt.client.widgets.form.validator.MatchesFieldValidator;
import net.wespot.gwt.client.network.AccountClient;
import net.wespot.gwt.client.network.ApplicationClient;
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
public class ManageApplication {
    public void loadPage() {
        final DynamicForm form = new DynamicForm();

        form.setUseAllDataSourceFields(true);

        HeaderItem header = new HeaderItem();
        header.setDefaultValue("Create Application");

        TextItem appName = new TextItem();
        appName.setName("appName");
        appName.setTitle("Application Name");

        TextItem clientId = new TextItem();
        clientId.setName("clientId");
        clientId.setTitle("Client Id");

        TextItem secret = new TextItem();
        secret.setName("secret");
        secret.setTitle("Shared Secret");

        TextItem redirectUri = new TextItem();
        redirectUri.setName("redirectUri");
        redirectUri.setTitle("Redirect Uri");




        ButtonItem submitApplicationButton = new ButtonItem();
        submitApplicationButton.setTitle("Submit Application");
        submitApplicationButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {

                ApplicationClient.getInstance().createApp(
                        form.getValueAsString("appName"),
                        form.getValueAsString("clientId"),
                        form.getValueAsString("secret"),
                        form.getValueAsString("redirectUri"),
                        new JsonCallback() {

                });

            }
        });

        form.setFields(header, appName, clientId, secret, redirectUri, submitApplicationButton);


        RootPanel.get("applications").add(form);

    }
}
