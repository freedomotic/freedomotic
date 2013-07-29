/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.jfrontend;

import it.freedomotic.core.ResourcesManager;

import it.freedomotic.objects.EnvObjectLogic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Only a test to demonstrate how to create custom widgets. This example draws a
 * red vertical bar at the left side of the object icon. Refer to ImageDrawer
 * renderObjects method to enable it.
 *
 * @author enrico
 */
public class WidgetTest {

    private final EnvObjectLogic obj;

    public WidgetTest(EnvObjectLogic obj) {
        this.obj = obj;
    }

    public synchronized BufferedImage draw() {
        try {
            String name = obj.getPojo().getCurrentRepresentation().getIcon();
            BufferedImage resource = ResourcesManager.getResource(name);
            Graphics2D canvas = resource.createGraphics();
            canvas.setColor(Color.red);
            canvas.fillRect(0, 0, (int) 10, (int) resource.getHeight());

            //ResourcesManager.addResource(name, canvas);
            return resource;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
