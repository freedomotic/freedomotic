/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.frontend;

import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.environment.Room;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.geometry.FreedomPoint;
import it.freedomotic.objects.BehaviorLogic;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.util.AWTConverter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Queue;

/**
 *
 * @author enrico
 */
public class ImageDrawer extends PlainDrawer {

    public ImageDrawer(JavaDesktopFrontend master) {
        super(master);
        Freedomotic.logger.info("Creating Image Drawer");
    }

    @Override
    public void renderWalls() {
    }

    @Override
    public void renderObjects() {
        for (Iterator it = EnvObjectPersistence.iterator(); it.hasNext();) {
            EnvObjectLogic obj = (EnvObjectLogic) it.next();
            if (obj != null) {
                setTransformContextFor(obj.getPojo());
                if (obj.getPojo().getCurrentRepresentation().getIcon() != null
                        && !obj.getPojo().getCurrentRepresentation().getIcon().equalsIgnoreCase("")) {
                    try {
                        paintImage(obj.getPojo());
                    } catch (RuntimeException e) {
                        drawPlainObject(obj);
                    } finally {
                        invalidateAnyTransform();
                    }
                } else {
                    drawPlainObject(obj);
                }
                if (obj.isChanged()) {
                    paintObjectDescription(obj);
                    obj.setChanged(false);
                }
                invalidateAnyTransform();
            }
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

    @Override
    public void renderZones() {
        for (ZoneLogic zone : Freedomotic.environment.getZones()) {
            if (zone != null) {
                Polygon pol = (Polygon) AWTConverter.convertToAWT(zone.getPojo().getShape());
                paintTexture(zone.getPojo().getTexture(), pol);
                if (zone instanceof Room) {
                    Room room = (Room) zone;
                    drawRoomObject(pol);
                }
            }
        }
    }

    protected void paintPersonAvatar(int x, int y, String icon) {
        paintImageCentredOnCoords(icon, x, y, new Dimension(60, 60));
    }

    @Override
    public void mouseEntersObject(EnvObjectLogic obj) {
        super.mouseEntersObject(obj);
        paintObjectDescription(obj);
    }

    private void paintObjectDescription(EnvObjectLogic obj) {
        StringBuilder description = new StringBuilder();
        if (!obj.getMessage().isEmpty()) {
            description.append(obj.getMessage()).append("\n");
        }
        description.append(obj.getPojo().getName()).append("\n");
        description.append(obj.getPojo().getDescription()).append("\n");
        for (BehaviorLogic b : obj.getBehaviors()) {
            if (b.isActive()) {
                description.append(b.getName()).append(": ").append(b.getValueAsString()).append(" [Active]\n");
            } else {
                description.append(b.getName()).append(": ").append(b.getValueAsString()).append(" [Inactive]\n");
            }
        }
        Rectangle2D box = getCachedShape(obj).getBounds2D();
        int x = (int) box.getMaxX() + 20;
        int y = (int) box.getY() + 10;
        Callout callout = new Callout(
                obj.getPojo().getName(),
                "object.description",
                description.toString(),
                x, y, 0.0f, 2000);
        if (!obj.getMessage().isEmpty()) {
            callout.setColor(Color.red.darker());
        }
        createCallout(callout);

    }

    @Override
    public void mouseExitsObject(EnvObjectLogic obj) {
        super.mouseExitsObject(obj);
        removeIndicators();
    }

    @Override
    public void mouseClickObject(EnvObjectLogic obj) {
        super.mouseClickObject(obj);

    }

    @Override
    public void mouseRightClickObject(EnvObjectLogic obj) {
        super.mouseRightClickObject(obj);
        ObjectEditor editor = new ObjectEditor(obj);
        editor.setVisible(true);

    }
}
