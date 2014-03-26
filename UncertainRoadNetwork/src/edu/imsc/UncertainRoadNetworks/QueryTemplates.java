package edu.imsc.UncertainRoadNetworks;

import java.io.BufferedReader;
import java.io.FileReader;

public class QueryTemplates {
	private static String readQuery(String fileName)
	{
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
	
	//Util.java
	public static String ttQuery = readQuery("QueryTemplates\\ttQuery.sql");
	public static String ttCongQuery = readQuery("QueryTemplates\\ttCongQuery.sql");
	public static String singleTTQuery = readQuery("QueryTemplates\\singleTTQuery.sql");
	public static String congQuery = readQuery("QueryTemplates\\congQuery.sql");
	public static String pearsonQuery = readQuery("QueryTemplates\\PearsonQuery.sql");
	
	//SpeedUp.java
	public static String edgeDistanceQuery = readQuery("QueryTemplates\\SelectEdgeDistance.sql");
	public static String sensorSpeedsQuery = readQuery("QueryTemplates\\SelectSensorSpeeds.sql");
	public static String sensorAvgSpeedQuery = readQuery("QueryTemplates\\SelectSensorAvgSpeed.sql");
	public static String timeInDepTravelTime = readQuery("QueryTemplates\\SelectTimeIndepTT.sql");
	public static String timeDepTravelTime = readQuery("QueryTemplates\\SelectTimeDepTT.sql");
	public static String avgTravleTime = readQuery("QueryTemplates\\SelectAvgTT.sql");
	public static String avgLinkTravleTime = readQuery("QueryTemplates\\SelectAvgLinkTT.sql");
	
	//DataPrepration.java
	public static String createPathSensors = readQuery("QueryTemplates\\DataPreparation\\CreatePathSensorsTable.sql");
	public static String selectSensroInfo = readQuery("QueryTemplates\\DataPreparation\\SelectSensorInfo.sql");
	public static String insertSensroInfo = readQuery("QueryTemplates\\DataPreparation\\InsertSensorInfo.sql");
	public static String createPathEdges = readQuery("QueryTemplates\\DataPreparation\\CreatePathEdgeTable.sql");
	public static String selectEdgeInfo = readQuery("QueryTemplates\\DataPreparation\\SelectEdgeInfo.sql");
	public static String insertEdgeInfo = readQuery("QueryTemplates\\DataPreparation\\InsertEdgeInfo.sql");
	public static String pathPatterns = readQuery("QueryTemplates\\DataPreparation\\PathPatterns.sql");
	
	//GenerateLinkCorrelations.java
	public static String createLinkCorr = readQuery("QueryTemplates\\LinkCorrelations\\CreateTable.sql");
	public static String selectEdgeTT = readQuery("QueryTemplates\\LinkCorrelations\\SelectEdgeTravelTimes.sql");
	public static String insertCont = readQuery("QueryTemplates\\LinkCorrelations\\InsertCont.sql");
	public static String selectCong = readQuery("QueryTemplates\\LinkCorrelations\\SelectCong.sql");
	public static String updateCong = readQuery("QueryTemplates\\LinkCorrelations\\UpdateCong.sql");
	
	//KML Generation
	public static String KMLGenerationQuery = readQuery("QueryTemplates\\KMLGenerationQuery.sql");
}
