package it.freedomotic.gwtclient.client.widgets;

import it.freedomotic.gwtclient.client.api.EnvironmentController;
import it.freedomotic.model.object.Behavior;
import it.freedomotic.model.object.BooleanBehavior;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.model.object.ListBehavior;
import it.freedomotic.model.object.PropertiesBehavior;
import it.freedomotic.model.object.RangedIntBehavior;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;
import com.kiouri.sliderbar.client.solution.iph.IpSliderBar51;
import com.kiouri.sliderbar.client.solution.simplehorizontal.SliderBarSimpleHorizontal;

public class EnvObjectWidget extends Composite {

	//Widget to show the contextual menu of the object behaviors
	public EnvObjectWidget(final EnvObject obj) {
		
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setStyleName("gwt-BehaviorsPopupPanel");
		verticalPanel.setWidth("100%");
		initWidget(verticalPanel);
		Label objectName = new Label(obj.getName());
		objectName.setStyleName("gwt-BehaviorsPopupPanel");
		objectName.addStyleName("ObjectName");
		verticalPanel.add(objectName);
		
		
		HorizontalPanel horizontalPanel;
		Label lbl;
		//TODO: create widgets (like BooleanBehaviorWidget, RangedIntBehaviorWidget...) for every behavior. So they could be easily configured 
		for (final Behavior b: obj.getBehaviors())
		{
			//TODO: handle PropertiesBehavior properly
			if (!(b instanceof PropertiesBehavior))
			{
				//horizontalPanel= new HorizontalPanel();				
				lbl = new Label(b.getName());
				lbl.setStyleName("gwt-BehaviorsPopupPanel");
				verticalPanel.add(lbl);
			}
			if (b instanceof BooleanBehavior)
			{				
				BooleanBehavior bb = (BooleanBehavior)b;				
				final IpSliderBar51 ipSliderBar51OnOff = new IpSliderBar51("Off","On");
				verticalPanel.add(ipSliderBar51OnOff);
				ipSliderBar51OnOff.setValue(bb.getValue()?1:0);
				ipSliderBar51OnOff.setStyleName("gwt-BehaviorsPopupPanel");
				ipSliderBar51OnOff.addStyleName("widget");
				ipSliderBar51OnOff.addBarValueChangedHandler(new BarValueChangedHandler() {					
					@Override
					public void onBarValueChanged(BarValueChangedEvent event) {
						// TODO Auto-generated method stub
						if (((BooleanBehavior) b).getValue()!= Boolean.valueOf(ipSliderBar51OnOff.getValue()!=0))
							EnvironmentController.changeBehavior(obj.getName(), b.getName(), Boolean.valueOf(ipSliderBar51OnOff.getValue()!=0).toString());
					}
				});		
		
				//verticalPanel.add(horizontalPanel);
			}
			else if(b instanceof RangedIntBehavior)
			{
				final RangedIntBehavior rb = (RangedIntBehavior) b;
				final SliderBarSimpleHorizontal slide = new SliderBarSimpleHorizontal(rb.getMax()-rb.getMin(), "100%", true);
				verticalPanel.add(slide);				
				slide.setValue(rb.getValue()-rb.getMin());				
				slide.setStyleName("gwt-BehaviorsPopupPanel");
				slide.addStyleName("widget");
				slide.addBarValueChangedHandler(new BarValueChangedHandler() {
					
					@Override
					public void onBarValueChanged(BarValueChangedEvent event) {
						//((RangedIntBehavior) b).setValue(slide.getValue()+rb.getMin());
						if (((RangedIntBehavior) b).getValue()!= (slide.getValue()+rb.getMin()))
							EnvironmentController.changeBehavior(obj.getName(), b.getName(), String.valueOf((slide.getValue()+rb.getMin())));	
					}
				});
				//verticalPanel.add(slide);	
			}
			else if(b instanceof ListBehavior)
			{
				final ListBehavior lb = (ListBehavior) b;
				final ListBox comboBox = new ListBox();
				comboBox.setStyleName("gwt-BehaviorsPopupPanel");
				comboBox.addStyleName("widget");
				for (String listValue : lb.getList()) {
                    comboBox.addItem(listValue);
                }
				verticalPanel.add(comboBox);				
                comboBox.setSelectedIndex(lb.indexOfSelection());
				comboBox.addChangeHandler(new ChangeHandler() {
					
					@Override
					public void onChange(ChangeEvent event) {
						//lb.setSelected(lb.get(comboBox.getSelectedIndex()).toString());
						if (lb.indexOfSelection()!=comboBox.getSelectedIndex())
							EnvironmentController.changeBehavior(obj.getName(), b.getName(), lb.get(comboBox.getSelectedIndex()).toString()); 					
					}
				});
				
								
			}
			//verticalPanel.add(horizontalPanel);				
		}				
		
	}

}
