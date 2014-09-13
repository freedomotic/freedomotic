/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MarketPlaceForm.java
 *
 * Created on 28-dic-2011, 23:50:20
 */
package com.freedomotic.jfrontend;

import com.freedomotic.api.API;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.jfrontend.utils.SpringUtilities;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.IPluginPackage;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.filesystem.PluginsManager;
import com.freedomotic.util.I18n.I18n;
import com.freedomotic.util.Info;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;

/**
 *
 * @author gpt
 */
public class MarketPlaceForm
        extends javax.swing.JFrame {
    //ArrayList<IPluginPackage> pluginList;

    ArrayList<IPluginCategory> pluginCategoryList;
    private static final IPlugCatComparator CatComp = new IPlugCatComparator();
    private static final IPlugPackComparator PackComp = new IPlugPackComparator();
    private final I18n I18n;
    private ClientStorage clients;

    private PluginsManager pluginsManager;

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
                    public void run() {
                        try {
                            String path = Info.PATHS.PATH_RESOURCES_FOLDER.toString();

                            if (category.retrievePluginsInfo() == null) {
                                return;
                            }

                            Collections.sort(category.retrievePluginsInfo(),
                                    PackComp);

                            //TODO: use package images.
                            ImageIcon iconPlugin
                                    = new ImageIcon(path + File.separatorChar
                                            + "plug.png", "Icon");
                            int row = 0;

                            for (final IPluginPackage pp : category.retrievePluginsInfo()) {
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
                                    String version = extractVersion(new File(pp.getFilePath(freedomoticVersion)).getName().toString());
                                    int result = clients.compareVersions(pp.getTitle(), version);
                                    //System.out.println("COMPARE VERSIONS: "+new File(pp.getFilePath()).getName().toString() + " " + version + " = "+result);
                                    if (result == -1) { //older version 
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

                                //JLabel lblDescription = new JLabel(pp.getDescription());
                                if (btnAction != null) {
                                    btnAction.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            installPackage(pp);
                                        }
                                    });
                                }

                                JButton btnMore = new JButton(I18n.msg("more_info"));
                                //btnMore.setEnabled(false);
                                btnMore.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        try {
                                            browse(new URI(pp.getURI()));
                                        } catch (URISyntaxException ex) {
                                            Logger.getLogger(MarketPlaceForm.class.getName())
                                                    .log(Level.SEVERE, null, ex);
                                        }
                                    }
                                });

                                lblIcon.setPreferredSize(new Dimension(80, 80));
                                lblIcon.setMaximumSize(new Dimension(80, 80));
                                pnlMain.add(lblIcon);
                                pnlMain.add(lblName);
                                //pnlMain.add(lblDescription);
                                pnlMain.add(btnMore);

                                if (btnAction != null) {
                                    pnlMain.add(btnAction);
                                } else {
                                    JButton disabled = new JButton(I18n.msg("install"));
                                    disabled.setEnabled(false);
                                    pnlMain.add(disabled);
                                }

                                row++;
                            }
                            
                            SpringUtilities.makeCompactGrid(pnlMain, row, 4, //rows, cols
                                    5, 5, //initX, initY
                                    5, 5); //xPad, yPad
                            politeWaitingMessage(false);
                            pnlMain.repaint();
                        } catch (Exception e) {
                            Freedomotic.logger.warning(Freedomotic.getStackTraceInfo(e));
                        }
                    }
                }).start();
            }
        });
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
        Freedomotic.logger.finest("Download string:" + string);
        task
                = new Runnable() {
                    boolean done = false;

                    @Override
                    public void run() {
                        try {
                            done = pluginsManager.installBoundle(new URL(string));
                        } catch (MalformedURLException ex) {
                            done = false;
                            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
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
     *
     */
    public static class IPlugCatComparator
            implements Comparator<IPluginCategory> {

        /**
         *
         * @param m1
         * @param m2
         * @return
         */
        @Override
        public int compare(IPluginCategory m1, IPluginCategory m2) {
            return m1.getName().compareTo(m2.getName());
        }
    }

    /**
     *
     */
    public static class IPlugPackComparator
            implements Comparator<IPluginPackage> {

        /**
         *
         * @param m1
         * @param m2
         * @return
         */
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
