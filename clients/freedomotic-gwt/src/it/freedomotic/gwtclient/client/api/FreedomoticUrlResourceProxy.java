package it.freedomotic.gwtclient.client.api;

import it.freedomotic.model.environment.Environment;

import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Put;
import org.restlet.client.resource.Result;



public interface FreedomoticUrlResourceProxy extends ClientProxy{

	 @Put("txt")
	 public void configure(String ip, Result<Void> callback);	 
	
}
