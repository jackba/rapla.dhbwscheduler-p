package org.rapla.plugin.dhbwscheduler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JTextArea;

import org.rapla.entities.Entity;
import org.rapla.entities.RaplaObject;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.MenuContext;
import org.rapla.gui.ObjectMenuFactory;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.RaplaMenuItem;

public class SchedulerReservationMenuFactory extends RaplaGUIComponent implements ObjectMenuFactory
{
	public static final String closed = new String("geplant");
	public static final String planning_open = new String("in Planung offen");
	public static final String planning_closed = new String("in Planung geschlossen");
	DhbwschedulerService service;
    public SchedulerReservationMenuFactory( RaplaContext context, Configuration config, DhbwschedulerService service) throws RaplaException
    {
        super( context );
        this.service = service;
    }

    public RaplaMenuItem[] create( final MenuContext menuContext, final RaplaObject focusedObject )
    {
    	Collection selectedObjects = menuContext.getSelectedObjects();
    	Collection selectionList = new HashSet();
    	
    	if ( selectedObjects != null )
    	{
    		selectionList.addAll( selectedObjects);
    	}
    	
    	if ( focusedObject != null)
    	{
    		selectionList.add( focusedObject);
    	}
    	final List<Reservation> selectedReservations = new ArrayList<Reservation>();
    	for ( Object obj: selectionList)
    	{
    		if ( obj instanceof Reservation)
    		{
    			selectedReservations.add( (Reservation) obj);
    		}
    		if ( obj instanceof Appointment)
    		{
    			selectedReservations.add( ((Appointment) obj).getReservation());
    		}
    		if ( obj instanceof AppointmentBlock)
    		{
    			selectedReservations.add( ((AppointmentBlock) obj).getAppointment().getReservation());
    		}

    	}
    	if ( selectedReservations.size() == 0)
    	{
    		return RaplaMenuItem.EMPTY_ARRAY;
    	}
    	List<RaplaMenuItem> menus = new ArrayList<RaplaMenuItem>();
        // create the menu entry
/*        {
		    final RaplaMenuItem menu = new RaplaMenuItem("MY_ACTION_1");
		    menu.setText( "My Action1" );
		    // Last the action for the marked menu 
		    menu.addActionListener( new ActionListener()
		    {
		        public void actionPerformed( ActionEvent e )
		        {
		            try 
		            {
		                Entity event = selectedReservations.get( 0);
						Reservation editableEvent = getClientFacade().edit( event);
		                // do something with the reservation
		                getClientFacade().store( editableEvent ); 
		            }
		            catch (RaplaException ex )
		            {
		                showException( ex, menuContext.getComponent());
		            }
		        }
		     });
		    menus.add( menu );
        }
*/
    	{
		    final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_ABSCHLIESSEN");
		    menu.setText( "Planung abschliessen" );
		    // Last the action for the marked menu 
		    menu.addActionListener( new ActionListener()
		    {
		        public void actionPerformed( ActionEvent e )
		        {
		        	try 
		            {
		                Entity event = selectedReservations.get( 0);
						Reservation editableEvent = getClientFacade().edit( event);
		                // do something with the reservation
						String design_status = (String) editableEvent.getClassification().getValue("planungsstatus");
						if (design_status != closed) {
							editableEvent.getClassification().setValue("Planungsstatus", closed);
						}
		                getClientFacade().store( editableEvent ); 
						createMessage("Plannung abgeschlossen", 200, 100,"Planungsstatus", menuContext);
		            }
		            catch (RaplaException ex )
		            {
		                showException( ex, menuContext.getComponent());
		            }
		        }
		     });
		    menus.add( menu );
        }
        {
		    final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_OEFFNEN");
		    menu.setText( "Planung oeffnen" );
		    // Last the action for the marked menu 
		    menu.addActionListener( new ActionListener()
		    {
		        public void actionPerformed( ActionEvent e )
		        {
		            try 
		            {
		                Entity event = selectedReservations.get( 0);
						Reservation editableEvent = getClientFacade().edit( event);
		                // do something with the reservation
						String design_status = (String) editableEvent.getClassification().getValue("planungsstatus");
						if (design_status != planning_open) {
							editableEvent.getClassification().setValue("Planungsstatus", planning_open);
						}
		                getClientFacade().store( editableEvent );
						createMessage("Plannung geoeffnet", 200, 100, "Planungsstatus", menuContext);

		            }
		            catch (RaplaException ex )
		            {
		                showException( ex, menuContext.getComponent());
		            }
		        }
		     });
		    menus.add( menu );
        }
        {
		    final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_SCHLIESSEN");
		    menu.setText( "Planung schliessen" );
		    // Last the action for the marked menu 
		    menu.addActionListener( new ActionListener()
		    {
		        public void actionPerformed( ActionEvent e )
		        {
		            try 
		            {
		                Entity event = selectedReservations.get( 0);
						Reservation editableEvent = getClientFacade().edit( event);
		                // do something with the reservation
						String design_status = (String) editableEvent.getClassification().getValue("planungsstatus");
						if (design_status != planning_closed) {
							editableEvent.getClassification().setValue("Planungsstatus", planning_closed);
						}
		                getClientFacade().store( editableEvent );
						createMessage("Plannung geschlossen", 200, 100, "Planungsstatus", menuContext);

		            }
		            catch (RaplaException ex )
		            {
		                showException( ex, menuContext.getComponent());
		            }
		        }
		     });
		    menus.add( menu );
        }
        {
	        final RaplaMenuItem menu = new RaplaMenuItem("SCHEDULING_ANSTOSSEN");
	        menu.setText( "Scheduling anstossen" );
	        //menu.setIcon( getIcon("icon.help"));
	        menu.addActionListener( new ActionListener()
	            {
	                public void actionPerformed( ActionEvent e )
	                {
	                    try 
	                    {
	                    	
	                    	
	                    	List<SimpleIdentifier> reservationIds = new ArrayList<SimpleIdentifier>();
	                    
	                    	for ( Reservation obj: selectedReservations)
	                    	{
                    			Comparable id = ((RefEntity<?>) obj).getId();
								reservationIds.add((SimpleIdentifier)id);
	                    	}
	                    	
	                    	SimpleIdentifier[] ids = reservationIds.toArray( new SimpleIdentifier[] {});
							String result = service.schedule( ids);
	                        JTextArea content = new JTextArea();
	                        content.setText( result);
	                        DialogUI dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,content,new String[] {"OK"});
	                        dialogUI.setSize( 300, 300);
	                        dialogUI.setTitle("Example Dialog");
	                        if (menuContext.getPoint() != null)
	                        {    
	                            dialogUI.setLocation( menuContext.getPoint() );
	                        }
	                        dialogUI.startNoPack();
	                    }
	                    catch (RaplaException ex )
	                    {
	                        showException( ex, menuContext.getComponent());
	                    }
	                }
	             });
		    menus.add( menu );
        }
        {
	        final RaplaMenuItem menu = new RaplaMenuItem("ERFASSUNG");
	        menu.setText( "Erfassungslink" );
	        //menu.setIcon( getIcon("icon.help"));
	        menu.addActionListener( new ActionListener()
	            {

	                public void actionPerformed( ActionEvent e )
	                {
	                    try 
	                    {
	                    	
	                    	
	                    	List<SimpleIdentifier> reservationIds = new ArrayList<SimpleIdentifier>();
	                    
	                    	for ( Reservation obj: selectedReservations)
	                    	{
                    			Comparable id = ((RefEntity<?>) obj).getId();
								reservationIds.add((SimpleIdentifier)id);
	                    	}
	                    	
	                    	SimpleIdentifier[] ids = reservationIds.toArray( new SimpleIdentifier[] {});
							//Methode neu erstellt. Muss noch bearbeitet werden
	                    	//String result = service.getInformation(ids);
	                    	String result = "";
	                        JTextArea content = new JTextArea();
	                        //content.setText( result);
	                        
	                        
	                        String[] resultArray = result.split(",");
	                        String strId = resultArray[0];
	                        String strName = resultArray[1];
	                        String strKurs = resultArray[2];
	                        result = "http://localhost:8051/rapla?page=scheduler-constraints&id=" + strId + "&name=" + strName + "&kurs=" + strKurs;
	                        //result = URLEncoder.encode(result, "ISO-8859-1");
	                        content.setText(result);
	                        
	                        DialogUI dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,content,new String[] {"OK"});
	                        dialogUI.setSize( 300, 300);
	                        dialogUI.setTitle("Example Dialog");
	                        if (menuContext.getPoint() != null)
	                        {    
	                            dialogUI.setLocation( menuContext.getPoint() );
	                        }
	                        dialogUI.startNoPack();
	                    }
	                    catch (RaplaException ex )
	                    {
	                        showException( ex, menuContext.getComponent());
	                    } 
// catch (UnsupportedEncodingException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
	                }
	             });
		    menus.add( menu );
        }
        
        return menus.toArray(new RaplaMenuItem[] {});
    }
    
    private void createMessage(String message, int x, int y, String title, final MenuContext menuContext) throws RaplaException {
        JTextArea content = new JTextArea();
        content.setText(message);
        DialogUI dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,content,new String[] {"OK"});
        dialogUI.setSize(x,y);
        dialogUI.setTitle(title);
        if (menuContext.getPoint() != null)
        {    
            dialogUI.setLocation( menuContext.getPoint() );
        }
        dialogUI.startNoPack();
    }
      
}
