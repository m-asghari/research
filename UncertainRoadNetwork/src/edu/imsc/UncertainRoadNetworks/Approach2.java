package edu.imsc.UncertainRoadNetworks;
//Link Travel Times: Discrete
//Time Dependency: None
//Link Corelation: None
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

public class Approach2 {
	
	public static PMF GenerateModel(String[] sensorList, String timeOfDay, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException{
		
		PMF retPMF = new PMF();
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			PMF edgePMF = Util.getPMF(from, timeOfDay, days, null);
			if (edgePMF == null)
				return null;
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
				if (actualTime == null)
					return null;
				edgePMF = edgePMF.Interpolate(actualTime, Util.alpha);
			}
			PMF newPMF = new PMF(retPMF.min + edgePMF.min, retPMF.max + edgePMF.max);
			for (int b = newPMF.min; b <= newPMF.max; b+=PMF.binWidth) {
				Double sum = 0.0;
				for (int h = retPMF.min; h <= retPMF.max && h <= b; h+=PMF.binWidth) {
					sum += retPMF.Prob(h) * edgePMF.Prob(b - h);
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
