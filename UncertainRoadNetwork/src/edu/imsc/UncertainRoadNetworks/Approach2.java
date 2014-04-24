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
		//
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		PMF retPMF = new PMF();
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];	
			Util.Log("\n\nfrom: " + from);
			PMF edgePMF = Util.getPMF(from, tod, days);
			if (Util.predictionMethod == PredictionMethod.Interpolated) {
				Double actualTime = Util.GetActualTravelTime(from, (Calendar)startTime.clone());
				if (actualTime != 0.0) 
					edgePMF = edgePMF.Interpolate(actualTime, Util.alpha);
			}
			PMF newPMF = new PMF(retPMF.min + edgePMF.min, retPMF.max + edgePMF.max);
			for (int b = newPMF.min; b <= newPMF.max; ++b) {
				Util.Log("\nb: " + Integer.toString(b));
				Double sum = 0.0;
				for (int h = retPMF.min; h <= retPMF.max && h <= b; ++h) {
					Util.Log("retPMF: " + retPMF.toString());
					Util.Log("edgePMF: " + edgePMF.toString());
					Util.Log("h(retPMF): " + Integer.toString(h));
					Util.Log("b-h(edgePMF): " + Integer.toString(b-h));
					sum += retPMF.Prob(h) * edgePMF.Prob(b - h);
					Util.Log(String.format("sum += %f * %f = %f", retPMF.Prob(h), edgePMF.Prob(b-h), sum));
				}
				newPMF.prob.put(b, sum);
				Util.Log(String.format("newPMF(%d) = %f", b, sum));
			}
			newPMF.Adjust();
			Util.Log(newPMF.toString());
			retPMF = newPMF;
		}
		return retPMF;
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
	
	public static String GetResults(Calendar startTime, Double actualTime, Double score) {
		String retStr = String.format("Start Time: %s, Actual Time: %f, Score: %f",
				Util.oracleDF.format(startTime.getTime()), actualTime, score);
		return retStr;
	}
}
