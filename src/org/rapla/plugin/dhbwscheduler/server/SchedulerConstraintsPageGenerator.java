package org.rapla.plugin.dhbwscheduler.server;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rapla.components.util.DateTools;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.*;
import org.rapla.servletpages.RaplaPageGenerator;
import org.rapla.storage.StorageOperator;


public class SchedulerConstraintsPageGenerator extends RaplaComponent implements RaplaPageGenerator {

	public SchedulerConstraintsPageGenerator(RaplaContext context,Configuration config) {
		super(context);
		setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
	}

	public void generatePage(ServletContext context,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException 
	{		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("content-type text/html");
		
		java.io.PrintWriter out = response.getWriter();
		out.print(getString("html_welcome_text"));
		
		out.print("Hier steht Chichis Formular!");		
		out.print(request.getParameter("id"));
		out.print(request.getParameter("name"));
		out.print(request.getParameter("kurs"));
		
		String linkPrefix = request.getPathTranslated() != null ? "../": "";
		out.println("<form action=\""+linkPrefix + "rapla\" method=\"get\">");
		
		out.println(getHiddenField("page", "scheduler-constraints"));
		out.println(getHiddenField("ID", "test"));
		
		if(request.getParameter("ID") != null)
		{
			String id = request.getParameter("ID");
			
			if (id.equals("2"))
			{
				
				int[][] cxd = new int[7][25];
				Date[] ausnahme = new Date[2];
				
				
				cxd[0][8] = 1;
				cxd[0][9] = 1;
				cxd[0][10] = 0;
				cxd[0][11] = 1;
				cxd[0][21] = 1;
				cxd[0][22] = 1;
				cxd[0][23] = 1;
				
				ausnahme[0] = new Date();
				ausnahme[1] = new Date();
				
				try {
					this.storeIntoReservation(5,cxd,ausnahme,2405);
					
				} catch (RaplaContextException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (EntityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				out.print("setz die Hunde los");
			}
		}
		
		
		
		out.print("<input type ='submit' value='anlegen'/>");
		out.close();
	}
	
	String getHiddenField( String fieldname, String value) {
        return "<input type=\"hidden\" name=\"" + fieldname + "\" value=\"" + value + "\"/>";
    }
	
	/*
	 * (non-Javadoc)
	 * Test Daten aus HTML Seite lesen
	 * @see org.rapla.plugin.dhbwscheduler.DhbwschedulerService#schedule(org.rapla.entities.storage.internal.SimpleIdentifier[])
	 */
	public void storeIntoReservation(int reservationID, int[][] calendar, Date[] ausnahmeDatum, int DozentID) throws RaplaContextException, EntityNotFoundException
	{
		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		SimpleIdentifier idtype = new SimpleIdentifier(Reservation.TYPE, reservationID); 
		Reservation veranstaltung = (Reservation) lookup.resolve(idtype);
		
		
		//Attribute setzen
		try {
			Reservation editVeranstaltung =getClientFacade().edit(veranstaltung);
			
			String planungsconstrains = (String) editVeranstaltung.getClassification().getValue("planungsconstraints");
			String ausnahmenconstraints = (String) editVeranstaltung.getClassification().getValue("ausnahmeconstraints");

									
			String newPlanungsconstraint = reservationStringbearbeiten(DozentID, planungsconstrains, constraintToString(calendar));
			String newAusnahmeconstraint = reservationStringbearbeiten(DozentID, ausnahmenconstraints, ausnahmenToString(ausnahmeDatum));
			
			
			if (!newPlanungsconstraint.isEmpty()){
				editVeranstaltung.getClassification().setValue("planungsconstraints", newPlanungsconstraint);
			}
			
			if(!newAusnahmeconstraint.isEmpty()){
				editVeranstaltung.getClassification().setValue("ausnahmeconstraints", newAusnahmeconstraint);
			}
						
			getClientFacade().store( editVeranstaltung );
			
			
		} catch (RaplaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
	}
	
	private String reservationStringbearbeiten(int dozentID, String getconstraint, String changeconstraint) {
		// TODO Auto-generated method stub
		if(getconstraint == null){
			getconstraint = "";
		}
		if (changeconstraint == null || changeconstraint.isEmpty()){
			return "";
		}
		
		String dozent = String.valueOf(dozentID) + "_";
		String DozentConstraints = "";
		String[] dozentenString = getconstraint.split("\n");
		
		if(getconstraint.contains(dozent)){
			for(int i = 0; i < dozentenString.length ; i++){
				
				if(dozentenString[i].contains(dozent)){
					dozentenString[i] = dozent + changeconstraint;
				}
				
				DozentConstraints += dozentenString[i] + "\n"; 
				
			}
		}else{
			if (getconstraint.isEmpty()){
				DozentConstraints += dozent + changeconstraint;
			}else{
				DozentConstraints += getconstraint + "\n" +  dozent + changeconstraint;
			}
			
		}
		
		
				
		if(DozentConstraints.endsWith("\n")){
			DozentConstraints = DozentConstraints.substring(0, DozentConstraints.length()-1);
		}
		
		return DozentConstraints;
		
	}

	private String ausnahmenToString(Date[] ausnahmeDatum) {
		
		String ausnahmenString = "";
		
		for (int i = 0; i< ausnahmeDatum.length ; i++)
		{
			if (ausnahmeDatum[i] != null){
				ausnahmenString = ausnahmenString + DateTools.formatDate(ausnahmeDatum[i]) + "," ;
			}
			
		}
		
		if (ausnahmenString.endsWith(",")){
			ausnahmenString = ausnahmenString.substring(0, ausnahmenString.length()-1);
		}
		
		return ausnahmenString;
		
	}

	private String constraintToString(int[][] constraints) {
		
		String stringconstraint = "";
		
		for (int day = 0; day < constraints.length; day++){
			
			int Time1 = 0;
			int Time2 = 0;
			int Marker = 0;
			stringconstraint = stringconstraint + String.valueOf(day+1) + ":"; 
			
			for (int hour = 0; hour < constraints[day].length; hour++){
				
				if (constraints[day][hour] == 1 && Marker == 0){
					
					//start
					Marker = 1;
					
					Time1 = hour;
					stringconstraint = stringconstraint + String.valueOf(Time1);
					
				}
				
				if (constraints[day][hour] == 0 && Marker == 1){
					
					//end
					Marker = 0;
					
					Time2 = hour;
					stringconstraint = stringconstraint + "-" + String.valueOf(Time2) + ",";
					
				}
			
			}
			if (stringconstraint.endsWith(",")){
				stringconstraint = stringconstraint.substring(0, stringconstraint.length()-1);
			}
			stringconstraint = stringconstraint + ";";
			
		}
		return stringconstraint;
	}
	
}
