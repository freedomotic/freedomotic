/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.model.object;


/**
 *
 * @author enrico
 */
public class PropertiesBehavior extends Behavior {

    private Properties properties = new Properties();

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    @Override
    public String toString() {
        if (properties.size() < 2) {
            return properties.size() + " records";
        } else {
            return properties.size() + " records";
        }
    }
}
