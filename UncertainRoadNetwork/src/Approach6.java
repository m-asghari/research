//Link Travel Times: Discrete
//Time Dependency: None
//Link Corelation: Yes
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

// 0 & False -> Normal
// 1 & True -> Congested
public class Approach6 {
	
	public static Pair<PMF, PMF> GenerateModel(String pathNumber, String[] sensorList, 
			String timeOfDay, ArrayList<Integer> days) throws SQLException, ParseException{		
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
			HashMap<Pair<Integer, Integer>, Double> transitionProb = Util.getCongestionChange(pathNumber, prev, from, tod, days);
			PMF edgeCongPMF = Util.getPMF(pathNumber, from, tod, days, true);
			PMF edgeNormPMF = Util.getPMF(pathNumber, from, tod, days, false);
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
			congPMF = newCongPMF;
			PMF newNormPMF = new PMF(prevMin + edgeNormPMF.min, prevMax + edgeNormPMF.max);
			for (int b = newNormPMF.min; b <= newNormPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = prevMin; h <= prevMax; ++h) {
					sum += transitionProb.get(t2f) * congPMF.Prob(h) * edgeNormPMF.Prob(b-h);
					sum += transitionProb.get(f2f) * normPMF.Prob(h) * edgeNormPMF.Prob(b-h);
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
