/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.restapi.server.interfaces;

import com.freedomotic.model.object.EnvObject;

import org.restlet.resource.Get;

/**
 *
 * @author gpt
 */
public interface ObjectResource extends FreedomoticResource {

    @Get("object|gwt_object")
    public EnvObject retrieveObject();
}
