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

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class RangedIntBehavior
        extends Behavior {

    private static final long serialVersionUID = 6390384029652176632L;

    private int value;
    private int max;
    private int min;
    private int scale;
    private int step;

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        if (scale == 1) {
            return String.valueOf(value);
        }

        return new Double((double) value / (double) getScale()).toString();
    }

    /**
     *
     * @return
     */
    public int getValue() {
        return value;
    }

    /**
     *
     * @return
     */
    public int getStep() {
        return step;
    }

    /**
     *
     * @return
     */
    public int getMax() {
        return max;
    }

    /**
     *
     * @return
     */
    public int getMin() {
        return min;
    }

    /**
     *
     * @return
     */
    public int getScale() {
        if (scale <= 0) {
            setScale(1);
        }

        return scale;
    }

    /**
     *
     * @param inputValue
     */
    public void setValue(int inputValue) {
        //activate this behavior if it was unactivated
        this.setActive(true);
        value = inputValue;
    }

    /**
     * @param max the max to set
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * @param min the min to set
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(int scale) {
        this.scale = scale;
    }

    /**
     * @param step the step to set
     */
    public void setStep(int step) {
        this.step = step;
    }
}
