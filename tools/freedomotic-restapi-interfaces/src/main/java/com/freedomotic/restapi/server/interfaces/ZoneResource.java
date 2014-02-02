/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.restapi.server.interfaces;

/**
 *
 * @author gpt
 */
import com.freedomotic.model.environment.Zone;

import org.restlet.resource.Get;

/**
 *
 * @author gpt
 */
public interface ZoneResource extends FreedomoticResource {

    @Get("object|gwt_object")
    public Zone retrieveZone();
}