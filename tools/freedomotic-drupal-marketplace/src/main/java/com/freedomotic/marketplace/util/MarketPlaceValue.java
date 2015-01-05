/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.marketplace.util;

/**
 *
 * @author gpt
 */
public class MarketPlaceValue {

    private String value;

    public String formatValue() {
        return "{\"value\":\"" + value + "\"}";
    }

    public String formatValueAsListElement() {
        if (value.equals("null")) {
            return "";
        } else {
            return "\"" + value + "\":\"" + value + "\"";
        }
    }

    public String getValue() {
        return value;
    }
}
