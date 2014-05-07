package org.rapla.plugin.dhbwscheduler.test;

//import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
//import org.rapla.entities.Entity;
//import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
//import org.rapla.facade.AllocationChangeEvent;
import org.rapla.facade.ClientFacade;
//import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerReservationHelper;
//import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;


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
	        raplaContainer.addContainerProvidedComponent( DhbwschedulerPlugin.RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( DhbwschedulerPlugin.RESOURCE_FILE.getId() ) );
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
	    
	    public void testGetStringStatus() throws RaplaException{
	    	DhbwschedulerReservationHelper helper = new DhbwschedulerReservationHelper(getContext());
	    	String status;
	    	status = helper.getStringStatus(0);
	    	assertEquals("uneingeladen", status);
	    	status = helper.getStringStatus(1);
	    	assertEquals("eingeladen", status);
	    	status = helper.getStringStatus(2);
	    	assertEquals("erfasst", status);
	    	status = helper.getStringStatus(3);
	    	assertEquals("teilweise eingeladen", status);
	    	status = helper.getStringStatus(4);
	    	assertEquals("teilweise erfasst", status);
	    	status = helper.getStringStatus(5);
	    	assertEquals("error", status);
	    }
	    
//	    public void testGetStudiengang() throws RaplaException{
//	    	DhbwschedulerReservationHelper helper = new DhbwschedulerReservationHelper(getContext());
//	    	
//	    	Reservation[] reservations = facade.getReservations(null, null, null, null);
//	    	Reservation r = reservations[reservations.length-1];
//	    	
//	    	helper.changeReservationAttribute(r, "studiengang", "Wirtschaft/Wirtschaftsinformatik");
//	    	String istStudiengang = (String) r.getClassification().getValue("studiengang");
//	    	assertEquals("Wirtschaft/Wirtschaftsinformatik", istStudiengang);
//	    	
//	    	String studiengang = helper.getStudiengang(r);
//	    	assertEquals("wirtschaftsinformatik", studiengang);
//	    }
	}

