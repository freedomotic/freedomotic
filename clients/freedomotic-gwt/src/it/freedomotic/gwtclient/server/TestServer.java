package it.freedomotic.gwtclient.server;

import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Test component that serves the GWT client page and acts as a server targetted
 * by AJAX calls sent from the client page.
 */
public class TestServer extends Component {

    public static void main(String[] args) throws Exception {
        TestServer testServer = new TestServer();
        testServer.getServers().add(Protocol.HTTP, 8889);

        testServer.start();

        System.out
                .println("Visit http://localhost:8889/SimpleExample.html in your browser.");

    }

    public TestServer() {
//        getClients().add(Protocol.CLAP);
//        getClients().add(Protocol.FILE);
//        getClients().add(Protocol.WAR);
    	getClients().add(Protocol.HTTP);
        getDefaultHost().attach(new RestServerRedirector());
    }

}
