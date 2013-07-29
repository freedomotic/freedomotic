/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

/**
 *
 * @author Enrico
 */
public class Coordinate {

    private int id;
    private int x;
    private int y;
    private int time;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getTime() {
        return time;
    }

    public int getId() {
        return id;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setId(int id) {
        this.id = id;
    }
}
