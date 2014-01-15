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
	
	public static int[] getDozConstraints (String Constraint){
		
		String DozCount[] = Constraint.split("/n");
		int[][] DozConst = new int[DozCount.length][168];
		int [] ergebnis = new int[168];
		
		for (int i = 0; i< DozCount.length;i++){
			int index = DozCount[i].indexOf('_');
			DozCount[i] = DozCount[i].substring(index+1,DozCount[i].indexOf('_',index+1));
			
			for (int j = 0; j<168; j++){
				DozConst[i][j] = Integer.valueOf(DozCount[i].substring(j, j+1));
			}
		}
		
		//mergen der Doz-Constraints
		for (int i = 0; i <168; i++){
			for (int j = 0; j<DozCount.length;j++){
				if (DozConst[j][i] == 0){
					ergebnis[i] = 0;
					break;
				}
				else{
					ergebnis[i] = 1;
				}
			}
		}
		
		return ergebnis;
	}
	
	public static int[] getDozConstraint (String Constraint, int doz_ID){
		
		return null;
	}
	
	public static Date[] getExceptionDates (Reservation reservation){
		//in arbeit!
		/*
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
		*/
		return null;
	}	
	
	public static Date[] getExceptionDatesDoz (Reservation reservation,int doz_ID ){
		
		return null;
	}
	
	public static int getStatus(String dozConst, int doz_id){
		return 0;
	}
	
}
