/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.events;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.api.EventTemplate;

/**
 * Channel <b>app.event.sensor.messages.MESSAGE_TYPE</b> informs that
 * freedomotic needs to inform the user with a message. The message type can be
 * rendered on screen, sent by email or other delivery methods depending on the
 * specified MESSAGE_TYPE. The MESSAGE_TYPE is a simple string like
 * <li>callout<li> <li>mail<li> <li>dialog<li>
 *
 * depending on the value of this string the message is send on a different
 * channel for example 'app.event.sensor.messages.callout' or
 * 'app.event.sensor.messages.mail' so it can be received listened by different
 * plugins that can implement the messaging feature their way.
 *
 * @author Enrico Nicoletti
 */
public class MessageEvent
        extends EventTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(MessageEvent.class.getName());
    private static final long serialVersionUID = 4733356918386875096L;

    /**
     *
     * @param source
     * @param message
     */
    public MessageEvent(Object source, String message) {
        super(source);
        this.getPayload().addStatement("message.text", message);
        //set a default message type
        this.getPayload().addStatement("message.type", "callout");
        //set a default message level
        this.getPayload().addStatement("message.level", "info");
    }

    /**
     * Type of message. For example 'callout', 'mail', ... This is used to build
     * the destination of the message. If this is 'mail' then the destination
     * address will be app.event.sensor.messages.mail if it is 'callout' this
     * event is sent on channel app.event.sensor.messages.callout
     *
     * @param type
     */
    public void setType(String type) {
        this.getPayload().addStatement("message.type", type);
    }

    public void setLevel(String level) {
        this.getPayload().addStatement("message.level", level);
    }

    /**
     * After how many milliseconds this message should expire
     *
     * @param expires
     */
    public void setExpiration(long expires) {
        this.getPayload().addStatement("message.expires",
                new Long(expires).toString());
    }

    /**
     * Sets the name of the sender, for example an email address
     *
     * @param from
     */
    public void setFrom(String from) {
        this.getPayload().addStatement("message.from", from);
    }

    /**
     * Sets the receiver of the message, for example a receiver address. This
     * property can be OPTIONAL or REQUIRED depending on the 'message.type'
     * property (which can be set using setType(String type)
     *
     * @param to
     */
    public void setTo(String to) {
        this.getPayload().addStatement("message.to", to);
    }
    

    /**
     * Sets the path of the message attachment, if any.
     * 
     * it is an OPTIONAL property
     *
     * @param path
     */
    public void setAttachmentPath(String path) {
        this.getPayload().addStatement("message.attachment", path);
    }
    
    /**
     * Sets the path of the attached file, if any.
     * 
     * it is an OPTIONAL property
     *
     * @param file representing the actual attachment
     */
    public void setAttachmentPath(File attachment) {
    	String path = (attachment!=null)?attachment.getAbsolutePath():"";
        this.getPayload().addStatement("message.attachment", path);
    }

    /**
     *
     * @return
     */
    public String getFrom() {
        return getPayload().getStatementValue("message.from");
    }

    /**
     *
     * @return
     */
    public String getTo() {
        return getPayload().getStatementValue("message.to");
    }

    /**
     *
     * @return
     */
    public String getText() {
        return getPayload().getStatementValue("message.text");
    }
    
    /**
     * 
     * @return the absolute attachment path, if any
     */
    public String getAttachmentPath() {
    	return getPayload().getStatementValue("message.attachment");
    }

    
    
    /**
     *
     * @return
     */
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
