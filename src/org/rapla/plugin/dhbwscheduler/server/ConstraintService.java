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
	 * DOZID_000000000000000000000000...111111111111111111111111_ExceptionDate,ExceptionDate_Status\n
	 * DOZID_000000000000000000000000...111111111111111111111111_ExceptionDate,ExceptionDate_Status
	 */
	

	public static String buildDozConstraint(int dozID, String dozConst, Date[] exceptDate, int status){
		int[] dozIDs = new int[1];
		dozIDs[0] = dozID;
		
		String[] dozConsts= new String[1];
		dozConsts[0] = dozConst;
		
		Date[][] exceptDates;
		if ( exceptDate == null) {
			exceptDates = new Date[1][0];
		}
		else {
			exceptDates = new Date[1][exceptDate.length];
			for(int i = 0; i < exceptDate.length; i++) {
				exceptDates[0][i] = exceptDate[i];
			}
		}
		
		int[] stati = new int[1];
		stati[0]  =status;
		
		return buildDozConstraint(dozIDs, dozConsts, exceptDates, stati);
	}
	
	public static String buildDozConstraint(int[] dozIDs, String[] dozConsts, Date[][] exceptDates, int[] status){
		String result = "";
		
		for(int i = 0; i<dozIDs.length; i++) {
			result += dozIDs[i] + "_";
			
			if (dozConsts[i] != null) {
				result += dozConsts[i];
			}
			
			result += "_";
			if( exceptDates[i] != null) {
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
			}
			result += "_";
			
			result += status[i];
			
			if(i < dozIDs.length-1) {
				result += "\n";
			}
		}
		
		return result;
	}
	
	public static int[] getDozIDs(String constraint){
		int[] result = {};
		if (constraint == null || constraint.equals("")) {
			return result;
		}
		String dozCount[] = constraint.split("\n");
		result = new int[dozCount.length];
		
		
		for(int i = 0; i < dozCount.length; i++){
			String[] split = dozCount[i].split("_");
			result[i] = Integer.valueOf(split[0]);
		}
		return result;
	}
	
	public static int[] getDozConstraints (String constraint){
		int [] ergebnis = {};
		
		if (constraint == null){
			return ergebnis;
		}
		
		ergebnis = new int[168];
		
		String dozCount[] = constraint.split("\n");
		int[][] dozConst = new int[dozCount.length][168];

		for (int i = 0; i< dozCount.length;i++){
			String[] split = dozCount[i].split("_");
			for (int j = 0; j<168; j++){
				dozConst[i][j] = split[1].charAt(j)-48;
			}
		}

		for (int i = 0; i <168; i++){
			for (int j = 0; j<dozCount.length;j++){
				if (dozConst[j][i] == 0){
					ergebnis[i] = 0;
					break;
				}
				else{
					ergebnis[i]+=dozConst[j][i];
				}
			}
		}
		return ergebnis;
	}
	
	public static int[] getDozConstraintsDoz (String constraint, int doz_ID){
		int [] ergebnis = {};
		
		if (constraint == null){
			return ergebnis;
		}
				
		String dozCount[] = constraint.split("\n");
		
		for(String dozConst:dozCount){
			
			String[] split = dozConst.split("_");
			if (Integer.valueOf(split[0]) == doz_ID){
				ergebnis = new int[168];
				int index = dozConst.indexOf('_')+1;

				for (int j = 0; j<168; j++){		
					ergebnis[j] = dozConst.charAt(index+j)-48;
				}
				break;
			}
		}
		return ergebnis;
	
	}
	
	public static Date[] getExceptionDates (String constraint){
		Date[] ergebnis = {};
		
		if (constraint == null){
			return ergebnis;
		}
		
		String dozCount[] = constraint.split("\n");
		String exceptionDates[][] = new String[dozCount.length][];

		
		for (int i = 0; i< dozCount.length;i++){
			String[] split = dozCount[i].split("_");
			exceptionDates[i] = split[2].split(",");
		}
		
		//mergen der ExceptionDates
		int anzahlExceptionDates = 0;
		for (int i = 0; i< dozCount.length;i++){
			anzahlExceptionDates+=exceptionDates[i].length;			
		}
		
		ergebnis = new Date[anzahlExceptionDates];
		
		int counter = 0;
		for (int i = 0; i<exceptionDates.length;i++){
			for (int j=0; j<exceptionDates[i].length;j++){
				if (!exceptionDates[i][j].isEmpty())
					ergebnis[counter++] = new Date(Long.parseLong(exceptionDates[i][j]));
			}
		}
		return ergebnis;
	}	
	
	public static Date[] getExceptionDatesDoz (String constraint,int doz_ID ){
		Date[] ergebnis = {};
		
		if (constraint == null){
			return ergebnis;
		}
		
		String dozCount[] = constraint.split("\n");
		String exceptionDates[] = {};
		
		for(int i = 0; i< dozCount.length;i++){
			String[] split = dozCount[i].split("_");
		
			if (Integer.valueOf(split[0]) == doz_ID){
				exceptionDates = split[2].split(",");
				break;
			}
		}

		if (exceptionDates.length==0){
			return ergebnis;
		}
		else{
		ergebnis = new Date[exceptionDates.length];
				
		int counter = 0;
		for (int i = 0; i<exceptionDates.length;i++){
				if (!exceptionDates[i].isEmpty())
					ergebnis[counter++] = new Date(Long.parseLong(exceptionDates[i]));
				}
		return ergebnis;
		}
	}
		
	
	public static int getStatus(String constraint, int doz_ID){
		
		if (constraint == null){
			return -1;
		}
		
		String DozCount[] = constraint.split("\n");
		
		for(String DozConst:DozCount){
			
			String[] split = DozConst.split("_");
			if (Integer.valueOf(split[0]) == doz_ID){
				return Integer.valueOf(split[3]);
			}
		}
		return -1;

	}
	
}
