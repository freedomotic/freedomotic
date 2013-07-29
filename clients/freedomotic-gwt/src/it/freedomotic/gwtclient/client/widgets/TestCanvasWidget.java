package it.freedomotic.gwtclient.client.widgets;

import com.google.gwt.canvas.client.Canvas;

public class TestCanvasWidget {

    private static int BORDER_X = 10; //the empty space around the map
    private static int BORDER_Y = 10; //the empty space around the map
    private static int CANVAS_WIDTH = 1300 + (BORDER_X * 2);
    private static int CANVAS_HEIGHT = 900 + (BORDER_X * 2);
    private double ENVIRONMENT_WIDTH = CANVAS_WIDTH;
    private double ENVIRONMENT_HEIGHT = CANVAS_HEIGHT;
    public Canvas canvas;

    public void TestCanvasWidget() {
        canvas = Canvas.createIfSupported();
        canvas.setWidth(CANVAS_WIDTH + "px");
        canvas.setHeight(CANVAS_HEIGHT + "px");
        canvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        canvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);
    }
}
