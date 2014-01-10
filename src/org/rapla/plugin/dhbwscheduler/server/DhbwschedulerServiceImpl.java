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
import java.util.HashSet;
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
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.ClientFacade;
import org.rapla.facade.Conflict;
import org.rapla.facade.ModificationModule;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.ReservationController;
import org.rapla.gui.internal.edit.reservation.AppointmentController;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.server.RemoteMethodFactory;
import org.rapla.server.RemoteSession;
import org.rapla.storage.StorageOperator;


/**
 * @author DHBW
 *
 */
public class DhbwschedulerServiceImpl extends RaplaComponent implements GlpkCallbackListener, GlpkTerminalListener, RemoteMethodFactory<DhbwschedulerService>, DhbwschedulerService {

	int[][] timeSlots = {{},{},{0,1},{2,3},{4,5},{6,7},{8,9}};
    private boolean hookUsed = false;
	private String model = "scheduler_gmpl" + new Date().getTime() + ".mod";
	private String data = "scheduler_data" + new Date().getTime() + ".dat";
	private String solution = "scheduler_solution" + new Date().getTime() + ".dat";

	private int doz_vor[][] = {{}};
	private int vor_res[][] = {{}};
	private int kurs_vor[][] = {{}};
	private ArrayList<Reservation> reservations;
	
