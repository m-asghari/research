package edu.imsc.UncertainRoadNetworks;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import oracle.jdbc.driver.OracleResultSet;

public class PathData {
	public static HashMap<String, HashMap<Integer, HashMap<String, Pair<Double, Boolean>>>> edgePatterns = new 
			HashMap<String, HashMap<Integer,HashMap<String,Pair<Double,Boolean>>>>();
	

	public static void main(String[] args) {
		LoadEdgePatterns();		
	}
	
	public static void LoadEdgePatterns() {
		try {
			Statement stm = Util.conn.createStatement();
			String query = QueryTemplates.edgePatternsQuery
					.replace("##PATH_NUM##", Util.pathNumber);
			
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
			while (ors.next()) {
				String from = ors.getString(1);
				if (!edgePatterns.containsKey(from)) {
					edgePatterns.put(from, new HashMap<Integer, HashMap<String,Pair<Double,Boolean>>>());
				}
				Integer day = ors.getInt(2);
				if (!edgePatterns.get(from).containsKey(day)) {
					edgePatterns.get(from).put(day, new HashMap<String, Pair<Double,Boolean>>());
				}
				String tod = ors.getString(3);
				Double travelTime = ors.getDouble(4);
				Boolean cong = (ors.getString(5).equals("TRUE")) ? true : false;
				edgePatterns.get(from).get(day).put(tod, new Pair<Double, Boolean>(travelTime, cong));
			}
			ors.close();
			stm.close();
			//Print();
		}
		catch (Exception e){
			Util.Log(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void Reset() {
		edgePatterns = new HashMap<String, HashMap<Integer,HashMap<String,Pair<Double,Boolean>>>>();
	}
	
	@SuppressWarnings("unused")
	private static void Print() {
		for (Entry<String, HashMap<Integer, HashMap<String, Pair<Double, Boolean>>>> e1 : edgePatterns.entrySet()) {
			for (Entry<Integer, HashMap<String, Pair<Double, Boolean>>> e2 : e1.getValue().entrySet()) {
				for (Entry<String, Pair<Double, Boolean>> e3 : e2.getValue().entrySet()) {
					System.out.print(String.format("From: %s, Day: %d, tod: %s, TravelTime: %f\n", e1.getKey(), e2.getKey(), e3.getKey(), e3.getValue().getFirst()));
				}
			}
		}
	}
	
	public static Pair<Double, Boolean> GetEdgePattern(String from, String tod, Integer day) {
		try {
			Pair<Double, Boolean> pattern = edgePatterns.get(from).get(day).get(tod);
			return pattern;
		}
		catch (NullPointerException npe){
			return null;
		}
	}
	
	public static ArrayList<Double> GetTravelTimes(String from, String tod, ArrayList<Integer> days, Boolean cong) {
		ArrayList<Double> travelTimes = new ArrayList<Double>();  
		for (Integer day : days) {
			Pair<Double, Boolean> pattern = GetEdgePattern(from, tod, day);
			if (pattern == null) continue;
			Double travelTime = pattern.getFirst();
			if ((cong == pattern.getSecond() || cong == null) && travelTime != null) travelTimes.add(travelTime);
		}
		return travelTimes;
	}
}


