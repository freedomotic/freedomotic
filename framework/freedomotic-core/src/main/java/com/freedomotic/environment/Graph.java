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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
 * La classe Grafo rappresenta un grafo mediante liste di adiacenza. In
 * particolare si è voluto dare un'implementazione che utilizzasse classi
 * standard di java.util. Di conseguenza: 1. la lista dei nodi è rappresentata
 * da una HashMap per poter accedere al nodo x in tempo costante 2. la lista dei
 * nodi adiacenti è rappresentata da un HashSet di archi, in modo tale da poter
 * verificare/accedere al nodo adiacente in tempo costante. Anziché
 * rappresentare il nodo adiacente e il peso dell'arco si è preferito
 * rappresentare l'arco completo per questioni di efficineza di altre
 * operazioni.
 *
 */
class Graph {

    private static final Logger LOG = LoggerFactory.getLogger(Graph.class.getName());
    HashMap nodi;
    int nArchi;

    /**
     *
     */
    public Graph() {
        nodi = new HashMap();
        nArchi = 0;
    }

    /**
     *
     * @return
     */
    public int nodesNumber() {
        return nodi.size();
    }

    /**
     *
     * @return
     */
    public int edgesNumber() {
        return nArchi;
    }

    /**
     * add(x) aggiunge un nodo al grafo con valore x se non esiste, nulla
     * altrimenti L'aggiunta di un nodo significa aggiungere la coppia (x,
     * lista) nella HashMap dove lista è una HashSet nuovo vuoto.
     *
     * @param x
     */
    public void add(Object x) {
        if (!nodi.containsKey(x)) {
            HashSet lista = new HashSet();
            nodi.put(x, lista);
        }
    }

    /**
     *
     * @param x
     */
    public void remove(Object x) {
        if (nodi.containsKey(x)) {
            Iterator arcoIncidenteI = ((HashSet) nodi.get(x)).iterator();
            GraphEdge a;
            Object y;

            while (arcoIncidenteI.hasNext()) {
                a = (GraphEdge) arcoIncidenteI.next();
                y = (a.x.equals(x)) ? a.y : a.x;

                if (((HashSet) nodi.get(y)).remove(a)) {
                    nArchi--;
                }
            }

            nodi.remove(x);
        }
    }

    /**
     * add(x,y,v) aggiunge un arco tra i nodi x e y con peso v
     *
     * @return
     */
    public boolean add(Object x, Object y, Object value) {
        boolean flag = false;
        boolean flag1 = false;

        if (!nodi.containsKey(x)) {
            add(x);
        }

        if (!nodi.containsKey(y)) {
            add(y);
        }

        GraphEdge a = new GraphEdge(x, y, value);
        flag = ((HashSet) nodi.get(x)).add(a);
        flag1 = ((HashSet) nodi.get(y)).add(a);
        flag = flag && flag1;

        if (flag) {
            nArchi++;
        }

        return flag;
    }

    /**
     *
     * @param a
     * @return
     */
    public boolean add(GraphEdge a) {
        return add(a.x, a.y, a.value);
    }

    /**
     *
     * @param x
     * @param y
     * @param value
     * @return
     */
    public boolean remove(Object x, Object y, Object value) {
        GraphEdge a = new GraphEdge(x, y, value);

        return remove(a);
    }

    /**
     *
     * @param a
     * @return
     */
    public boolean remove(GraphEdge a) {
        boolean flag = false;
        boolean flag1 = false;

        if (nodi.containsKey(a.x) && nodi.containsKey(a.y)) {
            flag = ((HashSet) nodi.get(a.x)).remove(a);
            flag1 = ((HashSet) nodi.get(a.y)).remove(a);
        }

        return flag || flag1;
    }

    /**
     *
     * @return
     */
    public Set getEdgeSet() {
        Set setArchi = new HashSet();
        Iterator hashSetI = nodi.values().iterator();

        while (hashSetI.hasNext()) {
            setArchi.addAll((Set) hashSetI.next());
        }

        return setArchi;
    }

    /**
     *
     * @param nodo
     * @return
     */
    public Set getEdgeSet(Object nodo) {
        if (nodi.containsKey(nodo)) //se il nodo è presente nel grafo
        {
            return (HashSet) nodi.get(nodo);
        } else {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public Set getNodeSet() {
        return nodi.keySet();
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        Object nodo;
        GraphEdge a;
        Iterator arcoI;
        Iterator nodoI = nodi.keySet().iterator();

        while (nodoI.hasNext()) {
            arcoI = ((Set) nodi.get(nodo = nodoI.next())).iterator();
            out.append("Nodo ").append(nodo.toString()).append(": ");

            while (arcoI.hasNext()) {
                a = (GraphEdge) arcoI.next();
                //out.append( ((a.x == nodo ) ? a.y.toString() : a.x.toString()) + "("+a.value.toString()+"), ");
                out.append(a.toString()).append(", ");
            }

            out.append("\n");
        }

        return out.toString();
    }
}
