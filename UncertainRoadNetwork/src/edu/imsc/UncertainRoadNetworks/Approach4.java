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
	
	public static PMF GenerateModel(String[] sensorList, String tod, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException {
		
		PMF retPMF = new PMF();
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			Double currTravelTime = 0.0;
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				currTravelTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
				if (currTravelTime == null)
					return null;
			}
			int min = retPMF.min;
			int max = retPMF.max;
			HashMap<Integer, PMF> edgePMFs = new HashMap<Integer, PMF>();
			for (int i = min; i <= max; i+=PMF.binWidth) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(Util.timeOfDayDF.parse(tod));
				cal.add(Calendar.MINUTE, i);
				String time = Util.timeOfDayDF.format(cal.getTime());
				PMF edgePMF = Util.getPMF(from, time, days, null);
				if (Util.predictionMethod == PredictionMethod.Interpolated) {
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
			newPMF.Adjust();
			retPMF = newPMF;
		}
		return retPMF;
	}
	
	public static Double GenerateActual(String[] sensorList,
			Calendar startTime) throws SQLException, ParseException {
		return Util.GetActualTravelTime(sensorList, startTime);
	}
	
	
	public static String GetResults(Calendar startTime, Double actualTime, Double score) {
		String retStr = String.format("Start Time: %s, Actual Time: %f, Score: %f",
				Util.oracleDF.format(startTime.getTime()), actualTime, score);
		return retStr;
	}
}
