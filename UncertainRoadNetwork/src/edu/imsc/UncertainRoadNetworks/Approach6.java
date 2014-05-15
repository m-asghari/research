package edu.imsc.UncertainRoadNetworks;
//Link Travel Times: Discrete
//Time Dependency: None
//Link Corelation: Yes
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

// 0 & False -> Normal
// 1 & True -> Congested
public class Approach6 {
	
	public static PMF GenerateModels(String[] sensorList, String tod, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException, IOException{		
		PMF congPMF = Util.getPMF(sensorList[0], tod, days, true);
		PMF normPMF = Util.getPMF(sensorList[0], tod, days, false);
		for (int s = 1; s < sensorList.length - 1; ++s) {
			String prev = sensorList[s-1];
			String from = sensorList[s];
			ArrayList<Double> transitionProb = Util.congChangeProb.get(new Pair<String, String>(prev, from));
			PMF edgeCongPMF = Util.getPMF(from, tod, days, true);
			PMF edgeNormPMF = Util.getPMF(from, tod, days, false);
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
				if (actualTime == null) {
					return null;
				}
				edgeCongPMF = edgeCongPMF.Interpolate(actualTime, Util.alpha);
				edgeNormPMF = edgeNormPMF.Interpolate(actualTime, Util.alpha);
			}
			int prevMin = Math.min(congPMF.min, normPMF.min);
			int prevMax = Math.max(congPMF.max, normPMF.max);
			PMF newCongPMF = new PMF(prevMin + edgeCongPMF.min, prevMax + edgeCongPMF.max);
			for (int b = newCongPMF.min; b <= newCongPMF.max; b+=PMF.binWidth) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax && h <= b; h+=PMF.binWidth) { 
					sum += transitionProb.get(Util.t2t) * congPMF.Prob(h) * edgeCongPMF.Prob(b-h);
					sum += transitionProb.get(Util.f2t) * normPMF.Prob(h) * edgeCongPMF.Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			PMF newNormPMF = new PMF(prevMin + edgeNormPMF.min, prevMax + edgeNormPMF.max);
			for (int b = newNormPMF.min; b <= newNormPMF.max; b+=PMF.binWidth) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax && h <= b; h+=PMF.binWidth) {
					sum += transitionProb.get(Util.t2f) * congPMF.Prob(h) * edgeNormPMF.Prob(b-h);
					sum += transitionProb.get(Util.f2f) * normPMF.Prob(h) * edgeNormPMF.Prob(b-h);
				}
				newNormPMF.prob.put(b, sum);
			}
			newCongPMF.Adjust();
			newNormPMF.Adjust();
			congPMF = newCongPMF;
			normPMF = newNormPMF;			
		}
		//Rewrinte using PathData
		String lastEdge = sensorList[sensorList.length - 2];
		Pair<Double, Double> probs = GenerateLinkCorrelations.GetLinkCongestion(lastEdge);
		Double normProb = probs.getFirst(), congProb = probs.getSecond();
		int min = Math.min(normPMF.min, congPMF.min);
		int max = Math.max(normPMF.max, congPMF.max);
		PMF retPMF = new PMF(min, max);
		for (int i = min; i <= max; i+=PMF.binWidth) {
			retPMF.prob.put(i, normProb*normPMF.Prob(i) + congProb*congPMF.Prob(i));
		}
		retPMF.Adjust();
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
