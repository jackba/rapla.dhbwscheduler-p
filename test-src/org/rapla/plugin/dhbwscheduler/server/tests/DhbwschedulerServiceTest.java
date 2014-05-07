package org.rapla.plugin.dhbwscheduler.server.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.entities.Entity;
//import org.rapla.entities.RaplaType;
import org.rapla.entities.User;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.Reservation;
//import org.rapla.entities.storage.RefEntity;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;
import org.rapla.plugin.freetime.FreetimePlugin;
import org.rapla.storage.StorageOperator;

@SuppressWarnings("restriction")
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
		raplaContainer.addContainerProvidedComponent( FreetimePlugin.RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( FreetimePlugin.RESOURCE_FILE.getId() ) );
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
	
	public void testMehrereVeranstOhneDoz() {
		try {
			String[] reservationIds = {"f07e6bdd-782c-4573-b2a8-a82433b9dc5d","2afd7fd2-838c-4b89-9b06-cc39efe417a2","ae459332-521c-494f-89dc-0709bc316951" };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (ProfessorInnen):<br/><br>Test_ohneDoz1<br/><br>Test_ohneDoz2<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testEineVeranstOhneDoz() {
		try {
			String[] reservationIds = {"f07e6bdd-782c-4573-b2a8-a82433b9dc5d" };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (ProfessorInnen):<br/><br>Test_ohneDoz1<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testMehrereVeranstOhneKurs() {
		try {
			String[] reservationIds = {"67fc4421-b11b-40cc-8bee-b8f838796789","a2c4e617-b7a8-4f93-8706-33e52dd6de28","ae459332-521c-494f-89dc-0709bc316951" };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (Kurs):<br/><br>Test_ohneKurs1<br/><br>Test_ohneKurs2<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testEineVeranstOhneKurs() {
		try {
			String[] reservationIds = {"67fc4421-b11b-40cc-8bee-b8f838796789" };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (Kurs):<br/><br>Test_ohneKurs1<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testMehrereVeranstOhneDozKurs() {
		try {
			String[] reservationIds = {"78c8f394-d81c-4630-a26f-895e1517c755","a8a16136-9b4f-40ce-903c-47a114a54663","ae459332-521c-494f-89dc-0709bc316951" };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (ProfessorInnen):<br/><br>Test_ohneDozKurs1<br/><br>Test_ohneDozKurs2<br/><br>Bei folgenden Veranstaltungen fehlen Ressourcen (Kurs):<br/><br>Test_ohneDozKurs1<br/><br>Test_ohneDozKurs2<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testEineVeranstOhneDozKurs() {
		try {
			String[] reservationIds = {"78c8f394-d81c-4630-a26f-895e1517c755" };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (ProfessorInnen):<br/><br>Test_ohneDozKurs1<br/><br>Bei folgenden Veranstaltungen fehlen Ressourcen (Kurs):<br/><br>Test_ohneDozKurs1<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testOhnePlanungszyklus() {
		try {
			String[] reservationIds = {"a2905e50-660b-4e41-8700-373c7b91be84"};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "Bei den übergebenen Verantstaltungen fehlt der Planungszyklus.";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testFehlerhafterPlanungszyklus() {
		try {
			String[] reservationIds = {"e8eb0576-fe31-4a62-9df9-281a1633d2cd"};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "Bei den übergebenen Verantstaltungen ist der Planungszyklus fehlerhaft.";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testMehrereLeerePlanungsconstraint() {
		try {
			String[] reservationIds = {"28ba686c-3613-46cd-a5cf-79b325ba812a","3a8b2511-ef26-47d5-a960-a2754a3cb644","ae459332-521c-494f-89dc-0709bc316951" };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Verantstaltungen fehlen die Planungsconstraints der Dozenten<br/><br>Test_ConstraintLeer1<br/><br>Test_ConstraintLeer2<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testEineLeerePlanungsconstraint() {
		try {
			String[] reservationIds = {"28ba686c-3613-46cd-a5cf-79b325ba812a"};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Verantstaltungen fehlen die Planungsconstraints der Dozenten<br/><br>Test_ConstraintLeer1<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testFeiertagPlanungswoche() {
		String[] reservationIds = {"a46b604a-f559-4bec-820f-aeab023a3b65"};
		String ergebnis = "";
		try {
			ergebnis = service.schedule(reservationIds);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		String erwartet = "Test2: 7";
		assertEquals(erwartet, ergebnis);

		StorageOperator lookup;
		@SuppressWarnings("rawtypes")
		Entity object = null;
		try {
			lookup = getContext().lookup(StorageOperator.class);
			object = lookup.resolve(reservationIds[0]);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		Reservation reservation = (Reservation) object;

		Appointment[] app = reservation.getAppointments();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		cal.set(2014, Calendar.FEBRUARY, 27, 8, 0);
		assertEquals(new Date(cal.getTimeInMillis()), app[0].getStart());
	}

	public void testFeiertagFolgewoche() {
		String[] reservationIds = {"ff40d509-8c8e-477c-8891-4fc17e676843"};
		String ergebnis = "";
		try {
			ergebnis = service.schedule(reservationIds);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		String erwartet = "Test3: 2";
		assertEquals(erwartet, ergebnis);

		StorageOperator lookup;
		@SuppressWarnings("rawtypes")
		Entity object = null;
		try {
			lookup = getContext().lookup(StorageOperator.class);
			object = lookup.resolve(reservationIds[0]);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		Reservation reservation = (Reservation) object;

		Appointment[] app = reservation.getAppointments();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		cal.set(2014, Calendar.MARCH, 10, 13, 0);
		assertEquals(new Date(cal.getTimeInMillis()), app[0].getStart());

		cal.set(2014, Calendar.MARCH, 20, 8, 0);
		assertEquals(new Date(cal.getTimeInMillis()), app[1].getStart());
	}
	
	public void testPriorisierungDoz() {
		String[] reservationIds = {"9cb1e542-ff75-4255-b582-e207cc020d64"};
		String ergebnis = "";
		try {
			ergebnis = service.schedule(reservationIds);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		String erwartet = "Test_Prof1_Montagmorgen_9: 1";
		assertEquals(erwartet, ergebnis);

		StorageOperator lookup;
		@SuppressWarnings("rawtypes")
		Entity object = null;
		try {
			lookup = getContext().lookup(StorageOperator.class);
			object = lookup.resolve(reservationIds[0]);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		Reservation reservation = (Reservation) object;

		Appointment[] app = reservation.getAppointments();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(2014, Calendar.FEBRUARY, 10, 10, 0); //Uhrzeit + 1 Stunde
		assertEquals(new Date(cal.getTimeInMillis()), app[0].getStart());
	}
	
	public void testPlanungszyklus_beachten() {
		String[] reservationIds = {"c8a98d16-76b5-409a-ac71-3d90f9c441e9"};
		String ergebnis = "";
		try {
			ergebnis = service.schedule(reservationIds);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		String erwartet = "Test_Prof1_Planungszyklus: 1";
		assertEquals(erwartet, ergebnis);

		StorageOperator lookup;
		@SuppressWarnings("rawtypes")
		Entity object = null;
		try {
			lookup = getContext().lookup(StorageOperator.class);
			object = lookup.resolve(reservationIds[0]);
		} catch (RaplaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		Reservation reservation = (Reservation) object;

		Appointment[] app = reservation.getAppointments();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(2014, Calendar.MARCH, 24, 9, 0); //Uhrzeit + 1 Stunde
		assertEquals(new Date(cal.getTimeInMillis()), app[0].getStart());
	}
}
