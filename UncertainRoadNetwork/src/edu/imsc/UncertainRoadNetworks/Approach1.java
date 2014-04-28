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
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		NormalDist retDist = new NormalDist(0, 0);
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			Util.Log("From: " + from);
			NormalDist edgeDist = Util.getNormalDist(from, tod, days);
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTravelTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
				if (actualTravelTime != 0.0)
					edgeDist = edgeDist.Interpolate(actualTravelTime, Util.alpha);
			}
			retDist.mean += edgeDist.mean;
			retDist.var += edgeDist.var;
			Util.Log("Edge Dist: " + edgeDist.toString());
			Util.Log("Ret Dist: " + retDist.toString());
		}
		return retDist;
	}

	public static NormalDist GenerateActual(String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = SpeedUp.TimeInependentTravelTime(sensorList, startTimes);
		NormalDist retDist = new NormalDist(travelTimes);
		return retDist;
	}
	
	public static Double GenerateActual(String[] sensorList,
			Calendar startTime) throws SQLException, ParseException {
		ArrayList<Calendar> temp = new ArrayList<Calendar>();
		temp.add(startTime);
		return SpeedUp.TimeInependentTravelTime(sensorList, temp).get(0);
	}
	
	public static String GetResults(Calendar startTime, NormalDist modelDist, Double actualTime, Double score) {
		String retStr = String.format("Start Time: %s, Model Distribution: %s, Actual Time: %f, Score: %f",
				Util.oracleDF.format(startTime.getTime()), modelDist.toString(), actualTime, score);
		return retStr;
	}
}
