package org.rapla.plugin.dhbwscheduler.server;

import java.io.IOException;
//import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerReservationHelper;
import org.rapla.servletpages.RaplaPageGenerator;
import org.rapla.storage.StorageOperator;

/**
 * 
 * @author DHBW (Dieckmann, Daniel; Dvorschak, Marc; Flickinger, Marco; Geissel, Markus; Gemeinhardt, Christian; Henne, Adrian; Köhler, Christoffer; Schaller, Benjamin; Werner, Benjamin)
 *
 */
@SuppressWarnings("restriction")
public class SchedulerConstraintsPageGenerator extends RaplaComponent implements RaplaPageGenerator {
	
	
	public SchedulerConstraintsPageGenerator(RaplaContext context,Configuration config) {
		super(context);
		setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
	}
	
//	protected void service(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {         
//
//        PrintWriter out = response.getWriter();            
//        out.println();
//  //      String descr = request.getParameter("comment");            
//    //    String[] myJsonData = request.getParameterValues("json[]");
//
//        String test = "test";
//
//       // response.sendRedirect("pasoServlet.jsp");
//    }
	
	/**
	 * A helper method for searching a string in a field
	 * @param feld
	 * @param suche
	 * @return
	 */
	public static String getInformation(String[] feld, String suche)
	{
		String ergebnis = "";
		for (int i = 0; i < feld.length; i++)
		{
			if (feld[i].contains(suche))
			{
				return (feld[i].substring(feld[i].indexOf("=")+1,feld[i].length()));
			}
		}
		return ergebnis;
	}
	
