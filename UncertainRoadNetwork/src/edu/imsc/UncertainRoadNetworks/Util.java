package edu.imsc.UncertainRoadNetworks;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import oracle.jdbc.OracleDriver;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;


public class Util {
	
	private static final String host = "gd.usc.edu";
	private static final String port = "1521";
	private static final String service = "adms";
	private static final String username = "sch_sensor";
	private static final String password = "phe334";
	
	public static enum PredictionMethod {Historic, Filtered, Interpolated};
	public static PredictionMethod predictionMethod = PredictionMethod.Historic;
	public static final Double alpha = 0.5;
	
	public static DateFormat oracleDF = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
	public static DateFormat timeOfDayDF = new SimpleDateFormat("HH:mm:ss");
	
	private static String ttQueryTemplate = readQuery("QueryTemplates/ttQuery.sql");
	private static String ttCongQueryTemplate = readQuery("QueryTemplates/ttCongQuery.sql");
	private static String singleTTQueryTemplate = readQuery("QueryTemplates/singleTTQuery.sql");
	private static String congQueryTemplate = readQuery("QueryTemplates/congQuery.sql");
	
	public static OracleConnection getConnection()
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

	public static String readQuery(String fileName)
	{
		String query = new String();
		try
		{
			//File folder = new File("QueryTemplates");
			//File[] files = folder.listFiles();
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

	
	public static HashMap<Pair<Integer, Integer>, Double>  getCongestionChange(String pathNumber, String from1, String from2, 
			Calendar time, ArrayList<Integer> days) throws SQLException, ParseException{
		OracleConnection conn = getConnection();
		Statement stm = conn.createStatement();
		//String startTime = timeOfDayDF.format(RoundTimeDown((Calendar)time.clone()).getTime());
		//String endTime = timeOfDayDF.format(RoundTimeUp((Calendar)time.clone()).getTime());
		String query = congQueryTemplate
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM1##", from1)
				.replace("##FROM2##", from2);
				//.replace("##START_TIME##", startTime)
				//.replace("##END_TIME##", endTime);
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
		conn.close();
		HashMap<Pair<Integer, Integer>, Double> retValue = new HashMap<Pair<Integer,Integer>, Double>();
		Double falseTotal = (f2f+f2t == 0.0) ? 1.0 : f2f+f2t;
		Double trueTotal = (t2f+t2t == 0.0) ? 1.0 : t2f+t2t;
		retValue.put(new Pair<Integer, Integer>(0, 0),  f2f/falseTotal);
		retValue.put(new Pair<Integer, Integer>(0, 1), f2t/falseTotal);
		retValue.put(new Pair<Integer, Integer>(1, 0), t2f/trueTotal);
		retValue.put(new Pair<Integer, Integer>(1, 1), t2t/trueTotal);
		return retValue;
	}
	
	private static ArrayList<Double> getTravelTimes(String pathNumber, String from, Calendar time, 
			ArrayList<Integer> days, boolean cong) throws SQLException, ParseException {
		OracleConnection conn = getConnection();
		Statement stm = conn.createStatement();
		String startTime = timeOfDayDF.format(RoundTimeDown((Calendar)time.clone()).getTime());
		String endTime = timeOfDayDF.format(RoundTimeUp((Calendar)time.clone()).getTime());
		//SELECT TIME, TRAVEL_TIME FROM PATH#_EDGE_PATTERNS WHERE FROM, TO, START <= TOD AND END >= TOD
		String query = ttCongQueryTemplate
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime)
				.replace("##CONG##", cong ? "TRUE" : "FALSE");
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		int daysIdx = 0;
		while (ors.next()) {
			Calendar dayCal = Calendar.getInstance();
			dayCal.setTime(ors.getTimestamp(1));
			Integer day = dayCal.get(Calendar.DAY_OF_YEAR);
			while (days.get(daysIdx) < day) 
				if (daysIdx < days.size() - 1) 
					daysIdx++;
			if (days.get(daysIdx) == day)
				travelTimes.add(ors.getDouble(2));
		}
		ors.close();
		stm.close();
		conn.close();
		return travelTimes;
	}
	
	private static ArrayList<Double> getTravelTimes(String pathNumber, String from, Calendar time,
			ArrayList<Integer> days) throws SQLException, ParseException {
		OracleConnection conn = getConnection();
		Statement stm = conn.createStatement();
		String startTime = timeOfDayDF.format(RoundTimeDown((Calendar)time.clone()).getTime());
		String endTime = timeOfDayDF.format(RoundTimeUp((Calendar)time.clone()).getTime());
		//SELECT TIME, TRAVEL_TIME FROM PATH#_EDGE_PATTERNS WHERE FROM, TO, START <= TOD AND END >= TOD
		String query = ttQueryTemplate
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		int daysIdx = 0;
		while (ors.next()) {
			Calendar dayCal = Calendar.getInstance();
			dayCal.setTime(ors.getTimestamp(1));
			Integer day = dayCal.get(Calendar.DAY_OF_YEAR);
			while (days.get(daysIdx) < day) 
				if (daysIdx < days.size() - 1) 
					daysIdx++;
			if (days.get(daysIdx) == day)
				travelTimes.add(ors.getDouble(2));
		}
		ors.close();
		stm.close();
		conn.close();
		return travelTimes;
	}
	
	public static NormalDist getNormalDist(String pathNumber, String from, Calendar time,
			ArrayList<Integer> days) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = getTravelTimes(pathNumber, from, time, days);
		return new NormalDist(travelTimes);
	}
	
