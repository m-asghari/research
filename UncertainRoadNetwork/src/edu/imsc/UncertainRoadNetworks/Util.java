package edu.imsc.UncertainRoadNetworks;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import oracle.jdbc.OracleDriver;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;


public class Util {
	
	private static Logging logger = new Logging();
	
	private static final String host = "gd.usc.edu";
	private static final String port = "1521";
	private static final String service = "adms";
	private static final String username = "sch_sensor";
	private static final String password = "phe334";
	public static final OracleConnection conn = getConnection();
	
	public static long p_passedMillis, l_passedMillis, pl_passedMillis;
	public static int p_timeCounter, l_timeCounter, pl_timeCounter;
	
	public static String path = "";
	public static String pathNumber = "";
	
	public static enum PredictionMethod {Historic, Filtered, Interpolated, NoPrediction};
	public static PredictionMethod predictionMethod = PredictionMethod.Historic;
	public static Double similarityThreshold;
	public static Double alpha;
	public static Double timeHorizon;
	public static ArrayList<Integer> days = new ArrayList<Integer>();
	
	public static Double maxSpeed = 65.0;
	
	//public static HashMap<Pair<String, String>, Double> pearsonCorrCoef;
	//public static HashMap<Pair<String, String>, ArrayList<Double>> congChangeProb;
	public static final int f2f = 0;
	public static final int f2t = 1;
	public static final int t2f = 2;
	public static final int t2t = 3;
	
	public static DateFormat oracleDF = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
	public static DateFormat timeOfDayDF = new SimpleDateFormat("HH:mm");
	
	public static long no_exact_data = 0;
	public static long no_close_data = 0;
	public static long no_model = 0;
	public static long model = 0;
	public static long no_actual = 0;
	public static long actual = 0;
	
	private static OracleConnection getConnection()
	{
		OracleConnection conn = null;
		
		String url = String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, service);
		
		try
		{
			DriverManager.registerDriver(new OracleDriver());
			conn = (OracleConnection) DriverManager.getConnection(url, username, password);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
		return conn;
	}
	
	public static void Log(String message) {
		logger.Add(message);
	}
	
