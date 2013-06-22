
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.model.environment.Zone;

/**
 *
 * @author Enrico
 */
public class LuminosityEvent extends EventTemplate {

    private static final long serialVersionUID = 1605869382477368794L;
	
	int luminosity;
    String zone;

    public LuminosityEvent(Object source, int temperature, Zone z) {
        this.luminosity = temperature;
        zone = z.getName();
        generateEventPayload();
    }


    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone", zone);
        payload.addStatement("luminosity", luminosity);
    }

    @Override
    public String toString() {
        return ("Luminosity in " + zone + " is " + luminosity + "%");
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.luminosity";
    }
}
