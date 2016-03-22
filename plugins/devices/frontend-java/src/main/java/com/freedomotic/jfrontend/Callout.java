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

import java.awt.Color;
import java.awt.Point;

/**
 *
 * @author Enrico Nicoletti
 */
public class Callout {

    private String text;
    private Point position;
    private int duration;
    private float rotation;
    private long timestamp;
    private String relatedTo;
    private String group;
    private Color color = Color.black;

    /**
     *
     * @param relatedObject
     * @param group
     * @param text
     * @param x
     * @param y
     * @param angle
     * @param duration
     */
    public Callout(String relatedObject, String group, String text, int x, int y, float angle, int duration) {
        this.text = text;
        this.position = new Point(x, y);

        if (duration <= 0) { //autodetect the durantion based on string length
            this.duration = 60 * text.length();
        } else {
            this.duration = duration;
        }

        this.rotation = angle;
        timestamp = System.currentTimeMillis();
        this.relatedTo = relatedObject;
        this.group = group;
    }

    /**
     *
     * @param text
     * @param duration
     * @param color
     */
    public Callout(String text, int duration, Color color) {
        this.text = text;
        this.position = new Point(50, 50);

        if (duration <= 0) { //autodetect the durantion based on string length
            this.duration = 100 * text.length();
        } else {
            this.duration = duration;
        }

        this.rotation = 0.0f;
        timestamp = System.currentTimeMillis();
        this.color = color;
        this.group = "info";
    }

    private String format(String text) {
        text
                = text + "\n" + text.substring(text.length() / 2,
                        text.length());

        return text;
    }

    /**
     *
     * @return
     */
    public int getDuration() {
        return duration;
    }

    /**
     *
     * @return
     */
    public String getGroup() {
        return group;
    }

    /**
     *
     * @param group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     *
     * @return
     */
    public String getRelatedTo() {
        return relatedTo;
    }

    /**
     *
     * @param relatedTo
     */
    public void setRelatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
    }

    /**
     *
     * @param value
     */
    public void setDuration(int value) {
        this.duration = value;
    }

    /**
     *
     * @return
     */
    public Point getPosition() {
        return position;
    }

    /**
     *
     * @param position
     */
    public void setPosition(Point position) {
        this.position = position;
    }

    /**
     *
     * @return
     */
    public float getRotation() {
        return rotation;
    }

    /**
     *
     * @param rotation
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     *
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     *
     * @return
     */
    public Object getRelated() {
        return relatedTo;
    }

    /**
     *
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     *
     * @return
     */
    public Color getColor() {
        return color;
    }

    void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }
}
