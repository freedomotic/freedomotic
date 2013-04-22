//Copyright 2009 Enrico Nicoletti
//eMail: enrico.nicoletti84@gmail.com
//
//This file is part of EventEngine.
//
//EventEngine is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//EventEngine is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with EventEngine; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
package it.freedomotic.frontend;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.core.ResourcesManager;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.environment.Room;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.environment.EnvironmentPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.util.Info;
import it.freedomotic.frontend.utils.OpenDialogFileFilter;
import it.freedomotic.frontend.utils.TipOfTheDay;
import it.freedomotic.objects.EnvObjectLogic;
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
public class MainWindow extends javax.swing.JFrame {

    private Drawer drawer;
    private float referenceRatio;
    private static boolean isFullscreen = false;
    private JavaDesktopFrontend master;
    JDesktopPane desktopPane;
    JInternalFrame frameClient;
    JInternalFrame frameMap;
    PluginJList lstClients;
    //JComboBox cmbFilter;
    boolean editMode;

    public Drawer getDrawer() {
        return drawer;
    }

    public MainWindow(final JavaDesktopFrontend master) {
        this.master = master;
        setWindowedMode();
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
        if (master.configuration.getBooleanProperty("show.tips", true)) {
            new TipOfTheDay(this);
        }
    }

//    public void showTipsOnStartup(boolean show) {
//        master.configuration.setProperty("show.tips", new Boolean(show).toString());
//    }
    private class MyDispatcher implements KeyEventDispatcher {

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
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
        } catch (InstantiationException ex) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
        } catch (IllegalAccessException ex) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
        } catch (UnsupportedLookAndFeelException ex) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
        }

        setDefaultLookAndFeelDecorated(true);
        initComponents();
        setLayout(new BorderLayout());
        desktopPane = new JDesktopPane();
        lstClients = new PluginJList();
        frameClient = new JInternalFrame();
        frameClient.setLayout(new BorderLayout());
        JScrollPane clientScroll = new JScrollPane(lstClients);
        frameClient.add(clientScroll, BorderLayout.CENTER);
        frameClient.setTitle("Loaded Plugins");
        frameClient.setResizable(true);
        frameClient.setMaximizable(true);

        frameMap = new JInternalFrame();
        frameMap.setTitle("Environment");
        frameMap.setMaximizable(true);
        frameMap.setResizable(true);
        desktopPane.add(frameMap);
        desktopPane.add(frameClient);
        desktopPane.moveToFront(this);
        this.getContentPane().add(desktopPane);
        try {
            frameClient.setSelected(true);
        } catch (PropertyVetoException ex) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
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
            Freedomotic.logger.severe("Unable to create a drawer to render the environment on the desktop frontend");
        }
        this.setTitle("Freedomotic " + Info.getLicense() + " - www.freedomotic.com");
        this.setSize(1100, 700);
        centerFrame(this);
        frameClient.moveToFront();
        frameMap.moveToFront();
        optimizeFramesDimension();
        drawer.repaint();
        lstClients.update();
        frameClient.setVisible(true);
        frameMap.setVisible(true);
        setEditMode(false);
        this.setVisible(true);
        isFullscreen = false;
    }

    private void setFullscreenMode() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            frameMap.setVisible(false);
            frameClient.setVisible(false);
            menuBar.setVisible(false);
            frameMap.dispose();
            frameClient.dispose();
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
                Freedomotic.logger.severe("Unable to create a drawer to render the environment on the desktop frontend in fullscreen mode");
            }
            gd.setFullScreenWindow(this);
            add(drawer);
            drawer.repaint();
            drawer.setVisible(true);
            Callout callout = new Callout(this.getClass().getCanonicalName(), "info", "Press ESC to exit fullscreen mode", 100, 100, 0, 5000);
            drawer.createCallout(callout);
            this.repaint();
            this.setVisible(true);
            isFullscreen = true;
        }
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
        frame.setLocation(
                (Toolkit.getDefaultToolkit().getScreenSize().width - frame.getWidth()) / 2,
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
                frameClient.setBounds(0, 0, desktopPane.getWidth() / 3, desktopPane.getHeight());
                frameMap.setBounds(desktopPane.getWidth() / 3, 0, (desktopPane.getWidth() / 3) * 2, desktopPane.getHeight());
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
        setMapTitle("Not inited");
        desktopPane.add(frameMap, javax.swing.JLayeredPane.DEFAULT_LAYER);
        desktopPane.setBackground(Renderer.BACKGROUND_COLOR);
        referenceRatio = new Float(prevEnv.getPojo().getWidth() / new Float(prevEnv.getPojo().getWidth()));
        frameMap.getContentPane().setBackground(Renderer.BACKGROUND_COLOR);
    }

    public void setDrawer(Drawer drawer) {
        frameMap.getContentPane().add(drawer);
        setMapTitle(drawer.getCurrEnv().getPojo().getName());
    }

    public void setMapTitle(String name) {
        frameMap.setTitle("Environment - " + name);
    }

    class StringListModel extends AbstractListModel {

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
        menuBar = new javax.swing.JMenuBar();
        mnuOpenNew = new javax.swing.JMenu();
        mnuNewEnvironment = new javax.swing.JMenuItem();
        mnuOpenEnvironment = new javax.swing.JMenuItem();
        mnuSave = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
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
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        mnuPluginConfigure = new javax.swing.JMenuItem();
        mnuWindow = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuTutorial = new javax.swing.JMenuItem();
        submnuHelp = new javax.swing.JMenuItem();

        jTextField1.setText("jTextField1");

        scrollTxtOut1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtOut1.setColumns(20);
        txtOut1.setEditable(false);
        txtOut1.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        txtOut1.setRows(5);
        txtOut1.setWrapStyleWord(true);
        txtOut1.setName("txtOutput"); // NOI18N
        txtOut1.setOpaque(false);
        scrollTxtOut1.setViewportView(txtOut1);

        scrollTxtOut2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtOut2.setColumns(20);
        txtOut2.setEditable(false);
        txtOut2.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        txtOut2.setRows(5);
        txtOut2.setWrapStyleWord(true);
        txtOut2.setName("txtOutput"); // NOI18N
        txtOut2.setOpaque(false);
        scrollTxtOut2.setViewportView(txtOut2);

        jMenuItem4.setText("jMenuItem4");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Freedomotic");
        setBackground(java.awt.SystemColor.window);
        setBounds(new java.awt.Rectangle(50, 20, 0, 0));
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(500, 400));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
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

        mnuOpenNew.setText("File");
        mnuOpenNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenNewActionPerformed(evt);
            }
        });

        mnuNewEnvironment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        mnuNewEnvironment.setText("New Environment");
        mnuNewEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuNewEnvironmentActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuNewEnvironment);

        mnuOpenEnvironment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        mnuOpenEnvironment.setText("Open Environment");
        mnuOpenEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenEnvironmentActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuOpenEnvironment);

        mnuSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        mnuSave.setText("Save Environment");
        mnuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuSave);

        mnuSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuSaveAs.setText("Save Environment As...");
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuSaveAs);
        mnuOpenNew.add(jSeparator1);

        mnuExit.setText("Exit");
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        mnuOpenNew.add(mnuExit);

        menuBar.add(mnuOpenNew);

        mnuEditMode.setText("Environment");

        mnuSelectEnvironment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        mnuSelectEnvironment.setText("Select Area/Floor");
        mnuSelectEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSelectEnvironmentActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuSelectEnvironment);

        jMenu4.setText("Area/Floor");

        mnuRenameEnvironment.setText("Rename");
        mnuRenameEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRenameEnvironmentActionPerformed(evt);
            }
        });
        jMenu4.add(mnuRenameEnvironment);

        mnuAddDuplicateEnvironment.setText("Add/Duplicate");
        mnuAddDuplicateEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAddDuplicateEnvironmentActionPerformed(evt);
            }
        });
        jMenu4.add(mnuAddDuplicateEnvironment);

        mnuChangeRenderer.setText("Change Renderer");
        mnuChangeRenderer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuChangeRendererActionPerformed(evt);
            }
        });
        jMenu4.add(mnuChangeRenderer);

        mnuBackground.setText("Change Background");
        mnuBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuBackgroundActionPerformed(evt);
            }
        });
        jMenu4.add(mnuBackground);

        mnuDelete.setText("Delete");
        mnuDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuDeleteActionPerformed(evt);
            }
        });
        jMenu4.add(mnuDelete);

        mnuEditMode.add(jMenu4);

        mnuRoomEditMode.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        mnuRoomEditMode.setText("Rooms Edit Mode");
        mnuRoomEditMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRoomEditModeActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuRoomEditMode);

        jMenu3.setText("Rooms");

        mnuRenameRoom.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        mnuRenameRoom.setText("Rename Room");
        mnuRenameRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRenameRoomActionPerformed(evt);
            }
        });
        jMenu3.add(mnuRenameRoom);

        mnuAddRoom.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        mnuAddRoom.setText("Add Room");
        mnuAddRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAddRoomActionPerformed(evt);
            }
        });
        jMenu3.add(mnuAddRoom);

        mnuRoomBackground.setText("Change Background");
        mnuRoomBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRoomBackgroundActionPerformed(evt);
            }
        });
        jMenu3.add(mnuRoomBackground);

        mnuRemoveRoom.setText("Remove Room");
        mnuRemoveRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRemoveRoomActionPerformed(evt);
            }
        });
        jMenu3.add(mnuRemoveRoom);

        mnuEditMode.add(jMenu3);

        menuBar.add(mnuEditMode);

        mnuObjects.setText("Objects");

        mnuObjectEditMode.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        mnuObjectEditMode.setText("Objects Edit Mode");
        mnuObjectEditMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuObjectEditModeActionPerformed(evt);
            }
        });
        mnuObjects.add(mnuObjectEditMode);

        menuBar.add(mnuObjects);

        jMenu2.setText("Automations");

        mnuAutomations.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
        mnuAutomations.setText("Manage Automations");
        mnuAutomations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAutomationsActionPerformed(evt);
            }
        });
        jMenu2.add(mnuAutomations);

        menuBar.add(jMenu2);

        jMenu1.setText("Plugins");

        jCheckBoxMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0));
        jCheckBoxMenuItem1.setText("Install from Marketplace");
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jCheckBoxMenuItem1);

        mnuPluginConfigure.setText("Configure");
        mnuPluginConfigure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPluginConfigureActionPerformed(evt);
            }
        });
        jMenu1.add(mnuPluginConfigure);

        menuBar.add(jMenu1);

        mnuWindow.setText("Window");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        jMenuItem2.setText("Plugins List");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        mnuWindow.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        jMenuItem3.setText("Fullscreen");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        mnuWindow.add(jMenuItem3);

        menuBar.add(mnuWindow);

        mnuHelp.setText("Help");

        mnuTutorial.setText("Tutorial");
        mnuTutorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTutorialActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuTutorial);

        submnuHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        submnuHelp.setText("About");
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
        Freedomotic.onExit();
    }//GEN-LAST:event_formWindowClosing

    private void submnuHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submnuHelpActionPerformed

        JOptionPane.showMessageDialog(this, ""
                + "Author: " + Info.getAuthor() + "\n"
                + "E-mail: " + Info.getAuthorMail() + "\n"
                + "Release: " + Info.getReleaseDate() + ". " + Info.getVersionCodeName() + " - v" + Info.getVersion() + "\n"
                + "Licence: " + Info.getLicense() + "\n\n"
                + "You can find support on:\n"
                + "http://code.google.com/p/freedomotic/" + "\n"
                + "http://freedomotic.com/");
}//GEN-LAST:event_submnuHelpActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {
        Freedomotic.onExit();
    }

    private void mnuOpenEnvironmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenEnvironmentActionPerformed
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
            Freedomotic.logger.info("Opening " + file.getAbsolutePath());
            try {
                boolean loaded = EnvironmentPersistence.loadEnvironmentsFromDir(file.getParentFile(), false);
                if (loaded) {
                    for (EnvironmentLogic env : EnvironmentPersistence.getEnvironments()) {
                        //EnvObjectPersistence.loadObjects(env.getObjectFolder(), false);
                        Freedomotic.config.setProperty("KEY_ROOM_XML_PATH",
                                env.getSource().toString().replace(
                                new File(Info.getApplicationPath() + "/data/furn").toString(), ""));
                    }
                }
            } catch (Exception e) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
            }
            setWindowedMode();
        } else {
            Freedomotic.logger.info("Open command cancelled by user.");
        }
}//GEN-LAST:event_mnuOpenEnvironmentActionPerformed

