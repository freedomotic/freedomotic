/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.jfrontend;

import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.geometry.FreedomPoint;
import java.awt.Rectangle;

/**
 *
 * @author Enrico
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
        FreedomPoint added = zone.getPojo().getShape().insert(point);

        return added;
    }
}
