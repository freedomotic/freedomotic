/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.restapiv3.representations;

import com.freedomotic.core.Condition;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.TriggerResource;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.UserCommandResource;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Matteo Mazzoni
 */
@XmlRootElement
public class ReactionRepresentation {

    private List<Condition> conditions = new ArrayList<Condition>();

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getTriggerURI() {
        return triggerURI;
    }

    public void setTriggerURI(String triggerURI) {
        this.triggerURI = triggerURI;
    }

    private String shortDescription;

    private String uuid;

    private String description;

    private String triggerURI;

    private String triggerUuid;

    /**
     * Get the value of triggerUuid
     *
     * @return the value of triggerUuid
     */
    public String getTriggerUuid() {
        return triggerUuid;
    }

    /**
     * Set the value of triggerUuid
     *
     * @param triggerUuid new value of triggerUuid
     */
    public void setTriggerUuid(String triggerUuid) {
        this.triggerUuid = triggerUuid;
    }

    private List<HashMap<String, String>> commands = new ArrayList<HashMap<String, String>>();

    public ReactionRepresentation() {

    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDescription() {
        return description;
    }

    public List<HashMap<String, String>> getCommands() {
        return commands;
    }

    public ReactionRepresentation(Reaction r) {
        this.description = r.getDescription();
        this.shortDescription = r.getShortDescription();
        this.triggerURI = UriBuilder.fromResource(TriggerResource.class).path(r.getTrigger().getUUID()).build().toString();
        this.triggerUuid = r.getTrigger().getUUID();
        for (Command c : r.getCommands()) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("uuid", c.getUuid());
            hm.put("uri", UriBuilder.fromResource(UserCommandResource.class).path(c.getUuid()).build().toString());
            commands.add(hm);
        }
        this.uuid = r.getUuid();
        this.conditions = r.getConditions();
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTrigger(String trigger) {
        this.triggerURI = trigger;
    }

    public void setCommands(List<HashMap<String, String>> commands) {
        this.commands = commands;
    }

}
