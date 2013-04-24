package it.freedomotic.jfrontend;

import it.freedomotic.api.Actuator;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.ListenEventsOn;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.events.MessageEvent;
import it.freedomotic.events.ObjectHasChangedBehavior;
import it.freedomotic.events.ZoneHasChanged;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author Enrico
 */
public class JavaDesktopFrontend extends Actuator {

    private MainWindow window;
    private Drawer drawer;
    private ListDrawer listDrawer;

    public JavaDesktopFrontend() {
        super("Desktop Frontend", "/it.freedomotic.jfrontend/desktop-frontend.xml");
    }

    @Override
    public void onStart() {
        try {
            //onEvent stuff
            addEventListener("app.event.sensor.object.behavior.change");
            addEventListener("app.event.sensor.environment.zone.change");
            addEventListener("app.event.sensor.plugin.change");
            //addEventListener("app.event.sensor.messages.callout");
            //onCommand stuff
            addCommandListener("app.actuators.plugins.controller.in");
            createMainWindow(); //creates the main frame
            listDrawer = new ListDrawer();
            listDrawer.setVisible(true);
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    public void createMainWindow() {
        if (window != null) {
            window.setVisible(false);
            window.dispose();
        }
        window = new MainWindow(this);
        window.setVisible(true);
    }

    public MainWindow getMainWindow() {
        return window;
    }

    protected Drawer createRenderer(EnvironmentLogic env) {
        try {
            if (env.getPojo().getRenderer().equalsIgnoreCase("photo")) {
                drawer = new PhotoDrawer(this);
                drawer.setCurrEnv(env);
            } else {
                if (env.getPojo().getRenderer().equalsIgnoreCase("image")) {
                    drawer = new ImageDrawer(this);
                    drawer.setCurrEnv(env);
                } else {
                    if (env.getPojo().getRenderer().equalsIgnoreCase("image")) {
                        drawer = new PlainDrawer(this);
                        drawer.setCurrEnv(env);
                    } else {
                        drawer = new ListDrawer();
                        drawer.setCurrEnv(env);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error while initializing a drawer in desktop frontend.");
            e.printStackTrace();
        }
        if (drawer instanceof Renderer) {
            Renderer renderer = (Renderer) drawer;
            renderer.callouts = new CalloutsUpdater(renderer, 1000);
        }
        return drawer;
    }

    @Override
    protected void onCommand(final Command c) throws IOException, UnableToExecuteException {
        String callout = c.getProperty("callout.message");
        if (callout != null) {
            Callout callout1 = new Callout(this.getClass().getCanonicalName(), "info", callout, 0, 0, 0, 0);
            drawer.createCallout(callout1);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        public void run() {
                            //Custom button text
                            Object[] options = c.getProperty("options").split(";");
                            int n = JOptionPane.showOptionDialog(window,
                                    c.getProperty("question"),
                                    "Please reply within " + (int) (c.getReplyTimeout() / 1000) + " seconds",
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[2]);
                            c.setProperty("result", options[n].toString());
                            //sendBack(c);
                        }
                    }).start();
                }
            });
        }
    }

    @Override
    protected void onEvent(EventTemplate event) {
        if (isRunning()) {
            if (event instanceof ObjectHasChangedBehavior) {
                drawer.setNeedRepaint(true);
            } else {
                if (event instanceof ZoneHasChanged) {
                    //writing the string on the screen
                    String zoneDesc = event.getProperty("zone.description");
                    Callout callout = new Callout(
                            zoneDesc,
                            2000,
                            Color.blue);
                    drawer.createCallout(callout);
                    drawer.setNeedRepaint(true);
                } else {
                    window.getPluginJList().update();
                }
            }
        }
    }
    
//    //annotation doesen't work because annotation parsing is enabled only in Protocol subclasses
//    @ListenEventsOn(channel = "app.event.sensor.messages.callout")
//    public void printCallout(EventTemplate event) {
//        System.out.println("received event " + event.toString());
//        MessageEvent message = (MessageEvent) event;
//        String text = message.getProperty("message.text");
//        Callout callout = new Callout(
//                text,
//                2000,
//                Color.blue);
//        drawer.createCallout(callout);
//        drawer.setNeedRepaint(true);
//    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
