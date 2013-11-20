package org.rapla.plugin.dhbwscheduler.server;

import java.util.ArrayList;
import java.util.List;

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

}
