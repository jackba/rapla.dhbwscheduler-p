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
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;

public class DhbwschedulerServiceTest extends RaplaTestCase {
	ClientFacade facade;
    Locale locale;

    public DhbwschedulerServiceTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(DhbwschedulerServiceTest.class);
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

	public void testRessourcenVerfuegbarkeit() throws Exception {
		DhbwschedulerServiceImpl service = new DhbwschedulerServiceImpl(getContext());
		int[] ergebnis = new int[10];
//		service.
	}

    
}
