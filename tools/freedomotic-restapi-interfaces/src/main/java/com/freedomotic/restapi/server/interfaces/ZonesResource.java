/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.restapi.server.interfaces;

import com.freedomotic.model.environment.Zone;

import java.util.ArrayList;

import org.restlet.resource.Get;

/**
 *
 * @author gpt
 */
public interface ZonesResource extends FreedomoticResource {

    @Get("object|gwt_object")
    public ArrayList<Zone> retrieveZones();
}
