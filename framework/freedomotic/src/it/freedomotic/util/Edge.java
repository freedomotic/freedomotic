
package it.freedomotic.util;

/**
 * Title:        Graph
 * Description:  Prova di implementazione di un struttura dati per rappresentare un grafo.
 * Copyright:    Copyright (c) 2000
 * Company:      Università degli Studi di Verona
 * @author Roberto Posenato
 * @version 1.0
 */

 /**
  * La classe arco serve per rappresentare un arco del grafo
  */
public class Edge implements Comparable {

  Object x, y;
  Object value;
  public boolean visitato;

  public Edge() {
    x = y = null;
    value = null;
  }

  public Edge(Object x1, Object y1, Object v) {
    x = x1;
    y = y1;
    value = v;
  }

  public Object getX() { return x; }
  public Object getY() { return y; }
  public Object getValue() { return value; }

  public boolean equals(Edge a) {
    return (x.equals(a.x) && y.equals(a.y) && value.equals(a.value));
  }

    @Override
  public int hashCode() {
    return x.hashCode()+y.hashCode()+value.hashCode();
  }

    @Override
  public String toString() {
    return "("+x.toString()+", "+y.toString()+", "+value.toString()+")";
  }

    @Override
  public boolean equals(Object x) {
    if (x.getClass()==this.getClass())
      return this.equals((Edge) x);
    else
      return false;
  }

    @Override
  public int compareTo(Object a) {
    int i = ((Comparable) value).compareTo(((Edge)a).value);
    if (i==0) {
      int j = ((Comparable) x).compareTo(((Edge)a).x);
      if (j == 0)
        return ((Comparable) y).compareTo(((Edge)a).y);
      else
        return j;
    } else {
      return i;
    }
  }
}
