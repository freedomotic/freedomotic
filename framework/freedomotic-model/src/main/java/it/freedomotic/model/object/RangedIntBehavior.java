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

/**
 *
 * @author Enrico
 */
public class RangedIntBehavior
        extends Behavior {

    private static final long serialVersionUID = 6390384029652176632L;
	
	private int value;
    private int max;
    private int min;
    private int scale;
    private int step;

    @Override
    public String toString() {
        if (scale == 1) {
            return String.valueOf(value);
        }

        return new Double((double) value / (double) getScale()).toString();
    }

    public int getValue() {
        return value;
    }

    public int getStep() {
        return step;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public int getScale() {
        if (scale <= 0) {
            scale = 1;
        }

        return scale;
    }

    public void setValue(int inputValue) {
        //activate this behavior if it was unactivated
        this.setActive(true);
        value = inputValue;
    }
}
