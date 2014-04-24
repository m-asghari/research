package edu.imsc.UncertainRoadNetworks;
//Link Travel Times: Discrete
//Time Dependency: None
//Link Corelation: Yes
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

// 0 & False -> Normal
// 1 & True -> Congested
public class Approach6 {
	
	public static Pair<PMF, PMF> GenerateModel(String[] sensorList, 
			String timeOfDay, ArrayList<Integer> days,
			Calendar startTime) throws SQLException, ParseException, IOException{		
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		FileWriter fw = new FileWriter("logs.txt");
		BufferedWriter bw = new BufferedWriter(fw);	
		
		PMF congPMF = Util.getPMF(sensorList[0], tod, days, true);
		PMF normPMF = Util.getPMF(sensorList[0], tod, days, false);
		for (int s = 1; s < sensorList.length - 1; ++s) {
			String prev = sensorList[s-1];
			String from = sensorList[s];
			ArrayList<Double> transitionProb = Util.congChangeProb.get(new Pair<String, String>(prev, from));
			PMF edgeCongPMF = Util.getPMF(from, tod, days, true);
			PMF edgeNormPMF = Util.getPMF(from, tod, days, false);
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double currTravelTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
				if (currTravelTime != 0.0) {
					edgeCongPMF = edgeCongPMF.Interpolate(currTravelTime, Util.alpha);
					edgeNormPMF = edgeNormPMF.Interpolate(currTravelTime, Util.alpha);
				}				
			}
			int prevMin = Math.min(congPMF.min, normPMF.min);
			int prevMax = Math.max(congPMF.max, normPMF.max);
			PMF newCongPMF = new PMF(prevMin + edgeCongPMF.min, prevMax + edgeCongPMF.max);
			for (int b = newCongPMF.min; b <= newCongPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) { 
					sum += transitionProb.get(Util.t2t) * congPMF.Prob(h) * edgeCongPMF.Prob(b-h);
					sum += transitionProb.get(Util.f2t) * normPMF.Prob(h) * edgeCongPMF.Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			PMF newNormPMF = new PMF(prevMin + edgeNormPMF.min, prevMax + edgeNormPMF.max);
			for (int b = newNormPMF.min; b <= newNormPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) {
					sum += transitionProb.get(Util.t2f) * congPMF.Prob(h) * edgeNormPMF.Prob(b-h);
					sum += transitionProb.get(Util.f2f) * normPMF.Prob(h) * edgeNormPMF.Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			congPMF = newCongPMF;
			normPMF = newNormPMF;			
		}
		bw.close();
		fw.close();
		return new Pair<PMF, PMF>(normPMF, congPMF);
	}
	
	public static PMF GenerateActual(String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = SpeedUp.TimeInependentTravelTime(sensorList, startTimes);
		PMF retPMF = new PMF(travelTimes);
		return retPMF;
	}
	
	public static Double GenerateActual(String[] sensorList,
			Calendar startTime) throws SQLException, ParseException {
		ArrayList<Calendar> temp = new ArrayList<Calendar>();
		temp.add(startTime);
		return SpeedUp.TimeInependentTravelTime(sensorList, temp).get(0);
	}
}
