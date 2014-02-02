/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.restapi.server.interfaces;

import com.freedomotic.restapi.model.PluginPojo;

import java.util.ArrayList;

import org.restlet.resource.Get;

/**
 *
 * @author gpt
 */
public interface PluginsResource extends FreedomoticResource {

    @Get("object|gwt_object")
    public ArrayList<PluginPojo> retrievePlugins();
}
