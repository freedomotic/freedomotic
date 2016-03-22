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
import com.freedomotic.things.EnvObjectLogic;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Only a test to demonstrate how to create custom widgets. This example draws a
 * red vertical bar at the left side of the object icon. Refer to ImageDrawer
 * renderObjects method to enable it.
 *
 * @author Enrico Nicoletti
 */
public class WidgetTest {

    private final EnvObjectLogic obj;

    /**
     *
     * @param obj
     */
    public WidgetTest(EnvObjectLogic obj) {
        this.obj = obj;
    }

    /**
     *
     * @return
     */
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
