package it.freedomotic.gwtclient.client.api.proxies;

import java.util.ArrayList;

import it.freedomotic.model.environment.Environment;

import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Result;

public interface EnvironmentsResourceProxy extends ClientProxy {

    @Get
    public void retrieve(Result<ArrayList<Environment>> callback);
}
