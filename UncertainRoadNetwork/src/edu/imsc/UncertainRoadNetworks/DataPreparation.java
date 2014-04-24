package edu.imsc.UncertainRoadNetworks;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.driver.OracleResultSet;

public class DataPreparation {
	public static FileWriter fw;
	public static BufferedWriter bw; 

	private static void GeneratePathSensorTable(String[] sensorList) throws SQLException {
		//Create a new table
		String query = QueryTemplates.createPathSensors
				.replace("##PATH_NUM##", Util.pathNumber);
		Statement stm = Util.conn.createStatement();
		stm.execute(query);
		stm.close();
		
		//Populate new table
		int position = 1;
		for (String sensor : sensorList) {
			String selectQuery = QueryTemplates.selectSensroInfo
					.replace("##LINK_ID##", sensor);
			
			Statement selStm = Util.conn.createStatement();
			OracleResultSet ors = (OracleResultSet) selStm.executeQuery(selectQuery);
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
				String insertQuery = QueryTemplates.insertSensroInfo
						.replace("##PATH_NUM##", Util.pathNumber)
						.replace("##POSITION##", Integer.toString(position))
						.replace("##LINK_ID##", sensor)
						.replace("##LAT##", lat)
						.replace("##LON##", lon)
						.replace("##POSTMILE##", postMile)
						.replace("##DIRECTION##", direction);
				Statement insStm = Util.conn.createStatement();
				insStm.execute(insertQuery);
				insStm.close();
			}
			ors.close();
			selStm.close();			
			position++;
		}	
	}

	private static String GeneratePathEdgeTable(int size) throws SQLException{
		StringBuilder sb = new StringBuilder();
		//Create a new table
		String createQuery = QueryTemplates.createPathEdges
				.replace("##PATH_NUM##", Util.pathNumber);
		sb.append(createQuery);
		
		//Populate new table
		for (int position = 1; position < size; ++position) {
			String from = Integer.toString(position);
			String to = Integer.toString(position + 1);
			
			String selectQuery = QueryTemplates.selectEdgeInfo
					.replace("##PATH_NUM##", Util.pathNumber)
					.replace("##FROM##", from)
					.replace("##TO##", to);
			Statement selStm = Util.conn.createStatement();
			OracleResultSet ors = (OracleResultSet) selStm.executeQuery(selectQuery);
			if (ors.next()) {
				String fromLink = ors.getString(1);
				String toLink = ors.getString(2);
				String dist = ors.getString(3);
				
				String insertQuery = QueryTemplates.insertEdgeInfo
						.replace("##PATH_NUM##", Util.pathNumber)
						.replace("##FROM##", fromLink)
						.replace("##TO##", toLink)
						.replace("##DISTANCE##", dist)
						.replace("##MAX_SPEED##", Double.toString(65.0));
				sb.append(insertQuery);				
			}
			ors.close();
			selStm.close();			
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unused")
	private static void GeneratePathQueries(String[] sensorList) throws SQLException, IOException {
		//FileWriter fw = new FileWriter(String.format("Path%s_Queries.sql", Util.pathNumber));
		//BufferedWriter bw = new BufferedWriter(fw);
		
		//GeneratePathSensorTable(sensorList);
		bw.write(GeneratePathEdgeTable(sensorList.length));
		bw.write(QueryTemplates.pathPatterns.replace("##PATH_NUM##", Util.pathNumber));
		
		//bw.close();
		//fw.close();
	}

	private static void UpdateTravelTimes(int totalPaths, String operation) throws SQLException{
		for (int pathNum = 1; pathNum <= totalPaths; ++pathNum) {
			Statement stm = Util.conn.createStatement();
			//UPDATE PATH#_EDGE_PATTERNS SET TRAVEL_TIME = TRAVEL_TIME / 60
			String query = QueryTemplates.updateTT
					.replace("##PATH_NUM##", Integer.toString(pathNum))
					.replace("##OPERATION##", operation);
			stm.execute(query);
			stm.close();
		}
	}

	public static void main(String[] args) {
		try {
			fw = new FileWriter("linkQueries.sql");
			bw = new BufferedWriter(fw);
			FileReader fr = new FileReader("paths.txt");
			BufferedReader br = new BufferedReader(fr);
			String link = "";
			int pathN = 100;
			while ((link = br.readLine()) != null) {
				pathN++;
				Util.pathNumber = Integer.toString(pathN);
				Util.path = link;
				String[] sensorList = Util.path.split("-");
				GeneratePathQueries(sensorList);
			}
			br.close();
			fr.close();
			bw.close();
			fw.close();
			//UpdateTravelTimes(52, "*");
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
