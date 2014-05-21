package org.rapla.plugin.dhbwscheduler;

import javax.jws.WebService;

import org.rapla.framework.RaplaException;

/**
 * 
 * @author DHBW (Dieckmann, Daniel; Dvorschak, Marc; Flickinger, Marco; Geissel, Markus; Gemeinhardt, Christian; Henne, Adrian; Köhler, Christoffer; Schaller, Benjamin; Werner, Benjamin)
 *
 */
@WebService
public interface DhbwschedulerService {
	
	/**
	 * Startet den Scheduler. Die übergebenen Vorlesungen müssen im Zustand "in Plannung geschlossen" sein und Dozenten-Constraints 
	 * besitzen.
	 * 
	 * @see
	 * org.rapla.plugin.dhbwscheduler.DhbwschedulerServiceImpl#schedule(org.rapla.entities.storage.internal.SimpleIdentifier[])
	 * 
	 * @param String[] reservationIds - IDs der zu plannenden Vorlesungen
	 */
	String schedule(String[] reservationIds) throws RaplaException;
	
	/**
	 * Versendet die Mail mit dem Link zur Abfrage der Constraints an die Dozenten. 
	 * Ist die Person noch nicht eingeladen, wird eine Einladung, andernsfalls eine Erinnerung versendet.
	 * 
	 * @param reservationID - ID der Veranstaltung
	 * @param dozentId - ID des Dozenten, an den die Email verschickt werden soll
	 * @param login - Name des eingeloggten Absenders
	 * @param url - URL zum Erfassungslink der Dozenten-Constraints
	 * 
	 * @throws RaplaException
	 * 
	 * @see
	 * org.rapla.plugin.dhbwscheduler.DhbwschedulerServiceImpl#sendMail(String reservationID,String dozentId, String login, String url)
	 */
	boolean sendMail(String reservationID,String dozentId, String login, String url) throws RaplaException;
}
