package org.rapla.plugin.dhbwscheduler.server;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.plugin.dhbwscheduler.*;
import org.rapla.servletpages.RaplaPageGenerator;


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
				DhbwschedulerServiceImpl service234 = new DhbwschedulerServiceImpl(this.getContext());
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
					service234.storeIntoReservation(2,cxd,ausnahme);
					
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
	
}
