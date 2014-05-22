package edu.imsc.UncertainRoadNetworks;

import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import oracle.jdbc.driver.OracleResultSet;

public class PathData {
	private static HashMap<String, HashMap<Integer, HashMap<String, Pair<Double, Boolean>>>> edgePatterns = new 
			HashMap<String, HashMap<Integer,HashMap<String,Pair<Double,Boolean>>>>();
	
	private static HashMap<Pair<String, String>, Double> pearsonCorrs = new HashMap<Pair<String,String>, Double>(); 
	
	private static HashMap<Pair<String, String>, ArrayList<Double>> congTrans = new HashMap<Pair<String,String>, ArrayList<Double>>();
	

	public static void main(String[] args) {
		LoadEdgePatterns();
		LoadEdgeCorrelations();
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
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void LoadEdgeCorrelations() {
		try {
			Statement stm = Util.conn.createStatement();
			String query = QueryTemplates.edgeDisCorrQuery
					.replace("##PATH_NUM##", Util.pathNumber);
			
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
			while (ors.next()) {
				String from = ors.getString(1);
				String to = ors.getString(2);
				Pair<String, String> ft = new Pair<String, String>(from, to);
				ArrayList<Double> trans = new ArrayList<Double>();
				trans.add(ors.getDouble(3));
				trans.add(ors.getDouble(4));
				trans.add(ors.getDouble(5));
				trans.add(ors.getDouble(6));
				congTrans.put(ft, trans);
			}
			ors.close();
			
			query = QueryTemplates.edgeConCorrQuery
					.replace("##PATH_NUM##", Util.pathNumber);
			ors = (OracleResultSet) stm.executeQuery(query);
			while (ors.next()) {
				String from = ors.getString(1);
				String to = ors.getString(2);
				Pair<String, String> ft = new Pair<String, String>(from, to);
				Pair<String, String> tf = new Pair<String, String>(to, from);
				Double pCorr = ors.getDouble(3);
				pearsonCorrs.put(ft, pCorr);
				pearsonCorrs.put(tf, pCorr);
			}
			ors.close();
			stm.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void Reset() {
		edgePatterns = new HashMap<String, HashMap<Integer,HashMap<String,Pair<Double,Boolean>>>>();
		pearsonCorrs = new HashMap<Pair<String,String>, Double>();
		congTrans = new HashMap<Pair<String,String>, ArrayList<Double>>();
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
	
	private static Pair<Double, Boolean> GetExactEdgePattern(String from, String tod, Integer day) {
		try {
			Pair<Double, Boolean> pattern = edgePatterns.get(from).get(day).get(tod);
			return pattern;
		}
		catch (NullPointerException npe){
			return null;
		}
	}
	
	private static ArrayList<Pair<Double, Boolean>> GetCloseEdgePatterns(String from, String tod, Integer day, Integer var) {
		ArrayList<Pair<Double, Boolean>> retList = new ArrayList<Pair<Double,Boolean>>();
		Calendar todCal = Calendar.getInstance();
		try {
			todCal.setTime(Util.timeOfDayDF.parse(tod));
		}
		catch (ParseException pe) {
			pe.printStackTrace();
		}
		for (int i = 1; i <= var; ++i) {
			Calendar copy = Calendar.getInstance();
			copy.setTime(todCal.getTime());
			copy.add(Calendar.MINUTE, i * 5);
			String copyTod = Util.timeOfDayDF.format(copy.getTime());
			Pair<Double, Boolean> p1 = GetExactEdgePattern(from, copyTod, day);
			if (p1 != null) retList.add(p1);
			copy.setTime(todCal.getTime());
			copy.add(Calendar.MINUTE, i * -5);
			copyTod = Util.timeOfDayDF.format(copy.getTime());
			Pair<Double, Boolean> p2 = GetExactEdgePattern(from, copyTod, day);
			if (p2 != null) retList.add(p2);
		}
		return retList;
	}
	
	public static Pair<Double, Boolean> GetEdgePattern(String from, String tod, Integer day) {
		Pair<Double, Boolean> pattern = GetExactEdgePattern(from, tod, day);
		if (pattern == null) 
		{
			Util.no_exact_data++;
			ArrayList<Pair<Double, Boolean>> closePatterns = GetCloseEdgePatterns(from, tod, day, 3);
			if (closePatterns.size() > 0) return closePatterns.get(0);
			Util.no_close_data++;
			return null;
		}
		return pattern;
	}
	
	public static ArrayList<Double> GetTravelTimes(String from, String tod, ArrayList<Integer> days, Boolean cong) {
		ArrayList<Double> travelTimes = new ArrayList<Double>();  
		for (Integer day : days) {
			//Pair<Double, Boolean> pattern = GetEdgePattern(from, tod, day);
			Pair<Double, Boolean> pattern = GetEdgePattern(from, tod, day);
			if (pattern == null) 
			{
				continue;
			}
			Double travelTime = pattern.getFirst();
			if ((cong == pattern.getSecond() || cong == null) && travelTime != null) travelTimes.add(travelTime);
		}
		return travelTimes;
	}
	
	public static Double GetPearsonCorr(String from, String to) {
		try {
			Double pCorr = pearsonCorrs.get(new Pair<String, String>(from, to));
			return pCorr;
		}
		catch (NullPointerException npe){
			return null;
		}
	}
	
	public static ArrayList<Double> GetCongTrans(String from, String to) {
		try {
			ArrayList<Double> trans = congTrans.get(new Pair<String, String>(from, to));
			return trans;
		}
		catch (NullPointerException npe){
			return null;
		}
	}
	
	public static Pair<Double, Double> GetLinkCongestion(String from) {
		Double congCtr = 0.0, normCtr = 0.0;
		try {
			for (HashMap<String,Pair<Double,Boolean>> patterns : edgePatterns.get(from).values())
				for (Pair<Double, Boolean> pattern : patterns.values()) {
					if (pattern.getSecond())
						congCtr++;
					else
						normCtr++;
				}
			Double congProb = 0.0, normProb = 0.0;
			if (congCtr + normCtr > 0) {
				congProb = congCtr / (congCtr + normCtr);
				normProb = normCtr / (congCtr + normCtr);
			}
			return new Pair<Double, Double>(normProb, congProb);
		}
		catch (NullPointerException nep) {
			return null;
		}
	}
}


