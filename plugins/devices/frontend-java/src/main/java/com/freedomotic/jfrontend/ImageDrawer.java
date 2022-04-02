/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
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

import com.freedomotic.api.Protocol;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.util.TopologyUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Enrico Nicoletti
 */

@SuppressWarnings("squid:S1948") //We are not planning to serialize UI components

public class ImageDrawer extends PlainDrawer {

    private Protocol masterProtocol;
    private String enableSensorsWidget;
    

    /**
     *
     * @param masterProtocol
     */
    public ImageDrawer(JavaDesktopFrontend masterProtocol, String enableSensorsWidget) {
        super(masterProtocol);
        this.masterProtocol = masterProtocol;
        this.enableSensorsWidget = enableSensorsWidget;
    }

    /**
     *
     */
    @Override
    public void renderWalls() {
        /*
         * This method should not be invoked from ImageDrawer class.
         * Throwing an UnsupportedOperationException, however, is not recommended due to backwards compatibility
         */
    }

    /**
     *
     */
    @Override
    public void renderObjects() {
        masterProtocol
                .getApi()
                .things()
                .findByEnvironment(getCurrEnv())
                .forEach(this::renderSingleObject);
    }

    /**
     *
     * @param obj
     */
    private void renderSingleObject(EnvObjectLogic obj) {
        if (obj != null) {
            setTransformContextFor(obj.getPojo());

            if ((obj.getPojo().getCurrentRepresentation().getIcon() != null)
                    && !obj.getPojo().getCurrentRepresentation().getIcon().equalsIgnoreCase("")) {
                try {
                    if ("true".equalsIgnoreCase(enableSensorsWidget)) {
                        Widget widget = new Widget(obj);
                        paintImage(widget.draw());
                    } else {
                        paintImage(obj.getPojo());
                    }
                } catch (RuntimeException e) {
                    drawPlainObject(obj);
                } finally {
                    invalidateAnyTransform();
                }
            } else {
                drawPlainObject(obj);
            }

            invalidateAnyTransform();
        }
    }

    /**
     *
     */
    @Override
    public void renderZones() {
        for (ZoneLogic zone : getCurrEnv().getZones()) {
            if (zone != null) {
                Polygon pol = (Polygon) TopologyUtils.convertToAWT(zone.getPojo().getShape());
                paintTexture(zone.getPojo().getTexture(),
                        pol);

                if (zone instanceof Room) {
                    drawRoomObject(pol);
                }
            }
        }
    }

    /**
     *
     * @param obj
     */
    @Override
    public void mouseEntersObject(EnvObjectLogic obj) {
        super.mouseEntersObject(obj);
        paintObjectDescription(obj);
    }

    private void paintObjectDescription(EnvObjectLogic obj) {
        StringBuilder description = new StringBuilder();
        description.append(obj.getPojo().getName()).append("\n");
        description.append(obj.getPojo().getDescription()).append("\n");

        for (BehaviorLogic b : obj.getBehaviors()) {
            if (b.isActive()) {
                description.append(b.getName()).append(": ").append(b.getValueAsString()).append(" [Active]\n");
            } else {
                description.append(b.getName()).append(": ").append(b.getValueAsString())
                        .append(" [Inactive]\n");
            }
        }

        Rectangle2D box = getCachedShape(obj).getBounds2D();
        int x = (int) box.getMaxX() + 20;
        int y = (int) box.getY() + 10;
        Callout callout
                = new Callout(obj.getPojo().getName(), "object.description",
                        description.toString(), x, y, 0.0f, 2000);
        createCallout(callout);
        setNeedRepaint(true);
    }

    /**
     *
     * @param obj
     */
    @Override
    public void mouseExitsObject(EnvObjectLogic obj) {
        super.mouseExitsObject(obj);
        removeIndicators();
    }

}
