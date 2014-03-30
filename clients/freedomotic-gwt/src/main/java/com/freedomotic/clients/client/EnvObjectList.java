package com.freedomotic.clients.client;


import com.freedomotic.clients.client.widgets.RangeLabelPager;
import com.freedomotic.clients.client.widgets.ShowMorePagerPanel;
import com.freedomotic.model.object.EnvObject;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class EnvObjectList extends Composite implements
        HasValueChangeHandlers<String> {

    private static EnvObjectListUiBinder uiBinder = GWT
            .create(EnvObjectListUiBinder.class);

    interface EnvObjectListUiBinder extends UiBinder<Widget, EnvObjectList> {
    }
    @UiField
    ShowMorePagerPanel pagerPanel;
    @UiField
    RangeLabelPager rangeLabelPager;
    private CellList<EnvObject> cellList;

    /**
     * The images used for this example.
     */
    static interface Images extends ClientBundle {

        @Source("resources/object.png")
        ImageResource object();
    }

    public EnvObjectList() {
        Images images = GWT.create(Images.class);
        EnvObjectCell envObjectCell = new EnvObjectCell(images.object());

        // Set a key provider that provides a unique key for each contact. If key is
        // used to identify contacts when fields (such as the name and address)
        // change.
        cellList = new CellList<EnvObject>(envObjectCell);
        FreedomoticObjectsController.getInstance().addDataDisplay(cellList);
        //List<EnvObject> values = FreedomoticController.getInstance().getObjects();
        //cellList.setRowData(values);
        cellList.setPageSize(30);
        cellList.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
        cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

        // Add a selection model so we can select cells.
        final SingleSelectionModel<EnvObject> selectionModel = new SingleSelectionModel<EnvObject>();
        cellList.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                //contactForm.setContact(selectionModel.getSelectedObject());
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
        // Set the cellList as the display of the pagers. This example has two
        // pagers. pagerPanel is a scrollable pager that extends the range when the
        // user scrolls to the bottom. rangeLabelPager is a pager that displays the
        // current range, but does not have any controls to change the range.
        pagerPanel.setDisplay(cellList);
        rangeLabelPager.setDisplay(cellList);


    }

    public EnvObjectList(String firstName) {
        //initWidget(uiBinder.createAndBindUi(this));
//		button.setText(firstName);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<String> handler) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * The Cell used to render a {@link EnvObject}.
     */
    static class EnvObjectCell extends AbstractCell<EnvObject> {

        /**
         * The html of the image used for Objects.
         */
        private final String imageHtml;

        public EnvObjectCell(ImageResource image) {
            this.imageHtml = AbstractImagePrototype.create(image).getHTML();
        }

        @Override
        public void render(Context context, EnvObject value, SafeHtmlBuilder sb) {
            // Value can be null, so do a null check..
            if (value == null) {
                return;
            }

            sb.appendHtmlConstant("<table>");

            // Add the contact image.
            sb.appendHtmlConstant("<tr><td rowspan='3'>");
            sb.appendHtmlConstant(imageHtml);
            sb.appendHtmlConstant("</td>");

            // Add the name.
            sb.appendHtmlConstant("<td style='font-size:95%;'>");
            sb.appendEscaped(value.getName());
            sb.appendHtmlConstant("</td></tr></table>");
//	      		"<tr><td>");
//	      sb.appendEscaped(value.getAddress());
//	      sb.appendHtmlConstant("</td></tr></table>");
        }
    }
}
