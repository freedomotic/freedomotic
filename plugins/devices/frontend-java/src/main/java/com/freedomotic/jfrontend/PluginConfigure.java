/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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
package com.freedomotic.jfrontend;

import com.freedomotic.api.API;
import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class PluginConfigure
        extends javax.swing.JFrame {

    private ClientStorage clients;
    private final API api;

    private PluginsManager pluginsManager;
    private static HashMap<Plugin, String> predefined = new HashMap<Plugin, String>();
    private static final Logger LOG = LoggerFactory.getLogger(PluginConfigure.class.getName());

    /**
     * Creates new form PluginConfigure
     *
     * @param api
     */
    public PluginConfigure(API api) {
        this.api = api;
        this.clients = api.getClientStorage();
        this.pluginsManager = api.getPluginManager();
        initComponents();
        populatePluginsList();
        //add listener to category selection changes
        cmbPlugin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Plugin item = (Plugin) cmbPlugin.getSelectedItem();
                btnDefault.setEnabled(false);
                getConfiguration(item);
            }
        });

        try {
            cmbPlugin.setSelectedIndex(0);
        } catch (Exception e) {
        }

        setPreferredSize(new Dimension(650, 550));
        pack();
        setVisible(true);
    }

    /**
     *
     * @param api
     * @param showClient
     */
    public PluginConfigure(API api, Client showClient) {
        this(api);
        if (showClient instanceof Plugin) {
            cmbPlugin.setSelectedItem((Plugin) showClient);
        }
    }

    private void populatePluginsList() {
        cmbPlugin.removeAllItems();

        for (Client client : api.getClients("plugin")) {
            if (client instanceof Plugin) {
                Plugin plugin = (Plugin) client;
                cmbPlugin.addItem(plugin);
            }
        }
    }

    private void getConfiguration(Plugin item) {
        txtArea.setContentType("text/xml");

        String config = readConfiguration(item.getFile()).trim();
        //add old config to predefined to be restored in a later step
        predefined.put(item, config);
        btnDefault.setEnabled(true);
        txtArea.setText(config);
    }

    private String readConfiguration(File file) {
        FileInputStream fis;
        BufferedInputStream bis;
        DataInputStream dis;
        StringBuilder buff = new StringBuilder();

        try {
            fis = new FileInputStream(file);

            // Here BufferedInputStream is added for fast reading.
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);

            // dis.available() returns 0 if the file does not have more lines.
            while (dis.available() != 0) {
                // this statement reads the line from the file and print it to
                // the console.
                buff.append(dis.readLine()).append("\n");
            }

            // dispose all the resources after using them.
            fis.close();
            bis.close();
            dis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buff.toString();
    }

    private void saveConfiguration(File file, String text)
            throws IOException {
        // Create file 
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(text);
        //Close the output stream
        out.close();
    }

    private void rollbackConfiguration() {
        Plugin item = (Plugin) cmbPlugin.getSelectedItem();
        txtArea.setText(predefined.get(item));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbPlugin = new javax.swing.JComboBox();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtArea = new javax.swing.JEditorPane();
        btnDefault = new javax.swing.JButton();
        uninstallButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(api.getI18n().msg("plugins_configuration_editor"));

        btnSave.setText(api.getI18n().msg("save_restart"));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setText(api.getI18n().msg("cancel"));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(txtArea);

        btnDefault.setText(api.getI18n().msg("undo_edit"));
        btnDefault.setEnabled(false);
        btnDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDefaultActionPerformed(evt);
            }
        });

        uninstallButton.setForeground(new java.awt.Color(255, 0, 0));
        uninstallButton.setText(api.getI18n().msg("uninstall")
        );
        uninstallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uninstallButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbPlugin, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDefault)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uninstallButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbPlugin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnCancel)
                    .addComponent(btnDefault)
                    .addComponent(uninstallButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_btnSaveActionPerformed

        Plugin item = (Plugin) cmbPlugin.getSelectedItem();
        String name = item.getName();

        try {
            saveConfiguration(item.getFile(),
                    txtArea.getText());
            //stopping and unloading the plugin
            clients.remove(item);
            //reload it with the new configuration
            System.out.println(item.getFile().getParentFile().toString());
            pluginsManager.loadSingleBoundle(item.getFile().getParentFile());

            //if not loaded sucessfully reset to old configuration
            if (clients.get(name) == null) {
                //reset to old working config and reload plugin
                rollbackConfiguration();
                saveConfiguration(item.getFile(),
                        txtArea.getText());
                pluginsManager.loadSingleBoundle(item.getFile().getParentFile());
                clients.get(name).start();
                JOptionPane.showMessageDialog(this,
                        api.getI18n().msg("warn_reset_old_config"));
            } else {
                clients.get(name).start();
                this.dispose();
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnDefaultActionPerformed(java.awt.event.ActionEvent evt)    {//GEN-FIRST:event_btnDefaultActionPerformed
        rollbackConfiguration();
    }//GEN-LAST:event_btnDefaultActionPerformed

    private void uninstallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uninstallButtonActionPerformed

        Plugin item = (Plugin) cmbPlugin.getSelectedItem();

        StringBuilder uninstallCandidates = new StringBuilder();
        File boundleRootFolder = item.getFile().getParentFile();
        for (Client client : clients.getClients()) {
            if (client instanceof Plugin) {
                Plugin plugin = (Plugin) client;
                //if this plugin is in the same plugin boundle of the one
                //the user is trying to uninstall
                if (plugin.getFile().getParentFile().equals(boundleRootFolder)) {
                    uninstallCandidates.append("'" + plugin.getName() + "' ");
                }
            }
        }
        String localizedMessage
                = api.getI18n().msg("confirm_plugin_delete", new Object[]{uninstallCandidates.toString()});

        int result = JOptionPane.showConfirmDialog(null,
                new JLabel(localizedMessage),
                api.getI18n().msg("confirm_deletion_title"),
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            pluginsManager.uninstallBundle(item);
            dispose();
        }
    }//GEN-LAST:event_uninstallButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDefault;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbPlugin;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JEditorPane txtArea;
    private javax.swing.JButton uninstallButton;
    // End of variables declaration//GEN-END:variables
}
