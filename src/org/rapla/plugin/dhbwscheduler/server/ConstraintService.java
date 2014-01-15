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
		/*Zeit als Long in den String packen
		 * String = exceptionDates[i][j].getTime();
		 * Dates werden mit Komma getrennt
		 */
		String result = "";
		
		for(int i = 0; i<doz_IDs.length; i++) {
			result += doz_IDs[i] + "_";
			
			for(int j = 0; j<dozConsts[i].length; j++) {
				for(int k = 0; k<dozConsts[i][j].length;k++) {
					result += dozConsts[i][j][k];
				}
			}
			
			result += "_";
			for (int j = 0; j<exceptDates[i].length; j++) {
				if (exceptDates[i][j] == null) {
					result = result.substring(0, result.length()-1);
					break;
				}
				result += exceptDates[i][j].getTime();
				if(j < exceptDates[i].length-1) {
					result += ",";
				}
			}
			result += "_";
			
			result += status[i];
			
			if(i < doz_IDs.length-1) {
				result += "\n";
			}
		}
		
		return result;
	}
	
	public static String[] getDozIDs(String constraint){
		String[] result = {};
		if (constraint == null) {
			return result;
		}
		String dozCount[] = constraint.split("/n");
		result = new String[dozCount.length];
		
		
		for(int i = 0; i < dozCount.length; i++){
			result[i] = dozCount[i].split("_")[0];
		}
		
		return result;
	}
	
	public static int[] getDozConstraints (String Constraint){
		String DozCount[] = Constraint.split("/n");
		int[][] DozConst = new int[DozCount.length][168];
		int [] ergebnis = new int[168];
		
		for (int i = 0; i< DozCount.length;i++){
			String[] split = DozCount[i].split("_");
			for (int j = 0; j<168; j++){
				DozConst[i][j] = split[1].charAt(j)-48;
			}
		}

		for (int i = 0; i <168; i++){
			for (int j = 0; j<DozCount.length;j++){
				if (DozConst[j][i] == 0){
					ergebnis[i] = 0;
					break;
				}
				else{
					ergebnis[i]+=DozConst[j][i];
				}
			}
		}
		return ergebnis;
	}
	
	public static int[] getDozConstraint (String Constraint, int doz_ID){
		String DozCount[] = Constraint.split("/n");
		int [] ergebnis = new int[168];
		
		for(String DozConst:DozCount){
			
			String[] split = DozConst.split("_");
			if (Integer.valueOf(split[0]) == doz_ID){
				int index = DozConst.indexOf('_')+1;

				for (int j = 0; j<168; j++){		
					ergebnis[j] = DozConst.charAt(index+j)-48;
				}
			}
		}
		return ergebnis;
	
	}
	
	public static Date[] getExceptionDates (String Constraint){
		String DozCount[] = Constraint.split("/n");
		String ExceptionDates[][] = new String[DozCount.length][];
		Date[] ergebnis = {};
		
		for (int i = 0; i< DozCount.length;i++){
			String[] split = DozCount[i].split("_");
			ExceptionDates[i] = split[2].split(",");
		}
		
		//mergen der ExceptionDates
		int anzahlExceptionDates = 0;
		for (int i = 0; i< DozCount.length;i++){
			anzahlExceptionDates+=ExceptionDates[i].length;			
		}
		
		ergebnis = new Date[anzahlExceptionDates];
		
		int counter = 0;
		for (int i = 0; i<ExceptionDates.length;i++){
			for (int j=0; j<ExceptionDates[i].length;j++){
				if (!ExceptionDates[i][j].isEmpty())
					ergebnis[counter++] = new Date(Long.parseLong(ExceptionDates[i][j]));
			}
		}
		return ergebnis;
	}	
	
	public static Date[] getExceptionDatesDoz (String Constraint,int doz_ID ){
		String DozCount[] = Constraint.split("/n");
		String ExceptionDates[] = {};
		Date[] ergebnis = {};
		
		for(int i = 0; i< DozCount.length;i++){
			String[] split = DozCount[i].split("_");
		
			if (Integer.valueOf(split[0]) == doz_ID){
				ExceptionDates = split[2].split(",");
				break;
			}
		}

		if (ExceptionDates.length==0){
			return ergebnis;
		}
		else{
		ergebnis = new Date[ExceptionDates.length];
				
		int counter = 0;
		for (int i = 0; i<ExceptionDates.length;i++){
				if (!ExceptionDates[i].isEmpty())
					ergebnis[counter++] = new Date(Long.parseLong(ExceptionDates[i]));
				}
		return ergebnis;
		}
	}
		
	
	public static int getStatus(String Constraint, int doz_ID){
		String DozCount[] = Constraint.split("/n");
		
		for(String DozConst:DozCount){
			
			String[] split = DozConst.split("_");
			if (Integer.valueOf(split[0]) == doz_ID){
				return Integer.valueOf(split[3]);
			}
		}
		return -1;

	}
	
}
