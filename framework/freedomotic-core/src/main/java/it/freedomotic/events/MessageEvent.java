/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;

/**
 * Channel <b>app.event.sensor.messages.MESSAGE_TYPE</b> informs that freedomotic
 * needs to inform the user with a message. The message type can be rendered on screen,
 * sent by email or other delivery methods depending on the specified MESSAGE_TYPE.
 * The MESSAGE_TYPE is a simple string like
 * <li>callout<li>
 * <li>mail<li>
 * <li>dialog<li>
 * 
 * depending on the value of this string the message is send on a different channel
 * for example 'app.event.sensor.messages.callout' or 'app.event.sensor.messages.mail'
 * so it can be received listened by different plugins that can implement the
 * messaging feature their way.
 * 
 * @author enrico
 */
public class MessageEvent extends EventTemplate {

    private static final long serialVersionUID = 4733356918386875096L;

	public MessageEvent(Object source, String message) {
        super(source);
        this.getPayload().addStatement("message.text", message);
        //set a default message type
        this.getPayload().addStatement("message.type", "callout");
    }

    /**
     * Type of message. For example 'callout', 'mail', ...
     * This is used to build the destination of the message. If this is
     * 'mail' then the destination address will be
     * app.event.sensor.messages.mail
     * if it is 'callout' this event is sent on channel
     * app.event.sensor.messages.callout
     * @param type 
     */
    public void setType(String type) {
        this.getPayload().addStatement("message.type", type);
    }

    /**
     * After how many milliseconds this message should expire
     * @param expires 
     */
    public void setExpiration(long expires) {
        this.getPayload().addStatement("message.expires", new Long(expires).toString());
    }
    
    /**
     * Sets the name of the sender, for example an email address
     * @param from 
     */
    public void setFrom(String from) {
        this.getPayload().addStatement("message.from", from);
    }
    
    /**
     * Sets the receiver of the message, for example a receiver address.
     * This property can be OPTIONAL or REQUIRED depending on the 'message.type'
     * property (which can be set using setType(String type)
     * @param to 
     */
    public void setTo(String to) {
        this.getPayload().addStatement("message.to", to);
    }
    
    public String getFrom(){
        return getPayload().getStatementValue("message.from");
    }
    
    public String getTo(){
        return getPayload().getStatementValue("message.to");
    }
    
    public String getText(){
        return getPayload().getStatementValue("message.text");
    }

    @Override
    public String getDefaultDestination() {
        //adds the type to channel definition only if is not empty
        String type = "";
        try {
            type = "." + getPayload().getStatements("message.type").get(0).getValue().toLowerCase().trim();
        } catch (Exception e) {
        }
        return "app.event.sensor.messages" + type;
    }
}
