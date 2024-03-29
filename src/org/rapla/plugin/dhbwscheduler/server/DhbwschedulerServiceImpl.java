package org.rapla.plugin.dhbwscheduler.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkCallback;
import org.gnu.glpk.GlpkCallbackListener;
import org.gnu.glpk.GlpkTerminal;
import org.gnu.glpk.GlpkTerminalListener;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_tran;
import org.gnu.glpk.glp_tree;
import org.rapla.components.util.DateTools;
import org.rapla.entities.User;
import org.rapla.entities.configuration.Preferences;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.Repeating;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.domain.internal.AppointmentImpl;
import org.rapla.entities.domain.internal.ReservationImpl;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.facade.ClientFacade;
import org.rapla.facade.Conflict;
import org.rapla.facade.RaplaComponent;
import org.rapla.facade.internal.FacadeImpl;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.plugin.freetime.FreetimeServiceRemote.Holiday;
import org.rapla.plugin.freetime.server.FreetimeService;
import org.rapla.plugin.mail.MailException;
import org.rapla.plugin.mail.MailPlugin;
import org.rapla.plugin.mail.server.MailInterface;
import org.rapla.storage.StorageOperator;

/**
 * Implementierung des Scheduler-Plugins. Das Plugin plant Vorlesungen im Status "in Plannung geschlossen"
 * 
 * @author DHBW (Dieckmann, Daniel; Dvorschak, Marc; Flickinger, Marco; Geissel, Markus; Gemeinhardt, Christian; Henne, Adrian; K�hler, Christoffer; Schaller, Benjamin; Werner, Benjamin)

 *
 */
 
