package org.rapla.plugin.dhbwscheduler.server;

import java.io.IOException;
import java.io.PrintWriter;
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

public class SchedulerConstraintsPageGenerator extends RaplaComponent implements RaplaPageGenerator {

	public SchedulerConstraintsPageGenerator(RaplaContext context,Configuration config) {
		super(context);
		setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {         

        PrintWriter out = response.getWriter();            
        out.println();
  //      String descr = request.getParameter("comment");            
    //    String[] myJsonData = request.getParameterValues("json[]");

        String test = "test";

       // response.sendRedirect("pasoServlet.jsp");
    }
	
	public String getInformation(String[] feld, String suche)
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
	
	public void generatePage(ServletContext context,
			HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException 
					{	
		response.setCharacterEncoding("UTF-8");
		response.setContentType("content-type text/html");

		java.io.PrintWriter out = response.getWriter();

		//Eingestellte Sprache des Clients/Browsers
		//Format sollte "de-de" sein
		String strLanguage = request.getHeader("accept-language");
		String lang = "";
		String[] strLang = strLanguage.split(",");
		String[] test2;
		if (strLang[0].contains("-"))
		{
			test2 = strLang[0].split("-");
			strLang[0] = test2[0];
		}
		lang = strLang[0];
		
		//--------------------------------------------------
		//D.D. - Sprache übersetzt in DhbwschedulerPluginResources.xml
		//Default Sprache ist Englisch d.h. falls die Sprache auslesen aus dem Browser nicht richtig funktioniert,
		//oder eine andere Sprache wie Deutsch oder Englisch übertragen wird.
		//--------------------------------------------------
		
		String semester = "?";			//Zahl des Semesters (Beispiel: 2.)
		String dozent = "?";					//Name Dozent
		String studiengang = "Unbekannt";		//Studiengang
		String kursName="Unbekannt";			//Kursname
		String beginZeit="dd.mm.jjjj";			//Beginn der Veranstaltung
		String endZeit="dd.mm.jjjj";			//Ende der Veranstaltung
		String vorlesungsZeit ="dd.mm.jj";		//Ende der Vorlesungszeit
		String veranst="Unbekannt";				//Veranstaltungsname
		String kontaktdaten="";					//Liste mit geänderten Kontaktdaten 
		String time = "";							//Inhalt der StundenTabelle
		String[] ausnahmenArray;				//Liste mit Daten der Ausnahmen
		Date[] dateArr;
		int stunden = 4;						//Vorlesungsstunden am Stück
		boolean aufsicht = false;				//Klausuraufsicht teilnehmen (ja | nein)
		String bemerkung = "";					//Inhalt des Bemerkungsfeldes
		int dayTimeStart = 8;					//Benötigt zum Aufbauen der Stundentabelle
		int dayTimeEnd = 18;					//Benötigt zum Aufbauen der Stundentabelle		
		String key = request.getParameter("key"); 

		String eventId = request.getParameter("id");	//ID der Veranstaltung
		String dozentId = request.getParameter("dozent");	//ID des Dozenten
		String linkPrefix = request.getPathTranslated() != null ? "../": "";

		StorageOperator lookup;
		try {
			lookup = getContext().lookup( StorageOperator.class);
			//String idtype = new SimpleIdentifier(Reservation.TYPE, Integer.parseInt(eventId));
			Reservation veranstaltung = (Reservation) lookup.resolve(eventId);
			
			String vs = (String) veranstaltung.getClassification().getValue("planungsconstraints");
			String dozentKey = lookup.resolve(dozentId).getId();
			dateArr=ConstraintService.getExceptionDatesDoz(vs, dozentKey);
			time = ConstraintService.getDozStringConstraint(vs, dozentKey);
			
			if (veranstaltung.getClassification().getValue("planungszyklus")!=null){
				//String irgendwas = (String) veranstaltung.getClassification().getValue("planungszyklus");
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
			for (int i = 0; i < veranstaltung.getResources().length; i++)
			{
				if (veranstaltung.getResources()[i].getClassification().getType().getKey().equals("kurs"))
				{
					if (i==0)
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		out.println("<!DOCTYPE html>"); // we have HTML5 
		out.println("<html>");

		//gesendete Daten werden hier ausgelesen, wenn eine Änderung vorgenommen wurde (changed = 1)
		if (request.getParameter("changed") != null && request.getParameter("changed").equals("1")){
			time = request.getParameter("time");	
			kontaktdaten= request.getParameter("contact");
			try{stunden = Integer.parseInt(request.getParameter("hours"));}
			catch(Exception e){}
			ausnahmenArray = request.getParameter("exception").split(",");		
			if(request.getParameter("control").equals("1")){
				aufsicht= true;
			}else if(request.getParameter("control").equals("0")){
				aufsicht = false;
			}
			bemerkung=request.getParameter("comment");
			boolean constraintIsSend = sendConstrainttoReservation(eventId,dozentId,time,ausnahmenArray);
			
			if(!constraintIsSend){
				//TODO christian hier könnte dann ein alert Feld erscheinen
				//error konnte nicht gesendet werden! alter an den Dozenten
				//out.print("alert('konnte nicht gespeichert werden');");
				out.println("<script type='text/javascript'>");
				out.print("alert('Daten gesendet!');");
				out.println("</script>");
			}else{
				out.println("<script type='text/javascript'>");
				out.print("alert('Daten gesendet!');");
				out.println("</script>");
			}
			
		}
		
		
		out.println("<head>");
		
		out.println("  <title>" + getI18n().getString("Semesterplanung",new Locale(lang)) + "</title>");
		
		//out.println(" <link REL=\"stylesheet\" href=\""+linkPrefix + "calendar.css\" type=\"text/css\">");
		out.println("	<link REL=\"stylesheet\" type=\"text/css\" href=\""+linkPrefix+"dhbw-scheduler/AnfrageformularStylesheet.css\">");
		out.println("	<script type=\"text/javascript\" src=\""+linkPrefix+"dhbw-scheduler/jquery-2.0.3.min.js\"></script>");
		out.println("	<script type=\"text/javascript\" src=\""+linkPrefix+"dhbw-scheduler/AnfrageformularScript.js\"></script>");
		out.println("<script type='text/javascript'>");
		out.println("</script>");
		out.println("</head>");
		out.println("<body>");
		out.print("		<input id='hiddenUrl' type='hidden' name='' value='"+linkPrefix+"rapla\'>");
		out.print("		<div id='wrapper'>");
		out.println("		<h3>");
		out.print("				" + getI18n().getString("Planung_des", new Locale(lang)) + " "+semester+". " + getI18n().getString("Semesters", new Locale(lang)) + " " + studiengang +  ";</br>");
		out.print("				" + getI18n().getString("Kurs", new Locale(lang)) + " " + kursName + ", "+beginZeit+" " + getI18n().getString("bis", new Locale(lang)) + " "+endZeit+" (" + getI18n().getString("Ende_der_Vorlesungszeit", new Locale(lang)) + " "+vorlesungsZeit+")");
		out.println("		</h3>");

		out.println("		<table id='tableForm1'>");
		out.print("				<tr>");
		out.print("					<th>" + getI18n().getString("Dozent_in", new Locale(lang)) + "</td>");
		out.print("					<td><input disabled='disabled' type='text' value='" + dozent + "'/></td>");
		out.print("				</tr>");
		out.print("				<tr>");
		out.print("					<th>" + getI18n().getString("Lehrveranstaltung", new Locale(lang)) + "</td>");
		out.print("					<td><input disabled='disabled' type='text' value='" + veranst + "'/></td>");
		out.print("				</tr>");
		out.println("		</table>");

		out.println("		<p><b>1. </b>" + getI18n().getString("Vorlesungsstunden_Frage", new Locale(lang)) + "</p>");
		out.println("		<input id='numberVorlesungsstunden' type='number' step='1' min='1' max='10' value='"+stunden+"'/>");
		out.println("		<label for='inpVorlesungsstunden'>" + getI18n().getString("Vorlesungsstunden", new Locale(lang)) + "</label>");

		out.println("		<p><b>2. </b>" + getI18n().getString("Zeiten_Frage", new Locale(lang)) + "</p>");

		out.println("		<p>" + getI18n().getString("SetzenSieEinMinus", new Locale(lang)) + "</p>");
		out.println("		<p>" + getI18n().getString("SetzenSieEinPlus", new Locale(lang)) + "</p>");
		out.println("		<table id='timeTable'>");
		out.println("			<thead>");
		out.print("					<tr>");
		out.print(" 					<th colspan='6' style='font-size:large;text-align:left;vertical-align:center;height:50px;'>" + getI18n().getString("Stundentabelle", new Locale(lang)) + "</th>");
		out.print(" 					<td>");
		out.print(" 						<input id='btnPlus' class='timeTableButtons' type='button' value='+'/>");
		out.print(" 						<input id='btnMinus' class='timeTableButtons' type='button' value='-'/>");
		out.print("				 			<input id='btnClear' class='timeTableButtons' type='button' value='X'/>");
		out.print(" 					</td>");
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
		out.println("		<p><b>3. </b>" + getI18n().getString("Ausnahmen_Frage", new Locale(lang)) + "</p>");

		out.print("			<input id='inpDatepicker' type='date' min='"+formatDateForDatepicker(beginZeit)+"' max='"+formatDateForDatepicker(endZeit)+"' value='"+formatDateForDatepicker(beginZeit)+"'/>");
		out.println("		<input id='btnSetDate' type='button' value='" + getI18n().getString("auswaehlen", new Locale(lang)) + "'/>");
		out.println("		<input id='btnDeleteDate' type='button' value='" + getI18n().getString("loeschen", new Locale(lang)) + "'/>");
		out.println("		<ul id='ulDateList'>");
		
		//for(int i=0;i<ausnahmenArray.length;i++){
		//	out.print("<li>"+ausnahmenArray[i]+"</li>");
		//}
		 
	
		out.println("		</ul>");
		out.println("		<form action=\"rapla\" method=\"get\">");
		out.println(getHiddenField("key", key));
		out.println("			<input id='inpChanged' type='hidden' name='changed' value='0'>");
		out.println("			<input id='inpKontakt' type='hidden' name='contact' value=''>");
		out.println("			<input id='inpStunden' type='hidden' name='hours' value=''>");
		out.println("			<input id='inpTimeTable' type='hidden' name='time' value=''>");
		out.println("			<input id='inpAusnahmen' type='hidden' name='exception' value=''>");		
		out.println("			<input id='inpAufsicht' type='hidden' name='control' value=''>");
		out.println("			<input id='inpBemerkungen' type='hidden' name='comment' value=''>");	
		out.print("				<input id='inpSubmit' type ='submit' name='rapla' value='" + getI18n().getString("senden", new Locale(lang)) + "'/>");
		out.println("		</form>");
		out.println("	</div>");
		out.println("</body>");
		out.println("</html>");

		out.close();

	}


	String getHiddenField( String fieldname, String value) {
		return "<input type=\"hidden\" name=\"" + fieldname + "\" value=\"" + value + "\"/>";
	}
	private String formatDateForDatepicker(String str){
		String[] strArr= str.split("\\.");
		return strArr[2]+"-"+strArr[1]+"-"+strArr[0];		
	}
	//Methode um TimeString (Werte der Stundentabelle) von einem String in Array umwandeln
	private String[][] formatTimeString(String str,int dayTimeStart, int dayTimeEnd){
		//Überprüfen ob TimeString leer ist, ggf. füllen
		if(str.equals("") || str == null){
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
	private boolean sendConstrainttoReservation(String eventId, String dozentId,
			String time, String[] ausnahmenArray) {
		
		String constraint = "";
		String newConstraint="";
		int status = 2;
		//int verId = Integer.parseInt(eventId);
		Date[] ausnahmenDateArray = new Date[ausnahmenArray.length];
		SimpleDateFormat strToDate = new SimpleDateFormat("yyyy-MM-dd");
		
		StorageOperator lookup;
		Reservation veranstaltung;
		try {
			lookup = getContext().lookup( StorageOperator.class);
			//SimpleIdentifier idtype = new SimpleIdentifier(Reservation.TYPE, verId);
			veranstaltung = (Reservation) lookup.resolve(eventId);
			
		} catch (RaplaContextException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		if (ausnahmenArray != null ){
			for(int i = 0 ; i< ausnahmenArray.length; i++){
				if(!ausnahmenArray[i].equals("") && ausnahmenArray[i] != null){
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
		newConstraint = ConstraintService.addorchangeSingleDozConstraint(constraint, dozentId, time, ausnahmenDateArray, status);
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
