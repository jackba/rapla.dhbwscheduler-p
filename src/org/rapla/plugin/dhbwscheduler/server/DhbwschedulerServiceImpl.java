package org.rapla.plugin.dhbwscheduler.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
public class DhbwschedulerServiceImpl extends RaplaComponent implements RemoteMethodFactory<DhbwschedulerService>, DhbwschedulerService {

	int[][] timeSlots = {{},{},{0,1},{2,3},{4,5},{6,7},{8,9}};
	
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
/*		//Original-Quellcode Hr. Kohlhaas		
  		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		List<Reservation> reservations = new ArrayList<Reservation>();
		for ( SimpleIdentifier id :reservationIds)
		{
			RefEntity<?> object = lookup.resolve( id);
			reservations.add( (Reservation) object);
		}
		StringBuilder result = new StringBuilder();
		for ( Reservation r : reservations)
		{
			result.append( r.getName(getLocale()));
			result.append( ", ");
		}
		return result.toString();
		
*/
		String result = "";
		return result;
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
	
}
