package edu.imsc.UncertainRoadNetworks;
//Link Travel Times: Discrete
//Time Dependency: Yes
//Link Corelation: None

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

public class Approach4 {
	
	public static PMF GenerateModel(String[] sensorList, String timeOfDay, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException {
		
		PMF retPMF = new PMF();
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			Double currTravelTime = 0.0;
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				currTravelTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
			}
			int min = retPMF.min;
			int max = retPMF.max;
			HashMap<Integer, PMF> edgePMFs = new HashMap<Integer, PMF>();
			for (int i = min; i <= max; i+=PMF.binWidth) {
				Calendar time = Calendar.getInstance();
				time.setTime(Util.timeOfDayDF.parse(timeOfDay));
				time.add(Calendar.MINUTE, i);
				PMF edgePMF = Util.getPMF(from, time, days);
				if (Util.predictionMethod == PredictionMethod.Interpolated) {
					if (currTravelTime != 0.0)
						edgePMF = edgePMF.Interpolate(currTravelTime, (Util.timeHorizon - min - i)/Util.timeHorizon);
				}
				edgePMFs.put(i, edgePMF);
			}
			PMF newPMF = new PMF(retPMF.min + edgePMFs.get(min).min, retPMF.max + edgePMFs.get(max).max);
			for (int b = newPMF.min; b <= newPMF.max; b+=PMF.binWidth) {
				Double sum = 0.0;
				for (int h = retPMF.min; h <= retPMF.max && h <= b; h+=PMF.binWidth) {
					sum += retPMF.Prob(h) * edgePMFs.get(h).Prob(b - h);
				}
				newPMF.prob.put(b, sum);
			}
			retPMF = newPMF;
			//System.out.println(String.format("Min: %d,  Max: %d", retPMF.min, retPMF.max));
		}
		return retPMF;
	}
	
	public static PMF GenerateActual(String[] sensorList,
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = SpeedUp.TimeDependentTravelTime(sensorList, startTimes);
		PMF retPMF = new PMF(travelTimes);
		return retPMF;
	}
	
	public static Double GenerateActual(String[] sensorList,
			Calendar startTime) throws SQLException, ParseException {
		ArrayList<Calendar> temp = new ArrayList<Calendar>();
		temp.add((Calendar)startTime.clone());
		return SpeedUp.TimeDependentTravelTime(sensorList, temp).get(0);
	}
	
	
	public static String GetResults(Calendar startTime, Double actualTime, Double score) {
		String retStr = String.format("Start Time: %s, Actual Time: %f, Score: %f",
				Util.oracleDF.format(startTime.getTime()), actualTime, score);
		return retStr;
	}
}
