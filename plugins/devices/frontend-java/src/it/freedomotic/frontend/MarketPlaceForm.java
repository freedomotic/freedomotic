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

import it.freedomotic.app.Freedomotic;
import it.freedomotic.plugins.AddonManager;
import it.freedomotic.service.MarketPlaceService;
import it.freedomotic.service.PluginPackage;
import it.freedomotic.util.Info;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

/**
 *
 * @author gpt
 */
public class MarketPlaceForm extends javax.swing.JFrame {

    ArrayList<PluginPackage> pluginList;

    /** Creates new form MarketPlaceForm */
    public MarketPlaceForm() {
        initComponents();
//        EventQueue.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                new Thread(new Runnable() {
//
//                    public void run() {
//                        MarketPlaceService mps = MarketPlaceService.getInstance();
//                        pluginList = mps.getPackageList();
//                        updatePluginsList();
//                    }
//                }).start();
//            }
//        });
        pluginList = Freedomotic.onlinePlugins;
        updatePluginsList();
        lstPlugins.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int i = lstPlugins.locationToIndex(p);
                PluginPackage pp = pluginList.get(i);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        installPackage(pp);
                    } else if (e.getClickCount() == 1) {
                        System.out.println("one click");
                    }
                }
            }
        });
        //TODO: check if the plugin package is already installed.
    }

    public Vector updatePluginsList() {
        try {
            String path = Info.getResourcesPath();
            //TODO: use package images.
            ImageIcon iconPlugin = new ImageIcon(path + File.separatorChar + "plug.png", "Icon");
            ImageIcon iconCoolPlugin = new ImageIcon(path + File.separatorChar + "plug-cool.png", "Icon");
            ImageIcon iconClient = new ImageIcon(path + File.separatorChar + "clientIcon1.png", "Icon");

            Vector vector = new Vector();

            for (PluginPackage pp : pluginList) {
                //boolean isRunning = c.isRunning();
                JPanel jp = new JPanel();

                jp.setLayout(new BorderLayout());


                jp.add(new JLabel(pp.getIcon()), BorderLayout.LINE_START);

                JLabel text = new JLabel(pp.getTitle());
                String description = pp.getDescription();
//                if ((description.length() > 41) && !(frameClient.isMaximum())) {
//                    description = description.substring(0, 40) + "...";
//                }
                JLabel lblDescription = new JLabel(description);
                text.setForeground(Color.black);
                Font font = lstPlugins.getFont();
                text.setFont(font.deriveFont(Font.BOLD, 12));
                lblDescription.setForeground(Color.gray);

                JPanel jpcenter = new JPanel();
                GridLayout grid = new GridLayout(0, 1);
                jpcenter.setLayout(grid);
                jpcenter.setOpaque(false);
                jp.add(jpcenter, BorderLayout.CENTER);
                jpcenter.add(text);
                jpcenter.add(lblDescription);
                jpcenter.setToolTipText(description);

                JPanel last = new JPanel();
                last.setOpaque(false);
                jp.add(last, BorderLayout.LINE_END);
                jp.setBackground(Color.white);
                vector.add(jp);
            }
            lstPlugins.setListData(vector);
            ListCellRenderer renderer = new CustomCellRenderer();
            lstPlugins.setCellRenderer(renderer);
            this.jProgressBar1.setVisible(false);
            return vector;
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
        return null;
    }

    private void installPackage(PluginPackage pp) {
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
                "Download of the requested plugin started in background (may take minutes). Continue to use Freedom, you will be notified when download completes.",
                "Download in progress", JOptionPane.INFORMATION_MESSAGE);
        Runnable task;
        final String string = pp.getFilePath();
        System.out.println("string de download:" + string);
        task = new Runnable() {

            boolean done = false;

            @Override
            public void run() {
                try {
                    done = AddonManager.installDevice(new URL(string));
                } catch (MalformedURLException ex) {
                    done = false;
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!done) {
                    JOptionPane.showMessageDialog(null,
                            "Unable to download the requested plugin. Check your internet connection and the provided URL.",
                            "Download Error", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Plugin downloaded. Restart Freedomotic to changes take effect.",
                            "Download Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        task.run();
    }

    class CustomCellRenderer implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Component component = (Component) value;
            component.setBackground(isSelected ? list.getSelectionBackground() : /*list.getBackground()*/ getListBackground(list, value, index, isSelected, cellHasFocus));
            component.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            setFont(list.getFont());
            return component;
        }

        private Color getListBackground(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (index % 2 == 0) {
                return (Color.decode("#f7f7f7"));
            } else {
                return list.getBackground();
            }

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstPlugins = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Freedomotic Official Online Marketplace");
        setMinimumSize(new java.awt.Dimension(521, 370));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        jLabel1.setText("This are the plugins available from our official online marketplace. Double click on a plugin name to install it.");
        getContentPane().add(jLabel1);

        jProgressBar1.setIndeterminate(true);
        getContentPane().add(jProgressBar1);

        lstPlugins.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Download of plugins list in progress...", "it can take some time please wait" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lstPlugins);

        getContentPane().add(jScrollPane1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList lstPlugins;
    // End of variables declaration//GEN-END:variables
}
