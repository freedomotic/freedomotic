/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.events;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.objects.impl.Person;

/**
 *
 * @author nicoletti
 */
public final class PersonEntersZone extends EventTemplate {

    private final String uuid;
    private final String zoneName;

    public PersonEntersZone(ZoneLogic aThis, Person p, Zone zone) {
        this.uuid = p.getPojo().getUUID();
        this.zoneName = zone.getName();
        generateEventPayload();
    }

    @Override
    protected void generateEventPayload() {
        payload.addStatement("person.id", uuid);
        payload.addStatement("zone.name", zoneName);
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.person.zone.enter";
    }
    
    public String getPersonId() {
        return uuid;
    }
    
    public String getZoneName() {
        return zoneName;
    }
}