	public static NormalDist getNormalDist(String from, String tod,
			ArrayList<Integer> days) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = PathData.GetTravelTimes(from, tod, days, null);
		if (travelTimes.size() == 0) {
			return null;
		}
		return new NormalDist(travelTimes);
	}

	public static PMF getPMF(String from, String tod,
			ArrayList<Integer> days, Boolean cong) throws SQLException, ParseException{
		ArrayList<Double> travelTimes = PathData.GetTravelTimes(from, tod, days, cong);
		//Util.Log(String.format("From: %s, Cong: %s, TravelTimes: %s", from, (cong == null) ? "null" : cong.toString(), travelTimes.toString()));
		if (travelTimes.size() == 0) {
			return null;
		}
		return new PMF(travelTimes);
	}
	
	public static ArrayList<Integer> FilterDays(ArrayList<Integer> days, String from,
			Calendar startTime) {
		ArrayList<Integer> filteredDays = new ArrayList<Integer>();
		String tod = Util.timeOfDayDF.format(startTime.getTime());
		Integer currentDay = startTime.get(Calendar.DAY_OF_YEAR);
		Double currentTT = null;
		try {
			currentTT = PathData.GetEdgePattern(from, tod, currentDay).getFirst();
		}
		catch (NullPointerException nep) {
			return filteredDays;
		}
		if (currentTT == null) return filteredDays;
		for (int day : days) {
			Double travelTime = null;
			try {
				travelTime = PathData.GetEdgePattern(from, tod, day).getFirst();
			}
			catch (NullPointerException nep) {
				continue;
			}
			if (travelTime == null) continue;
			if (Math.abs(travelTime - currentTT)/currentTT < similarityThreshold )
				filteredDays.add(day);			
		}		
		return filteredDays;
	}
	
	public static Double GetActualTravelTime(String[] sensorList, Calendar startTime) {
		Double travelTime = 0.0;
		Calendar currentTime = (Calendar)startTime.clone();
		for (int i = 0; i < sensorList.length - 1; ++i) {
			String from = sensorList[i];
			Double edgeTravelTime = GetActualEdgeTravelTime(from, RoundTimeDown((Calendar)currentTime.clone()));
			if (edgeTravelTime == null) {
				return null;
			}
			int second = RoundDouble(edgeTravelTime, 1);
			currentTime.add(Calendar.SECOND, second);
			travelTime += edgeTravelTime;			
		}		
		return travelTime;
	}
	
	public static Double GetSnapshotTravelTime(String[] sensorList, Calendar startTime) {
		Double travelTime = 0.0;
		for (int i = 0; i < sensorList.length - 1; ++i) {
			String from = sensorList[i];
			try {
				Double edgeTravelTime = GetActualEdgeTravelTime(from, RoundTimeDown((Calendar)startTime.clone()));
				if (edgeTravelTime == null) {
					return null;
				}
				travelTime += edgeTravelTime;
			}
			catch (Exception e) {
				System.out.print(e.getMessage());
			}
		}
		return travelTime;
	}
	
	public static Double GetActualEdgeTravelTime(String from, Calendar startTime) {
		int day = startTime.get(Calendar.DAY_OF_YEAR);
		String tod = timeOfDayDF.format(startTime.getTime());
		Double travelTime = null;
		try {
			travelTime = PathData.GetEdgePattern(from, tod, day).getFirst();
		}
		catch (NullPointerException nep) {
			//travelTime = GetMeanEdgeTravelTime(from, tod, days);
			//if (travelTime == null){
				return null;
			//}
		}
		return travelTime;
	}
	
	public static Double GetMeanEdgeTravelTimee(String from, String tod,	ArrayList<Integer> days) {
		ArrayList<Double> travelTimes = PathData.GetTravelTimes(from, tod, days, null);
		if (travelTimes.size() == 0) {
			return null;
		}
		return GetMean(travelTimes);
	}
		
	public static ArrayList<Double> Interpolate(ArrayList<Double> input, Double v, Double alpha) {
		ArrayList<Double> retList = new ArrayList<Double>();
		for (Double d : input)
			retList.add((1-alpha)*d + alpha * v);
		return retList;
	}

	public static Double PearsonCorrCoef(ArrayList<Double> X, ArrayList<Double> Y) {
		int size = Math.min(X.size(), Y.size());
		Double sumX = 0.0, sumY = 0.0;
		for (int i = 0; i < size; ++i) {
			sumX += X.get(i);
			sumY += Y.get(i);
		}
		Double avgX = sumX / size;
		Double avgY = sumY / size;
		
		sumX = sumY = 0.0;
		for (int i = 0; i < size; ++i) {
			sumX += Math.pow(X.get(i) - avgX, 2);
			sumY += Math.pow(Y.get(i) - avgY, 2);
		}
		Double stdX = Math.sqrt(sumX / size);
		Double stdY = Math.sqrt(sumY / size);
		
		Double sum = 0.0;
		for (int i = 0; i < size; ++i) {
			sum += (X.get(i) - avgX) * (Y.get(i) - avgY);
		}
		
		Double retVal = sum / (stdX * stdY * (size - 1));
		return retVal;
	}
	
	public static HashMap<Pair<Integer, Integer>, Double>  getCongestionChange(String from1, String from2, 
			Calendar time, ArrayList<Integer> days) throws SQLException, ParseException{
		
		Statement stm = conn.createStatement();
		String query = QueryTemplates.congQuery
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM1##", from1)
				.replace("##FROM2##", from2);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		Double f2f = 0.0, f2t = 0.0, t2f = 0.0, t2t = 0.0;
		while (ors.next()) {
			boolean status1 = (ors.getString(2).equals("TRUE")) ? true : false;
			boolean status2 = (ors.getString(3).equals("TRUE")) ? true : false;
			if (!status1 && !status2) f2f++;
			if (!status1 && status2) f2t++;
			if (status1 && !status2) t2f++;
			if (status1 && status2) t2t++;			
		}
		ors.close();
		stm.close();
		HashMap<Pair<Integer, Integer>, Double> retValue = new HashMap<Pair<Integer,Integer>, Double>();
		Double falseTotal = (f2f+f2t == 0.0) ? 1.0 : f2f+f2t;
		Double trueTotal = (t2f+t2t == 0.0) ? 1.0 : t2f+t2t;
		retValue.put(new Pair<Integer, Integer>(0, 0),  f2f/falseTotal);
		retValue.put(new Pair<Integer, Integer>(0, 1), f2t/falseTotal);
		retValue.put(new Pair<Integer, Integer>(1, 0), t2f/trueTotal);
		retValue.put(new Pair<Integer, Integer>(1, 1), t2t/trueTotal);
		return retValue;
	}

	public static int RoundDouble(double input, int base) {
		int retValue = ((int)input/base)*base;		
		if (input % base > (double)base/2) retValue += base;
		return retValue;
	}
		
	public static Calendar RoundTimeDown(Calendar input) {
		int minutes = input.get(Calendar.MINUTE);
		int offset = minutes % 5;
		input.add(Calendar.MINUTE, -offset);
		input.set(Calendar.SECOND, 0);
		return input;
	}
	
	public static Calendar RoundTimeUp(Calendar input) {
		int minutes = input.get(Calendar.MINUTE);
		int offset = minutes % 5;
		input.add(Calendar.MINUTE, 5 - offset);
		input.set(Calendar.SECOND, 0);
		return input;
	}
	
	public static Double GetMean(ArrayList<Double> input) {
		Double sum = 0.0;
		for (Double d : input)
			sum += d;
		int size = (input.size() == 0) ? 1 : input.size();
		return sum/size;
	}
	

	/*public static void Initialize() {
		pearsonCorrCoef = PearsonCorrCoef();
		congChangeProb = CongChageProbs();
	}
	
	public static HashMap<Pair<String, String>, Double> PearsonCorrCoef() {
		HashMap<Pair<String, String>, Double> retMap = new HashMap<Pair<String,String>, Double>();
		
		try {
			Statement stm = conn.createStatement();
			String query = QueryTemplates.pearsonQuery
					.replace("##PATH_NUM##", pathNumber);
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
			while (ors.next()) {
				String link1 = ors.getString(1);
				String link2 = ors.getString(2);
				retMap.put(new Pair<String, String>(link1, link2), ors.getDouble(3));
				}
			ors.close();
			stm.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return retMap;
	}
	
	private static HashMap<Pair<String, String>, ArrayList<Double>> CongChageProbs() {
		HashMap<Pair<String, String>, ArrayList<Double>> retMap = new HashMap<Pair<String,String>, ArrayList<Double>>();
		
		try {
			Statement stm = conn.createStatement();
			String query = QueryTemplates.congQuery
					.replace("##PATH_NUM##", pathNumber);
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
			while (ors.next()) {
				String link1 = ors.getString(1);
				String link2 = ors.getString(2);
				ArrayList<Double> probs = new ArrayList<Double>();
				probs.add(ors.getDouble(3));
				probs.add(ors.getDouble(4));
				probs.add(ors.getDouble(5));
				probs.add(ors.getDouble(6));
				retMap.put(new Pair<String, String>(link1, link2), probs);
				}
			ors.close();
			stm.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return retMap;
	}*/
	
	//Deprecated
	/*public static PMF getPMF(String from, Calendar time,
			ArrayList<Integer> days, boolean cong) throws SQLException, ParseException{
		ArrayList<Double> travelTimes = getTravelTimes(from, time, days, cong);
		return new PMF(travelTimes);
		}
	
	public static ArrayList<Integer> FilterDays(ArrayList<Integer> days, String from,
			Calendar startTimeStamp) throws SQLException, ParseException{
		Statement stm = conn.createStatement();
		String query = QueryTemplates.edgeDistanceQuery
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		Double baseTravelTime = 1.0;
		if (ors.next()) baseTravelTime = (ors.getDouble(1)*3600)/maxSpeed;
		ors.close();
		
		String startTime = oracleDF.format(RoundTimeDown((Calendar)startTimeStamp.clone()).getTime());
		String endTime = oracleDF.format(RoundTimeUp((Calendar)startTimeStamp.clone()).getTime());
		query = QueryTemplates.singleTTQuery
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime);
		ors = (OracleResultSet) stm.executeQuery(query);
		Double startTravelTime = 0.0;
		if (ors.next()) startTravelTime = ors.getDouble(1);
		else {
			startTravelTime = GetMean(getTravelTimes(from, (Calendar)startTimeStamp.clone(), days));
		}
		ors.close();

		startTime = timeOfDayDF.format(RoundTimeDown((Calendar)startTimeStamp.clone()).getTime());
		endTime = timeOfDayDF.format(RoundTimeUp((Calendar)startTimeStamp.clone()).getTime());
		query = QueryTemplates.ttQuery
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime);
		ors = (OracleResultSet) stm.executeQuery(query);
		ArrayList<Integer> filteredDays = new ArrayList<Integer>();
		int daysIdx = 0;
		boolean cont = true;
		while (ors.next() && cont) {
			Calendar dayCal = Calendar.getInstance();
			dayCal.setTime(ors.getTimestamp(1));
			Integer day = dayCal.get(Calendar.DAY_OF_YEAR);
			Double travelTime = ors.getDouble(2);
			while (days.get(daysIdx) < day)
				if (daysIdx < days.size() - 1)
					daysIdx++;
				else {
					cont = false;
					break;
				}
			if (days.get(daysIdx) == day)
				if (Math.abs(travelTime - startTravelTime) <= (similarityThreshold*baseTravelTime))
					filteredDays.add(day);
		}
		ors.close();
		stm.close();
		return filteredDays;
	}
	
	public static Double GetActualTravelTime(String from,
			Calendar startTime) throws SQLException{
		Double retValue = 0.0;
		
		String startTimeStr = oracleDF.format(RoundTimeDown((Calendar)startTime.clone()).getTime());
		String endTimeStr = oracleDF.format(RoundTimeUp((Calendar)startTime.clone()).getTime());
		Statement stm = conn.createStatement();
		String qeury = QueryTemplates.singleTTQuery
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTimeStr)
				.replace("##END_TIME##", endTimeStr);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(qeury);
		if (ors.next()) retValue = ors.getDouble(1);
		ors.close();
		stm.close();
		return retValue;
	}
	
	private static ArrayList<Double> getTravelTimes(String from, Calendar time,
			ArrayList<Integer> days, boolean cong) throws SQLException, ParseException {
		Statement stm = conn.createStatement();
		String startTime = timeOfDayDF.format(RoundTimeDown((Calendar)time.clone()).getTime());
		String endTime = timeOfDayDF.format(RoundTimeUp((Calendar)time.clone()).getTime());
		//SELECT TIME, TRAVEL_TIME FROM PATH#_EDGE_PATTERNS WHERE FROM, TO, START <= TOD AND END >= TOD
		String query = QueryTemplates.ttCongQuery
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime)
				.replace("##CONG##", cong ? "TRUE" : "FALSE");
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		int daysIdx = 0;
		boolean cont = true;
		while (ors.next() && cont) {
			Calendar dayCal = Calendar.getInstance();
			dayCal.setTime(ors.getTimestamp(1));
			Integer day = dayCal.get(Calendar.DAY_OF_YEAR);
			while (days.get(daysIdx) < day)
				if (daysIdx < days.size() - 1)
					daysIdx++;
				else {
					cont = false;
					break;
				}
			if (days.get(daysIdx) == day)
				travelTimes.add(ors.getDouble(2));
		}
		ors.close();
		stm.close();
		return travelTimes;
	}
		
	private static Double GetAverageEdgeTravelTime(String from, Calendar tod) throws SQLException {
		Statement stm = conn.createStatement();
		
		String startTime = Util.timeOfDayDF.format(Util.RoundTimeDown((Calendar)tod.clone()).getTime());
		String endTime = Util.timeOfDayDF.format(Util.RoundTimeUp((Calendar)tod.clone()).getTime());
		String query = QueryTemplates.avgLinkTravleTime
				.replace("##PATH_NUM##", Util.pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		Double retVal = 0.0;
		if (ors.next())
			retVal = ors.getDouble(1);
		ors.close();
		stm.close();
		return retVal;
	}
	
	public static ArrayList<Double> getTravelTimes(String from, Calendar time,
			ArrayList<Integer> days) throws SQLException, ParseException {
		
		Statement stm = conn.createStatement();
		String startTime = timeOfDayDF.format(RoundTimeDown((Calendar)time.clone()).getTime());
		String endTime = timeOfDayDF.format(RoundTimeUp((Calendar)time.clone()).getTime());
		String query = QueryTemplates.ttQuery
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		int daysIdx = 0;
		boolean cont = true;
		while (ors.next() && cont) {
			Calendar dayCal = Calendar.getInstance();
			dayCal.setTime(ors.getTimestamp(1));
			Integer day = dayCal.get(Calendar.DAY_OF_YEAR);
			while (days.get(daysIdx) < day) 
				if (daysIdx < days.size() - 1)
					daysIdx++;
				else {
					cont = false;
					break;
				}
			if (days.get(daysIdx) == day)
				travelTimes.add(ors.getDouble(2));
		}
		ors.close();
		stm.close();
		return travelTimes;
	}*/
}