@SuppressWarnings({ "unused", "restriction" })
public class DhbwschedulerServiceImpl extends RaplaComponent implements
		GlpkCallbackListener, GlpkTerminalListener, DhbwschedulerService {

	int[][] timeSlots = { {}, {}, { 0, 1 }, { 2, 3 }, { 4, 5 }, { 6, 7 }, { 8, 9 } };
	private boolean hookUsed = false;
	private String model;
	private String data;
	private String solution;

	private int doz_vor[][] = { {} };
	private int vor_res[][] = { {} };
	private int kurs_vor[][] = { {} };
	private int doz_cost[][] = { {} };
	private ArrayList<Reservation> reservations;
	private ArrayList<Reservation> reservationsPlannedByScheduler;

	private FreetimeService freetimeService = null;
	private User user;

	/**
	 * Constructor 
	 * 
	 * @param context
	 */
	public DhbwschedulerServiceImpl(RaplaContext context, User user) {
		super(context);
		this.user = user;
		setChildBundleName(DhbwschedulerPlugin.RESOURCE_FILE);
	}

	/**
	 * Startet den Scheduler. Die �bergebenen Vorlesungen m�ssen im Zustand "in Plannung geschlossen" sein und Dozenten-Constraints 
	 * besitzen.
	 * 
	 * @see
	 * org.rapla.plugin.dhbwscheduler.DhbwschedulerService#schedule(org.rapla.entities.storage.internal.SimpleIdentifier[])
	 * 
	 * @param String[] reservationIds - IDs der zu plannenden Vorlesungen
	 */
	@Override
	public String schedule(String[] reservationIds)
			throws RaplaException {
		StorageOperator lookup = getContext().lookup(StorageOperator.class);
		reservationsPlannedByScheduler = new ArrayList<Reservation>();
		reservations = new ArrayList<Reservation>();
		for (String id : reservationIds) {
			Reservation reservation = (Reservation) lookup.resolve(id);
			
			String string = getString("planning_closed");
			String value = (String) reservation.getClassification().getValue("planungsstatus");
			if (value.equals(string)) {
				reservations.add(reservation);
			}
		}

		if (reservations.size() == 0) {
			return "Keine Veranstaltungen zu planen";
		}

		String postProcessingResults = "";

		try {
			freetimeService = getService(FreetimeService.class);
		} catch (UnsupportedOperationException e) {
			postProcessingResults += "<br>" + getString("no_holiday_plugin") + "<br/>";
		}

		Allocatable planning = (Allocatable) reservations.get(0).getClassification().getValue("planungszyklus");
		if (planning == null) {
			throw new RaplaException(getString("missing_planningperiod"));
		}
		
		Date startDatum = (Date) planning.getClassification().getValue("startdate");
		Date endeDatum = (Date) planning.getClassification().getValue("enddate");
		
		if(startDatum == null || endeDatum == null || endeDatum.before(startDatum)) {
			throw new RaplaException(getString("check_planningperiod"));
		}
		
		Calendar tmp = Calendar.getInstance(DateTools.getTimeZone());
		Date anfangWoche = startDatum;
		tmp.setTime(anfangWoche);

		if (DateTools.getWeekday(anfangWoche) != DateTools.MONDAY) {
			tmp.add(Calendar.DAY_OF_YEAR, DateTools.MONDAY - DateTools.getWeekday(anfangWoche));
			anfangWoche = new Date(tmp.getTimeInMillis());
		}
		
		tmp.add(Calendar.DAY_OF_YEAR, 5);

		Date endeWoche = new Date(tmp.getTimeInMillis());

		// plane solange, wie der Anfang der neuen Woche vor dem Ende des Planungszyklus liegt
		while ((anfangWoche.before(endeDatum)) && (!(reservations.isEmpty()))) {

			// PRE-Processing
			preProcessing(anfangWoche, endeWoche);

			// Schedule
			aufbau_scheduler_mod(model, solution);

			aufbau_scheduler_data(data, doz_vor, kurs_vor, vor_res, doz_cost);

			solve();

			// POST-Processing
			String resultProcessing = postProcessing(anfangWoche, endeWoche);
			getLogger().info("Planung:\n" + resultProcessing);
			
			// neue Woche planen
			tmp.setTime(anfangWoche);
			tmp.add(Calendar.DAY_OF_YEAR, 7);
			anfangWoche = new Date(tmp.getTimeInMillis());
			tmp.add(Calendar.DAY_OF_YEAR, 5);
			endeWoche = new Date(tmp.getTimeInMillis());
		}
		
		postProcessingResults += getString("planning_finished");

		postProcessingResults += resolveConflicts(startDatum, endeDatum);
		
		postProcessingResults += mindExceptionDates(startDatum, endeDatum);

		if(reservations.size() == 0) {
			// Alle Veranstaltungen geplant
			getLogger().info(postProcessingResults);
			return postProcessingResults;
		} else {
			String result = postProcessingResults + "\n" + getString("planning_not_successful");
			for(int i = 0; i < reservations.size(); i++) {
				result += reservations.get(0).getClassification().getName(getLocale()) + "\n";
				reservations.remove(0);
			}
			getLogger().info(result);
			return result;
		}
	}

	/**
	 * 
	 * Startet den GPLK-Solver.
	 * 
	 * @throws RaplaException
	 */
	private void solve() throws RaplaException {
		glp_prob lp = null;
		glp_tran tran;
		glp_iocp iocp;

		int skip = 0;
		int ret;
		GLPK.glp_java_set_numeric_locale("C");

		// listen to callbacks
		GlpkCallback.addListener(this);
		// listen to terminal output
		GlpkTerminal.addListener(this);

		lp = GLPK.glp_create_prob();
		// System.out.println("Problem created");

		tran = GLPK.glp_mpl_alloc_wksp();
		ret = GLPK.glp_mpl_read_model(tran, model, skip);
		if (ret != 0) {
			GLPK.glp_mpl_free_wksp(tran);
			GLPK.glp_delete_prob(lp);
			throw new RaplaException("Internal: Model file not found: " + model);
		}
		ret = GLPK.glp_mpl_read_data(tran, data);
		if (ret != 0) {
			GLPK.glp_mpl_free_wksp(tran);
			GLPK.glp_delete_prob(lp);
			throw new RaplaException("Internal: Data file not found: " + data);
		}

		// generate model
		GLPK.glp_mpl_generate(tran, null);
		// build model
		GLPK.glp_mpl_build_prob(tran, lp);
		// set solver parameters
		iocp = new glp_iocp();
		GLPK.glp_init_iocp(iocp);
		iocp.setPresolve(GLPKConstants.GLP_ON);
		// do not listen to output anymore
		GlpkTerminal.removeListener(this);
		// solve model
		ret = GLPK.glp_intopt(lp, iocp);
		// postsolve model
		if (ret == 0) {
			GLPK.glp_mpl_postsolve(tran, lp, GLPKConstants.GLP_MIP);
		}
		// free memory
		GLPK.glp_mpl_free_wksp(tran);
		GLPK.glp_delete_prob(lp);

		// do not listen for callbacks anymore
		GlpkCallback.removeListener(this);

		// check that the hook function has been used for terminal output.
		if (!hookUsed) {
			System.out.println("Error: The terminal output hook was not used.");
			System.exit(1);
		}
	}

	/**
	 * 
	 * Startet das Pre-Processing, das die Daten f�r den GLPK-Solver zusammenstellt.
	 * 
	 * @param startDatum - Beginn der zu plannenden Woche
	 * @param endeDatum - Ende der zu plannenden Woche
	 */
	private void preProcessing(Date startDatum, Date endeDatum)
			throws RaplaException {
		String result = "";

		model = "scheduler_gmpl" + new Date().getTime() + ".mod";
		data = "scheduler_data" + new Date().getTime() + ".dat";
		solution = "scheduler_solution" + new Date().getTime() + ".dat";

		try {
			doz_vor = buildZuordnungDozentenVorlesung();
		} catch (RaplaException e) {
			result += e.getMessage();
		}

		try {
			kurs_vor = buildZuordnungKursVorlesung();
		} catch (RaplaException e) {
			result += e.getMessage();
		}

		try {
			vor_res = buildAllocatableVerfuegbarkeit(startDatum, endeDatum);
		} catch (RaplaException e) {
			result += e.getMessage();
		}

		try {
			doz_cost = buildDozentenKosten();
		} catch (RaplaException e) {
			result += e.getMessage();
		}

		if (!result.isEmpty()) {
			throw new RaplaException(result);
		}
	}

	/**
	 * 
	 * @return int[][] - Int-Array, das die Dozentenkosten der Form [Vorlesung][Zeitslots] abbildet
	 * 
	 * @throws RaplaException
	 */
	private int[][] buildDozentenKosten() throws RaplaException {
		int[][] doz_cost = new int[reservations.size()][10];
		
		for (int i = 0; i < reservations.size(); i++) {
			for (int j = 0; j < 10; j++) {
				doz_cost[i][j] = 0;
			}
		}

		ArrayList<Reservation> veranstaltungenOhnePlanungsconstraints = new ArrayList<Reservation>();
		int vorlesungNr = 0;
		for (Reservation vorlesung : reservations) {
			// get the planungsconstraints
			String planungsconstraint = getDozentenConstraint(vorlesung);
			if (planungsconstraint.isEmpty()) {
				veranstaltungenOhnePlanungsconstraints.add(vorlesung);
			} else {
				// get the slots blocked by the planungsconstraints
				int[] slotsKosten = splitDozentenKostenSlots(planungsconstraint);
				for (int i = 0; i < 10; i++) {
					doz_cost[vorlesungNr][i] = slotsKosten[i];
				}
			}
			vorlesungNr++;
		}
		if (!(veranstaltungenOhnePlanungsconstraints.isEmpty())) {
			String veranstaltungenOhnePlanungsconstraintsListe = "";
			for (Reservation r : veranstaltungenOhnePlanungsconstraints) {
				veranstaltungenOhnePlanungsconstraintsListe = veranstaltungenOhnePlanungsconstraintsListe + "<br>" + r.getName(getLocale()) + "<br/>";
			}
			throw (new RaplaException(""));
		}
		return doz_cost;
	}

	/**
	 * Uebertraegt die Kosten der Dozenten aus dem String in ein int-Array, wobei die Kosten gleichzeitig aufaddiert werden.
	 * 
	 * @param String dozentenconstraint
	 * 
	 * @return int[]
	 */
	private int[] splitDozentenKostenSlots(String dozentenConstraint) {
		int[] belegteSlots = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		int[] dozConst = ConstraintService.getDozConstraints(dozentenConstraint);

		int slotCounter = 0;

		for (int i = 24; i < dozConst.length - 24; i++) {
			int stundenCounter = (i % 12);
			belegteSlots[slotCounter] += dozConst[i];

			if (stundenCounter == 11) {
				slotCounter++;
			}
		}
		return belegteSlots;
	}
	
	/**
	 * Verschieben der Vorlesungen in die gefundenen Zeitslots und Auffraeumen der Dateien. 
	 * 
	 * @param startDatum - Start der zu plannenden Woche
	 * @param endeDatum - Ende der zu plannenden Woche
	 * 
	 * @return String - Ergebnis des Schedulers zur Ausgabe in einem Dialog.
	 */
	private String postProcessing(Date startDatum, Date endeDatum) throws RaplaContextException, RaplaException {
		String solutionString = auslese_Solution(solution);
		String result = "";
		Calendar tmp = Calendar.getInstance(DateTools.getTimeZone());

		int[][] solRes = splitSolution(solutionString);

		for (int i = solRes.length - 1; i >= 0; i--) {
			Date newStart = new Date(tmp.getTimeInMillis());
			Date startWeek = startDatum;
			tmp.setTimeInMillis(startWeek.getTime());
			
			Reservation reservation = reservations.get((solRes[i][0]) - 1);
			result += reservation.getClassification().getName(getLocale()) + ": " + solRes[i][1] + "\n";
			reservation = getClientFacade().edit(reservation);
			for(Appointment appointment : reservation.getAppointments()){
				newStart = setStartDate(solRes[i][1], startWeek, reservation);
				appointment.move(newStart);
				tmp.add(Calendar.WEEK_OF_YEAR, 1);
				startWeek = tmp.getTime();
			}

			getClientFacade().store(reservation);
			reservations.remove((solRes[i][0]) - 1);
			reservationsPlannedByScheduler.add(reservation);
		}
		
		// Dateien aufraeumen
		new File(model).delete();
		new File(data).delete();
		new File(solution).delete();

		return result;
	}
	
	/**
	 * 
	 * L�sen von m�glichen Konflikten zwischen Terminen.
	 * 
	 * @param startDatum - Start des Plannungszyklus.
	 * @param endDatum - Ende des Planungszyklus.
	 * @return String - M�gliche Fehlermeldungen zur Ausgabe in einem Dialog.
	 * @throws RaplaException
	 */
	private String resolveConflicts(Date startDatum, Date endDatum) throws RaplaException {
		String notResolved = "";
		for (Reservation veranstaltung : reservationsPlannedByScheduler){
			//get all conflicts caused by this reservation
			while (getClientFacade().getConflicts(veranstaltung).length > 0) {
				// if there are conflicts, move the appointment
				Conflict[] conflicts = getClientFacade().getConflicts(veranstaltung);
				Conflict conflict = conflicts[0];
				Appointment newApp = null;
				String appointmentId = conflict.getAppointment1();
				// add an exception to the conflicting appointment for the conflicting date
				veranstaltung = getClientFacade().edit(veranstaltung);
				Appointment conflictingAppointment = (Appointment)((ReservationImpl)veranstaltung).findEntityForId(appointmentId);
				Date dateOfConflict = conflict.getStartDate();
				Repeating repeating = conflictingAppointment.getRepeating();
				if (repeating != null) {
					newApp = createNewAppointment(veranstaltung, repeating, conflictingAppointment.getStart(), conflictingAppointment.getEnd(), dateOfConflict);
				} else {
					newApp = conflictingAppointment;
				}
				// get next free time for all resources of the reservation with the new appointment
				Boolean couldMove = moveAppointmentWithDozConstraints(startDatum, endDatum, veranstaltung, newApp);
				if(!couldMove){
					notResolved = notResolved + "\n" + getString("conflict_resolving_not_successful") + veranstaltung.getName(getLocale()) + "\n";
					break;
				}
			}
		}
		return notResolved;
		
	}

	/**
	 * 
	 * Verschiebt einzelne Termine von Veranstaltungen/Vorlesungen unter Beachtung der Dozentenvorgaben und des Planungszyklus.
	 * 
	 * @param startDatum - Start des Plaungszyklus
	 * @param endDatum - Ende des Planungszyklus
	 * @param veranstaltung - Zu planende Vorlesung
	 * @param newAppointment - zu verschiebenderer Termin
	 * @return true if it works, false if not
	 * @throws RaplaException
	 */
	private boolean moveAppointmentWithDozConstraints(Date startDatum, Date endDatum, Reservation veranstaltung, Appointment newAppointment)
			throws RaplaException {
		Date nextStartDate = startDatum;
		Date newStart = new Date();
		Date newStartBackup = new Date();
		int[] dozConstr = ConstraintService.getDozConstraints(getDozentenConstraint(veranstaltung));
		int stelleConstraint = 0;
		boolean breakLoop = false;
		Calendar calInst = Calendar.getInstance(DateTools.getTimeZone());
		while(dozConstr[stelleConstraint] <= 0){
			newStart = getNextFreeTime(veranstaltung.getAllocatables(), newAppointment, nextStartDate, endDatum);
			calInst.setTimeInMillis(newStart.getTime());
			int dayOfWeek = calInst.get(Calendar.DAY_OF_WEEK);
			int slot = 0;
			int hour = calInst.get(Calendar.HOUR_OF_DAY);
			if(hour < 12) {
				//morgens
				slot = timeSlots[dayOfWeek][0];
				stelleConstraint = 24 + ((slot - 1) * 12) + hour;
			} else {
				//mittags
				slot = timeSlots[dayOfWeek][1];
				stelleConstraint = 24 + ((slot - 1) * 12) + (hour - 12);
			}
			if(nextStartDate.after(endDatum) || newStart.equals(newStartBackup)){
				veranstaltung.removeAppointment(newAppointment);
				breakLoop = true;
				break;
			}
			newStartBackup = newStart;
		}
		if (!breakLoop) {
			newAppointment.move(newStart);
		}
		return !breakLoop;
	}

	/**
	 * 
	 * Erstellt einen neuen Termin fuer eine Veranstaltung - in der Regel ein Ausnahmetermin.
	 * 
	 * @param veranstaltung - zu planende Veranstaltung
	 * @param repeating - Objekt, das die Wiederholung eines Termins kapselt
	 * @param start - Start des Planungszyklus
	 * @param end - Ende des Planungszyklus
	 * @param dateOfConflict - Zeitpunkt des Konflikts zwischen Terminen oder mit Dozenten-Constraints.
	 * @return new Appointment 
	 * @throws RaplaException
	 */
	private Appointment createNewAppointment(Reservation veranstaltung, Repeating repeating, Date start, Date end, Date dateOfConflict) throws RaplaException {
		if ((repeating != null)
				&& (!(repeating.isException(dateOfConflict.getTime())))) {
			repeating.addException(dateOfConflict);
		}
		// add a new appointment for the date with the conflict
		Appointment newApp = getModification().newAppointment(start, end, user);
		veranstaltung.addAppointment(newApp);
		return newApp;
	}

	/**
	 * Schreibt den Loesungsstring in ein Int-Array.
	 * 
	 * @param solution
	 * @return int[][]
	 */
	private int[][] splitSolution(String solutionString) {
		String[] solReservations = solutionString.split("\n");

		int[][] solutionArray = new int[solReservations.length][2];
		int i = 0;

		for (String solRes : solReservations) {
			if (solRes.indexOf(",") > -1) {
				solutionArray[i][0] = Integer.valueOf(solRes.substring(0, solRes.indexOf(",")).trim());
				solutionArray[i][1] = Integer.valueOf(solRes.substring(solRes.indexOf(",") + 1).trim());
				i++;
			}
		}

		return solutionArray;
	}

	/**
	 * 
	 * Verschieben eines Termins auf eine Uhrzeit, die von den Dozenten-Constraints zugelassen wird.
	 * 
	 * @param slot - Slot, wo die Veranstaltung geplant hin geplant werden soll 
	 * @param startDate - Startzeit des Termins
	 * @param reservation - zu planende Veranstaltung
	 * @return Date - der neue Startzeitpunkt
	 * @throws RaplaException
	 */
	private Date setStartDate(int slot, Date startDate, Reservation reservation) throws RaplaException {
		Date newStart;
		 
		Calendar cal = Calendar.getInstance(DateTools.getTimeZone());
		
		cal.setTimeInMillis(startDate.getTime());
		if (slot > 1) {
			cal.add(Calendar.HOUR, (slot - 1) * 12);
		}

		// Worktime beachten
		if (cal.get(Calendar.HOUR_OF_DAY) < (getCalendarOptions().getWorktimeStartMinutes() / 60)) {
			cal.add(Calendar.MINUTE, getCalendarOptions().getWorktimeStartMinutes());
		}

		// Dozenten Constraint beachten

		int[] dozConstr = ConstraintService.getDozConstraints(getDozentenConstraint(reservation));
		int start = 24 + ((slot - 1) * 12);
		for (int i = start; i < start + 12; i++) {
			int index = i;
			if ((slot % 2) != 0) {
				// Abends
				index += cal.get(Calendar.HOUR_OF_DAY) - 1;
			} else {
				// Morgens
				index += cal.get(Calendar.HOUR_OF_DAY) - 13;
			}

			if (dozConstr[index] > 0) {
				break;
			} else {
				cal.add(Calendar.HOUR, 1);
			}
		}

		return new Date(cal.getTimeInMillis());
	}

	/**
	 * 
	 * Sucht f�r einen Termin und eine Menge von Resourcen den naechsten freien Termin.
	 * 
	 * @param allocatables - die Resourcen
	 * @param appointment - der zu planende Termin
	 * @param startDate - Start des Planungszyklus
	 * @param endDate - Ende des Planungszyklus
	 * @return Date - naechster freier Termin
	 * @throws RaplaException
	 */
	private Date getNextFreeTime(Allocatable[] allocatables, Appointment appointment, Date startDate, Date endDate) throws RaplaException {
		// Alle gerade geplanten Reservierungen bei der Betrachtung ausschließen
		StorageOperator lookup = getContext().lookup(StorageOperator.class);

		Integer worktimeStartMinutes = getCalendarOptions().getWorktimeStartMinutes();
		Integer worktimeEndMinutes = getCalendarOptions().getWorktimeEndMinutes();

		Integer[] excludedDays = getCalendarOptions().getExcludeDays().toArray(new Integer[] {});
		Integer rowsPerHour = getCalendarOptions().getRowsPerHour();
		
		HashSet<Reservation> ignoreList = getIgnoreList(allocatables, startDate, endDate);

		return lookup.getNextAllocatableDate(Arrays.asList(allocatables), appointment, ignoreList, worktimeStartMinutes, worktimeEndMinutes, excludedDays, rowsPerHour);
	}
	
	/**
	 * 
	 * Erstellt eine Liste mit bei Abfragen in Rapla zu ignorienden Veranstaltungen (v.a. Veranstaltungen "in Planung ge�ffnet"
	 * und "Planung abgeschlossen" sollen ignoriert werden). 
	 * 
	 * @param allocatables - Resourcen
	 * @param startDate - Start des Planungszyklus
	 * @param endDate - Ende des Planungszyklus
	 * @return HashSet - IgnoreList
	 * @throws RaplaException
	 */
	private HashSet<Reservation> getIgnoreList(Allocatable[] allocatables, Date startDate, Date endDate) throws RaplaException {
		HashSet<Reservation> ignoreList = new HashSet<Reservation>();

		Reservation[] vorlesungenMitGleichenResourcen = getClientFacade().getReservationsForAllocatable(allocatables, startDate, endDate, null);
		for (Reservation vorlesungMitGleicherResource : vorlesungenMitGleichenResourcen) {
			// for each of these reservations, look if there are in closed
			Object value = vorlesungMitGleicherResource.getClassification().getValue("planungsstatus");
			if (value.equals(getString("planning_open")) 
					|| ((value.equals(getString("planning_closed"))) 
							&& !(reservationsPlannedByScheduler.contains(vorlesungMitGleicherResource)))) {
				ignoreList.add(vorlesungMitGleicherResource);
			}
		}
		return ignoreList;
	}

	/**
	 * 
	 * Baut die Matrix (int-Array), die enth�lt ob alle Resource in einem Slot f�r eine Veranstaltung verf�gabr sind.
	 * Feiertage werden nur beachtet, wenn das holiday-plugin aktiviert ist!
	 * 
	 * @param start - Anfang der Woche
	 * @param ende  - Ende der Woche
	 * @return int[][] - die Matrix mit der Resourcenverfuegbarkeit
	 * @throws RaplaException
	 */
	private int[][] buildAllocatableVerfuegbarkeit(Date start, Date ende) throws RaplaException {
		Calendar startCal = Calendar.getInstance(DateTools.getTimeZone());
		Calendar endeCal = Calendar.getInstance(DateTools.getTimeZone());
		startCal.setTime(start);
		endeCal.setTime(ende);
		
		// build array, first all times are allowed
		int[][] vor_res = new int[reservations.size()][10];
		for (int i = 0; i < reservations.size(); i++) {
			for (int j = 0; j < 10; j++) {
				vor_res[i][j] = 1;
			}
		}
		// beachten von Feiertagen, nur wenn das holiday-plugin aktiviert ist
		if (freetimeService != null) {
			// alle Freiertage im Plaungszeitraum
			List<Holiday> holidayList = freetimeService.getHolidays(start, ende);
			for ( Holiday singleHoliday : holidayList) {
				Calendar c = Calendar.getInstance(DateTools.getTimeZone());
				c.setTime(singleHoliday.date);
				if (c.after(startCal) && c.before(endeCal)) {
					int dayOfWeekOfHoliday = c.get(Calendar.DAY_OF_WEEK);
					for (int j = 0; j < reservations.size(); j++) {
						vor_res[j][timeSlots[dayOfWeekOfHoliday][0]] = 0;
						vor_res[j][timeSlots[dayOfWeekOfHoliday][1]] = 0;
					}
				}
			}
		}
		
		ArrayList<Reservation> veranstaltungenOhnePlanungsconstraints = new ArrayList<Reservation>();
		int vorlesungNr = 0;
		for (Reservation vorlesung : reservations) {
			// get all resources from all reservations
			Allocatable[] allocatables = vorlesung.getAllocatables();
			// get all other reservations for these resources
			Reservation[] vorlesungenMitGleichenResourcen = getClientFacade().getReservationsForAllocatable(allocatables, start, ende, null);
			for (Reservation vorlesungMitGleicherResource : vorlesungenMitGleichenResourcen) {
				// for each of these reservations, look if there are in planning_closed
				String test = (String) vorlesungMitGleicherResource.getClassification().getValue("planungsstatus");
				if (test == null 
						|| test.equals(getString("closed"))
						|| reservationsPlannedByScheduler.contains(vorlesungMitGleicherResource)) {
					// nur geplante Veranstaltungen muessen beachtet werden
					Appointment[] termine = splitIntoSingleAppointments(vorlesungMitGleicherResource);
					for (Appointment termin : termine) {
						Date beginn = termin.getStart();
						Calendar cal = Calendar.getInstance(DateTools.getTimeZone());
						cal.setTime(beginn);

						if (termin.isWholeDaysSet()) {
							vor_res[vorlesungNr][timeSlots[cal.get(Calendar.DAY_OF_WEEK)][0]] = 0;
							vor_res[vorlesungNr][timeSlots[cal.get(Calendar.DAY_OF_WEEK)][1]] = 0;
						} else {
							// Pruefung, ob innerhalb von Start und Ende
							if (cal.after(startCal) && cal.before(endeCal)) {
								if (cal.get(Calendar.HOUR_OF_DAY) < 12) {
									// set the field for the vorlesungNr and the slot to zero if the appointment starts 
									//before 12 a.m., the appointment will block the slot at the morning
									vor_res[vorlesungNr][timeSlots[cal.get(Calendar.DAY_OF_WEEK)][0]] = 0;
								} else {
									// else the appointment is after 12 a.m. and it will block the slot at the afternoon
									vor_res[vorlesungNr][timeSlots[cal.get(Calendar.DAY_OF_WEEK)][1]] = 0;
								}
							}
						}
					}
				}
			}
			// get the planungsconstraints
			String planungsconstraint = getDozentenConstraint(vorlesung);
			if (planungsconstraint.isEmpty()) {
				veranstaltungenOhnePlanungsconstraints.add(vorlesung);
			} else {
				// get the slots blocked by the planungsconstraints
				int[] belegteSlots = splitDozentenConstraintSlots(planungsconstraint);
				for (int i = 0; i < 10; i++) {
					// copy the blocking only if the slot turns from (not)
					// allowed to not allowed
					if (belegteSlots[i] == 0) {
						vor_res[vorlesungNr][i] = belegteSlots[i];
					}
				}
			
				// Exceptiondates beachten - get Constraints
				String dozentenConstraint = getDozentenConstraint(vorlesung);
				if (planungsconstraint.isEmpty()) {
					veranstaltungenOhnePlanungsconstraints.add(vorlesung);
				} else {
					// get Exception dates
					Date[] exceptionsDates = ConstraintService.getExceptionDates(dozentenConstraint);
					for (Date exceptionDate : exceptionsDates) {
						if (exceptionDate != null) {
							if (exceptionDate.after(start) && exceptionDate.before(ende)) {
								Calendar kalender = Calendar.getInstance(DateTools.getTimeZone());
								kalender.setTime(exceptionDate);
								vor_res[vorlesungNr][timeSlots[kalender.get(Calendar.DAY_OF_WEEK)][0]] = 0;
								vor_res[vorlesungNr][timeSlots[kalender.get(Calendar.DAY_OF_WEEK)][1]] = 0;
							}
						}
					}
				}
			}
			vorlesungNr++;
		}
		if (!(veranstaltungenOhnePlanungsconstraints.isEmpty())) {
			String veranstaltungenOhnePlanungsconstraintsListe = "";
			for (Reservation r : veranstaltungenOhnePlanungsconstraints) {
				veranstaltungenOhnePlanungsconstraintsListe = veranstaltungenOhnePlanungsconstraintsListe + "<br>" + r.getName(getLocale()) + "<br/>";
			}
			throw (new RaplaException("<br>" + getString("missing_planing_constraints") + "<br/>" + veranstaltungenOhnePlanungsconstraintsListe));
		}

		return vor_res;
	}

	/**
	 * 
	 * Teilt zu Bearbeitung einzelner Termine, eine Veranstaltung, die normalerweise nur Wiederholungen enthaelt,
	 * in mehrere Termine auf.
	 * 
	 * @param vorlesung - zu teilende Veranstaltung
	 * @return Appointment[] - Array, das die einzelnen Termine enthaelt
	 * @throws RaplaException
	 */
	private Appointment[] splitIntoSingleAppointments(Reservation vorlesung) throws RaplaException {
		List<Appointment> splitAppointments = new ArrayList<Appointment>();

		// Generate time blocks from selected appointment
		Appointment[] termine = vorlesung.getAppointments();
		for (Appointment appointment : termine){
			List<AppointmentBlock> splits = new ArrayList<AppointmentBlock>();
			appointment.createBlocks(appointment.getStart(), DateTools.fillDate(appointment.getMaxEnd()), splits);
			
			Appointment wholeAppointment = appointment;
			
			// Create single appointments for every time block
			for (AppointmentBlock block: splits)
			{
				Appointment newApp = new AppointmentImpl(new Date(block.getStart()), new Date(block.getEnd()));
				// Add appointment to list
				splitAppointments.add( newApp );
			}
		}
		return splitAppointments.toArray(new Appointment[] {});
	}

	/**
	 * 
	 * Gibt die Dozenten-Constraints zurueck, die an der uebergebenen Veranstaltung haengen. 
	 * 
	 * @param reservation - Veranstaltung, deren Dozenten-Constraints ausgegeben werden sollen
	 * @return String - die unbearbeiteten Dozenten-Constraints, die an der uebergebenen Veranstaltung haengen
	 */
	private String getDozentenConstraint(Reservation reservation) {

		Object constraintObj = reservation.getClassification().getValue("planungsconstraints");
		String result = "";
		if (constraintObj != null) {
			result = constraintObj.toString();
		}
		return result;
	}

	/**
	 * 
	 * Uebertraegt die uebergebenen Dozenten-Constraints in ein int-Array, das die belegten Slots abbildet.
	 * 
	 * @param dozentenConstraint - die Dozenten-Constraint, die abgebildet werden sollen 
	 * @return int[] - int-Array, das die belegten Slots abbildet
	 */
	private int[] splitDozentenConstraintSlots(String dozentenConstraint) {
		int[] belegteSlots = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		int[] dozConst = ConstraintService.getDozConstraints(dozentenConstraint);

		int slotCounter = 0;

		for (int i = 24; i < dozConst.length - 24; i++) {
			int stundenCounter = (i % 12);
			if (dozConst[i] > 0) {
				// sobald eine Stunde verfügbar ist, ist der Slot verfügbar
				belegteSlots[slotCounter] = 1;
			}

			if (stundenCounter == 11) {
				slotCounter++;
			}
		}
		return belegteSlots;
	}

	/**
	 * 
	 * Baut die Matrix (int-Array), die jeder Verantstaltung ihre(n) Dozenten zurordnet. 
	 * Veranstaltungen muessen Dozenten zugeordnet sein! 
	 * 
	 * @return int[][] - die Matrix (int-Array), die jeder Verantstaltung ihre(n) Dozenten zurordnet
	 * @throws RaplaException
	 */
	private int[][] buildZuordnungDozentenVorlesung() throws RaplaException {
		Set<Allocatable> dozenten = new HashSet<Allocatable>();
		ArrayList<Reservation> veranstaltungenOhneDozent = new ArrayList<Reservation>();
		String type = "";
		for (DynamicType alltype : getClientFacade().getDynamicTypes("person")) {
			if (alltype.getKey().equals("professor")) {
				type = alltype.getName(getLocale());
			}
		}
		for (Reservation veranstaltung : reservations) {
			boolean hasProfessor = false;
			// get all resources for all reservations
			Allocatable[] ressourcen = veranstaltung.getAllocatables();
			for (Allocatable a : ressourcen) {
				// if the resource is a professor, add it to the set (no
				// duplicate elements allowed)
				DynamicType allocatableType = a.getClassification().getType();
				if (allocatableType.getKey().equals("professor")) {
					dozenten.add(a);
					hasProfessor = true;
				}
			}
			if (!hasProfessor) {
				veranstaltungenOhneDozent.add(veranstaltung);
			}
		}
		if (!(veranstaltungenOhneDozent.isEmpty())) {
			String veranstaltungenOhneDozentenListe = "";
			for (Reservation r : veranstaltungenOhneDozent) {
				veranstaltungenOhneDozentenListe = veranstaltungenOhneDozentenListe + "<br>" + r.getName(getLocale()) + "<br/>";
			}
			throw (new RaplaException("<br>" + getString("missing_allocatables") + " (" + type + "):" + "<br/>" + veranstaltungenOhneDozentenListe));
		}
		// build the array to assign the professors to their reservations
		int[][] doz_vor = new int[dozenten.size()][reservations.size()];
		int i = 0;
		for (Allocatable a : dozenten) {
			int j = 0;
			for (Reservation veranstaltung : reservations) {
				// check, if the reservation has allocated the professor
				if (veranstaltung.hasAllocated(a)) {
					// yes
					doz_vor[i][j] = 1;
				} else {
					// no
					doz_vor[i][j] = 0;
				}
				j++;
			}
			i++;
		}
		return doz_vor;
	}

	/**
	 * 
	 *Baut die Matrix (int-Array), die jeder Verantstaltung ihre(n) Kurs(e) zurordnet. 
	 *Veranstaltungen muss mindestens ein Kurs zugeordnet sein!  
	 * 
	 * @return int[][] - die Matrix (int-Array), die jeder Verantstaltung ihre(n) Kurs(e) zurordnet
	 * @throws RaplaException
	 */
	private int[][] buildZuordnungKursVorlesung() throws RaplaException {
		Set<Allocatable> kurse = new HashSet<Allocatable>();
		ArrayList<Reservation> veranstaltungenOhneKurse = new ArrayList<Reservation>();
		String type = "";
		for (DynamicType alltype : getClientFacade().getDynamicTypes("resource")) {
			if (alltype.getKey().equals("kurs")) {
				type = alltype.getName(getLocale());
			}
		}
		for (Reservation veranstaltung : reservations) {
			// get all resources for all reservations
			Allocatable[] ressourcen = veranstaltung.getAllocatables();
			boolean hasKurs = false;
			for (Allocatable a : ressourcen) {
				if (a.getClassification().getType().getKey().equals("kurs")) {
					// if the resource is a kurs, add it to the set (no duplicate elements allowed)
					kurse.add(a);
					hasKurs = true;
				}
			}
			if (!hasKurs) {
				veranstaltungenOhneKurse.add(veranstaltung);
			}
		}
		if (!(veranstaltungenOhneKurse.isEmpty())) {
			String veranstaltungenOhneKurseListe = "";
			for (Reservation r : veranstaltungenOhneKurse) {
				veranstaltungenOhneKurseListe = veranstaltungenOhneKurseListe + "<br>" + r.getName(getLocale()) + "<br/>";
			}
			throw (new RaplaException("<br>" + getString("missing_allocatables") + " (" + type + "):" + "<br/>" + veranstaltungenOhneKurseListe));
		}
		// build the array to assign the kurse to their reservations
		int[][] kurs_vor = new int[kurse.size()][reservations.size()];
		int i = 0;
		for (Allocatable a : kurse) {
			int j = 0;
			for (Reservation veranstaltung : reservations) {
				// check, if the reservation has allocated the kurs
				if (veranstaltung.hasAllocated(a)) {
					// yes
					kurs_vor[i][j] = 1;
				} else {
					// no
					kurs_vor[i][j] = 0;
				}
				j++;
			}
			i++;
		}
		return kurs_vor;
	}

	/**
	 * Implementiert wg. GLPK.
	 * 
	 * @see org.gnu.glpk.GLPK
	 */
	@Override
	public boolean output(String str) {
		hookUsed = true;
		System.out.print(str);
		return false;
	}

	/**
	 * 
	 * Implementiert wg. GLPK.
	 * 
	 * @see org.gnu.glpk.GLPK
	 */
	@Override
	public void callback(glp_tree tree) {
		int reason = GLPK.glp_ios_reason(tree);
		if (reason == GLPKConstants.GLP_IBINGO) {
			System.out.println("Better solution found");
		}
	}

	/**
	 * 
	 * Schreibt die Daten in die Datei mit der Problembeschreibung f�r den GLPK-Solver.
	 * 
	 * @param data_file - die Datei mit der Problembeschreibung f�r den GLPK-Solver
	 * @param doz_vor - die Matrix (int-Array), die jeder Verantstaltung ihre(n) Dozenten zurordnet
	 * @param kurs_vor - die Matrix (int-Array), die jeder Verantstaltung ihre(n) Kurs(e) zurordnet
	 * @param vor_res - die Matrix (int-Array) mit der Resourcenverfuegbarkeit
	 * @param doz_cost - die Matrix (int-Array), welche die Dozentenkosten abbildet
	 */
	private void aufbau_scheduler_data(String data_file, int[][] doz_vor,
			int[][] kurs_vor, int[][] vor_res, int[][] doz_cost) {
		String file = "data; \n";
		if (doz_vor.length == 0 || kurs_vor.length == 0 || vor_res.length == 0 || doz_cost.length == 0) {
			return;
		}

		// Anzahl Vorlesungen
		file += "set I :=";
		for (int i = 1; i <= vor_res.length; i++) {
			file += " " + i + "";
		}
		file += ";\n";

		// Anzahl Kurse
		file += "set K :=";
		for (int i = 1; i <= kurs_vor.length; i++) {
			file += " " + i + "";
		}
		file += ";\n";

		// Anzahl Timeslots
		file += "set T :=";
		for (int i = 1; i <= vor_res[0].length; i++) {
			file += " " + i + "";
		}
		file += ";\n";

		// Anzahl Dozenten
		file += "set D :=";
		for (int i = 1; i <= doz_vor.length; i++) {
			file += " " + i + "";
		}
		file += ";\n";

		// Zuordnung Dozent, Vorlesung
		file += "param doz_vor :  ";
		for (int i = 1; i <= doz_vor[0].length; i++) {
			file += "" + i + " ";
		}
		file += " :=";
		for (int i = 1; i <= doz_vor.length; i++) {
			file += "\n  " + i;
			for (int j = 0; j < doz_vor[0].length; j++) {
				file += " " + doz_vor[i - 1][j];
			}
		}
		file += "; \n";

		// Zuordnung Verfuegbarkeit Ressourcen
		file += "param vor_res :  ";
		for (int i = 1; i <= vor_res[0].length; i++) {
			file += "" + i + " ";
		}
		file += " :=";
		for (int i = 1; i <= vor_res.length; i++) {
			file += "\n  " + i;
			for (int j = 0; j < vor_res[0].length; j++) {
				file += " " + vor_res[i - 1][j];
			}
		}
		file += "; \n";

		// Dozenten Kosten
		file += "param doz_cost :  ";
		for (int i = 1; i <= doz_cost[0].length; i++) {
			file += "" + i + " ";
		}
		file += " :=";
		for (int i = 1; i <= doz_cost.length; i++) {
			file += "\n  " + i;
			for (int j = 0; j < doz_cost[0].length; j++) {
				file += " " + doz_cost[i - 1][j];
			}
		}
		file += "; \n";
		
		// Zuordnung Kurs, Vorlesung
		file += "param kurs_vor :  ";
		for (int i = 1; i <= kurs_vor[0].length; i++) {
			file += "" + i + " ";
		}
		file += " :=";
		for (int i = 1; i <= kurs_vor.length; i++) {
			file += "\n  " + i;
			for (int j = 0; j < kurs_vor[0].length; j++) {
				file += " " + kurs_vor[i - 1][j];
			}
		}
		file += "; \n";

		// Datei schreiben
		FileWriter fw;

		try {
			fw = new FileWriter(new File(data_file));
			fw.write(file);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Schreibt den Kopf der Datei f�r den GLPK-Solver.
	 * 
	 * @param mod_file - Pfad zur Datei mit der Problembeschreibung
	 * @param sol_file - Pfad zur Datei, in welche die Loesung geschrieben werden soll
	 */
	private void aufbau_scheduler_mod(String mod_file, String sol_file) {
		String file = "param f, symbolic := \"" + sol_file + "\"; \n\n";
		file += "#Anzahl Vorlesungen\n";
		file += "set I;\n";
		file += "#Anzahl Kurse\n";
		file += "set K;\n";
		file += "#Anzahl Timeslots\n";
		file += "set T;\n";
		file += "#Anzahl Dozenten\n";
		file += "set D;\n";
		file += "#Zuordnung Dozent, Vorlesung\n";
		file += "param doz_vor{d in D, i in I};\n";
		file += "#Verfuegbarkeit Ressourcen\n";
		file += "param vor_res{i in I, t in T};\n";
		file += "#Zuordnung Kurs, Vorlesung\n";
		file += "param kurs_vor{k in K, i in I};\n";
		file += "#Kosten Vorlesung, Timeslots\n";
		file += "param doz_cost{i in I, t in T};\n";
		file += "var x{i in I, t in T}, binary;\n";

		file += "maximize obj : sum{i in I, t in T}(vor_res[i,t]*x[i,t]*doz_cost[i,t]);\n";

		file += "#Veranstalung nur einmal planen\n";
		file += "s.t. veranst{i in I}: sum{t in T} x[i,t] <= 1;\n";

		file += "#Dozenten nicht doppelt\n";
		file += "s.t. dozent{d in D, t in T}: sum{i in I} (doz_vor[d,i]*x[i,t]) <= 1;\n";

		file += "#Kurs nicht doppelt\n";
		file += "s.t. kurs{k in K, t in T}: sum{i in I} (kurs_vor[k,i]*x[i,t]) <= 1;\n";

		file += "solve;\n";

		// file += "printf \"solution:\\n\" > f;\n";
		file += "printf {i in I, t in T: x[i,t] == 1} \"%i, %i \\n\", i, t >> f;\n";
		file += "printf \"\\n\">> f;\n";

		FileWriter fw;

		try {
			fw = new FileWriter(new File(mod_file));
			fw.write(file);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Liest die Loesung aus der entsprechenden Datei aus.
	 * 
	 * @param sol_file - Pfad zur Datei, in welche die Loesung geschrieben wurde
	 * @return String - Loesung
	 */
	private String auslese_Solution(String sol_file) {
		String auslese = "";
		String zeile;

		BufferedReader br;

		try {
			br = new BufferedReader(new FileReader(new File(sol_file)));
			while ((zeile = br.readLine()) != null) {
				auslese += zeile + "\n";
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return auslese;
	}
	
	/**
	 * 
	 * Beachten von Ausnahmeterminen der Dozenten.
	 * 
	 * @param startDatum - Start des Plaungszyklus
	 * @param endDatum - Ende des Planungszyklus
	 * @return String - Fehlermeldungen
	 * @throws RaplaException
	 */
	@SuppressWarnings("static-access")
	private String mindExceptionDates(Date startDatum, Date endDatum)
			throws RaplaException {

		String notResolved = "";
		// gehe �ber alle Reservations
		for (Reservation veranstaltung : reservationsPlannedByScheduler) {
			veranstaltung = getClientFacade().edit(veranstaltung);
			// get Constraints
			String dozentenConstraint = getDozentenConstraint(veranstaltung);
			// get Exception dates
			Date[] exceptionsDates = ConstraintService
					.getExceptionDates(dozentenConstraint);
			// gehe �ber alle Appointment
				for (Appointment a : splitIntoSingleAppointments(veranstaltung)) {
					for (Date exceptionDate : exceptionsDates) {
						if (exceptionDate != null) {
							Calendar calApp = Calendar.getInstance(DateTools.getTimeZone());
							Calendar calException = Calendar.getInstance(DateTools.getTimeZone());
							calApp.setTime(a.getStart());
							calException.setTime(exceptionDate);
							// pr�fe, ob nicht an einem Exception Dates
							if ((calApp.YEAR == calException.YEAR)
									&& (calApp.MONTH == calException.MONTH)
									&& (calApp.DAY_OF_MONTH == calException.DAY_OF_MONTH)) {
								// ggf. createNewAppointment() aufrufen
								Appointment newApp = createNewAppointment(veranstaltung, a.getRepeating(),
										startDatum, endDatum, a.getStart());
								Boolean couldMove = moveAppointmentWithDozConstraints(startDatum, endDatum, veranstaltung, newApp);
								if(!couldMove){
									notResolved = notResolved + "\n" + veranstaltung.getName(getLocale()) + "\n";
									break;
								}
							}
						}
					}
				}
				// Pruefung, ob noch innerhalb des Plannungszyklus
				boolean couldMove = true;
				veranstaltung = getClientFacade().edit(veranstaltung);
				for (Appointment a : splitIntoSingleAppointments(veranstaltung)) {
					Date appointStart = a.getStart();
					if (appointStart.after(endDatum)) {
						Appointment newAppointment = createNewAppointment(
								veranstaltung, a.getRepeating(), appointStart,
								a.getEnd(), a.getStart());
						boolean appMoved = moveAppointmentWithDozConstraints(
								startDatum, endDatum, veranstaltung,
								newAppointment);
						if(!appMoved){
							couldMove = false;
						}
					}
				}
				if (!couldMove) {
					notResolved += (veranstaltung.getName(getLocale())
							+ getString("beyond_planning_peroid") + "\n");
				}
			}
		return notResolved;
	}

	/**
	 * Versendet die Mail mit dem Link zur Abfrage der Constraints an die Dozenten. 
	 * Ist die Person noch nicht eingeladen, wird eine Einladung, andernsfalls eine Erinnerung versendet.
	 * 
	 * @param reservationID - ID der Veranstaltung
	 * @param dozentId - ID des Dozenten, an den die Email verschickt werden soll
	 * @param login - Name des eingeloggten Absenders
	 * @param url - URL zum Erfassungslink der Dozenten-Constraints
	 * 
	 * @throws RaplaException
	 */
	@Override
	public boolean sendMail(String reservationID, String dozentId, String login, String url) throws RaplaException {

		StorageOperator lookup = getContext().lookup(StorageOperator.class);
		final MailInterface mailClient = getContext().lookup(MailInterface.class);

		Reservation veranstaltung = (Reservation) lookup.resolve(reservationID);
		Allocatable dozent = (Allocatable) lookup.resolve(dozentId);

		boolean returnval = false;
		boolean isPerson = false;
		String email = "";
		String name = "";
		String vorname = "";
		String titel = "";

		isPerson = dozent.isPerson();
		
		
		// Dozent.getClassification().getValue("");
		if (isPerson) {
			
			email = (String) dozent.getClassification().getValue("email");
			name = (String) dozent.getClassification().getValue("surname");
			vorname = (String) dozent.getClassification().getValue("firstname");
			titel = (String) dozent.getClassification().getValue("title");

			if(email == null || email.equals("leer") || email.equals("")){
				return false;
			}
			String studiengang = "";
			if (veranstaltung.getClassification().getValue("studiengang") != null) {
				studiengang = veranstaltung.getClassification().getValue("studiengang").toString();
				if (studiengang.contains(" ")) {
					int pos = studiengang.indexOf(" ");
					studiengang = studiengang.substring(0, pos);
				}
			}
			String veranstaltungstitel = (String) veranstaltung.getClassification().getValue("title");
			String betreff;

			ClientFacade facade = getContext().lookup(ClientFacade.class);
			Preferences prefs = facade.getPreferences(null);
			final String defaultSender = prefs.getEntryAsString(MailPlugin.DEFAULT_SENDER_ENTRY, "");
			
			String constraint = (String) veranstaltung.getClassification().getValue("planungsconstraints");
			int status = ConstraintService.getStatus(constraint, dozentId);

			// 1 = eingeladen das bedeutet er hat shcon eine Mail bekommen und muss erinnert werden.
			if (status == 1) {
				betreff = getString("email_Betreff_Erinnerung");
			} else {
				betreff = getString("email_Betreff_Einladung");
			}
			betreff += veranstaltungstitel;

			String Inhalt = getString("email_anrede") + titel + " " + vorname
					+ " " + name + ",\n\n" + getString("email_Inhalt") + "\n"
					+ veranstaltungstitel + " (" + studiengang + ")" + "\n\n"
					+ getString("Link_Text") + "\n"
					+ // <a href=" + url+ ">" + url + "</a> \n\n" +
					url + "\n\n" + getString("email_Signatur") + "\n" + login
					+ "\n";
			// getUser().getEmail();

			try {
				mailClient.sendMail(defaultSender, email, betreff, Inhalt);
				returnval = true;
			} catch (MailException e) {
				e.printStackTrace();
				returnval = false;
			}

		} else {
			;
		}

		return returnval;
	}
}
