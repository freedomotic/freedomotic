package com.freedomotic.clients.client.api.proxies;

import com.freedomotic.model.object.EnvObject;
import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Result;

import java.util.List;

public interface ObjectsResourceProxy extends ClientProxy {

    @Get
    public void retrieveObjects(Result<List<EnvObject>> callback);
}