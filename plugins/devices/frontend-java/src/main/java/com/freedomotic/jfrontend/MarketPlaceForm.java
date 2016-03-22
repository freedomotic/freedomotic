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
import com.freedomotic.app.Freedomotic;
import com.freedomotic.jfrontend.utils.SpringUtilities;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.IPluginPackage;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.i18n.I18n;
import com.freedomotic.settings.Info;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gabriel Pulido de Torres
 */
public class MarketPlaceForm extends javax.swing.JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(MarketPlaceForm.class.getName());

    ArrayList<IPluginCategory> pluginCategoryList;
    private static final IPlugCatComparator CatComp = new IPlugCatComparator();
    private static final IPlugPackComparator PackComp = new IPlugPackComparator();
    private final I18n I18n;
    private final ClientStorage clients;

    private final PluginsManager pluginsManager;

    /**
     * Creates new form MarketPlaceForm
     *
     * @param api
     */
    public MarketPlaceForm(API api) {
        this.I18n = api.getI18n();
        this.clients = api.getClientStorage();
        this.pluginsManager = api.getPluginManager();
        this.setPreferredSize(new Dimension(800, 600));
        initComponents();
        cmbCategory.setEnabled(false);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MarketPlaceService mps = MarketPlaceService.getInstance();
                        pluginCategoryList = mps.getCategoryList();
                        retrieveCategories();
                    }
                }).start();
            }
        });
    }

    /**
     *
     */
    public final void retrieveCategories() {
        cmbCategory.setEnabled(false);
        Collections.sort(pluginCategoryList, CatComp);

        for (IPluginCategory pc : pluginCategoryList) {
            //do not use pc.retrievePluginsInfo() here
            //it forces to download all plugins in all categories!!
            cmbCategory.addItem(pc.getName());
        }

        //add listener to category selection changes
        cmbCategory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = cmbCategory.getSelectedIndex();
                retrievePlugins(pluginCategoryList.get(index));
            }
        });

        //force to retrive plugins for first category
        if (!pluginCategoryList.isEmpty()) {
            retrievePlugins(pluginCategoryList.get(0));
        }
    }

    private void politeWaitingMessage(boolean isActive) {
        jProgressBar1.setVisible(isActive);
        txtInfo.setVisible(isActive);
        pnlMain.setEnabled(!isActive);
        cmbCategory.setEnabled(!isActive);
    }

    /**
     *
     * @param category
     */
    public final void retrievePlugins(final IPluginCategory category) {
        politeWaitingMessage(true);
        pnlMain.setLayout(new SpringLayout());
        pnlMain.removeAll();
        pnlMain.setBackground(Color.white);
        pnlMain.repaint();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String path = Info.PATHS.PATH_RESOURCES_FOLDER.toString();

                            if (category.retrievePluginsInfo() == null) {
                                return;
                            }

                            Collections.sort(category.retrievePluginsInfo(), PackComp);

                            //TODO: use package images.
                            ImageIcon pluginIcon
                                    = new ImageIcon(path + File.separatorChar + "plug.png", "Icon");
                            int row = 0;

                            for (final IPluginPackage pp : category.retrievePluginsInfo()) {
                                renderBoundle(pp, pluginIcon);
                                row++;
                            }

                            SpringUtilities.makeCompactGrid(pnlMain, row, 4, //rows, cols
                                    5, 5, //initX, initY
                                    5, 5); //xPad, yPad
                            politeWaitingMessage(false);
                            pnlMain.repaint();
                        } catch (Exception e) {
                            LOG.warn(Freedomotic.getStackTraceInfo(e));
                        }
                    }
                }).start();
            }
        });
    }

    private void renderBoundle(final IPluginPackage pp, ImageIcon iconPlugin) {
        JLabel lblIcon;

        if (pp.getIcon() != null) {
            lblIcon = new JLabel(pp.getIcon());
        } else {
            lblIcon = new JLabel(iconPlugin);
        }

        JLabel lblName = new JLabel(pp.getTitle());
        JButton btnAction = null;
        String freedomoticVersion = Info.getMajor() + "." + Info.getMinor();
        if (pp.getFilePath(freedomoticVersion) != null
                && !pp.getFilePath(freedomoticVersion).isEmpty()
                && pp.getTitle() != null) {
            String version = extractVersion(new File(pp.getFilePath(freedomoticVersion)).getName());
            int result = clients.compareVersions(pp.getTitle(), version);
            //System.out.println("COMPARE VERSIONS: "+new File(pp.getFilePath()).getName().toString() + " " + version + " = "+result);
            if (result == -1) { //older version or not yet installed
                //btnAction = new JButton(pp.getTitle() + " (Install version " + version + ")");
                btnAction = new JButton(I18n.msg("install"));
            } else {
                if (result == 1) { //newer version
                    //btnAction = new JButton(pp.getTitle() + " (Update from " + version + " to " + version + ")");
                    btnAction = new JButton(I18n.msg("update"));
                }
            }
        } else {
            lblName
                    = new JLabel(I18n.msg(
                                    "X_unavailable",
                                    new Object[]{
                                        pp.getTitle()
                                    }));
        }

        if (btnAction != null) {
            btnAction.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    installPackage(pp);
                }
            });
        }

        JButton btnMore = new JButton(I18n.msg("more_info"));
        btnMore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    browse(new URI(pp.getURI()));
                } catch (URISyntaxException ex) {
                    LOG.error(ex.getMessage());
                }
            }
        });

        lblIcon.setPreferredSize(new Dimension(80, 80));
        lblIcon.setMaximumSize(new Dimension(80, 80));
        pnlMain.add(lblIcon);
        pnlMain.add(lblName);
        pnlMain.add(btnMore);

        if (btnAction != null) {
            pnlMain.add(btnAction);
        } else {
            JButton disabled = new JButton(I18n.msg("install"));
            disabled.setEnabled(false);
            pnlMain.add(disabled);
        }
    }

    private String extractVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x-1.212 is needed
        //remove extension
        filename
                = filename.substring(0,
                        filename.lastIndexOf("."));

        String[] tokens = filename.split("-");

        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1] + "-" + tokens[2];
        } else {
            return filename;
        }
    }

    private void installPackage(IPluginPackage pp) {
        String freedomoticVersion = Info.getMajor() + "." + Info.getMinor();

        if (pp.getFilePath(freedomoticVersion) == null) {
            JOptionPane.showMessageDialog(this,
                    I18n.msg(
                            "warn_plugin_X_unavailable",
                            new Object[]{pp.getTitle(), pp.getURI()}));

            return;
        }

        //Custom button text
        Object[] options = {I18n.msg("yes_please"), I18n.msg("no_thanks")};
        int n
                = JOptionPane.showOptionDialog(null,
                        I18n.msg(
                                "confirm_package_X_download",
                                new Object[]{pp.getTitle()}),
                        I18n.msg("title_install_package"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]);

        if (n != 0) {
            return;
        }

        JOptionPane.showMessageDialog(null,
                I18n.msg("info_download_started"),
                I18n.msg("title_download_started"),
                JOptionPane.INFORMATION_MESSAGE);

        Runnable task;
        final String string = pp.getFilePath(freedomoticVersion);
        LOG.info("Download string: {}", string);
        task
                = new Runnable() {
                    boolean done = false;

                    @Override
                    public void run() {
                        try {
                            done = pluginsManager.installBoundle(new URL(string));
                        } catch (MalformedURLException ex) {
                            done = false;
                            LOG.error(ex.getMessage());
                        }

                        if (!done) {
                            JOptionPane.showMessageDialog(null,
                                    I18n.msg("info_download_failed"),
                                    I18n.msg("title_download_failed"),
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    I18n.msg("info_package_install_completed"),
                                    I18n.msg("title_install_completed"),
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                };
        task.run();
    }

    private void browse(URI uri) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            JOptionPane.showInputDialog(
                    null,
                    I18n.msg("info_point_browser_to"),
                    I18n.msg("info"),
                    JOptionPane.PLAIN_MESSAGE, null, null,
                    uri.toString());
        } else {
            try {
                java.awt.Desktop.getDesktop().browse(uri);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Order plugin categories by name
     */
    public static class IPlugCatComparator implements Comparator<IPluginCategory> {

        @Override
        public int compare(IPluginCategory m1, IPluginCategory m2) {
            return m1.getName().compareTo(m2.getName());
        }
    }

    /**
     * Order plugin buoudles by name
     */
    public static class IPlugPackComparator implements Comparator<IPluginPackage> {

        @Override
        public int compare(IPluginPackage m1, IPluginPackage m2) {
            return m1.getTitle().compareTo(m2.getTitle());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtInfo = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        cmbCategory = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        pnlMain = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(I18n.msg("title_marketplace"));
        setMinimumSize(new java.awt.Dimension(521, 370));

        txtInfo.setText(I18n.msg("connecting_online_repo"));

        jProgressBar1.setIndeterminate(true);

        pnlMain.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(pnlMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 609, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtInfo)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbCategory;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JLabel txtInfo;

    // End of variables declaration//GEN-END:variables
}
