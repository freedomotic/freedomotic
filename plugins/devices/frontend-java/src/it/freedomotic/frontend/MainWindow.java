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

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.environment.Room;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.persistence.EnvironmentPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.util.OpenDialogFileFilter;
import it.freedomotic.util.Info;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class MainWindow extends javax.swing.JFrame {
    
    private Renderer drawer;
    private float referenceRatio;
    private static boolean isFullscreen = false;
    private JavaDesktopFrontend master;
    JDesktopPane desktopPane;
    JInternalFrame frameClient;
    JInternalFrame frameMap;
    PluginJList lstClients;
    JComboBox cmbFilter;
    boolean editMode;
    
    public Renderer getDrawer() {
        return (Renderer) drawer;
    }
    
    public MainWindow(final JavaDesktopFrontend master) {
        this.master = master;
        setWindowedMode();
        
        
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
        
    }
    
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
        //add a filter combobox
        cmbFilter = new JComboBox();
        cmbFilter.addItem(new String("Plugin"));
        cmbFilter.addItem(new String("Client"));
        cmbFilter.addItem(new String("Object"));
        cmbFilter.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                lstClients.setFilter((String) cmbFilter.getSelectedItem());
                lstClients.update();
            }
        });
        
        frameClient.add(cmbFilter, BorderLayout.NORTH);
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
        initializeRenderer();
        drawer = master.createRenderer();
        if (drawer != null) {
            setDrawer(drawer);
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
            drawer = master.createRenderer();
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
        Freedomotic.environment.getPojo().setRenderer(renderer.toLowerCase());
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
    
    public void initializeRenderer() {
        drawer = null;
        frameMap.dispose();
        frameMap = new JInternalFrame();
        frameMap.setBackground(new java.awt.Color(38, 186, 254));
        frameMap.setIconifiable(false);
        frameMap.setMaximizable(true);
        frameMap.setResizable(true);
        frameMap.setTitle("Environment");
        desktopPane.add(frameMap, javax.swing.JLayeredPane.DEFAULT_LAYER);
        desktopPane.setBackground(Renderer.BACKGROUND_COLOR);
        referenceRatio = new Float(Freedomotic.environment.getPojo().getWidth() / new Float(Freedomotic.environment.getPojo().getWidth()));
        frameMap.getContentPane().setBackground(Renderer.BACKGROUND_COLOR);
    }
    
    public void setDrawer(JPanel drawer) {
        frameMap.getContentPane().add(drawer);
    }
    
    public void setMapTitle(String name) {
        frameMap.setTitle(name);
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
        mnuSaveAs = new javax.swing.JMenu();
        mnuOpenEnvironment = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        mnuEditMode = new javax.swing.JMenu();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();
        mnuLockObjects = new javax.swing.JCheckBoxMenuItem();
        mnuRenameRoom = new javax.swing.JMenuItem();
        mnuAddRoom = new javax.swing.JMenuItem();
        mnuRemoveRoom = new javax.swing.JMenuItem();
        mnuChangeRenderer = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        mnuAutomations = new javax.swing.JMenuItem();
        mnuWindow = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
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

        mnuSaveAs.setText("File");
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });

        mnuOpenEnvironment.setText("Open Environment");
        mnuOpenEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenEnvironmentActionPerformed(evt);
            }
        });
        mnuSaveAs.add(mnuOpenEnvironment);

        jMenuItem5.setText("Save Environment As...");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        mnuSaveAs.add(jMenuItem5);
        mnuSaveAs.add(jSeparator1);

        jMenuItem1.setText("Esci");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        mnuSaveAs.add(jMenuItem1);

        menuBar.add(mnuSaveAs);

        jMenu1.setText("Plugins");

        jCheckBoxMenuItem1.setText("Install from Marketplace");
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jCheckBoxMenuItem1);

        menuBar.add(jMenu1);

        mnuEditMode.setText("Environment");

        jCheckBoxMenuItem2.setText("Edit Mode");
        jCheckBoxMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem2ActionPerformed(evt);
            }
        });
        mnuEditMode.add(jCheckBoxMenuItem2);

        mnuLockObjects.setSelected(true);
        mnuLockObjects.setText("Lock Objects Position");
        mnuLockObjects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLockObjectsActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuLockObjects);

        mnuRenameRoom.setText("Rename Room");
        mnuRenameRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRenameRoomActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuRenameRoom);

        mnuAddRoom.setText("Add Room");
        mnuAddRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAddRoomActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuAddRoom);

        mnuRemoveRoom.setText("Remove Room");
        mnuRemoveRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRemoveRoomActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuRemoveRoom);

        mnuChangeRenderer.setText("Change Renderer");
        mnuChangeRenderer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuChangeRendererActionPerformed(evt);
            }
        });
        mnuEditMode.add(mnuChangeRenderer);

        menuBar.add(mnuEditMode);

        jMenu2.setText("Automation");

        mnuAutomations.setText("Manage Automations");
        mnuAutomations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAutomationsActionPerformed(evt);
            }
        });
        jMenu2.add(mnuAutomations);

        menuBar.add(jMenu2);

        mnuWindow.setText("Window");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
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

        submnuHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        submnuHelp.setText("Help");
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
                + "Release: " + Info.getReleaseDate() + " - v" + Info.getVersion() + "\n"
                + "Licence: " + Info.getLicense() + "\n\n"
                + "You can find support on:\n"
                + "http://code.google.com/p/freedomotic/" + "\n"
                + "http://freedomotic.com/");
}//GEN-LAST:event_submnuHelpActionPerformed
    
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
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
            //This is where a real application would open the file.
            Freedomotic.logger.info("Opening " + file.getAbsolutePath());
            try {
                boolean loaded = Freedomotic.loadEnvironment(file);
                if (loaded) {
                    EnvObjectPersistence.loadObjects(new File(Info.getApplicationPath() + "/data/furn/" + Freedomotic.environment.getPojo().getObjectsFolder()), false);
                }
            } catch (Exception e) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
            }
            frameMap.repaint();
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
    
    private void jCheckBoxMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem2ActionPerformed
        drawer.setEditMode(jCheckBoxMenuItem2.getState());
        this.setEditMode(jCheckBoxMenuItem2.getState());
    }//GEN-LAST:event_jCheckBoxMenuItem2ActionPerformed
    
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
        Room room = new Room();
        room.setPojo(z);
        room.getPojo().setTexture(new File(Info.getResourcesPath() + "/wood.jpg"));
        room.init();
        Freedomotic.environment.addRoom(room);
        drawer.createHandles(room);
    }//GEN-LAST:event_mnuAddRoomActionPerformed
    
    private void mnuRemoveRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRemoveRoomActionPerformed
        ZoneLogic zone = drawer.getSelectedZone();
        if (zone == null) {
            JOptionPane.showMessageDialog(this, "Select a room first");
        } else {
            Freedomotic.environment.removeZone(zone);
            drawer.createHandles(null);
        }
    }//GEN-LAST:event_mnuRemoveRoomActionPerformed
    
    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
    }//GEN-LAST:event_mnuSaveAsActionPerformed
    
    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        final JFileChooser fc = new JFileChooser(Info.getDatafilePath() + "/furn/");
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            try {
                EnvironmentPersistence.saveAs(folder);
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Freedomotic.logger.info("Save command cancelled by user.");
        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed
    
    private void mnuLockObjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLockObjectsActionPerformed
        drawer.setObjectsLock(mnuLockObjects.getState());
    }//GEN-LAST:event_mnuLockObjectsActionPerformed
    
    private void mnuAutomationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAutomationsActionPerformed
        Command c = new Command();
        c.setName("Popup Automation Editor Gui");
        c.setReceiver("app.actuators.plugins.controller.in");
        c.setProperty("plugin", "Automations Editor");
        c.setProperty("action", "show"); //the default choice
        Command reply = Freedomotic.sendCommand(c);
    }//GEN-LAST:event_mnuAutomationsActionPerformed
    
    private void mnuChangeRendererActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuChangeRendererActionPerformed
        Object[] possibilities = {"plain", "image", "photo"};
        String input = (String) JOptionPane.showInputDialog(
                this,
                "Select a renderer for the map",
                "Choose map renderer",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                "plain");

        //If a string was returned
        if ((input != null) && (input.length() > 0)) {
            changeRenderer(input);
            return;
        }
        
    }//GEN-LAST:event_mnuChangeRendererActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem mnuAddRoom;
    private javax.swing.JMenuItem mnuAutomations;
    private javax.swing.JMenuItem mnuChangeRenderer;
    private javax.swing.JMenu mnuEditMode;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JCheckBoxMenuItem mnuLockObjects;
    private javax.swing.JMenuItem mnuOpenEnvironment;
    private javax.swing.JMenuItem mnuRemoveRoom;
    private javax.swing.JMenuItem mnuRenameRoom;
    private javax.swing.JMenu mnuSaveAs;
    private javax.swing.JMenu mnuWindow;
    private javax.swing.JScrollPane scrollTxtOut1;
    private javax.swing.JScrollPane scrollTxtOut2;
    private javax.swing.JMenuItem submnuHelp;
    private javax.swing.JTextArea txtOut1;
    private javax.swing.JTextArea txtOut2;
    // End of variables declaration//GEN-END:variables
}
