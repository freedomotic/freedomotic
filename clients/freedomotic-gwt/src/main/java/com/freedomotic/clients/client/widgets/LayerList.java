package com.freedomotic.clients.client.widgets;

import com.freedomotic.clients.client.resources.MyCellListResources;
import com.freedomotic.clients.client.widgets.celltypes.LayerCell;
import com.freedomotic.clients.client.utils.EnvironmentWidget;
import com.freedomotic.clients.client.utils.LayerPojo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;


import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import java.util.List;

/**
 * Created by gpt on 3/04/14.
 */
public class LayerList extends Composite {

    public void setAssociatedEnvironment(EnvironmentWidget associatedEnvironment) {
        this.associatedEnvironment = associatedEnvironment;
    }

    interface LayerListUiBinder extends UiBinder<HTMLPanel, LayerList> {
    }

    private static LayerListUiBinder ourUiBinder = GWT.create(LayerListUiBinder.class);

    @UiField(provided = true)
    CellList<LayerPojo> layerList;

    ListDataProvider<LayerPojo> dataProviderList;

    //TODO: this should be ExtendedCanvas. The layerlist should not be aware of the "bussines" of the canvas
    private EnvironmentWidget associatedEnvironment;

    final SingleSelectionModel<LayerPojo> selectionModel;

    @UiConstructor
    public LayerList() {
        // Define a key provider for a Contact. We use the unique ID as the key,
        // which allows to maintain selection even if the name changes.
        ProvidesKey keyProvider = new ProvidesKey<LayerPojo>() {
            @Override
            public Object getKey(LayerPojo layerPojo) {
                return (layerPojo == null) ? null : layerPojo.getName();
            }
        };

        //TODO: The custom style css doesn't work if the selectionmodel is used!!!
        MyCellListResources.INSTANCE.cellListStyle().ensureInjected();
        layerList =  new CellList<LayerPojo>(new LayerCell(), MyCellListResources.INSTANCE, keyProvider);


        selectionModel = new SingleSelectionModel<LayerPojo>(keyProvider);

        layerList.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler(){
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                if (associatedEnvironment != null)
                {
                    //TODO: this is "wrong". The layerlist should not now about environments. Find a way to wire better
                    LayerPojo selected = selectionModel.getSelectedObject();
                    if (selected != null) {
                        associatedEnvironment.setEnvironment(selected.getUUID());
                    }

                }
            }
        });


        HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
        dataProviderList = new ListDataProvider<>();
        dataProviderList.addDataDisplay(layerList);

    }

    public void populateData(List<LayerPojo> layers)
    {
        //TODO: This is being called twice on load. Review if all is right
        List<LayerPojo> data = dataProviderList.getList();
        data.clear();
        data.addAll(layers);
        dataProviderList.refresh();
        selectionModel.setSelected(data.get(0), true);

    }


}