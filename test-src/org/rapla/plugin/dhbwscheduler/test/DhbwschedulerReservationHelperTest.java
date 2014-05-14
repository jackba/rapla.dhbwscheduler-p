package org.rapla.plugin.dhbwscheduler.test;

import java.util.Locale;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.rapla.RaplaTestCase;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.entities.domain.Reservation;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerReservationHelper;

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
	    	
	    	Reservation newr = helper.changeReservationAttribute(r, "planungsstatus", "geplant");
	    	String istStatus = (String) newr.getClassification().getValue("planungsstatus");
	    	assertEquals("geplant", istStatus);
	    	
	    	newr = helper.changeReservationAttribute(r, "planungsstatus", "in Planung");
	    	istStatus = (String) newr.getClassification().getValue("planungsstatus");
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
	}

