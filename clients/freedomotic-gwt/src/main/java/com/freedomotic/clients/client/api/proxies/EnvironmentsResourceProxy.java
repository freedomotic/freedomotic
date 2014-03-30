package com.freedomotic.clients.client.api.proxies;


import com.freedomotic.model.environment.Environment;
import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Result;

import java.util.ArrayList;

public interface EnvironmentsResourceProxy extends ClientProxy {

    @Get
    public void retrieveEnvironments(Result<ArrayList<Environment>> callback);
}
