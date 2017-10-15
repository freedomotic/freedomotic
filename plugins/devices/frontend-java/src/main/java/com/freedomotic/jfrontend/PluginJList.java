/**
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
 * <p>
 * This file is part of Freedomotic
 * <p>
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * <p>
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;
import java.util.Vector;

/**
 *
 * @author Enrico Nicoletti
 */
public final class PluginJList extends JList {

    private static final Logger LOG = LoggerFactory.getLogger(PluginJList.class.getName());
    private String filter;
    private static final String TYPE_PLUGIN = "plugin";
    private static final String TYPE_OBJECT = "object";

    private MainWindow parentWindow;

    /**
     *
     * @param parentWindow
     */
    public PluginJList(final MainWindow parentWindow) {
        this.parentWindow = parentWindow;
        setFilter(TYPE_PLUGIN); //default value for filterning the list
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int i = locationToIndex(p);
                java.util.List<Client> clients = (java.util.List<Client>) getApi().getClients(getFilter());
                Client client = clients.get(i);

                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    if (client.isRunning()) {
                        client.stop();
                    } else {
                        client.start();
                    }
                    update();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Optional.of(e).filter(MouseEvent::isPopupTrigger).ifPresent(this::doPop);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Optional.of(e).filter(MouseEvent::isPopupTrigger).ifPresent(this::doPop);
            }

            private void doPop(MouseEvent e) {
                Point p = e.getPoint();
                int i = locationToIndex(p);
                java.util.List<Client> clients = (java.util.List<Client>) getApi().getClients(getFilter());
                final Client client = clients.get(i);
                JPopupMenu menu = new JPopupMenu();
                JMenuItem mnuConfigure = null;
                switch (client.getType()) {
                    case TYPE_PLUGIN:
                        mnuConfigure = new JMenuItem("Configure " + client.getName());
                        break;
                    case TYPE_OBJECT:
                        mnuConfigure = new JMenuItem("Add " + client.getName() + " Object");
                        break;
                    default:
                        mnuConfigure = new JMenuItem("Placeholder menu");
                }
                mnuConfigure.addActionListener(event -> {
                    switch (client.getType()) {
                        case TYPE_PLUGIN:
                            client.start();
                            client.showGui();
                            update();
                            break;
                        case TYPE_OBJECT:
                            ObjectPluginPlaceholder objp = (ObjectPluginPlaceholder) client;
                            Optional.ofNullable(parentWindow).ifPresent(window -> objp.startOnEnv(window.getDrawer().getCurrEnv())); //adds the object to the environment
                            break;
                        default:
                            break;
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
        return parentWindow.getPlugin().getApi();
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
            ImageIcon defaultIconRunning = new ImageIcon(ResourcesManager.getResource("plugin-running.png", 64, 64));
            ImageIcon defaultIconStopped = new ImageIcon(ResourcesManager.getResource("plugin-stopped.png", 64, 64));

            Vector vector = new Vector();
            Collection<Client> clients = getApi().getClients(getFilter());

            clients.stream().filter(addon -> addon.getType().equalsIgnoreCase(getFilter())).forEach(addon -> vector.add(processAddon(addon, defaultIconRunning, defaultIconStopped)));
            setListData(vector);
            ListCellRenderer renderer = new CustomCellRenderer();
            setCellRenderer(renderer);
        } catch (Exception e) {
            LOG.error(Freedomotic.getStackTraceInfo(e));
        }
    }

    /**
     * This method returns a Jpanel for the addon
     * @param addon addon for which Jpanel is to be constructed
     * @param defaultIconRunning default icon for running status of addon
     * @param defaultIconStopped default icon for stopped status of addon
     * @return JPanel
     */
    private JPanel processAddon(Client addon, ImageIcon defaultIconRunning, ImageIcon defaultIconStopped) {
        boolean isRunning = addon.isRunning();
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        BufferedImage imageRunning = null;
        BufferedImage imageStopped = null;
        if (addon.getType().equalsIgnoreCase(TYPE_PLUGIN)) {
            imageRunning = ResourcesManager.getResource(addon.getClass().getSimpleName().toLowerCase()
                    + "-running.png", 64, 64);
            imageStopped = ResourcesManager.getResource(addon.getClass().getSimpleName().toLowerCase()
                    + "-stopped.png", 64, 64);
        } else {
            if (addon.getType().equalsIgnoreCase(TYPE_OBJECT)) {
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
        return jp;
    }

    class CustomCellRenderer
            implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component component = (Component) value;
            component.setBackground(isSelected ? list.getSelectionBackground()
                    : getListBackground(list, index));
            component.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            return component;
        }

        private Color getListBackground(JList list, int index) {
            if ((index % 2) == 0) {
                return (Color.decode("#f7f7f7"));
            } else {
                return list.getBackground();
            }
        }
    }
}
