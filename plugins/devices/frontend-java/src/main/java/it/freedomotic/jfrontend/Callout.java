/*Copyright 2009 Enrico Nicoletti
 * eMail: enrico.nicoletti84@gmail.com
 *
 * This file is part of Freedomotic.
 *
 * Freedomotic is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Freedomotic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedom; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.jfrontend;

import java.awt.Color;
import java.awt.Point;

/**
 *
 * @author Enrico Nicoletti (enrico.nicoletti84@gmail.com)
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

    public Callout(String relatedObject, String group, String text, int x, int y, float angle, int duration) {
        this.text = text;
        this.position = new Point(x, y);

        if (duration <= 0) { //autodetect the durantion based on string length
            this.duration = 45 * text.length();
        } else {
            this.duration = duration;
        }

        this.rotation = angle;
        timestamp = System.currentTimeMillis();
        this.relatedTo = relatedObject;
        this.group = group;
    }

    public Callout(String text, int duration, Color color) {
        this.text = text;
        this.position = new Point(50, 50);

        if (duration <= 0) { //autodetect the durantion based on string length
            this.duration = 45 * text.length();
        } else {
            this.duration = duration;
        }

        this.rotation = 0.0f;
        timestamp = System.currentTimeMillis();
        this.color = color;
        this.group = "info";
    }

    private String format(String text) {
        text =
                text + "\n" + text.substring(text.length() / 2,
                text.length());

        return text;
    }

    public int getDuration() {
        return duration;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
    }

    public void setDuration(int value) {
        this.duration = value;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getRelated() {
        return relatedTo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }
}
