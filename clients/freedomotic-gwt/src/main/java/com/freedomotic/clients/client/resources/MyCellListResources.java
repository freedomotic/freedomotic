package com.freedomotic.clients.client.resources;

import com.google.gwt.core.client.GWT;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellList.Style;

/**
 * Created by gpt on 6/04/14.
 */
public interface MyCellListResources extends CellList.Resources {

    public MyCellListResources INSTANCE =
            GWT.create(MyCellListResources.class);

    /**
     * The styles used in this widget.
     */
    @Source("MyCellList.css")
    @Override
    public Style cellListStyle();
}