	public static PMF getPMF(String pathNumber, String from, Calendar time,
			ArrayList<Integer> days) throws SQLException, ParseException{
		ArrayList<Double> travelTimes = getTravelTimes(pathNumber, from, time, days);
		return new PMF(travelTimes);
	}
	
	public static PMF getPMF(String pathNumber, String from, Calendar time,
			ArrayList<Integer> days, boolean cong) throws SQLException, ParseException{
		ArrayList<Double> travelTimes = getTravelTimes(pathNumber, from, time, days, cong);
		return new PMF(travelTimes);
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

	public static int RoundDouble(double input) {
		int retValue = (int) input;
		if (input % 1 > 0.5) retValue++;
		return retValue;
	}


	public static Double GetActualTravelTime(String pathNumber, String from,
			Calendar startTime) throws SQLException{
		Double retValue = 0.0;
		OracleConnection conn = getConnection();
		
		String startTimeStr = oracleDF.format(RoundTimeDown((Calendar)startTime.clone()).getTime()); 
		Statement stm = conn.createStatement();
		String qeury = singleTTQueryTemplate
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTimeStr);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(qeury);
		if (ors.next()) retValue = ors.getDouble(1);
		ors.close();
		stm.close();
		conn.close();
		return retValue;
	}
	
	public static ArrayList<Integer> FilterDays(String pathNumber, ArrayList<Integer> days, String from, 
			Calendar startTimeStamp) throws SQLException, ParseException{
		OracleConnection conn = getConnection();
		String startTime = oracleDF.format(RoundTimeDown((Calendar)startTimeStamp.clone()).getTime()); 
		Statement stm = conn.createStatement();
		String query = singleTTQueryTemplate
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		Double startTravelTime = 1.0;
		if (ors.next()) startTravelTime = ors.getDouble(1);
		ors.close();
		
		startTime = timeOfDayDF.format(RoundTimeDown((Calendar)startTimeStamp.clone()).getTime());
		String endTime = timeOfDayDF.format(RoundTimeUp((Calendar)startTimeStamp.clone()).getTime());
		query = ttQueryTemplate
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime);
		ors = (OracleResultSet) stm.executeQuery(query);
		ArrayList<Integer> filteredDays = new ArrayList<Integer>();
		int daysIdx = 0;
		while (ors.next()) {
			Calendar dayCal = Calendar.getInstance();
			dayCal.setTime(ors.getTimestamp(1));
			Integer day = dayCal.get(Calendar.DAY_OF_YEAR);
			Double travelTime = ors.getDouble(2);
			while (days.get(daysIdx) < day) 
				if (daysIdx < days.size() - 1) 
					daysIdx++;
			if (days.get(daysIdx) == day)
				if (Math.abs(travelTime - startTravelTime) <= 0.3)
					filteredDays.add(day);
		}
		ors.close();
		stm.close();
		conn.close();
		return filteredDays;
	}
}
