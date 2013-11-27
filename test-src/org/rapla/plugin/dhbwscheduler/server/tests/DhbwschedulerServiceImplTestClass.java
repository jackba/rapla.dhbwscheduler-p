package org.rapla.plugin.dhbwscheduler.server.tests;

import java.util.Date;
import java.util.Map;

import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;

import solver.constraints.IntConstraint;
import solver.variables.IntVar;

public class DhbwschedulerServiceImplTestClass extends DhbwschedulerServiceImpl {

	@Override
	public int[] buildAllocatableVerfügbarkeit(Date start, Date ende,
			int dauer, Reservation[] reservation) {
		return super.buildAllocatableVerfügbarkeit(start, ende, dauer, reservation);
	}

	@Override
	public String[] splitDozentenConstraint(String Dozentenconstraint,
			int day) {
		return super.splitDozentenConstraint(Dozentenconstraint, day);
	}

	@Override
	public Object getClassification(SimpleIdentifier id, String attribute)
			throws RaplaContextException, EntityNotFoundException {
		return super.getClassification(id, attribute);
	}

	@Override
	public String solveSchedule(int[] dozentenconstraint,
			int[] allocatableverfügbarkeit, IntConstraint[] nebenbedingungen) {
		return super.solveSchedule(dozentenconstraint, allocatableverfügbarkeit,
				nebenbedingungen);
	}

	@Override
	public IntConstraint[] buildNebenbedingungen(IntVar dozentenVariable,
			IntVar allocatableVariable) {
		return super.buildNebenbedingungen(dozentenVariable, allocatableVariable);
	}

	@Override
	public int getReservationDauer(SimpleIdentifier reservationId) {
		return super.getReservationDauer(reservationId);
	}

	@Override
	public int getReservationWiederholung(SimpleIdentifier reservationId) {
		return super.getReservationWiederholung(reservationId);
	}

	@Override
	public Map<SimpleIdentifier, int[]> sortReservations(
			Map<SimpleIdentifier, int[]> reservations) {
		return super.sortReservations(reservations);
	}

	@Override
	public boolean editReservation(SimpleIdentifier reservationId,
			Date startNeu) {
		return super.editReservation(reservationId, startNeu);
	}

	@Override
	public boolean editAppointment(SimpleIdentifier appointmentId,
			Date startNeu) {
		return super.editAppointment(appointmentId, startNeu);
	}

	public DhbwschedulerServiceImplTestClass(RaplaContext context) {
		super(context);
	}
	
	public int[] buildDozentenConstraint (Date start, Date ende, int dauer, String dozentenConstraint){
		return super.buildDozentenConstraint(start, ende, dauer, dozentenConstraint);
	}

}
