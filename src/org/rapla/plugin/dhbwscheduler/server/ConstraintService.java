package org.rapla.plugin.dhbwscheduler.server;

import java.util.Date;

public class ConstraintService{

	/*Constraint:
	 * 0: DozentID
	 * 1: Constraints
	 * 2: Dates
	 * 3: Status
	 * DOZID_000000000000000000000000...111111111111111111111111_ExceptionDate,ExceptionDate_Status\n
	 * DOZID_000000000000000000000000...111111111111111111111111_ExceptionDate,ExceptionDate_Status
	 */

	public static final int CHANGE_SINGLECONSTRAINT = 1;
	public static final int CHANGE_SINGLEDATES = 2;
	public static final int CHANGE_SINGLESTATUS = 3;

	/**
	 * 
	 * @param constraint
	 * @param doz_ID
	 * @param changevalue
	 * @param value
	 * @return
	 */
	public static String changeDozConstraint(String constraint, String doz_ID, int changevalue,Object value){
		Object[] obj = new Object[1];
		obj[0] = value;
		return changeDozConstraint(constraint, doz_ID, changevalue, obj );
	}
	
	/**
	 * 
	 * @param constraint
	 * @param doz_ID
	 * @param changevalue
	 * @param value
	 * @return
	 */
	public static String changeDozConstraint(String constraint, String doz_ID, int changevalue,Object[] value){
		String result = "";
		
		if (constraint == null){
			return result;
		}
		
//		if(value == null || value[0] == null){
//			//es soll doch keine änderung vorgenommen werden
//			return constraint;
//		}
		
		String[] dozentIds = getDozIDs(constraint);
		String[] dozentConstraints = new String[dozentIds.length];
		
		Date[][] execptions = new Date[dozentIds.length][];
		int[] status = new int[dozentIds.length];
		
		for(int i = 0; i < dozentIds.length ; i++){
			
			Date[] dozentExecption = getExceptionDatesDoz(constraint, dozentIds[i]);			
			int dozstatus = getStatus(constraint,dozentIds[i]);
			dozentConstraints[i] = getDozStringConstraint(constraint, dozentIds[i]);
			
			if (dozentIds[i].equals(doz_ID)){
				try{
					switch(changevalue){
					case CHANGE_SINGLEDATES:
						dozentExecption = (Date[]) value;
						break;
					case CHANGE_SINGLESTATUS:
						dozstatus = (Integer) value[0];
						break;
					case CHANGE_SINGLECONSTRAINT:
						dozentConstraints[i] = (String) value[0];
						break;
					}
				}catch(ClassCastException ce){
					ce.printStackTrace();
					return result;
				}
				
			}
			
			execptions[i] 	= dozentExecption;
			status[i] 		= dozstatus;
		}
		result = buildDozConstraint(dozentIds,dozentConstraints,execptions,status);
		return result;
	}
	
	/**
	 * Initializing the Constraints
	 * @param constraint
	 * @param dozID
	 * @return
	 */
	public static String initDozConstraint(String constraint, String[] dozID){
		
		String newConstraint ="";
		
		if (constraint == null){
			String[] dozentid = new String[dozID.length];
			int[] newStatus = new int[dozID.length];

			for (int x = 0; x < dozID.length; x++){
				dozentid[x] 	= dozID[x];
				newStatus[x] 	= 0;
			}

			newConstraint = ConstraintService.buildDozConstraint(dozentid, new String[dozID.length], new Date[dozID.length][], newStatus);
			
		}else{
			
			//ConstraintService.addorchangeSingleDozConstraint();
			for (int x = 0; x < dozID.length; x++){
				boolean hit = false;
				
				//ist der Dozent schon vorhanden, wird der Constraint beibehalten, bei einem neuen Dozenten wird dieser hinzugefügt. 
				for(int i = 0; i< ConstraintService.getDozIDs(constraint).length ; i++){
					
					String key = ConstraintService.getDozIDs(constraint)[i];
					
					if (key.equals(dozID[x])){
						
						
						newConstraint += buildDozConstraint(key, 
								getDozStringConstraint(constraint, key), 
								getExceptionDatesDoz(constraint, key),								
								getStatus(constraint, key)); 
						
						hit = true;
						break;
					}
				}
				//neuer Dozent
				if(!hit){
					newConstraint += buildDozConstraint(dozID[x], null, null, 0);
				}
				
				if(x < dozID.length-1) {
					newConstraint += "\n";
				}
			}
		}
		
		return newConstraint;
	}
	
