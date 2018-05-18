/**
 *
 * Copyright (c) 2009-2018 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.mqttbroker;

/**
 *
 * @author Mauro Cicolella
 */
public final class MqttTopic {

    String topicPath = null;
    String fieldsDelimiter = null;
    Integer numberOfFields = 0;

    MqttTopic(String topicPath, Integer numberOfFields, String fieldsDelimiter) {
        setTopicPath(topicPath);
        setNumberOfFields(numberOfFields);
        setFieldsDelimiter(fieldsDelimiter);
    }

    public void setTopicPath(String topicPath) {

        this.topicPath = topicPath;
    }

    public void setNumberOfFields(Integer numberOfFields) {

        this.numberOfFields = numberOfFields;
    }

    public void setFieldsDelimiter(String fieldsDelimiter) {

        this.fieldsDelimiter = fieldsDelimiter;
    }
    
     public String getTopicPath() {

        return(topicPath);
    }

    public Integer getNumberOfFields() {

        return(numberOfFields);
    }

    public String getFieldsDelimiter() {

        return(fieldsDelimiter);
    }

}
