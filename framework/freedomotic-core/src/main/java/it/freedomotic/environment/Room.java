/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.environment;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.model.environment.Zone;

import it.freedomotic.objects.impl.Gate;

import it.freedomotic.util.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author enrico
 */
public class Room
        extends ZoneLogic {

    private List<Gate> gates;
    private List<Room> reachable;

    public Room(Zone pojo) {
        super(pojo);
    }

    @RequiresPermissions("zones:update")    
    public void addGate(Gate gate) {
        try {
            gates.add(gate);
            getEnv().getGraph().add(gate.getFrom(),
                    gate.getTo(),
                    gate);
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    @RequiresPermissions("zones:update")    
    private void addLink(Room link) {
        if ((!reachable.contains(link)) && (link != this)) {
            reachable.add(link);
        }
    }

    @Override
    @RequiresPermissions("zones:read")    
    public void init(EnvironmentLogic env) {
        super.init(env);

        if (gates == null) {
            gates = new ArrayList<Gate>();
        }

        if (reachable == null) {
            reachable = new ArrayList<Room>();
        }

        getPojo().setAsRoom(true);
    }

    @RequiresPermissions("zones:read")    
    public void visit() {
        //reset current links
        reachable.clear();

        LinkedBlockingQueue<Room> queue = new LinkedBlockingQueue<Room>();
        ArrayList<Edge> visited = new ArrayList<Edge>();
        queue.add(this);

        while (!queue.isEmpty()) {
            // operazione di dequeue
            Room node = queue.poll();

            //Freedomotic.logger.info("Evaluating node " + node.getPojo().getName());
            if (getEnv().getGraph().getEdgeSet(node) != null) { //if this room (the node) has adiacent rooms

                for (Object object : getEnv().getGraph().getEdgeSet(node)) {
                    Edge adiacent = (Edge) object;

                    //Freedomotic.logger.info("  " + node.getPojo().getName() + " is linked with arch " + adiacent.toString());
                    if (!visited.contains(adiacent)) {
                        //Freedomotic.logger.info("    This arch is not visited ");
                        visited.add(adiacent);

                        //operazione di enqueue coda
                        Room x = (Room) adiacent.getX();
                        Room y = (Room) adiacent.getY();
                        Gate gate = (Gate) adiacent.getValue();
                        boolean open = gate.isOpen();

                        if (open) {
                            if (x.getPojo().getName().equalsIgnoreCase(node.getPojo().getName())) {
                                queue.offer(y);
                                addLink(node); //from node
                                addLink(y); //to y
                                //Freedomotic.logger.info("From " + node.getPojo().getName() + " you can reach " + y.getPojo().getName());
                            } else {
                                queue.offer(x);
                                addLink(node);
                                addLink(x);

                                //Freedomotic.logger.info("From " + node.getPojo().getName() + " you can reach " + x.getPojo().getName());
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresPermissions("zones:read")    
    public String getDescription() {
        return getPojo().getDescription();
    }

    @RequiresPermissions("zones:update")    
    public void setDescription(String description) {
        getPojo().setDescription(description);
    }

    @RequiresPermissions("zones:update")    
    public void updateDescription() {
        StringBuilder buff = new StringBuilder();

        for (Room room : reachable) {
            buff.append(room.getPojo().getName()).append(" ");
        }

        if (!buff.toString().isEmpty()) {
            setDescription("From " + this.getPojo().getName() + " you can reach " + buff.toString());
        } else {
            setDescription("");
        }

        this.setChanged();
    }

    @Override
    @RequiresPermissions("zones:read")    
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    @RequiresPermissions("zones:read")    
    public int hashCode() {
        int hash = 3;

        return hash;
    }
}
