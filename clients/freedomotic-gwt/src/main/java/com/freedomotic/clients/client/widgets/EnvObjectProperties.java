package com.freedomotic.clients.client.widgets;

import com.freedomotic.model.object.EnvObject;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;

public class EnvObjectProperties extends DecoratedPopupPanel {

    public EnvObjectProperties(EnvObject obj) {
        super(true);

        setWidget(new EnvObjectWidget(obj));
    }

    public void refreshObject(EnvObject obj)
    {
        setWidget(new EnvObjectWidget(obj));
    }
}
