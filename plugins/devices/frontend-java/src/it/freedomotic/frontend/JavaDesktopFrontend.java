package it.freedomotic.frontend;

import it.freedomotic.api.Actuator;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.events.MessageEvent;
import it.freedomotic.events.ObjectHasChangedBehavior;
import it.freedomotic.events.ZoneHasChanged;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.reactions.Command;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.EventListener;
import javax.swing.JOptionPane;

/**
 *
 * @author Enrico
 */
public class JavaDesktopFrontend extends Actuator {

    private MainWindow window;
    private Renderer drawer;

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
            addEventListener("app.event.sensor.messages.>");
            //onCommand stuff
            addCommandListener("app.actuators.plugins.controller.in");
            addCommandListener("command.jfrontend.user.callout");
            createMainWindow(); //creates the main frame
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

    protected Renderer createRenderer() {
        try {
            if (Freedomotic.environment.getPojo().getRenderer().equalsIgnoreCase("photo")) {
                drawer = new PhotoDrawer(this);
            } else {
                if (Freedomotic.environment.getPojo().getRenderer().equalsIgnoreCase("image")) {
                    drawer = new ImageDrawer(this);
                } else {
                    drawer = new PlainDrawer(this);
                }
            }
        } catch (Exception e) {
            System.out.println("Error while initializing a drawer in desktop frontend.");
            e.printStackTrace();
        }
        return drawer;
    }

    @Override
    protected void onCommand(final Command c) throws IOException, UnableToExecuteException {
        String callout = c.getProperty("callout.message");
        if (callout != null) {
            Callout callout1 = new Callout(this.getClass().getCanonicalName(), "info", callout, 0, 0, 0, 10000);
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
                            sendBack(c);
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
                    if (event instanceof MessageEvent) {
                        MessageEvent message = (MessageEvent) event;
                        String text = message.getProperty("message.text");
                        Callout callout = new Callout(
                                text,
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
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
