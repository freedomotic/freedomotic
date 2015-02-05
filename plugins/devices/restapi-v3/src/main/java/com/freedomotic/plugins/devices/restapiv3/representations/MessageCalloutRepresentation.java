/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.representations;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author matteo
 */
@XmlRootElement
public class MessageCalloutRepresentation {
    private final String message;

    public MessageCalloutRepresentation(String message) {
        this.message = message;
    }
    
    public String getMessage(){
        return this.message;
    }
    
    
}