	/**
	 * 
	 * @param constraint
	 * @param dozID
	 * @param dozConst
	 * @param exceptDate
	 * @param status
	 * @return
	 */
	public static String addorchangeSingleDozConstraint(String constraint, String dozID, String dozConst, Date[] exceptDate, int status){
		String newConstraint = constraint;
		boolean newdoz = false;
		if (newConstraint == null){
			return buildDozConstraint(dozID,dozConst,exceptDate,status);
		}
		for(String dozkey : ConstraintService.getDozIDs(newConstraint)){
			if(dozkey.equals(dozID)){
				//Dozent gibt es schon da drin muss also nicht neu hinzugefügt werden
				newConstraint = changeDozConstraint(newConstraint,dozID,CHANGE_SINGLEDATES,exceptDate);
				newConstraint = changeDozConstraint(newConstraint,dozID,CHANGE_SINGLECONSTRAINT,dozConst);
				newConstraint = changeDozConstraint(newConstraint,dozID,CHANGE_SINGLESTATUS,status);
				newdoz = false;
				break;
				
			}else{
				newdoz = true;
			}
		}
		if(newdoz){
			newConstraint += "\n" + buildDozConstraint(dozID,dozConst,exceptDate,status);
		}
		
		//newConstraint = changeDozConstraint(newConstraint,dozID,CHANGE_SINGLEDATES,exceptDate);
		//newConstraint = changeDozConstraint(newConstraint,dozID,CHANGE_SINGLECONSTRAINT,dozConst);
		//newConstraint = changeDozConstraint(newConstraint,dozID,CHANGE_SINGLESTATUS,status);
			
		return newConstraint;
	}
	