	/**
	 * Generate page for lecturer
	 */
	public void generatePage(ServletContext context,
			HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException 
					{	
		response.setCharacterEncoding("UTF-8");
		response.setContentType("content-type text/html");

		java.io.PrintWriter out = response.getWriter();

		//Eingestellte Sprache des Clients/Browsers
		//Format sollte "de-de" sein
		String lang = "";
		String strLanguage = request.getHeader("accept-language");
		if (strLanguage != null)
		{
			lang = strLanguage.substring(0,2);
		}
		else
		{
			//Default ist Englisch
			lang = "en";
		}
		
		//--------------------------------------------------
		//D.D. - Sprache übersetzt in DhbwschedulerPluginResources.xml
		//Default Sprache ist Englisch d.h. falls die Sprache auslesen aus dem Browser nicht richtig funktioniert,
		//oder eine andere Sprache wie Deutsch oder Englisch übertragen wird.
		//--------------------------------------------------
		
		//Boolean Peng ist als Flag gedacht
		boolean peng = false;
		String strdisabled= "";
		String semester = "?";					//Zahl des Semesters (Beispiel: 2.)
		String dozent = "?";					//Name Dozent
		String studiengang = "Unbekannt";		//Studiengang
		String kursName="Unbekannt";			//Kursname
		String beginZeit="dd.mm.jjjj";			//Beginn der Veranstaltung
		String endZeit="dd.mm.jjjj";			//Ende der Veranstaltung
		String vorlesungsZeit ="dd.mm.jj";		//Ende der Vorlesungszeit
		String veranst="Unbekannt";				//Veranstaltungsname
//		String kontaktdaten="";					//Liste mit geänderten Kontaktdaten 
		String time = "";							//Inhalt der StundenTabelle
		String[] ausnahmenArray = null;				//Liste mit Daten der Ausnahmen
		Date[] ausnahmenDateArray = null;
		int stunden = 4;						//Vorlesungsstunden am Stück
//		boolean aufsicht = false;				//Klausuraufsicht teilnehmen (ja | nein)
//		String bemerkung = "";					//Inhalt des Bemerkungsfeldes
		int dayTimeStart = 8;					//Benötigt zum Aufbauen der Stundentabelle
		int dayTimeEnd = 18;					//Benötigt zum Aufbauen der Stundentabelle		
		String key = request.getParameter("key"); 
		String alertmessage = "";
		String eventId = request.getParameter("id");	//ID der Veranstaltung
		String dozentId = request.getParameter("dozent");	//ID des Dozenten
		String linkPrefix = request.getPathTranslated() != null ? "../": "";
		
		
		//gesendete Daten werden hier ausgelesen, wenn eine Änderung vorgenommen wurde (changed = 1)
		if (request.getParameter("changed") != null && request.getParameter("changed").equals("1")){
			time = request.getParameter("time");	
//			kontaktdaten= request.getParameter("contact");
			try{stunden = Integer.parseInt(request.getParameter("hours"));}
			catch(Exception e){}
			if (request.getParameter("exception")!=null && request.getParameter("exception").contains(","))
			{
				ausnahmenArray = request.getParameter("exception").split(",");	
			}
			else
			{
				ausnahmenArray = new String[1];
				ausnahmenArray[0] = request.getParameter("exception");
			}
//			if(request.getParameter("control").equals("1")){
//				aufsicht= true;
//			}else if(request.getParameter("control").equals("0")){
//				aufsicht = false;
//			}
//			bemerkung=request.getParameter("comment");
			boolean constraintIsSend = sendConstrainttoReservation(eventId,dozentId,time,ausnahmenArray);
			
			if(!constraintIsSend){
				getLogger().info("Daten wurden nicht gesendet");
				alertmessage = "Daten wurden nicht gesendet!";
			}else{
				alertmessage = "Daten gesendet!";
			}
			
		}
		
		StorageOperator lookup;
		if (eventId != null || dozentId != null)
		{
		try {
			lookup = getContext().lookup( StorageOperator.class);
			//String idtype = new SimpleIdentifier(Reservation.TYPE, Integer.parseInt(eventId));
			Reservation veranstaltung = (Reservation) lookup.resolve(eventId);
			String vs = (String) veranstaltung.getClassification().getValue("planungsconstraints");
			String planungsstatus = (String) veranstaltung.getClassification().getValue("planungsstatus");
			if (!planungsstatus.equals(getString("planning_open"))){
				strdisabled = "disabled='disabled'";
			}
			
			
			String dozentKey = lookup.resolve(dozentId).getId();
//			dateArr=ConstraintService.getExceptionDatesDoz(vs, dozentKey);
			time = ConstraintService.getDozStringConstraint(vs, dozentKey);
			ausnahmenDateArray = ConstraintService.getExceptionDatesDoz(vs, dozentKey);
			
			if (veranstaltung.getClassification().getValue("planungszyklus")!=null){
				Allocatable xy = (Allocatable) veranstaltung.getClassification().getValue("planungszyklus");
				semester = (String) xy.getClassification().getValue("semester");
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
				
				beginZeit =  dateFormat.format(xy.getClassification().getValue("startdate"));
				endZeit = dateFormat.format(xy.getClassification().getValue("enddate"));
				vorlesungsZeit = endZeit;
			}
			
			dayTimeStart = getCalendarOptions().getWorktimeStartMinutes()/60;
			dayTimeEnd = getCalendarOptions().getWorktimeEndMinutes()/60;
			
			veranst = veranstaltung.getName(getLocale());
			for (int i = 0; i < veranstaltung.getPersons().length; i++)
			{
				String pTest =  veranstaltung.getPersons()[i].getId();
				String pID =  pTest;
				if (pID.equals(dozentId))
				{
					if (veranstaltung.getPersons()[i].getClassification().getValue("firstname")!=null)
					{
						dozent =  veranstaltung.getPersons()[i].getClassification().getValue("firstname").toString();
					}
					if (veranstaltung.getPersons()[i].getClassification().getValue("surname")!=null)
					{
						dozent = dozent + " " + veranstaltung.getPersons()[i].getClassification().getValue("surname").toString();
					}


				}
			}
			int t = 0;
			for (int i = 0; i < veranstaltung.getResources().length; i++)
			{
				if (veranstaltung.getResources()[i].getClassification().getType().getKey().equals("kurs"))
				{
					if (t==0)
					{

						if (veranstaltung.getResources()[i].getClassification().getValue("name")!=null)
						{
							kursName = veranstaltung.getResources()[i].getClassification().getValue("name").toString(); 
						}
						else
						{
							kursName = "";
						}
						if (veranstaltung.getClassification().getValue("studiengang")!=null)
						{
							studiengang = veranstaltung.getClassification().getValue("studiengang").toString();
							if (studiengang.contains(" "))
							{
								int pos = studiengang.indexOf(" ");
								studiengang = studiengang.substring(0, pos);
							}
						}
						else
						{
							studiengang = "";
						}
						t = t + 1;
					}
					else
					{
						if (veranstaltung.getResources()[i].getClassification().getValue("name")!=null)
						{
							kursName = kursName + "/" + veranstaltung.getResources()[i].getClassification().getValue("name").toString();
						}
					}

				}
			}
			
		} catch (RaplaContextException e) {
			getLogger().error(e.toString());
			e.printStackTrace();
			peng = true;
		} catch (EntityNotFoundException e) {
			getLogger().error(e.toString());
			e.printStackTrace();
			peng = true;
		}
		}
		else
		{
			peng = true;
		}
		
		if (!peng) 
		{	
			
							
			out.println("<!DOCTYPE html>"); // we have HTML5 
			out.println("<html>");
		
			if (request.getParameter("changed") != null && request.getParameter("changed").equals("1")){
				out.println("<script type='text/javascript'>");
				out.print("alert('"+alertmessage+"');");
				out.println("</script>");
			}
			out.println("<head>");
			out.println("  <title>" + getI18n().getString("Semesterplanung",new Locale(lang)) + "</title>");
			out.println("	<meta charset='UTF-8'>");
			out.println("	<meta name='viewport' content='width=device-width, initial-scale=1.0,maximum-scale=1.0, user-scalable=no'/>");
			out.println("	<link REL=\"stylesheet\" type=\"text/css\" href=\""+linkPrefix+"dhbw-scheduler/AnfrageformularStylesheet.css\">");
			out.println("	<script type=\"text/javascript\" src=\""+linkPrefix+"dhbw-scheduler/Modernizr.js\"></script>");
			out.println("	<script type=\"text/javascript\" src=\""+linkPrefix+"dhbw-scheduler/respond.js\"></script>");
			out.println("	<script type=\"text/javascript\" src=\""+linkPrefix+"dhbw-scheduler/jquery-2.0.3.min.js\"></script>");
			out.println("	<script type=\"text/javascript\" src=\""+linkPrefix+"dhbw-scheduler/AnfrageformularScript.js\"></script>");
			//out.println(" <link REL=\"stylesheet\" href=\""+linkPrefix + "calendar.css\" type=\"text/css\">");
						
			out.println("<script type='text/javascript'>");
			out.println("</script>");
			out.println("</head>");
			out.println("<body>");
			out.print("		<input id='hiddenUrl' type='hidden' name='' value='"+linkPrefix+"rapla\'>");
			out.print("		<div id='wrapper'>");
			
			out.println("<header id='mainHeader'>");
			out.println("<img id='logo' name='dhbwLogo' alt='DHBW Karlsruhe' src='dhbw_logo.jpg'/>");
			out.println("<h1>");
			out.println(""+ getI18n().getString("Planung_des", new Locale(lang)) + " "+semester+". " + getI18n().getString("Semesters", new Locale(lang)) + " " + studiengang +  "</br>");
			out.println("</br> " + getI18n().getString("Kurs", new Locale(lang)) + " " + kursName + ", "+beginZeit+" " + getI18n().getString("bis", new Locale(lang)) + " "+endZeit);
			out.println("</h1>");
			out.println("</header>");
			
			out.println("<div id='content'>");
			out.println("<h2>" + getI18n().getString("Lehrveranstaltung", new Locale(lang)) + " " + veranst + "</h2>");
			out.println("<h2>" + getI18n().getString("Dozent_in", new Locale(lang)) + " " + dozent + "</h2>");
			
			out.println("<section>");
			out.println("<header>");
			out.println("<h4>1. " + getI18n().getString("Vorlesungsstunden_Frage", new Locale(lang)) + "</h4>");
			out.println("</header>");
			out.println("<content>");
			out.println("<div>");
			out.println("<input "+ strdisabled +" id='numberVorlesungsstunden' type='number' step='1' min='1' max='10' value='"+stunden+"'/>");
			out.println("<label for='inpVorlesungsstunden'>" + getI18n().getString("Vorlesungsstunden", new Locale(lang)) + "</label>");
			out.println("</div>");
			out.println("</content>");
			out.println("</section>");			
			
			out.println("<section>");
			out.println("<header>");
			out.println("<h4>2. " + getI18n().getString("Zeiten_Frage", new Locale(lang)) + "</h4>");
			out.println("</header>");
			out.println("<footer>");
			out.println("<h5>" + getI18n().getString("SetzenSieEinMinus", new Locale(lang)) + "</h5>");
			out.println("<h5>" + getI18n().getString("SetzenSieEinPlus", new Locale(lang)) + "</h5>");
			out.println("</footer>");
			out.println("<content>");	
			out.println("		<table id='timeTable' >");
			out.println("			<thead>");
			out.print("					<tr>");
			out.print(" 					<th colspan='7' id='timeTableHead'>");
			out.println("						<p>" + getI18n().getString("Stundentabelle", new Locale(lang))+"</p>");
			out.print(" 						<input id='btnPlus' class='timeTableButtons' "+ strdisabled +" type='button' value='+'/>");
			out.print(" 						<input id='btnMinus' class='timeTableButtons' "+ strdisabled +" type='button' value='-'/>");
			out.print("				 			<input id='btnClear' class='timeTableButtons' "+ strdisabled +" type='button' value='X'/>");
			out.print(" 					</th>");
			out.print(" 				</tr>");
			out.print("				 	<tr>");
			out.print("						<th id='firstCell'>" + getI18n().getString("Zeit", new Locale(lang)) + "</th>");
			out.print("						<th>" + getI18n().getString("Montag", new Locale(lang)) + "</th>");
			out.print("						<th>" + getI18n().getString("Dienstag", new Locale(lang)) + "</th>");
			out.print("						<th>" + getI18n().getString("Mittwoch", new Locale(lang)) + "</th>");
			out.print("						<th>" + getI18n().getString("Donnerstag", new Locale(lang)) + "</th>");
			out.print("						<th>" + getI18n().getString("Freitag", new Locale(lang)) + "</th>");
			out.print("						<th>" + getI18n().getString("Samstag", new Locale(lang)) + "</th>");
			out.print("					</tr>");
			out.println("			</thead>");
			out.println(" 			<tbody id='timeTableBody'>");

			String[][] timeArray = formatTimeString(time,dayTimeStart,dayTimeEnd);
			String tempVal="";
			for(int i=dayTimeStart;i<dayTimeEnd;i++){

				out.print("<tr>");
				out.print("	<th>"+i+".00 - "+(i+1)+".00</th>");
				//Schleife zum Erstellen der Zellen pro Wochentag (Mo-Sa)
				for(int j=1;j<7;j++){
					tempVal=timeArray[j][i];
					if(tempVal.equals("0")){
						out.print("	<td class='tdMinus'>-</td>");
					}
					else if(tempVal.equals("2")){
						out.print("	<td class='tdPlus'>+</td>");
					}
					else{
						out.print("	<td class='tdNeutral'></td>");
					}
				}
				out.print("</tr>");

			}
			out.println(" 			</tbody>");
			out.println("		</table>");
			out.println("</content>");
			out.println("</section>");
			
			out.println("<section>");
			out.println("<header>");
			out.println("<h4>3. " + getI18n().getString("Ausnahmen_Frage", new Locale(lang)) + "</h4>");
			out.println("</header>");
			out.println("<content>");	
			out.println("		<table id='dateTable' >");
			out.println("<tr>");
			out.println("<td><input id='inpDatepicker' type='date' "+ strdisabled +" min='"+formatDateForDatepicker(beginZeit)+"' max='"+formatDateForDatepicker(endZeit)+"' value='"+formatDateForDatepicker(beginZeit)+"'/></td>");
			out.println("<td><input id='btnSetDate' type='button' "+ strdisabled +" value='" + getI18n().getString("auswaehlen", new Locale(lang)) + "'/></td>");
			out.println("<td><input id='btnDeleteDate' type='button' "+ strdisabled +" value='" + getI18n().getString("loeschen", new Locale(lang)) + "'/></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan='3'><ul id='ulDateList'>");
			
			SimpleDateFormat ausnahmedateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			if(ausnahmenDateArray != null){
				for(int i=0;i<ausnahmenDateArray.length;i++){
					if (ausnahmenDateArray[i] != null){
					out.print("<li>"+ausnahmedateFormat.format(ausnahmenDateArray[i])+"</li>");
					}
				}
			}
			out.println("		</ul>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");
			
			out.println("	<div>");
			out.println("		<form action=\"rapla\" method=\"get\">");
			out.println("	<meta charset='UTF-8'>");
			out.println(getHiddenField("key", key));
			out.println("			<input id='inpChanged' type='hidden' name='changed' value='0'>");
			out.println("			<input id='inpKontakt' type='hidden' name='contact' value=''>");
			out.println("			<input id='inpStunden' type='hidden' name='hours' value=''>");
			out.println("			<input id='inpTimeTable' type='hidden' name='time' value=''>");
			out.println("			<input id='inpAusnahmen' type='hidden' name='exception' value=''>");		
			out.println("			<input id='inpAufsicht' type='hidden' name='control' value=''>");
			out.println("			<input id='inpBemerkungen' type='hidden' name='comment' value=''>");	
			out.print("				<input id='inpSubmit' type ='submit' "+ strdisabled +" name='rapla' value='" + getI18n().getString("senden", new Locale(lang)) + "'/>");
			out.println("		</form>");
			out.println("	</div>");
			out.println("</content>");
			out.println("</section>");
			out.println("	</div>");
			out.println("</body>");
			out.println("</html>");

			out.close();
		}
		else
		{
			out.println("<!DOCTYPE html>"); // we have HTML5 
			out.println("<html>");
			out.println("<body>");
			out.println("<h1>PENG - Beim Laden der Semesterplanung ist etwas schief gegangen!</h1>");
			out.println("</body>");
			out.println("</html>");
		}


	}
	
	/**
	 * create a hidden field with values.
	 * @param fieldname
	 * @param value
	 * @return
	 */
	public String getHiddenField( String fieldname, String value) {
		return "<input type=\"hidden\" name=\"" + fieldname + "\" value=\"" + value + "\"/>";
	}
	
	/**
	 * formats the Dates for the Datepicker object
	 * @param str
	 * @return
	 */
	public String formatDateForDatepicker(String str){
		String[] strArr= str.split("\\.");
		return strArr[2]+"-"+strArr[1]+"-"+strArr[0];		
	}
	//Methode um TimeString (Werte der Stundentabelle) von einem String in Array umwandeln
	/**
	 * Changes a String to int Array[][] with 24x7 hours. 
	 * @param str
	 * @param dayTimeStart
	 * @param dayTimeEnd
	 * @return
	 */
	private String[][] formatTimeString(String str,int dayTimeStart, int dayTimeEnd){
		//Überprüfen ob TimeString leer ist, ggf. füllen
		if (str == null)
		{
			str = "";
		}
		if(str.equals("")){
			//24*7 =168
			
			for (int j = 0; j<7 ; j++)
			{
				for (int i = 0; i<24 ; i++){
					if (i< dayTimeStart || i> dayTimeEnd){
						str+="0";
					}else{
						str+="1";
					}
				}
			}
			
		}
		char[] charArray = str.toCharArray();
		int counter=0;		//Zähler um Wochentage durch zu zählen
		int count=0;		//Zähler um Stunden durch zu zählen
		String[][] timeArray = new String[7][24];
		//Schleife um alle Zeichen (7*24 = 168) durch zu gehen
		for(int i=0;i<charArray.length;i++){
			timeArray[counter][count] = ""+charArray[i];			
			count++;
			if(count==24){
				counter++;
				count=0;
			}
		}
		return timeArray;
	}
	
	/**
	 * sends the formular to the Reservation an Store the information.
	 * @param eventId
	 * @param dozentId
	 * @param time
	 * @param ausnahmenArray
	 * @return return true if the information can be stored
	 */
	private boolean sendConstrainttoReservation(String eventId, String dozentId,
			String time, String[] ausnahmenArray) {
		
		String constraint = "";
		String newConstraint="";
		//int verId = Integer.parseInt(eventId);
		Date[] ausnahmenDateArray = new Date[ausnahmenArray.length];
		SimpleDateFormat strToDate = new SimpleDateFormat("yyyy-MM-dd");
		
		StorageOperator lookup;
		Reservation veranstaltung;
		try {
			lookup = getContext().lookup( StorageOperator.class);
			//SimpleIdentifier idtype = new SimpleIdentifier(Reservation.TYPE, verId);
			veranstaltung = (Reservation) lookup.resolve(eventId);
			
		} catch (RaplaContextException | RuntimeException | EntityNotFoundException e) {
			getLogger().error(e.toString());
			e.printStackTrace();
			return false;
		}
		
		
		if (ausnahmenArray != null ){
			for(int i = 0 ; i< ausnahmenArray.length; i++){
				if(ausnahmenArray[i] != null &&!ausnahmenArray[i].equals("")){
					try {
						ausnahmenDateArray[i] = strToDate.parse(ausnahmenArray[i]);
					} catch (ParseException e) {
						e.printStackTrace();
						return false;
					}
				}
					
			}
		}
		
		
		constraint = (String) veranstaltung.getClassification().getValue("planungsconstraints"); 
		newConstraint = ConstraintService.addorchangeSingleDozConstraint(constraint, dozentId, time, ausnahmenDateArray, ConstraintService.STATUS_RECORDED);
		DhbwschedulerReservationHelper helperClass = new DhbwschedulerReservationHelper( getContext());
		veranstaltung = helperClass.changeReservationAttribute(veranstaltung,"planungsconstraints",newConstraint);
		veranstaltung = helperClass.changeReservationAttribute(veranstaltung,"erfassungsstatus",helperClass.getStringStatus(ConstraintService.getReservationStatus(newConstraint)));
		
		if(veranstaltung == null){
			return false;
		}else{
			return true;
		}

	}	
}
