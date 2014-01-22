package org.rapla.plugin.dhbwscheduler.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rapla.components.util.DateTools;
import org.rapla.components.util.ParseDateException;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.plugin.dhbwscheduler.*;
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

		String dozent = "Unbekannt";			//Name Dozent
		String semester ="?";					//Zahl des Semesters (Beispiel: 2.)
		String studiengang = "Unbekannt";		//Studiengang
		String kursName="Unbekannt";			//Kursname
		String beginZeit="dd.mm.jjjj";			//Beginn der Veranstaltung
		String endZeit="dd.mm.jjjj";			//Ende der Veranstaltung
		String vorlesungsZeit ="dd.mm.jj";		//Ende der Vorlesungszeit
		String veranst="Unbekannt";				//Veranstaltungsname
		String kontaktdaten="";				//Liste mit geänderten Kontaktdaten 
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
			SimpleIdentifier idtype = new SimpleIdentifier(Reservation.TYPE, Integer.parseInt(eventId));
			Reservation veranstaltung = (Reservation) lookup.resolve(idtype);
			
			String vs = (String) veranstaltung.getClassification().getValue("planungsconstraints");
			dateArr=ConstraintService.getExceptionDatesDoz(vs, Integer.parseInt(dozentId));
			time = ConstraintService.getDozStringConstraint(vs, Integer.parseInt(dozentId));
			
			veranst = veranstaltung.getName(getLocale());
			for (int i = 0; i < veranstaltung.getPersons().length; i++)
			{
				Comparable pTest = ((RefEntity<?>) veranstaltung.getPersons()[i]).getId();
				SimpleIdentifier pID = (SimpleIdentifier) pTest;
				if (pID.getKey()==Integer.parseInt(dozentId))
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
				if (veranstaltung.getResources()[i].getClassification().getType().getElementKey().equals("kurs"))
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
			
			beginZeit = veranstaltung.getFirstDate().toLocaleString();
			endZeit = veranstaltung.getMaxEnd().toLocaleString();
			beginZeit = beginZeit.substring(0,beginZeit.indexOf(" "));
			endZeit = endZeit.substring(0,endZeit.indexOf(" "));
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
		
		out.println("  <title>Semesterplanung</title>");

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
		out.print("				Planung des "+semester+". Semesters " + studiengang +  ";</br>");
		out.print("				Kurs " + kursName + ", "+beginZeit+" bis "+endZeit+" (Ende der Vorlesungszeit: "+vorlesungsZeit+")");
		out.println("		</h3>");

		out.println("		<table id='tableForm1'>");
		out.print("				<tr>");
		out.print("					<th>Dozent/in:</td>");
		out.print("					<td><input disabled='disabled' type='text' value='" + dozent + "'/></td>");
		out.print("				</tr>");
		out.print("				<tr>");
		out.print("					<th>Lehrveranst.:</td>");
		out.print("					<td><input disabled='disabled' type='text' value='" + veranst + "'/></td>");
		out.print("				</tr>");
		out.println("		</table>");

		out.println("		<p>Wenn sich Ihre Kontaktdaten (bspw. E-Mail-Adresse, Telefonnummern) ge&auml;ndert oder ganz neu ergeben haben (E-Mail!), bitte hier eintragen:</p>");
		out.println("		<textarea id='taKontakt' rows='5' col='65'>"+kontaktdaten+"</textarea>");
		out.println("		<p><b>1.</b>Wie viele Vorlesungsstunden am St&uuml;ck m&ouml;chten Sie pro Vorlesungstermin halten?</p>");
		out.println("		<input id='numberVorlesungsstunden' type='number' step='1' min='1' max='10' value='"+stunden+"'/>");
		out.println("		<label for='inpVorlesungsstunden'>Vorlesungsstunden</label>");

		out.println("		<p><b>2.</b> Bitte nennen Sie die Zeiten, zu denen wir Sie f&uuml;r die o.a. Vorlesung(en) einplanen k&ouml;nnen. Gehen Sie bitte beim Ausf&uuml;llen der Stundentabelle folgenderma&szlig;en vor:</p>");

		out.println("		<p> Setzen Sie ein <b>- (Minus)</b> in alle Zeitfelder, in denen Sie nicht k&ouml;nnen!</p>");
		out.println("		<p> Setzen Sie ein <b>+ (Plus)</b> in alle Zeitfelder, die f&uuml;r Sie besonders angenehm sind!</p>");
		out.println("		<table id='timeTable'>");
		out.println("			<thead>");
		out.print("					<tr>");
		out.print(" 					<th colspan='6' style='font-size:large;text-align:left;vertical-align:center;height:50px;'>Stundentabelle</th>");
		out.print(" 					<td>");
		out.print(" 						<input id='btnPlus' class='timeTableButtons' type='button' value='+'/>");
		out.print(" 						<input id='btnMinus' class='timeTableButtons' type='button' value='-'/>");
		out.print("				 			<input id='btnClear' class='timeTableButtons' type='button' value='X'/>");
		out.print(" 					</td>");
		out.print(" 				</tr>");
		out.print("				 	<tr>");
		out.print("						<th id='firstCell'>Zeit</th>");
		out.print("						<th>Montag</th>");
		out.print("						<th>Dienstag</th>");
		out.print("						<th>Mittwoch</th>");
		out.print("						<th>Donnerstag</th>");
		out.print("						<th>Freitag</th>");
		out.print("						<th>Samstag</th>");
		out.print("					</tr>");
		out.println("			</thead>");
		out.println(" 			<tbody id='timeTableBody'>");

		String[][] timeArray = formatTimeString(time);
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
		out.println("		<p><b>3.</b>Nennen Sie im Folgenden alle Tage in dem Vorlesungszeitraum, die terminlich anderweitig schon belegt sind (z.B. Urlaub, Gesch&auml;ftstermine):</p>");

		out.print("			<input id='inpDatepicker' type='date' min='"+formatDateForDatepicker(beginZeit)+"' max='"+formatDateForDatepicker(endZeit)+"' value='"+formatDateForDatepicker(beginZeit)+"'/>");
		out.println("		<input id='btnSetDate' type='button' value='ausw&auml;hlen'/>");
		out.println("		<input id='btnDeleteDate' type='button' value='l&ouml;schen'/>");
		out.println("		<ul id='ulDateList'>");
		
		//for(int i=0;i<ausnahmenArray.length;i++){
		//	out.print("<li>"+ausnahmenArray[i]+"</li>");
		//}
		 
	
		out.println("		</ul>");
		out.println("		<p><b>4.</b>Ich m&ouml;chte die Aufsicht in der Klausur falls terminlich m&ouml;glich selbst &uuml;bernehmen.</p>");
		if(aufsicht){
			out.print("			<input id='cbYes' type='radio' name='cbGroupKlausur' value='1' checked='checked'/>");
			out.print("			<label for='cbYes'>Ja</label>");
			out.print("			<input id='cbNo' type='radio' name='cbGroupKlausur' value='0'/>");
			out.print("			<label for='cbNo'>Nein</label>");
		}
		else{
			out.print("			<input id='cbYes' type='radio' name='cbGroupKlausur' value='1'/>");
			out.print("			<label for='cbYes'>Ja</label>");
			out.print("			<input id='cbNo' type='radio' name='cbGroupKlausur' value='0' checked='checked'/>");
			out.print("			<label for='cbNo'>Nein</label>");
		}
		out.println("		<p>Platz f&uuml;r weitere Bemerkungen :</p>");
		out.println("		<textarea id='taBemerkungen' rows='5' col='65'>"+bemerkung+"</textarea>");
		out.println("		<form action=\"rapla\" method=\"get\">");
					out.println(getHiddenField("key", key));
		out.println("			<input id='inpChanged' type='hidden' name='changed' value='0'>");
		out.println("			<input id='inpKontakt' type='hidden' name='contact' value=''>");
		out.println("			<input id='inpStunden' type='hidden' name='hours' value=''>");
		out.println("			<input id='inpTimeTable' type='hidden' name='time' value=''>");
		out.println("			<input id='inpAusnahmen' type='hidden' name='exception' value=''>");		
		out.println("			<input id='inpAufsicht' type='hidden' name='control' value=''>");
		out.println("			<input id='inpBemerkungen' type='hidden' name='comment' value=''>");	
		out.print("				<input id='inpSubmit' type ='submit' name='rapla' value='Senden'/>");
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
	private String[][] formatTimeString(String str){
		//Überprüfen ob TimeString leer ist, ggf. füllen
		if(str.equals("") || str == null){
			for(int i=0;i<168;i++){
				str+="1";
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
		int verId = Integer.parseInt(eventId);
		int dozId = Integer.parseInt(dozentId);
		Date[] ausnahmenDateArray = new Date[ausnahmenArray.length];
		SimpleDateFormat strToDate = new SimpleDateFormat("yyyy-MM-dd");
		
		StorageOperator lookup;
		Reservation veranstaltung;
		try {
			lookup = getContext().lookup( StorageOperator.class);
			SimpleIdentifier idtype = new SimpleIdentifier(Reservation.TYPE, verId);
			veranstaltung = (Reservation) lookup.resolve(idtype);
			
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
		newConstraint = ConstraintService.addorchangeSingleDozConstraint(constraint, dozId, time, ausnahmenDateArray, status);
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
