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
package com.freedomotic.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: Graph Description: Prova di implementazione di un struttura dati per
 * rappresentare un grafo. Copyright: Copyright (c) 2000 Company: Università
 * degli Studi di Verona
 *
 * @author Roberto Posenato
 * @version 1.0
 */
/**
 * La classe arco serve per rappresentare un arco del grafo
 */
class GraphEdge implements Comparable<GraphEdge> {

    private static final Logger LOG = LoggerFactory.getLogger(GraphEdge.class.getName());
    Object x, y;
    Object value;

    /**
     *
     */
    public boolean visitato;

    /**
     *
     */
    public GraphEdge() {
        x = y = null;
        value = null;
    }

    /**
     *
     * @param x1
     * @param y1
     * @param v
     */
    public GraphEdge(Object x1, Object y1, Object v) {
        x = x1;
        y = y1;
        value = v;
    }

    /**
     *
     * @return
     */
    public Object getX() {
        return x;
    }

    /**
     *
     * @return
     */
    public Object getY() {
        return y;
    }

    /**
     *
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     *
     * @param a
     * @return
     */
    public boolean equals(GraphEdge a) {
        return (x.equals(a.x) && y.equals(a.y) && value.equals(a.value));
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return x.hashCode() + y.hashCode() + value.hashCode();
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "(" + x.toString() + ", " + y.toString() + ", " + value.toString() + ")";
    }

    /**
     *
     * @param x
     * @return
     */
    @Override
    public boolean equals(Object x) {
        if (x.getClass() == this.getClass()) {
            return this.equals((GraphEdge) x);
        } else {
            return false;
        }
    }

    /**
     *
     * @param a
     * @return
     */
    @Override
    public int compareTo(GraphEdge a) {
        int i = ((Comparable<Object>) value).compareTo(a.value);
        if (i == 0) {
            int j = ((Comparable<Object>) x).compareTo(a.x);
            if (j == 0) {
                return ((Comparable<Object>) y).compareTo(a.y);
            } else {
                return j;
            }
        } else {
            return i;
        }
    }
}
