package org.rapla.plugin.dhbwscheduler;

import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.server.ConstraintService;

public class DhbwschedulerReservationHelper extends RaplaComponent
{

	public final String CLOSED = new String("geplant");
	public final String PLANNING_OPEN = new String("in Planung");
	public final String PLANNING_CLOSED = new String("in Planung geschlossen");
	public final String PLANUNGSSTATUS = new String("planungsstatus");

	public DhbwschedulerReservationHelper(RaplaContext context) {
		super(context);
		setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
	}
	
	public Reservation changeReservationAttribute(Reservation r ,String Attribute, String value){
		try {
			Reservation editableEvent = getClientFacade().edit( r);
			editableEvent = getClientFacade().edit( r);
			editableEvent.getClassification().setValue(Attribute, value);
			getClientFacade().store( editableEvent );
			getClientFacade().refresh();
			return editableEvent;
		} catch (RaplaException e1) {
			e1.printStackTrace();
			getLogger().info("ERROR:" + e1.toString());
		}
		return null;
	}
	
	public Reservation changeReservationAttribute(Reservation r ,String Attribute, Allocatable value){
		try {
			Reservation editableEvent = getClientFacade().edit( r);
			editableEvent = getClientFacade().edit( r);
			editableEvent.getClassification().setValue(Attribute, value);
			getClientFacade().store( editableEvent );
			getClientFacade().refresh();
			return editableEvent;
		} catch (RaplaException e1) {
			e1.printStackTrace();
			getLogger().info("ERROR:" + e1.toString());
		}
		return null;
	}
	
	public String getStringStatus(int status) {
		String returnvalue = "";
		switch(status){
		case ConstraintService.STATUS_UNINVITED:
			returnvalue = getString("uneingeladen");
			break;
		case ConstraintService.STATUS_INVITED:
			returnvalue = getString("eingeladen");
			break;
		case ConstraintService.STATUS_RECORDED:
			returnvalue = getString("erfasst");
			break;
		case ConstraintService.STATUS_PARTIAL_INVITED:
			returnvalue = getString("teileingeladen");
			break;
		case ConstraintService.STATUS_PARTIAL_RECORDED:
			returnvalue = getString("teilerfasst");
			break;
		default:
			returnvalue = "error";						
		}
		return returnvalue;
	}
	
	public String getStudiengang(Reservation r){
		String studiengang = "";
		if (r.getClassification().getValue("studiengang")!=null)
		{
			studiengang = r.getClassification().getValue("studiengang").toString();
			if (studiengang.contains(" "))
			{
				int pos = studiengang.indexOf(" ");
				studiengang = studiengang.substring(0, pos);
			}
		}
		return studiengang;
	}
	
}