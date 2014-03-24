//Link Travel Times: Discrete
//Time Dependency: None
//Link Corelation: None
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class Approach2 {
	
	public static PMF GenerateModel(String pathNumber, String[] sensorList, 
			String timeOfDay, ArrayList<Integer> days) throws SQLException, ParseException{
		
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		PMF retPMF = new PMF();
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];	
			PMF edgePMF = Util.getPMF(pathNumber, from, tod, days);
			PMF newPMF = new PMF(retPMF.min + edgePMF.min, retPMF.max + edgePMF.max);
			for (int b = newPMF.min; b <= newPMF.max; ++b) {
				Double sum = 0.0;
				for (int h = retPMF.min; h <= retPMF.max; ++h) {
					sum += retPMF.Prob(h) * edgePMF.Prob(b - h);
				}
				newPMF.prob.put(b, sum);
			}
			retPMF = newPMF;
		}
		return retPMF;
	}
	
	//Checked Wed, Compatible with SpeedUp.TimeIndependentTravelTime
	public static PMF GenerateActual(String pathNumber, String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = SpeedUp.TimeInependentTravelTime(pathNumber, sensorList, startTimes);
		PMF retPMF = new PMF(travelTimes);
		return retPMF;
	}
}