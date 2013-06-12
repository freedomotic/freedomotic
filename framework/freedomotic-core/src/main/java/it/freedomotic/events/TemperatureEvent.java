package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.model.environment.Zone;

/**
 *
 * @author Enrico
 */
public class TemperatureEvent extends EventTemplate {

    private int temperature;
    private String zone;

    public TemperatureEvent(Object source, int temperature, Zone z) {
        this.temperature = temperature;
        zone = z.getName();
        generateEventPayload();
    }


    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone", zone);
        payload.addStatement("temperature", temperature);
    }

    @Override
    public String toString() {
        return ("Temperature in " + zone + " is " + temperature + "°C");
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.temperature";
    }
}
