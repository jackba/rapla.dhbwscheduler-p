package org.rapla.plugin.dhbwscheduler.server.tests;

import java.util.Date;

import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;

public class DhbwschedulerServiceImplTestClass extends DhbwschedulerServiceImpl {

	@Override
	public int[][] buildAllocatableVerfuegbarkeit(Date start, Date ende,
			Reservation[] reservation) throws RaplaException {
		return super.buildAllocatableVerfuegbarkeit(start, ende, reservation);
	}

	@Override
	public int[] splitDozentenConstraint(String Dozentenconstraint) {
		return super.splitDozentenConstraint(Dozentenconstraint);
	}

	@Override
	public Object getClassification(SimpleIdentifier id, String attribute)
			throws RaplaContextException, EntityNotFoundException {
		return super.getClassification(id, attribute);
	}

	@Override
	public String solveSchedule() {
		return super.solveSchedule();
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
	
	public int[][] buildDozentenConstraint (Date start, Date ende, String dozentenConstraint){
		return super.buildDozentenConstraint(start, ende, dozentenConstraint);
	}

}
