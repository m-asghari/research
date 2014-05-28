package edu.imsc.UncertainRoadNetworks;
//Link Travel Times: Continuous
//Time Dependency: None
//Link Corelation: None
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

public class Approach1 {
	
	public static NormalDist GenerateModel(String[] sensorList, String timeOfDay,
			ArrayList<Integer> days, Calendar queryTime) throws SQLException, ParseException{
		Calendar startCal1, startCal2, endCal1, endCal2;
		long p_PassedMillis = 0, l_passedMillis = 0, pl_passedMillis = 0;
		
		NormalDist retDist = new NormalDist(0, 0);
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			startCal1 = Calendar.getInstance();
			if (Util.predictionMethod == PredictionMethod.Filtered) {
				days = Util.FilterDays(days, from, (Calendar)queryTime.clone());
				if (days.size() == 0) return null;
			}
			NormalDist edgeDist = Util.getNormalDist(from, timeOfDay, days);
			if (edgeDist == null) {
				Util.no_model++;
				return null;
			}
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTravelTime = Util.GetActualEdgeTravelTime(from, (Calendar)queryTime.clone());
				if (actualTravelTime == null) {
					Util.no_actual++;
					//actualTravelTime = Util.GetMeanEdgeTravelTime(from, timeOfDay, days);
					//if (actualTravelTime == null) {
						return null;
					//}					
				}
				edgeDist = edgeDist.Interpolate(actualTravelTime, Util.alpha);
			}
			endCal1 = Calendar.getInstance();
			startCal2 = Calendar.getInstance();
			retDist.mean += edgeDist.mean;
			retDist.var += edgeDist.var;
			endCal2 = Calendar.getInstance();
			p_PassedMillis += endCal2.getTimeInMillis() - startCal2.getTimeInMillis();
			l_passedMillis += endCal1.getTimeInMillis() - startCal1.getTimeInMillis();
			pl_passedMillis += endCal2.getTimeInMillis() - startCal1.getTimeInMillis();
		}
		Util.p_passedMillis += p_PassedMillis; Util.p_timeCounter++;
		Util.l_passedMillis += l_passedMillis; Util.l_timeCounter++;
		Util.pl_passedMillis += pl_passedMillis; Util.pl_timeCounter++;
		return retDist;
	}

	public static Double GenerateActual(String[] sensorList, 
			Calendar queryTime) throws SQLException, ParseException {
		return Util.GetActualTravelTime(sensorList, queryTime);
	}
	
	public static String GetResults(Calendar startTime, NormalDist modelDist, Double actualTime, Double score) {
		String retStr = String.format("Start Time: %s, Model Distribution: %s, Actual Time: %f, Score: %f",
				Util.oracleDF.format(startTime.getTime()), modelDist.toString(), actualTime, score);
		return retStr;
	}
}
