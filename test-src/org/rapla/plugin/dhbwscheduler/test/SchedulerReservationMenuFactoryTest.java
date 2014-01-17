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
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;


public class SchedulerReservationMenuFactoryTest extends RaplaTestCase {
		ClientFacade facade;
	    Locale locale;

	    public SchedulerReservationMenuFactoryTest(String name) {
	        super(name);
	    }

	    public static Test suite() {
	        return new TestSuite(SchedulerReservationMenuFactoryTest.class);
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

	    public void testgetClassification(){
	    	
	    	//Reservation editableEvent = getFacade().getReservations(user, start, end, filters);
			// do something with the reservation
			//setDesignStatus(editableEvent, getString("planning_closed"));	    	
	    	assertEquals(true, false);
	    }
	}

