/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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
package com.freedomotic.environment;

import com.freedomotic.model.environment.Zone;
import com.freedomotic.things.GenericGate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author enrico
 */
public class Room
        extends ZoneLogic {

    private List<GenericGate> gates;
    private List<Room> reachable;

    /**
     *
     * @param pojo
     */
    public Room(Zone pojo) {
        super(pojo);
    }

    /**
     *
     * @param gate
     */
    @RequiresPermissions("zones:update")    
    public void addGate(GenericGate gate) {
        try {
            gates.add(gate);
            getEnv().getGraph().add(gate.getFrom(),
                    gate.getTo(),
                    gate);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while adding a Gate", e);
        }
    }

    @RequiresPermissions("zones:update")    
    private void addLink(Room link) {
        if ((!reachable.contains(link)) && (link != this)) {
            reachable.add(link);
        }
    }

    /**
     *
     * @param env
     */
    @Override
    @RequiresPermissions("zones:read")    
    public void init(EnvironmentLogic env) {
        super.init(env);

        if (gates == null) {
            gates = new ArrayList<GenericGate>();
        }

        if (reachable == null) {
            reachable = new ArrayList<Room>();
        }

        getPojo().setAsRoom(true);
    }

    /**
     *
     */
    @RequiresPermissions("zones:read")    
    public void visit() {
        //reset current links
        reachable.clear();

        LinkedBlockingQueue<Room> queue = new LinkedBlockingQueue<Room>();
        ArrayList<GraphEdge> visited = new ArrayList<GraphEdge>();
        queue.add(this);

        while (!queue.isEmpty()) {
            // operazione di dequeue
            Room node = queue.poll();

            //LOG.info("Evaluating node " + node.getPojo().getName());
            if (getEnv().getGraph().getEdgeSet(node) != null) { //if this room (the node) has adiacent rooms

                for (Object object : getEnv().getGraph().getEdgeSet(node)) {
                    GraphEdge adiacent = (GraphEdge) object;

                    //LOG.info("  " + node.getPojo().getName() + " is linked with arch " + adiacent.toString());
                    if (!visited.contains(adiacent)) {
                        //LOG.info("    This arch is not visited ");
                        visited.add(adiacent);

                        //operazione di enqueue coda
                        Room x = (Room) adiacent.getX();
                        Room y = (Room) adiacent.getY();
                        GenericGate gate = (GenericGate) adiacent.getValue();
                        boolean open = gate.isOpen();

                        if (open) {
                            if (x.getPojo().getName().equalsIgnoreCase(node.getPojo().getName())) {
                                queue.offer(y);
                                addLink(node); //from node
                                addLink(y); //to y
                                //LOG.info("From " + node.getPojo().getName() + " you can reach " + y.getPojo().getName());
                            } else {
                                queue.offer(x);
                                addLink(node);
                                addLink(x);

                                //LOG.info("From " + node.getPojo().getName() + " you can reach " + x.getPojo().getName());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("zones:read")    
    public String getDescription() {
        return getPojo().getDescription();
    }

    /**
     *
     * @param description
     */
    @RequiresPermissions("zones:update")    
    public void setDescription(String description) {
        getPojo().setDescription(description);
    }

    /**
     *
     */
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

    /**
     *
     * @param obj
     * @return
     */
    @Override
    @RequiresPermissions("zones:read")    
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     *
     * @return
     */
    @Override
    @RequiresPermissions("zones:read")    
    public int hashCode() {
        int hash = 3;

        return hash;
    }
    private static final Logger LOG = Logger.getLogger(Room.class.getName());
}
