package it.freedomotic.gwtclient.client;

import it.freedomotic.gwtclient.client.api.FreedomoticStompHelper;
import it.freedomotic.gwtclient.client.api.Payload;
import it.freedomotic.gwtclient.client.api.proxies.ObjectsResourceProxy;
import it.freedomotic.model.object.EnvObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.restlet.client.resource.Result;

import com.google.gwt.core.client.GWT;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;


public class FreedomoticObjectsController {

	public static final int STOMP_ERROR = 0;
	public static final int REST_ERROR = 1;
	public static final int CONNECTED= 2;
	public static final String ROOT_URL= "/gwt_client/v2"; 
	
	private static FreedomoticObjectsController INSTANCE=null;  //Singleton reference
	//private Client stompClient;
	private static ObjectsResourceProxy objectsResource;		
	private List<EnvObject> freedomObjects = new ArrayList<EnvObject>();	
	private HashMap<String,String> freedomObjectsDictionary;
	
	
	/**
	   * The provider that holds the list of contacts in the database.
	   */
	private ListDataProvider<EnvObject> dataProvider = new ListDataProvider<EnvObject>();
	  
	 // Private constructor suppresses 
    private FreedomoticObjectsController() {    		
    }
 
    // Sync creator to avoid multi-thread problems
    private static void createInstance() {
        if (INSTANCE == null) { 
            INSTANCE = new FreedomoticObjectsController();
        }
    }
 
    public static FreedomoticObjectsController getInstance() {
        if (INSTANCE == null) createInstance();
        return INSTANCE;
    }
    
    public int init()
    {
//    	if (!initStompClient())
//    		return STOMP_ERROR;
    	if(!prepareRestResource())
    		return REST_ERROR;
    	return CONNECTED;
    }
    
    //TODO: create a generic client to retrieve all resources. See restlet project.
    public static boolean prepareRestResource()
    {
    	objectsResource = GWT.create(ObjectsResourceProxy.class);
		// Set up the contact resource
		objectsResource.getClientResource().setReference("v2/objects/");    	   
        //TODO: Find how to check the configuration
        return true; 
    }
    
    public void retrieve() 
	{		
    	// Retrieve the contact
		objectsResource.retrieve(new Result<List<EnvObject>>() {
		    public void onFailure(Throwable caught) {
		        //TODO: Handle the error
		    }		   
			@Override
			public void onSuccess(List<EnvObject> result) {
				freedomObjects = result;				
				List<EnvObject> list = dataProvider.getList();
				list.clear();
				list.addAll(result);
			}
		});						
	}
     
    public ListDataProvider<EnvObject> getDataProvider()
    {
    	return dataProvider;
    	
    }
	public void addDataDisplay(HasData<EnvObject> display) {
		dataProvider.addDataDisplay(display);		
	}	
	 /**
	   * Refresh all displays.
	   */
	public void refreshDisplays() {
	    dataProvider.refresh();
	}
	
   	public static void message(String message) {    		
		Payload payload = FreedomoticStompHelper.parseMessage(message);
//		EnvObject obj = FloorPlanWidget.environmentEnvironmentController.getInstance().getObject(payload.getStatements("object.name").get(0).getValue());    				 
//		EnvObject obj = 
//		Iterator it = payload.iterator();
//        while (it.hasNext()) {
//            Statement st = (Statement) it.next();	            
//			if (st.getAttribute().equalsIgnoreCase("object.currentRepresentation"))
//			{    			
////				if (obj.getCurrentRepresentationIndex() !=  Integer.parseInt(st.getValue()))
////				{
////					obj.setCurrentRepresentation(Integer.parseInt(st.getValue()));
////					setChanged();
////				}    				    				
//			}    			    			    			
//			else if (!st.getAttribute().equalsIgnoreCase("object.name"))
//			{ 
//				Behavior bh = obj.getBehavior(st.getAttribute());							 
//				if (bh instanceof BooleanBehavior)
//				{
//					boolean bl = Boolean.parseBoolean(st.getValue()); 
//					if (bl !=((BooleanBehavior)bh).getValue())
//					{
//						((BooleanBehavior) bh).setValue(bl);
//						setChanged();
//					}
//
//				}
//				else if (bh instanceof RangedIntBehavior)
//				{
//					int val = Integer.parseInt(st.getValue());
//					if (val !=((RangedIntBehavior)bh).getValue())
//					{
//						((RangedIntBehavior) bh).setValue(val);
//						setChanged();    						
//					}								 								 
//				}
//				else if (bh instanceof ListBehavior)
//				{
//					String val = st.getValue();
//					if (!val.equals(((ListBehavior)bh).getSelected()))
//						((ListBehavior)bh).setSelected(val);
//						setChanged();
//				}
//			}    		
//		}
//		if (hasChanged())
//		{
//			notifyObservers();
//		}
	}

}
