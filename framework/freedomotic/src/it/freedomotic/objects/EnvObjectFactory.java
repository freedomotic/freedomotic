package it.freedomotic.objects;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.EnvObjectMappingException;
import it.freedomotic.model.object.EnvObject;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class EnvObjectFactory {

    /**
     * Instantiate the right logic manager for an object pojo using the
     * pojo "type" field
     * @param pojo
     * @return
     */
    public static EnvObjectLogic create(EnvObject pojo) {
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        try {
            Class clazz = classLoader.loadClass(pojo.getHierarchy()); //eg: it.freedomotic.objects.impl.ElectricDevice

            EnvObjectLogic logic = (EnvObjectLogic) clazz.newInstance();
            logic.setPojo(pojo);
            try {
                logic.init();
            } catch (EnvObjectMappingException envObjectMappingException) {
                logic.setMessage("This object has a not valid command mapping.\nCheck it out otherwise it may not work.");
            }
            if ((pojo.getPhisicalAddress().equalsIgnoreCase("unknown") 
                    || pojo.getProtocol().equalsIgnoreCase("unknown")) 
                    && !pojo.getActAs().equalsIgnoreCase("virtual")){
                logic.setMessage(logic.getMessage() + "\nThis object has unknown protocol or address value.\nCheck it out otherwise it may not work.");
            }
            return logic;
        } catch (InstantiationException ex) {
            Logger.getLogger(EnvObjectFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EnvObjectFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Freedomotic.logger.severe("Class '" + pojo.getHierarchy() + "' not found. "
                    + "The related object plugin is not "
                    + "loaded succesfully or you have a wrong hierarchy "
                    + "value in your XML definition of the object."
                    + "The hierarchy value is composed by the package name plus the java file name "
                    + "like it.freedomotic.objects.impl.Light not it.freedomotic.objects.impl.ElectricDevice.Light");
        }
        throw new RuntimeException("Exception while creating an Environment Object Logic from XML description.");
    }
}
