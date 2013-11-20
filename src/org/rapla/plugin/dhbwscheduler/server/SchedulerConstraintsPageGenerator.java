package org.rapla.plugin.dhbwscheduler.server;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
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
		out.close();
	}

}
