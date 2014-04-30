package edu.imsc.UncertainRoadNetworks;
//Link Travel Times: Discrete
//Time Dependency: Yes
//Link Corelation: Yes
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

// 0 & False -> Normal
// 1 & True -> Congested
public class Approach7 {
	
	public static PMF GenerateModel(String[] sensorList, String timeOfDay, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException{		
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		PMF congPMF = Util.getPMF(sensorList[0], tod, days, true);
		PMF normPMF = Util.getPMF(sensorList[0], tod, days, false);
		for (int s = 1; s < sensorList.length - 1; ++s) {
			String prev = sensorList[s-1];
			String from = sensorList[s];
			Double currTravelTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
			int prevMin = Math.min(congPMF.min, normPMF.min);
			int prevMax = Math.max(congPMF.max, normPMF.max);
			HashMap<Integer, PMF> edgeNormPMFs = new HashMap<Integer, PMF>();
			HashMap<Integer, PMF> edgeCongPMFs = new HashMap<Integer, PMF>();
			ArrayList<Double> transitionProbs = Util.congChangeProb.get(new Pair<String, String>(prev, from));
			for (int i = prevMin; i <= prevMax; ++i) {
				Calendar time = Calendar.getInstance();
				time.setTime(Util.timeOfDayDF.parse(timeOfDay));
				time.add(Calendar.MINUTE, i);
				PMF edgeNormPMF = Util.getPMF(from, time, days, false);
				PMF edgeCongPMF = Util.getPMF(from, time, days, true);
				if (Util.predictionMethod == PredictionMethod.Interpolated) {
					if (currTravelTime != 0.0) {
						edgeNormPMF = edgeNormPMF.Interpolate(currTravelTime, (Util.timeHorizon - prevMin - i)/Util.timeHorizon);
						edgeCongPMF = edgeCongPMF.Interpolate(currTravelTime, (Util.timeHorizon - prevMin - i)/Util.timeHorizon);
					}
				}
				edgeNormPMFs.put(i, Util.getPMF(from, time, days, false));
				edgeCongPMFs.put(i, Util.getPMF(from, time, days, true));
			}
			PMF newCongPMF = new PMF(prevMin + edgeCongPMFs.get(prevMin).min, prevMax + edgeCongPMFs.get(prevMax).max);
			for (int b = newCongPMF.min; b <= newCongPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) {
					sum += transitionProbs.get(Util.t2t) * congPMF.Prob(h) * edgeCongPMFs.get(h).Prob(b-h);
					sum += transitionProbs.get(Util.f2t) * normPMF.Prob(h) * edgeCongPMFs.get(h).Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			newCongPMF.Adjust();
			congPMF = newCongPMF;
			PMF newNormPMF = new PMF(prevMin + edgeNormPMFs.get(prevMin).min, prevMax + edgeNormPMFs.get(prevMax).max);
			for (int b = newNormPMF.min; b <= newNormPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) {
					sum += transitionProbs.get(Util.t2f) * congPMF.Prob(h) * edgeNormPMFs.get(h).Prob(b-h);
					sum += transitionProbs.get(Util.f2f) * normPMF.Prob(h) * edgeNormPMFs.get(h).Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			newNormPMF.Adjust();
			normPMF = newNormPMF;
		}
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
	
	public static PMF GenerateActual(String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = SpeedUp.TimeInependentTravelTime(sensorList, startTimes);
		PMF retPMF = new PMF(travelTimes);
		return retPMF;
	}
}
