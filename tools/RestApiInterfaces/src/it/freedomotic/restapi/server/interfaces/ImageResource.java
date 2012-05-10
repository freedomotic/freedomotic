/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.interfaces;

import org.restlet.representation.FileRepresentation;
import org.restlet.resource.Get;

/**
 *
 * @author gpt
 */
public interface ImageResource {
    
    @Get
    public String getImagePath();
}
