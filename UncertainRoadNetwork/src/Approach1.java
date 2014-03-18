//Link Travel Times: Continuous
//Time Dependency: None
//Link Corelation: None
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;

public class Approach1 {
	
	private static String selectTravelTimeQueryTemplate = Util.readQuery("QueryTemplates\\SelectTravelTime.sql");
	
	public static NormalDist GenerateModel(String pathNumber, String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException{
		HashMap<String, ArrayList<Double>> travelTimes = new HashMap<String, ArrayList<Double>>();
		OracleConnection conn = Util.getConnection();
		
		for (Calendar startTime : startTimes) {
			String query = selectTravelTimeQueryTemplate
					.replace("##PATH_NUM##", pathNumber)
					.replace("##TRAVEL_TIME##", Util.oracleDF.format(startTime));
			Statement stm = conn.createStatement();
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
			while (ors.next()) {
				String fromNode = ors.getString(1);
				double travelTime = ors.getDouble(2);
				travelTimes.get(fromNode).add(travelTime);
			}
			ors.close();
			stm.close();
			//Select from, travel_time from path_edge_patters where time = startTime
		}
		
		conn.close();
		ArrayList<NormalDist> edgeDist = new ArrayList<NormalDist>();
		for (Entry<String, ArrayList<Double>> e : travelTimes.entrySet()) {
			edgeDist.add(new NormalDist(e.getValue()));
		}
		NormalDist retDist = new NormalDist(0, 0);
		for (NormalDist dist : edgeDist)
			retDist = retDist.Add(dist);
		return retDist;
	}

	public static NormalDist GenerateActual(String pathNumber, String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		for (Calendar startTime : startTimes) {
			ArrayList<Calendar> temp = new ArrayList<Calendar>();
			temp.add(startTime);
			Calendar endTime = SpeedUp.TravelTime(pathNumber, sensorList, temp).get(0);
			travelTimes.add(SpeedUp.ToMinutes(startTime, endTime));
		}
		NormalDist retDist = new NormalDist(travelTimes);
		return retDist;
	}
}