	/**
	 * 
	 * @param dozID
	 * @param dozConst
	 * @param exceptDate
	 * @param status
	 * @return
	 */
	public static String buildDozConstraint(String dozID, String dozConst, Date[] exceptDate, int status){
		String[] dozIDs = new String[1];
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
	
	/**
	 * 
	 * @param dozIDs
	 * @param dozConsts
	 * @param exceptDates
	 * @param status
	 * @return
	 */
	public static String buildDozConstraint(String[] dozIDs, String[] dozConsts, Date[][] exceptDates, int[] status){
 		String result = "";
		
		for(int i = 0; i<dozIDs.length; i++) {
			result += dozIDs[i] + "_";
			
			if (dozConsts[i] != null) {
				result += dozConsts[i];
			}
			
			result += "_";
			if( exceptDates != null && exceptDates[i] != null && exceptDates[i].length > 0) {
				if ( exceptDates[i][0] != null){

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
			}
			result += "_";
			
			result += status[i];
			
			if(i < dozIDs.length-1) {
				result += "\n";
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param constraint
	 * @return
	 */
	public static String[] getDozIDs(String constraint){
		String[] result = {};
		if (constraint == null || constraint.equals("")) {
			return result;
		}
		String dozCount[] = constraint.split("\n");
		result = new String[dozCount.length];
		
		
		for(int i = 0; i < dozCount.length; i++){
			String[] split = dozCount[i].split("_");
			result[i] = split[0];
		}
		return result;
	}
	
	/**
	 * 
	 * @param constraint
	 * @param dozID
	 * @return
	 */
	public static String getDozStringConstraint (String constraint, String dozID){
		String dozConstraints = "";
		
		if (constraint == null){
			return dozConstraints;
		}
		
		String[] dozCount = constraint.split("\n");
		
		for(String dozConst:dozCount){
			
			String[] split = dozConst.split("_");
			if (split[0].equals(dozID)){
				dozConstraints = split[1];
				break;
			}
		}
	
		return dozConstraints;
	}

	/**
	 * 
	 * @param constraint
	 * @return
	 */
	public static int[] getDozConstraints (String constraint){
		
		//TODO: mehrere Dozenten, einer hat Constraints eingetragen der andere nicht. Was passiert dann?
		
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
	
	/**
	 * 
	 * @param constraint
	 * @param doz_ID
	 * @return
	 */
	public static int[] getDozIntConstraints (String constraint, String doz_ID){
		int [] ergebnis = {};
		
		if (constraint == null){
			return ergebnis;
		}
				
		String dozCount[] = constraint.split("\n");
		
		for(String dozConst:dozCount){
			
			String[] split = dozConst.split("_");
			if (split[0].equals(doz_ID)){
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
	
	/**
	 * 
	 * @param constraint
	 * @return
	 */
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
	
	/**
	 * 
	 * @param constraint
	 * @param doz_ID
	 * @return
	 */
	public static Date[] getExceptionDatesDoz (String constraint,String doz_ID ){
		Date[] ergebnis = {};
		
		if (constraint == null){
			return ergebnis;
		}
		
		String dozCount[] = constraint.split("\n");
		String exceptionDates[] = {};
		
		for(int i = 0; i< dozCount.length;i++){
			String[] split = dozCount[i].split("_");
		
			if (split[0].equals(doz_ID)){
				if(!split[2].split(",").equals("")){
					exceptionDates = split[2].split(",");
				}
				break;
			}
		}

		if (exceptionDates.length!=0){
			ergebnis = new Date[exceptionDates.length];
				
			int counter = 0;
			for (int i = 0; i<exceptionDates.length;i++){
				if (!exceptionDates[i].isEmpty()) {
					ergebnis[counter++] = new Date(Long.parseLong(exceptionDates[i]));
				}
				else {
					ergebnis = new Date[0];
					break;
				}
			}
		}
		return ergebnis;
	}

	/**
	 * 
	 * @param constraint
	 * @return
	 */
	public static int getReservationStatus(String constraint){
		String[] dozIDs = getDozIDs(constraint);
		
		int returnvalue = -1;
		
		boolean uneingeladen = false;
		boolean eingeladen = false;
		boolean erfasst = false;
		;
		
		for(int i = 0 ; i < dozIDs.length ; i++){
						
			switch(getStatus(constraint,dozIDs[i])){
			case 0:
				uneingeladen = true;
				break;
			case 1:
				eingeladen = true;
				break;
			case 2:
				erfasst = true;
				break;
			}
		}
		
		if(eingeladen && erfasst || uneingeladen && erfasst || uneingeladen && eingeladen){
			if(erfasst){
				//Teilwese erfasst
				returnvalue = 4;
			}else{
				//teilweise eingeladen
				returnvalue = 3;
			}
		}else{
			if(uneingeladen)
				returnvalue = 0;
			if(eingeladen)
				returnvalue = 1;
			if(erfasst)
				returnvalue = 2;
		}
		
		return returnvalue;
		
	}
	
	/**
	 * 
	 * @param constraint
	 * @param doz_ID
	 * @return
	 */
	public static int getStatus(String constraint, String doz_ID){
		
		if (constraint == null){
			return -1;
		}
		
		String DozCount[] = constraint.split("\n");
		
		for(String DozConst:DozCount){
			
			String[] split = DozConst.split("_");
			if (split[0].equals(doz_ID)){
				return Integer.valueOf(split[3]);
			}
		}
		return -1;

	}
	
}
