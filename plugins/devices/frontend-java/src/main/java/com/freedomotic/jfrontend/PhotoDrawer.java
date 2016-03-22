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
package com.freedomotic.jfrontend;

import com.freedomotic.core.ResourcesManager;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.util.TopologyUtils;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class PhotoDrawer extends ImageDrawer {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoDrawer.class.getName());

    /**
     *
     * @param master
     */
    public PhotoDrawer(JavaDesktopFrontend master) {
        super(master);
    }

    /**
     *
     */
    @Override
    public void prepareBackground() {
        BufferedImage img = null;
        String fileName = getCurrEnv().getPojo().getBackgroundImage();
        img = ResourcesManager.getResource(fileName,
                getCurrEnv().getPojo().getWidth(),
                getCurrEnv().getPojo().getHeight());

        if (img != null) {
            getContext().drawImage(img, 0, 0, this);
        } else {
            LOG.warn("Cannot find environment background image {}", fileName);
        }
    }

    /**
     *
     */
    @Override
    public void prepareForeground() {
    }

    /**
     *
     */
    @Override
    public void renderEnvironment() {
    }

    /**
     *
     */
    @Override
    public void renderZones() {
        for (ZoneLogic zone : getCurrEnv().getZones()) {
            if (zone != null) {
                Polygon pol = (Polygon) TopologyUtils.convertToAWT(zone.getPojo().getShape());

                if (zone instanceof Room) {
                    Room room = (Room) zone;
                }
            }
        }
    }
}
