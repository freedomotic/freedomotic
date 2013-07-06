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

import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.core.ResourcesManager;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.environment.EnvironmentPersistence;
import it.freedomotic.environment.Room;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.events.GenericEvent;
import it.freedomotic.exceptions.DaoLayerException;
import it.freedomotic.jfrontend.Renderer;
import it.freedomotic.jfrontend.utils.OpenDialogFileFilter;
import it.freedomotic.jfrontend.utils.TipOfTheDay;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.reactions.Command;
import it.freedomotic.security.Auth;
import it.freedomotic.util.I18n.ComboLanguage;
import it.freedomotic.util.I18n.I18n;
import it.freedomotic.util.Info;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author enrico
 */
public class MainWindow
        extends javax.swing.JFrame {

    private Drawer drawer;
    //private float referenceRatio;
    private static boolean isFullscreen = false;
    private JavaDesktopFrontend master;
    JDesktopPane desktopPane;
    JInternalFrame frameClient;
    JInternalFrame frameMap;
    PluginJList lstClients;
    //JComboBox cmbFilter;
    boolean editMode;
    private final Auth Auth;
    private final I18n I18n;
    boolean isAuthenticated = false;
    private static final Logger LOG = Logger.getLogger(JavaDesktopFrontend.class.getName());

    public Drawer getDrawer() {
        return drawer;
    }

    public MainWindow(final JavaDesktopFrontend master) {
        this.I18n = master.getApi().getI18n();
        UIManager.put("OptionPane.yesButtonText", I18n.msg("yes"));
        UIManager.put("OptionPane.noButtonText", I18n.msg("no"));
        UIManager.put("OptionPane.cancelButtonText", I18n.msg("cancel"));
        this.master = master;
        this.Auth = master.getApi().getAuth();
        ObjectEditor.setAPI(master.getApi());

        setWindowedMode();
        updateMenusPermissions();

        String defEnv = master.getApi().getConfig().getProperty("KEY_ROOM_XML_PATH");
        EnvironmentLogic env = EnvironmentPersistence.getEnvByUUID(defEnv.substring(defEnv.length() - 41, defEnv.length() - 5));
        setEnvironment(env);

        checkDeletableEnvironments();

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());

        if (master.configuration.getBooleanProperty("show.tips", true)) {
            new TipOfTheDay(master);
        }
    }

    private void updateMenusPermissions() {
        mnuSwitchUser.setEnabled(Auth.isInited());
        mnuNewEnvironment.setEnabled(Auth.isPermitted("environments:create"));
        mnuOpenEnvironment.setEnabled(Auth.isPermitted("environments:load"));
        frameMap.setVisible(Auth.isPermitted("environments:read"));
        mnuSave.setEnabled(Auth.isPermitted("environments:save"));
        mnuSaveAs.setEnabled(Auth.isPermitted("environments:save"));
        mnuRenameEnvironment.setEnabled(Auth.isPermitted("environments:update"));
        mnuPluginConfigure.setEnabled(Auth.isPermitted("plugins:update"));
        mnuPluginList.setEnabled(Auth.isPermitted("plugins:read"));
        frameClient.setVisible(Auth.isPermitted("plugins:read"));
        mnuPrivileges.setEnabled(Auth.isPermitted("auth:privileges:read") || Auth.isPermitted("auth:privileges:update"));
        mnuSelectEnvironment.setEnabled(master.getApi().getEnvironments().size() > 1);
    }

    private void setEnvironment(EnvironmentLogic input) {
        drawer.setCurrEnv(input);
        setMapTitle(input.getPojo().getName());

        master.getApi().getConfig().setProperty("KEY_ROOM_XML_PATH",
                input.getSource().toString().replace(
                new File(Info.PATH_DATA_FOLDER + "/furn").toString(), ""));
    }

