/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Client;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.model.ds.Config;
import it.freedomotic.objects.EnvObjectLogic;
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

    private File example;
    private EnvObjectLogic object;

    public ObjectPlugin(File example) {
        this.example = example;
        try {
            object = EnvObjectPersistence.loadObject(example);
        } catch (IOException ex) {
            Logger.getLogger(ObjectPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public EnvObjectLogic getObject() {
        return object;
    }

    @Override
    public void setName(String name) {
        //no name change allowed. do nothing
    }

    @Override
    public String getDescription() {
        return object.getPojo().getDescription();
    }

    @Override
    public void setDescription(String description) {
        //no change allowed
    }

    @Override
    public String getName() {
        return object.getPojo().getName();
    }

    @Override
    public String getType() {
        return "Object";
    }

    @Override
    public void start() {
        EnvObjectPersistence.add(object, EnvObjectPersistence.MAKE_UNIQUE);
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isRunning() {
        //is running if there is already an object of this kind in the map
//        boolean found = false;
//        for (EnvObjectLogic obj : EnvObjectPersistence.getObjectList()) {
//            if (obj.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
//                found = true;
//            }
//        }
//        return found;
        return true;
    }

    public File getTemplate() {
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
    
    public void startOnEnv(EnvironmentLogic env){
        EnvObjectLogic obj = EnvObjectPersistence.add(object, EnvObjectPersistence.MAKE_UNIQUE);
        obj.setEnv(env);
    }
}
