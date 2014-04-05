package com.freedomotic.clients.client.widgets.celltypes;

import com.freedomotic.clients.client.utils.LayerPojo;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiRenderer;

/**
 * Created by gpt on 4/04/14.
 */
public class LayerCell extends AbstractCell {


    interface MyUiRenderer extends UiRenderer {
        void render(SafeHtmlBuilder sb, String name);
    }
    private static MyUiRenderer renderer = GWT.create(MyUiRenderer.class);

    @UiField
    String name;

    @Override
    public void render(Context context, Object layer, SafeHtmlBuilder safeHtmlBuilder) {
        if (layer != null)
            renderer.render(safeHtmlBuilder, ((LayerPojo) layer).getName());

    }
}