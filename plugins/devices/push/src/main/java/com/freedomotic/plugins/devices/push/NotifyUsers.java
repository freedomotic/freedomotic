/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.freedomotic.plugins.devices.push;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.behaviors.PropertiesBehaviorLogic;
import com.freedomotic.things.GenericPerson;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Map.Entry;

/**
 *
 * @author matteo
 */
public class NotifyUsers extends Protocol{

    public NotifyUsers() {
       //every plugin needs a name and a manifest XML file
        super("NotifyUsers", "/push/notifyusers-manifest.xml");
        setPollingWait(-1); //millisecs interval between hardware device status reads
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        // receives a command for notifying a user.
        for (String username : c.getProperty("notify.users").split(",")){
            // user has to be a Person envobject, as we're using his property behavior
            
            for (EnvObjectLogic person : getApi().things().findByName(username)){
                if (person instanceof GenericPerson){
                    MessageEvent mess = new MessageEvent(null, c.getProperty("push.message"));
                    mess.setType("notify.user." + username);
            
                    // insert command properties
                    for (Entry<Object, Object> prop :c.getProperties().entrySet()){
                        mess.addProperty("reason." + prop.getKey().toString(),prop.getValue().toString());
                    }
                   
                    // enriches event and sent it to the user's message channel 
                    PropertiesBehaviorLogic props = (PropertiesBehaviorLogic) person.getBehavior("properties");
                    for (String prop : props.getPojo().getPropertiesName()){
                        mess.addProperty("user." + prop, props.getPojo().getProperty(prop));
                    }
                   
                    mess.addProperty("user.activity", person.getBehavior("activity").getValueAsString());
                    mess.addProperty("user.present", person.getBehavior("present").getValueAsString());
                    
                    notifyEvent(mess);  
                }
            }
            
        }
        // after that, we use reactions to define notification rules 
        // like: if user (receives a message AND) is online  -> speak the message loud 
        // or:   if user (receives a message AND) is offline -> send him a notification through his preferred provider 
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onEvent(EventTemplate event) {
        
    }
    
}
