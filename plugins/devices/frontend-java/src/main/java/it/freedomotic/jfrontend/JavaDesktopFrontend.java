/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package it.freedomotic.jfrontend;

import it.freedomotic.api.Actuator;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.events.ObjectHasChangedBehavior;
import it.freedomotic.events.ZoneHasChanged;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.jfrontend.extras.GraphPanel;
import it.freedomotic.jfrontend.utils.SplashLogin;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.reactions.Command;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Enrico
 */
public class JavaDesktopFrontend
        extends Actuator {

    private MainWindow window;
    private Drawer drawer;
    private Map<String, GraphPanel> graphs = new HashMap<String, GraphPanel>();
    private SplashLogin sl;
    private static final Logger LOG = Logger.getLogger(JavaDesktopFrontend.class.getName());
    private boolean init = false;

    public JavaDesktopFrontend() {
        super("Desktop Frontend", "/frontend-java/desktop-frontend.xml");
    }

    @Override
    public void onStop() {
        window.setVisible(false);
        window.dispose();
        window = null;
        //listDrawer = null;
        drawer = null;
        for (GraphPanel pg : graphs.values()) {
            pg.dispose();
        }
        graphs.clear();
        sl = null;
    }

    @Override
    public void onStart() {
        try {
            //onEvent stuff
            addEventListener("app.event.sensor.object.behavior.change");
            addEventListener("app.event.sensor.environment.zone.change");
            addEventListener("app.event.sensor.plugin.change");
            if (getApi().getAuth().isInited()) {
                sl = new SplashLogin(this);
                if (!init) {
                    sl.trySSO();
                }
            }
            init = true;
        } catch (Exception e) {
            LOG.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    public void createMainWindow() {

        if (window == null) {
            window = new MainWindow(this);
        }
        window.setVisible(true);
        
        if (sl != null) {
            sl.setVisible(false);
            sl.dispose();
            sl = null;
        }

        LOG.log(Level.INFO, "JFrontend running as user: {0}", getApi().getAuth().getPrincipal());
    }

    public MainWindow getMainWindow() {
        return window;
    }

    protected Drawer createRenderer(EnvironmentLogic env) {
        try {
            if (env.getPojo().getRenderer().equalsIgnoreCase("photo")) {
                drawer = new PhotoDrawer(this);
                drawer.setCurrEnv(env);
            } else if (env.getPojo().getRenderer().equalsIgnoreCase("image")) {
                drawer = new ImageDrawer(this);
                drawer.setCurrEnv(env);
            } else if (env.getPojo().getRenderer().equalsIgnoreCase("plain")) {
                drawer = new PlainDrawer(this);
                drawer.setCurrEnv(env);
            } else {
                drawer = new ListDrawer(this);
                drawer.setCurrEnv(env);
            }
        } catch (Exception e) {
            Logger.getLogger(JavaDesktopFrontend.class.getName()).severe("Error while initializing a drawer in desktop frontend.");
            e.printStackTrace();
        }

        if (drawer instanceof Renderer) {
            Renderer renderer = (Renderer) drawer;
            renderer.callouts = new CalloutsUpdater(renderer, 1000);
        }

        return drawer;
    }

    @Override
    protected void onCommand(final Command c)
            throws IOException, UnableToExecuteException {
        String callout = c.getProperty("callout.message");

        if (callout != null) {
            Callout callout1 = new Callout(this.getClass().getCanonicalName(),
                    "info",
                    callout,
                    0,
                    0,
                    0,
                    0);
            drawer.createCallout(callout1);
        } else if (c.getProperty("command").equals("GRAPH-DATA")) { // show GUI
            // get related object from address
            EnvObjectLogic obj = (EnvObjectLogic) getApi().getObjectByAddress("harvester", c.getProperty("event.object.address")).toArray()[0];
            // open GraphWindow related to the object
            if (obj.getBehavior("data") != null) { // has a 'data' behavior
                if (!obj.getBehavior("data").getValueAsString().isEmpty()) {
                    GraphPanel gw = graphs.get(obj.getPojo().getUUID());
                    if (gw != null) {
                        gw.reDraw();
                    } else {
                        gw = new GraphPanel(this, obj);
                        graphs.put(obj.getPojo().getUUID(), gw);
                    }
                }

                //sendBack(c);
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        public void run() {
                            //Custom button text
                            if (c.getProperty("options") != null) {
                                Object[] options = c.getProperty("options").split(";");
                                int n =
                                        JOptionPane.showOptionDialog(window,
                                        c.getProperty("question"),
                                        "Please reply within "
                                        + (int) (c.getReplyTimeout() / 1000)
                                        + " seconds",
                                        JOptionPane.YES_NO_CANCEL_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        options,
                                        options[2]);
                                c.setProperty("result",
                                        options[n].toString());
                            }

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
                for (GraphPanel gp : graphs.values()) {
                    gp.reDraw();
                }
                if (drawer != null) {
                    drawer.setNeedRepaint(true);

                }
            } else {
                if (event instanceof ZoneHasChanged) {
                    //writing the string on the screen
                    String zoneDesc = event.getProperty("zone.description");
                    Callout callout = new Callout(zoneDesc, 2000, Color.blue);
                    drawer.createCallout(callout);
                    drawer.setNeedRepaint(true);
                } else {
                    // TODO check why a NPE was raised sometimes
                    if (null != window) {
                        final PluginJList pluginJList = window.getPluginJList();
                        if (null != pluginJList) {
                            pluginJList.update();
                        }
                    }
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
