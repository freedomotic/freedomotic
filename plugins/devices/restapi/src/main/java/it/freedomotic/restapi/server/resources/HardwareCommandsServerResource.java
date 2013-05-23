/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.restapi.server.interfaces.CommandsResource;

import java.util.ArrayList;

import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 *
 * @author gpt
 */
public class HardwareCommandsServerResource extends ServerResource implements CommandsResource {

    private static volatile ArrayList<Command> commands;

    @Override
    protected void doInit() throws ResourceException {
        commands = new ArrayList<Command>(CommandPersistence.getHardwareCommands());
//        for (Iterator it = CommandPersistence.iterator(); it.hasNext();) {
//            Command command = (Command) it.next();
//            commands.add(command);
//        }
    }

    @Override
    public String retrieveXml() {
        String ret = "";
        XStream xstream = FreedomXStream.getXstream();
        ret = xstream.toXML(commands);
        return ret;
    }

    @Override
    public String retrieveJson() {
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        ret = xstream.toXML(commands);
        return ret;
    }
}
