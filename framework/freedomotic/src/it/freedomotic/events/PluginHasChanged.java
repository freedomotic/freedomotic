/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;

/**
 *
 * @author Enrico
 */
public class PluginHasChanged extends EventTemplate {

    public enum PluginActions {

        SHOW, HIDE, DISPOSE, MAXIMIZE, MINIMIZE, START, STOP, ENQUEUE, DEQUEUE, DESCRIPTION
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
