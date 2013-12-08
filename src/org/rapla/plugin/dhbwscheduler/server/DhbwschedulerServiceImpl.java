package org.rapla.plugin.dhbwscheduler.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.rapla.components.util.DateTools;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.server.RemoteMethodFactory;
import org.rapla.server.RemoteSession;
import org.rapla.storage.StorageOperator;

import solver.constraints.IntConstraint;
import solver.variables.IntVar;

/**
 * @author DHBW
 *
 */
public class DhbwschedulerServiceImpl extends RaplaComponent implements RemoteMethodFactory<DhbwschedulerService>, DhbwschedulerService {

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
	
	/*
	 * (non-Javadoc)
	 * Test Daten aus HTML Seite lesen
	 * @see org.rapla.plugin.dhbwscheduler.DhbwschedulerService#schedule(org.rapla.entities.storage.internal.SimpleIdentifier[])
	 */
	public void leseDaten(int reservationID, int[][] constraints, Date[] ausnahmen) throws RaplaContextException, EntityNotFoundException
	{
		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		SimpleIdentifier idtype = new SimpleIdentifier(Reservation.TYPE, reservationID); 
		Reservation veranstaltung = (Reservation) lookup.resolve(idtype);
		
		
		// Constraints auslesen und als String zusammenbauen
		String stringconstraint = constraintToString(constraints);
		String stringausnahmeDatum = ausnahmenToString(ausnahmen);
		
		
		//Attribute setzen
		try {
			Reservation editVeranstaltung =getClientFacade().edit(veranstaltung);
			
			editVeranstaltung.getClassification().setValue("planungsconstraints", stringconstraint);
			editVeranstaltung.getClassification().setValue("ausnahmeconstraints", stringausnahmeDatum);
			
			getClientFacade().store( editVeranstaltung );
		} catch (RaplaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
	}
	
	private String ausnahmenToString(Date[] ausnahmen) {
		
		String ausnahmenString = "";
		
		for (int i = 0; i< ausnahmen.length ; i++)
		{
			if (ausnahmen[i] != null){
				ausnahmenString = ausnahmenString + DateTools.formatDate(ausnahmen[i]) + "," ;
			}
			
		}
		
		if (ausnahmenString.endsWith(",")){
			ausnahmenString = ausnahmenString.substring(0, ausnahmenString.length()-1);
		}
		
		return ausnahmenString;
		
	}

	private String constraintToString(int[][] constraints) {
		
		String stringconstraint = "";
		
		for (int day = 0; day < constraints.length; day++){
			
			stringconstraint = stringconstraint + String.valueOf(day+1) + ":"; 
			for (int hour = 0; hour < constraints[day].length; hour++){
				
				if (constraints[day][hour] == 1 && constraints[day][hour-1] == 0){
					
					stringconstraint = stringconstraint + String.valueOf(hour+1);
					
				}
				if (constraints[day][hour] == 1 && constraints[day][hour+1] == 0){
					stringconstraint = stringconstraint + "-" + String.valueOf(hour+1) + ",";
				}
			}
			if (stringconstraint.endsWith(",")){
				stringconstraint = stringconstraint.substring(0, stringconstraint.length()-1);
			}
			stringconstraint = stringconstraint + ";";
			
		}
		return stringconstraint;
	}

	

	public String getInformation(SimpleIdentifier[] reservationIds)  throws RaplaException {
		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		List<Reservation> reservations = new ArrayList<Reservation>();
		int key = 0;
		for ( SimpleIdentifier id :reservationIds)
		{
			RefEntity<?> object = lookup.resolve( id);
			reservations.add( (Reservation) object);
			key = id.getKey();
		}
		StringBuilder result = new StringBuilder();
		for ( Reservation r : reservations)
		{
			//Veranstaltung ID
			result.append(key);
			result.append( ",");
			//Veranstaltung Name
			result.append( r.getName(getLocale()));
			result.append( ",");
			//Resource Kurs
			result.append(r.getResources()[0].getName(getLocale()));

		}
		return result.toString();
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
	 * @param dauer
	 * @param dozentenConstraint
	 * @return
	 */
	protected int[] buildDozentenConstraint(Date start, Date ende, int dauer, String dozentenConstraint){
		return null;
	}
	
	/**
	 * @param start
	 * @param ende
	 * @param dauer
	 * @param reservation
	 * @return
	 */
	protected int[] buildAllocatableVerfügbarkeit(Date start, Date ende, int dauer, Reservation[] reservation) {
		return null;
	}
	
	/**
	 * @param Dozentenconstraint
	 * @param day
	 * @return
	 */
	protected String[] splitDozentenConstraint(String Dozentenconstraint, int day) {
		return null;
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
	protected String solveSchedule(int[] dozentenconstraint, int[] allocatableverfügbarkeit, IntConstraint[] nebenbedingungen) {
		return null;
	}
	
	/**
	 * @param dozentenVariable
	 * @param allocatableVariable
	 * @return
	 */
	protected IntConstraint[] buildNebenbedingungen(IntVar dozentenVariable, IntVar allocatableVariable) {
		return null;
	}

	/**
	 * @param reservationId
	 * @return
	 */
	protected int getReservationDauer(SimpleIdentifier reservationId) {
		return -1;
	}
	
	/**
	 * @param reservationId
	 * @return
	 */
	protected int getReservationWiederholung(SimpleIdentifier reservationId) {
		return -1;
	}
	
	/**
	 * @param reservations
	 * @return
	 */
	protected Map<SimpleIdentifier, int[]> sortReservations(Map<SimpleIdentifier, int[]> reservations) {
		//int[] Stelle 0 = Dauer, Stelle 1 =Wdh
		return null;
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
