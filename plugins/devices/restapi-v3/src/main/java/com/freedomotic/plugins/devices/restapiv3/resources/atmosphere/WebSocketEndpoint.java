/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.resources.atmosphere;

import com.freedomotic.api.EventTemplate;

/**
 *
 * @author enrico
 */
public interface WebSocketEndpoint {

    void broadcast(EventTemplate message);
    
}
