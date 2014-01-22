package org.rapla.plugin.dhbwscheduler.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import org.rapla.components.util.ParseDateException;
import org.rapla.components.util.SerializableDateTimeFormat;
import org.rapla.components.util.undo.CommandUndo;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.configuration.Preferences;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.Period;
import org.rapla.entities.domain.Repeating;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.domain.internal.AppointmentImpl;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.ClientFacade;
import org.rapla.facade.Conflict;
import org.rapla.facade.ModificationModule;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.framework.StartupEnvironment;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.ReservationController;
import org.rapla.gui.internal.edit.reservation.AppointmentController;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.plugin.freetime.server.FreetimeService;
import org.rapla.plugin.mail.MailException;
import org.rapla.plugin.mail.MailPlugin;
import org.rapla.plugin.mail.server.MailInterface;
import org.rapla.plugin.urlencryption.UrlEncryption;
import org.rapla.server.RemoteMethodFactory;
import org.rapla.server.RemoteSession;
import org.rapla.storage.StorageOperator;

/**
 * @author DHBW
 *
 */
@SuppressWarnings({ "unused", "restriction" })
public class DhbwschedulerServiceImpl extends RaplaComponent implements
		GlpkCallbackListener, GlpkTerminalListener,
		RemoteMethodFactory<DhbwschedulerService>, DhbwschedulerService {

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
	private ArrayList<Reservation> reservationsPlannedByScheduler = new ArrayList<Reservation>();

	private FreetimeService freetimeService = null;

	/**
	 * @param context
	 */
	public DhbwschedulerServiceImpl(RaplaContext context) {
		super(context);
		setChildBundleName(DhbwschedulerPlugin.RESOURCE_FILE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapla.server.RemoteMethodFactory#createService(org.rapla.server.RemoteSession)
	 */
	@Override
	public DhbwschedulerService createService(RemoteSession remoteSession) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rapla.plugin.dhbwscheduler.DhbwschedulerService#schedule(org.rapla.entities.storage.internal.SimpleIdentifier[])
	 */
	@Override
	public String schedule(SimpleIdentifier[] reservationIds)
			throws RaplaException {
		StorageOperator lookup = getContext().lookup(StorageOperator.class);
		reservations = new ArrayList<Reservation>();
		for (SimpleIdentifier id : reservationIds) {
			RefEntity<?> object = lookup.resolve(id);
			Reservation reservation = (Reservation) object;
			
			String string = getString("planning_closed");
			Object value = reservation.getClassification().getValue("planungsstatus");
			if (value.equals(string)) {
				reservations.add(reservation);
			}
		}

		String postProcessingResults = "";

		try {
			freetimeService = getService(FreetimeService.class);
		} catch (UnsupportedOperationException e) {
			postProcessingResults += "<br>" + getString("no_holiday_plugin") + "<br/";
		}

		Calendar tmp = Calendar.getInstance(DateTools.getTimeZone());
		tmp.set(Calendar.DAY_OF_MONTH, 20);
		tmp.set(Calendar.MONTH, 0);
		tmp.set(Calendar.YEAR, 2014);
		tmp.set(Calendar.HOUR_OF_DAY, 0);
		tmp.set(Calendar.MINUTE, 0);
		tmp.set(Calendar.SECOND, 0);
		tmp.set(Calendar.MILLISECOND, 0);

		Date startDatum = new Date(tmp.getTimeInMillis()); // TODO: Auf jeden Fall noch zu fuellen

		tmp.set(Calendar.DAY_OF_MONTH, 17);
		tmp.set(Calendar.MONTH, 1);

		Date endeDatum = new Date(tmp.getTimeInMillis()); // TODO: Auf jeden Fall noch zu fuellen

		Date anfangWoche = startDatum;

		tmp.setTime(anfangWoche);
		tmp.add(Calendar.DAY_OF_YEAR, 5);

		Date endeWoche = new Date(tmp.getTimeInMillis());

		// plane solange, wie der Anfang der neuen Woche vor dem Ende des Planungszyklus liegt
		while ((anfangWoche.before(endeDatum)) && (!(reservations.isEmpty()))) {

			// PRE-Processing
			preProcessing(anfangWoche, endeWoche);

			// Schedule
			aufbau_scheduler_mod(model, solution);

//			aufbau_scheduler_data(data, doz_vor, kurs_vor, vor_res);
			aufbau_scheduler_data(data, doz_vor, kurs_vor, vor_res, doz_cost);

			solve();

			// POST-Processing
			postProcessingResults += ("\n" + postProcessing(anfangWoche, endeWoche));

			// neue Woche planen
			tmp.setTime(anfangWoche);
			tmp.add(Calendar.DAY_OF_YEAR, 7);
			anfangWoche = new Date(tmp.getTimeInMillis());
			tmp.add(Calendar.DAY_OF_YEAR, 5);
			endeWoche = new Date(tmp.getTimeInMillis());
		}

		if(reservations.size() == 0) {
			// Alle Veranstaltungen geplant
			return postProcessingResults;
		} else {
			String result = getString("planning_not_successful") + "\n";
			for(int i = 0; i < reservations.size(); i++) {
				result += reservations.get(0).getClassification().getName(getLocale()) + "\n";
				reservations.remove(0);
			}
			return result;
		}
	}

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
	 * @param startDatum
	 * @param endeDatum
	 * @return
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
	 * @return
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
			throw (new RaplaException("<br>" + getString("missing_planing_constraints") + "<br/>" + veranstaltungenOhnePlanungsconstraintsListe));
		}
		return doz_cost;
	}

	/**
	 * @param Dozentenconstraint
	 * @return int[]
	 */
	private int[] splitDozentenKostenSlots(String dozentenConstraint) {
		int[] belegteSlots = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		int[] dozConst = ConstraintService.getDozConstraints(dozentenConstraint);

		int slotCounter = 0;

		for (int i = 24; i < dozConst.length - 24; i++) {
			int stundenCounter = (i % 24);
			belegteSlots[slotCounter] += dozConst[i];

			if (stundenCounter == 11) {
				slotCounter++;
			}
		}
		return belegteSlots;
	}
	
	/**
	 * 
	 * @param startDatum
	 * @param endeDatum
	 */
	private String postProcessing(Date startDatum, Date endeDatum) throws RaplaContextException, RaplaException {
		String solutionString = auslese_Solution(solution);
		String result = "";
		Calendar tmp = Calendar.getInstance(DateTools.getTimeZone());

		Date newStart = new Date(tmp.getTimeInMillis());

		int[][] solRes = splitSolution(solutionString);

		for (int i = solRes.length - 1; i >= 0; i--) {
			Reservation reservation = reservations.get((solRes[i][0]) - 1);
			result += reservation.getClassification().getName(getLocale()) + ": " + solRes[i][1] + "\n";
			reservation = getClientFacade().edit(reservation);
			Appointment appointment = reservation.getAppointments()[0];
			Allocatable[] allocatables = reservation.getAllocatablesFor(appointment);

			// String dozConstraint = getDozentenConstraint(reservation);
			// int[] dozConstr = splitDozentenConstraint(dozConstraint);
			// Slot-Datum einstellen
			newStart = setStartDate(solRes[i][1], startDatum, endeDatum, appointment, allocatables, reservation);
			appointment.move(newStart);

			getClientFacade().store(reservation);
			reservations.remove((solRes[i][0]) - 1);
			reservationsPlannedByScheduler.add(reservation);
		}

		//TODO: Kommentar entfernen
		// Dateien aufräumen
/*		new File(model).delete();
		new File(data).delete();
		new File(solution).delete();
*/
		return result;
	}

	/**
	 * 
	 * @param solution
	 * @return
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
	 * @param slot
	 * @param startDate
	 * @param endDate
	 * @param appointment
	 * @param allocatables
	 * @param reservation
	 * @return
	 * @throws RaplaException
	 */
	private Date setStartDate(int slot, Date startDate, Date endDate, Appointment appointment, Allocatable[] allocatables, Reservation reservation) throws RaplaException {
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
		cal.add(Calendar.MINUTE, -15);

		// Dozenten Constraint beachten
		// TODO: muss noch getestet werden.

		int[] dozConstr = ConstraintService.getDozConstraints(getDozentenConstraint(reservation));

		int start = 24 + (slot * 12);
		for (int i = start; i < start + 12; i++) {
			int index = i;
			if ((slot % 2) == 0) {
				// Morgens 000000001111
				index += cal.get(Calendar.HOUR_OF_DAY) - 1;
			} else {
				// Abends 111111100000
				index += cal.get(Calendar.HOUR_OF_DAY) - 13;
			}

			if (dozConstr[index] > 0) {
				break;
			} else {
				cal.add(Calendar.HOUR, 1);
			}
		}

		newStart = new Date(cal.getTimeInMillis());

		Date oldDate = appointment.getStart();

		appointment.move(newStart);

		// Alle gerade geplanten Reservierungen bei der Betrachtung ausschließen
		StorageOperator lookup = getContext().lookup(StorageOperator.class);

		Integer worktimeStartMinutes = getCalendarOptions().getWorktimeStartMinutes();
		Integer worktimeEndMinutes = getCalendarOptions().getWorktimeEndMinutes();

		Integer[] excludedDays = getCalendarOptions().getExcludeDays().toArray(new Integer[] {});
		Integer rowsPerHour = getCalendarOptions().getRowsPerHour();

		HashSet<Reservation> ignoreList = getIgnoreList(allocatables, startDate, endDate);

		newStart = lookup.getNextAllocatableDate(Arrays.asList(allocatables), appointment, ignoreList, worktimeStartMinutes, worktimeEndMinutes, excludedDays, rowsPerHour);

		appointment.move(oldDate);

		return newStart;
	}

	/**
	 * 
	 * @param allocatables
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RaplaException
	 */
	private HashSet<Reservation> getIgnoreList(Allocatable[] allocatables, Date startDate, Date endDate) throws RaplaException {
		HashSet<Reservation> ignoreList = new HashSet<Reservation>();

		Reservation[] vorlesungenMitGleichenResourcen = getClientFacade().getReservationsForAllocatable(allocatables, startDate, endDate, null);
		for (Reservation vorlesungMitGleicherResource : vorlesungenMitGleichenResourcen) {
			// for each of these reservations, look if there are in closed
			if (vorlesungMitGleicherResource.getClassification().getValue("planungsstatus").equals(getString("planning_closed"))) {
				ignoreList.add(vorlesungMitGleicherResource);
			}
		}
		return ignoreList;
	}

	/**
	 * @param start - Anfang der Woche
	 * @param ende  - Ende der Woche
	 * @return int[][]
	 * @throws RaplaException
	 */
	private int[][] buildAllocatableVerfuegbarkeit(Date start, Date ende) throws RaplaException {

		//TODO: pruefen ob diese Methode sauber arbeitet. Ich bin mir nicht sicher
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
			String[][] holidays = freetimeService.getHolidays(start, ende);
			for (int i = 0; i < holidays.length; i++) {
				String holiday = holidays[i][0];
				SerializableDateTimeFormat dateFormat = new SerializableDateTimeFormat();
				try {
					Date holidayDate = dateFormat.parseDate(holiday, false);
					Calendar c = Calendar.getInstance();
					c.setTime(holidayDate);
					int dayOfWeekOfHoliday = c.get(Calendar.DAY_OF_WEEK);
					for (int j = 0; j < reservations.size(); j++) {
						vor_res[j][timeSlots[dayOfWeekOfHoliday][0]] = 0;
						vor_res[j][timeSlots[dayOfWeekOfHoliday][1]] = 0;
					}
				} catch (ParseDateException e) {
					getLogger().warn(e.getLocalizedMessage());
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
				if (vorlesungMitGleicherResource.getClassification().getValue("planungsstatus").equals(getString("closed"))
						|| reservationsPlannedByScheduler.contains(vorlesungMitGleicherResource)) {
					// nur geplante Veranstaltungen muessen beachtet werden
					// Appointment[] termine = vorlesungMitGleicherResource.getAppointments();
					Appointment[] termine = splitIntoSingleAppointments(vorlesungMitGleicherResource);
					for (Appointment termin : termine) {
						Date beginn = termin.getStart();
						Calendar cal = Calendar.getInstance();
						cal.setTime(beginn);

						if (termin.isWholeDaysSet()) {
							vor_res[vorlesungNr][timeSlots[cal.get(Calendar.DAY_OF_WEEK)][0]] = 0;
							vor_res[vorlesungNr][timeSlots[cal.get(Calendar.DAY_OF_WEEK)][1]] = 0;
						} else {
							// Prüfung, ob innerhalb von Start und Ende
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
		// TODO: ExeptionDates vom Dozenten beachten

		return vor_res;
	}

	/**
	 * 
	 * @param vorlesungMitGleicherResource
	 * @return
	 * @throws RaplaException
	 */
	private Appointment[] splitIntoSingleAppointments(Reservation vorlesungMitGleicherResource) throws RaplaException {
		List<Appointment> splitAppointments = new ArrayList<Appointment>();

		// Generate time blocks from selected appointment
		Appointment[] termine = vorlesungMitGleicherResource.getAppointments();
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
	 * @param reservation
	 * @return
	 */
	private String getDozentenConstraint(Reservation reservation) {

		Object constraintObj = reservation.getClassification().getValue(getString("planning_constraints"));
		String result = "";
		if (constraintObj != null) {
			result = constraintObj.toString();
		}
		return result;
	}

	/**
	 * @param Dozentenconstraint
	 * @return int[]
	 */
	private int[] splitDozentenConstraintSlots(String dozentenConstraint) {
		int[] belegteSlots = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		int[] dozConst = ConstraintService.getDozConstraints(dozentenConstraint);

		int slotCounter = 0;

		for (int i = 24; i < dozConst.length - 24; i++) {
			int stundenCounter = (i % 24);
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
	 * @param reservation
	 * @return int[][]
	 * @throws RaplaException
	 */
	private int[][] buildZuordnungDozentenVorlesung() throws RaplaException {
		Set<Allocatable> dozenten = new HashSet<Allocatable>();
		ArrayList<Reservation> veranstaltungenOhneDozent = new ArrayList<Reservation>();
		String type = "";
		for (DynamicType alltype : getClientFacade().getDynamicTypes("resource")) {
			if (alltype.getElementKey().equals("professor")) {
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
				if (allocatableType.getElementKey().equals("professor")) {
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
	 * @param reservation
	 * @return int[][]
	 * @throws RaplaException
	 */
	private int[][] buildZuordnungKursVorlesung() throws RaplaException {
		Set<Allocatable> kurse = new HashSet<Allocatable>();
		ArrayList<Reservation> veranstaltungenOhneKurse = new ArrayList<Reservation>();
		String type = "";
		for (DynamicType alltype : getClientFacade().getDynamicTypes("resource")) {
			if (alltype.getElementKey().equals("kurs")) {
				type = alltype.getName(getLocale());
			}
		}
		for (Reservation veranstaltung : reservations) {
			// get all resources for all reservations
			Allocatable[] ressourcen = veranstaltung.getAllocatables();
			boolean hasKurs = false;
			for (Allocatable a : ressourcen) {
				if (a.getClassification().getType().getElementKey().equals("kurs")) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gnu.glpk.GLPK
	 */
	@Override
	public boolean output(String str) {
		hookUsed = true;
		System.out.print(str);
		return false;
	}

	/*
	 * (non-Javadoc)
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
	 * @param data_file
	 * @param doz_vor
	 * @param kurs_vor
	 * @param vor_res
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

		// System.out.println(file);

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
	 * @param mod_file
	 * @param sol_file
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
	 * @param sol_file
	 * @return String
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

	@Override
	public boolean sendMail(SimpleIdentifier reservationID, SimpleIdentifier dozentId, String login, String url) throws RaplaException {

		StorageOperator lookup = getContext().lookup(StorageOperator.class);
		final MailInterface MailClient = getContext().lookup(MailInterface.class);

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
			int status = ConstraintService.getStatus(constraint, dozentId.getKey());

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
				MailClient.sendMail(defaultSender, email, betreff, Inhalt);
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
