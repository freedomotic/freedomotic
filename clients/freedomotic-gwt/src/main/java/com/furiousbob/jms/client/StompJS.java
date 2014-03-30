package com.furiousbob.jms.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public class StompJS {

    protected interface Resources extends ClientBundle {

        @Source("com/furiousbob/jms/stomp.js")
        TextResource stompjs();
    }
    private static final Resources RESOURCES = GWT.create(Resources.class);
    private static boolean installed = false;

    public static synchronized void install() {
        if (!installed) {
            ScriptElement e = Document.get().createScriptElement();
            e.setText(RESOURCES.stompjs().getText());
            Document.get().getBody().appendChild(e);
            installed = true;
        }
    }

    private StompJS() {
    }
}
