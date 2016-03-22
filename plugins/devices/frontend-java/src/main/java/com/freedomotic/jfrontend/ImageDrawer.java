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

import com.freedomotic.api.Protocol;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.util.TopologyUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.Queue;

/**
 *
 * @author Enrico Nicoletti
 */
public class ImageDrawer extends PlainDrawer {

    Protocol master;

    /**
     *
     * @param master
     */
    public ImageDrawer(JavaDesktopFrontend master) {
        super(master);
        this.master = master;
    }

    /**
     *
     */
    @Override
    public void renderWalls() {
    }

    /**
     *
     */
    @Override
    public void renderObjects() {
        for (EnvObjectLogic obj : master.getApi().things().findByEnvironment(getCurrEnv())) {
            renderSingleObject(obj);
        }
    }

    /**
     *
     * @param obj
     */
    public void renderSingleObject(EnvObjectLogic obj) {
        if (obj != null) {
            setTransformContextFor(obj.getPojo());

            if ((obj.getPojo().getCurrentRepresentation().getIcon() != null)
                    && !obj.getPojo().getCurrentRepresentation().getIcon().equalsIgnoreCase("")) {
                try {
                    //WidgetTest widget = new WidgetTest(obj);
                    //paintImage(widget.draw());
                    paintImage(obj.getPojo());
                } catch (RuntimeException e) {
                    drawPlainObject(obj);
                } finally {
                    invalidateAnyTransform();
                }
            } else {
                drawPlainObject(obj);
            }

            //paintObjectDescription(obj);
            invalidateAnyTransform();
        }
    }

    private void drawTrace(int[] xTrace, int[] yTrace, Color color) {
        getContext().setColor(color);

        int num = (int) Math.min(xTrace.length, yTrace.length);
        getContext().drawPolyline(xTrace, yTrace, num);
    }

    private int[] getXTrace(Queue<FreedomPoint> trace) {
        int size = trace.size();
        int[] xPoints = new int[size];
        int i = 0;

        for (FreedomPoint p : trace) {
            if (i < size) {
                xPoints[i] = (int) p.getX();
                i++;
            }
        }

        return xPoints;
    }

    private int[] getYTrace(Queue<FreedomPoint> trace) {
        int size = trace.size();
        int[] yPoints = new int[size];
        int i = 0;

        for (FreedomPoint p : trace) {
            if (i < size) {
                yPoints[i] = (int) p.getY();
                i++;
            }
        }

        return yPoints;
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
                    Room room = (Room) zone;
                    drawRoomObject(pol);
                }
            }
        }
    }

    /**
     *
     * @param x
     * @param y
     * @param icon
     */
    protected void paintPersonAvatar(int x, int y, String icon) {
        paintImageCentredOnCoords(icon,
                x,
                y,
                new Dimension(60, 60));
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
//        if (!obj.getMessage().isEmpty()) {
//            description.append(obj.getMessage()).append("\n");
//        }
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
//        if (!obj.getMessage().isEmpty()) {
//            callout.setColor(Color.red.darker());
//        }
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

    /**
     *
     * @param obj
     */
    @Override
    public void mouseClickObject(EnvObjectLogic obj) {
        super.mouseClickObject(obj);
    }

    /**
     *
     * @param obj
     */
    @Override
    public void mouseRightClickObject(EnvObjectLogic obj) {
        super.mouseRightClickObject(obj);
    }
}
