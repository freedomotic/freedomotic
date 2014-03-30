package com.freedomotic.clients.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

public class RestServerRedirector extends Application {

    @Override
    public Restlet createInboundRoot() {

        Router router = new Router(getContext());

        String restapihost = getContext().getParameters().getFirstValue("restapi.host", "localhost");
        String restapiport = getContext().getParameters().getFirstValue("restapi.port", "8111");

        String target = "http://" + restapihost + ":" + restapiport + "/v2{rr}";
        router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
        Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_SERVER_OUTBOUND);
        router.attach("/v2", redirector);
        router.attach("/", new Directory(getContext(), "war:///"));
        return router;
    }

    ;
	
	public void createRedirector(String ip) {
        System.out.println("creating redirector: " + ip);
        String target = "http://" + ip + "8111/v2{rr}";
        Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_SERVER_OUTBOUND);
//		router.attach("/v1", redirector);
        ((Router) getInboundRoot()).attach("/v2", redirector);
        System.out.println("Finishing redirector");
    }
}
