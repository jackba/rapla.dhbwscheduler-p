package org.rapla.plugin.dhbwscheduler.server.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.entities.RaplaType;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;
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
		raplaContainer.addContainerProvidedComponent( DhbwschedulerPlugin.RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( DhbwschedulerPlugin.RESOURCE_FILE.getId() ) );
		RaplaContext context = getContext();
		facade = context.lookup(ClientFacade.class);
		facade.login("homer", "duffs".toCharArray());
		locale = Locale.getDefault();
		service = new DhbwschedulerServiceImpl(context);
	}

	protected void tearDown() throws Exception {
		facade.logout();
		super.tearDown();
	}
	
	public void testMehrereVeranstOhneDoz() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = {
					new SimpleIdentifier(type, 20),
					new SimpleIdentifier(type, 21),
					new SimpleIdentifier(type, 19) };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (ProfessorInnen):<br/><br>Test_ohneDoz1<br/><br>Test_ohneDoz2<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testEineVeranstOhneDoz() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 20)};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (ProfessorInnen):<br/><br>Test_ohneDoz1<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testMehrereVeranstOhneKurs() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = {
					new SimpleIdentifier(type, 22),
					new SimpleIdentifier(type, 23),
					new SimpleIdentifier(type, 19) };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (Kurs):<br/><br>Test_ohneKurs1<br/><br>Test_ohneKurs2<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testEineVeranstOhneKurs() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 22)};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (Kurs):<br/><br>Test_ohneKurs1<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testMehrereVeranstOhneDozKurs() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = {
					new SimpleIdentifier(type, 24),
					new SimpleIdentifier(type, 25),
					new SimpleIdentifier(type, 19) };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (ProfessorInnen):<br/><br>Test_ohneDozKurs1<br/><br>Test_ohneDozKurs2<br/><br>Bei folgenden Veranstaltungen fehlen Ressourcen (Kurs):<br/><br>Test_ohneDozKurs1<br/><br>Test_ohneDozKurs2<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testEineVeranstOhneDozKurs() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 24)};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Veranstaltungen fehlen Ressourcen (ProfessorInnen):<br/><br>Test_ohneDozKurs1<br/><br>Bei folgenden Veranstaltungen fehlen Ressourcen (Kurs):<br/><br>Test_ohneDozKurs1<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testOhnePlanungszyklus() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 39)};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "Bei den übergebenen Verantstaltungen fehlt der Planungszyklus.";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testFehlerhafterPlanungszyklus() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 29)};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "Bei den übergebenen Verantstaltungen ist der Planungszyklus fehlerhaft.";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testMehrereLeerePlanungsconstraint() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = {
				new SimpleIdentifier(type, 30),
				new SimpleIdentifier(type, 31),
				new SimpleIdentifier(type, 19) };
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Verantstaltungen fehlen die Planungsconstraints der Dozenten<br/><br>Test_ConstraintLeer1<br/><br>Test_ConstraintLeer2<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testEineLeerePlanungsconstraint() {
		try {
			@SuppressWarnings("rawtypes")
			RaplaType type = RaplaType.get(Reservation.class);
			SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 30)};
			service.schedule(reservationIds);
		} catch (RaplaException e) {
			String erwartet = "<br>Bei folgenden Verantstaltungen fehlen die Planungsconstraints der Dozenten<br/><br>Test_ConstraintLeer1<br/>";
			assertEquals(erwartet, e.getMessage());
		}
	}

	public void testFeiertagPlanungswoche() {
		@SuppressWarnings("rawtypes")
		RaplaType type = RaplaType.get(Reservation.class);
		SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 44)};
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
		RefEntity<?> object = null;
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
		@SuppressWarnings("rawtypes")
		RaplaType type = RaplaType.get(Reservation.class);
		SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 45)};
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
		RefEntity<?> object = null;
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
		@SuppressWarnings("rawtypes")
		RaplaType type = RaplaType.get(Reservation.class);
		SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 32)};
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
		RefEntity<?> object = null;
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
		@SuppressWarnings("rawtypes")
		RaplaType type = RaplaType.get(Reservation.class);
		SimpleIdentifier[] reservationIds = { new SimpleIdentifier(type, 47)};
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
		RefEntity<?> object = null;
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
