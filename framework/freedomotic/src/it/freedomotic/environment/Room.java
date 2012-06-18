/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.environment;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.objects.impl.Gate;
import it.freedomotic.util.Edge;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author enrico
 */
public class Room extends ZoneLogic {

    private ArrayList<Gate> gates;
    public ArrayList<Room> reachable;

    public void addGate(Gate gate) {
        try {
            gates.add(gate);
            Freedomotic.environment.getGraph().add(gate.getFrom(), gate.getTo(), gate);
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private void addLink(Room link) {
        if ((!reachable.contains(link)) && (link != this)) {
            reachable.add(link);
        }
    }

    @Override
    public void init() {
        super.init();
        if (gates == null) {
            gates = new ArrayList<Gate>();
        }
        if (reachable == null) {
            reachable = new ArrayList<Room>();
        }
        getPojo().setAsRoom(true);
    }

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

            if (Freedomotic.environment.getGraph().getEdgeSet(node) != null) { //if this room (the node) has adiacent rooms
                for (Object object : Freedomotic.environment.getGraph().getEdgeSet(node)) {
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
                    } else {
                        //Freedomotic.logger.info("    This arch is visited)");
                    }
                }
            }
        }
    }

    public String getDescription() {
        return getPojo().getDescription();
    }

    public void setDescription(String description) {
        getPojo().setDescription(description);
    }

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
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}
