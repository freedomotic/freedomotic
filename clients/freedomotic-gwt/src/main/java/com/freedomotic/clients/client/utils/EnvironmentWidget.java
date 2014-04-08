package com.freedomotic.clients.client.utils;

import com.freedomotic.clients.client.api.EnvironmentsController;
import com.freedomotic.clients.client.widgets.LayerList;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.object.EnvObject;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.kiouri.sliderbar.client.solution.gmap.SliderBarGMap;

/**
 * Created by gpt on 1/04/14.
 */
public class EnvironmentWidget {

    public static Environment environment = null;

    //TODO: add get /set
    boolean dataInitialized = false;
    boolean layerControllerAttached = false;
    //timer refresh rate, in milliseconds
    private static final double MARGIN = 50;

    private Widget parent;
    private ExtendedCanvas extendedCanvas;

    private LayerList mLayerList;
    SliderBarGMap sliderBarGMap;
    public EnvironmentWidget(final Widget parent) {

        this.parent = parent;
        initCanvas();


        //Use instead of a timer
        AnimationScheduler.get().requestAnimationFrame(
            new AnimationScheduler.AnimationCallback()
            {
               @Override
               public void execute(double v) {
                   if (dataInitialized) {
                       if (!layerControllerAttached && mLayerList!= null) {
                           mLayerList.populateData(extendedCanvas.getLayers());
                           layerControllerAttached = true;
                       }
                        extendedCanvas.draw();
                   } else {
                       initializeData();
                   }
                   AnimationScheduler.get().requestAnimationFrame(this);
               }
            });

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                extendedCanvas.setSize();
                if (environment == null) {
                    //TODO: move to extended canvas
                    extendedCanvas.fitToScreen(extendedCanvas.getCanvasWitdh(), extendedCanvas.getCanvasWitdh(), 0, 0);
                }
                else
                {
                    extendedCanvas.fitToScreen(environment.getWidth(), environment.getHeight(), environment.getWidth()/2 ,environment.getHeight()/2);

                }
            }
        });

    }

    void initCanvas() {
        extendedCanvas = new ExtendedCanvas();
        //extendedCanvas.setSize();
        extendedCanvas.registerHandlers();
    }

    public void resizeToFit()
    {
        extendedCanvas.setSize();


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
            extendedCanvas.fitToScreen(environment.getWidth(), environment.getHeight(), environment.getWidth()/2 ,environment.getHeight()/2);

            if (mLayerList != null)
                mLayerList.populateData(extendedCanvas.getLayers());
            //this.parent.addNorth(new EnvListBox(this), 4);
            dataInitialized = true;

        }


    }

    void createEnvironmentLayer(Environment environment)
    {
        Layer envLayer = extendedCanvas.addLayer(environment.getUUID(), environment.getName());

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
                extendedCanvas.fitToScreen(environment.getWidth(),environment.getHeight(), environment.getWidth()/2 ,environment.getHeight()/2);
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
