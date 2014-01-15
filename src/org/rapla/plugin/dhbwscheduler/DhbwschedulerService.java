package org.rapla.plugin.dhbwscheduler;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jws.WebService;

import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.framework.RaplaException;

@WebService
public interface DhbwschedulerService {
	String schedule(SimpleIdentifier[] reservationIds) throws RaplaException;
	String sendMail(SimpleIdentifier reservationID,SimpleIdentifier dozentId, String login, String url) throws RaplaException;
}
