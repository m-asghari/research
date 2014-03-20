//Link Travel Times: Continuous
//Time Dependency: None
//Link Corelation: None
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class Approach1 {
	
	public static NormalDist GenerateModel(String pathNumber, String[] sensorList, 
			String timeOfDay, ArrayList<Integer> days, ArrayList<Calendar> startTimes) throws SQLException, ParseException{
		Calendar tod = Calendar.getInstance();
		tod.setTime(Util.timeOfDayDF.parse(timeOfDay));
		
		
		NormalDist retDist = new NormalDist(0, 0);
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			String to = sensorList[s+1];
			NormalDist edgeDist = Util.getNormalDist(pathNumber, from, to, tod, days);
			retDist.mean += edgeDist.mean;
			retDist.var += edgeDist.var;
		}
		return retDist;
	}

	public static NormalDist GenerateActual(String pathNumber, String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Calendar> endTimes = SpeedUp.TimeInependentTravelTime(pathNumber, sensorList, startTimes);
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		for (int i = 0; i < startTimes.size(); ++i)
			travelTimes.add(SpeedUp.ToMinutes(startTimes.get(i), endTimes.get(i)));
		NormalDist retDist = new NormalDist(travelTimes);
		return retDist;
	}
}
