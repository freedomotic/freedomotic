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
package com.freedomotic.model.object;

import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.model.geometry.FreedomShape;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Enrico
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Representation
        implements Serializable {

    private static final long serialVersionUID = 5024359210563760316L;

    private boolean tangible;
    private boolean intersecable;
    private FreedomPoint offset;
    private double rotation;
    private String icon;
    private String fillColor;
    private String textColor;
    private String borderColor;
    private double scaleX;
    private double scaleY;
    private FreedomShape shape;

    /**
     *
     * @return
     */
    public boolean isIntersecable() {
        return intersecable;
    }

    /**
     *
     * @param intersecable
     */
    public void setIntersecable(boolean intersecable) {
        this.intersecable = intersecable;
    }

    /**
     *
     * @return
     */
    public double getScaleX() {
        return Math.max(0.1, scaleX);
    }

    /**
     *
     * @param scaleX
     */
    public void setScaleX(double scaleX) {
        this.scaleX = Math.max(0.1, scaleX);
    }

    /**
     *
     * @return
     */
    public double getScaleY() {
        return Math.max(0.1, scaleY);
    }

    /**
     *
     * @param scaleY
     */
    public void setScaleY(double scaleY) {
        this.scaleY = Math.max(0.1, scaleY);
    }

    /**
     *
     * @param value
     */
    public void setTangible(boolean value) {
        tangible = value;
    }

    /**
     *
     * @return
     */
    public boolean isTangible() {
        return tangible;
    }

    /**
     *
     * @param icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     *
     * @return
     */
    public String getIcon() {
        return icon;
    }

    /**
     *
     * @param offset
     */
    public void setOffset(FreedomPoint offset) {
        this.offset = offset;
    }

    /**
     *
     * @param rotation
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    /**
     *
     * @param shape
     */
    public void setShape(FreedomShape shape) {
        this.shape = shape;
    }

    /**
     *
     * @param fillColor
     */
    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    /**
     *
     * @param stringColor
     */
    public void setTextColor(String stringColor) {
        this.textColor = stringColor;
    }

    /**
     *
     * @param borderColor
     */
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    /**
     *
     * @return
     */
    public FreedomShape getShape() {
        return shape;
    }

    /**
     *
     * @return
     */
    public FreedomPoint getOffset() {
        return this.offset;
    }

    /**
     *
     * @param x
     * @param y
     */
    public void setOffset(int x, int y) {
        setOffset(new FreedomPoint(x, y));
    }

    /**
     *
     * @return
     */
    public double getRotation() {
        return this.rotation;
    }

    /**
     *
     * @return
     */
    @XmlTransient
    public boolean isFilled() {
        if (fillColor != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    @XmlTransient
    public boolean isBordered() {
        if (borderColor != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public String getFillColor() {
        return this.fillColor;
    }

    /**
     *
     * @return
     */
    public String getTextColor() {
        return this.textColor;
    }

    /**
     *
     * @return
     */
    public String getBorderColor() {
        return this.borderColor;
    }
}
