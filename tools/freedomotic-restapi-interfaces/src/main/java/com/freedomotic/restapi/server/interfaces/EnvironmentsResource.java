/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.restapi.server.interfaces;

import com.freedomotic.model.environment.Environment;

import java.util.ArrayList;

import org.restlet.resource.Get;

/**
 *
 * @author gpt
 */
public interface EnvironmentsResource extends FreedomoticResource {

    @Get("object|gwt_object")
    public ArrayList<Environment> retrieveEnvironments();
}
