package com.freedomotic.clients.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

public class ConfigurationDialog extends DialogBox {

    private OkCancelDialogCallback okCancelDialogCallback;

    public ConfigurationDialog(OkCancelDialogCallback callback) {
        setText("Configuration");
        setModal(true);
        this.okCancelDialogCallback = callback;


        setHTML("New dialog");

        VerticalPanel verticalPanel = new VerticalPanel();
        setWidget(verticalPanel);
        verticalPanel.setSize("100%", "100%");

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        verticalPanel.add(horizontalPanel);
        verticalPanel.setCellHeight(horizontalPanel, "100%");
        verticalPanel.setCellWidth(horizontalPanel, "100%");
        horizontalPanel.setSize("324px", "29px");

        Label lblBrokerUrl = new Label("Broker url: ");
        horizontalPanel.add(lblBrokerUrl);

        final TextBox txtBrokerUrl = new TextBox();
        horizontalPanel.add(txtBrokerUrl);

        HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
        verticalPanel.add(horizontalPanel_1);
        verticalPanel.setCellWidth(horizontalPanel_1, "100%");
        verticalPanel.setCellHorizontalAlignment(horizontalPanel_1, HasHorizontalAlignment.ALIGN_CENTER);

        Button btnAcept = new Button("Acept");
        btnAcept.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                okCancelDialogCallback.okButtonClick(txtBrokerUrl.getText());
                hide();

            }
        });
        horizontalPanel_1.add(btnAcept);
        horizontalPanel_1.setCellVerticalAlignment(btnAcept, HasVerticalAlignment.ALIGN_MIDDLE);

        Button btnCancel = new Button("Cancel");
        btnCancel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                okCancelDialogCallback.cancelButtonClick();
                hide();
            }
        });
        horizontalPanel_1.add(btnCancel);
        horizontalPanel_1.setCellVerticalAlignment(btnCancel, HasVerticalAlignment.ALIGN_MIDDLE);
        horizontalPanel_1.setCellHorizontalAlignment(btnCancel, HasHorizontalAlignment.ALIGN_RIGHT);
    }
}
