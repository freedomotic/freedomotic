/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Client;
import it.freedomotic.model.ds.Config;
import it.freedomotic.core.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class ObjectPlugin implements Client {

    private Class clazz;
    public File example;

    public ObjectPlugin(Class clazz, File folder) {
        this.clazz = clazz;
        String tmp = clazz.getSimpleName().toLowerCase();
        this.example = new File(folder + "/data/examples/" + tmp + "/" + tmp + ".xobj");
    }

    public void setManifest(File manifest) {
    }

    @Override
    public void setName(String name) {
        //no name change allowed. do nothing
    }

    @Override
    public String getDescription() {
        return "This object is a " + clazz.getSimpleName();
    }

    @Override
    public void setDescription(String description) {
        //no change allowed
    }

    @Override
    public String getName() {
        return clazz.getSimpleName();
    }

    @Override
    public String getType() {
        return "Object";
    }

    @Override
    public void start() {
        try {
            EnvObjectPersistence.loadObject(example, true);
        } catch (IOException ex) {
            Logger.getLogger(ObjectPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        //is running if there is already an object of this kind in the map
        boolean found = false;
        for (EnvObjectLogic obj : EnvObjectPersistence.getObjectList()) {
            if (obj.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
                found = true;
            }
        }
        return found;
    }

    public File getExample() {
        return example;
    }

    @Override
    public void showGui() {
    }

    @Override
    public void hideGui() {
    }

    @Override
    public Config getConfiguration() {
        return new Config();
    }
}
