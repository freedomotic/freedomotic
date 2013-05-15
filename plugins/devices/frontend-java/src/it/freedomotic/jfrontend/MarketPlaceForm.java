/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MarketPlaceForm.java
 *
 * Created on 28-dic-2011, 23:50:20
 */
package it.freedomotic.jfrontend;

import it.freedomotic.api.Client;
import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.jfrontend.utils.SpringUtilities;
import it.freedomotic.plugins.AddonLoader;
import it.freedomotic.plugins.ClientStorage;
import it.freedomotic.service.IPluginCategory;
import it.freedomotic.service.IPluginPackage;
import it.freedomotic.service.MarketPlaceService;
import it.freedomotic.util.Info;
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
import javax.swing.SwingUtilities;

/**
 *
 * @author gpt
 */
public class MarketPlaceForm extends javax.swing.JFrame {

    //ArrayList<IPluginPackage> pluginList;
    ArrayList<IPluginCategory> pluginCategoryList;
    private static final IPlugCatComparator CatComp = new IPlugCatComparator();
    private static final IPlugPackComparator PackComp = new IPlugPackComparator();

    /**
     * Creates new form MarketPlaceForm
     */
    public MarketPlaceForm() {
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

    public final void retrieveCategories() {
        cmbCategory.setEnabled(false);
        Collections.sort(pluginCategoryList, CatComp);
        for (IPluginCategory pc : pluginCategoryList) {
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
                            String path = Info.PATH_RESOURCES_FOLDER.toString();
                            if (category.getPlugins() == null) {
                                return;
                            }
                            Collections.sort(category.getPlugins(),PackComp);
                            //TODO: use package images.
                            ImageIcon iconPlugin = new ImageIcon(path + File.separatorChar + "plug.png", "Icon");
                            ImageIcon iconCoolPlugin = new ImageIcon(path + File.separatorChar + "plug-cool.png", "Icon");
                            ImageIcon iconClient = new ImageIcon(path + File.separatorChar + "clientIcon1.png", "Icon");
                            int row = 0;
                            for (final IPluginPackage pp : category.getPlugins()) {
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
                                        && pp.getFilePath(freedomoticVersion) != ""
                                        && pp.getTitle() != null) {
                                    String version = extractVersion(new File(pp.getFilePath(freedomoticVersion)).getName().toString());
                                    int result = Plugin.compareVersions(pp.getTitle(), version);
                                    //System.out.println("COMPARE VERSIONS: "+new File(pp.getFilePath()).getName().toString() + " " + version + " = "+result);
                                    if (result == -1) { //older version
                                        //btnAction = new JButton(pp.getTitle() + " (Install version " + version + ")");
                                        btnAction = new JButton("Install");
                                    } else {
                                        if (result == 1) { //newer version
                                            //btnAction = new JButton(pp.getTitle() + " (Update from " + version + " to " + version + ")");
                                            btnAction = new JButton("Update");
                                        }
                                    }
                                } else {
                                    lblName = new JLabel(pp.getTitle() + (" (Unavailable)"));
                                }
                                JLabel lblDescription = new JLabel(pp.getDescription());
                                if (btnAction != null) {
                                    btnAction.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            installPackage(pp);
                                        }
                                    });
                                }

                                JButton btnMore = new JButton("More info...");
                                //btnMore.setEnabled(false);
                                btnMore.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        try {
                                            browse(new URI(pp.getURI()));
                                        } catch (URISyntaxException ex) {
                                            Logger.getLogger(MarketPlaceForm.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                });

                                lblIcon.setPreferredSize(new Dimension(80, 80));
                                lblIcon.setMaximumSize(new Dimension(80, 80));
                                pnlMain.add(lblIcon);
                                pnlMain.add(lblName);
                                pnlMain.add(lblDescription);
                                pnlMain.add(btnMore);
                                if (btnAction != null) {
                                    pnlMain.add(btnAction);
                                } else {
                                    JButton disabled = new JButton("Install");
                                    disabled.setEnabled(false);
                                    pnlMain.add(disabled);
                                }
                                row++;
                            }
                            SpringUtilities.makeCompactGrid(pnlMain,
                                    row, 5, //rows, cols
                                    5, 5, //initX, initY
                                    5, 5);//xPad, yPad
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
        filename = filename.substring(0, filename.lastIndexOf("."));
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
                    "It seems that " + pp.getTitle() + " plugin developer have not "
                    + "already released any stable version. \nYou can ask more info about this plugin "
                    + "sending a mail to its author. \nYou can get author mail from " + pp.getURI());
            return;
        }
        //Custom button text
        Object[] options = {"Yes, please",
            "No, thanks"};
        int n = JOptionPane.showOptionDialog(null, "Do you want to download and install the package \n"
                + pp.getTitle() + "?",
                "Install package",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        if (n != 0) {
            return;
        }
        JOptionPane.showMessageDialog(null,
                "Download of the requested plugin started in background (may take minutes). \n"
                + "Continue to use Freedomotic, you will be notified when download completes.",
                "Download in progress", JOptionPane.INFORMATION_MESSAGE);
        Runnable task;
        final String string = pp.getFilePath(freedomoticVersion);
        System.out.println("string de download:" + string);
        task = new Runnable() {
            boolean done = false;

            @Override
            public void run() {
                try {
                    done = AddonLoader.installDevice(new URL(string));
                } catch (MalformedURLException ex) {
                    done = false;
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!done) {
                    JOptionPane.showMessageDialog(null,
                            "Unable to download the requested plugin. Check your internet connection and "
                            + "the provided URL.",
                            "Download Error", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Plugin downloaded, installed and ready to be started.",
                            "Download Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        task.run();
    }

    private void browse(URI uri) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            JOptionPane.showInputDialog(
                    null,
                    "Please point your web browser to",
                    "Message",
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

    public static class IPlugCatComparator implements Comparator<IPluginCategory> {

        @Override
        public int compare(IPluginCategory m1, IPluginCategory m2) {
            return m1.getName().compareTo(m2.getName());
        }
    }

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
        setTitle("Freedomotic Official Online Marketplace");
        setMinimumSize(new java.awt.Dimension(521, 370));

        txtInfo.setText("Connecting to online repository...");

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
