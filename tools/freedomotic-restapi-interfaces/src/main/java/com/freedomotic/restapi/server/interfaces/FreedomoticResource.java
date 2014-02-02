/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.restapi.server.interfaces;

import org.restlet.resource.Get;

/**
 *
 * @author gpt
 */
public interface FreedomoticResource {

    @Get("json")
    public String retrieveJson();

    @Get("xml")
    public String retrieveXml();
}
