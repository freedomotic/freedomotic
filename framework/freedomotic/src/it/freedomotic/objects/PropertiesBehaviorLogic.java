package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.PropertiesBehavior;

/**
 *
 * @author enrico
 */
public class PropertiesBehaviorLogic implements BehaviorLogic {

    private PropertiesBehavior data;
    private Listener listener;
    private boolean changed;
    
    public interface Listener {
        
    public void propertyChanged(String key, String value, Config params, boolean fireCommand);
}

    public PropertiesBehaviorLogic(PropertiesBehavior pojo) {
        this.data = pojo;
    }

    @Override
    public synchronized final void filterParams(final Config params, boolean fireCommand) {
        String[] parsed = params.getProperty("value").trim().split("=");
        String key = parsed[0].trim();
        String value = parsed[1].trim();

//        if (key != null && value != null && !key.isEmpty() && !value.isEmpty()) {
//            String currentValue = data.getProperty(key);
//            if (currentValue != null) {
//                if (!currentValue.equalsIgnoreCase(value)) {
//                    //notify the user wants to change a property in the list
//                    params.setProperty(key, value);
//                    listener.propertyChanged(key, value, params, fireCommand);
//                }
//            }
//        }
    }

    @Override
    public String getName() {
        return data.getName();
    }

//    public String getProperty(String key) {
//        return data.getProperty(key);
//    }
//
//    public void setProperty(String key, String value) {
//        data.setProperty(key, value);
//    }
    @Override
    public boolean isActive() {
        return data.isActive();
    }

    @Override
    public String getValueAsString() {
        return data.toString();
    }

    public void addListener(Listener propertiesBehaviorListener) {
        listener = propertiesBehaviorListener;
    }
    
        @Override
    public boolean isChanged() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setChanged(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
