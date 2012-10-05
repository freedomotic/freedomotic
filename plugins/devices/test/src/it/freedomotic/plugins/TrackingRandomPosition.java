/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Sensor;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.impl.Person;
import it.freedomotic.objects.EnvObjectPersistence;
import java.awt.Point;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 *
 * @author Enrico
 */
public class TrackingRandomPosition extends Sensor {

    OutputStream out;
    boolean connected = false;
    final int SLEEP_TIME = 1000;
    final int NUM_MOTE = configuration.getIntProperty("people-count", 1);

    public TrackingRandomPosition() {
        super("Tracking Simulator (Random)", "/it.nicoletti.test/tracking-simulator-random.xml");
        setDescription("It simulates a motes WSN that send information about movable sensors position. Positions are randomly generated");
        setAsPollingSensor();
    }


    private boolean canGo(int destX, int destY) {
        return true;
    }

    private Point inventPosition() {
        int x = 0, y = 0;
        boolean validPos = false;
        while (!validPos) {

            Random rsegno = new Random();
            int segno = rsegno.nextInt(2);

            Random rx = new Random();
            Random ry = new Random();
            x = rx.nextInt(700);
            y = ry.nextInt(700);
            if (canGo(x, y)) {
                validPos = true;
            }
        }
        return new Point(x, y);
    }

    protected void onRun() {
        Random numero = new Random();
        int s = numero.nextInt(SLEEP_TIME);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException interruptedException) {
        }

        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
            if (object instanceof it.freedomotic.objects.impl.Person){
                Person person = (Person)object;
                Point position = inventPosition();
                person.getPojo().setCurrentRepresentation(0);
                person.getPojo().getCurrentRepresentation().setOffset((int)position.getX(), (int)position.getY());
            }
        }
    }

    @Override
    protected void onInformationRequest() throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