private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
    optimizeFramesDimension();
}//GEN-LAST:event_formComponentResized

private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
    if (frameClient.isClosed()) {
        frameClient.show();
    } else {
        frameClient.hide();
    }
}//GEN-LAST:event_jMenuItem2ActionPerformed

private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1ActionPerformed
    MarketPlaceForm marketPlace = new MarketPlaceForm();
    marketPlace.setVisible(true);
}//GEN-LAST:event_jCheckBoxMenuItem1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        master.getMainWindow().setFullscreenMode();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void mnuRoomEditModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRoomEditModeActionPerformed
        if (mnuRoomEditMode.getState() == true) {
            drawer.setObjectEditMode(false);
            mnuObjectEditMode.setSelected(drawer.getRoomEditMode());
            setEditMode(true);
            drawer.setRoomEditMode(true);
            lstClients.setFilter("Plugin");
            setMapTitle("(EDIT ROOMS MODE) " + drawer.getCurrEnv().getPojo().getName());
        } else {
            drawer.setRoomEditMode(false);
            setEditMode(false);
            mnuRoomEditMode.setSelected(drawer.getRoomEditMode());
            lstClients.setFilter("Plugin");
            setMapTitle(drawer.getCurrEnv().getPojo().getName());
        }
    }//GEN-LAST:event_mnuRoomEditModeActionPerformed

    private void mnuRenameRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRenameRoomActionPerformed
        ZoneLogic zone = drawer.getSelectedZone();
        if (zone == null) {
            JOptionPane.showMessageDialog(this, "Select a room first");
        } else {
            String input = JOptionPane.showInputDialog("Enter here the new name for zone " + zone.getPojo().getName());
            zone.getPojo().setName(input.trim());
            drawer.setNeedRepaint(true);
        }
    }//GEN-LAST:event_mnuRenameRoomActionPerformed

    private void mnuAddRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAddRoomActionPerformed
        Zone z = new Zone();
        z.init();
        z.setName("NewRoom" + Math.random());
        Room room = new Room(z);
        room.getPojo().setTexture((new File(Info.getResourcesPath() + "/wood.jpg")).getName());
        room.init(drawer.getCurrEnv());
        drawer.getCurrEnv().addRoom(room);
        drawer.createHandles(room);
    }//GEN-LAST:event_mnuAddRoomActionPerformed

    private void mnuRemoveRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRemoveRoomActionPerformed
        ZoneLogic zone = drawer.getSelectedZone();
        if (zone == null) {
            JOptionPane.showMessageDialog(this, "Select a room first");
        } else {
            drawer.getCurrEnv().removeZone(zone);
            drawer.createHandles(null);
        }
    }//GEN-LAST:event_mnuRemoveRoomActionPerformed

    private void mnuOpenNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenNewActionPerformed
    }//GEN-LAST:event_mnuOpenNewActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
        final JFileChooser fc = new JFileChooser(Info.getDatafilePath() + "/furn/");
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            try {
                EnvironmentPersistence.saveEnvironmentsToFolder(folder);
            } catch (Exception ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Freedomotic.logger.info("Save command cancelled by user.");
        }
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void mnuObjectEditModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuObjectEditModeActionPerformed
        if (mnuObjectEditMode.getState() == true) {
            //in edit objects mode
            //deactivate room edit mode
            drawer.setRoomEditMode(false);
            mnuRoomEditMode.setSelected(drawer.getRoomEditMode());
            //activate object edit mode
            drawer.setObjectEditMode(true);
            //switch to objects list
            lstClients.setFilter("Object");
            setMapTitle("(EDIT OBJECTS MODE): " + drawer.getCurrEnv().getPojo().getName());
        } else {
            drawer.setObjectEditMode(false);
            mnuObjectEditMode.setSelected(drawer.getObjectEditMode());
            lstClients.setFilter("Plugin");
            setMapTitle(drawer.getCurrEnv().getPojo().getName());
        }
    }//GEN-LAST:event_mnuObjectEditModeActionPerformed

    private void mnuAutomationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAutomationsActionPerformed
        Command c = new Command();
        c.setName("Popup Automation Editor Gui");
        c.setReceiver("app.actuators.plugins.controller.in");
        c.setProperty("plugin", "Automations Editor");
        c.setProperty("action", "show"); //the default choice
        Command reply = Freedomotic.sendCommand(c);
    }//GEN-LAST:event_mnuAutomationsActionPerformed

    private void mnuChangeRendererActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuChangeRendererActionPerformed
        Object[] possibilities = {"list", "plain", "image", "photo"};
        String input = (String) JOptionPane.showInputDialog(
                this,
                "Select a renderer for the map",
                "Choose map renderer",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                drawer.getCurrEnv().getPojo().getRenderer());

        //If a string was returned
        if ((input != null) && (input.length() > 0)) {
            changeRenderer(input);
        }

    }//GEN-LAST:event_mnuChangeRendererActionPerformed

    private void mnuBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuBackgroundActionPerformed
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
            Freedomotic.logger.info("Opening " + file.getAbsolutePath());
            drawer.getCurrEnv().getPojo().setBackgroundImage(file.getName());
            drawer.setNeedRepaint(true);
            frameMap.validate();
        }
    }//GEN-LAST:event_mnuBackgroundActionPerformed

    private void mnuRoomBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRoomBackgroundActionPerformed
        ZoneLogic zone = drawer.getSelectedZone();
        if (zone == null) {
            JOptionPane.showMessageDialog(this, "Select a room first");
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
                Freedomotic.logger.info("Opening " + file.getAbsolutePath());
                zone.getPojo().setTexture(file.getName());
                drawer.setNeedRepaint(true);
                frameMap.validate();
            }
        }
    }//GEN-LAST:event_mnuRoomBackgroundActionPerformed

    private void mnuNewEnvironmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuNewEnvironmentActionPerformed
        File oldEnv = EnvironmentPersistence.getEnvironments().get(0).getSource();
        File template = new File(Info.getApplicationPath() + "/data/furn/templates/template-square/template-square.xenv");
        Freedomotic.logger.info("Opening " + template.getAbsolutePath());
        drawer.setCurrEnv(0);
        try {
            boolean loaded = EnvironmentPersistence.loadEnvironmentsFromDir(template.getParentFile(), false);
            if (loaded) {
                //EnvObjectPersistence.loadObjects(EnvironmentPersistence.getEnvironments().get(0).getObjectFolder(), false);
                final JFileChooser fc = new JFileChooser(Info.getDatafilePath() + "/furn/");
                int returnVal = fc.showSaveDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File folder = fc.getSelectedFile();
                    if (!folder.getName().isEmpty()) {
                        try {
                            EnvironmentPersistence.saveAs(drawer.getCurrEnv(), folder);
                            Freedomotic.config.setProperty("KEY_ROOM_XML_PATH", folder.getName() + "/" + folder.getName() + ".xenv");
                            drawer.getCurrEnv().setSource(new File(folder + "/" + folder.getName() + ".xenv"));
                        } catch (IOException ex) {
                            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else {
                    Freedomotic.logger.info("Save command cancelled by user.");
                    EnvironmentPersistence.loadEnvironmentsFromDir(oldEnv.getParentFile(), false);
                    Freedomotic.config.setProperty("KEY_ROOM_XML_PATH",
                                oldEnv.getAbsolutePath().toString().replace(
                                new File(Info.getApplicationPath() + "/data/furn").toString(), ""));
                    drawer.setCurrEnv(0);        
                }
            }
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
        setWindowedMode();
    }//GEN-LAST:event_mnuNewEnvironmentActionPerformed

    private void mnuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveActionPerformed
        String environmentFilePath = Info.getApplicationPath() + "/data/furn/" + Freedomotic.config.getProperty("KEY_ROOM_XML_PATH");
        EnvironmentPersistence.saveEnvironmentsToFolder(new File(environmentFilePath).getParentFile());
    }//GEN-LAST:event_mnuSaveActionPerformed

    private void mnuPluginConfigureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPluginConfigureActionPerformed
        new PluginConfigure();
    }//GEN-LAST:event_mnuPluginConfigureActionPerformed

    private void mnuTutorialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuTutorialActionPerformed
        new TipOfTheDay(this);
    }//GEN-LAST:event_mnuTutorialActionPerformed

    private void mnuSelectEnvironmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSelectEnvironmentActionPerformed
        Object[] possibilities = EnvironmentPersistence.getEnvironments().toArray();
        EnvironmentLogic input = (EnvironmentLogic) JOptionPane.showInputDialog(
                this,
                "Please select an environment",
                "Choose environment",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                drawer.getCurrEnv());

        //If a string was returned
        if (input != null) {
            drawer.setCurrEnv(input);
        }
    }//GEN-LAST:event_mnuSelectEnvironmentActionPerformed

    private void mnuAddDuplicateEnvironmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAddDuplicateEnvironmentActionPerformed
        EnvironmentLogic newEnv = EnvironmentPersistence.add(drawer.getCurrEnv(), true);
        String input = JOptionPane.showInputDialog("Enter here the new name for Area/Floor " + newEnv.getPojo().getName());
        newEnv.getPojo().setName(input.trim());
        drawer.setCurrEnv(newEnv);
        setMapTitle(newEnv.getPojo().getName());

    }//GEN-LAST:event_mnuAddDuplicateEnvironmentActionPerformed

    private void mnuRenameEnvironmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRenameEnvironmentActionPerformed
        String input = JOptionPane.showInputDialog("Enter here the new name for Area/Floor", drawer.getCurrEnv().getPojo().getName());
        drawer.getCurrEnv().getPojo().setName(input.trim());
        setMapTitle(drawer.getCurrEnv().getPojo().getName());
    }//GEN-LAST:event_mnuRenameEnvironmentActionPerformed

    private void mnuDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDeleteActionPerformed
        int result = JOptionPane.showConfirmDialog(null, "You're about to delete the current environment", "Confirm deletion?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            EnvironmentLogic oldenv = drawer.getCurrEnv();
            drawer.setCurrEnv(0);


            EnvironmentPersistence.remove(oldenv);

        }
    }//GEN-LAST:event_mnuDeleteActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JSeparator jSeparator1;
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
    private javax.swing.JMenuItem mnuNewEnvironment;
    private javax.swing.JCheckBoxMenuItem mnuObjectEditMode;
    private javax.swing.JMenu mnuObjects;
    private javax.swing.JMenuItem mnuOpenEnvironment;
    private javax.swing.JMenu mnuOpenNew;
    private javax.swing.JMenuItem mnuPluginConfigure;
    private javax.swing.JMenuItem mnuRemoveRoom;
    private javax.swing.JMenuItem mnuRenameEnvironment;
    private javax.swing.JMenuItem mnuRenameRoom;
    private javax.swing.JMenuItem mnuRoomBackground;
    private javax.swing.JCheckBoxMenuItem mnuRoomEditMode;
    private javax.swing.JMenuItem mnuSave;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuSelectEnvironment;
    private javax.swing.JMenuItem mnuTutorial;
    private javax.swing.JMenu mnuWindow;
    private javax.swing.JScrollPane scrollTxtOut1;
    private javax.swing.JScrollPane scrollTxtOut2;
    private javax.swing.JMenuItem submnuHelp;
    private javax.swing.JTextArea txtOut1;
    private javax.swing.JTextArea txtOut2;
    // End of variables declaration//GEN-END:variables
}