//    public void showTipsOnStartup(boolean show) {
//        master.configuration.setProperty("show.tips", new Boolean(show).toString());
//    }
    private class MyDispatcher
            implements KeyEventDispatcher {

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    master.getMainWindow().setWindowedMode();
                }

                if (e.getKeyCode() == KeyEvent.VK_F11) {
                    master.getMainWindow().setFullscreenMode();
                }
            }

            return false;
        }
    }

    protected void setEditMode(boolean editMode) {
        this.editMode = editMode;
        mnuRenameRoom.setEnabled(editMode);
        mnuRemoveRoom.setEnabled(editMode);
        mnuAddRoom.setEnabled(editMode);
        mnuRoomBackground.setEnabled(editMode);
    }

    private void checkDeletableEnvironments() {
        // disable remove option if ther's just an available environment
        if (EnvironmentPersistence.getEnvironments().size() == 1) {
            mnuDelete.setEnabled(false);
        } else {
            mnuDelete.setEnabled(true);
        }

    }

    private void setWindowedMode() {
        this.setVisible(false);
        this.dispose();
        this.setUndecorated(false);
        this.setResizable(true);

        try {
            this.getContentPane().removeAll();
        } catch (Exception e) {
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            LOG.severe("Cannot find system look&feel\n" + ex.toString());
        } catch (InstantiationException ex) {
            LOG.severe("Cannot instantiate system look&feel\n" + ex.toString());
        } catch (IllegalAccessException ex) {
            LOG.severe("Illegal access to system look&feel\n" + ex.toString());
        } catch (UnsupportedLookAndFeelException ex) {
            LOG.severe("Unsupported system look&feel\n" + ex.toString());
        }

        setDefaultLookAndFeelDecorated(true);
        initComponents();
        setLayout(new BorderLayout());
        desktopPane = new JDesktopPane();
        lstClients = new PluginJList(this);
        frameClient = new JInternalFrame();
        frameClient.setLayout(new BorderLayout());

        JScrollPane clientScroll = new JScrollPane(lstClients);
        frameClient.add(clientScroll, BorderLayout.CENTER);
        frameClient.setTitle(I18n.msg("loaded_plugins"));
        frameClient.setResizable(true);
        frameClient.setMaximizable(true);
        frameMap = new JInternalFrame();
        setMapTitle("");
        frameMap.setMaximizable(true);
        frameMap.setResizable(true);
        desktopPane.add(frameMap);
        desktopPane.add(frameClient);
        frameClient.moveToFront();
        frameClient.setVisible(Auth.isPermitted("plugins:read"));
        desktopPane.moveToFront(this);
        this.getContentPane().add(desktopPane);

        try {
            frameClient.setSelected(true);
        } catch (PropertyVetoException ex) {
            LOG.severe(Freedomotic.getStackTraceInfo(ex));
        }

        EnvironmentLogic previousEnv = EnvironmentPersistence.getEnvironments().get(0);

        if (drawer != null) {
            previousEnv = drawer.getCurrEnv();
        }

        initializeRenderer(previousEnv);
        drawer = master.createRenderer(previousEnv);

        if (drawer != null) {
            setDrawer(drawer);
            ResourcesManager.clear();
        } else {
            LOG.severe("Unable to create a drawer to render the environment on the desktop frontend");
        }

        this.setTitle("Freedomotic " + Info.getLicense() + " - www.freedomotic.com");
        this.setSize(1100, 700);
        centerFrame(this);
        frameClient.moveToFront();
        frameMap.moveToFront();
        optimizeFramesDimension();
        drawer.repaint();
        lstClients.update();
        frameClient.setVisible(Auth.isPermitted("plugins:read"));
        frameMap.setVisible(Auth.isPermitted("environments:read"));
        setEditMode(false);
        this.setVisible(true);
        isFullscreen = false;

    }

    private void setFullscreenMode() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        if (gd.isFullScreenSupported()) {
            frameMap.setVisible(false);
            menuBar.setVisible(false);
            frameMap.dispose();
            if (Auth.isPermitted("plugins:read")) {
                frameClient.setVisible(false);
                frameClient.dispose();
            }
            desktopPane.removeAll();
            desktopPane.moveToBack(this);
            setVisible(false);
            dispose();
            setUndecorated(true);
            setResizable(false);
            setLayout(new BorderLayout());
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            drawer = master.createRenderer(drawer.getCurrEnv());

            if (drawer != null) {
                setDrawer(drawer);
            } else {
                LOG.severe("Unable to create a drawer to render the environment on the desktop frontend in fullscreen mode");
            }

            gd.setFullScreenWindow(this);
            add(drawer);
            drawer.repaint();
            drawer.setVisible(true);
            Callout callout = new Callout(this.getClass().getCanonicalName(), "info", I18n.msg("esc_to_exit_fullscreen"), 100, 100, 0, 5000);
            drawer.createCallout(callout);
            this.repaint();
            this.setVisible(true);
            isFullscreen = true;
        }
    }

    private void logUser() {
        // send command to restart java frontend
        Command c = new Command();
        c.setName("Restart Java frontend");
        c.setReceiver("app.actuators.plugins.controller.in");
        c.setProperty("plugin", master.getName());
        c.setProperty("action", "restart"); //the default choice

        Freedomotic.sendCommand(c);
    }

    private void changeRenderer(String renderer) {
        drawer.getCurrEnv().getPojo().setRenderer(renderer.toLowerCase());
        master.getMainWindow().setWindowedMode();
    }

    public JInternalFrame getFrameMap() {
        return frameMap;
    }

    public PluginJList getPluginJList() {
        return lstClients;
    }

    public static void centerFrame(JFrame frame) {
        frame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - frame.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - frame.getHeight()) / 2);
    }

    public void maximizeMap() {
        try {
            frameMap.setMaximum(true);
        } catch (Exception e) {
        }
    }

    public void setQuarterSize() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) dim.getWidth() / 2, (int) dim.getHeight() / 2);
    }

    public void optimizeFramesDimension() {
        try {
            if (!frameMap.isMaximum() || !frameClient.isIcon()) {
                frameClient.setBounds(0,
                        0,
                        desktopPane.getWidth() / 3,
                        desktopPane.getHeight());
                frameMap.setBounds(desktopPane.getWidth() / 3,
                        0,
                        (desktopPane.getWidth() / 3) * 2,
                        desktopPane.getHeight());
            } else {
                maximizeMap();
                frameClient.hide();
            }
        } catch (Exception e) {
        }
    }

    public void initializeRenderer(EnvironmentLogic prevEnv) {
        drawer = null;
        frameMap.dispose();
        frameMap = new JInternalFrame();
        frameMap.setBackground(new java.awt.Color(38, 186, 254));
        frameMap.setIconifiable(false);
        frameMap.setMaximizable(true);
        frameMap.setResizable(true);
        setMapTitle(I18n.msg("not_inited") + I18n.msg("inited"));
        desktopPane.add(frameMap, javax.swing.JLayeredPane.DEFAULT_LAYER);
//        referenceRatio = new Float(prevEnv.getPojo().getWidth() / new Float(prevEnv.getPojo().getWidth()));

    }

    public void setDrawer(Drawer drawer) {
        frameMap.getContentPane().add(drawer);
        Renderer renderer = (Renderer) drawer;
        desktopPane.setBackground(renderer.getBackgroundColor());
        frameMap.getContentPane().setBackground(renderer.backgroundColor);
        setMapTitle(drawer.getCurrEnv().getPojo().getName());
    }

    public void setMapTitle(String name) {
        String envName = "";
        try {
            envName = drawer.getCurrEnv().getSource().getParentFile().getName() + "/";
        } catch (Exception e) {
            //do nothing, this is not important
        }
        frameMap.setTitle(I18n.msg("environment") + ": " + envName + name);

    }

    class StringListModel
            extends AbstractListModel {

        private java.util.List<String> list;

        public StringListModel(ArrayList<String> strings) {
            list = strings;
        }

        @Override
        public Object getElementAt(int index) {
            return list.get(index);
        }

        @Override
        public int getSize() {
            return list.size();
        }
    }

    /*
     * AUTOGENERATED CODE BELOW - DON'T MIND IT
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        scrollTxtOut1 = new javax.swing.JScrollPane();
        txtOut1 = new javax.swing.JTextArea();
        scrollTxtOut2 = new javax.swing.JScrollPane();
        txtOut2 = new javax.swing.JTextArea();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        menuBar = new javax.swing.JMenuBar();
        mnuOpenNew = new javax.swing.JMenu();
        mnuNewEnvironment = new javax.swing.JMenuItem();
        mnuOpenEnvironment = new javax.swing.JMenuItem();
        mnuSave = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        mnuSwitchUser = new javax.swing.JMenuItem();
        mnuExit = new javax.swing.JMenuItem();
        mnuEditMode = new javax.swing.JMenu();
        mnuSelectEnvironment = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        mnuRenameEnvironment = new javax.swing.JMenuItem();
        mnuAddDuplicateEnvironment = new javax.swing.JMenuItem();
        mnuChangeRenderer = new javax.swing.JMenuItem();
        mnuBackground = new javax.swing.JMenuItem();
        mnuDelete = new javax.swing.JMenuItem();
        mnuRoomEditMode = new javax.swing.JCheckBoxMenuItem();
        jMenu3 = new javax.swing.JMenu();
        mnuRenameRoom = new javax.swing.JMenuItem();
        mnuAddRoom = new javax.swing.JMenuItem();
        mnuRoomBackground = new javax.swing.JMenuItem();
        mnuRemoveRoom = new javax.swing.JMenuItem();
        mnuObjects = new javax.swing.JMenu();
        mnuObjectEditMode = new javax.swing.JCheckBoxMenuItem();
        jMenu2 = new javax.swing.JMenu();
        mnuAutomations = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jCheckBoxMarket = new javax.swing.JCheckBoxMenuItem();
        mnuPluginConfigure = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        mnuLanguage = new javax.swing.JMenuItem();
        mnuPrivileges = new javax.swing.JMenuItem();
        mnuWindow = new javax.swing.JMenu();
        mnuPluginList = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuTutorial = new javax.swing.JMenuItem();
        submnuHelp = new javax.swing.JMenuItem();

        jTextField1.setText("jTextField1");

        scrollTxtOut1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtOut1.setEditable(false);
        txtOut1.setColumns(20);
        txtOut1.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        txtOut1.setRows(5);
        txtOut1.setWrapStyleWord(true);
        txtOut1.setName("txtOutput"); // NOI18N
        txtOut1.setOpaque(false);
        scrollTxtOut1.setViewportView(txtOut1);

        scrollTxtOut2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtOut2.setEditable(false);
        txtOut2.setColumns(20);
        txtOut2.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        txtOut2.setRows(5);
        txtOut2.setWrapStyleWord(true);
        txtOut2.setName("txtOutput"); // NOI18N
        txtOut2.setOpaque(false);
        scrollTxtOut2.setViewportView(txtOut2);

        jMenuItem4.setText("jMenuItem4");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Freedomotic");
        setBackground(java.awt.SystemColor.window);
        setBounds(new java.awt.Rectangle(50, 20, 0, 0));
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(500, 400));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });

        mnuOpenNew.setText(I18n.msg("file"));
        mnuOpenNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenNewActionPerformed(evt);
            }
        });

        mnuNewEnvironment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        mnuNewEnvironment.setText(I18n.msg("new") + I18n.msg("environment"));
        mnuNewEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuNewEnvironmentActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuNewEnvironment);

        mnuOpenEnvironment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        mnuOpenEnvironment.setText(I18n.msg("open") + I18n.msg("environment"));
        mnuOpenEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenEnvironmentActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuOpenEnvironment);

        mnuSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        mnuSave.setText(I18n.msg("save") + I18n.msg("environment"));
        mnuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuSave);

        mnuSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuSaveAs.setText(I18n.msg("save_X_as",new Object[]{I18n.msg("environment")}));
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuSaveAs);
        mnuOpenNew.add(jSeparator1);

        mnuSwitchUser.setText(I18n.msg("change_user"));
        mnuSwitchUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSwitchUserActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuSwitchUser);

        mnuExit.setText(I18n.msg("exit"));
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuExit);

        menuBar.add(mnuOpenNew);

        mnuEditMode.setText(I18n.msg("environment"));

        mnuSelectEnvironment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        mnuSelectEnvironment.setText(I18n.msg("select_X",new Object[]{I18n.msg("area_floor")}));
        mnuSelectEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSelectEnvironmentActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuSelectEnvironment);

        jMenu4.setText(I18n.msg("area_floor"));

        mnuRenameEnvironment.setText(I18n.msg("rename"));
        mnuRenameEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRenameEnvironmentActionPerformed(evt);
            }
        });
        jMenu4.add(mnuRenameEnvironment);

        mnuAddDuplicateEnvironment.setText(I18n.msg("add")+"/"+I18n.msg("duplicate"));
        mnuAddDuplicateEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAddDuplicateEnvironmentActionPerformed(evt);
            }
        });
        jMenu4.add(mnuAddDuplicateEnvironment);

        mnuChangeRenderer.setText(I18n.msg("change_X",new Object[]{I18n.msg("renderer")}));
        mnuChangeRenderer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuChangeRendererActionPerformed(evt);
            }
        });
        jMenu4.add(mnuChangeRenderer);

        mnuBackground.setText(I18n.msg("change_X",new Object[]{I18n.msg("background")}));
        mnuBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuBackgroundActionPerformed(evt);
            }
        });
        jMenu4.add(mnuBackground);

        mnuDelete.setText(I18n.msg("delete"));
        mnuDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuDeleteActionPerformed(evt);
            }
        });
        jMenu4.add(mnuDelete);

        mnuEditMode.add(jMenu4);

        mnuRoomEditMode.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        mnuRoomEditMode.setText(I18n.msg("X_edit_mode",new Object[]{I18n.msg("rooms")}));
        mnuRoomEditMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRoomEditModeActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuRoomEditMode);

        jMenu3.setText(I18n.msg("rooms"));

        mnuRenameRoom.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        mnuRenameRoom.setText(I18n.msg("rename") + I18n.msg("room"));
        mnuRenameRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRenameRoomActionPerformed(evt);
            }
        });
        jMenu3.add(mnuRenameRoom);

        mnuAddRoom.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        mnuAddRoom.setText(I18n.msg("add") + I18n.msg("room"));
        mnuAddRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAddRoomActionPerformed(evt);
            }
        });
        jMenu3.add(mnuAddRoom);

        mnuRoomBackground.setText(I18n.msg("change_X",new Object[]{I18n.msg("background")}));
        mnuRoomBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRoomBackgroundActionPerformed(evt);
            }
        });
        jMenu3.add(mnuRoomBackground);

        mnuRemoveRoom.setText(I18n.msg("remove") + I18n.msg("room"));
        mnuRemoveRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRemoveRoomActionPerformed(evt);
            }
        });
        jMenu3.add(mnuRemoveRoom);

        mnuEditMode.add(jMenu3);

        menuBar.add(mnuEditMode);

        mnuObjects.setText(I18n.msg("objects"));

        mnuObjectEditMode.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        mnuObjectEditMode.setText(I18n.msg("X_edit_mode",new Object[]{I18n.msg("objects")}));
        mnuObjectEditMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuObjectEditModeActionPerformed(evt);
            }
        });
        mnuObjects.add(mnuObjectEditMode);

        menuBar.add(mnuObjects);

        jMenu2.setText(I18n.msg("automations"));

        mnuAutomations.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
        mnuAutomations.setText(I18n.msg("manage") + I18n.msg("automations"));
        mnuAutomations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAutomationsActionPerformed(evt);
            }
        });
        jMenu2.add(mnuAutomations);

        menuBar.add(jMenu2);

        jMenu1.setText(I18n.msg("plugins"));

        jCheckBoxMarket.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0));
        jCheckBoxMarket.setText(I18n.msg("install_from_marketplace"));
        jCheckBoxMarket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMarketActionPerformed(evt);
            }
        });
        jMenu1.add(jCheckBoxMarket);

        mnuPluginConfigure.setText(I18n.msg("configure"));
        mnuPluginConfigure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPluginConfigureActionPerformed(evt);
            }
        });
        jMenu1.add(mnuPluginConfigure);

        menuBar.add(jMenu1);

        jMenu5.setText(I18n.msg("settings"));

        mnuLanguage.setText(I18n.msg("language"));
        mnuLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLanguageActionPerformed(evt);
            }
        });
        jMenu5.add(mnuLanguage);

        mnuPrivileges.setText(I18n.msg("privileges"));
        mnuPrivileges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPrivilegesActionPerformed(evt);
            }
        });
        jMenu5.add(mnuPrivileges);

        menuBar.add(jMenu5);

        mnuWindow.setText(I18n.msg("window"));

        mnuPluginList.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        mnuPluginList.setText(I18n.msg("X_list",new Object[]{I18n.msg("plugins")}));
        mnuPluginList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPluginListActionPerformed(evt);
            }
        });
        mnuWindow.add(mnuPluginList);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        jMenuItem3.setText(I18n.msg("fullscreen"));
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        mnuWindow.add(jMenuItem3);

        menuBar.add(mnuWindow);

        mnuHelp.setText(I18n.msg("help"));

        mnuTutorial.setText(I18n.msg("tutorial"));
        mnuTutorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTutorialActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuTutorial);

        submnuHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        submnuHelp.setText(I18n.msg("about"));
        submnuHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submnuHelpActionPerformed(evt);
            }
        });
        mnuHelp.add(submnuHelp);

        menuBar.add(mnuHelp);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
    }//GEN-LAST:event_formMouseClicked

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
    }//GEN-LAST:event_formMouseMoved

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFreedomotic();
    }//GEN-LAST:event_formWindowClosing

    private void submnuHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submnuHelpActionPerformed

        JOptionPane.showMessageDialog(this, ""
                + I18n.msg("running_as_user") + ": " + Auth.getPrincipal() + "\n"
                + I18n.msg("author") + ": " + Info.getAuthor() + "\n"
                + I18n.msg("email") + ": " + Info.getAuthorMail() + "\n"
                + I18n.msg("release") + ": " + Info.getReleaseDate() + ". " + Info.getVersionCodeName() + " - v" + Info.getVersion() + "\n"
                + I18n.msg("licence") + ": " + Info.getLicense() + "\n\n"
                + I18n.msg("find_support_msg") + ":\n"
                + "http://code.google.com/p/freedomotic/" + "\n"
                + "http://freedomotic.com/");
}//GEN-LAST:event_submnuHelpActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {
        closeFreedomotic();
    }

    private void closeFreedomotic() {
        GenericEvent exitSignal = new GenericEvent(this);
        exitSignal.setDestination("app.event.system.exit");
        Freedomotic.sendEvent(exitSignal);
    }

    public Plugin getPlugin() {
        return master;
    }

    private void mnuOpenEnvironmentActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuOpenEnvironmentActionPerformed
        mnuSaveActionPerformed(null);
        final JFileChooser fc = new JFileChooser(Info.getDatafilePath() + "/furn/");
        File file = null;
        OpenDialogFileFilter filter = new OpenDialogFileFilter();
        filter.addExtension("xenv");
        filter.setDescription("Freedomotic XML Environment file");
        fc.addChoosableFileFilter(filter);
        fc.setFileFilter(filter);

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            LOG.info("Opening " + file.getAbsolutePath());

            try {
                boolean loaded = EnvironmentPersistence.loadEnvironmentsFromDir(file.getParentFile(),
                        false);

                if (loaded) {
                    mnuSelectEnvironmentActionPerformed(null);
                }
            } catch (Exception e) {
                LOG.severe(Freedomotic.getStackTraceInfo(e));
            }

            setWindowedMode();
        } else {
            LOG.info(I18n.msg("canceled_by_user"));
        }
    }//GEN-LAST:event_mnuOpenEnvironmentActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt)    {//GEN-FIRST:event_formComponentResized
        optimizeFramesDimension();
    }//GEN-LAST:event_formComponentResized

private void mnuPluginListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPluginListActionPerformed
    if (frameClient.isClosed()) {
        frameClient.show();
    } else {
        frameClient.hide();
    }
}//GEN-LAST:event_mnuPluginListActionPerformed

private void jCheckBoxMarketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMarketActionPerformed
    MarketPlaceForm marketPlace = new MarketPlaceForm(master.getApi());
    marketPlace.setVisible(true);
}//GEN-LAST:event_jCheckBoxMarketActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_jMenuItem3ActionPerformed
        master.getMainWindow().setFullscreenMode();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void mnuRoomEditModeActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuRoomEditModeActionPerformed

        if (mnuRoomEditMode.getState() == true) {
            drawer.setObjectEditMode(false);
            mnuObjectEditMode.setSelected(drawer.getRoomEditMode());
            setEditMode(true);
            drawer.setRoomEditMode(true);
            lstClients.setFilter("Plugin");
            setMapTitle("(" + I18n.msg("room_edit_mode") + ") " + drawer.getCurrEnv().getPojo().getName());
        } else {
            drawer.setRoomEditMode(false);
            setEditMode(false);
            mnuRoomEditMode.setSelected(drawer.getRoomEditMode());
            lstClients.setFilter("Plugin");
            setMapTitle(drawer.getCurrEnv().getPojo().getName());
        }
    }//GEN-LAST:event_mnuRoomEditModeActionPerformed

    private void mnuRenameRoomActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuRenameRoomActionPerformed

        ZoneLogic zone = drawer.getSelectedZone();

        if (zone == null) {
            JOptionPane.showMessageDialog(this,
                    I18n.msg("select_room_first"));
        } else {
            String input = JOptionPane.showInputDialog(I18n.msg("enter_new_name_for_zone") + zone.getPojo().getName());
            zone.getPojo().setName(input.trim());
            drawer.setNeedRepaint(true);
        }
    }//GEN-LAST:event_mnuRenameRoomActionPerformed

    private void mnuAddRoomActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuAddRoomActionPerformed

        Zone z = new Zone();
        z.init();
        z.setName(I18n.msg("room") + Math.random());

        Room room = new Room(z);
        room.getPojo().setTexture((new File(Info.getResourcesPath() + "/wood.jpg")).getName());
        room.init(drawer.getCurrEnv());
        drawer.getCurrEnv().addRoom(room);
        drawer.createHandles(room);
    }//GEN-LAST:event_mnuAddRoomActionPerformed

    private void mnuRemoveRoomActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuRemoveRoomActionPerformed

        ZoneLogic zone = drawer.getSelectedZone();

        if (zone == null) {
            JOptionPane.showMessageDialog(this, I18n.msg("select_room_first"));
        } else {
            drawer.getCurrEnv().removeZone(zone);
            drawer.createHandles(null);
        }
    }//GEN-LAST:event_mnuRemoveRoomActionPerformed

    private void mnuOpenNewActionPerformed(java.awt.event.ActionEvent evt) {
//GEN-FIRST:event_mnuOpenNewActionPerformed
    }//GEN-LAST:event_mnuOpenNewActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuSaveAsActionPerformed

        final JFileChooser fc = new JFileChooser(Info.getDatafilePath() + "/furn/");
        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();

            try {
                EnvironmentPersistence.saveEnvironmentsToFolder(folder);


            } catch (Exception ex) {
                Logger.getLogger(MainWindow.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            LOG.info(I18n.msg("canceled_by_user"));
        }
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void mnuObjectEditModeActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuObjectEditModeActionPerformed

        if (mnuObjectEditMode.getState() == true) {
            //in edit objects mode
            //deactivate room edit mode
            drawer.setRoomEditMode(false);
            mnuRoomEditMode.setSelected(drawer.getRoomEditMode());
            //activate object edit mode
            drawer.setObjectEditMode(true);
            //switch to objects list
            lstClients.setFilter("Object");
            setMapTitle("(" + I18n.msg("object_edit_mode") + "): " + drawer.getCurrEnv().getPojo().getName());
        } else {
            drawer.setObjectEditMode(false);
            mnuObjectEditMode.setSelected(drawer.getObjectEditMode());
            lstClients.setFilter("Plugin");
            setMapTitle(drawer.getCurrEnv().getPojo().getName());
        }
    }//GEN-LAST:event_mnuObjectEditModeActionPerformed

    private void mnuAutomationsActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuAutomationsActionPerformed

        Command c = new Command();
        c.setName("Popup Automation Editor Gui");
        c.setReceiver("app.actuators.plugins.controller.in");
        c.setProperty("plugin", "Automations Editor");
        c.setProperty("action", "show"); //the default choice

        Freedomotic.sendCommand(c);
    }//GEN-LAST:event_mnuAutomationsActionPerformed

    private void mnuChangeRendererActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuChangeRendererActionPerformed

        Object[] possibilities = {"list", "plain", "image", "photo"};
        String input = (String) JOptionPane.showInputDialog(
                this,
                I18n.msg("select_renderer"),
                I18n.msg("select_renderer_title"),
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                drawer.getCurrEnv().getPojo().getRenderer());

        //If a string was returned
        if ((input != null) && (!input.isEmpty())) {
            changeRenderer(input);
        }
    }//GEN-LAST:event_mnuChangeRendererActionPerformed

    private void mnuBackgroundActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuBackgroundActionPerformed

        final JFileChooser fc = new JFileChooser(Info.getDatafilePath() + "/resources/");
        OpenDialogFileFilter filter = new OpenDialogFileFilter();
        filter.addExtension("png");
        filter.addExtension("jpeg");
        filter.addExtension("jpg");
        filter.setDescription("Image files (png, jpeg)");
        fc.addChoosableFileFilter(filter);
        fc.setFileFilter(filter);

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            //This is where a real application would open the file.
            LOG.info("Opening " + file.getAbsolutePath());
            drawer.getCurrEnv().getPojo().setBackgroundImage(file.getName());
            drawer.setNeedRepaint(true);
            frameMap.validate();
        }
    }//GEN-LAST:event_mnuBackgroundActionPerformed

    private void mnuRoomBackgroundActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuRoomBackgroundActionPerformed

        ZoneLogic zone = drawer.getSelectedZone();

        if (zone == null) {
            JOptionPane.showMessageDialog(this,
                    I18n.msg("select_room_first"));
        } else {
            final JFileChooser fc = new JFileChooser(Info.getDatafilePath() + "/resources/");
            OpenDialogFileFilter filter = new OpenDialogFileFilter();
            filter.addExtension("png");
            filter.addExtension("jpeg");
            filter.addExtension("jpg");
            filter.setDescription("Image files (png, jpeg)");
            fc.addChoosableFileFilter(filter);
            fc.setFileFilter(filter);

            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                LOG.info("Opening " + file.getAbsolutePath());
                zone.getPojo().setTexture(file.getName());
                drawer.setNeedRepaint(true);
                frameMap.validate();
            }
        }
    }//GEN-LAST:event_mnuRoomBackgroundActionPerformed

    private void mnuNewEnvironmentActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuNewEnvironmentActionPerformed

        mnuSaveActionPerformed(null);
        File oldEnv = EnvironmentPersistence.getEnvironments().get(0).getSource();

        //creates a new environment coping it from a template
        File template =
                new File(Info.getApplicationPath() + "/data/furn/templates/template-square/template-square.xenv");
        LOG.info("Opening " + template.getAbsolutePath());
        drawer.setCurrEnv(0);

        try {
            boolean loaded = EnvironmentPersistence.loadEnvironmentsFromDir(template.getParentFile(),
                    false);

            if (loaded) {
                //EnvObjectPersistence.loadObjects(EnvironmentPersistence.getEnvironments().get(0).getObjectFolder(), false);
                final JFileChooser fc = new JFileChooser(Info.getDatafilePath() + "/furn/");
                int returnVal = fc.showSaveDialog(this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File folder = fc.getSelectedFile();

                    if (!folder.getName().isEmpty()) {
                        EnvironmentLogic newenv = EnvironmentPersistence.getEnvironments().get(0);
                        newenv.setSource(
                                new File(folder + "/" + newenv.getPojo().getUUID() + ".xenv"));
                        setEnvironment(newenv);
                        //save the new environment
                        EnvironmentPersistence.saveAs(newenv, folder);
                    }
                } else {
                    Freedomotic.logger.info("Save command cancelled by user.");
                    //reload the old file
                    EnvironmentPersistence.loadEnvironmentsFromDir(oldEnv.getParentFile(),
                            false);

                    setEnvironment(EnvironmentPersistence.getEnvironments().get(0));
                }
            }
        } catch (Exception e) {
            LOG.severe(Freedomotic.getStackTraceInfo(e));
        }

        setWindowedMode();
    }//GEN-LAST:event_mnuNewEnvironmentActionPerformed

    private void mnuSaveActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuSaveActionPerformed

        String environmentFilePath =
                Info.getApplicationPath() + "/data/furn" + master.getApi().getConfig().getProperty("KEY_ROOM_XML_PATH");

        try {
            EnvironmentPersistence.saveEnvironmentsToFolder(new File(environmentFilePath).getParentFile());
        } catch (DaoLayerException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot save environment at "
                    + new File(environmentFilePath).getAbsolutePath());
        }
    }//GEN-LAST:event_mnuSaveActionPerformed

    private void mnuPluginConfigureActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuPluginConfigureActionPerformed
        new PluginConfigure(master.getApi());
    }//GEN-LAST:event_mnuPluginConfigureActionPerformed

    private void mnuTutorialActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_mnuTutorialActionPerformed
        new TipOfTheDay(master);
    }//GEN-LAST:event_mnuTutorialActionPerformed

    private void mnuSelectEnvironmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSelectEnvironmentActionPerformed
        if (Auth.isPermitted("environments:read")) {

            if (master.getApi().getEnvironments().size() == 1) {
                drawer.setCurrEnv(0);
                setMapTitle(master.getApi().getEnvironments().get(0).getPojo().getName());
            } else {
                Object[] possibilities = EnvironmentPersistence.getEnvironments().toArray();
                EnvironmentLogic input = (EnvironmentLogic) JOptionPane.showInputDialog(
                        this,
                        I18n.msg("select_env"),
                        I18n.msg("select_env_title"),
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        possibilities,
                        drawer.getCurrEnv());

                //If a string was returned
                if (input != null) {
                    setEnvironment(input);
                } else {
                    setEnvironment(EnvironmentPersistence.getEnvironments().get(0));
                }
            }
        }
    }//GEN-LAST:event_mnuSelectEnvironmentActionPerformed

    private void mnuAddDuplicateEnvironmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAddDuplicateEnvironmentActionPerformed
        EnvironmentLogic newEnv = EnvironmentPersistence.add(drawer.getCurrEnv(), true);
        String input = JOptionPane.showInputDialog(I18n.msg("enter_new_name_for_env") + newEnv.getPojo().getName());
        newEnv.getPojo().setName(input.trim());
        newEnv.setSource(new File(drawer.getCurrEnv().getSource().getParentFile() + "/" + newEnv.getPojo().getUUID() + ".xenv"));
        setEnvironment(EnvironmentPersistence.getEnvByUUID(newEnv.getPojo().getUUID()));
        checkDeletableEnvironments();
    }//GEN-LAST:event_mnuAddDuplicateEnvironmentActionPerformed

    private void mnuRenameEnvironmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRenameEnvironmentActionPerformed
        String input = JOptionPane.showInputDialog(I18n.msg("enter_new_name_for_env"), drawer.getCurrEnv().getPojo().getName());
        drawer.getCurrEnv().getPojo().setName(input.trim());
        setMapTitle(drawer.getCurrEnv().getPojo().getName());
    }//GEN-LAST:event_mnuRenameEnvironmentActionPerformed

    private void mnuDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDeleteActionPerformed
        int result = JOptionPane.showConfirmDialog(null,
                I18n.msg("confirm_env_delete"),
                I18n.msg("confirm_deletion_title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            EnvironmentLogic oldenv = drawer.getCurrEnv();

            if (EnvironmentPersistence.getEnvironments().get(0) != drawer.getCurrEnv()) {
                setEnvironment(EnvironmentPersistence.getEnvironments().get(0));
            } else {
                setEnvironment(EnvironmentPersistence.getEnvironments().get(1));
            }

            EnvironmentPersistence.remove(oldenv);
            setWindowedMode();
            checkDeletableEnvironments();
        }
    }//GEN-LAST:event_mnuDeleteActionPerformed
    private void mnuSwitchUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSwitchUserActionPerformed
        logUser();
    }//GEN-LAST:event_mnuSwitchUserActionPerformed

    private void mnuPrivilegesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPrivilegesActionPerformed
        // TODO add your handling code here:
        new PrivilegesConfiguration(master.getApi());
    }//GEN-LAST:event_mnuPrivilegesActionPerformed

    private void mnuLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLanguageActionPerformed
        //JDK 1,7 version: JComboBox<i18n.ComboLanguage> combo = new JComboBox<i18n.ComboLanguage>(I18n.getAvailableLocales());
        //JDK 1.6 version: next line
        JComboBox combo = new JComboBox(I18n.getAvailableLocales());
        for (ComboLanguage cmb : I18n.getAvailableLocales()) {
            if (cmb.getValue().equals(I18n.getDefaultLocale())) {
                combo.setSelectedItem(cmb);
                break;
            }
        }
        JLabel lbl = new JLabel(I18n.msg("language"));
        int result = JOptionPane.showConfirmDialog(
                this,
                new Object[]{lbl, combo},
                I18n.msg("language"),
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            ComboLanguage selected = (ComboLanguage) combo.getSelectedItem();
            I18n.setDefaultLocale(selected.getValue());
            updateStrings();
        }
    }//GEN-LAST:event_mnuLanguageActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    }//GEN-LAST:event_formWindowClosed

    private void updateStrings() {
        mnuOpenNew.setText(I18n.msg("file"));
        mnuNewEnvironment.setText(I18n.msg("new") + I18n.msg("environment"));
        mnuOpenEnvironment.setText(I18n.msg("open") + I18n.msg("environment"));
        mnuSave.setText(I18n.msg("save") + I18n.msg("environment"));
        mnuSaveAs.setText(I18n.msg("save_X_as", new Object[]{I18n.msg("environment")}));
        mnuSwitchUser.setText(I18n.msg("change_user"));
        mnuExit.setText(I18n.msg("exit"));
        mnuEditMode.setText(I18n.msg("environment"));
        mnuSelectEnvironment.setText(I18n.msg("select_X", new Object[]{I18n.msg("area_floor")}));
        jMenu4.setText(I18n.msg("area_floor"));
        mnuRenameEnvironment.setText(I18n.msg("rename"));
        mnuAddDuplicateEnvironment.setText(I18n.msg("add") + "/" + I18n.msg("duplicate"));
        mnuChangeRenderer.setText(I18n.msg("change_X", new Object[]{I18n.msg("renderer")}));
        mnuBackground.setText(I18n.msg("change_X", new Object[]{I18n.msg("background")}));
        mnuDelete.setText(I18n.msg("delete"));
        mnuRoomEditMode.setText(I18n.msg("X_edit_mode", new Object[]{I18n.msg("rooms")}));
        jMenu3.setText(I18n.msg("rooms"));
        mnuRenameRoom.setText(I18n.msg("rename") + I18n.msg("room"));
        mnuAddRoom.setText(I18n.msg("add") + I18n.msg("room"));
        mnuRoomBackground.setText(I18n.msg("change_X", new Object[]{I18n.msg("background")}));
        mnuRemoveRoom.setText(I18n.msg("remove") + I18n.msg("room"));
        mnuObjects.setText(I18n.msg("objects"));
        mnuObjectEditMode.setText(I18n.msg("X_edit_mode", new Object[]{I18n.msg("objects")}));
        jMenu2.setText(I18n.msg("automations"));
        mnuAutomations.setText(I18n.msg("manage") + I18n.msg("automations"));
        jCheckBoxMarket.setText(I18n.msg("install_from_marketplace"));
        mnuPluginConfigure.setText(I18n.msg("configure"));
        jMenu5.setText(I18n.msg("settings"));
        mnuLanguage.setText(I18n.msg("language"));
        mnuPrivileges.setText(I18n.msg("privileges"));
        mnuWindow.setText(I18n.msg("window"));
        mnuPluginList.setText(I18n.msg("X_list", new Object[]{I18n.msg("plugins")}));
        jMenuItem3.setText(I18n.msg("fullscreen"));
        mnuHelp.setText(I18n.msg("help"));
        mnuTutorial.setText(I18n.msg("tutorial"));
        submnuHelp.setText(I18n.msg("about"));
        frameClient.setTitle(I18n.msg("loaded_plugins"));
        setMapTitle(drawer.getCurrEnv().getPojo().getName());

        // frameMap
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCheckBoxMarket;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem mnuAddDuplicateEnvironment;
    private javax.swing.JMenuItem mnuAddRoom;
    private javax.swing.JMenuItem mnuAutomations;
    private javax.swing.JMenuItem mnuBackground;
    private javax.swing.JMenuItem mnuChangeRenderer;
    private javax.swing.JMenuItem mnuDelete;
    private javax.swing.JMenu mnuEditMode;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuLanguage;
    private javax.swing.JMenuItem mnuNewEnvironment;
    private javax.swing.JCheckBoxMenuItem mnuObjectEditMode;
    private javax.swing.JMenu mnuObjects;
    private javax.swing.JMenuItem mnuOpenEnvironment;
    private javax.swing.JMenu mnuOpenNew;
    private javax.swing.JMenuItem mnuPluginConfigure;
    private javax.swing.JMenuItem mnuPluginList;
    private javax.swing.JMenuItem mnuPrivileges;
    private javax.swing.JMenuItem mnuRemoveRoom;
    private javax.swing.JMenuItem mnuRenameEnvironment;
    private javax.swing.JMenuItem mnuRenameRoom;
    private javax.swing.JMenuItem mnuRoomBackground;
    private javax.swing.JCheckBoxMenuItem mnuRoomEditMode;
    private javax.swing.JMenuItem mnuSave;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuSelectEnvironment;
    private javax.swing.JMenuItem mnuSwitchUser;
    private javax.swing.JMenuItem mnuTutorial;
    private javax.swing.JMenu mnuWindow;
    private javax.swing.JScrollPane scrollTxtOut1;
    private javax.swing.JScrollPane scrollTxtOut2;
    private javax.swing.JMenuItem submnuHelp;
    private javax.swing.JTextArea txtOut1;
    private javax.swing.JTextArea txtOut2;
    // End of variables declaration//GEN-END:variables
}
