package org.rapla.plugin.dhbwscheduler.server.tests;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.Configuration;
import org.rapla.framework.DefaultConfiguration;
import org.rapla.plugin.dhbwscheduler.server.ConstraintService;
import org.rapla.plugin.dhbwscheduler.server.SchedulerConstraintsPageGenerator;

public class SchedulerConstraintsPageGeneratorTest  extends RaplaTestCase {
	ClientFacade facade;
    Locale locale;
	public SchedulerConstraintsPageGeneratorTest(String name) {
		super(name);
	}
	
    public static Test suite() {
        return new TestSuite(SchedulerConstraintsPageGeneratorTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        facade = getContext().lookup(ClientFacade.class );
        facade.login("homer","duffs".toCharArray());
        locale = Locale.getDefault();
    }

    protected void tearDown() throws Exception {
        facade.logout();
        super.tearDown();
    }
    
    public void testeGetInformation(){
    	String[] feld = new String[3];
    	feld[0] = "1";
    	feld[1] = "2";
    	feld[2] = "4";
    	String suche = "4";
    	String suche2 = "3";
    	assertEquals("4",SchedulerConstraintsPageGenerator.getInformation(feld,suche));
    	assertEquals("",SchedulerConstraintsPageGenerator.getInformation(feld,suche2));
    }
    
    public void testeGetHiddenField(){
    	String ergebnis = "<input type=\"hidden\" name=\"2\" value=\"3\"/>";
    	String test1 = "2";
    	String test2 = "3";
    	DefaultConfiguration config = new DefaultConfiguration("locale"); 
    	SchedulerConstraintsPageGenerator pg = new SchedulerConstraintsPageGenerator(getContext(),config);
    	assertEquals(ergebnis,pg.getHiddenField(test1,test2));
    }
    
    public void testeformatDateForDatepicker(){
    	String ergebnis = "3-2-1";
    	String test = "1.2.3";
    	DefaultConfiguration config = new DefaultConfiguration("locale"); 
    	SchedulerConstraintsPageGenerator pg = new SchedulerConstraintsPageGenerator(getContext(),config);
    	assertEquals(ergebnis,pg.formatDateForDatepicker(test));
    }
    
    public void testegeneratePage(){
    	DefaultConfiguration config = new DefaultConfiguration("locale"); 
    	SchedulerConstraintsPageGenerator pg = new SchedulerConstraintsPageGenerator(getContext(),config);
    	try {	
    		HttpServletRequest request = mock(HttpServletRequest.class);
    		HttpServletResponse response = mock(HttpServletResponse.class);
    		ServletContext context = mock(ServletContext.class);
			pg.generatePage(context, request, response);
		} catch (IOException | ServletException e) {
			e.printStackTrace();
		}
    }
    
}
