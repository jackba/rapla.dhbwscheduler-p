package org.rapla.plugin.dhbwscheduler.test;

import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.entities.Entity;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerReservationHelper;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;


public class DhbwschedulerReservationHelperTest extends RaplaTestCase {
		ClientFacade facade;
	    Locale locale;

	    public DhbwschedulerReservationHelperTest(String name) {
	        super(name);
	    }

	    public static Test suite() {
	        return new TestSuite(DhbwschedulerReservationHelperTest.class);
	    }

	    protected void setUp() throws Exception {
	        super.setUp();
	        facade = getContext().lookup(ClientFacade.class );
	        facade.login("homer","duffs".toCharArray());
	        locale = Locale.getDefault();
	    }

	    protected void tearDown() throws Exception {
	        facade.logout();
	        super.tearDown();
	    }

	    public void testchangeReservationAttribute() throws RaplaException {
	    	DhbwschedulerReservationHelper helper = new DhbwschedulerReservationHelper(getContext());
	    	Reservation[] reservations = facade.getReservations(null, null, null, null);
	    	Reservation r = reservations[reservations.length-1];
	    	helper.changeReservationAttribute(r, "planungsstatus", "geplant");
	    	String istStatus = (String) r.getClassification().getValue("planungsstatus");
	    	assertEquals("geplant", istStatus);
	    	
	    	helper.changeReservationAttribute(r, "planungsstatus", "in Planung");
	    	istStatus = (String) r.getClassification().getValue("planungsstatus");
	    	assertEquals("in Planung", istStatus);
	    }
	}

