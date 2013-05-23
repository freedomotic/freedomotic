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
package it.freedomotic.model.ds;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class ColorList {

    static ArrayList<Color> colors = new ArrayList<Color>();
    static int last = 0;

    public ColorList() {
        colors.add(Color.red);
        colors.add(Color.green);
        colors.add(Color.blue);
        colors.add(Color.magenta);
        colors.add(Color.orange);
        colors.add(Color.pink);
        colors.add(Color.yellow);
    }

    public static Color getRandom() {
        Random rand = new Random();

        return (new Color(rand.nextInt(256),
                rand.nextInt(256),
                rand.nextInt(256)));
    }

    public static Color getNext() {
        if (last >= colors.size()) {
            last = 0;
        }

        Color c = colors.get(last);
        last++;

        return c;
    }
}