	/**
	 * @param context
	 */
	public DhbwschedulerServiceImpl(RaplaContext context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see org.rapla.server.RemoteMethodFactory#createService(org.rapla.server.RemoteSession)
	 */
	@Override
	public DhbwschedulerService createService(RemoteSession remoteSession) {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.rapla.plugin.dhbwscheduler.DhbwschedulerService#schedule(org.rapla.entities.storage.internal.SimpleIdentifier[])
	 */
	@Override
	public String schedule(SimpleIdentifier[] reservationIds)  throws RaplaException {		
		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		reservations = new ArrayList<Reservation>();
		for ( SimpleIdentifier id :reservationIds)
		{
			RefEntity<?> object = lookup.resolve( id);
			Reservation reservation = (Reservation) object;
			if(reservation.getClassification().getValue("planungsstatus").equals("in Planung geschlossen")){
				reservations.add(reservation);	
			}
		}
		
		String postProcessingResults = "";
		
		Calendar tmp = Calendar.getInstance(DateTools.getTimeZone());
		tmp.set(Calendar.DAY_OF_MONTH, 6);
		tmp.set(Calendar.MONTH, 0);
		tmp.set(Calendar.YEAR, 2014);
		tmp.set(Calendar.HOUR_OF_DAY, 0);
		tmp.set(Calendar.MINUTE, 0);
		tmp.set(Calendar.SECOND, 0);
		tmp.set(Calendar.MILLISECOND, 0);
        
		Date startDatum = new Date(tmp.getTimeInMillis());   // TODO: Auf jeden Fall noch zu fuellen 

		tmp.set(Calendar.DAY_OF_MONTH, 10);

		Date endeDatum = new Date(tmp.getTimeInMillis());    // TODO: Auf jeden Fall noch zu fuellen 
		
		Date anfangWoche = startDatum;
		
		tmp.setTime(anfangWoche);
	    tmp.add(Calendar.DAY_OF_YEAR, 5);
		
		Date endeWoche = new Date(tmp.getTimeInMillis());
		
	    //plane solange, wie der Anfang der neuen Woche vor dem Ende des Planungszyklus liegt		
		while(anfangWoche.before(endeDatum)) {
		
			//PRE-Processing
			preProcessing(anfangWoche, endeWoche);
			
			//Schedule
			aufbau_scheduler_mod(model, solution);
			
			aufbau_scheduler_data(data, doz_vor, kurs_vor, vor_res);
	    	
	        solve();
	        
	        //POST-Processing
	        postProcessingResults += ("\n" + postProcessing(anfangWoche, endeWoche));
	        
	        //neue Woche planen
	        tmp.setTime(anfangWoche);
	        tmp.add(Calendar.DAY_OF_YEAR, 7);
	        anfangWoche = new Date(tmp.getTimeInMillis());
	        tmp.add(Calendar.DAY_OF_YEAR, 5);
	        endeWoche = new Date(tmp.getTimeInMillis());
		}  
        
		getClientFacade().refresh();
        return postProcessingResults;         	
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
//        System.out.println("Problem created");

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
	private void preProcessing(Date startDatum, Date endeDatum) throws RaplaException{
		String result = "";
		
		try {
			doz_vor = buildZuordnungDozentenVorlesung();
		} catch(RaplaException e) {
			result += e.getMessage();
		}
		
		try {
			kurs_vor = buildZuordnungKursVorlesung();
		} catch(RaplaException e) {
			result += e.getMessage();
		}
		
		try {
			vor_res = buildAllocatableVerfuegbarkeit(startDatum, endeDatum);
		} catch(RaplaException e) {
			result += e.getMessage();
		}
		
		if (!result.isEmpty()) {
			throw new RaplaException(result);
		}
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
        
        for(int i = solRes.length-1; i >= 0 ;i--) {
        	Reservation reservation = reservations.get((solRes[i][0]) - 1);
        	result += reservation.getClassification().getName(getLocale()) + ": " + solRes[i][1] + "\n";
        	reservation = getClientFacade().edit(reservation);
        	Appointment appointment = reservation.getAppointments()[0];
    		Allocatable[] allocatables = reservation.getAllocatablesFor( appointment);

    		//Slot-Datum einstellen
        	newStart = setStartDate(solRes[i][1], startDatum, appointment, allocatables);
    		appointment.move(newStart);
			
    		getClientFacade().store(reservation);
    		reservations.remove((solRes[i][0])-1);
        }
        
        //Dateien aufräumen
        new File(model).delete();
        new File(data).delete();
        new File(solution).delete();
        
        return result;
	}
	
	/**
	 * 
	 * @param solution
	 * @return
	 */
	private int[][] splitSolution(String solutionString){
		String[] solReservations = solutionString.split("\n");
		
		int[][] solutionArray = new int[solReservations.length][2];
		int i = 0;
		
		for(String solRes : solReservations){
			if (solRes.indexOf(",") > -1) {
				solutionArray[i][0] = Integer.valueOf(solRes.substring(0, solRes.indexOf(",")).trim());
				solutionArray[i][1] = Integer.valueOf(solRes.substring(solRes.indexOf(",")+1 ).trim());
				i++;
			}
		}
		
		return solutionArray;
	}
	
	/**
	 * 
	 * @param slot
	 * @return
	 */
	private Date setStartDate(int slot, Date startDate, Appointment appointment, Allocatable[]  allocatables) throws RaplaException {
		Date newStart;
		
		Calendar cal = Calendar.getInstance(DateTools.getTimeZone());
		cal.setTimeInMillis(startDate.getTime());
		if (slot > 1) {
			cal.add(Calendar.HOUR, (slot-1)*12);
		}
		if( cal.get(Calendar.HOUR_OF_DAY) < (getCalendarOptions().getWorktimeStartMinutes() / 60)) {
			cal.add(Calendar.MINUTE, getCalendarOptions().getWorktimeStartMinutes());
		}
		newStart = new Date(cal.getTimeInMillis());
		
		appointment.move(newStart);
		
		newStart = getQuery().getNextAllocatableDate(Arrays.asList(allocatables), appointment,getCalendarOptions() );

		//TODO: getNextFreeTime + Constraints prüfen
		return new Date(cal.getTimeInMillis());
	}
	
	/**
	 * @param start
	 * @param ende
	 * @param dozentenConstraint
	 * @return int[][]
	 */
	private int[][] buildDozentenConstraint(Date start, Date ende, String dozentenConstraint){
		//TODO: Kostenmatrix für Dozentenwünsche
		return null;
	}
	
	/**
	 * @param start - Anfang der Woche
	 * @param ende - Ende der Woche
	 * @return int[][]
	 * @throws RaplaException 
	 */
	
	@SuppressWarnings("static-access")
	private int[][] buildAllocatableVerfuegbarkeit(Date start, Date ende) throws RaplaException {
		//build array, first all times are allowed
		int[][] vor_res = new int[reservations.size()][10];
		for (int i = 0; i < reservations.size(); i++){
			for (int j = 0; j < 10; j++){
				vor_res[i][j] = 1;
			}
		}
		ArrayList<Reservation> veranstaltungenOhnePlanungsconstraints = new ArrayList<Reservation>();
		int vorlesungNr = 0;
		for (Reservation vorlesung : reservations) {
			//get all resources from all reservations
			Allocatable[] allocatables = vorlesung.getAllocatables();
			//get all other reservations for these resources
			Reservation[] vorlesungenMitGleichenResourcen = getClientFacade().getReservationsForAllocatable(allocatables, start, ende, null);
			for (Reservation vorlesungMitGleicherResource : vorlesungenMitGleichenResourcen){
				//for each of these reservations, look if there are "geplant"
				if(vorlesungMitGleicherResource.getClassification().getValue("planungsstatus").equals("geplant")){
					//nur geplante Veranstaltungen muessen beachtet werden
					Appointment[] termine = vorlesungMitGleicherResource.getAppointments();
					for (Appointment termin : termine){
						Date beginn = termin.getStart();
						Calendar cal = Calendar.getInstance(DateTools.getTimeZone());
						cal.setTime(beginn);
						//TODO: Prüfung ob innerhalb von Start und Ende notwendig ??
						if(cal.after(start) && cal.before(ende)){
							if(cal.HOUR_OF_DAY < 12){
								//set the field for the vorlesungNr and the slot to zero
								//if the appointment starts before 12 a.m., the appointment will block
								//the slot at the morning
								vor_res[vorlesungNr][timeSlots[cal.DAY_OF_WEEK][0]] = 0;
							} else {
								//else the appointment is after 12 a.m. and it will block
								//the slot at the afternoon
							vor_res[vorlesungNr][timeSlots[cal.DAY_OF_WEEK][1]] = 0;
							}
						}
					}
				}
			}
			//get the planungsconstraints 
			Object constraintObj = vorlesung.getClassification().getValue("planungsconstraints");
			if(constraintObj == null){
				veranstaltungenOhnePlanungsconstraints.add(vorlesung);
			} else {
				String planungsconstraint = constraintObj.toString();
				//get the slots blocked by the planungsconstraints
				int[] belegteSlots = splitDozentenConstraint(planungsconstraint);
				for(int i = 0; i < 10; i++){
					//copy the blocking only if the slot turns from (not) allowed to not allowed
					if(belegteSlots[i] == 0){
						vor_res[vorlesungNr][i] = belegteSlots[i];
					}
				}
			}
			vorlesungNr++;
		}
		if(!(veranstaltungenOhnePlanungsconstraints.isEmpty())){
			String veranstaltungenOhnePlanungsconstraintsListe = "";
			for(Reservation r : veranstaltungenOhnePlanungsconstraints){
				veranstaltungenOhnePlanungsconstraintsListe += r.getName(getLocale()) + "\n";
			}
			throw(new RaplaException("Bei folgenden Verantstaltungen fehlen die Planungsconstraints der Dozenten: \n" + veranstaltungenOhnePlanungsconstraintsListe));
		}
		return vor_res;
	}
	
	/**
	 * @param Dozentenconstraint
	 * @return int[]
	 */
	private int[] splitDozentenConstraint(String dozentenConstraint) {
		//first, all slots aren't allowed
		int[] belegteSlots = {0,0,0,0,0,0,0,0,0,0};
		int idIndex = dozentenConstraint.indexOf('_');
		dozentenConstraint = dozentenConstraint.substring(idIndex + 1);
		String[] constraintsTage = dozentenConstraint.split(";");
		for(String constraint : constraintsTage){
			//get the day of week
			int dayOfWeek = Calendar.MONDAY;
			char day = constraint.charAt(0);
			switch(day){
			case '1':
				break;
			case '2':
				dayOfWeek = Calendar.TUESDAY;
				break;
			case '3':
				dayOfWeek = Calendar.WEDNESDAY;
				break;
			case '4':
				dayOfWeek = Calendar.THURSDAY;
				break;
			case '5':
				dayOfWeek = Calendar.FRIDAY;
				break;
			}
			//get time at that day
			String timepoint = constraint.substring(constraint.indexOf(':') + 1, constraint.indexOf('-'));
			int hourOfDay = Integer.valueOf(timepoint);
			if(hourOfDay < 12){
				//if the time is before 12 a.m., the slot at the morning will 
				//be allowed
				belegteSlots[timeSlots[dayOfWeek][0]] = 1;
			} else {
				//else the slot in the afternoon will be allowed
				belegteSlots[timeSlots[dayOfWeek][1]] = 1;
			}
			//look for more constraints separated by commas
			if(constraint.contains(",")){
				timepoint = constraint.substring(constraint.indexOf(',') + 1, constraint.indexOf('-'));
				hourOfDay = Integer.valueOf(timepoint);
				//normally, the next available slot should be the the afternoon slot
				if(hourOfDay > 12){
					//check, if it's really the afternoon slot
					belegteSlots[timeSlots[dayOfWeek][1]] = 1;
				}
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
		for (Reservation veranstaltung : reservations){
			boolean hasProfessor = false;
			//get all resources for all reservations
			Allocatable[] ressourcen = veranstaltung.getAllocatables();
			for (Allocatable a : ressourcen){
				//if the resource is a professor, add it to the set (no duplicate elements allowed)
				if(a.getClassification().getType().getElementKey().equals("professor")){
					dozenten.add(a);
					hasProfessor = true;
				}
			}
			if(!hasProfessor){
				veranstaltungenOhneDozent.add(veranstaltung);
			}
		}
		if(!(veranstaltungenOhneDozent.isEmpty())){
			String veranstaltungenOhneDozentenListe = "";
			for(Reservation r : veranstaltungenOhneDozent){
				veranstaltungenOhneDozentenListe += r.getName(getLocale()) + "\n";
			}
			throw(new RaplaException("Bei folgenden Verantstaltungen fehlt ein Dozent: \n" + veranstaltungenOhneDozentenListe));
		}
		//build the array to assign the professors to their reservations 
		int[][] doz_vor = new int[dozenten.size()][reservations.size()];
		int i = 0;
		for (Allocatable a : dozenten){
			int j = 0;
			for(Reservation veranstaltung : reservations){
				//check, if the reservation has allocated the professor
				if(veranstaltung.hasAllocated(a)){
					//yes
					doz_vor[i][j] = 1;
				} else {
					//no
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
	private int[][] buildZuordnungKursVorlesung() throws RaplaException{
		Set<Allocatable> kurse = new HashSet<Allocatable>();
		ArrayList<Reservation> veranstaltungenOhneKurse = new ArrayList<Reservation>();
		for (Reservation veranstaltung : reservations){
			//get all resources for all reservations
			Allocatable[] ressourcen = veranstaltung.getAllocatables();
			boolean hasKurs = false;
			for (Allocatable a : ressourcen){
				if(a.getClassification().getType().getElementKey().equals("professor")){
					//if the resource is a kurs, add it to the set (no duplicate elements allowed)
					kurse.add(a);
					hasKurs = true;
				}
			}
			if(!hasKurs){
				veranstaltungenOhneKurse.add(veranstaltung);
			}
		}
		if(!(veranstaltungenOhneKurse.isEmpty())){
			String veranstaltungenOhneKurseListe = "";
			for(Reservation r : veranstaltungenOhneKurse){
				veranstaltungenOhneKurseListe += r.getName(getLocale()) + "\n";
			}
			throw(new RaplaException("Bei folgenden Verantstaltungen fehlt ein Kurs: \n" + veranstaltungenOhneKurseListe));
		}
		//build the array to assign the kurse to their reservations 
		int[][] kurs_vor = new int[kurse.size()][reservations.size()];
		int i = 0;
		for (Allocatable a : kurse){
			int j = 0;
			for(Reservation veranstaltung : reservations){
				//check, if the reservation has allocated the kurs
				if(veranstaltung.hasAllocated(a)){
					//yes
					kurs_vor[i][j] = 1;
				} else {
					//no
					kurs_vor[i][j] = 0;
				}
				j++;
			}
			i++;
		}
		return kurs_vor;
	}

	/**
	 * @param reservationId
	 * @param startNeu
	 * @return boolean
	 */
	private boolean editReservation(SimpleIdentifier reservationId, Date startNeu) {
		return false;
	}
	
	/**
	 * @param appointmentId
	 * @param startNeu
	 * @return boolean
	 */
	private boolean editAppointment(SimpleIdentifier appointmentId, Date startNeu) {
		return false;
	}

    /* (non-Javadoc)
	 * @see org.gnu.glpk.GLPK
     * 
     */
    @Override
    public boolean output(String str) {
        hookUsed = true;
        System.out.print(str);
        return false;
    }
    
    /* (non-Javadoc)
	 * @see org.gnu.glpk.GLPK
     * 
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
    private void aufbau_scheduler_data(String data_file, int[][] doz_vor, int[][] kurs_vor, int[][] vor_res){
    	String file = "data; \n";
    	if( doz_vor.length == 0 || kurs_vor.length == 0 || vor_res.length == 0 ) {
    		return;
    	}
    	
    	//Anzahl Vorlesungen
    	file += "set I :=";
    	for(int i = 1; i <= vor_res.length; i++) {
    		file += " " + i + "";
    	}
    	file += ";\n";
    	
    	//Anzahl Kurse
    	file += "set K :=";
    	for(int i = 1; i <= kurs_vor.length; i++) {
    		file += " " + i + "";
    	}
    	file += ";\n";
    	
    	//Anzahl Timeslots
    	file += "set T :=";
    	for(int i = 1; i <= vor_res[0].length; i++) {
    		file += " " + i + "";
    	}
    	file += ";\n";
    	
    	//Anzahl Dozenten
    	file += "set D :=";
    	for(int i = 1; i <= doz_vor.length; i++) {
    		file += " " + i + "";
    	}
    	file += ";\n";
    	
    	//Zuordnung Dozent, Vorlesung
    	file += "param doz_vor :  ";
    	for(int i = 1; i <= doz_vor[0].length; i++) {
    		file += "" + i + " ";
    	}
    	file += " :=";
    	for(int i = 1; i <= doz_vor.length; i++) {
    		file += "\n  " + i;
    		for(int j = 0; j < doz_vor[0].length; j++) {
    			file += " " + doz_vor[i-1][j];
    		}
    	}
		file += "; \n";

    	//Zuordnung Verfuegbarkeit Ressourcen
    	file += "param vor_res :  ";
    	for(int i = 1; i <= vor_res[0].length; i++) {
    		file += "" + i + " ";
    	}
    	file += " :=";
    	for(int i = 1; i <= vor_res.length; i++) {
    		file += "\n  " + i;
    		for(int j = 0; j < vor_res[0].length; j++) {
    			file += " " + vor_res[i-1][j];
    		}
    	}
		file += "; \n";

    	//Zuordnung Kurs, Vorlesung
    	file += "param kurs_vor :  ";
    	for(int i = 1; i <= kurs_vor[0].length; i++) {
    		file += "" + i + " ";
    	}
    	file += " :=";
    	for(int i = 1; i <= kurs_vor.length; i++) {
    		file += "\n  " + i;
    		for(int j = 0; j < kurs_vor[0].length; j++) {
    			file += " " + kurs_vor[i-1][j];
    		}
    	}
		file += "; \n";

//    	System.out.println(file);
    	
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
		file += "var x{i in I, t in T}, binary;\n";
		
		file += "maximize obj : sum{i in I, t in T}(vor_res[i,t]*x[i,t]);\n";
		
		file += "#Veranstalung nur einmal planen\n";
		file += "s.t. veranst{i in I}: sum{t in T} x[i,t] <= 1;\n";
		  
		file += "#Dozenten nicht doppelt\n";
		file += "s.t. dozent{d in D, t in T}: sum{i in I} (doz_vor[d,i]*x[i,t]) <= 1;\n";
		
		file += "#Kurs nicht doppelt\n";
		file += "s.t. kurs{k in K, t in T}: sum{i in I} (kurs_vor[k,i]*x[i,t]) <= 1;\n";
		
		file += "solve;\n";
		
//		file += "printf \"solution:\\n\" > f;\n";
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
}
