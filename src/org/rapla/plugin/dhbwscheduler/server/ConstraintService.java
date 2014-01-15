package org.rapla.plugin.dhbwscheduler.server;


import java.util.Date;

import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.storage.StorageOperator;

public class ConstraintService extends RaplaComponent{

	public ConstraintService(RaplaContext context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/*Constraint:
	 * DOZID_000000000000000000000000...111111111111111111111111_ExceptionDate,ExceptionDate_Status/n
	 * DOZID_000000000000000000000000...111111111111111111111111_ExceptionDate,ExceptionDate_Status
	 */
	

	public static String buildDozConstraint(int[] doz_IDs, int[][][] dozConsts, Date[][] exceptDates, int[] status){
		
		return null;
	}
	
	public static String[] getDozIDs(String Constraint){
		
		return null;
	}
	
	public static String getDozConstraint (String Constraint){
		// in arbeit!
		/*String DozCount[] = Constraint.split("/n");
		
		for (int i = 0; i< DozCount.length;i++){
			DozCount[i] = 
		}
		String ergebnis="";
		for (int i = 0; i<168;i++)
			ergebnis = "" + (int) Math.random();
			*/
		
		return null;
	}
	
	public static String getDozConstraints (String Constraint, int doz_ID){
		
		return null;
	}
	
	public static String[] getExceptionDates (Reservation reservation){
		//in arbeit!
		
		int i = 2;
		int j = 0;
		
		String dozConst = reservation.getClassification().getValue("Doz_Const").toString();
		String splitDozConst[] = dozConst.split("_");
		
		String[] exceptionDates = new String[10];
		
		while (splitDozConst[i] != null){
			exceptionDates[j] = splitDozConst[i];
			i=i+4;
			j++;
		}
		return exceptionDates;
	}	
	
	public static String[] getExceptionDate (Reservation reservation,int doz_ID ){
		
		return null;
	}
	
	public static int getStatus(String dozConst, int doz_id){
		return 0;
	}
	
}
