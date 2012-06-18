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


    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone.name", zone.getName());
        payload.addStatement("zone.description", zone.getDescription());
        if (room != null) {
            payload.addStatement("zone.type", "room");
        }else{
            payload.addStatement("zone.type", "zone");
        }
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.environment.zone.change";

    }
}
