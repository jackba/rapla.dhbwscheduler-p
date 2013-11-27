package org.rapla.plugin.dhbwscheduler.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.server.RemoteMethodFactory;
import org.rapla.server.RemoteSession;
import org.rapla.storage.StorageOperator;

import solver.constraints.IntConstraint;
import solver.variables.IntVar;

public class DhbwschedulerServiceImpl extends RaplaComponent implements RemoteMethodFactory<DhbwschedulerService>, DhbwschedulerService {

	public DhbwschedulerServiceImpl(RaplaContext context) {
		super(context);
	}

	@Override
	public DhbwschedulerService createService(RemoteSession remoteSession) {
		return this;
	}

	@Override
	public String schedule(SimpleIdentifier[] reservationIds)  throws RaplaException {
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
	}
	
	private int[] buildDozentenConstraint(Date start, Date ende, int dauer, String dozentenConstraint){
		return null;
	}
	
	private int[] buildAllocatableVerfügbarkeit(Date start, Date ende, int dauer, Reservation[] reservation) {
		return null;
	}
	
	private String[] splitDozentenConstraint(String Dozentenconstraint, int day) {
		return null;
	}
	
	private Object getClassification(SimpleIdentifier id, String attribute) {
		return null;
	}
	
	private String solveSchedule(int[] dozentenconstraint, int[] allocatableverfügbarkeit, IntConstraint[] nebenbedingungen) {
		return null;
	}
	
	private IntConstraint[] buildNebenbedingungen(IntVar dozentenVariable, IntVar allocatableVariable) {
		return null;
	}
	
	private int getReservationDauer(SimpleIdentifier id) {
		return -1;
	}
	
	private int getReservationWiederholung(SimpleIdentifier id) {
		return -1;
	}
	
	private Map<SimpleIdentifier, int[]> sortReservations(Map<SimpleIdentifier, int[]> reservations) {
		//int[] Stelle 0 = Dauer, Stelle 1 =Wdh
		return null;
	}
	
	private boolean editReservation(SimpleIdentifier id, Date startNeu) {
		return false;
	}
	
	private boolean editAppointment(SimpleIdentifier id, Date startNeu) {
		return false;
	}
	
}
