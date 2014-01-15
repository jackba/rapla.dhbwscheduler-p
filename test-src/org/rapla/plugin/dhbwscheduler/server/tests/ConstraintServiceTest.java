package org.rapla.plugin.dhbwscheduler.server.tests;

import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.entities.domain.Allocatable;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.dhbwscheduler.server.ConstraintService;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;

public class ConstraintServiceTest extends RaplaTestCase {
	ClientFacade facade;
    Locale locale;

    public ConstraintServiceTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ConstraintServiceTest.class);
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

    public void testGetDozConstraints(){
        String TestConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "Date,Date,Date_"
        		+ "Status/n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "Date,Date_"
        		+ "Status";
    	int[] sollergebnis = {2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    	int[] testergebnis= ConstraintService.getDozConstraints(TestConstraint);
    	
    	for (int i = 0; i< sollergebnis.length; i++){
    		assertEquals(sollergebnis[i],testergebnis[i]);
    	}
    	
    }
    
    public void testGetDozConstraint(){
    	String TestConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "Date,Date,Date_"
        		+ "Status/n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "Date,Date_"
        		+ "Status";
    	
    	int[] sollergebnis = {1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    	                      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    	int[] testergebnis= ConstraintService.getDozConstraint(TestConstraint,111);
    	
    	for (int i = 0; i< sollergebnis.length; i++){
    		assertEquals(sollergebnis[i],testergebnis[i]);
    	}
    	
    }
}
