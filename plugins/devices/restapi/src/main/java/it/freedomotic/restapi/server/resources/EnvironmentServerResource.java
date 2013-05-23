/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import it.freedomotic.environment.EnvironmentPersistence;
import it.freedomotic.model.environment.Environment;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.restapi.server.interfaces.EnvironmentResource;

import org.restlet.resource.ServerResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 *
 * @author gpt
 */
public class EnvironmentServerResource extends ServerResource implements EnvironmentResource {

    private static volatile Environment env;

    @Override
    public void doInit() {
        int number = Integer.parseInt((String) getRequest().getAttributes().get("number"));
        env = EnvironmentPersistence.getEnvironments().get(number).getPojo();
    }

    @Override
    public String retrieveXml() {
        System.out.println("RetrieveXML");
        String ret = "";
        XStream xstream = FreedomXStream.getXstream();
        ret = xstream.toXML(env);
        return ret;
    }

    @Override
    public String retrieveJson() {
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        ret = xstream.toXML(env);
        return ret;
    }

    @Override
    public Environment retrieveEnvironment() {
        return env;
    }
}
