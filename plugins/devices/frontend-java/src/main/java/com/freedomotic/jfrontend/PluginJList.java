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
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.freedomotic.core.ResourcesManager.getResource;

/**
 *
 * @author Enrico Nicoletti
 */
public final class PluginJList extends JList {

    private static final Logger LOG = LoggerFactory.getLogger(PluginJList.class.getName());
    private static final String PLUGIN_TYPE = "plugin";
    private static final String OBJECT_TYPE = "object";
    private String filter;

    private MainWindow mainWindow;

    public enum Icon {
        RUNNING("running"),
        STOPPED("stopped");

        private String type;

        Icon(String type) {
            this.type = type;
        }

        public String getValue() {
            return type;
        }
    }

    /**
     * @param mainWindow
     */
    PluginJList(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setFilter(PLUGIN_TYPE); //default value for filtering the list
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
            public void mousePressed(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    doPop(event);
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    doPop(event);
                }
            }
        });
    }

    private void doPop(MouseEvent event) {
        Point point = event.getPoint();
        int index = locationToIndex(point);
        List<Client> clients = Lists.newArrayList(getApi().getClients(getFilter()));
        final Client client = clients.get(index);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mnuConfigure;

        if (PLUGIN_TYPE.equalsIgnoreCase(client.getType())) {
            mnuConfigure = new JMenuItem("Configure " + client.getName());
        } else if (OBJECT_TYPE.equalsIgnoreCase(client.getType())) {
            mnuConfigure = new JMenuItem("Add " + client.getName() + " Object");
        } else {
            mnuConfigure = new JMenuItem("Placeholder menu");
        }


        mnuConfigure.addActionListener(e1 -> {
            if (PLUGIN_TYPE.equalsIgnoreCase(client.getType())) {
                client.start();
                client.showGui();
                update();
            } else if (OBJECT_TYPE.equalsIgnoreCase(client.getType())) {
                ObjectPluginPlaceholder pluginPlaceholder = (ObjectPluginPlaceholder) client;
                if (mainWindow != null) {
                    pluginPlaceholder.startOnEnv(mainWindow.getDrawer().getCurrEnv()); //adds the object to the environment
                }
            }
        });
        menu.add(mnuConfigure);
        menu.show(event.getComponent(), event.getX(), event.getY());
    }

    private API getApi() {
        return mainWindow.getPlugin().getApi();
    }

    /**
     * @return
     */
    public String getFilter() {
        return filter;
    }

    /**
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

            Vector vector = new Vector();
            Collection<Client> clients = getApi().getClients(getFilter());

            clients.stream()
                    .filter(client -> client.getType().equalsIgnoreCase(getFilter()))
                    .map(this::createJPanelForClient)
                    .forEach(vector::add);

            setListData(vector);
            ListCellRenderer renderer = new CustomCellRenderer();
            setCellRenderer(renderer);
        } catch (Exception e) {
            LOG.error(Freedomotic.getStackTraceInfo(e));
        }
    }

    private JPanel createJPanelForClient(Client client) {
        boolean isRunning = client.isRunning();
        JPanel jPanelMain = new JPanel();
        jPanelMain.setLayout(new BorderLayout());

        if (isRunning) {
            jPanelMain.add(new JLabel(getImageIcon(client, Icon.RUNNING.getValue())), BorderLayout.LINE_START);
        } else {
            jPanelMain.add(new JLabel(getImageIcon(client, Icon.STOPPED.getValue())), BorderLayout.LINE_START);
        }

        JLabel text = new JLabel(client.getName());
        String description = client.getDescription();

        JLabel lblDescription = new JLabel(description);
        text.setForeground(Color.black);

        Font font = getFont();
        text.setFont(font.deriveFont(Font.BOLD, 12));
        lblDescription.setForeground(Color.gray);

        if (!isRunning) {
            text.setForeground(Color.lightGray);
            lblDescription.setForeground(Color.lightGray);
        }

        JPanel jPanelCenter = new JPanel();
        GridLayout grid = new GridLayout(0, 1);
        jPanelCenter.setLayout(grid);
        jPanelCenter.setOpaque(false);
        jPanelCenter.add(text);
        jPanelCenter.add(lblDescription);
        jPanelCenter.setToolTipText(description);

        JPanel jPanelLast = new JPanel();
        jPanelLast.setOpaque(false);

        jPanelMain.add(jPanelCenter, BorderLayout.CENTER);
        jPanelMain.add(jPanelLast, BorderLayout.LINE_END);
        jPanelMain.setBackground(Color.white);

        return jPanelMain;
    }

    private ImageIcon getImageIcon(Client client, String iconType) {
        ImageIcon defaultIcon = new ImageIcon(getResource("plugin-" + iconType + ".png", 64, 64));

        Optional<BufferedImage> image = Optional.empty();
        if (PLUGIN_TYPE.equalsIgnoreCase(client.getType())) {
            image = getClientImage(client.getClass().getSimpleName().toLowerCase() + "-" + iconType + ".png");
        } else if (OBJECT_TYPE.equalsIgnoreCase(client.getType())) {
            image = getPlaceHolderImage(client);
        }

        return image.map(ImageIcon::new).orElse(defaultIcon);
    }

    private Optional<BufferedImage> getClientImage(String imageName) {
        return Optional.ofNullable(getResource(imageName, 64, 64));
    }

    private Optional<BufferedImage> getPlaceHolderImage(Client client) {
        ObjectPluginPlaceholder obj = (ObjectPluginPlaceholder) client;
        String icon = obj.getObject().getPojo().getRepresentations().get(0).getIcon();

        if (icon == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(getResource(icon, 64, 64));
    }

    class CustomCellRenderer implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = (Component) value;
            component.setBackground(isSelected ? list.getSelectionBackground() : getListBackground(list, index));
            component.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            return component;
        }

        private Color getListBackground(JList list, int index) {
            if ((index % 2) == 0) {
                return Color.decode("#f7f7f7");
            } else {
                return list.getBackground();
            }
        }
    }
}
