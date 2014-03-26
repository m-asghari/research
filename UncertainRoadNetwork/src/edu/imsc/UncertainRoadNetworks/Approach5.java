package edu.imsc.UncertainRoadNetworks;
//Link Travel Times: Continuous
//Time Dependency: None
//Link Corelation: Yes

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

public class Approach5 {
	
	public static NormalDist GenerateModel(String[] sensorList, String timeOfDay, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException {
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		HashMap<String, ArrayList<Double>> allTravelTimes = new HashMap<String, ArrayList<Double>>();
		 
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			//ArrayList<Double> travelTimes = Util.getTravelTimes(pathNumber, from, tod, days);
			allTravelTimes.put(from, Util.getTravelTimes(from, tod, days));
		}
		
		NormalDist retDist = new NormalDist(0, 0);
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			ArrayList<Double> travelTimes = allTravelTimes.get(from);
			NormalDist edgeDist = new NormalDist(travelTimes);
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTravelTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
				if (actualTravelTime != 0.0)
					edgeDist = edgeDist.Interpolate(actualTravelTime, Util.alpha);
			}
			retDist.mean += edgeDist.mean;
			retDist.var += edgeDist.var;
			Double sum = 0.0;
			for (int i = 0; i < s; ++i)
				sum += Util.pearsonCorrCoef.get(new Pair<String, String>(sensorList[s], sensorList[i]));
			for (int i = s+1; i < sensorList.length - 1; ++i)
				sum += Util.pearsonCorrCoef.get(new Pair<String, String>(sensorList[s], sensorList[i]));
			retDist.var += sum;
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

}
