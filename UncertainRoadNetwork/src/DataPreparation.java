import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;

public class DataPreparation {
	
	public static void ClearPathSensorTable(String pathNumber) throws SQLException{
		OracleConnection conn = Util.getConnection();
		
		String clearPathSensors = Util.readQuery("QueryTemplates\\ClearPathSensors.sql")
				.replace("##PATH_NUM##", pathNumber);
		Statement clearStm = conn.createStatement();
		clearStm.execute(clearPathSensors);
		clearStm.close();
		
		conn.close();
	}
	
	public static void PopulatePathSensorTable(String pathNumber, String[] sensorList) throws SQLException{
		OracleConnection conn = Util.getConnection();

		String selectQueryTemplate = Util.readQuery("QueryTemplates\\SelectSensorInfo.sql");
		String insertQueryTemplate = Util.readQuery("QueryTemplates\\InsertSensorInfo.sql");
		int position = 1;
		for (String sensor : sensorList) {
			String selectQuery = selectQueryTemplate.replace("##LINK_ID##", sensor);
			
			Statement stm = conn.createStatement();
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(selectQuery);
			if (ors.next()) {
				String lat = ors.getString(1);
				String lon = ors.getString(2);
				String postMile = ors.getString(3);
				String direction = "";
				switch (ors.getString(4)) {
				case "0": direction = "North"; break;
				case "1": direction = "South"; break;
				case "2": direction = "East"; break;
				case "3": direction = "West"; break;
				}
				String insertQuery = insertQueryTemplate
						.replace("##PATH_NUM##", pathNumber)
						.replace("##POSITION##", Integer.toString(position))
						.replace("##LINK_ID##", sensor)
						.replace("##LAT##", lat)
						.replace("##LON##", lon)
						.replace("##POSTMILE##", postMile)
						.replace("##DIRECTION##", direction);
				Statement insStm = conn.createStatement();
				insStm.execute(insertQuery);
				insStm.close();
			}
			ors.close();
			stm.close();			
			position++;
		}
		conn.close();
	}
	
	public static void ClearPathEdgeTable(String pathNumber) throws SQLException {
		OracleConnection conn = Util.getConnection();
		
		String clearPathEdges = Util.readQuery("QueryTemplates\\ClearPathEdges.sql")
				.replace("##PATH_NUM##", pathNumber);
		Statement clearStm = conn.createStatement();
		clearStm.execute(clearPathEdges);
		clearStm.close();
		
		conn.close();
	}
	
	public static void PopulatePathEdgeTable(String pathNumber, int size) throws SQLException {
		OracleConnection conn = Util.getConnection();
				
		String selectQueryTemplate = Util.readQuery("QueryTemplates\\SelectEdgeInfo.sql");
		String insertQueryTemplate = Util.readQuery("QueryTemplates\\InsertEdgeInfo.sql");
		for (int position = 1; position < size; ++position) {
			String from = Integer.toString(position);
			String to = Integer.toString(position + 1);
			
			String selectQuery = selectQueryTemplate
					.replace("##PATH_NUM##", pathNumber)
					.replace("##FROM##", from)
					.replace("##TO##", to);
			Statement selStm = conn.createStatement();
			OracleResultSet ors = (OracleResultSet) selStm.executeQuery(selectQuery);
			if (ors.next()) {
				String fromLink = ors.getString(1);
				String toLink = ors.getString(2);
				String dist = ors.getString(3);
				
				String insertQuery = insertQueryTemplate
						.replace("##PATH_NUM##", pathNumber)
						.replace("##FROM##", fromLink)
						.replace("##TO##", toLink)
						.replace("##DISTANCE##", dist);
				Statement insStm = conn.createStatement();
				insStm.execute(insertQuery);
				insStm.close();				
			}
			ors.close();
			selStm.close();			
		}
		conn.close();		
	}

}
