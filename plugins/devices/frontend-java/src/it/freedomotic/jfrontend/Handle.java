/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.jfrontend;

import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.model.geometry.FreedomPoint;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

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

    public Handle(ZoneLogic zone, FreedomPoint point) {
        this.zone = zone;
        this.handle = new Rectangle(point.getX() - 13, point.getY() - 13, 26, 26);
        this.point = point;
        this.selected=false;
        this.visible=true;
    }

    public Rectangle getHandle() {
        return handle;
    }

    protected void setHandle(Rectangle handle) {
        this.handle = handle;
    }

    public FreedomPoint getPoint() {
        return point;
    }

    protected void setPoint(FreedomPoint point) {
        this.point = point;
    }

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
    
    protected void setSelected(boolean selected) {
        this.selected = selected;
    }

    public ZoneLogic getZone() {
        return zone;
    }

    protected void setZone(ZoneLogic zone) {
        this.zone = zone;
    }

    protected void remove() {
        if (zone.getPojo().getShape().getPoints().contains(this.point)) {
            zone.getPojo().getShape().remove(this.point);
        }
    }

    protected void move(int x, int y) {
        point.setX(x);
        point.setY(y);
        handle.setBounds(x - 13, y - 13, 26, 26);
    }

    public FreedomPoint addAdiacent() {
        FreedomPoint added = zone.getPojo().getShape().insert(point);
        return added;
    }
}
