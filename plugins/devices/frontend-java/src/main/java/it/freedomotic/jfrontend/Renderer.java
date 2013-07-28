/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.jfrontend;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.core.ResourcesManager;

import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.environment.EnvironmentPersistence;
import it.freedomotic.environment.Room;
import it.freedomotic.environment.ZoneLogic;

import it.freedomotic.model.geometry.FreedomPoint;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.model.object.Representation;

import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;

import it.freedomotic.util.TopologyUtils;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JPanel;

/**
 *
 * @author enrico
 */
public class Renderer
        extends Drawer
        implements MouseListener,
        MouseMotionListener {

    private JavaDesktopFrontend plugin;
    private Graphics graph;
    private Graphics2D graph2D;
    private AffineTransform originalRenderingContext;
    private AffineTransform panelTransform;
    private AffineTransform objectTransform = panelTransform;
    private double widthRescale = 1.0;
    private double heightRescale = 1.0;
    private boolean backgroundChanged = true;
    private int environmentWidth =
            (int) EnvironmentPersistence.getEnvironments().get(0).getPojo().getWidth();
    private int environmentHeight =
            (int) EnvironmentPersistence.getEnvironments().get(0).getPojo().getHeight();
    private static int BORDER_X = 10; //the empty space around the map
    private static int BORDER_Y = 10; //the empty space around the map
    private double CANVAS_WIDTH = environmentWidth + (BORDER_X * 2);
    private double CANVAS_HEIGHT = environmentHeight + (BORDER_Y * 2);
    protected Color backgroundColor =
            TopologyUtils.convertColorToAWT(EnvironmentPersistence.getEnvironments().get(0).getPojo()
            .getBackgroundColor());
    private EnvObjectLogic selectedObject;
    private ArrayList<Indicator> indicators = new ArrayList<Indicator>();
    private HashMap<String, Shape> cachedShapes = new HashMap<String, Shape>();
    private boolean inDrag;
    private boolean roomEditMode = false;
    private FreedomPoint originalHandleLocation = null;
    private ArrayList<Handle> handles = new ArrayList<Handle>();
    private ZoneLogic selectedZone;
    protected CalloutsUpdater callouts;
    private boolean objectEditMode = false;
    private Point messageCorner = new Point(50, 50);
    private Dimension dragDiff = null;
    private EnvironmentLogic currEnv = EnvironmentPersistence.getEnvironments().get(0);
    private HashMap<EnvObjectLogic, ObjectEditor> objEditorPanels = new HashMap<EnvObjectLogic, ObjectEditor>();

    public void openObjEditor(EnvObjectLogic obj) {
        if (objEditorPanels.containsKey(obj)) {
            if (objEditorPanels.get(obj) == null) {
                objEditorPanels.remove(obj);
                objEditorPanels.put(obj,
                        new ObjectEditor(obj));
            }
        } else {
            objEditorPanels.put(obj,
                    new ObjectEditor(obj));
        }

        ObjectEditor currEditorPanel = objEditorPanels.get(obj);
        currEditorPanel.setVisible(true);
        currEditorPanel.toFront();
    }

    public EnvironmentLogic getCurrEnv() {
        return this.currEnv;
    }

    public void setCurrEnv(EnvironmentLogic env) {
        this.currEnv = env;
        updateEnvRelatedVars();

        setNeedRepaint(true);
    }

    public void updateEnvRelatedVars() {
        environmentWidth = currEnv.getPojo().getWidth();
        environmentHeight = currEnv.getPojo().getHeight();
        backgroundColor = TopologyUtils.convertColorToAWT(currEnv.getPojo().getBackgroundColor());
    }

    protected EnvObjectLogic getSelectedObject() {
        return selectedObject;
    }

    protected void addIndicator(Shape shape, Color color) {
        indicators.add(new Indicator(shape, color));
    }

    protected void addIndicator(Shape shape) {
        indicators.add(new Indicator(shape));
    }

    protected void removeIndicators() {
        indicators.clear();
        selectedObject = null;
        setNeedRepaint(false);
    }

    public ZoneLogic getSelectedZone() {
        return selectedZone;
    }

    private void setSelectedZone(ZoneLogic selectedZone) {
        this.selectedZone = selectedZone;
    }

    public Renderer(JavaDesktopFrontend master) {
        this.plugin = master;
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
        callouts = new CalloutsUpdater(this, 1000);
        repaint();
    }

    private void addCustomMouseListener() {
        addMouseListener(this);
    }

    private void addCustomMouseMotionListener() {
        addMouseMotionListener(this);
    }

    public synchronized void setNeedRepaint(boolean repaintBackground) {
        backgroundChanged = repaintBackground;

        Graphics2D g2 = (Graphics2D) this.getGraphics();

        if (g2 != null) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }

        this.repaint();
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

    public void prepareBackground() {
    }

    public void renderEnvironment() {
    }

    public void renderWalls() {
    }

    public void prepareForeground() {
    }

    public void renderObjects() {
    }

    public void renderPeople() {
    }

    public void renderZones() {
    }

    public void mouseEntersObject(EnvObjectLogic obj) {
    }

    public void mouseExitsObject(EnvObjectLogic obj) {
    }

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
            Freedomotic.logger.severe("Error while painting environment");
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        } finally {
            restoreTransformContext();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //long start = System.currentTimeMillis();
        if (backgroundChanged) {
            backgroundChanged = false;

            synchronized (this) {
                new Runnable() {
                    @Override
                    public void run() {
                        BufferedImage background = createDrawableCanvas();
                        paintEnvironmentLayer(background.getGraphics());
                        ResourcesManager.addResource("background", background);
                    }
                }.run();
            }
        }

        setContext(g);
        getContext().drawImage(ResourcesManager.getResource("background"),
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

    private void renderCalloutsLayer() {
        Iterator it = callouts.iterator();

        while (it.hasNext()) {
            Callout callout = (Callout) it.next();
            drawString(callout.getText(),
                    (int) callout.getPosition().getX(),
                    (int) callout.getPosition().getY(),
                    (float) 0.0,
                    callout.getColor());
        }
    }

    public void createCallout(Callout callout) {
        callouts.addCallout(callout);
    }

    private void createSceneTransformContext(Graphics g) {
        graph2D = (Graphics2D) getContext();
        originalRenderingContext = graph2D.getTransform();

        AffineTransform newRenderingContext = (AffineTransform) (originalRenderingContext.clone());
        newRenderingContext.scale(widthRescale, heightRescale);
        newRenderingContext.translate(BORDER_X, BORDER_Y);
        graph2D.setTransform(newRenderingContext);
    }

    protected Graphics getContext() {
        return graph;
    }

    private void setContext(Graphics g) {
        this.graph = g;
    }

    protected Graphics2D getRenderingContext() {
        return graph2D;
    }

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
        Graphics imgGraphics;

        try {
            img = new BufferedImage(getWidth(),
                    getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }

        return img;
    }

    public static Shape getTranslatedShape(final Shape shape, Point translation) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }

        final AffineTransform transform =
                AffineTransform.getTranslateInstance(translation.getX(),
                translation.getY());

        return transform.createTransformedShape(shape);
    }

    public static Shape getRotatedShape(Shape shape, double rotation) {
        AffineTransform localAT = null;
        Shape localShape = null;
        localAT =
                AffineTransform.getRotateInstance(Math.toRadians(rotation),
                shape.getBounds().getX(),
                shape.getBounds().getY());
        localShape = localAT.createTransformedShape(shape);

        return localShape;
    }

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

    protected void paintImageCentredOnCoords(String icon, int x, int y, Dimension dimension) {
        BufferedImage img = null;
        img = ResourcesManager.getResource(icon, (int) dimension.getWidth(), (int) dimension.getHeight());

        if (img != null) {
            getContext().drawImage(img, x - (img.getWidth() / 2), y - (img.getHeight() / 2), this);
        }
    }

    protected void paintImageCentredOnCoords(BufferedImage img, int x, int y) {
        if (img != null) {
            getContext().drawImage(img, x - (img.getWidth() / 2), y - (img.getHeight() / 2), this);
        }
    }

    protected void paintImage(EnvObject obj)
            throws RuntimeException {
        BufferedImage img = null;
        Shape shape = TopologyUtils.convertToAWT(obj.getCurrentRepresentation().getShape());
        Rectangle box = shape.getBounds();
        img = ResourcesManager.getResource(obj.getCurrentRepresentation().getIcon(),
                (int) box.getWidth(),
                (int) box.getHeight()); //-1 means no resizeing

        if (img != null) {
            getContext().drawImage(img, 0, 0, this);
        } else {
            Freedomotic.logger.warning("Cannot find image " + obj.getCurrentRepresentation().getIcon()
                    + " for object " + obj.getName());
            throw new RuntimeException();
        }
    }

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
        RoundRectangle2D round =
                new RoundRectangle2D.Double(rect.getX(),
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

    protected Point toRealCoords(Point clickPoint) {
        int x = (int) (clickPoint.getX() / widthRescale) - BORDER_X;
        int y = (int) (clickPoint.getY() / heightRescale) - BORDER_Y;

        return new Point(x, y);
    }

    protected EnvObjectLogic mouseOnObject(Point p) {
        Point mousePointer = toRealCoords(p);

        for (EnvObjectLogic logic : EnvObjectPersistence.getObjectByEnvironment(getCurrEnv().getPojo().getUUID())) {
            if (getCachedShape(logic).contains(mousePointer)) {
                return logic;
            }
        }

        return null;
    }

    protected ZoneLogic mouseOnZone(Point p) {
        Iterator it = currEnv.getZones().iterator();
        boolean onZone = false;

        while (it.hasNext()) {
            ZoneLogic zone = (ZoneLogic) it.next();

            if (zone.getPojo().isRoom()) {
                Point mouse = toRealCoords(p);
                onZone =
                        TopologyUtils.contains(zone.getPojo().getShape(),
                        new FreedomPoint((int) mouse.getX(), (int) mouse.getY()));

                if (onZone == true) {
                    return zone;
                }
            }
        }

        return null;
    }

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
        shape =
                getTranslatedShape(shape,
                new Point(x, y));
        shape =
                getRotatedShape(shape,
                obj.getCurrentRepresentation().getRotation());
        cachedShapes.put(object.getPojo().getUUID(),
                shape);

        return shape;
    }

    private void rebuildShapesCache() {
        for (EnvObjectLogic obj : EnvObjectPersistence.getObjectByEnvironment(getCurrEnv().getPojo().getUUID())) {
            rebuildShapeCache(obj);
        }
    }

    private void rebuildShapeCache(EnvObjectLogic obj) {
        applyShapeModifiers(obj);
    }

    protected Shape getCachedShape(EnvObjectLogic obj) {
        if (cachedShapes.containsKey(obj.getPojo().getUUID())) {
            return cachedShapes.get(obj.getPojo().getUUID());
        } else {
            return applyShapeModifiers(obj);
        }
    }

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
                CalloutsUpdater.clearAll();
            }
        } else { //if edit mode
            removeIndicators();
            CalloutsUpdater.clearAll();

            toRealCoords(e.getPoint());

            //click on an handle in edit mode
            Handle clickedHandle = mouseOnHandle(e.getPoint());

            if (clickedHandle != null) {
                if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON1)) {
                    clickedHandle.setSelected(true);
                } else {
                    if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1)) {
                        clickedHandle.addAdiacent();
                        createHandles(null);
                    } else {
                        if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON3)) {
                            clickedHandle.remove();
                            createHandles(null);
                        }
                    }
                }
            } else {
                //click on a zone in edit mode if no handle is selected
                ZoneLogic zone = mouseOnZone(e.getPoint());

                if (zone != null) {
                    removeIndicators();
                    addIndicator(TopologyUtils.convertToAWT(zone.getPojo().getShape()));
                    selectedZone = zone;
                    createHandles(zone);
                } else {
                    handles.clear();
                    // createHandles(null);
                }
            }

            setNeedRepaint(true);
        }
    }

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

    @Override
    public void mouseReleased(MouseEvent e) {
        if (inDrag) {
            Handle currHandle = null;
            for (Handle handle : handles) {
                if (handle.isSelected()) {
                    currHandle = handle;
                }
                handle.setSelected(false);
            }

            //stop dragging an object
            if (objectEditMode && (selectedObject != null)) {
                Point coords = toRealCoords(e.getPoint());
                selectedObject.setLocation((int) (coords.getX() - dragDiff.getWidth()),
                        (int) (coords.getY() - (int) dragDiff.getHeight()));
            }
            //check if rooms overlap
            //selectedZone = handle.getZone();
            if (roomEditMode && (selectedZone) != null) {
                Area currentZoneArea = new Area(TopologyUtils.convertToAWT(selectedZone.getPojo().getShape()));
                for (Room r : currEnv.getRooms()) {
                    if (!r.equals(selectedZone)) {
                        Shape testZoneShape = TopologyUtils.convertToAWT(r.getPojo().getShape());
                        Area testArea = new Area(testZoneShape);
                        testArea.intersect(currentZoneArea);
                        if (!testArea.isEmpty()) {
                            currHandle.move(originalHandleLocation.getX(), originalHandleLocation.getY());
                            removeIndicators();
                            addIndicator(TopologyUtils.convertToAWT(selectedZone.getPojo().getShape()));
                            break;
                        }
                    }
                }
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

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        inDrag = true;

        Point coords = toRealCoords(e.getPoint());

        if ((dragDiff == null) && (getSelectedObject() != null)) {
            dragDiff = new Dimension((int) Math.abs(coords.getX()
                    - getSelectedObject().getPojo().getCurrentRepresentation()
                    .getOffset().getX()),
                    (int) Math.abs(coords.getY()
                    - getSelectedObject().getPojo().getCurrentRepresentation()
                    .getOffset().getY()));
        }

        int xSnapped = (int) coords.getX() - ((int) coords.getX() % 5);
        int ySnapped = (int) coords.getY() - ((int) coords.getY() % 5);

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

                Callout callout =
                        new Callout(this.getClass().getCanonicalName(), "mouse", xSnapped + "cm," + ySnapped + "cm",
                        (int) coords.getX(), (int) coords.getY(), 0, -1);
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
                        Area currentZoneArea = new Area(TopologyUtils.convertToAWT(selectedZone.getPojo().getShape()));
                        for (Room r : currEnv.getRooms()) {
                            if (!r.equals(selectedZone)) {
                                Shape testZoneShape = TopologyUtils.convertToAWT(r.getPojo().getShape());
                                Area testArea = new Area(testZoneShape);
                                testArea.intersect(currentZoneArea);
                                if (!testArea.isEmpty()) {
                                    addIndicator(testZoneShape, new Color(255, 0, 0, 50));
                                }
                            }
                        }
                    }
                }

                setNeedRepaint(true);
            }
        }
    }

    public void mouseDoubleClickObject(EnvObjectLogic obj) {
    }

    public void mouseRightClickObject(EnvObjectLogic obj) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!roomEditMode) {
            EnvObjectLogic obj = mouseOnObject(e.getPoint());

            if ((obj == null) && (selectedObject != null)) {
                removeIndicators();
                mouseExitsObject(selectedObject);
                callouts.clear("object.description");
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

                //create a callot which says the coordinates of click
                Callout callout =
                        new Callout(this.getClass().getCanonicalName(), "mouse",
                        (int) mouse.getX() + "cm," + (int) mouse.getY() + "cm", (int) mouse.getX(),
                        (int) mouse.getY(), 0, -1);
                createCallout(callout);
                repaint();
            }
        }
    }

    private void clear() {
        this.indicators.clear();
        callouts.clearAll();
        this.backgroundChanged = true;
    }

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
            getContext().setColor(new Color(0, 0, 255, 50));

            if (handle.isSelected()) {
                getContext().setColor(new Color(255, 0, 0, 50));
            }

            getContext()
                    .fillRect((int) handle.getHandle().getBounds().getX(),
                    (int) handle.getHandle().getBounds().getY(),
                    (int) handle.getHandle().getBounds().getWidth(),
                    (int) handle.getHandle().getBounds().getHeight());
        }
    }

    public void setRoomEditMode(boolean edit) {
        roomEditMode = edit;

        if (roomEditMode) {
            Callout callout =
                    new Callout(this.getClass().getCanonicalName(), "info",
                    "Double click on an handle to create a now one in the middle of the segment.\n"
                    + "Right click on an handle to delete it.", 45, -45, 0, -1);
            createCallout(callout);
            createHandles(null);
        } else {
            handles.clear();
            indicators.clear();
            selectedZone = null;
            callouts.clearAll();
        }

        setNeedRepaint(true);
    }

    @Override
    public boolean getRoomEditMode() {
        return roomEditMode;
    }

    @Override
    public boolean getObjectEditMode() {
        return objectEditMode;
    }

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

    public Color getBackgroundColor() {
        return backgroundColor;
    }
}
