package org.rapla.plugin.dhbwscheduler.server.tests;

import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rapla.RaplaTestCase;
import org.rapla.facade.ClientFacade;
import org.rapla.plugin.dhbwscheduler.server.ConstraintService;

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
    
    public void testNullWerte(){
    	//666 = Boese Doz_ID
    	String TestConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "Date,Date,Date_"
        		+ "Status";
    	
    	assertEquals(-1,ConstraintService.getStatus(null, "111"));
    	assertEquals(-1,ConstraintService.getStatus(TestConstraint,"666"));
    	
    	assertEquals(0,ConstraintService.getDozConstraints(null).length);
    	
    	assertEquals(0,ConstraintService.getDozIntConstraints(null, "111").length);
    	assertEquals(0,ConstraintService.getDozIntConstraints(TestConstraint, "666").length);
    	
    	assertEquals(0,ConstraintService.getDozIDs(null).length);
    	
    	assertEquals(0,ConstraintService.getExceptionDates(null).length);
    	
    	assertEquals(0,ConstraintService.getExceptionDatesDoz(null, "666").length);
    	assertEquals(0,ConstraintService.getExceptionDatesDoz(TestConstraint, "666").length);
    	
    	assertEquals(true,ConstraintService.getDozStringConstraint(null, "666").isEmpty());
    	
    	assertEquals("", ConstraintService.changeDozConstraint(null, "111", ConstraintService.CHANGE_SINGLECONSTRAINT, ""));
    }
    
    public void testcreateclass(){
    	ConstraintService consserv = new ConstraintService();
    }

    public void testGetDozConstraints_1Dozent(){
        String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "Date,Date,Date_"
        		+ "Status";
    	int[] sollergebnis = {1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
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
    
    public void testGetDozConstraints_2Dozenten(){
        String testConstraint = ""
        		+ "111_"
        		+ "101020000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "Date,Date,Date_"
        		+ "Status\n"
        		+ "222_"
        		+ "100121111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "Date,Date_"
        		+ "Status";
    	int[] sollergebnis = {2,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
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
    
    public void testGetDozConstraints_3Dozenten(){
        String testConstraint = ""
        		+ "111_"
        		+ "101021100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "Date,Date,Date_"
        		+ "Status\n"
        		+ "222_"
        		+ "100122211111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "Date,Date_"
        		+ "Status\n"
        		+ "222_"
        		+ "100122111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "Date,Date_"
        		+ "Status";
    	int[] sollergebnis = {3,0,0,0,6,5,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
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
    	int[] testergebnis= ConstraintService.getDozIntConstraints(testConstraint,"111");
    	
    	for (int i = 0; i< sollergebnis.length; i++){
    		assertEquals(sollergebnis[i],testergebnis[i]);
    	}
    	
    }
    
    public void testGetExceptionDates_1(){
    	String testConstraint = ""
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
    	
    	Date[] testergebnis= ConstraintService.getExceptionDates(testConstraint);    	
    	
    	assertEquals(sollergebnis[0],testergebnis[0]);
    }
    
    public void testGetExceptionDates_2(){
    	String testConstraint = ""
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
    	
    	Date[] testergebnis= ConstraintService.getExceptionDates(testConstraint);    	
    	
    	assertEquals(sollergebnis[0],testergebnis[0]);
    	assertEquals(sollergebnis[1],testergebnis[1]);
    }
    
    public void testGetExceptionDates_3(){
    	String testConstraint = ""
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
    	
    	Date[] testergebnis= ConstraintService.getExceptionDates(testConstraint);    	
    	
    	assertEquals(sollergebnis[0],testergebnis[0]);
    	assertEquals(sollergebnis[1],testergebnis[1]);
    	assertEquals(sollergebnis[2],testergebnis[2]);
    	assertEquals(sollergebnis[3],testergebnis[3]);
    	assertEquals(sollergebnis[4],testergebnis[4]);
    	
    }
    
    public void testGetExceptionDatesDoz(){
    	String testConstraint = ""
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
    	
    	Date[] testergebnis= ConstraintService.getExceptionDatesDoz(testConstraint,"111");    	
    	
    	assertEquals(sollergebnis[0],testergebnis[0]);
    	assertEquals(sollergebnis[1],testergebnis[1]);
    }
    
    public void testGetExceptionDatesDoz_4() {
    	String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140_"
        		+ "Status\n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "_"
        		+ "Status";
    	
    	assertEquals(0,ConstraintService.getExceptionDatesDoz(testConstraint,"222").length);
    }

    public void testGetStatus(){
    	String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140_"
        		+ "0\n"
        		+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "1161775163140_"
        		+ "1";
    	    	
    	int testergebnis1= ConstraintService.getStatus(testConstraint,"111");    	
    	int testergebnis2= ConstraintService.getStatus(testConstraint,"222");   
    	
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
    	
    	String[] dozIDs = {"111", "222"};
    	
    	String[] dozConsts = { "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", 
    			"100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" };
    	
    	int[] status = {1, 2};
    	
    	String testergebnis= ConstraintService.buildDozConstraint(dozIDs, dozConsts, exceptDates, status);    	
    	
    	assertEquals(ergebnisConstraint,testergebnis);
    }

    public void testbuildDozConstraintNULL(){
    	String ergebnisConstraint = ""
        		+ "111_"
        		+ "_"
        		+ "_"
        		+ "1\n"
    			+ "222_"
        		+ "_"
        		+ "_"
        		+ "2";
    	
    	String[] dozIDs = {"111", "222"};
    	
    	int[] status = {1, 2};
    	
    	String[] dozConsts = new String[2];
    	Date[][] exceptDates = {{}, {}};
    	
    	String testergebnis= ConstraintService.buildDozConstraint(dozIDs, dozConsts, exceptDates, status);    	
    	
    	assertEquals(ergebnisConstraint,testergebnis);
    }

    public void testbuildDozConstraintEINERNULL(){
    	String ergebnisConstraint = ""
        		+ "111_"
        		+ "_"
        		+ "_"
        		+ "1";
    	
    	String dozID = "111";
    	
    	int status = 1;
    	
    	String testergebnis= ConstraintService.buildDozConstraint(dozID, null, null, status);    	
    	
    	assertEquals(ergebnisConstraint,testergebnis);
    }

    public void testbuildDozConstraintEINER(){
    	String ergebnisConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1";
    	
    	String dozID = "111";
    	
    	int status = 1;

    	String dozConst = "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    	
    	Date[] exceptDates = new Date[3];
    	exceptDates[0] = new Date(1161775163140L);
    	exceptDates[1] = new Date(2161775163140L);
    	exceptDates[2] = new Date(2161775173140L);
    	
    	String testergebnis= ConstraintService.buildDozConstraint(dozID, dozConst, exceptDates, status);    	
    	
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
    		
		String[] dozIDergebnis = ConstraintService.getDozIDs(testConstraint);
    		
		assertEquals("111",dozIDergebnis[0]);
		assertEquals("222",dozIDergebnis[1]);
    }

	public void testchangeSingleConstraint() {
    	String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";

    	String ergebnisConstraint = ""
        		+ "111_"
        		+ "111111111100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";

    	String change_const = "111111111100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    	
    	assertEquals(ergebnisConstraint, ConstraintService.changeDozConstraint(testConstraint, "111", ConstraintService.CHANGE_SINGLECONSTRAINT, change_const));
    	
	}

	public void testchangeDates() {
    	String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";

    	String ergebnisConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161785163140,2161795163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";

    	Date[] changeDates = new Date[3];
    	changeDates[0] = new Date(1161785163140L);
    	changeDates[1] = new Date(2161795163140L);
    	changeDates[2] = new Date(2161775173140L);
    	
    	assertEquals(ergebnisConstraint, ConstraintService.changeDozConstraint(testConstraint, "111", ConstraintService.CHANGE_SINGLEDATES, changeDates));
	}


	public void testchangeStatus() {
    	String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";

    	String ergebnisConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "2\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";

    	int changeStatus = 2;
    	assertEquals(ergebnisConstraint, ConstraintService.changeDozConstraint(testConstraint, "111", ConstraintService.CHANGE_SINGLESTATUS, changeStatus));
	}

	public void testchangeStatusException() {
    	String testConstraint = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";

    	assertEquals("", ConstraintService.changeDozConstraint(testConstraint, "111", ConstraintService.CHANGE_SINGLESTATUS, "abc"));
	}
	
	public void testgetReservationStatus(){
		String testConstraint0 = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";
	
		
		
		String testConstraint1 = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "1\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "1";
		
		
		
		String testConstraint2 = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "0\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "0";
		
		
		
		String testConstraint3 = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "2\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";
		
		
		
		String testConstraint4 = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "0\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "2";
		
		
		
		String testConstraint5 = ""
        		+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "0\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "1";
		
		
		

		
//		0 uneingeladen
//		1 eingeladen
//		2 erfasst
//		3 teilweise erfasst
//		4 teilweise eingeladen
		assertEquals(-1,ConstraintService.getReservationStatus(null));
		assertEquals(4,ConstraintService.getReservationStatus(testConstraint0));
		assertEquals(1,ConstraintService.getReservationStatus(testConstraint1));
		assertEquals(0,ConstraintService.getReservationStatus(testConstraint2));
		assertEquals(2,ConstraintService.getReservationStatus(testConstraint3));
		assertEquals(4,ConstraintService.getReservationStatus(testConstraint4));
		assertEquals(3,ConstraintService.getReservationStatus(testConstraint5));
	}
	
	public void testaddorchangeSingleDozConstraint(){
		String constraint1 	= ""
				+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "0\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "1";
		
		String dozID		= "222";
		String dozConst		= "100111111001111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
		Date[] exceptDate	= new Date[2];
		exceptDate[0]		= new Date(1161775163140L);;
		exceptDate[1]		= new Date(2161775173140L);;
		int status			= 0;
		
		String ergebnis_constraint = ""
				+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "0\n"
    			+ "222_"
        		+ "100111111001111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "1161775163140,2161775173140_"
        		+ "0";
		assertEquals(ergebnis_constraint,ConstraintService.addorchangeSingleDozConstraint(constraint1,dozID,dozConst,exceptDate,status));
		
		String constraint2 	= ""
				+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "0";
		assertEquals(ergebnis_constraint,ConstraintService.addorchangeSingleDozConstraint(constraint2,dozID,dozConst,exceptDate,status));
		
		String ergebnis_constraint2= ""
				+ "222_"
        		+ "100111111001111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "1161775163140,2161775173140_"
        		+ "0";
		
		assertEquals(ergebnis_constraint2,ConstraintService.addorchangeSingleDozConstraint(null,dozID,dozConst,exceptDate,status));
	}	
	public void testinitDozConstraint(){
		String constraint1 	= ""
				+ "111_"
        		+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "0\n"
    			+ "222_"
        		+ "100111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111_"
        		+ "2161775183140,2161775193140_"
        		+ "1";
		
		String erg_constraint1 = ""
				+ "111_"
				+ "101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_"
        		+ "1161775163140,2161775163140,2161775173140_"
        		+ "0\n"
				+ "223___0\n"
				+ "333___0";
		
		String erg_constraint2 = ""
				+ "111___0\n"
				+ "223___0\n"
				+ "333___0";
		
		String[] dozid = new String[3];
		dozid[0] = "111";
		dozid[1] = "223";
		dozid[2] = "333";
				
		assertEquals(erg_constraint1,ConstraintService.initDozConstraint(constraint1,dozid));
		assertEquals(erg_constraint2,ConstraintService.initDozConstraint(null,dozid));
	}
}


