package edu.imsc.UncertainRoadNetworks;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.driver.OracleResultSet;

public class DataPreparation {
	
	public static void GeneratePathSensorTable(String[] sensorList) throws SQLException {
		//StringBuilder sb = new StringBuilder();
		
		//Create a new table
		String query = Util.readQuery("QueryTemplates\\DataPreparation\\CreatePathSensorsTable.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		//sb.append(query);
		Statement stm = Util.conn.createStatement();
		stm.execute(query);
		stm.close();
		
		//Populate new table
		String selectQueryTemplate = Util.readQuery("QueryTemplates\\DataPreparation\\SelectSensorInfo.sql");
		String insertQueryTemplate = Util.readQuery("QueryTemplates\\DataPreparation\\InsertSensorInfo.sql");
		int position = 1;
		for (String sensor : sensorList) {
			String selectQuery = selectQueryTemplate.replace("##LINK_ID##", sensor);
			
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
				String insertQuery = insertQueryTemplate
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
				//sb.append(insertQuery);
			}
			ors.close();
			selStm.close();			
			position++;
		}
		//return sb.toString();		
	}

	public static String GeneratePathEdgeTable(int size) throws SQLException{
		StringBuilder sb = new StringBuilder();
		//Create a new table
		String createQuery = Util.readQuery("QueryTemplates\\DataPreparation\\CreatePathEdgeTable.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		sb.append(createQuery);
		//Statement stm = Util.conn.createStatement();
		//stm.execute(createQuery);
		//stm.close();
		
		//Populate new table
		String selectQueryTemplate = Util.readQuery("QueryTemplates\\DataPreparation\\SelectEdgeInfo.sql");
		String insertQueryTemplate = Util.readQuery("QueryTemplates\\DataPreparation\\InsertEdgeInfo.sql");
		for (int position = 1; position < size; ++position) {
			String from = Integer.toString(position);
			String to = Integer.toString(position + 1);
			
			String selectQuery = selectQueryTemplate
					.replace("##PATH_NUM##", Util.pathNumber)
					.replace("##FROM##", from)
					.replace("##TO##", to);
			Statement selStm = Util.conn.createStatement();
			OracleResultSet ors = (OracleResultSet) selStm.executeQuery(selectQuery);
			if (ors.next()) {
				String fromLink = ors.getString(1);
				String toLink = ors.getString(2);
				String dist = ors.getString(3);
				
				String insertQuery = insertQueryTemplate
						.replace("##PATH_NUM##", Util.pathNumber)
						.replace("##FROM##", fromLink)
						.replace("##TO##", toLink)
						.replace("##DISTANCE##", dist)
						.replace("##MAX_SPEED##", Double.toString(65.0));
				sb.append(insertQuery);
				//Statement insStm = Util.conn.createStatement();
				//insStm.execute(insertQuery);
				//insStm.close();				
			}
			ors.close();
			selStm.close();			
		}
		return sb.toString();
	}
	
	public static void GeneratePathSpeedPatternsTable() throws SQLException {
		//Create a new table
		String fileContent = Util.readQuery("QueryTemplates\\DataPreparation\\CreatePathSpeedPatternsTable.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		String[] queries = fileContent.split(";");
		for (String query : queries) {
			Statement stm = Util.conn.createStatement();
			stm.execute(query);
			stm.close();
		}
		
		//Populate new table
		String insertQuery = Util.readQuery("QueryTemplates\\DataPreparation\\InsertPathSpeedPatterns.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		Statement stm = Util.conn.createStatement();
		stm.execute(insertQuery);
		stm.close();		
	}
	
	public static void GeneratePathEdgePatternsTable() throws SQLException {
		//Create Temp table
		String createTemp = Util.readQuery("QueryTemplates\\DataPreparation\\CreateTempTable.sql");
		Statement createStm = Util.conn.createStatement();
		createStm.execute(createTemp);
		createStm.close();
		
		//Populate Temp table
		String fileContent = Util.readQuery("QueryTemplates\\DataPreparation\\InsertTempTable.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		String[] insQueries = fileContent.split(";");
		for (String insTemp : insQueries) {
			Statement stm = Util.conn.createStatement();
			stm.execute(insTemp);
			stm.close();
		}
		
		//Create a new table
		fileContent = Util.readQuery("QueryTemplates\\DataPreparation\\CreatePathEdgePatternsTable.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		String[] queries = fileContent.split(";");
		for (String query : queries) {
			Statement stm = Util.conn.createStatement();
			stm.execute(query);
			stm.close();
		}
		
		//Populate new table
		String insQuery = Util.readQuery("QueryTemplates\\DataPreparation\\InsertPathEdgePatterns.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		Statement insStm = Util.conn.createStatement();
		insStm.execute(insQuery);
		insStm.close();
		
		//Drop Temp table
		String dropQuery = Util.readQuery("QueryTemplates\\DataPreparation\\DropTempTable.sql");
		Statement dropStm = Util.conn.createStatement();
		dropStm.execute(dropQuery);
		dropStm.close();		
	}
	
	public static void GeneratePathQueries(String[] sensorList) throws SQLException, IOException {
		FileWriter fw = new FileWriter(String.format("Path%s_Queries.sql", Util.pathNumber));
		BufferedWriter bw = new BufferedWriter(fw);
		
		GeneratePathSensorTable(sensorList);
		bw.write(GeneratePathEdgeTable(sensorList.length));
		bw.write(Util.readQuery("QueryTemplates\\DataPreparation\\PathPatterns.sql").replace("##PATH_NUM##", Util.pathNumber));
		
		bw.close();
		fw.close();
	}
	
	public static void PrepareNewData(String[] sensorList) throws SQLException, IOException{
		//GeneratePathSensorTable(pathNumber, sensorList);
		//GeneratePathEdgeTable(pathNumber, sensorList.length);
		//GeneratePathQueries(pathNumber,);
		//GeneratePathSpeedPatternsTable(pathNumber);
		//GeneratePathEdgePatternsTable(pathNumber);
	}

	public static void main(String[] args) {
		String path = "768701-774344-770599-768297-768283-770587-770012-770024-770036-770354-770048-770331-770544-770061-770556-770076-771202-770089-770103-771636-770475-770487-770116-769895-769880-769866-769847-768230-767610-767598-718076-767471-718072-767454-762329-767621-767573-718066-767542-718064-767495-718375-716955-718370-716949-760650-718045-718173-760643-760635-774671-718166-764037";
		String[] sensorList = path.split("-");
		try {
			GeneratePathQueries(sensorList);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void ClearPathSensorTable() throws SQLException{
		String clearPathSensors = Util.readQuery("QueryTemplates\\ClearPathSensors.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		Statement clearStm = Util.conn.createStatement();
		clearStm.execute(clearPathSensors);
		clearStm.close();
	}
	
	public static void PopulatePathSensorTable(String[] sensorList) throws SQLException{
		String selectQueryTemplate = Util.readQuery("QueryTemplates\\SelectSensorInfo.sql");
		String insertQueryTemplate = Util.readQuery("QueryTemplates\\InsertSensorInfo.sql");
		int position = 1;
		for (String sensor : sensorList) {
			String selectQuery = selectQueryTemplate.replace("##LINK_ID##", sensor);
			
			Statement stm = Util.conn.createStatement();
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
			stm.close();			
			position++;
		}
	}
	
	public static void ClearPathEdgeTable() throws SQLException {
		String clearPathEdges = Util.readQuery("QueryTemplates\\ClearPathEdges.sql")
				.replace("##PATH_NUM##", Util.pathNumber);
		Statement clearStm = Util.conn.createStatement();
		clearStm.execute(clearPathEdges);
		clearStm.close();
	}
	
	public static void PopulatePathEdgeTable(int size) throws SQLException {
		String selectQueryTemplate = Util.readQuery("QueryTemplates\\SelectEdgeInfo.sql");
		String insertQueryTemplate = Util.readQuery("QueryTemplates\\InsertEdgeInfo.sql");
		for (int position = 1; position < size; ++position) {
			String from = Integer.toString(position);
			String to = Integer.toString(position + 1);
			
			String selectQuery = selectQueryTemplate
					.replace("##PATH_NUM##", Util.pathNumber)
					.replace("##FROM##", from)
					.replace("##TO##", to);
			Statement selStm = Util.conn.createStatement();
			OracleResultSet ors = (OracleResultSet) selStm.executeQuery(selectQuery);
			if (ors.next()) {
				String fromLink = ors.getString(1);
				String toLink = ors.getString(2);
				String dist = ors.getString(3);
				
				String insertQuery = insertQueryTemplate
						.replace("##PATH_NUM##", Util.pathNumber)
						.replace("##FROM##", fromLink)
						.replace("##TO##", toLink)
						.replace("##DISTANCE##", dist);
				Statement insStm = Util.conn.createStatement();
				insStm.execute(insertQuery);
				insStm.close();				
			}
			ors.close();
			selStm.close();			
		}	
	}

	
}
