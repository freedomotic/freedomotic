/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package it.freedomotic.model.object;

import it.freedomotic.model.geometry.FreedomPoint;
import it.freedomotic.model.geometry.FreedomShape;

import java.io.Serializable;

/**
 *
 * @author Enrico
 */
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

    public boolean isIntersecable() {
        return intersecable;
    }

    public void setIntersecable(boolean intersecable) {
        this.intersecable = intersecable;
    }

    public double getScaleX() {
        return Math.max(0.1, scaleX);
    }

    public void setScaleX(double scaleX) {
        this.scaleX = Math.max(0.1, scaleX);
    }

    public double getScaleY() {
        return Math.max(0.1, scaleY);
    }

    public void setScaleY(double scaleY) {
        this.scaleY = Math.max(0.1, scaleY);
    }

    public void setTangible(boolean value) {
        tangible = value;
    }

    public boolean isTangible() {
        return tangible;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public void setOffset(FreedomPoint offset) {
        this.offset = offset;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void setShape(FreedomShape shape) {
        this.shape = shape;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public void setTextColor(String stringColor) {
        this.textColor = stringColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public FreedomShape getShape() {
        return shape;
    }

    public FreedomPoint getOffset() {
        return this.offset;
    }

    public void setOffset(int x, int y) {
        setOffset(new FreedomPoint(x, y));
    }

    public double getRotation() {
        return this.rotation;
    }

    public boolean isFilled() {
        if (fillColor != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isBordered() {
        if (borderColor != null) {
            return true;
        } else {
            return false;
        }
    }

    public String getFillColor() {
        return this.fillColor;
    }

    public String getTextColor() {
        return this.textColor;
    }

    public String getBorderColor() {
        return this.borderColor;
    }
}
