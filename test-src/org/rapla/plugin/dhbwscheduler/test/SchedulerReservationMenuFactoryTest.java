package org.rapla.plugin.dhbwscheduler.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JPanel;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.entities.RaplaObject;
import org.rapla.entities.User;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.Reservation;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.Configuration;
import org.rapla.framework.DefaultConfiguration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.MenuContext;
import org.rapla.gui.toolkit.RaplaMenuItem;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerReservationHelper;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.plugin.dhbwscheduler.SchedulerReservationMenuFactory;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;
import org.rapla.plugin.urlencryption.UrlEncryption;
import org.rapla.plugin.urlencryption.server.UrlEncryptionService;
import org.rapla.storage.StorageOperator;

public class SchedulerReservationMenuFactoryTest extends RaplaTestCase {
	
		ClientFacade facade;
	    Locale locale;
	    DhbwschedulerService service;
	    UrlEncryption urlcrypt;
	    DefaultConfiguration config;
	    
	    public SchedulerReservationMenuFactoryTest(String name) {
	        super(name);
	    }

	    public static Test suite() {
	        return new TestSuite(SchedulerReservationMenuFactoryTest.class);
	    }

	    protected void setUp() throws Exception {
	        super.setUp();
	        raplaContainer.addContainerProvidedComponent( DhbwschedulerPlugin.RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( DhbwschedulerPlugin.RESOURCE_FILE.getId() ) );
	        facade = getContext().lookup(ClientFacade.class );
	        facade.login("homer","duffs".toCharArray());
	        locale = Locale.getDefault();
	        User user = facade.getUser();
	        RaplaContext context = getContext();
	        config = new DefaultConfiguration();
	        service = new DhbwschedulerServiceImpl(context, user);
	        urlcrypt = new UrlEncryptionService(context,config);
	    }

	    protected void tearDown() throws Exception {
	        facade.logout();
	        super.tearDown();
	    }
	    
	    
		public void testcreate() throws Exception{
		
		RaplaMenuItem[] rawmenI = null;

		ClientFacade facade = getFacade();
		
		
		
		

//		//Reservation reservation = facade.newReservation();
//
//        Date startDate = getRaplaLocale().toRaplaDate(2014, 5, 11);
//        Date endDate = getRaplaLocale().toRaplaDate(2014, 5, 12);
//        Appointment appointment = facade.newAppointment(startDate, endDate);
//        reservation.addAppointment(appointment);
//
//        reservation.getClassification().setValue("title", "test");
//
//		Allocatable pers1 = facade.newPerson();
//		Allocatable pers2 = facade.newPerson();
//		
//		
//		pers1.getClassification().setValue("surname", "Wurst");
//		pers1.getClassification().setValue("firstname", "Hans");
//		pers1.getClassification().setValue("email", "test@test.avdfdfefdgt");
//		
//		pers2.getClassification().setValue("surname", "Pan");
//		pers2.getClassification().setValue("firstname", "Peter");
//		
//		facade.store(pers1);
//		facade.store(pers2);
//		reservation.addAllocatable(pers1);
//		reservation.addAllocatable(pers2);
		
		//DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
		//HelperClass.changeReservationAttribute(reservation, "studiengang", (Allocatable) reservation.getClassification().getValue("studiengang"));
		
		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		
		Reservation reservation = (Reservation) lookup.resolve("c6b8bce9-a173-4960-b760-a8840e07beeb");
		
		MenuContext menu = new MenuContext(getContext(), reservation);
		SchedulerReservationMenuFactory menFac = new SchedulerReservationMenuFactory(
				getContext(), config, service, urlcrypt); 

		List<Reservation> rcs = new ArrayList<Reservation>();
		Reservation res2 =  (Reservation) lookup.resolve("72860f35-7f63-4d69-bd66-4c27bb6204ac");
		rcs.add(res2);
		rcs.add(reservation);
		
		menu.setSelectedObjects(rcs);

		rawmenI = menFac.create(menu, reservation);

		for (int i = 0; i < rawmenI.length; i++) {
			if(i == 5){
				rawmenI[1].doClick();
			}
			rawmenI[i].doClick();
			
		}
			
			
		}
}
