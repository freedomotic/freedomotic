package com.freedomotic.clients.client.utils;

import com.freedomotic.clients.client.api.EnvironmentsController;
import com.freedomotic.clients.client.widgets.EnvObjectProperties;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.object.EnvObject;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import java.util.ArrayList;

/**
 * Created by gpt on 1/04/14.
 */
public class EnvironmentWidget {

    public static Environment environment = null;
    ArrayList<DrawableRoom> drawingRooms = new ArrayList<DrawableRoom>();
    ArrayList<DrawableObject> drawingObjects = new ArrayList<DrawableObject>();
    ArrayList<DrawableObject> objectsShowingBehaviors = new ArrayList<DrawableObject>();


    //TODO: add get /set
    boolean dataInitialized = false;
    //timer refresh rate, in milliseconds
    static final int refreshRate = 25;

    private static final double MARGIN = 50;
    private static int BORDER_X = 10; //the empty space around the map
    private static int BORDER_Y = 10; //the empty space around the map
    private static int CANVAS_WIDTH = 1300 + (BORDER_X * 2);
    private static int CANVAS_HEIGHT = 900 + (BORDER_X * 2);

    private DockLayoutPanel parent;
    private ExtendedCanvas extendedCanvas;

    public EnvironmentWidget(DockLayoutPanel parent, Environment env) {

        this.parent = parent;
        environment = env;
        initCanvas();

        // setup timer
        final Timer timer = new Timer() {
            @Override
            public void run() {
                if (dataInitialized) {
                    extendedCanvas.draw();
                } else {
                    initializeData();
                    //TODO: por aqui
                    //fitToScreen();
                }
            }
        };
        timer.scheduleRepeating(refreshRate);

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                int parentWidth = getCanvas().getParent().getOffsetWidth();
                int parentHeight = getCanvas().getParent().getOffsetHeight();
                extendedCanvas.setSize(parentWidth, parentHeight);
                if (environment == null) {
                    extendedCanvas.fitToScreen(parentWidth, parentHeight);
                }
                else
                {
                    extendedCanvas.fitToScreen(environment.getWidth(), environment.getHeight());

                }

            }
        });


    }

    void initCanvas() {
        extendedCanvas = new ExtendedCanvas();
        extendedCanvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        //TODO: Refactor to make EnvironmentWidget not be aware of DrawableElements
        extendedCanvas.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final DrawableElement de = extendedCanvas.getElementUnderCoordinates(event.getX(), event.getY());
                if (de != null && de instanceof DrawableObject){
                    final DrawableObject dobj = (DrawableObject)de;
                    final EnvObjectProperties eop = new EnvObjectProperties(dobj.getEnvObject());
                    if (!dobj.isShowingBehavioursPanel()) //to avoid blinking
                    {
                        int left = extendedCanvas.getCanvas().getAbsoluteLeft();// + dobj.getCurrentWidth() / 4;
                        int top =  extendedCanvas.getCanvas().getAbsoluteTop();// - dobj.getCurrentHeight() / 2;
                        dobj.showBehavioursPanel(left, top, extendedCanvas.getmScaleFactor());
                        objectsShowingBehaviors.add(dobj);
                    }
                }

            }
        });

        extendedCanvas.addMouseMoveHandler(new MouseMoveHandler() {
            //TODO: maybe there is a better way making DrawableObjects implement MouseListener and transforming it on widgets
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
                //TODO: move to the extendedCanvas
                final DrawableElement de =  extendedCanvas.getElementUnderCoordinates(event.getX(), event.getY());
                if (de != null && de instanceof DrawableObject){
                    DrawableObject dobj = (DrawableObject)de;
                    if (!dobj.isShowingBehavioursPanel()) //to avoid blinking
                    {
                        int left = extendedCanvas.getCanvas().getAbsoluteLeft();
                        int top = extendedCanvas.getCanvas().getAbsoluteTop();
                        dobj.showBehavioursPanel(left, top, extendedCanvas.getmScaleFactor());
                        objectsShowingBehaviors.add(dobj);

                    }
                } else {
                    for (DrawableObject dobj2 : objectsShowingBehaviors) {
                        dobj2.hideBehavioursPanel();
                    }
                    objectsShowingBehaviors.clear();

                }
            }
        });

    }

    void initializeData() {
        //TODO: Refactor to make EnvironmentWidget not be aware of DrawableElements
        if (EnvironmentsController.getInstance().HasData()) {
            if (environment == null) {
                environment = EnvironmentsController.getInstance().getEnvironments().get(0);
            }
            DrawableEnvironment drawableEnvironment = new DrawableEnvironment(environment);
            extendedCanvas.initCanvas();
            extendedCanvas.addDrawingElement(drawableEnvironment);
            drawingRooms.clear();
            drawingObjects.clear();

            // create all drawingrooms
            for (Zone r : environment.getZones()) {
                if (r.isRoom()) {
                    DrawableRoom dr = new DrawableRoom(r);
                    drawingRooms.add(dr);
                    extendedCanvas.addDrawingElement(dr);
                    // TODO: Take care of the objects not in room
                    for (EnvObject obj : r.getObjects()) {
                        DrawableObject dobj = new DrawableObject(obj);
                        drawingObjects.add(dobj);
                        extendedCanvas.addDrawingElement(dobj);
                    }
                }
            }
            extendedCanvas.fitToScreen(environment.getWidth(), environment.getHeight());
            //this.parent.addNorth(new EnvListBox(this), 4);
            dataInitialized = true;

        }


    }

    public Canvas getCanvas() {
        return extendedCanvas.getCanvas();
    }


    public void setEnvironment(String envUUID) {
        for (Environment env : EnvironmentsController.getInstance().getEnvironments()) {
            if (env.getUUID().equals(envUUID)) {
                environment = env;
                this.dataInitialized = false;
                break;
            }
        }
    }

}
