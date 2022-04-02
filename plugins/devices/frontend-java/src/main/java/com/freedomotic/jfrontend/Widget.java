/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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

import com.freedomotic.app.Freedomotic;
import com.freedomotic.core.ResourcesManager;
import com.freedomotic.things.EnvObjectLogic;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Enrico Nicoletti
 */
public class Widget {

    private final EnvObjectLogic obj;

    /**
     *
     * @param obj
     */
    public Widget(EnvObjectLogic obj) {
        this.obj = obj;
    }

    /**
     *
     * @return
     */
    public synchronized BufferedImage draw() {
        try {
            String name = obj.getPojo().getCurrentRepresentation().getIcon();
            BufferedImage resource;
            Graphics2D canvas;
            switch (obj.getPojo().getSimpleType()) {

                case "barometer":
                    resource = ResourcesManager.getResource(name, 96, 96);
                    canvas = resource.createGraphics();
                    canvas.setColor(Color.GRAY);
                    canvas.fillRect(0, 0, (int) 96, (int) 96);
                    canvas.setFont(new Font("Arial", Font.BOLD, 22));
                    canvas.setColor(Color.WHITE);
                    canvas.drawString(obj.getBehavior("pressure").getValueAsString() + " Pa", 7, 55);
                    break;

                case "hygrometer":
                    resource = ResourcesManager.getResource(name, 96, 96);
                    canvas = resource.createGraphics();
                    canvas.setColor(Color.BLUE);
                    canvas.fillRect(0, 0, (int) 96, (int) 96);
                    canvas.setFont(new Font("Arial", Font.BOLD, 22));
                    canvas.setColor(Color.WHITE);
                    canvas.drawString(obj.getBehavior("humidity").getValueAsString() + " %", 7, 55);
                    break;

                case "powermeter":
                    resource = ResourcesManager.getResource(name, 96, 96);
                    canvas = resource.createGraphics();
                    canvas.setColor(Color.ORANGE);
                    canvas.fillRect(0, 0, (int) 96, (int) 96);
                    canvas.setFont(new Font("Arial", Font.BOLD, 22));
                    canvas.setColor(Color.WHITE);
                    canvas.drawString(obj.getBehavior("voltage").getValueAsString() + " V", 7, 30);
                    canvas.drawString(obj.getBehavior("power").getValueAsString() + " W", 7, 58);
                    canvas.drawString(obj.getBehavior("current").getValueAsString() + " A", 7, 85);

                    break;

                case "thermometer":
                    resource = ResourcesManager.getResource(name, 96, 96);
                    canvas = resource.createGraphics();
                    canvas.setColor(Color.RED);
                    canvas.fillRect(0, 0, (int) 96, (int) 96);
                    canvas.setFont(new Font("Arial", Font.BOLD, 22));
                    canvas.setColor(Color.WHITE);
                    canvas.drawString(obj.getBehavior("temperature").getValueAsString() + " C", 7, 55);
                    break;

                default:
                    resource = ResourcesManager.getResource(name);
            }
            return resource;
        } catch (Exception e) {
            Freedomotic.getStackTraceInfo(e);
        }
        return null;
    }
}
