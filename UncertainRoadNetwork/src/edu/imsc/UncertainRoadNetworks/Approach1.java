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
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException{
		
		NormalDist retDist = new NormalDist(0, 0);
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			NormalDist edgeDist = Util.getNormalDist(from, timeOfDay, days);
			if (edgeDist == null)
				return null;
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTravelTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
				if (actualTravelTime == null)
					return null;
				edgeDist = edgeDist.Interpolate(actualTravelTime, Util.alpha);
			}
			retDist.mean += edgeDist.mean;
			retDist.var += edgeDist.var;
		}
		return retDist;
	}

	public static Double GenerateActual(String[] sensorList,
			Calendar startTime) throws SQLException, ParseException {
		return Util.GetActualTravelTime(sensorList, startTime);
	}
	
	public static String GetResults(Calendar startTime, NormalDist modelDist, Double actualTime, Double score) {
		String retStr = String.format("Start Time: %s, Model Distribution: %s, Actual Time: %f, Score: %f",
				Util.oracleDF.format(startTime.getTime()), modelDist.toString(), actualTime, score);
		return retStr;
	}
}
