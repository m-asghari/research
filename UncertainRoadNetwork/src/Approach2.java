//Link Travel Times: Discrete
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

public class Approach2 {
	
	private static String selectTravelTimeQueryTemplate = Util.readQuery("QueryTemplates\\SelectTravelTime.sql");
	
	public static PMF GenerateModel(String pathNumber, String[] sensorList, 
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
		}
		
		conn.close();
		ArrayList<PMF> edgePMF = new ArrayList<PMF>();
		for (Entry<String, ArrayList<Double>> e : travelTimes.entrySet()) {
			edgePMF.add(new PMF(e.getValue()));
		}
		PMF retPMF = new PMF(0, 0);
		retPMF.prob.put(0, 1.0);
		for (PMF dist : edgePMF)
			retPMF = retPMF.Add(dist);
		return retPMF;
	}
	
	public static PMF GenerateActual(String pathNumber, String[] sensorList, 
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		for (Calendar startTime : startTimes) {
			ArrayList<Calendar> temp = new ArrayList<Calendar>();
			temp.add(startTime);
			Calendar endTime = SpeedUp.TravelTime(pathNumber, sensorList, temp).get(0);
			travelTimes.add(SpeedUp.ToMinutes(startTime, endTime));
		}
		PMF retPMF = new PMF(travelTimes);
		return retPMF;
	}
}
