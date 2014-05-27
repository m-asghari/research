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
	
	public static PMF GenerateModel(String[] sensorList, String tod, 
			ArrayList<Integer> days, Calendar startTime) throws SQLException, ParseException{		
		Calendar startCal1, startCal2, endCal1, endCal2;
		long p_PassedMillis = 0, l_passedMillis = 0, pl_passedMillis = 0;
		
		startCal1 = Calendar.getInstance();
		PMF congPMF = Util.getPMF(sensorList[0], tod, days, true);
		PMF normPMF = Util.getPMF(sensorList[0], tod, days, false);
		endCal1 = Calendar.getInstance();
		l_passedMillis = endCal1.getTimeInMillis() - startCal1.getTimeInMillis();
		if (congPMF == null && normPMF == null)
			return null;
		if (congPMF == null) congPMF = new PMF(0, 0);
		if (normPMF == null) normPMF = new PMF(0, 0);
		for (int s = 1; s < sensorList.length - 1; ++s) {
			String prev = sensorList[s-1];
			String from = sensorList[s];
			startCal1 = Calendar.getInstance();
			int prevMin = Math.min(congPMF.min, normPMF.min);
			int prevMax = Math.max(congPMF.max, normPMF.max);
			HashMap<Integer, PMF> edgeNormPMFs = new HashMap<Integer, PMF>();
			HashMap<Integer, PMF> edgeCongPMFs = new HashMap<Integer, PMF>();
			ArrayList<Double> transitionProbs = PathData.GetCongTrans(prev, from);
			String prevTime = "";
			PMF prevCongPMF = null, prevNormPMF = null;
			for (int i = prevMin; i <= prevMax; ++i) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(Util.timeOfDayDF.parse(tod));
				cal.add(Calendar.SECOND, i);
				String time = Util.timeOfDayDF.format(Util.RoundTimeDown((Calendar)cal.clone()).getTime());
				PMF edgeNormPMF = null, edgeCongPMF = null;
				if (time.equals(prevTime)) {
					edgeNormPMF = (PMF) prevNormPMF.clone();
					edgeCongPMF = (PMF) prevCongPMF.clone();
				}
				else {
					edgeNormPMF = Util.getPMF(from, time, days, false);
					edgeCongPMF = Util.getPMF(from, time, days, true);
					if (edgeCongPMF == null && edgeNormPMF == null)
						return null;
					if (edgeCongPMF == null) edgeCongPMF = new PMF(0, 0);
					if (edgeNormPMF == null) edgeNormPMF = new PMF(0, 0);
					prevCongPMF = (PMF) edgeCongPMF.clone();
					prevNormPMF = (PMF) edgeNormPMF.clone();
					prevTime = time;
				}
				if (Util.predictionMethod == PredictionMethod.Interpolated) {
					Double currTravelTime = Util.GetActualEdgeTravelTime(from, (Calendar)startTime.clone());
					if (currTravelTime == null) {
						return null;
					}
					edgeNormPMF = edgeNormPMF.Interpolate(currTravelTime, Util.alpha);
					edgeCongPMF = edgeCongPMF.Interpolate(currTravelTime, Util.alpha);
				}
				edgeNormPMFs.put(i, edgeNormPMF);
				edgeCongPMFs.put(i, edgeCongPMF);
			}
			endCal1 = Calendar.getInstance();
			PMF newCongPMF = new PMF(prevMin + edgeCongPMFs.get(prevMin).min, prevMax + edgeCongPMFs.get(prevMax).max);
			startCal2 = Calendar.getInstance();
			for (int b = newCongPMF.min; b <= newCongPMF.max; b += PMF.binWidth) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; h += PMF.binWidth) {
					sum += transitionProbs.get(Util.t2t) * congPMF.Prob(h) * edgeCongPMFs.get(h).Prob(b-h);
					sum += transitionProbs.get(Util.f2t) * normPMF.Prob(h) * edgeCongPMFs.get(h).Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			newCongPMF.Adjust();
			congPMF = newCongPMF;
			PMF newNormPMF = new PMF(prevMin + edgeNormPMFs.get(prevMin).min, prevMax + edgeNormPMFs.get(prevMax).max);
			for (int b = newNormPMF.min; b <= newNormPMF.max; b += PMF.binWidth) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; h += PMF.binWidth) {
					sum += transitionProbs.get(Util.t2f) * congPMF.Prob(h) * edgeNormPMFs.get(h).Prob(b-h);
					sum += transitionProbs.get(Util.f2f) * normPMF.Prob(h) * edgeNormPMFs.get(h).Prob(b-h);
				}
				newNormPMF.prob.put(b, sum);
			}
			newNormPMF.Adjust();
			normPMF = newNormPMF;
			endCal2 = Calendar.getInstance();
			p_PassedMillis += endCal2.getTimeInMillis() - startCal2.getTimeInMillis();
			l_passedMillis += endCal1.getTimeInMillis() - startCal1.getTimeInMillis();
			pl_passedMillis += endCal2.getTimeInMillis() - startCal1.getTimeInMillis();
		}
		//Rewrite using PathData
		String lastEdge = sensorList[sensorList.length - 2];
		Pair<Double, Double> probs = PathData.GetLinkCongestion(lastEdge);
		Double normProb = probs.getSecond(), congProb = probs.getFirst();
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
}
