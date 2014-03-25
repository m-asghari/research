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
	
	public static NormalDist GenerateModel(String pathNumber, String[] sensorList, String timeOfDay, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException {
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		HashMap<String, ArrayList<Double>> allTravelTimes = new HashMap<String, ArrayList<Double>>();
		
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			ArrayList<Double> travelTimes = Util.getTravelTimes(pathNumber, from, tod, days);
			allTravelTimes.put(from, Util.getTravelTimes(pathNumber, from, tod, days));
		}
		
		NormalDist retDist = new NormalDist(0, 0);
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			ArrayList<Double> travelTimes = allTravelTimes.get(from);
			NormalDist edgeDist = new NormalDist(travelTimes);
			retDist.mean += edgeDist.mean;
			retDist.var += edgeDist.var;
			Double sum = 0.0;
			for (int i = 0; i < s; ++i)
				sum += Util.PearsonCorrCoef(allTravelTimes.get(sensorList[i]), travelTimes);
			for (int i = s+1; i < sensorList.length - 1; ++i)
				sum += Util.PearsonCorrCoef(allTravelTimes.get(sensorList[i]), travelTimes);
			retDist.var += sum;
		}
		return retDist;
	}
	
	public static NormalDist GenerateActual(String pathNumber, String[] sensorList,
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Calendar> endTimes = SpeedUp.TimeDependentTravelTime(pathNumber, sensorList, startTimes);
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		for (int i = 0; i < startTimes.size(); ++i) {
			travelTimes.add(SpeedUp.ToMinutes(startTimes.get(i), endTimes.get(i)));			
		}
		NormalDist retDist = new NormalDist(travelTimes);
		return retDist;
	}

}
