/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

import it.freedomotic.model.object.EnvObject;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.restapi.server.interfaces.ObjectsResource;

import java.util.ArrayList;
import java.util.Collection;

import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 *
 * @author gpt
 */
public class ObjectsServerResource extends ServerResource implements ObjectsResource {

    private static volatile ArrayList<EnvObject> objects;

    @Override
    protected void doInit() throws ResourceException {
        Collection<EnvObjectLogic> objectsLogic = EnvObjectPersistence.getObjectList();
        objects = new ArrayList<EnvObject>();
        for (EnvObjectLogic objLogic : objectsLogic) {
            this.objects.add(objLogic.getPojo());
        }



    }

    @Override
    public String retrieveXml() {
        String ret = "";
        XStream xstream = FreedomXStream.getXstream();
        ret = xstream.toXML(objects);
        return ret;

    }

    @Override
    public String retrieveJson() {
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.ID_REFERENCES);
        ret = xstream.toXML(objects);
        return ret;
    }

    @Override
    public ArrayList<EnvObject> retrieveObjects() {
        return objects;
    }
}
