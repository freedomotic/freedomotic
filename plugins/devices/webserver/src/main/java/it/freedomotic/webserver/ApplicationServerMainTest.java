/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.webserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author gpt
 */
public class ApplicationServerMainTest {

    public static void main(String[] args) throws Exception {

        Server server = new Server(8080);
        String dir = "/home/gpt/Desarrollo/freedomotic/framework/freedomotic/plugins/devices/es.gpulido.webserver/data/webapps/gwt_client";
        WebAppContext context = new WebAppContext();

        context.setDescriptor(dir + "/WEB-INF/web.xml");
        context.setResourceBase("/home/gpt/Desarrollo/freedomotic/framework/freedomotic/plugins/devices/es.gpulido.webserver/data/webapps/gwt_client");
        context.setContextPath("/");
        context.setParentLoaderPriority(true);

        server.setHandler(context);
        server.start();


    }
}
