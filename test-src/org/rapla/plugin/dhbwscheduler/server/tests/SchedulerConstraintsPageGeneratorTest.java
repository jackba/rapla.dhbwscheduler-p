package org.rapla.plugin.dhbwscheduler.server.tests;

import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.entities.domain.Reservation;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.DefaultConfiguration;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.server.SchedulerConstraintsPageGenerator;
import org.rapla.storage.StorageOperator;

@SuppressWarnings("restriction")
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
        raplaContainer.addContainerProvidedComponent( DhbwschedulerPlugin.RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( DhbwschedulerPlugin.RESOURCE_FILE.getId() ) );
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
    
    public void testegeneratePage() throws Exception{
    	DefaultConfiguration config = new DefaultConfiguration("locale"); 
    	SchedulerConstraintsPageGenerator pg = new SchedulerConstraintsPageGenerator(getContext(),config);

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletContext context = mock(ServletContext.class);
		
		PrintWriter writer = new PrintWriter("servlettest.txt");
        when(response.getWriter()).thenReturn(writer);
        
        //Null Werte Testen
        pg.generatePage(context, request, response);

		StorageOperator lookup = getContext().lookup( StorageOperator.class);
		//String idtype = new SimpleIdentifier(Reservation.TYPE, Integer.parseInt(eventId));
		Reservation r = (Reservation) lookup.resolve("c6b8bce9-a173-4960-b760-a8840e07beeb");
		
		//c6b8bce9-a173-4960-b760-a8840e07beeb mit allem (Studiengang, Kurs, Prof, Planungszyklus)
		//11993379-6c87-438b-b0ff-07a58461de1a ohne alles ??
		//1880c994-cb67-4ecf-bee5-0160c909e209 ohne alles
		//72860f35-7f63-4d69-bd66-4c27bb6204ac ohne Studiengang
		
		when(request.getParameter("id")).thenReturn(r.getId());
		when(request.getParameter("dozent")).thenReturn(r.getPersons()[0].getId());
		
		pg.generatePage(context, request, response);
		
		when(request.getParameter("id")).thenReturn(r.getId());
		when(request.getParameter("dozent")).thenReturn(r.getPersons()[1].getId());
		
		pg.generatePage(context, request, response);
		
		Reservation r2 = (Reservation) lookup.resolve("11993379-6c87-438b-b0ff-07a58461de1a");
		
		when(request.getParameter("id")).thenReturn(r2.getId());
		when(request.getParameter("dozent")).thenReturn(r2.getPersons()[0].getId());
		pg.generatePage(context, request, response);
		
		//FAIL ID Test
		//Reservation r3 = (Reservation) lookup.resolve("00000000-6c87-438b-b0ff-07a58461de1a");
		
		when(request.getParameter("id")).thenReturn("00000000-6c87-438b-b0ff-07a58461de1a");
		when(request.getParameter("dozent")).thenReturn(r2.getPersons()[0].getId());
		pg.generatePage(context, request, response);
		
		//a64520f7-69ae-4a76-9e4d-1513a1d7e363
		Reservation r3 = (Reservation) lookup.resolve("a64520f7-69ae-4a76-9e4d-1513a1d7e363");
		
		when(request.getParameter("id")).thenReturn(r3.getId());
		when(request.getParameter("dozent")).thenReturn(r3.getPersons()[0].getId());
		pg.generatePage(context, request, response);
		
		//72860f35-7f63-4d69-bd66-4c27bb6204ac
		Reservation r4 = (Reservation) lookup.resolve("72860f35-7f63-4d69-bd66-4c27bb6204ac");
		
		when(request.getParameter("id")).thenReturn(r4.getId());
		when(request.getParameter("dozent")).thenReturn(r4.getPersons()[0].getId());
		pg.generatePage(context, request, response);
		
		//changed
		when(request.getParameter("changed")).thenReturn("1");
		
		//alles
		when(request.getParameter("id")).thenReturn(r.getId());
		when(request.getParameter("dozent")).thenReturn(r.getPersons()[0].getId());
		pg.generatePage(context, request, response);
		
		//ohne Stuga
		when(request.getParameter("id")).thenReturn(r.getId());
		when(request.getParameter("dozent")).thenReturn(r.getPersons()[1].getId());
		pg.generatePage(context, request, response);
		
		//ohne
		when(request.getParameter("id")).thenReturn(r2.getId());
		when(request.getParameter("dozent")).thenReturn(r2.getPersons()[0].getId());
		pg.generatePage(context, request, response);
		
		when(request.getParameter("hours")).thenReturn("10");
		pg.generatePage(context, request, response);
		
		when(request.getParameter("exception")).thenReturn("2014-03-24,2014-03-25");
		pg.generatePage(context, request, response);
		
		when(request.getHeader("accept-language")).thenReturn("de-de");
		pg.generatePage(context, request, response);
    }
    
    public void testvalidateGeneratePage() throws Exception{
    	DefaultConfiguration config = new DefaultConfiguration("locale"); 
    	SchedulerConstraintsPageGenerator pg = new SchedulerConstraintsPageGenerator(getContext(),config);

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletContext context = mock(ServletContext.class);
		
		PrintWriter writer = new PrintWriter("servletvalidatetest.txt");
        when(response.getWriter()).thenReturn(writer);
        
        StorageOperator lookup = getContext().lookup( StorageOperator.class);
        Reservation r = (Reservation) lookup.resolve("c6b8bce9-a173-4960-b760-a8840e07beeb");
        
        //r.getClassification().setValue("planungsconstraints",null);
               
        when(request.getParameter("id")).thenReturn(r.getId());
		when(request.getParameter("dozent")).thenReturn(r.getPersons()[0].getId());
		when(request.getParameter("exception")).thenReturn("2014-03-24,2014-03-25");
		when(request.getParameter("time")).thenReturn("000000000000000000000000000000002222222222000000000000001111111111000000000000001111111111000000000000001111111111000000000000002222222222000000000000001111111111000000");
		when(request.getParameter("changed")).thenReturn("1");
        
        pg.generatePage(context, request, response);
        
        Reservation ErgRes = (Reservation) lookup.resolve("c6b8bce9-a173-4960-b760-a8840e07beeb");
        String Constraint = (String) ErgRes.getClassification().getValue("planungsconstraints");
        
        //3faa3f42-51eb-4455-8294-8ce331d8f105_000000000000000000000000000000002222222222000000000000001111111111000000000000001111111111000000000000001111111111000000000000002222222222000000000000001111111111000000_1395615600000,1395702000000_2
        String ErgConstraint = r.getPersons()[0].getId()
        				+ 		"_000000000000000000000000000000002222222222000000000000001111111111000000000000001111111111000000000000001111111111000000000000002222222222000000000000001111111111000000"
        				+ 		"_1395615600000,1395702000000"
        				+		"_2";
        assertEquals(ErgConstraint,Constraint);
//        
//        
//        assertequals(Constraint,)
        
        
        
        
        
        
    }
    
}
