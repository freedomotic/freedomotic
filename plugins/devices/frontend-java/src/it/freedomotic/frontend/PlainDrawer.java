/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.frontend;

import it.freedomotic.events.ObjectReceiveClick;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.model.geometry.FreedomPoint;
import it.freedomotic.model.geometry.FreedomPolygon;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.util.TopologyUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.Iterator;
import java.util.Queue;

/**
 *
 * @author enrico
 */
public class PlainDrawer extends Renderer {

    private Color PERIMETRAL_WALLS_COLOR = Color.black;
    private Color PERIMETRAL_WALLS_COLOR_DARK = Color.black;
    private Color PERIMETRAL_WALLS_COLOR_LIGHT = Color.black;
    private Color INTERNAL_WALLS_COLOR = Color.black;
    private Color INTERNAL_WALLS_COLOR_DARK = Color.black;
    private int PERIMETRAL_WALLS_TICKNESS = 15;
    private int INTERNAL_WALLS_TICKNESS = 3;
    private int ENVIRONMENT_SHADOW_OFFSET = 10;
    private Color ENVIRONMENT_SHADOW_COLOR = Renderer.BACKGROUND_COLOR.darker();

    public PlainDrawer(JavaDesktopFrontend master) {
        super(master);
        Freedomotic.logger.info("Creating Plain Drawer");
    }

    @Override
    public void prepareBackground() {
    }

    @Override
    public void prepareForeground() {
        //the line that delimits the internal shape of the environment (walls excuded)
//        getContext().setColor(Color.black);
//        Polygon poly = Freedomotic.environment.getShape();
//        getContext().drawPolygon(poly);
    }

    @Override
    public void renderEnvironment() {
        Polygon poly = (Polygon) TopologyUtils.convertToAWT(Freedomotic.environment.getPojo().getShape());
//        System.out.println("Shape: "+Freedomotic.environment.getShape());
//        System.out.println("Polygon: "+poly);

        Graphics2D g2 = (Graphics2D) getContext();

        //render fake shadow
        getContext().translate(ENVIRONMENT_SHADOW_OFFSET, ENVIRONMENT_SHADOW_OFFSET);
        final BasicStroke stroke3 = new BasicStroke(50.0f,
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER);
        g2.setStroke(stroke3);
        getContext().setColor(ENVIRONMENT_SHADOW_COLOR);
        g2.fillPolygon(poly);
        int offset = (ENVIRONMENT_SHADOW_OFFSET / 4) * 3;
        getContext().translate(-offset, -offset);

        //external border of perimetral wall
        final BasicStroke stroke4 = new BasicStroke(PERIMETRAL_WALLS_TICKNESS,
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER);
        g2.setStroke(stroke4);
        getContext().setColor(PERIMETRAL_WALLS_COLOR_LIGHT);
        g2.drawPolygon(poly);

        //center of perimetral wall
        final BasicStroke stroke = new BasicStroke(PERIMETRAL_WALLS_TICKNESS / 10,
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER);
        g2.setStroke(stroke);

        //internal of perimetral wall
        getContext().setColor(PERIMETRAL_WALLS_COLOR);
        g2.drawPolygon(poly);
        final BasicStroke stroke2 = new BasicStroke(PERIMETRAL_WALLS_TICKNESS / 4,
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER);
        g2.setStroke(stroke2);
        getContext().setColor(PERIMETRAL_WALLS_COLOR_DARK);
        g2.drawPolygon(poly);

        g2.setStroke(new BasicStroke()); //reset to default stroke
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
                drawPlainObject(obj);
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
                getContext().drawPolygon((Polygon) TopologyUtils.convertToAWT(zone.getPojo().getShape()));
            }
        }
    }

    protected void drawRoomObject(Polygon poly) {

        Graphics2D g2 = (Graphics2D) getContext();
        g2.setColor(Color.green);
        g2.setStroke(new BasicStroke()); //reset to default stroke
        g2.draw(poly);

        Color walls = INTERNAL_WALLS_COLOR;
        getContext().setColor(walls);


        g2.drawPolygon(poly);

        final BasicStroke stroke2 = new BasicStroke(INTERNAL_WALLS_TICKNESS,
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER);
        g2.setStroke(stroke2);
        Color walls2 = INTERNAL_WALLS_COLOR_DARK;
        getContext().setColor(walls2);
        g2.drawPolygon(poly);

        g2.setStroke(new BasicStroke()); //reset to default stroke
    }

    protected void drawPlainObject(EnvObjectLogic obj) {
        Graphics2D graph2D = (Graphics2D) getContext();
        //rebuildShapeCache(obj);
        //Shape shape = getCachedShape(obj);
        Shape shape = TopologyUtils.convertToAWT(obj.getPojo().getCurrentRepresentation().getShape());
        Color fill = Color.decode(obj.getPojo().getCurrentRepresentation().getFillColor());
        Color border = Color.decode(obj.getPojo().getCurrentRepresentation().getBorderColor());
        if (fill != null) {
            graph2D.setColor(fill);
            graph2D.fill(shape);
        }
        if (border != null) {
            graph2D.setColor(border);
            graph2D.draw(shape);
        } else {
            graph2D.setColor(Color.black);
            graph2D.draw(shape);
        }

    }

    @Override
    public void mouseEntersObject(EnvObjectLogic obj) {
    }

    @Override
    public void mouseExitsObject(EnvObjectLogic obj) {
    }

    @Override
    public void mouseClickObject(EnvObjectLogic obj) {
        ObjectReceiveClick event = new ObjectReceiveClick(this, obj, ObjectReceiveClick.SINGLE_CLICK);
        Freedomotic.sendEvent(event);
    }

    @Override
    public void mouseDoubleClickObject(EnvObjectLogic obj) {
        ObjectReceiveClick event = new ObjectReceiveClick(this, obj, ObjectReceiveClick.DOUBLE_CLICK);
        Freedomotic.sendEvent(event);
    }

    @Override
    public void mouseRightClickObject(EnvObjectLogic obj) {
        ObjectReceiveClick event = new ObjectReceiveClick(this, obj, ObjectReceiveClick.RIGHT_CLICK);
        Freedomotic.sendEvent(event);
    }
}
