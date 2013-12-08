package org.rapla.plugin.dhbwscheduler.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
		SimpleIdentifier idtype = new SimpleIdentifier(Allocatable.TYPE, 2); 
		Reservation veranstaltung = (Reservation) lookup.resolve(idtype);
		
		//Attribut auslesen & zurückgeben
		veranstaltung.getClassification().setValue("planungsconstraints", "12");	
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
