package org.rapla.plugin.dhbwscheduler.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.ClientFacade;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
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
    // Variable zum wiederfinden, welche Reservierung mit welchem Index in den Scheduler gegeben wird
    private Reservation[] reservations_scheduler;
    
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
		String model = "scheduler_gmpl" + new Date().getTime() + ".mod";
		String data = "scheduler_data" + new Date().getTime() + ".dat";
		String solution = "scheduler_solution" + new Date().getTime() + ".dat";

/*		int doz_vor[][] = { { 0, 0, 1, 1 }, { 1, 0, 0, 0 }, { 0, 1, 0, 0 } };
		int vor_res[][] = { { 1, 0, 0, 0, 1, 0, 1, 0, 1, 0 },
				{ 0, 1, 0, 1, 0, 1, 0, 1, 0, 1 },
				{ 1, 1, 0, 0, 1, 1, 0, 0, 1, 1 },
				{ 1, 1, 0, 0, 1, 1, 0, 0, 1, 1 } };
		int kurs_vor[][] = { { 1, 1, 0, 1 }, { 0, 0, 1, 0 }, { 1, 0, 0, 1 },
				{ 0, 1, 0, 0 } };
*/

		int doz_vor[][];
		int vor_res[][];
		int kurs_vor[][];
		
		Date start = new Date();   // #################### Auf jeden Fall noch zu füllen #############################
		Date ende = new Date();    // #################### Auf jeden Fall noch zu füllen #############################

		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		List<Reservation> reservations = new ArrayList<Reservation>();
		for ( SimpleIdentifier id :reservationIds)
		{
			RefEntity<?> object = lookup.resolve( id);
			reservations.add( (Reservation) object);
		}
		
		Reservation[] r = (Reservation[]) reservations.toArray();
		doz_vor = buildZuordnungDozentenVorlesung(r);
		kurs_vor = buildZuordnungKursVorlesung(r);
		vor_res = buildAllocatableVerfuegbarkeit(start, ende, r);
		
		aufbau_scheduler_mod(model);
		
		aufbau_scheduler_data(data, doz_vor, kurs_vor, vor_res);
    	
        GLPK.glp_java_set_numeric_locale("C");
        solve(model, data, solution);
        
        String result = auslese_Solution(solution);
        return result; 
	}
	
	public void solve(String model, String data, String solution) {
        glp_prob lp = null;
        glp_tran tran;
        glp_iocp iocp;

        int skip = 0;
        int ret;

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
            throw new RuntimeException("Model file not found: " + model);
        }
        ret = GLPK.glp_mpl_read_data(tran, data);
        if (ret != 0) {
            GLPK.glp_mpl_free_wksp(tran);
            GLPK.glp_delete_prob(lp);
            throw new RuntimeException("Data file not found: " + data);
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
	 * @param start
	 * @param ende
	 * @param dozentenConstraint
	 * @return
	 */
	protected int[][] buildDozentenConstraint(Date start, Date ende, String dozentenConstraint){
		//TODO: Kostenmatrix für Dozentenwünsche
		return null;
	}
	
	/**
	 * @param start - Anfang der Woche
	 * @param ende - Ende der Woche
	 * @param reservation
	 * @return
	 * @throws RaplaException 
	 */
	protected int[][] buildAllocatableVerfuegbarkeit(Date start, Date ende, Reservation[] reservation) throws RaplaException {
		int[][] vor_res = new int[reservation.length][10];
		for (int i = 0; i < reservation.length; i++){
			for (int j = 0; j < 10; j++){
				vor_res[i][j] = 1;
			}
		}
		int vorlesungNr = 0;
		for (Reservation vorlesung : reservation) {
			Allocatable[] allocatables = vorlesung.getAllocatables();
			Reservation[] vorlesungenMitGleichenResourcen = getClientFacade().getReservationsForAllocatable(allocatables, start, ende, null);
			for (Reservation vorlesungMitGleicherResource : vorlesungenMitGleichenResourcen){
				Appointment[] termine = vorlesungMitGleicherResource.getAppointments();
				for (Appointment termin : termine){
					Date beginn = termin.getStart();
					Calendar cal = Calendar.getInstance();
					cal.setTime(beginn);
					if(cal.HOUR_OF_DAY < 12){
						vor_res[vorlesungNr][timeSlots[cal.DAY_OF_WEEK][0]] = 0;
					} else {
						vor_res[vorlesungNr][timeSlots[cal.DAY_OF_WEEK][1]] = 0;
					}
				}
			}
			String planungsconstraint = vorlesung.getClassification().getValue("planungsconstraints").toString();
			int[] belegteSlots = splitDozentenConstraint(planungsconstraint);
			for(int i = 0; i < 10; i++){
				if(belegteSlots[i] == 0){
					vor_res[vorlesungNr][i] = belegteSlots[i];
				}
			}
		    // Zwischenspeichern welche Reservierung mit welchem Index in den 
			// Scheduler gegeben wird. Nötig um später das Ergebnis wieder der Reservierung zuordnen zu können
			reservations_scheduler[vorlesungNr] = vorlesung;
			
			vorlesungNr++;
		}
		return vor_res;
	}
	
	/**
	 * @param Dozentenconstraint
	 * @param day
	 * @return
	 */
	protected int[] splitDozentenConstraint(String dozentenConstraint) {
		int[] belegteSlots = {0,0,0,0,0,0,0,0,0,0};
		int idIndex = dozentenConstraint.indexOf('_');
		dozentenConstraint = dozentenConstraint.substring(idIndex + 1);
		String[] constraintsTage = dozentenConstraint.split(";");
		for(String constraint : constraintsTage){
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
			String timepoint = constraint.substring(constraint.indexOf(':') + 1, constraint.indexOf('-'));
			int hourOfDay = Integer.valueOf(timepoint);
			if(hourOfDay < 12){
				belegteSlots[timeSlots[dayOfWeek][0]] = 1;
			} else {
				belegteSlots[timeSlots[dayOfWeek][1]] = 1;
			}
			if(constraint.contains(",")){
				timepoint = constraint.substring(constraint.indexOf(',') + 1, constraint.indexOf('-'));
				hourOfDay = Integer.valueOf(timepoint);
				if(hourOfDay > 12){
					belegteSlots[timeSlots[dayOfWeek][1]] = 1;
				}
			}
		}
		return belegteSlots;
	}
	
	/**
	 * Holt zu der übergebenen Id das gewünschte Attribut und gibt dieses zurück
	 * 
	 * @param id
	 * @param attribute
	 * @return
	 * @throws RaplaContextException 
	 * @throws EntityNotFoundException 
	 */
	protected Object getClassification(SimpleIdentifier id, String attribute) throws RaplaContextException, EntityNotFoundException {
		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		//Veranstaltung als Objekt besorgen
		Reservation veranstaltung = (Reservation) lookup.resolve(id);
		
		//Attribut auslesen & zurückgeben
		return veranstaltung.getClassification().getValue(attribute);		
	}
	
	/**
	 * @param dozentenconstraint
	 * @param allocatableverfügbarkeit
	 * @param nebenbedingungen
	 * @return
	 */
	protected String solveSchedule() {
		return null;
	}
	
	/**
	 * @param dozentenVariable
	 * @param allocatableVariable
	 * @return
	 */
	protected int[][] buildZuordnungDozentenVorlesung(Reservation[] reservation) {
		Set<Allocatable> dozenten = new HashSet<Allocatable>();
		for (Reservation veranstaltung : reservation){
			Allocatable[] ressourcen = veranstaltung.getAllocatables();
			for (Allocatable a : ressourcen){
				if(a.getClassification().getType().getName().toString() == "professor"){
					dozenten.add(a);
				}
			}
		}
		Allocatable[] dozentenArray = (Allocatable[]) dozenten.toArray();
		int[][] doz_vor = new int[dozentenArray.length][reservation.length];
		int i = 0;
		for (Allocatable a : dozentenArray){
			int j = 0;
			for(Reservation veranstaltung : reservation){
				if(veranstaltung.hasAllocated(a)){
					doz_vor[i][j] = 1;
				} else {
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
	 * @return
	 */
	protected int[][] buildZuordnungKursVorlesung(Reservation[] reservation){
		Set<Allocatable> kurse = new HashSet<Allocatable>();
		for (Reservation veranstaltung : reservation){
			Allocatable[] ressourcen = veranstaltung.getAllocatables();
			for (Allocatable a : ressourcen){
				if(a.getClassification().getType().getName().toString() == "kurs"){
					kurse.add(a);
				}
			}
		}
		Allocatable[] kursArray = (Allocatable[]) kurse.toArray();
		int[][] kurs_vor = new int[kursArray.length][reservation.length];
		int i = 0;
		for (Allocatable a : kursArray){
			int j = 0;
			for(Reservation veranstaltung : reservation){
				if(veranstaltung.hasAllocated(a)){
					kurs_vor[i][j] = 1;
				} else {
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
	 * @return
	 */
	protected boolean editReservation(SimpleIdentifier reservationId, Date startNeu) {
		return false;
	}
	
	/**
	 * @param appointmentId
	 * @param startNeu
	 * @return
	 */
	protected boolean editAppointment(SimpleIdentifier appointmentId, Date startNeu) {
		return false;
	}

    @Override
    public boolean output(String str) {
        hookUsed = true;
        System.out.print(str);
        return false;
    }

    @Override
    public void callback(glp_tree tree) {
        int reason = GLPK.glp_ios_reason(tree);
        if (reason == GLPKConstants.GLP_IBINGO) {
            System.out.println("Better solution found");
        }
    }
    
    private void aufbau_scheduler_data(String data_file, int[][] doz_vor, int[][] kurs_vor, int[][] vor_res){
    	String file = "data; \n";
    	
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
    
    private void aufbau_scheduler_mod(String mod_file) {
    	String file = "param f, symbolic := \"scheduler_solution.dat\"; \n\n";
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
    	file += "#Verf�gbarkeit Ressourcen\n";
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
		
		file += "printf \"solution:\\n\" > f;\n";
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
