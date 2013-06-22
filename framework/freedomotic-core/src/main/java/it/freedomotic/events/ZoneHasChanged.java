package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.environment.Room;
import it.freedomotic.model.environment.Zone;

/**
 * Channel <b>app.event.sensor.environment.zone.change</b> informs that a zone
 * (eg: an environment room) has changed its state (zone description or zone topology)
 * @author Enrico
 */
public class ZoneHasChanged extends EventTemplate {

    private static final long serialVersionUID = -2676123835322299252L;

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
