/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.frontend;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.core.ResourcesManager;
import it.freedomotic.environment.Room;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.util.AWTConverter;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/**
 *
 * @author enrico
 */
public class PhotoDrawer extends ImageDrawer {

    public PhotoDrawer(JavaDesktopFrontend master) {
        super(master);
        Freedomotic.logger.info("Creating photo drawer");
    }

    @Override
    public void prepareBackground() {
        BufferedImage img = null;
        String fileName = Freedomotic.environment.getPojo().getBackgroundImage();
        img = ResourcesManager.getResource(fileName,
                Freedomotic.environment.getPojo().getWidth(),
                Freedomotic.environment.getPojo().getHeight());
        if (img != null) {
            getContext().drawImage(img, 0, 0, this);
        } else {
            Freedomotic.logger.warning("Cannot find environment background image " + fileName);
        }
    }

    @Override
    public void prepareForeground() {
    }

    @Override
    public void renderEnvironment() {
    }

    @Override
    public void renderZones() {
        for (ZoneLogic zone : Freedomotic.environment.getZones()) {
            if (zone != null) {
                Polygon pol = (Polygon) AWTConverter.convertToAWT(zone.getPojo().getShape());
                if (zone instanceof Room) {
                    Room room = (Room) zone;
                }
            }
        }
    }
}
