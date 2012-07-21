package it.freedomotic.gwtclient.client.api;

import it.freedomotic.model.object.EnvObject;

import java.util.List;

import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Result;

public interface ObjectsResourceProxy extends ClientProxy{

	 @Get
	 public void retrieve(Result<List<EnvObject>> callback);
	
}