/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.i2c;

/**
 *
 * @author matteo
 */
public class Board {

    private String alias;
    private int address;
    private int devNum;
    private int lineStatus[];

    public Board(int address, int devNum, String alias) {
        this.address = address;
        this.alias = alias;
        this.devNum = devNum;
        lineStatus = new int[devNum];
    }

    public int getLineStatus(int line) {
        return lineStatus[line];
    }

    // return 1 if old valu and new value differ
    public boolean setLineStatus(int line, int val) {
        int oldval = lineStatus[line];
        lineStatus[line] = val;
        return (oldval != val);
    }

    public void setStatus(byte globVal) {
        int val = 0;
        for (int i = 0; i < 8; i++) {
            val = (globVal >> i) & 0x0001; //extract i-pos bit of byte
            setLineStatus(i, val);
        }

    }

    public int getAddress() {
        return address;
    }

    public String getAlias() {
        return alias;
    }

    public byte toBeWritten(int line, int val) {
        int output = 0;
        for (int i = 0; i < 8; i++) {
            int locval = (line == i) ? ((val == -1) ? 1 - lineStatus[i] : val) : lineStatus[i];
            output += (locval & 0x0001) << i;
        }
        return (byte) (output & 0x07);
    }
}
