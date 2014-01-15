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
        String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "Date,Date,Date_"
        		+ "Status\n"
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
    	int[] testergebnis= ConstraintService.getDozConstraints(testConstraint);
    	
    	for (int i = 0; i< sollergebnis.length; i++){
    		assertEquals(sollergebnis[i],testergebnis[i]);
    	}
    	
    }
    
    public void testGetDozConstraint(){
    	String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "Date,Date,Date_"
        		+ "Status\n"
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
    	int[] testergebnis= ConstraintService.getDozConstraint(testConstraint,111);
    	
    	for (int i = 0; i< sollergebnis.length; i++){
    		assertEquals(sollergebnis[i],testergebnis[i]);
    	}
    	
    }
    
    public void testGetExceptionDates_1(){
    	String TestConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140_"
        		+ "Status\n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "_"
        		+ "Status";
    	
    	Date[] sollergebnis = new Date[1];
    	sollergebnis[0] = new Date(1161775163140L);
    	
    	Date[] testergebnis= ConstraintService.getExceptionDates(TestConstraint);    	
    	
    	assertEquals(sollergebnis[0],testergebnis[0]);
    }
    
    public void testGetExceptionDates_2(){
    	String TestConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140_"
        		+ "Status\n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "_"
        		+ "Status";
    	
    	Date[] sollergebnis = new Date[2];
    	sollergebnis[0] = new Date(1161775163140L);
    	sollergebnis[1] = new Date(2161775163140L);
    	
    	Date[] testergebnis= ConstraintService.getExceptionDates(TestConstraint);    	
    	
    	assertEquals(sollergebnis[0],testergebnis[0]);
    	assertEquals(sollergebnis[1],testergebnis[1]);
    }
    
    public void testGetExceptionDates_3(){
    	String TestConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "Status\n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "Status";
    	
    	Date[] sollergebnis = new Date[5];
    	sollergebnis[0] = new Date(1161775163140L);
    	sollergebnis[1] = new Date(2161775163140L);
    	sollergebnis[2] = new Date(2161775173140L);
    	sollergebnis[3] = new Date(2161775183140L);
    	sollergebnis[4] = new Date(2161775193140L);
    	
    	Date[] testergebnis= ConstraintService.getExceptionDates(TestConstraint);    	
    	
    	assertEquals(sollergebnis[0],testergebnis[0]);
    	assertEquals(sollergebnis[1],testergebnis[1]);
    	assertEquals(sollergebnis[2],testergebnis[2]);
    	assertEquals(sollergebnis[3],testergebnis[3]);
    	assertEquals(sollergebnis[4],testergebnis[4]);
    	
    }
    
    public void testGetExceptionDatesDoz(){
    	String TestConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140_"
        		+ "Status\n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "1161775163140_"
        		+ "Status";
    	
    	Date[] sollergebnis = new Date[2];
    	sollergebnis[0] = new Date(1161775163140L);
    	sollergebnis[1] = new Date(2161775163140L);
    	
    	Date[] testergebnis= ConstraintService.getExceptionDatesDoz(TestConstraint,111);    	
    	
    	assertEquals(sollergebnis[0],testergebnis[0]);
    	assertEquals(sollergebnis[1],testergebnis[1]);
    }
    
    public void testGetStatus(){
    	String TestConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140_"
        		+ "0\n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "1161775163140_"
        		+ "1";
    	    	
    	int testergebnis1= ConstraintService.getStatus(TestConstraint,111);    	
    	int testergebnis2= ConstraintService.getStatus(TestConstraint,222);   
    	
    	assertEquals(testergebnis1,0);
    	assertEquals(testergebnis2,1);
    }
    
    public void testbuildDozConstraint(){
    	String ergebnisConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";
    	
    	Date[][] exceptDates = new Date[2][5];
    	exceptDates[0][0] = new Date(1161775163140L);
    	exceptDates[0][1] = new Date(2161775163140L);
    	exceptDates[0][2] = new Date(2161775173140L);
    	exceptDates[1][0] = new Date(2161775183140L);
    	exceptDates[1][1] = new Date(2161775193140L);
    	
    	int[] doz_IDs = {111, 222};
    	
    	int[][][] dozConsts = new int[2][7][24];
    	for (int i = 0; i < 2; i++) {
    		for (int j = 0; j < 7; j++) {
    			for (int k = 0; k < 24; k++) {
    				dozConsts[i][j][k] = i;
    			}
    		}
    	}
    	
    	dozConsts[0][0][0] = 1;
    	dozConsts[0][0][2] = 1;
    	dozConsts[1][0][1] = 0;
    	dozConsts[1][0][2] = 0;
    	
    	int[] status = {1, 2};
    	
    	String testergebnis= ConstraintService.buildDozConstraint(doz_IDs, dozConsts, exceptDates, status);    	
    	
    	assertEquals(ergebnisConstraint,testergebnis);
    }
    
    public void testgetDozIDs(){
    	String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";
    		
		int[] dozIDergebnis = ConstraintService.getDozIDs(testConstraint);
    		
		assertEquals(111,dozIDergebnis[0]);
		assertEquals(222,dozIDergebnis[1]);
    		
    }

}
