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
	

	public static PMF GenerateModel(String[] sensorList, String tod, 
			ArrayList<Integer> days, Calendar queryTime) throws SQLException, ParseException, IOException{
		Calendar startCal1, startCal2, endCal1, endCal2;
		long p_PassedMillis = 0, l_passedMillis = 0, pl_passedMillis = 0;
		
		startCal1 = Calendar.getInstance();
		Util.Log("From: " + sensorList[0]);
		PMF congPMF = Util.getPMF(sensorList[0], tod, days, true);
		PMF normPMF = Util.getPMF(sensorList[0], tod, days, false);
		endCal1 = Calendar.getInstance();
		l_passedMillis = endCal1.getTimeInMillis() - startCal1.getTimeInMillis();
		if (congPMF == null && normPMF == null)
			return null;
		if (congPMF == null) congPMF = new PMF(0, 0);
		if (normPMF == null) normPMF = new PMF(0, 0);
		Util.Log("congPMF: " + congPMF.toString());
		Util.Log("normPMF: " + normPMF.toString());
		for (int s = 1; s < sensorList.length - 1; ++s) {
			String prev = sensorList[s-1];
			String from = sensorList[s];
			Util.Log("From: " + from);
			startCal1 = Calendar.getInstance();
			//ArrayList<Double> transitionProb = Util.congChangeProb.get(new Pair<String, String>(prev, from));
			ArrayList<Double> transitionProb = PathData.GetCongTrans(prev, from);
			Util.Log(String.format("Transition Probs from %s to %s: %s", prev, from, transitionProb.toString()));
			if (Util.predictionMethod == PredictionMethod.Filtered) {
				days = Util.FilterDays(days, from, (Calendar)queryTime.clone());
				if (days.size() == 0) return null;
			}
			PMF edgeCongPMF = Util.getPMF(from, tod, days, true);
			PMF edgeNormPMF = Util.getPMF(from, tod, days, false);
			if (edgeCongPMF == null && edgeNormPMF == null)
				return null;
			if (edgeCongPMF == null) 
				edgeCongPMF = new PMF(0, 0);
			if (edgeNormPMF == null)
				edgeNormPMF = new PMF(0, 0);
			Util.Log("edgeCongPMF: " + edgeCongPMF.toString());
			Util.Log("edgeNormPMF: " + edgeNormPMF.toString());
			
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTime = Util.GetActualEdgeTravelTime(from, (Calendar)queryTime.clone());
				if (actualTime == null) {
					return null;
				}
				edgeCongPMF = edgeCongPMF.Interpolate(actualTime, Util.alpha);
				edgeNormPMF = edgeNormPMF.Interpolate(actualTime, Util.alpha);
			}
			endCal1 = Calendar.getInstance();
			int prevMin = Math.min(congPMF.min, normPMF.min);
			int prevMax = Math.max(congPMF.max, normPMF.max);
			Util.Log(String.format("prevMin: %d, prevMax: %d", prevMin, prevMax));
			PMF newCongPMF = new PMF(prevMin + edgeCongPMF.min, prevMax + edgeCongPMF.max);
			startCal2 = Calendar.getInstance();
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
			Util.Log("newCongPMF: " + newCongPMF.toString());
			Util.Log("newNormPMF: " + newNormPMF.toString());
			congPMF = newCongPMF;
			normPMF = newNormPMF;
			Pair<Double, Double> probs = PathData.GetLinkCongestion(from);
			Double normProb = probs.getFirst(), congProb = probs.getSecond();
			Util.Log(String.format("normProb: %f, congProb: %f", normProb, congProb));
			int min = Math.min(normPMF.min, congPMF.min);
			int max = Math.max(normPMF.max, congPMF.max);
			PMF newPMF = new PMF(min, max);
			for (int i = min; i <= max; i+=PMF.binWidth) {
				newPMF.prob.put(i, normProb*normPMF.Prob(i) + congProb*congPMF.Prob(i));
			}
			newPMF.Adjust();
			Util.Log("newPMF: " + newPMF.toString());
			endCal2 = Calendar.getInstance();
			p_PassedMillis += endCal2.getTimeInMillis() - startCal2.getTimeInMillis();
			l_passedMillis += endCal1.getTimeInMillis() - startCal1.getTimeInMillis();
			pl_passedMillis += endCal2.getTimeInMillis() - startCal1.getTimeInMillis();
		}
		//Rewrinte using PathData
		String lastEdge = sensorList[sensorList.length - 2];
		Pair<Double, Double> probs = PathData.GetLinkCongestion(lastEdge);
		Double normProb = probs.getFirst(), congProb = probs.getSecond();
		int min = Math.min(normPMF.min, congPMF.min);
		int max = Math.max(normPMF.max, congPMF.max);
		PMF retPMF = new PMF(min, max);
		for (int i = min; i <= max; i+=PMF.binWidth) {
			retPMF.prob.put(i, normProb*normPMF.Prob(i) + congProb*congPMF.Prob(i));
		}
		retPMF.Adjust();
		Util.p_passedMillis += p_PassedMillis; Util.p_timeCounter++;
		Util.l_passedMillis += l_passedMillis; Util.l_timeCounter++;
		Util.pl_passedMillis += pl_passedMillis; Util.pl_timeCounter++;
		retPMF.ComputeMean();
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
