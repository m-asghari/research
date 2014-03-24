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
import java.util.HashMap;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

// 0 & False -> Normal
// 1 & True -> Congested
public class Approach6 {
	
	public static Pair<PMF, PMF> GenerateModel(String pathNumber, String[] sensorList, 
			String timeOfDay, ArrayList<Integer> days,
			Calendar startTime) throws SQLException, ParseException, IOException{		
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		FileWriter fw = new FileWriter("logs.txt");
		BufferedWriter bw = new BufferedWriter(fw);
		
		Pair<Integer, Integer> f2f = new Pair<Integer, Integer>(0, 0);
		Pair<Integer, Integer> f2t = new Pair<Integer, Integer>(0, 1);
		Pair<Integer, Integer> t2f = new Pair<Integer, Integer>(1, 0);
		Pair<Integer, Integer> t2t = new Pair<Integer, Integer>(1, 1);		
		
		//bw.write(String.format("%s\n", sensorList[0]));
		PMF congPMF = Util.getPMF(pathNumber, sensorList[0], tod, days, true);
		//bw.write(String.format("congPMF: %s", congPMF.toString()));
		PMF normPMF = Util.getPMF(pathNumber, sensorList[0], tod, days, false);
		//bw.write(String.format("normPMF: %s", normPMF.toString()));
		for (int s = 1; s < sensorList.length - 1; ++s) {
			String prev = sensorList[s-1];
			String from = sensorList[s];
			HashMap<Pair<Integer, Integer>, Double> transitionProb = Util.getCongestionChange(pathNumber, prev, from, tod, days);
			PMF edgeCongPMF = Util.getPMF(pathNumber, from, tod, days, true);
			PMF edgeNormPMF = Util.getPMF(pathNumber, from, tod, days, false);
			// TODO interpolate both edges if necessary ...
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTime = Util.GetActualTravelTime(pathNumber, from, (Calendar)startTime.clone());
				edgeCongPMF = edgeCongPMF.Interpolate(actualTime, Util.alpha);
				edgeNormPMF = edgeNormPMF.Interpolate(actualTime, Util.alpha);
			}
			//bw.write(String.format("%s\n", sensorList[s]));
			//bw.write(String.format("Transitions: %s\n", transitionProb.toString()));
			//bw.write(String.format("congPMF: %s", congPMF.toString()));
			//bw.write(String.format("normPMF: %s", normPMF.toString()));
			//bw.write(String.format("edgeCongPMF: %s", edgeCongPMF.toString()));
			//bw.write(String.format("edgeNormPMF: %s", edgeNormPMF.toString()));
			int prevMin = Math.min(congPMF.min, normPMF.min);
			int prevMax = Math.max(congPMF.max, normPMF.max);
			PMF newCongPMF = new PMF(prevMin + edgeCongPMF.min, prevMax + edgeCongPMF.max);
			for (int b = newCongPMF.min; b <= newCongPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) {
					sum += transitionProb.get(t2t) * congPMF.Prob(h) * edgeCongPMF.Prob(b-h);
					sum += transitionProb.get(f2t) * normPMF.Prob(h) * edgeCongPMF.Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			PMF newNormPMF = new PMF(prevMin + edgeNormPMF.min, prevMax + edgeNormPMF.max);
			for (int b = newNormPMF.min; b <= newNormPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) {
					sum += transitionProb.get(t2f) * congPMF.Prob(h) * edgeNormPMF.Prob(b-h);
					sum += transitionProb.get(f2f) * normPMF.Prob(h) * edgeNormPMF.Prob(b-h);
				}
				newCongPMF.prob.put(b, sum);
			}
			congPMF = newCongPMF;
			normPMF = newNormPMF;
			//bw.write(String.format("newCongPMF: %s", newCongPMF.toString()));
			//bw.write(String.format("newNormPMF: %s\n", newNormPMF.toString()));
			
		}
		bw.close();
		fw.close();
		return new Pair<PMF, PMF>(normPMF, congPMF);
	}
	
	public static PMF GenerateActual(String pathNumber, String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = SpeedUp.TimeInependentTravelTime(pathNumber, sensorList, startTimes);
		PMF retPMF = new PMF(travelTimes);
		return retPMF;
	}
}
