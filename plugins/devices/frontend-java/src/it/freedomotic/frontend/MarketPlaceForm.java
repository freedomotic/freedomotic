/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MarketPlaceForm.java
 *
 * Created on 28-dic-2011, 23:50:20
 */
package it.freedomotic.frontend;

import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.frontend.utils.PropertiesPanel_1;
import it.freedomotic.plugins.AddonLoader;
import it.freedomotic.service.IPluginCategory;
import it.freedomotic.service.MarketPlaceService;
import it.freedomotic.service.IPluginPackage;
import it.freedomotic.util.Info;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author gpt
 */
public class MarketPlaceForm extends javax.swing.JFrame {

    ArrayList<IPluginPackage> pluginList;
    ArrayList<IPluginCategory> pluginCategoryList;

    /**
     * Creates new form MarketPlaceForm
     */
    public MarketPlaceForm() {
        initComponents();
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

        //pluginCategoryList = Freedomotic.onlinePluginCategories;
    }

    public final void retrieveCategories() {
        for (IPluginCategory pc : pluginCategoryList) {
            cmbCategory.addItem(pc.getName() + " (" + pc.getPlugins().size() + " plugins)");
            txtInfo.setText("Retrieved new category: " + pc.getName());
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
        jProgressBar1.setVisible(false);
        validate();
    }

    public final void retrievePlugins(IPluginCategory category) {
        String path = Info.PATH_RESOURCES_FOLDER.toString();
        if (category.getPlugins() == null) {
            return;
        }
        //TODO: use package images.
        ImageIcon iconPlugin = new ImageIcon(path + File.separatorChar + "plug.png", "Icon");
        ImageIcon iconCoolPlugin = new ImageIcon(path + File.separatorChar + "plug-cool.png", "Icon");
        ImageIcon iconClient = new ImageIcon(path + File.separatorChar + "clientIcon1.png", "Icon");

        PropertiesPanel_1 panel = new PropertiesPanel_1(category.getPlugins().size(), 5);
        panel.removeAll();
        int row = 0;
        for (final IPluginPackage pp : category.getPlugins()) {
            txtInfo.setText("Retrieved plugin " + (row +1) + " of " + category.getPlugins().size());
            JLabel lblIcon;
            if (pp.getIcon() != null) {
                lblIcon = new JLabel(pp.getIcon());
            } else {
                lblIcon = new JLabel(iconPlugin);
            }
            JLabel lblName = new JLabel(pp.getTitle());
            JButton btnAction = null;
            String freedomoticVersion = Info.getMajor() +"."+Info.getMinor();
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
            btnMore.setEnabled(false);
            btnMore.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        browse(new URI(pp.getUri()));
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MarketPlaceForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            lblIcon.setPreferredSize(new Dimension(80, 80));
            lblIcon.setMaximumSize(new Dimension(80, 80));
            panel.addElement(lblIcon, row, 0);
            panel.addElement(lblName, row, 1);
            panel.addElement(lblDescription, row, 2);
            panel.addElement(btnMore, row, 3);
            if (btnAction != null) {
                panel.addElement(btnAction, row, 4);
            } else {
                JButton disabled = new JButton("Install");
                disabled.setEnabled(false);
                panel.addElement(disabled, row, 4);
            }
            row++;
        }
        txtInfo.setText("Click on Install button to enable one of this plugins.");
        panel.layoutPanel();
        jPanel1.removeAll();
        jPanel1.add(panel);
        jProgressBar1.setVisible(false);
        validate();
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
        if (pp.getFilePath() == null) {
            JOptionPane.showMessageDialog(this,
                    "It seems that " + pp.getTitle() + " plugin developer have not "
                    + "already released any stable version. \nYou can ask more info about this plugin "
                    + "sending a mail to its author. \nYou can get author mail from " + pp.getUri());
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
        final String string = pp.getFilePath();
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
            JOptionPane.showMessageDialog(null, "Please point your browser to " + uri.toString());
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            JOptionPane.showMessageDialog(null, "Please point your browser to " + uri.toString());
        }

        try {
            desktop.browse(uri);
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Freedomotic Official Online Marketplace");
        setMinimumSize(new java.awt.Dimension(521, 370));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        txtInfo.setText("Connecting to online repository...");
        getContentPane().add(txtInfo);

        jProgressBar1.setIndeterminate(true);
        getContentPane().add(jProgressBar1);

        getContentPane().add(cmbCategory);

        jPanel1.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(jPanel1);

        getContentPane().add(jScrollPane1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbCategory;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel txtInfo;
    // End of variables declaration//GEN-END:variables
}
