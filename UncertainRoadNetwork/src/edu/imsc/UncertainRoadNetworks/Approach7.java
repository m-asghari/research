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
	
	public static Pair<PMF, PMF> GenerateModel(String pathNumber, String[] sensorList, String timeOfDay, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException{		
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		Pair<Integer, Integer> f2f = new Pair<Integer, Integer>(0, 0);
		Pair<Integer, Integer> f2t = new Pair<Integer, Integer>(0, 1);
		Pair<Integer, Integer> t2f = new Pair<Integer, Integer>(1, 0);
		Pair<Integer, Integer> t2t = new Pair<Integer, Integer>(1, 1);		
		
		PMF congPMF = Util.getPMF(pathNumber, sensorList[0], tod, days, true);
		PMF normPMF = Util.getPMF(pathNumber, sensorList[0], tod, days, false);
		for (int s = 1; s < sensorList.length - 1; ++s) {
			String prev = sensorList[s-1];
			String from = sensorList[s];
			int prevMin = Math.min(congPMF.min, normPMF.min);
			int prevMax = Math.max(congPMF.max, normPMF.max);
			HashMap<Integer, PMF> edgeNormPMFs = new HashMap<Integer, PMF>();
			HashMap<Integer, PMF> edgeCongPMFs = new HashMap<Integer, PMF>();
			HashMap<Integer, HashMap<Pair<Integer, Integer>, Double> > transitionProbs = new HashMap<Integer, HashMap<Pair<Integer,Integer>,Double>>();
			for (int i = prevMin; i <= prevMax; ++i) {
				Calendar time = Calendar.getInstance();
				time.setTime(Util.timeOfDayDF.parse(timeOfDay));
				time.add(Calendar.MINUTE, i);
				Calendar currStartTime = Calendar.getInstance();
				currStartTime.setTime(startTime.getTime());
				currStartTime.add(Calendar.MINUTE, i);
				PMF edgeNormPMF = Util.getPMF(pathNumber, from, time, days, false);
				PMF edgeCongPMF = Util.getPMF(pathNumber, from, time, days, true);
				if (Util.predictionMethod == PredictionMethod.Interpolated) {
					Double actualTime = Util.GetActualTravelTime(pathNumber, from, currStartTime);
					edgeNormPMF = edgeNormPMF.Interpolate(actualTime, Util.alpha);
					edgeCongPMF = edgeCongPMF.Interpolate(actualTime, Util.alpha);
				}
				edgeNormPMFs.put(i, Util.getPMF(pathNumber, from, time, days, false));
				edgeCongPMFs.put(i, Util.getPMF(pathNumber, from, time, days, true));
				transitionProbs.put(i, Util.getCongestionChange(pathNumber, prev, from, time, days));
			}
			PMF newCongPMF = new PMF(prevMin + edgeCongPMFs.get(prevMin).min, prevMax + edgeCongPMFs.get(prevMax).max);
			for (int b = newCongPMF.min; b <= newCongPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) {
					sum += transitionProbs.get(h).get(t2t) * congPMF.Prob(h) * edgeCongPMFs.get(h).Prob(b-h);
					sum += transitionProbs.get(h).get(f2t) * normPMF.Prob(h) * edgeCongPMFs.get(h).Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			congPMF = newCongPMF;
			PMF newNormPMF = new PMF(prevMin + edgeNormPMFs.get(prevMin).min, prevMax + edgeNormPMFs.get(prevMax).max);
			for (int b = newNormPMF.min; b <= newNormPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) {
					sum += transitionProbs.get(h).get(t2f) * congPMF.Prob(h) * edgeNormPMFs.get(h).Prob(b-h);
					sum += transitionProbs.get(h).get(f2f) * normPMF.Prob(h) * edgeNormPMFs.get(h).Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			normPMF = newNormPMF;
		}
		return new Pair<PMF, PMF>(normPMF, congPMF);
	}
	
	public static PMF GenerateActual(String pathNumber, String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = SpeedUp.TimeInependentTravelTime(pathNumber, sensorList, startTimes);
		PMF retPMF = new PMF(travelTimes);
		return retPMF;
	}
}
