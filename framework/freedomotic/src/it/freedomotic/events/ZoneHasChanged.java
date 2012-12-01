package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.environment.Room;
import it.freedomotic.model.environment.Zone;

/**
 *
 * @author Enrico
 */
public class ZoneHasChanged extends EventTemplate {

    public ZoneHasChanged(Object source, Zone zone) {
        this.setSender(source);
        payload.addStatement("zone.name", zone.getName());
        payload.addStatement("zone.description", zone.getDescription());
        Room room = null; //TODO: just a reminder for this property
        if (room != null) {
            payload.addStatement("zone.type", "room");
        } else {
            payload.addStatement("zone.type", "zone");
        }
    }

    @Override
    protected void generateEventPayload() {
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.environment.zone.change";

    }
}
