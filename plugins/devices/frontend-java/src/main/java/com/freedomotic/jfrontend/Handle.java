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

import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.geometry.FreedomPoint;
import java.awt.Rectangle;

/**
 *
 * @author Enrico Nicoletti
 */
public class Handle {

    private ZoneLogic zone;
    private Rectangle handle;
    private FreedomPoint point;
    private boolean selected;
    private boolean visible;

    /**
     *
     * @param zone
     * @param point
     */
    public Handle(ZoneLogic zone, FreedomPoint point) {
        this.zone = zone;
        this.handle = new Rectangle(point.getX() - 13, point.getY() - 13, 26, 26);
        this.point = point;
        this.selected = false;
        this.visible = true;
    }

    /**
     *
     * @return
     */
    public Rectangle getHandle() {
        return handle;
    }

    /**
     *
     * @param handle
     */
    protected void setHandle(Rectangle handle) {
        this.handle = handle;
    }

    /**
     *
     * @return
     */
    public FreedomPoint getPoint() {
        return point;
    }

    /**
     *
     * @param point
     */
    protected void setPoint(FreedomPoint point) {
        this.point = point;
    }

    /**
     *
     * @return
     */
    public boolean isSelected() {
        return selected;
    }

//    public boolean isVisible() {
//        return visible;
//    }
//
//    public void setVisible(boolean visible) {
//        this.visible = visible;
//    }
    /**
     *
     * @param selected
     */
    protected void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     *
     * @return
     */
    public ZoneLogic getZone() {
        return zone;
    }

    /**
     *
     * @param zone
     */
    protected void setZone(ZoneLogic zone) {
        this.zone = zone;
    }

    /**
     *
     */
    protected void remove() {
        if (zone.getPojo().getShape().getPoints().contains(this.point)) {
            zone.getPojo().getShape().remove(this.point);
        }
    }

    /**
     *
     * @param x
     * @param y
     */
    protected void move(int x, int y) {
        point.setX(x);
        point.setY(y);
        handle.setBounds(x - 13, y - 13, 26, 26);
    }

    /**
     *
     * @return
     */
    public FreedomPoint addAdiacent() {
        //DISABLED AS NOT WORKING
//        FreedomPoint previous = point;
//        //finding the point the precedes the current one, if any
//        for (int i = 0; i < zone.getPojo().getShape().getPoints().size(); i++) {
//            if (zone.getPojo().getShape().getPoints().get(i) == point) {
//                try {
//                    FreedomPoint tmp = zone.getPojo().getShape().getPoints().get(i - 1);
//                    if (tmp != null) {
//                        previous = tmp;
//                    }
//                } catch (Exception e) {
//                    //do nothig, best effort
//                }
//            }
//        }
//
//        //apply pitagora to find the middle between previous point and current one
//        FreedomPoint added = zone.getPojo().getShape().insert(midPoint(previous));
        FreedomPoint added = zone.getPojo().getShape().insert(midPoint(point));
        return added;
    }

    private FreedomPoint midPoint(FreedomPoint p) {
        int mx = (p.getX() + point.getX()) / 2;
        int my = (p.getY() + point.getY()) / 2;
        return new FreedomPoint(mx, my);
    }
}
