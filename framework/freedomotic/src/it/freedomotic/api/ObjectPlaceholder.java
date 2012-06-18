/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.api;

import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.persistence.EnvObjectPersistence;
import java.io.File;

/**
 *
 * @author enrico
 */
public class ObjectPlaceholder implements Client {

    private Class clazz;
    public File example;

    public ObjectPlaceholder(Class clazz, File folder) {
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
        //if (!isRunning()){
        //add this object to the environment
        //Config configuration = ConfigPersistence.deserialize(new File(folder.getAbsolutePath() + "/manifest.xml"));
        EnvObjectPersistence.loadObjects(example, true);
        //}
    }

    @Override
    public void stop() {
        EnvObjectPersistence.loadObjects(example, true);
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
}
