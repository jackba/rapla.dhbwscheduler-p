package org.rapla.plugin.dhbwscheduler;

import javax.jws.WebService;

import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.framework.RaplaException;

@WebService
public interface DhbwschedulerService {
	String schedule(SimpleIdentifier[] reservationIds) throws RaplaException;
}
