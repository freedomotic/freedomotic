/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;

/**
 * Channel <b>app.event.sensor.plugin.change</b> informs about plugin related 
 * events like plugin started, stopped, description
 * changes, and so on.
 * 
 * @author Enrico
 */
public class PluginHasChanged extends EventTemplate {

    private static final long serialVersionUID = 5203339184820441643L;

	public enum PluginActions {
        SHOW, HIDE, 
        DESCRIPTION,
        START, STOP, DISPOSE, 
        MAXIMIZE, MINIMIZE, 
        ENQUEUE, DEQUEUE
   };


    public PluginHasChanged(Object source, String pluginName, PluginActions action) {

        payload.addStatement("plugin", pluginName);
        payload.addStatement("action", action.toString());

    }

    @Override
    protected void generateEventPayload() {
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.plugin.change";
    }
}
