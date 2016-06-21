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

import com.freedomotic.app.Freedomotic;
import com.freedomotic.core.ResourcesManager;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.i18n.I18n;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.model.object.Representation;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.util.TopologyUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class Renderer extends Drawer implements MouseListener, MouseMotionListener {

    private static final Logger LOG = LoggerFactory.getLogger(Renderer.class.getName());

    private JavaDesktopFrontend plugin;
    private Graphics graph;
    private Graphics2D graph2D;
    private AffineTransform originalRenderingContext;
    private AffineTransform panelTransform;
    private AffineTransform objectTransform = panelTransform;
    private double widthRescale = 1.0;
    private double heightRescale = 1.0;
    private boolean backgroundChanged = true;
    private int environmentWidth;
    private int environmentHeight;
    private static final int BORDER_X = 10; //the empty space around the map
    private static final int BORDER_Y = 10; //the empty space around the map
    private double CANVAS_WIDTH;
    private double CANVAS_HEIGHT;
    private static final int SNAP_TO_GRID = 20; //a grid of 20cm

    /**
     *
     */
    public static final int HIGH_OPACITY = 180;

    /**
     *
     */
    public static final int DEFAULT_OPACITY = 150;

    /**
     *
     */
    public static final int LOW_OPACITY = 120;
    private static Map<ZoneLogic, Color> zoneColors = new HashMap<ZoneLogic, Color>();
    private final I18n I18n;

    /**
     *
     */
    protected Color backgroundColor;
    private EnvObjectLogic selectedObject;
    private ArrayList<Indicator> indicators = new ArrayList<Indicator>();
    private HashMap<String, Shape> cachedShapes = new HashMap<String, Shape>();
    private boolean inDrag;
    private boolean roomEditMode = false;
    private FreedomPoint originalHandleLocation = null;
    private ArrayList<Handle> handles = new ArrayList<Handle>();
    private ZoneLogic selectedZone;

    /**
     *
     */
    protected CalloutsUpdater calloutsUpdater;
    private boolean objectEditMode = false;
    private Point messageCorner = new Point(50, 50);
    private Dimension dragDiff = null;
    private EnvironmentLogic currEnv;
    private BufferedImage background;

    private Map<EnvObjectLogic, ObjectEditor> objEditorPanels = new HashMap<EnvObjectLogic, ObjectEditor>();

    /**
     *
     * @param master
     */
    public Renderer(JavaDesktopFrontend master) {
        this.plugin = master;
        this.I18n = plugin.getApi().getI18n();
        environmentWidth = (int) getEnvironments().get(0).getPojo().getWidth();
        environmentHeight = (int) getEnvironments().get(0).getPojo().getHeight();
        CANVAS_WIDTH = environmentWidth + (BORDER_X * 2);
        CANVAS_HEIGHT = environmentHeight + (BORDER_Y * 2);
        backgroundColor = TopologyUtils.convertColorToAWT(getEnvironments().get(0).getPojo().getBackgroundColor());
        calloutsUpdater = new CalloutsUpdater(this, 900);
        currEnv = getEnvironments().get(0);
        ResourcesManager.clear();
        clear();
        addCustomMouseListener();
        addCustomMouseMotionListener();
        setBackground(backgroundColor);
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                backgroundChanged = true;
                findRescaleFactor();
            }
        });
        repaint();
    }

    private List<EnvironmentLogic> getEnvironments() {
        return plugin.getApi().environments().findAll();
    }

    /**
     * Opens object editor GUI.
     *
     * @param obj the object to edit
     */
    public void openObjEditor(EnvObjectLogic obj) {
        if (objEditorPanels.containsKey(obj)) {
            if (objEditorPanels.get(obj) == null) {
                objEditorPanels.remove(obj);
                objEditorPanels.put(obj, createNewObjectEditor(obj));
            }
        } else {
            objEditorPanels.put(obj, createNewObjectEditor(obj));
        }

        final ObjectEditor currEditorPanel = objEditorPanels.get(obj);
        currEditorPanel.setVisible(true);
        currEditorPanel.toFront();
    }

    private ObjectEditor createNewObjectEditor(final EnvObjectLogic o) {
        final ObjectEditor oe = new ObjectEditor(o);
        oe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    objEditorPanels.remove(o);
                } catch (Exception ex) {
                    LOG.error("Cannot unload object editor frame", ex);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    if (objEditorPanels.containsKey(o)) {
                        objEditorPanels.remove(o);
                    }
                } catch (Exception ex) {
                    LOG.error("Cannot unload object editor frame", ex);
                }
            }
        });
        return oe;
    }

    /**
     *
     * @return
     */
    public Map<EnvObjectLogic, ObjectEditor> getOpenThingEditors() {
        return objEditorPanels;
    }

    /**
     * Returns the current environment.
     *
     * @return the current environment
     */
    @Override
    public EnvironmentLogic getCurrEnv() {
        return this.currEnv;
    }

    /**
     * Sets the current environment.
     *
     * @param env
     */
    @Override
    public void setCurrEnv(EnvironmentLogic env) {
        this.currEnv = env;
        updateEnvRelatedVars();
        setNeedRepaint(true);
    }

    /**
     *
     */
    public void updateEnvRelatedVars() {
        environmentWidth = currEnv.getPojo().getWidth();
        environmentHeight = currEnv.getPojo().getHeight();
        backgroundColor = TopologyUtils.convertColorToAWT(currEnv.getPojo().getBackgroundColor());
    }

    /**
     *
     * @return
     */
    protected EnvObjectLogic getSelectedObject() {
        return selectedObject;
    }

    /**
     *
     * @param shape
     * @param color
     */
    protected void addIndicator(Shape shape, Color color) {
        indicators.add(new Indicator(shape, color));
    }

    /**
     *
     * @param shape
     */
    protected void addIndicator(Shape shape) {
        indicators.add(new Indicator(shape));
    }

    /**
     *
     */
    protected void removeIndicators() {
        indicators.clear();
        selectedObject = null;
        setNeedRepaint(false);
    }

    /**
     *
     * @return
     */
    @Override
    public ZoneLogic getSelectedZone() {
        return selectedZone;
    }

    /**
     *
     * @param selectedZone
     */
    @Override
    public void setSelectedZone(ZoneLogic selectedZone) {
        removeIndicators();
        addIndicator(TopologyUtils.convertToAWT(selectedZone.getPojo().getShape()));
        this.selectedZone = selectedZone;
        createHandles(selectedZone);

        //highlight the other rooms
        for (Room room : getCurrEnv().getRooms()) {
            if (room != selectedZone) {
                if (zoneColors.get(room) == null) {
                    zoneColors.put(room, new Color(rand(0, 255), rand(0, 255), rand(0, 255), LOW_OPACITY));
                }
                addIndicator(TopologyUtils.convertToAWT(room.getPojo().getShape()), zoneColors.get(room));
            }
        }
    }

    private void addCustomMouseListener() {
        addMouseListener(this);
    }

    private void addCustomMouseMotionListener() {
        addMouseMotionListener(this);
    }

    /**
     *
     * @param repaintBackground
     */
    @Override
    public synchronized void setNeedRepaint(boolean repaintBackground) {
        backgroundChanged = repaintBackground;
        this.repaint();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Iterator it = getOpenThingEditors().entrySet().iterator();
                while (it.hasNext()) {
                    Entry entry = (Entry) it.next();
                    EnvObjectLogic thing = (EnvObjectLogic) entry.getKey();
                    ObjectEditor editor = (ObjectEditor) entry.getValue();
                    //The object may have changed, refresh this panel
                    //Both null checks are REQUIRED!
                    if (thing != null && thing.getPojo() != null) {
                        editor = new ObjectEditor(thing);
                    } else {
                        it.remove();
                    }
                }
            }
        });

    }

    private void renderIndicators() {
        for (Indicator i : indicators) {
            if (i.getShape() instanceof Polygon) {
                getContext().setColor(i.getColor().darker());
                getContext().drawPolygon((Polygon) i.getShape());
                getContext().setColor(i.getColor());
                getContext().fillPolygon((Polygon) i.getShape());
            }
        }
    }

    /**
     *
     */
    public void prepareBackground() {
    }

    /**
     *
     */
    public void renderEnvironment() {
    }

    /**
     *
     */
    public void renderWalls() {
    }

    /**
     *
     */
    public void prepareForeground() {
    }

    /**
     *
     */
    public void renderObjects() {
    }

    /**
     *
     */
    public void renderPeople() {
    }

    /**
     *
     */
    public void renderZones() {
    }

    /**
     *
     * @param obj
     */
    public void mouseEntersObject(EnvObjectLogic obj) {
    }

    /**
     *
     * @param obj
     */
    public void mouseExitsObject(EnvObjectLogic obj) {
    }

    /**
     *
     * @param obj
     */
    public void mouseClickObject(EnvObjectLogic obj) {
    }

    private void paintEnvironmentLayer(Graphics g) {
        setContext(g); //painting on an image, not rendered directly on jpanel
        graph2D = (Graphics2D) getContext();
        //painting uniform background
        getContext().setColor(backgroundColor);
        getContext().fillRect(0,
                0,
                this.getWidth(),
                this.getHeight());
        graph2D.scale(widthRescale, heightRescale);

        try {
            //translating the environment to mach the trasparent area in wall image
            graph2D.translate(BORDER_X, BORDER_Y);
            prepareBackground();
            renderEnvironment();
            renderZones();
            prepareForeground();

            //go to point 0,0
            graph2D.translate(-BORDER_X, -BORDER_Y);
            //render the wall image
            renderWalls();
            graph2D.translate(BORDER_X, BORDER_Y);

            if (!roomEditMode) {
                //selection markers
                renderIndicators();
                renderObjects();
            } else {
                renderIndicators();
                renderHandles();
            }
        } catch (Exception e) {
            LOG.error("Error while painting environment");
            LOG.error(Freedomotic.getStackTraceInfo(e));
        } finally {
            restoreTransformContext();
        }
    }

    /**
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = setRenderingQuality(g);
        super.paintComponent(g2);

        //long start = System.currentTimeMillis();
        if (backgroundChanged) {
            backgroundChanged = false;

            synchronized (this) {
                new Runnable() {

                    @Override
                    public void run() {
                        BufferedImage bkg = createDrawableCanvas();
                        paintEnvironmentLayer(setRenderingQuality(bkg.createGraphics()));
                        background = bkg;
                    }
                }.run();
            }
        }

        setContext(g2);
        getContext().drawImage(background,
                0,
                0,
                this);
        createSceneTransformContext(getContext());
        renderPeople();
        renderCalloutsLayer();
        restoreTransformContext();

        //long end = System.currentTimeMillis();
        //Freedomotic.logger.severe("Repainting process takes " + (end-start) + "ms");
    }

    private Graphics2D setRenderingQuality(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        if (g2 != null) {
            //g2.setComposite(AlphaComposite.Src);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        }
        return g2;
    }

    private void renderCalloutsLayer() {
        int numOfInfoLines = 0;
        int offset = 0;

        // Print mouse coordinates
        Callout mousePointer = calloutsUpdater.getMousePointerCallout();
        if (mousePointer != null) {
            drawString(mousePointer.getText(),
                    (int) mousePointer.getPosition().getX(),
                    (int) mousePointer.getPosition().getY(),
                    (float) 0.0,
                    mousePointer.getColor());
        }

        // Print all other callouts
        synchronized (calloutsUpdater.getPrintableCallouts()) {
            for (Callout callout : calloutsUpdater.getPrintableCallouts()) {
                //display multiple info callouts on different lines
                if (callout.getGroup().equalsIgnoreCase("info")) {
                    offset = (numOfInfoLines * 50);
                    numOfInfoLines++;
                    drawString(callout.getText(),
                            (int) callout.getPosition().getX(),
                            (int) callout.getPosition().getY() + offset,
                            (float) 0.0,
                            callout.getColor());
                } else {
                    drawString(callout.getText(),
                            (int) callout.getPosition().getX(),
                            (int) callout.getPosition().getY(),
                            (float) 0.0,
                            callout.getColor());
                }

                //limit to the last 10 most recent lines
                if (numOfInfoLines > 10) {
                    return;
                }
            }
        }
    }

    /**
     *
     * @param callout
     */
    @Override
    public void createCallout(Callout callout) {
        calloutsUpdater.addCallout(callout);
    }

    private void createSceneTransformContext(Graphics g) {
        graph2D = (Graphics2D) getContext();
        originalRenderingContext = graph2D.getTransform();

        AffineTransform newRenderingContext = (AffineTransform) (originalRenderingContext.clone());
        newRenderingContext.scale(widthRescale, heightRescale);
        newRenderingContext.translate(BORDER_X, BORDER_Y);
        graph2D.setTransform(newRenderingContext);
    }

    /**
     *
     * @return
     */
    protected Graphics getContext() {
        return graph;
    }

    private void setContext(Graphics g) {
        this.graph = g;
    }

    /**
     *
     * @return
     */
    protected Graphics2D getRenderingContext() {
        return graph2D;
    }

    /**
     *
     */
    protected void restoreTransformContext() {
        try {
            graph2D.setTransform(originalRenderingContext);
        } catch (Exception e) {
        }
    }

    private void findRescaleFactor() {
        widthRescale = (double) ((double) this.getWidth() / (double) CANVAS_WIDTH);
        heightRescale = (double) ((double) this.getHeight() / (double) CANVAS_HEIGHT);

        if (widthRescale < heightRescale) {
            heightRescale = widthRescale;
        } else {
            widthRescale = heightRescale;
        }
    }

    /**
     *
     * @param obj
     * @return
     */
    protected AffineTransform setTransformContextFor(EnvObject obj) {
        Graphics2D tmpGraph = (Graphics2D) getContext();
        panelTransform = tmpGraph.getTransform();

        AffineTransform newAt = (AffineTransform) (panelTransform.clone());
        int x = (int) obj.getCurrentRepresentation().getOffset().getX();
        int y = (int) obj.getCurrentRepresentation().getOffset().getY();
        newAt.translate(x, y);
        newAt.rotate(Math.toRadians(obj.getCurrentRepresentation().getRotation()));
        tmpGraph.setTransform(newAt);
        objectTransform = newAt;

        return newAt;
    }

    /**
     *
     */
    protected void invalidateAnyTransform() {
        Graphics2D tmpGraph = (Graphics2D) getContext();
        tmpGraph.setTransform(panelTransform);
    }

    private void invalidateLastTransform() {
        Graphics2D tmpGraph = (Graphics2D) getContext();
        tmpGraph.setTransform(objectTransform);
    }

    private BufferedImage createDrawableCanvas() {
        BufferedImage img = null;

        try {
            img = new BufferedImage(getWidth(),
                    getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        } catch (Exception e) {
            LOG.error(Freedomotic.getStackTraceInfo(e));
        }

        return img;
    }

    /**
     *
     * @param shape
     * @param offset
     * @return
     */
    public static Shape getTranslatedShape(final Shape shape, Point offset) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }

        final AffineTransform transform = AffineTransform.getTranslateInstance(offset.getX(),
                offset.getY());

        return transform.createTransformedShape(shape);
    }

    /**
     *
     * @param shape
     * @param rotation
     * @return
     */
    public static Shape getRotatedShape(Shape shape, double rotation) {
        AffineTransform localAT = null;
        Shape localShape = null;
        localAT = AffineTransform.getRotateInstance(Math.toRadians(rotation),
                shape.getBounds().getX(),
                shape.getBounds().getY());
        localShape = localAT.createTransformedShape(shape);

        return localShape;
    }

    /**
     *
     * @param textureFile
     * @param shape
     */
    protected void paintTexture(String textureFile, Shape shape) {
        BufferedImage img = null;
        Graphics2D g2 = (Graphics2D) getContext();

        if (textureFile != null) {
            img = ResourcesManager.getResource(textureFile);

            if (img != null) {
                Rectangle2D replicationPath = new Rectangle2D.Double(0,
                        0,
                        img.getWidth(),
                        img.getHeight());
                TexturePaint texture = new TexturePaint(img, replicationPath);
                g2.setPaint(texture);
                g2.fill(shape);
            }
        }
    }

    /**
     *
     * @param icon
     * @param x
     * @param y
     * @param dimension
     */
    protected void paintImageCentredOnCoords(String icon, int x, int y, Dimension dimension) {
        BufferedImage img = null;
        img = ResourcesManager.getResource(icon, (int) dimension.getWidth(), (int) dimension.getHeight());

        if (img != null) {
            getContext().drawImage(img, x - (img.getWidth() / 2), y - (img.getHeight() / 2), this);
        }
    }

    /**
     *
     * @param img
     * @param x
     * @param y
     */
    protected void paintImageCentredOnCoords(BufferedImage img, int x, int y) {
        if (img != null) {
            getContext().drawImage(img, x - (img.getWidth() / 2), y - (img.getHeight() / 2), this);
        }
    }

    /**
     *
     * @param obj
     * @throws RuntimeException
     */
    protected void paintImage(EnvObject obj) {

        BufferedImage img = ResourcesManager.getResource(
                obj.getCurrentRepresentation().getIcon(), -1, -1); //-1 means no resizeing

        if (img != null) {
            getContext().drawImage(img, 0, 0, this);
        } else {
            throw new RuntimeException("Cannot find image " + obj.getCurrentRepresentation().getIcon() + " for object " + obj.getName());
        }
    }

    /**
     *
     * @param img
     */
    protected void paintImage(BufferedImage img) {
        getContext().drawImage(img, 0, 0, this);
    }

    private void drawString(String text, int x, int y, float angle, Color color) {
        Graphics2D localGraph = (Graphics2D) getContext();
        final int BORDER = 10;
        Font font = new Font("SansSerif", Font.PLAIN, 25);
        localGraph.setFont(font);

        //parse lines
        String[] lines = text.trim().split("\n");
        int longest = 0;
        int maxChar = 0;

        for (int k = 0; k < lines.length; k++) {
            if (lines[k].length() > maxChar) {
                maxChar = lines[k].length();
                longest = k;
            }
        }

        //draw background
        AffineTransform origAt = localGraph.getTransform();
        AffineTransform newAt = (AffineTransform) (origAt.clone());
        Rectangle2D rect = localGraph.getFontMetrics().getStringBounds(lines[longest], localGraph);
        RoundRectangle2D round = new RoundRectangle2D.Double(rect.getX(),
                rect.getY(), rect.getWidth() + (BORDER * 2),
                (rect.getHeight() * lines.length) + (BORDER * 2), 25, 25);
        newAt.rotate(Math.toRadians(angle),
                x,
                y);
        localGraph.setTransform(newAt);

        Shape shape = getTranslatedShape(round,
                new Point(x, y));
        Color transparent = new Color(color.getRed(),
                color.getGreen(),
                color.getBlue(),
                190);
        localGraph.setColor(transparent);
        localGraph.fill(shape);

        //draw single lines
        y += (BORDER - 27);

        for (int j = 0; j < lines.length; j++) {
            if (!lines[j].trim().isEmpty()) {
                y += 27;
                localGraph.setColor(Color.white);
                localGraph.drawString(lines[j], x + BORDER, y);
            }
        }

        localGraph.setTransform(origAt);
    }

    /**
     *
     * @param clickPoint
     * @return
     */
    protected Point toRealCoords(Point clickPoint) {
        int x = (int) (clickPoint.getX() / widthRescale) - BORDER_X;
        int y = (int) (clickPoint.getY() / heightRescale) - BORDER_Y;

        return new Point(x, y);
    }

    /**
     *
     * @param p
     * @return
     */
    protected EnvObjectLogic mouseOnObject(Point p) {
        Point mousePointer = toRealCoords(p);

        for (EnvObjectLogic logic : plugin.getApi().things().findByEnvironment(currEnv)) {
            if (getCachedShape(logic).contains(mousePointer)) {
                return logic;
            }
        }

        return null;
    }

    /**
     *
     * @param p
     * @return
     */
    protected ZoneLogic mouseOnZone(Point p) {
        Iterator it = currEnv.getZones().iterator();
        boolean onZone = false;

        while (it.hasNext()) {
            ZoneLogic zone = (ZoneLogic) it.next();

            if (zone.getPojo().isRoom()) {
                Point mouse = toRealCoords(p);
                onZone = TopologyUtils.contains(zone.getPojo().getShape(),
                        new FreedomPoint((int) mouse.getX(), (int) mouse.getY()));
                if (onZone == true) {
                    return zone;
                }
            }
        }

        return null;
    }

    /**
     *
     * @param p
     * @return
     */
    protected Handle mouseOnHandle(Point p) {
        Point mouse = toRealCoords(p);

        for (Handle handle : handles) {
            Rectangle rect = (Rectangle) handle.getHandle();

            if (rect.contains(mouse)) {
                return handle;
            }
        }

        return null;
    }

    private Shape applyShapeModifiers(EnvObjectLogic object) {
        EnvObject obj = object.getPojo();
        int x = obj.getCurrentRepresentation().getOffset().getX();
        int y = obj.getCurrentRepresentation().getOffset().getY();
        Shape shape = TopologyUtils.convertToAWT(obj.getShape());
        shape = getTranslatedShape(shape,
                new Point(x, y));
        shape = getRotatedShape(shape,
                obj.getCurrentRepresentation().getRotation());
        cachedShapes.put(object.getPojo().getUUID(),
                shape);

        return shape;
    }

    private void rebuildShapesCache() {
        for (EnvObjectLogic obj : plugin.getApi().things().findByEnvironment(getCurrEnv())) {
            rebuildShapeCache(obj);
        }
    }

    private void rebuildShapeCache(EnvObjectLogic obj) {
        applyShapeModifiers(obj);
    }

    /**
     *
     * @param obj
     * @return
     */
    protected Shape getCachedShape(EnvObjectLogic obj) {
        if (cachedShapes.containsKey(obj.getPojo().getUUID())) {
            return cachedShapes.get(obj.getPojo().getUUID());
        } else {
            return applyShapeModifiers(obj);
        }
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (!roomEditMode) {
            EnvObjectLogic obj = mouseOnObject(e.getPoint());

            if (obj != null) {
                //single click on an object
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 1) {
                        mouseClickObject(obj);
                    } else {
                        //double click on an object
                        if (e.getClickCount() == 2) {
                            mouseDoubleClickObject(obj);
                        }
                    }
                } else {
                    //right click on an object
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        if (e.getClickCount() == 1) {
                            mouseRightClickObject(obj);
                        }
                    }
                }
            } else {
                removeIndicators();
                calloutsUpdater.clearAll();
            }
        } else { //if edit mode
            removeIndicators();
            calloutsUpdater.clearAll();

            toRealCoords(e.getPoint());

            //click on an handle in edit mode
            Handle clickedHandle = mouseOnHandle(e.getPoint());

            //single right click
            if (clickedHandle != null) {
                if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON1)) {
                    clickedHandle.setSelected(true);
                } else {
                    //double right click
                    if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1)) {
                        clickedHandle.addAdiacent();
                        setSelectedZone(clickedHandle.getZone());
                    } else {
                        //single left click
                        if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON3)) {
                            clickedHandle.remove();
                            setSelectedZone(clickedHandle.getZone());
                        }
                    }
                }
            } else {
                //click on a zone in edit mode if no handle is selected
                ZoneLogic zone = mouseOnZone(e.getPoint());
                if (zone != null) {
                    Callout callout = new Callout(this.getClass().getCanonicalName(), "info",
                            I18n.msg("room_zone_selected") + " [" + zone.getPojo().getName() + "]", 50, 150, 0, -1);
                    createCallout(callout);
                    setSelectedZone(zone);
                } else {
                    handles.clear();
                    // createHandles(null);
                }
            }
            setNeedRepaint(true);
        }
    }

    /**
     *
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (roomEditMode) {
            Point mouse = toRealCoords(e.getPoint());
            Iterator it = handles.iterator();
            boolean found = false;

            while (it.hasNext() && !found) {
                Handle entry = (Handle) it.next();
                entry.setSelected(false);

                Rectangle handle = (Rectangle) entry.getHandle();

                if (handle.contains(mouse)) {
                    entry.setSelected(true);
                    found = true;
                }
            }
        }
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (inDrag) {
            for (Handle handle : handles) {
                handle.setSelected(false);
            }

            //stop dragging an object
            if (objectEditMode && (selectedObject != null)) {
                Point coords = toRealCoords(e.getPoint());
                selectedObject.setLocation((int) (coords.getX() - dragDiff.getWidth()),
                        (int) (coords.getY() - (int) dragDiff.getHeight()));
            }
            //check if rooms overlap
            if (roomEditMode && (selectedZone) != null) {
                setSelectedZone(selectedZone);
            }
        }

        inDrag = false;
        dragDiff = null;
        selectedObject = null;
        originalHandleLocation = null;
        //removeIndicators();
        rebuildShapesCache();
        setNeedRepaint(true);
    }

    private List<ZoneLogic> overlappedRooms(ZoneLogic zone) {
        List<ZoneLogic> overlapped = new ArrayList<ZoneLogic>();
        Area currentZoneArea = new Area(TopologyUtils.convertToAWT(zone.getPojo().getShape()));
        for (Room r : currEnv.getRooms()) {
            if (!r.equals(selectedZone)) {
                Shape testZoneShape = TopologyUtils.convertToAWT(r.getPojo().getShape());
                Area testArea = new Area(testZoneShape);
                testArea.intersect(currentZoneArea);
                if (!testArea.isEmpty()) {
                    overlapped.add(r);
                }
            }
        }
        return overlapped;
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        inDrag = true;

        Point coords = toRealCoords(e.getPoint());

        if ((dragDiff == null) && (getSelectedObject() != null)) {
            dragDiff = new Dimension((int) Math.abs(coords.getX()
                    - getSelectedObject().getPojo().getCurrentRepresentation().getOffset().getX()),
                    (int) Math.abs(coords.getY()
                            - getSelectedObject().getPojo().getCurrentRepresentation().getOffset().getY()));
        }

        int xSnapped = (int) coords.getX() - ((int) coords.getX() % SNAP_TO_GRID);
        int ySnapped = (int) coords.getY() - ((int) coords.getY() % SNAP_TO_GRID);

        //in object edit mode
        if (objectEditMode && (getSelectedObject() != null)) {
            for (Representation representation : getSelectedObject().getPojo().getRepresentations()) {
                //move an object
                representation.setOffset(xSnapped - (int) dragDiff.getWidth(),
                        ySnapped - (int) dragDiff.getHeight());
                setNeedRepaint(true);
            }
        } else {
            if (roomEditMode) {
                removeIndicators();

                Callout callout = new Callout(this.getClass().getCanonicalName(), "mouse",
                        selectedZone.getPojo().getName() + ": " + xSnapped + "cm," + ySnapped + "cm",
                        (int) coords.getX() + 50, (int) coords.getY() + 50, 0, -1);
                createCallout(callout);

                for (Handle handle : handles) {
                    //move the zone point
                    if (handle.isSelected()) {
                        if (null == originalHandleLocation) {
                            originalHandleLocation = new FreedomPoint(handle.getPoint().getX(), handle.getPoint().getY());
                        }
                        handle.move(xSnapped, ySnapped);
                        addIndicator(TopologyUtils.convertToAWT(selectedZone.getPojo().getShape()));
                        // add indicators for overlapping zones
                        selectedZone = handle.getZone();
                        for (ZoneLogic overlapped : overlappedRooms(selectedZone)) {
                            //mark the zone in red if it is overlapped
                            addIndicator(TopologyUtils.convertToAWT(overlapped.getPojo().getShape()), new Color(255, 0, 0, HIGH_OPACITY));
                        }
                    }
                }
                setNeedRepaint(true);
            }
        }
    }

    /**
     *
     * @param obj
     */
    public void mouseDoubleClickObject(EnvObjectLogic obj) {
    }

    /**
     *
     * @param obj
     */
    public void mouseRightClickObject(EnvObjectLogic obj) {
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        if (!roomEditMode) {
            EnvObjectLogic obj = mouseOnObject(e.getPoint());

            if ((obj == null) && (selectedObject != null)) {
                removeIndicators();
                mouseExitsObject(selectedObject);
                calloutsUpdater.clear("object.description");
            }

            if (obj != null) {
                //addIndicator(cachedShapes.get(obj));
                if (obj != selectedObject) {
                    mouseEntersObject(obj);
                    selectedObject = obj;
                }
            }
        } else { //in edit mode but no dragging

            if (!inDrag) {
                Point mouse = toRealCoords(e.getPoint());

                //create a callout which says the coordinates of the mouse in the environment (centimeters)
                Callout callout = new Callout(this.getClass().getCanonicalName(), "mouse",
                        (int) mouse.getX() + "cm," + (int) mouse.getY() + "cm", (int) mouse.getX(),
                        (int) mouse.getY(), 0, -1);
                createCallout(callout);
                repaint();
            }
        }
    }

    private void clear() {
        this.indicators.clear();
        calloutsUpdater.clearAll();
        this.backgroundChanged = true;
    }

    /**
     *
     * @param forZone
     */
    protected void createHandles(ZoneLogic forZone) {
        handles.clear();

        if (forZone != null) { //create for all zones

            Iterator<FreedomPoint> it = forZone.getPojo().getShape().getPoints().iterator();

            while (it.hasNext()) {
                FreedomPoint corner = (FreedomPoint) it.next();
                handles.add(new Handle(forZone, corner));
            }
        } else {
            for (ZoneLogic zone : currEnv.getZones()) {
                Iterator<FreedomPoint> it = zone.getPojo().getShape().getPoints().iterator();

                while (it.hasNext()) {
                    FreedomPoint corner = (FreedomPoint) it.next();
                    handles.add(new Handle(zone, corner));
                }
            }
        }

        repaint();
    }

    private void renderHandles() {
        for (Handle handle : handles) {
            getContext().setColor(new Color(0, 0, 255, DEFAULT_OPACITY));

            if (handle.isSelected()) {
                getContext().setColor(new Color(255, 0, 0, DEFAULT_OPACITY));
            }

            getContext().fillRect((int) handle.getHandle().getBounds().getX(),
                    (int) handle.getHandle().getBounds().getY(),
                    (int) handle.getHandle().getBounds().getWidth(),
                    (int) handle.getHandle().getBounds().getHeight());
        }
    }

    /**
     *
     * @param edit
     */
    @Override
    public void setRoomEditMode(boolean edit) {
        roomEditMode = edit;

        if (roomEditMode) {
            Callout callout = new Callout(this.getClass().getCanonicalName(), "info",
                    I18n.msg("environment_editing_instructions") + ":\n"
                    + "- " + I18n.msg("environment_editing_instructions_add_new_room") + "\n"
                    + "- " + I18n.msg("environment_editing_instructions_change_room_shape") + "\n"
                    + "- " + I18n.msg("environment_editing_instructions_remove_room") + "\n"
                    + "- " + I18n.msg("environment_editing_instructions_create_draggable_point") + "\n"
                    + "- " + I18n.msg("environment_editing_instructions_delete_draggable_point") + "\n", 100, 200, 0, -1);
            createCallout(callout);
            createHandles(null);
            //find the first room and select it
            Room selectedRoom = getCurrEnv().getRooms().get(0);
            if (selectedRoom != null) {
                setSelectedZone(selectedRoom);
            }
        } else {
            handles.clear();
            indicators.clear();
            selectedZone = null;
            calloutsUpdater.clearAll();
        }

        setNeedRepaint(true);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getRoomEditMode() {
        return roomEditMode;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getObjectEditMode() {
        return objectEditMode;
    }

    /**
     *
     */
    protected void removeSelectedHandles() {
        for (Handle handle : handles) {
            if (handle.isSelected()) {
                handle.remove();
            }
        }
    }

    @Override
    void setObjectEditMode(boolean state) {
        objectEditMode = state;
    }

    /**
     *
     * @return
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    private int rand(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
