/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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
package com.freedomotic.marketplace.util;

import com.freedomotic.marketplace.IPluginPackage;

import javax.swing.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * New version of the POJO that represents a plugin.
 *
 * @author Gabriel Pulido de Torres
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MarketPlacePlugin2 implements IPluginPackage {

    private String nid;
    private String title;
    private String description;
    private String body;
    private String teaser;
    private ArrayList<MarketPlaceUser> field_developer;
    private ArrayList<MarketPlaceValue> field_status;
    private ArrayList<MarketPlaceValue> field_description;
    private ArrayList<MarketPlaceValue> field_category;
    private ArrayList<MarketPlaceValue> field_plugin_category;
    private ArrayList<MarketPlaceFile> field_icon;
    private ArrayList<MarketPlaceValue> field_os;
    private ArrayList<MarketPlaceFile> field_file;
    private String uri;
    private String type;

    @XmlTransient
    private transient ImageIcon icon;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getURI() {
        return "http://www.www.freedomotic-iot.com/node/" + nid;
    }

    /**
     * @deprecated
     * @return the path
     */
    @Override
    @Deprecated
    @XmlTransient
    public String getFilePath() {
        if (field_file != null && !field_file.isEmpty() && field_file.get(0) != null) {
            return field_file.get(0).getFilepath();
        }
        return "";
    }

    @Override
    public String getFilePath(String version) {
        if (field_file != null) {
            for (MarketPlaceFile marketPlaceFile : field_file) {
                //TODO: change for a regular expression to match the version
                if (marketPlaceFile != null
                        && marketPlaceFile.getFilename() != null
                        && marketPlaceFile.getFilename().contains(version)) {
                    //freedomotic website link will be something like that
                    //http://www.freedomotic-iot.com/sites/all/modules/pubdlcnt/pubdlcnt.php?file=http://www.freedomotic-iot.com/sites/default/files/com.freedomotic.mailer-5.4.x-1.6.device&nid=1197
                    return "http://www.www.freedomotic-iot.com/sites/all/modules/pubdlcnt/pubdlcnt.php?file="
                            + marketPlaceFile.getFilepath()
                            + "&nid=671";
                }
            }
        }
        return "";
    }

    /**
     * Get the file index by a version
     * @return
     */
    public int getFileIndexByVersion(String version) {
        int i = -1;
        if (field_file != null) {
            for (MarketPlaceFile marketPlaceFile : field_file) {
                i++;
                //TODO: change for a regular expression to match the version
                if (marketPlaceFile.getFilename().contains(version)) {
                    return i;
                }
            }
        }
        return i + 1;
    }

    @XmlTransient
    public int getFileCount() {
        return field_file.size();
    }

    /**
     * Return the field file list
     * @return
     */
    public List<MarketPlaceFile> getFiles() {
        if (field_file == null) {
            return new ArrayList<>();
        } else {
            field_file.removeAll(Collections.singleton(null));
            return field_file;
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return field_description.get(0).getValue();
    }

    @Override
    @XmlTransient
    public ImageIcon getIcon() {
        if (icon == null) {
            if (field_icon != null
                    && !field_icon.isEmpty()
                    && field_icon.get(0) != null) {
                icon = DrupalRestHelper.retrieveImageIcon("/" + field_icon.get(0).getRelativeFilepath());
            } else {
                icon = DrupalRestHelper.defaultIconImage;
            }
        }
        return icon;
    }

    /**
     * Adds a file to the plugin, taking into account the core versions and name
     */
    public void addFile(MarketPlaceFile file) {
        String version = extractCorePluginVersion(file.getFilename());
        boolean found = false;
        //check if the plugin has a file with that version
        for (MarketPlaceFile pluginFile : getFiles()) {
            if (pluginFile.getFilename() != null
                    && extractCorePluginVersion(pluginFile.getFilename()).equals(version)) {
                int index = field_file.indexOf(pluginFile);
                //Substitute the file
                //TODO: decide what to do with the old files
                field_file.set(index, file);
                found = true;
            }
        }
        if (!found) {
            //Two posibilities. The name is not right or it is a new version.
            //We guess that is for a new version.
            //TODO: handle the case of not right.
            // We add the file at the end of the list
            field_file.add(file);
        }

    }

    /**
     * Return the base data
     * @return
     */
    public String formatBaseData() {
        return "\"type\":\"" + type + "\","
                + "\"language\":\"und\"";
    }

    /**
     * Return the field category in the JSON format
     * @return
     */
    public String formatFieldCategory() {
        StringBuilder jsonString = new StringBuilder();
        for (int i = 0; i < field_category.size(); i++) {
            if (i == 0) {
                jsonString.append("\"field_category\":{");
            }
            jsonString.append("\"" + i + "\":" + field_category.get(i).formatValue());
            if (i != field_category.size() - 1) {
                jsonString.append(",");
            }
            if (i == field_category.size() - 1) {
                jsonString.append("}");
            }
        }
        return jsonString.toString();
    }

    /**
     * Return the field plugin in the JSON format
     * @return
     */
    public String formatFieldPluginCategory() {
        StringBuilder jsonString = new StringBuilder();
        //we are assuming that the Plugin is well formed
        //(ie, has at least one correct Plugin category)
        jsonString.append("\"field_plugin_category\":{\"value\":{");
        boolean first = true;
        for (MarketPlaceValue value : field_plugin_category) {

            if (value.getValue() != null) {
                if (!first) {
                    jsonString.append(",");
                }
                jsonString.append(value.formatValueAsListElement());
                first = false;
            }
        }

        jsonString.append("}}");
        return jsonString.toString();
    }

    /**
     * Return the field OS in the JSON format
     * @return
     */
    public String formatFieldOS() {
        StringBuilder jsonString = new StringBuilder();
        String s = null;
        for (int i = 0; i < field_os.size(); i++) {
            if (i == 0) {
                jsonString.append("\"field_os\":{\"value\":{");
            }
            s = field_os.get(i).getValue();
            jsonString.append("\"" + s + "\":\"" + s + "\"");
            if (i != field_os.size() - 1) {
                jsonString.append(",");
            }
            if (i == field_os.size() - 1) {
                jsonString.append("}}");
            }
        }

        return jsonString.toString();
    }

    /**
     * Return the field file in the JSON format
     * @return
     */
    public String formatFieldFile() {
        StringBuilder jsonString = new StringBuilder();
        List<MarketPlaceFile> files = getFiles();
        for (int i = 0; i < files.size(); i++) {
            MarketPlaceFile pluginFile = files.get(i);
            pluginFile.setDescription(extractVersion(pluginFile.getFilename()));
            if (i == 0) {
                jsonString.append("\"field_file\":{");
            }
            jsonString.append("\"" + i + "\":{" + pluginFile.formatFile() + "}");
            if (i != files.size() - 1) {
                jsonString.append(",");
            }
            if (i == files.size() - 1) {
                jsonString.append("}");
            }
        }
        return jsonString.toString();
    }

    /**
     * Return the core plugin version part of the filename
     * @param filename
     * @return
     */
    public static String extractCorePluginVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x is needed
        //remove extension
        filename = filename.substring(0, filename.lastIndexOf('.'));
        String[] tokens = filename.split("-");
        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1];
        } else {
            return filename;
        }
    }

    /**
     * Return the version part of the filename
     * @param filename
     * @return
     */
    public static String extractVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x-1.212 is needed
        //remove extension
        filename = filename.substring(0, filename.lastIndexOf('.'));
        String[] tokens = filename.split("-");
        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1] + "-" + tokens[2];
        } else {
            return filename;
        }
    }

    /**
     * Return the Icons
     * @return
     */
    public List<MarketPlaceFile> getIcons() {
        if (field_icon == null) {
            return new ArrayList<>();
        } else {
            field_icon.removeAll(Collections.singleton(null));
            return field_icon;
        }
    }
}
