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
	
	public static NormalDist GenerateModel(String pathNumber,
			String[] sensorList, String timeOfDay,
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException{
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		NormalDist retDist = new NormalDist(0, 0);
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			NormalDist edgeDist = Util.getNormalDist(pathNumber, from, tod, days);
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTravelTime = Util.GetActualTravelTime(pathNumber, from, (Calendar)startTime.clone());
				edgeDist = edgeDist.Interpolate(actualTravelTime, Util.alpha);
			}
			retDist.mean += edgeDist.mean;
			retDist.var += edgeDist.var;
		}
		return retDist;
	}

	public static NormalDist GenerateActual(String pathNumber, String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = SpeedUp.TimeInependentTravelTime(pathNumber, sensorList, startTimes);
		NormalDist retDist = new NormalDist(travelTimes);
		return retDist;
	}
	
	public static Double GenerateActual(String pathNumber, String[] sensorList,
			Calendar startTime) throws SQLException, ParseException {
		ArrayList<Calendar> temp = new ArrayList<Calendar>();
		temp.add(startTime);
		return SpeedUp.TimeInependentTravelTime(pathNumber, sensorList, temp).get(0);
	}
}
