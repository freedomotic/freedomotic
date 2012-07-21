package it.freedomotic.gwtclient.server;

import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

public class FreedomoticUrl extends ServerResource{
	    
	
	 @Override
	    public void doInit() { 
		 System.out.println("doinit");
	 }
	  @Put
	  public StringRepresentation configure(String ip)
	  {
		  System.out.println("configRedirector");
		  ((RestServerRedirector)this.getApplication()).createRedirector(ip);
		  return new StringRepresentation(ip);
	  }
	  
	  	    
}
