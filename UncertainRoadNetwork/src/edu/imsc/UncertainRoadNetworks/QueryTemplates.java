package edu.imsc.UncertainRoadNetworks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class QueryTemplates {
	private static String readQuery(String path)
	{
		String[] dirs = path.split("-");
		String fileName = Combine(dirs);
		String query = new String();
		try
		{
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null)
			{
				sb.append(line);
				sb.append("\n");
			}
			query = sb.toString();
			br.close();
			fr.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return query;
	}
	
	private static String Combine(String[] dirs) {
		if (dirs.length == 0)
			return "";
		File f = new File(dirs[0]);
		for (int i = 1; i < dirs.length; i++) {
			f = new File(f, dirs[i]);
		}
		return f.getPath();
	}
	
	//Util.java
	public static String ttQuery = readQuery("QueryTemplates-ttQuery.sql");
	public static String ttCongQuery = readQuery("QueryTemplates-ttCongQuery.sql");
	public static String singleTTQuery = readQuery("QueryTemplates-singleTTQuery.sql");
	public static String congQuery = readQuery("QueryTemplates-congQuery.sql");
	public static String pearsonQuery = readQuery("QueryTemplates-PearsonQuery.sql");
	public static String edgeDistanceQuery = readQuery("QueryTemplates-SelectEdgeDistance.sql");
	
	//PathData
	public static String edgePatternsQuery = readQuery("QueryTemplates-PathData-SelectEdgePatterns.sql");
	public static String edgeConCorrQuery = readQuery("QueryTemplates-PathData-SelectEdgeConCorr.sql");
	public static String edgeDisCorrQuery = readQuery("QueryTemplates-PathData-SelectEdgeDisCorr.sql");
	
	//SpeedUp.java
	public static String sensorSpeedsQuery = readQuery("QueryTemplates-SelectSensorSpeeds.sql");
	public static String sensorAvgSpeedQuery = readQuery("QueryTemplates-SelectSensorAvgSpeed.sql");
	public static String timeInDepTravelTime = readQuery("QueryTemplates-SelectTimeIndepTT.sql");
	public static String timeDepTravelTime = readQuery("QueryTemplates-SelectTimeDepTT.sql");
	public static String avgTravleTime = readQuery("QueryTemplates-SelectAvgTT.sql");
	public static String avgLinkTravleTime = readQuery("QueryTemplates-SelectAvgLinkTT.sql");
	
	//DataPrepration.java
	public static String createPathSensors = readQuery("QueryTemplates-DataPreparation-CreatePathSensorsTable.sql");
	public static String selectSensroInfo = readQuery("QueryTemplates-DataPreparation-SelectSensorInfo.sql");
	public static String insertSensroInfo = readQuery("QueryTemplates-DataPreparation-InsertSensorInfo.sql");
	public static String createPathEdges = readQuery("QueryTemplates-DataPreparation-CreatePathEdgeTable.sql");
	public static String selectEdgeInfo = readQuery("QueryTemplates-DataPreparation-SelectEdgeInfo.sql");
	public static String insertEdgeInfo = readQuery("QueryTemplates-DataPreparation-InsertEdgeInfo.sql");
	public static String pathPatterns = readQuery("QueryTemplates-DataPreparation-PathPatterns.sql");
	public static String updateTT = readQuery("QueryTemplates-DataPreparation-UpdateTravelTimes.sql");
	public static String dropPath = readQuery("QueryTemplates-DataPreparation-DropPath.sql");
	
	//GenerateLinkCorrelations.java
	public static String createLinkCorr = readQuery("QueryTemplates-LinkCorrelations-CreateTable.sql");
	public static String selectEdgeTT = readQuery("QueryTemplates-LinkCorrelations-SelectEdgeTravelTimes.sql");
	public static String insertCont = readQuery("QueryTemplates-LinkCorrelations-InsertCont.sql");
	public static String selectCong = readQuery("QueryTemplates-LinkCorrelations-SelectCong.sql");
	public static String updateCong = readQuery("QueryTemplates-LinkCorrelations-UpdateCong.sql");
	public static String selectEdgeCong = readQuery("QueryTemplates-LinkCorrelations-SelectEdgeCong.sql");
	
	//KML Generation
	public static String KMLGenerationQuery = readQuery("QueryTemplates-KMLGenerationQuery.sql");
}
