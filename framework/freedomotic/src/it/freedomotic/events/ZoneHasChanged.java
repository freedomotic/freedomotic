package it.freedomotic.events;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.environment.Room;
import it.freedomotic.model.environment.Zone;

/**
 *
 * @author Enrico
 */
public class ZoneHasChanged extends EventTemplate {

    @XStreamOmitField
    private Zone zone = null;
    private Room room = null;

    public ZoneHasChanged(Object source, Zone zone) {
        this.setSender(source);
        this.zone = zone;
        generateEventPayload();
    }

    public ZoneHasChanged(Object source, Room room) {
        this.setSender(source);
        this.room = room;
        generateEventPayload();
    }

    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone.name", zone.getName());
        if (zone != null) {
            payload.addStatement("zone.type", "zone");
        } else {
            if (room != null) {
                payload.addStatement("zone.type", "room");
                payload.addStatement("zone.description", room.getDescription());
            }
        }
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.environment.zone.change";

    }
}
