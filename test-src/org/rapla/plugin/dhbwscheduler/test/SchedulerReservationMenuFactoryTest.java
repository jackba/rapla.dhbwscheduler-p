package org.rapla.plugin.dhbwscheduler.test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.components.util.DateTools;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.entities.Entity;
import org.rapla.entities.User;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.ClassificationFilter;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.plugin.dhbwscheduler.SchedulerReservationMenuFactory;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;
import org.rapla.server.ServerService;
import org.rapla.server.ServerServiceContainer;


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

	    public void testSetDesignStatus() throws RaplaException{
	    	SchedulerReservationMenuFactory factory;
	    	Reservation[] editableEvent;
	    	Configuration config = I18nBundleImpl.createConfig(DhbwschedulerPlugin.RESOURCE_FILE.getId());
	   	 	DhbwschedulerService service;
				    	
	    	User user_1;
			try {
				user_1 = getFacade().getUser("Homer");
			} catch (RaplaException e1) {
				assertEquals(false, "user_1 konnte befüllt werden");
				return;
			}
			
			try {
				service = getService( DhbwschedulerService.class );
			} catch (RaplaException e2) {
				e2.printStackTrace();
				fail();
				return;
			}
	    	
	    	Calendar tmp = Calendar.getInstance(DateTools.getTimeZone());
			tmp.set(Calendar.DAY_OF_MONTH, 1);
			tmp.set(Calendar.MONTH, Calendar.JANUARY);
			tmp.set(Calendar.YEAR, 2001);
			tmp.set(Calendar.HOUR_OF_DAY, 0);
			tmp.set(Calendar.MINUTE, 0);
			tmp.set(Calendar.SECOND, 0);
			tmp.set(Calendar.MILLISECOND, 0);
	        Date startDatum= new Date(tmp.getTimeInMillis());
	        
			tmp.set(Calendar.DAY_OF_MONTH, 30);
			tmp.set(Calendar.MONTH, Calendar.DECEMBER);
			tmp.set(Calendar.YEAR, 2004);
			tmp.set(Calendar.HOUR_OF_DAY, 0);
			tmp.set(Calendar.MINUTE, 0);
			tmp.set(Calendar.SECOND, 0);
			tmp.set(Calendar.MILLISECOND, 0);
	        Date endDatum= new Date(tmp.getTimeInMillis());
	    	
	    	try {
				editableEvent = getFacade().getReservations(null, startDatum, endDatum, null);
			} catch (RaplaException e) {
				assertEquals(null, user_1);
				return;
			}
	    	
	    	try {
	    		factory = new SchedulerReservationMenuFactory(getContext(), config, service);
			} catch (RaplaException e) {
				assertEquals(false, "SchedulerReservationMenuFactory konnte initialisiert werden");
				return;
			}
	    	factory.setDesignStatus(editableEvent[0], "planning_closed");
	    	String istStatus = (String) editableEvent[0].getClassification().getValue("planungsstatus");
	    	assertEquals("planning_closed",istStatus);
	    	
	    	factory.setDesignStatus(editableEvent[0], "planning_opend");
	    	istStatus = (String) editableEvent[0].getClassification().getValue("planungsstatus");
	    	assertEquals("planning_closed",istStatus);
	    }
	}

