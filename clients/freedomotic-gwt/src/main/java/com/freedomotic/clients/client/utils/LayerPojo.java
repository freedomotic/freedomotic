package com.freedomotic.clients.client.utils;

import java.io.Serializable;

/**
 * Created by gpt on 4/04/14.
 */
public class LayerPojo {

    private final String UUID;
    private String mName;
    private boolean mVisible = false;

    public LayerPojo(String UUID)
    {
        this.UUID =UUID;
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUUID() {
        return UUID;
    }
}
