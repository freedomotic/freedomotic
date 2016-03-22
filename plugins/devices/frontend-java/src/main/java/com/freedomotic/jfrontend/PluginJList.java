/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
import com.freedomotic.app.Freedomotic;
import com.freedomotic.core.ResourcesManager;
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public final class PluginJList extends JList {

    private static final Logger LOG = LoggerFactory.getLogger(PluginJList.class.getName());
    private String filter;

    /**
     *
     */
    public boolean inDrag = false;

    /**
     *
     */
    public int dragged = 0;
    private MainWindow parent;

    /**
     *
     * @param parent
     */
    public PluginJList(final MainWindow parent) {
        this.parent = parent;
        setFilter("plugin"); //default value for filterning the list
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int i = locationToIndex(p);
                java.util.List<Client> clients = (java.util.List<Client>) getApi().getClients(getFilter());
                Client client = (Client) clients.get(i);

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        if (client.isRunning()) {
                            client.stop();
                        } else {
                            client.start();
                        }

                        update();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    doPop(e);
                } else {
                    //drag started
                    inDrag = true;
                    dragged = 0;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    doPop(e);
                }
            }

            private void doPop(MouseEvent e) {
                Point p = e.getPoint();
                int i = locationToIndex(p);
                java.util.List<Client> clients = (java.util.List<Client>) getApi().getClients(getFilter());
                final Client client = (Client) clients.get(i);
                JPopupMenu menu = new JPopupMenu();
                JMenuItem mnuConfigure = null;

                if (client.getType().equalsIgnoreCase("plugin")) {
                    mnuConfigure = new JMenuItem("Configure " + client.getName());
                } else {
                    if (client.getType().equalsIgnoreCase("object")) {
                        mnuConfigure = new JMenuItem("Add " + client.getName() + " Object");
                    } else {
                        mnuConfigure = new JMenuItem("Placeholder menu");
                    }
                }

                mnuConfigure.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (client.getType().equalsIgnoreCase("plugin")) {
                            client.start();
                            client.showGui();
                            update();
                        }

                        if (client.getType().equalsIgnoreCase("object")) {
                            ObjectPluginPlaceholder objp = (ObjectPluginPlaceholder) client;

                            if (parent instanceof MainWindow) {
                                MainWindow mw = (MainWindow) parent;
                                objp.startOnEnv(mw.getDrawer().getCurrEnv()); //adds the object to the environment
                            }
                        }
                    }
                });
                menu.add(mnuConfigure);
                menu.show(e.getComponent(),
                        e.getX(),
                        e.getY());
            }
        });
    }

    private API getApi() {
        return parent.getPlugin().getApi();
    }

    /**
     *
     * @return
     */
    public String getFilter() {
        return filter;
    }

    /**
     *
     * @param filter
     */
    public void setFilter(String filter) {
        this.filter = filter;
        update();
    }

    /**
     *
     */
    public void update() {
        try {
            ImageIcon defaultIconRunning
                    = new ImageIcon(ResourcesManager.getResource("plugin-running.png", 64, 64)); //new ImageIcon(path + File.separatorChar + "plug.png", "Icon");
            ImageIcon defaultIconStopped
                    = new ImageIcon(ResourcesManager.getResource("plugin-stopped.png", 64, 64)); //new ImageIcon(path + File.separatorChar + "plug-cool.png", "Icon");

            Vector vector = new Vector();
            Collection<Client> clients = getApi().getClients(getFilter());

            for (Client addon : clients) {
                if (addon.getType().equalsIgnoreCase(getFilter())) {
                    boolean isRunning = addon.isRunning();
                    JPanel jp = new JPanel();

                    jp.setLayout(new BorderLayout());

                    BufferedImage imageRunning = null;
                    BufferedImage imageStopped = null;

                    if (addon.getType().equalsIgnoreCase("plugin")) {
                        imageRunning = ResourcesManager.getResource(addon.getClass().getSimpleName().toLowerCase()
                                + "-running.png", 64, 64);
                        imageStopped = ResourcesManager.getResource(addon.getClass().getSimpleName().toLowerCase()
                                + "-stopped.png", 64, 64);
                    } else {
                        if (addon.getType().equalsIgnoreCase("object")) {
                            ObjectPluginPlaceholder obj = (ObjectPluginPlaceholder) addon;
                            String icon = obj.getObject().getPojo().getRepresentations().get(0).getIcon();

                            if (icon != null) {
                                imageRunning = ResourcesManager.getResource(icon, 64, 64);
                                imageStopped = ResourcesManager.getResource(icon, 64, 64);
                            }
                        }
                    }

                    ImageIcon customIconRunning = defaultIconRunning;
                    ImageIcon customIconStopped = defaultIconStopped;

                    if (imageRunning != null) {
                        customIconRunning = new ImageIcon(imageRunning);
                    }

                    if (imageStopped != null) {
                        customIconStopped = new ImageIcon(imageStopped);
                    }

                    if (isRunning) {
                        jp.add(new JLabel(customIconRunning),
                                BorderLayout.LINE_START);
                    } else {
                        jp.add(new JLabel(customIconStopped),
                                BorderLayout.LINE_START);
                    }

                    JLabel text = new JLabel(addon.getName());
                    String description = addon.getDescription();

                    JLabel lblDescription = new JLabel(description);
                    text.setForeground(Color.black);

                    Font font = getFont();
                    text.setFont(font.deriveFont(Font.BOLD, 12));
                    lblDescription.setForeground(Color.gray);

                    if (!isRunning) {
                        text.setForeground(Color.lightGray);
                        lblDescription.setForeground(Color.lightGray);
                    }

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
            }

            setListData(vector);

            ListCellRenderer renderer = new CustomCellRenderer();
            setCellRenderer(renderer);
        } catch (Exception e) {
            LOG.error(Freedomotic.getStackTraceInfo(e));
        }
    }

    class CustomCellRenderer
            implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Component component = (Component) value;
            component.setBackground(isSelected ? list.getSelectionBackground()
                    : /*
                     * list.getBackground()
                     */ getListBackground(list, value, index, isSelected, cellHasFocus));
            component.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            //setFont(list.getFont());
            return component;
        }

        private Color getListBackground(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if ((index % 2) == 0) {
                return (Color.decode("#f7f7f7"));
            } else {
                return list.getBackground();
            }
        }
    }
}
