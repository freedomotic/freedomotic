/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

public class UserServerResource extends ServerResource {

    @Override
    public void doInit() {
        if (((String) getRequest().getAttributes().get("useraction")).equals("logout")) {
            getRequest().getClientInfo().setAuthenticated(false);
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
    }

    @Override
    public Representation get() {
        return null;
    }

    @Override
    public Representation post(Representation entity) {
        // Handle post
        // ...
        return null;
    }
}
