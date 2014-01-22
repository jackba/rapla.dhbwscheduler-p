package org.rapla.plugin.dhbwscheduler;

import org.rapla.entities.domain.Reservation;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;

public class Test extends RaplaComponent
{

	public Test(RaplaContext context) {
		super(context);
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
}