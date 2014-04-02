package com.freedomotic.clients.client.utils;

import com.freedomotic.clients.client.api.EnvironmentsController;
import com.freedomotic.clients.client.widgets.LayerList;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.object.EnvObject;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;

/**
 * Created by gpt on 1/04/14.
 */
public class EnvironmentWidget {

    public static Environment environment = null;

    //TODO: add get /set
    boolean dataInitialized = false;
    boolean layerControllerAttached = false;
    //timer refresh rate, in milliseconds
    static final int refreshRate = 25;

    private static final double MARGIN = 50;
    private static int BORDER_X = 10; //the empty space around the map
    private static int BORDER_Y = 10; //the empty space around the map
    private static int CANVAS_WIDTH = 1300 + (BORDER_X * 2);
    private static int CANVAS_HEIGHT = 900 + (BORDER_X * 2);

    private DockLayoutPanel parent;
    private ExtendedCanvas extendedCanvas;

    private LayerList mLayerList;

    public EnvironmentWidget(DockLayoutPanel parent) {

        this.parent = parent;
        initCanvas();

        // setup timer
        final Timer timer = new Timer() {
            @Override
            public void run() {
                if (dataInitialized) {
                    if (!layerControllerAttached && mLayerList!= null) {
                        mLayerList.populateData(extendedCanvas.getLayers());
                        layerControllerAttached = true;
                    }

                    extendedCanvas.draw();
                } else {
                    initializeData();
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
                    extendedCanvas.fitToScreen(parentWidth, parentHeight, 0, 0);
                }
                else
                {
                    //TODO: Maybe we need to center on the environment
                    extendedCanvas.fitToScreen(environment.getWidth(), environment.getHeight(), 0 ,0);

                }
            }
        });


    }

    void initCanvas() {
        extendedCanvas = new ExtendedCanvas();
        extendedCanvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        extendedCanvas.registerHandlers();
    }

    void initializeData() {
        extendedCanvas.initCanvas();
        //TODO: Refactor to make EnvironmentWidget not be aware of DrawableElements
        if (EnvironmentsController.getInstance().HasData()) {
            for(Environment environment: EnvironmentsController.getInstance().getEnvironments()) {
                createEnvironmentLayer(environment);
            }
            environment = EnvironmentsController.getInstance().getEnvironments().get(0);
            extendedCanvas.changeLayerVisibility(environment.getUUID(), true);
            //TODO: maybe we need to center on the environment coordinates
            extendedCanvas.fitToScreen(environment.getWidth(), environment.getHeight(), 0 ,0);

            if (mLayerList != null)
                mLayerList.populateData(extendedCanvas.getLayers());
            //this.parent.addNorth(new EnvListBox(this), 4);
            dataInitialized = true;

        }


    }

    void createEnvironmentLayer(Environment environment)
    {
        Layer envLayer = extendedCanvas.addLayer(environment.getUUID());
        envLayer.setName(environment.getName());
        envLayer.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        DrawableEnvironment drawableEnvironment = new DrawableEnvironment(environment);
        extendedCanvas.addDrawingElement(drawableEnvironment, envLayer);

        // create all drawingrooms
        for (Zone r : environment.getZones()) {
            if (r.isRoom()) {
                DrawableRoom dr = new DrawableRoom(r);
                extendedCanvas.addDrawingElement(dr, envLayer);
                // TODO: Take care of the objects not in room
                for (EnvObject obj : r.getObjects()) {
                    DrawableObject dobj = new DrawableObject(obj);
                    extendedCanvas.addDrawingElement(dobj, envLayer);
                }
            }
        }


    }


    public Canvas getCanvas() {
        return extendedCanvas.getCanvas();
    }


    public void setEnvironment(String envUUID) {
        for (Environment env : EnvironmentsController.getInstance().getEnvironments()) {
            if (env.getUUID().equals(envUUID)) {
                environment = env;
                extendedCanvas.changeLayerVisibility(env.getUUID(), true);
                //this.dataInitialized = false;
            }
            else
            {
                extendedCanvas.changeLayerVisibility(env.getUUID(), false);
            }
        }
    }

    public void setLayerList(LayerList layerList)
    {
        mLayerList = layerList;
        layerList.setAssociatedEnvironment(this);
    }

}
