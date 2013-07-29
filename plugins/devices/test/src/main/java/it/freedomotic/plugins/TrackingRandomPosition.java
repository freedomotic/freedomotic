/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.environment.EnvironmentPersistence;

import it.freedomotic.exceptions.UnableToExecuteException;

import it.freedomotic.model.geometry.FreedomPoint;

import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.objects.impl.Person;

import it.freedomotic.reactions.Command;

import java.io.IOException;
import java.util.Random;

/**
 *
 * @author Enrico
 */
public class TrackingRandomPosition
        extends Protocol {

    public TrackingRandomPosition() {
        //set plugin name and manufest path
        super("Tracking Simulator (Random)", "/it.nicoletti.test/tracking-simulator-random.xml");
        //set plugin description
        setDescription("It simulates a motes WSN that send information about "
                + "movable sensors position. Positions are randomly generated");
        //wait time between events generation
        //onRun() is called every 2000 milliseconds
        setPollingWait(2000);
    }

    private boolean canGo(int destX, int destY) {
        //can be reimplemented
        //always true
        return true;
    }

    private FreedomPoint randomLocation() {
        int x = 0;
        int y = 0;
        boolean validPos = false;

        while (!validPos) {
            Random rx = new Random();
            Random ry = new Random();
            x = rx.nextInt(EnvironmentPersistence.getEnvironments().get(0).getPojo().getWidth());
            y = ry.nextInt(EnvironmentPersistence.getEnvironments().get(0).getPojo().getHeight());

            if (canGo(x, y)) {
                validPos = true;
            }
        }

        return new FreedomPoint(x, y);
    }

    protected void onRun() {
        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
            if (object instanceof it.freedomotic.objects.impl.Person) {
                Person person = (Person) object;
                FreedomPoint location = randomLocation();
                person.getPojo().getCurrentRepresentation()
                        .setOffset((int) location.getX(), (int) location.getY());
                person.setChanged(true);
            }
        }
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        //do nothing, this plugin just sends events
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //do nothing, no external event is listened
    }
}
