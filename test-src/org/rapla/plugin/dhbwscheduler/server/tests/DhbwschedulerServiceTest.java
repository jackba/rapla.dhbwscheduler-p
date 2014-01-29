package org.rapla.plugin.dhbwscheduler.server.tests;

import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.entities.RaplaObject;
import org.rapla.entities.RaplaType;
import org.rapla.entities.User;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;

public class DhbwschedulerServiceTest extends RaplaTestCase {
	ClientFacade facade;
	Locale locale;
	DhbwschedulerServiceImpl service;

	public DhbwschedulerServiceTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(DhbwschedulerServiceTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		raplaContainer.addContainerProvidedComponent( DhbwschedulerPlugin.RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( DhbwschedulerPlugin.RESOURCE_FILE.getId() ) );
		RaplaContext context = getContext();
		facade = context.lookup(ClientFacade.class);
		facade.login("homer", "duffs".toCharArray());
		locale = Locale.getDefault();
		User user = facade.getUser();
		service = new DhbwschedulerServiceImpl(context, user);
	}

	protected void tearDown() throws Exception {
		facade.logout();
		super.tearDown();
	}
	
	public void testScheduleStandard() {

		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			@SuppressWarnings("restriction")
			SimpleIdentifier[] reservationIds = {
					new SimpleIdentifier(type, 10),
					new SimpleIdentifier(type, 11),
					new SimpleIdentifier(type, 12),
					new SimpleIdentifier(type, 13),
					new SimpleIdentifier(type, 14),
					new SimpleIdentifier(type, 15),
					new SimpleIdentifier(type, 16),
					new SimpleIdentifier(type, 17),
					new SimpleIdentifier(type, 18),
					new SimpleIdentifier(type, 19) };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail();
		}

	}
}
