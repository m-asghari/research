import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;


public class SpeedUp {
	
	private static DateFormat defaultDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	private static DateFormat oracleDF = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
	
	private static String startTime = "2013-01-11 06:00:00.0";
	private static int startHour = 6;
	private static String dayOfWeek = "fri";
	private static String endTime = "2014-01-01 00:00:00.0";
	private static int hoursPerDay = 12;
	private static int testInterval = 5;
	
	private static String edgeDistanceQueryTemplate = Util.readQuery("QueryTemplates\\SelectEdgeDistance.sql");
	private static String sensorSpeedsQueryTemplate = Util.readQuery("QueryTemplates\\SelectSensorSpeeds.sql");
	private static String sensorAvgSpeedQueryTemplate = Util.readQuery("QueryTemplates\\SelectSensorAvgSpeed.sql");
	private static String timeInDepTravelTimeTemplate = Util.readQuery("QueryTemplates\\SelectTimeIndepTT.sql");
	private static String timeDepTravelTimeTemplate = Util.readQuery("QueryTemplates\\SelectTimeDepTT.sql");
	private static String avgTravleTimeTemplate = Util.readQuery("QueryTemplates\\SelectAvgTT.sql");
	
	private static FileWriter gfw;
	private static BufferedWriter gbw;

	public static void main(String[] args) {
		
		String[] paths = {"768701-774344-718405-759858-759850-759835-759844-759822-718393-718392-717006-715996-716573-717613-716571-717610-717608-764101-768066-717490-717489-717488-764766-717486-769405-769403-717484-769388-717481-769373-717472-717468-717466-717462-717461-717458-716339-717453-717450-717446-716331-716328-764853-760643-760635-774671-718166",
				"768701-774344-770599-768297-768283-770587-770012-770024-770036-770354-770048-770331-770544-770061-770556-770076-771202-770089-770103-770475-770487-770116-769895-769880-769866-769847-767610-767598-718076-767471-718072-767454-762329-767621-767573-718066-767542-718064-767495-716955-716949-760650-718045-760643-760635-774671-718166"};
		
		//String[] paths = {"768701-774344-718405-759858-759850-759835-759844-759822-718393-718392-717006-715996-716573-717613-716571-717610-717608-764101-768066-717490-717489-717488-764766-717486-769405-769403-717484-769388-717481-769373-717472-717468-717466-717462-717461-717458-716339-717453-717450-717446-716331-716328-764853-760643-760635-774671-718166"};
		try{
			gfw = new FileWriter("logs.txt");
			gbw = new BufferedWriter(gfw);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		int pathNumber = 0;
		for (String path : paths) {
			pathNumber++;
			String pathNum = Integer.toString(pathNumber);
			ArrayList<String> results = InitializeResults();
			String[] sensorList = path.split("-");
			
			Calendar endYearCal = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();
			try {
				DataPreparation.ClearPathSensorTable(pathNum);
				DataPreparation.PopulatePathSensorTable(pathNum, sensorList);
				DataPreparation.ClearPathEdgeTable(pathNum);
				DataPreparation.PopulatePathEdgeTable(pathNum, sensorList.length);
				//FileWriter fw = new FileWriter(String.format("fri_path%d.csv", pathNumber));
				//BufferedWriter bw = new BufferedWriter(fw);
				endYearCal.setTime(defaultDF.parse(endTime));
				//endYearCal.setTime(defaultDF.parse("2013-01-09 00:00:00.0"));
				cal.setTime(defaultDF.parse(startTime));
				while(cal.before(endYearCal)) {
					Calendar endDayCal = Calendar.getInstance();
					endDayCal.setTime(cal.getTime());
					endDayCal.add(Calendar.HOUR_OF_DAY, hoursPerDay);
					ArrayList<Calendar> startTimes = new ArrayList<Calendar>();
					while (cal.before(endDayCal)) {
						startTimes.add((Calendar)cal.clone());
						gbw.write(defaultDF.format(cal.getTime()));
						gbw.write("\n");
						cal.add(Calendar.MINUTE, 5);
					}
					ArrayList<Calendar> endTimes = TravelTime(pathNum, sensorList, startTimes);
					
					for(int i = 0; i < startTimes.size(); ++i) {
						long travelTimeInMillis = endTimes.get(i).getTimeInMillis() - startTimes.get(i).getTimeInMillis();
						double travelTimeInMinutes = (double)(travelTimeInMillis)/(1000*60);
						//bw.write(String.format("%s,%f\n", defaultDF.format(startTimes.get(i).getTime()), travelTimeInMinutes));
						//gbw.write(String.format("Travel Time for %s: %f\n", defaultDF.format(startTimes.get(i).getTime()), travelTimeInMinutes));
						String old = results.get(i);
						results.set(i, old + String.format(",%f", travelTimeInMinutes));
					}
					//bw.write("\n");
					cal.add(Calendar.DAY_OF_YEAR, 7);
					cal.set(Calendar.HOUR_OF_DAY, startHour);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
				}
				//bw.close();
				//fw.close();
				FileWriter fw = new FileWriter(String.format("%s_path%d.csv", dayOfWeek, pathNumber));
				BufferedWriter bw = new BufferedWriter(fw);
				for (String result : results) {					
					bw.write(result);
					bw.write("\n");
				}
				bw.close();
				fw.close();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		try {
			gbw.close();
			gfw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double ToMinutes(Calendar start, Calendar end) {
		long travelTimeInMillis = end.getTimeInMillis() - start.getTimeInMillis();
		return (double)(travelTimeInMillis)/(1000*60);		
	}

	private static ArrayList<String> InitializeResults() {
		ArrayList<String> results = new ArrayList<String>();
		Calendar resCal = Calendar.getInstance();
		Calendar resEndDay = Calendar.getInstance();
		try {
			resCal.setTime(defaultDF.parse(startTime));
			resEndDay.setTime(resCal.getTime());
			resEndDay.add(Calendar.HOUR_OF_DAY, hoursPerDay);
			while (resCal.before(resEndDay)) {
				results.add(defaultDF.format(resCal.getTime()));
				resCal.add(Calendar.MINUTE, testInterval);
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return results;
	}

	public static ArrayList<Calendar> TimeInependentTravelTime(String pathNumber, 
			String[] sensorList, ArrayList<Calendar> startTimes) throws SQLException, ParseException{
		OracleConnection conn = Util.getConnection();
		ArrayList<Calendar> retTimes = new ArrayList<Calendar>();
		for (Calendar cal : startTimes)
			retTimes.add((Calendar)cal.clone());
		
		HashMap<String, Double> avgTravelTimes = new HashMap<String, Double>();
		Statement avgStm = conn.createStatement();
		//select From, AVG(Travel_Time) From Path#_Edge_Patterns Group By From;
		String avgQuery = avgTravleTimeTemplate
				.replace("##PATH_NUM##", pathNumber);
		OracleResultSet avgOrs = (OracleResultSet) avgStm.executeQuery(avgQuery);
		while (avgOrs.next()) {
			String from = avgOrs.getString(1);
			Double tt = avgOrs.getDouble(2);
			avgTravelTimes.put(from, tt);
		}
		
		for (Calendar startTime : startTimes) {
			@SuppressWarnings("unchecked")
			HashMap<String, Double> travelTimes = (HashMap<String, Double>) avgTravelTimes.clone();
			Statement stm = conn.createStatement();
			//select From, TravelTime From PathN_Edge_Patterns where time >= start_time and time <= end_time;
			String qStartTime = Util.oracleDF.format(Util.RoundTimeDown((Calendar)startTime.clone()));
			String qEndTime = Util.oracleDF.format(Util.RoundTimeUp((Calendar)startTime.clone()));
			String query = timeInDepTravelTimeTemplate
					.replace("##PATH_NUM##", pathNumber)
					.replace("##START_TIME##", qStartTime)
					.replace("##END_TIME##", qEndTime);
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
			while (ors.next()) {
				String from = ors.getString(1);
				Double tt = ors.getDouble(2);
				travelTimes.put(from, tt);
			}
			ors.close();
			stm.close();
			
			Double sum = 0.0;
			for (Map.Entry<String, Double> e : travelTimes.entrySet())
				sum += e.getValue();
			Calendar endTime = Calendar.getInstance();
			endTime.setTime(startTime.getTime());
			endTime.add(Calendar.MINUTE, sum.intValue());
			retTimes.add(endTime);
		}
		conn.close();
		return retTimes;
	}
	
	public static ArrayList<Calendar> SingleDayTimeDependantTravelTime(String pathNumber,
			String[] sensorList, ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		OracleConnection conn = Util.getConnection();
		ArrayList<Calendar> currentTimes = new ArrayList<Calendar>();
		for (Calendar cal : startTimes)
			currentTimes.add((Calendar)cal.clone());
		
		Calendar lbTime = Calendar.getInstance();
		lbTime.setTime(startTimes.get(0).getTime());
		lbTime.add(Calendar.YEAR, -1);
		Calendar ubTime = Calendar.getInstance();
		ubTime.setTime(startTimes.get(0).getTime());
		ubTime.add(Calendar.YEAR, 1);
		
		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			String to = sensorList[s+1];
			
			//select Time, TravelTime from Path#_Edge_Patterns where time >= start_time and time < end_time and from = from and to = to order by time;
			String startTime = Util.oracleDF.format(Util.RoundTimeDown((Calendar)currentTimes.get(0).clone()));
			String endTime = Util.oracleDF.format(Util.RoundTimeUp((Calendar)currentTimes.get(currentTimes.size()-1).clone()));
			String query = timeDepTravelTimeTemplate
					.replace("##PATH_NUM##", pathNumber)
					.replace("##FROM##", from)
					.replace("##TO##", to)
					.replace("##START_TIME##", startTime)
					.replace("##END_TIME##", endTime);
			Statement stm = conn.createStatement();
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
			ArrayList<Pair<Calendar, Double>> travelTimes = new ArrayList<Pair<Calendar,Double>>();
			travelTimes.add(new Pair<Calendar, Double>(lbTime, 45.0));
			while (ors.next()) {
				Calendar time = Calendar.getInstance();
				time.setTime(ors.getTimestamp(1));
				Double tt = ors.getDouble(2);
				travelTimes.add(new Pair<Calendar, Double>(time, tt));
			}
			travelTimes.add(new Pair<Calendar, Double>(ubTime, 45.0));
			
			int p = 0;
			for (int i = 0; i < currentTimes.size(); ++i) {
				if (!currentTimes.get(i).before(travelTimes.get(p+1).getFirst())) {
					p++;
				}
				currentTimes.get(i).add(Calendar.SECOND, travelTimes.get(p).getSecond().intValue());
			}
		}
		conn.close();
		return currentTimes;
	}
	
	public static ArrayList<Calendar> TimeDependentTravelTime(String pathNumber,
			String[] sensorList, ArrayList<Calendar> startTimes) throws SQLException, ParseException{
		ArrayList<Calendar> endTimes = new ArrayList<Calendar>();
		for (Calendar startTime : startTimes) {
			ArrayList<Calendar> temp = new ArrayList<Calendar>();
			temp.add((Calendar)startTime.clone());
			endTimes.add(SingleDayTimeDependantTravelTime(pathNumber, sensorList, temp).get(0));
		}
		return endTimes;
	}
	
	public static ArrayList<Calendar> TravelTime(String pathNumber, String[] sensorList,
			ArrayList<Calendar> startTimes) throws SQLException, ParseException{
		OracleConnection conn = Util.getConnection();
		ArrayList<Calendar> currentTimes = new ArrayList<Calendar>();
		for (Calendar cal : startTimes)
			currentTimes.add((Calendar)cal.clone());
		
		for (int sensor = 0; sensor < sensorList.length - 1; ++sensor) {
			String fromSensor = sensorList[sensor];
			String toSensor = sensorList[sensor + 1];
			double distance = -1;
			Statement distStm = conn.createStatement();
			String distQuery = edgeDistanceQueryTemplate
					.replace("##PATH_NUM##", pathNumber)
					.replace("##FROM##", fromSensor)
					.replace("##TO##", toSensor);
			OracleResultSet distOrs = (OracleResultSet) distStm.executeQuery(distQuery);
			if (distOrs.next()) distance = distOrs.getDouble(1);
			distOrs.close();
			distStm.close();
			//gbw.write(String.format("From: %s\tTo: %s\tDistance: %f\n", fromSensor, toSensor, distance));
			String startTime = oracleDF.format(Util.RoundTimeDown((Calendar)currentTimes.get(0).clone()).getTime());
			String endTime = oracleDF.format(Util.RoundTimeUp((Calendar)currentTimes.get(currentTimes.size() - 1).clone()).getTime());
			Calendar lbTime = Calendar.getInstance();
			lbTime.setTime(oracleDF.parse(startTime));
			lbTime.add(Calendar.YEAR, -1);
			Calendar ubTime = Calendar.getInstance();
			ubTime.setTime(oracleDF.parse(endTime));
			ubTime.add(Calendar.YEAR, 1);
			
			double avgFromSp = 45, avgToSp = 45;
			
			String spQueryTemplate = sensorSpeedsQueryTemplate
					.replace("##START_TIME##", startTime)
					.replace("##END_TIME##", endTime);
			String avgSpQueryTemplate = sensorAvgSpeedQueryTemplate
					.replace("##START_TIME##", startTime)
					.replace("##END_TIME##", endTime);
			
			Statement fromSpStm = conn.createStatement();
			String fromSpQuery = spQueryTemplate.replace("##LINK_ID##", fromSensor);
			OracleResultSet fromOrs = (OracleResultSet) fromSpStm.executeQuery(fromSpQuery);
			ArrayList<Pair<Calendar, Double>> fromRes = new ArrayList<Pair<Calendar,Double>>();
			fromRes.add(new Pair<Calendar, Double>(lbTime, -1.0));
			while (fromOrs.next()) {
				Calendar time = Calendar.getInstance();
				time.setTime(fromOrs.getTimestamp(1));
				double speed = fromOrs.getDouble(2);
				fromRes.add(new Pair<Calendar, Double>(time, speed));
			}
			fromRes.add(new Pair<Calendar, Double>(ubTime, -1.0));
			String fromAvgSpQuery = avgSpQueryTemplate.replace("##LINK_ID##", fromSensor);
			fromOrs = (OracleResultSet) fromSpStm.executeQuery(fromAvgSpQuery);
			if (fromOrs.next()) avgFromSp = fromOrs.getDouble(1);
			if (avgFromSp == 0) avgFromSp = 45;
			fromOrs.close();
			fromSpStm.close();
			
			Statement toSpStm = conn.createStatement();
			String toSpQuery = spQueryTemplate.replace("##LINK_ID##", toSensor);
			OracleResultSet toOrs = (OracleResultSet) toSpStm.executeQuery(toSpQuery);
			ArrayList<Pair<Calendar, Double>> toRes = new ArrayList<Pair<Calendar,Double>>();
			toRes.add(new Pair<Calendar, Double>(lbTime, -1.0));
			while (toOrs.next()) {
				Calendar time = Calendar.getInstance();
				time.setTime(toOrs.getTimestamp(1));
				double speed = toOrs.getDouble(2);
				toRes.add(new Pair<Calendar, Double>(time, speed));
			}
			toRes.add(new Pair<Calendar, Double>(ubTime, -1.0));
			String toAvgSpQuery = avgSpQueryTemplate.replace("##LINK_ID##", toSensor);
			toOrs = (OracleResultSet) toSpStm.executeQuery(toAvgSpQuery);
			if (toOrs.next()) avgToSp = toOrs.getDouble(1);
			if (avgToSp == 0) avgToSp = 45;
			toOrs.close();
			toSpStm.close();
			
			int fromIndex = 0;
			int toIndex = 0;
			double fromSp, toSp;
			
			ArrayList<Integer> tt = new ArrayList<Integer>();
			//int j = -1;
			for (Calendar currentTime : currentTimes) {
				//j++;
				//gbw.write(String.format("CurrentTime[%d] = %s\n", j, oracleDF.format(currentTime.getTime())));
				if (!currentTime.before(fromRes.get(fromIndex+1).getFirst())) {
					fromIndex++;
				}
				if (!currentTime.before(toRes.get(toIndex+1).getFirst())) {
					toIndex++;					
				}
				fromSp = fromRes.get(fromIndex).getSecond(); fromSp = (fromSp <= 0) ? avgFromSp : fromSp;
				toSp = toRes.get(toIndex).getSecond(); toSp = (toSp <= 0) ? avgToSp : toSp;
				double speed = (fromSp + toSp) / 2;
				int seconds = (int)((distance/speed)*3600);
				tt.add(seconds);
				//gbw.write(String.format("from: %f\tto: %f\tspeed: %f\tdistance= %f\ttime: %d\n", fromSp, toSp, speed, distance, seconds));				
			}
			for (int i = 0; i < currentTimes.size(); ++i) {
				//gbw.write(String.format("CurrentTime[%d] = %s\ttt[%d] = %d\n", i, oracleDF.format(currentTimes.get(i).getTime()), i, tt.get(i)));
				currentTimes.get(i).add(Calendar.SECOND, tt.get(i));
				//gbw.write(String.format("CurrentTime[%d] = %s\n", i, oracleDF.format(currentTimes.get(i).getTime())));
			}
		}
		conn.close();
		return currentTimes;
	}

}
