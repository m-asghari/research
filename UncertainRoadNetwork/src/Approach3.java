//Link Travel Times: Continuous
//Time Dependency: Yes
//Link Corelation: None

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class Approach3 {
	
	public static NormalDist GenerateModel(String pathNumber, String[] sensorList,
			String timeOfDay, ArrayList<Integer> days) throws SQLException, ParseException {
		// TODO Auto-generated method stub		
		return null;
	}
	
	public static NormalDist GenerateActual(String pathNumber, String[] sensorList,
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Calendar> endTimes = SpeedUp.TimeDependentTravelTime(pathNumber, sensorList, startTimes);
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		for (int i = 0; i < startTimes.size(); ++i) {
			travelTimes.add(SpeedUp.ToMinutes(startTimes.get(i), endTimes.get(i)));			
		}
		NormalDist retDist = new NormalDist(travelTimes);
		return retDist;
	}	
}
