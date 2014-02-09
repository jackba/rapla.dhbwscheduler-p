package org.rapla.plugin.dhbwscheduler;

import javax.jws.WebService;

import org.rapla.framework.RaplaException;

@WebService
public interface DhbwschedulerService {
	String schedule(String[] reservationIds) throws RaplaException;
	boolean sendMail(String reservationID,String dozentId, String login, String url) throws RaplaException;
}
