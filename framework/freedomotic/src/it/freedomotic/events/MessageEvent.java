/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import java.util.Date;

/**
 *
 * @author enrico
 */
public class MessageEvent extends EventTemplate {

    public MessageEvent(Object source, String message) {
        super(source);
        this.getPayload().addStatement("message.text", message);
    }

    public void setType(String type) {
        this.getPayload().addStatement("message.type", type);
    }

    public void setMedium(String medium) {
        this.getPayload().addStatement("message.medium", medium);
    }

    public void setExpiration(Date expires) {
        this.getPayload().addStatement("message.expires", expires.toString());
    }

    @Override
    public String getDefaultDestination() {
        //adds the type to channel definition only if is not empty
        String type = "";
        try {
            type = "." + getPayload().getStatements("message.type").get(0).getValue();
        } catch (Exception e) {
        }
        return "app.event.sensor.messages" + type;
    }
}
