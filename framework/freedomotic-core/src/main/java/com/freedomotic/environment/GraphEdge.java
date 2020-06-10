/**
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
 * <p>
 * This file is part of Freedomotic
 * <p>
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * <p>
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.environment;

/**
 * Title: Graph Description: Prova di implementazione di un struttura dati per
 * rappresentare un grafo. Copyright: Copyright (c) 2000 Company: Università
 * degli Studi di Verona
 * <p>
 * La classe arco serve per rappresentare un arco del grafo
 *
 * @author Roberto Posenato
 * @version 1.0
 */
class GraphEdge implements Comparable<GraphEdge> {

    private final Object x;
    private final Object y;
    private final Object value;

    /**
     * Creates edge in graph between objects x and y with given value.
     *
     * @param x     First node of the graph edge
     * @param y     Second node of the graph edge
     * @param value Edge's value
     */
    public GraphEdge(Object x, Object y, Object value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    /**
     * Gets the first node in edge
     *
     * @return first node object
     */
    public Object getX() {
        return x;
    }

    /**
     * Gets the second node in edge
     *
     * @return second node object
     */
    public Object getY() {
        return y;
    }

    /**
     * Gets the edge's value
     *
     * @return edge's value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Checks equality between this and the other GraphEdge object.
     *
     * @param other the other graph edge object
     * @return true iff this object equals other
     */
    public boolean equals(GraphEdge other) {
        return (x.equals(other.x) && y.equals(other.y) && value.equals(other.value));
    }

    /**
     * Returns hash code of an object
     *
     * @return integer value with hashcode
     */
    @Override
    public int hashCode() {
        return x.hashCode() + y.hashCode() + value.hashCode();
    }

    /**
     * Returns string representation of GraphEdge object.
     *
     * @return string with GraphEdge representation
     */
    @Override
    public String toString() {
        return "(" + x.toString() + ", " + y.toString() + ", " + value.toString() + ")";
    }

    /**
     * Checks equality between this and other object.
     *
     * @param other the other object
     * @return true iff this object equals other
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other.getClass() == this.getClass()) {
            return this.equals((GraphEdge) other);
        } else {
            return false;
        }
    }

    /**
     * Allows for comparing two GraphEdge objects
     *
     * @param other the other object to compare
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the other object
     */
    @Override
    public int compareTo(GraphEdge other) {
        int i = ((Comparable<Object>) value).compareTo(other.value);
        if (i == 0) {
            int j = ((Comparable<Object>) x).compareTo(other.x);
            if (j == 0) {
                return ((Comparable<Object>) y).compareTo(other.y);
            } else {
                return j;
            }
        } else {
            return i;
        }
    }
}
