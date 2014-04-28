package edu.imsc.UncertainRoadNetworks;

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

import oracle.jdbc.driver.OracleResultSet;

public class SpeedUp {

	private static DateFormat defaultDF = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.S");
	private static DateFormat oracleDF = new SimpleDateFormat(
			"dd-MMM-yy hh.mm.ss.SSS a");

	private static String startTime = "2013-01-11 06:00:00.0";
	private static int startHour = 6;
	private static String dayOfWeek = "fri";
	private static String endTime = "2014-01-01 00:00:00.0";
	private static int hoursPerDay = 12;
	private static int testInterval = 5;

	private static FileWriter gfw;
	private static BufferedWriter gbw;

	public static void main(String[] args) {

		try {
			gfw = new FileWriter("logs.txt");
			gbw = new BufferedWriter(gfw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ArrayList<String> results = InitializeResults();
		String[] sensorList = Util.path.split("-");

		Calendar endYearCal = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		try {
			// FileWriter fw = new FileWriter(String.format("fri_path%d.csv",
			// pathNumber));
			// BufferedWriter bw = new BufferedWriter(fw);
			endYearCal.setTime(defaultDF.parse(endTime));
			// endYearCal.setTime(defaultDF.parse("2013-01-09 00:00:00.0"));
			cal.setTime(defaultDF.parse(startTime));
			while (cal.before(endYearCal)) {
				Calendar endDayCal = Calendar.getInstance();
				endDayCal.setTime(cal.getTime());
				endDayCal.add(Calendar.HOUR_OF_DAY, hoursPerDay);
				ArrayList<Calendar> startTimes = new ArrayList<Calendar>();
				while (cal.before(endDayCal)) {
					startTimes.add((Calendar) cal.clone());
					gbw.write(defaultDF.format(cal.getTime()));
					gbw.write("\n");
					cal.add(Calendar.MINUTE, 5);
				}
				ArrayList<Calendar> endTimes = TravelTime(sensorList,
						startTimes);

				for (int i = 0; i < startTimes.size(); ++i) {
					long travelTimeInMillis = endTimes.get(i).getTimeInMillis()
							- startTimes.get(i).getTimeInMillis();
					double travelTimeInMinutes = (double) (travelTimeInMillis)
							/ (1000 * 60);
					// bw.write(String.format("%s,%f\n",
					// defaultDF.format(startTimes.get(i).getTime()),
					// travelTimeInMinutes));
					// gbw.write(String.format("Travel Time for %s: %f\n",
					// defaultDF.format(startTimes.get(i).getTime()),
					// travelTimeInMinutes));
					String old = results.get(i);
					results.set(i,
							old + String.format(",%f", travelTimeInMinutes));
				}
				// bw.write("\n");
				cal.add(Calendar.DAY_OF_YEAR, 7);
				cal.set(Calendar.HOUR_OF_DAY, startHour);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
			}
			// bw.close();
			// fw.close();
			FileWriter fw = new FileWriter(String.format("%s_path%s.csv",
					dayOfWeek, Util.pathNumber));
			BufferedWriter bw = new BufferedWriter(fw);
			for (String result : results) {
				bw.write(result);
				bw.write("\n");
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			gbw.close();
			gfw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static double ToMinutes(Calendar start, Calendar end) {
		long travelTimeInMillis = end.getTimeInMillis()
				- start.getTimeInMillis();
		return (double) (travelTimeInMillis) / (1000 * 60);
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
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return results;
	}

	public static ArrayList<Double> TimeInependentTravelTime(
			String[] sensorList, ArrayList<Calendar> startTimes)
			throws SQLException, ParseException {
		ArrayList<Double> retTimes = new ArrayList<Double>();

		HashMap<String, Double> avgTravelTimes = new HashMap<String, Double>();
		/*Statement avgStm = Util.conn.createStatement();
		// select From, AVG(Travel_Time) From Path#_Edge_Patterns Group By From;
		String avgQuery = QueryTemplates.avgTravleTime
				.replace("##PATH_NUM##", Util.pathNumber);
		OracleResultSet avgOrs = (OracleResultSet) avgStm
				.executeQuery(avgQuery);
		while (avgOrs.next()) {
			String from = avgOrs.getString(1);
			Double tt = avgOrs.getDouble(2);
			avgTravelTimes.put(from, tt);
		}
		avgOrs.close();
		avgStm.close();*/

		for (Calendar startTime : startTimes) {
			Statement avgStm = Util.conn.createStatement();
			// select From, AVG(Travel_Time) From Path#_Edge_Patterns Group By From;
			String avgStartTime = Util.timeOfDayDF.format(Util.RoundTimeDown((Calendar)startTime.clone()).getTime());
			String avgEndTime = Util.timeOfDayDF.format(Util.RoundTimeUp((Calendar)startTime.clone()).getTime());
			String avgQuery = QueryTemplates.avgTravleTime
					.replace("##PATH_NUM##", Util.pathNumber)
					.replace("##START_TIME##", avgStartTime)
					.replace("##END_TIME##", avgEndTime);
			OracleResultSet avgOrs = (OracleResultSet) avgStm
					.executeQuery(avgQuery);
			while (avgOrs.next()) {
				String from = avgOrs.getString(1);
				Double tt = avgOrs.getDouble(2);
				avgTravelTimes.put(from, tt);
			}
			avgOrs.close();
			avgStm.close();
			
			@SuppressWarnings("unchecked")
			HashMap<String, Double> travelTimes = (HashMap<String, Double>) avgTravelTimes.clone();
			Statement stm = Util.conn.createStatement();
			// select From, TravelTime From PathN_Edge_Patterns where time >=
			// start_time and time <= end_time;
			String qStartTime = Util.oracleDF.format(Util.RoundTimeDown(
					(Calendar) startTime.clone()).getTime());
			String qEndTime = Util.oracleDF.format(Util.RoundTimeUp(
					(Calendar) startTime.clone()).getTime());
			String query = QueryTemplates.timeInDepTravelTime
					.replace("##PATH_NUM##", Util.pathNumber)
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
			retTimes.add(sum);
		}
		return retTimes;
	}

	public static ArrayList<Calendar> SingleDayTimeDependantTravelTime(
			String[] sensorList, ArrayList<Calendar> startTimes)
			throws SQLException, ParseException {
		ArrayList<Calendar> currentTimes = new ArrayList<Calendar>();
		for (Calendar cal : startTimes)
			currentTimes.add((Calendar) cal.clone());

		Calendar lbTime = Calendar.getInstance();
		lbTime.setTime(startTimes.get(0).getTime());
		lbTime.add(Calendar.YEAR, -1);
		Calendar ubTime = Calendar.getInstance();
		ubTime.setTime(startTimes.get(0).getTime());
		ubTime.add(Calendar.YEAR, 1);

		for (int s = 0; s < sensorList.length - 1; ++s) {
			String from = sensorList[s];
			String to = sensorList[s + 1];

			Double avgTravelTimes = 1.0;
			Statement avgStm = Util.conn.createStatement();
			// select AVG(Travel_Time) From Path#_Edge_Patterns WHERE "FROM" =
			// FROM;
			String avgQuery = QueryTemplates.avgLinkTravleTime
					.replace("##PATH_NUM##", Util.pathNumber)
					.replace("##FROM##", from);
			OracleResultSet avgOrs = (OracleResultSet) avgStm
					.executeQuery(avgQuery);
			while (avgOrs.next()) {
				avgTravelTimes = avgOrs.getDouble(1);
			}
			avgOrs.close();
			avgStm.close();

			// select Time, TravelTime from Path#_Edge_Patterns where time >=
			// start_time and time < end_time and from = from and to = to order
			// by time;
			String startTime = Util.oracleDF.format(Util.RoundTimeDown(
					(Calendar) currentTimes.get(0).clone()).getTime());
			String endTime = Util.oracleDF.format(Util.RoundTimeUp(
					(Calendar) currentTimes.get(currentTimes.size() - 1)
							.clone()).getTime());
			String query = QueryTemplates.timeDepTravelTime
					.replace("##PATH_NUM##", Util.pathNumber)
					.replace("##FROM##", from).replace("##TO##", to)
					.replace("##START_TIME##", startTime)
					.replace("##END_TIME##", endTime);
			Statement stm = Util.conn.createStatement();
			OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
			ArrayList<Pair<Calendar, Double>> travelTimes = new ArrayList<Pair<Calendar, Double>>();
			travelTimes.add(new Pair<Calendar, Double>(lbTime, avgTravelTimes));
			while (ors.next()) {
				Calendar time = Calendar.getInstance();
				time.setTime(ors.getTimestamp(1));
				Double tt = ors.getDouble(2);
				travelTimes.add(new Pair<Calendar, Double>(time, tt));
			}
			ors.close();
			stm.close();
			travelTimes.add(new Pair<Calendar, Double>(ubTime, avgTravelTimes));

			int p = 0;
			for (int i = 0; i < currentTimes.size(); ++i) {
				if (!currentTimes.get(i).before(
						travelTimes.get(p + 1).getFirst())) {
					p++;
				}
				Double seconds = travelTimes.get(p).getSecond() * 60.0;
				currentTimes.get(i).add(Calendar.SECOND, seconds.intValue());
			}
		}
		return currentTimes;
	}

	public static ArrayList<Double> TimeDependentTravelTime(
			String[] sensorList, ArrayList<Calendar> startTimes)
			throws SQLException, ParseException {
		ArrayList<Double> retTimes = new ArrayList<Double>();
		for (Calendar startTime : startTimes) {
			ArrayList<Calendar> temp = new ArrayList<Calendar>();
			temp.add((Calendar) startTime.clone());
			Calendar endTime = SingleDayTimeDependantTravelTime(sensorList, temp).get(0);
			retTimes.add(ToMinutes((Calendar)startTime.clone(), endTime));
		}
		return retTimes;
	}

	public static ArrayList<Calendar> TravelTime(String[] sensorList,
			ArrayList<Calendar> startTimes) throws SQLException, ParseException {
		ArrayList<Calendar> currentTimes = new ArrayList<Calendar>();
		for (Calendar cal : startTimes)
			currentTimes.add((Calendar) cal.clone());

		for (int sensor = 0; sensor < sensorList.length - 1; ++sensor) {
			String fromSensor = sensorList[sensor];
			String toSensor = sensorList[sensor + 1];
			double distance = -1;
			Statement distStm = Util.conn.createStatement();
			String distQuery = QueryTemplates.edgeDistanceQuery
					.replace("##PATH_NUM##", Util.pathNumber)
					.replace("##FROM##", fromSensor)
					.replace("##TO##", toSensor);
			OracleResultSet distOrs = (OracleResultSet) distStm
					.executeQuery(distQuery);
			if (distOrs.next())
				distance = distOrs.getDouble(1);
			distOrs.close();
			distStm.close();
			// gbw.write(String.format("From: %s\tTo: %s\tDistance: %f\n",
			// fromSensor, toSensor, distance));
			String startTime = oracleDF.format(Util.RoundTimeDown(
					(Calendar) currentTimes.get(0).clone()).getTime());
			String endTime = oracleDF.format(Util.RoundTimeUp(
					(Calendar) currentTimes.get(currentTimes.size() - 1)
							.clone()).getTime());
			Calendar lbTime = Calendar.getInstance();
			lbTime.setTime(oracleDF.parse(startTime));
			lbTime.add(Calendar.YEAR, -1);
			Calendar ubTime = Calendar.getInstance();
			ubTime.setTime(oracleDF.parse(endTime));
			ubTime.add(Calendar.YEAR, 1);

			double avgFromSp = 45, avgToSp = 45;

			String spQueryTemplate = QueryTemplates.sensorSpeedsQuery
					.replace("##START_TIME##", startTime)
					.replace("##END_TIME##", endTime);
			String avgSpQueryTemplate = QueryTemplates.sensorAvgSpeedQuery
					.replace("##START_TIME##", startTime)
					.replace("##END_TIME##", endTime);

			Statement fromSpStm = Util.conn.createStatement();
			String fromSpQuery = spQueryTemplate.replace("##LINK_ID##",
					fromSensor);
			OracleResultSet fromOrs = (OracleResultSet) fromSpStm
					.executeQuery(fromSpQuery);
			ArrayList<Pair<Calendar, Double>> fromRes = new ArrayList<Pair<Calendar, Double>>();
			fromRes.add(new Pair<Calendar, Double>(lbTime, -1.0));
			while (fromOrs.next()) {
				Calendar time = Calendar.getInstance();
				time.setTime(fromOrs.getTimestamp(1));
				double speed = fromOrs.getDouble(2);
				fromRes.add(new Pair<Calendar, Double>(time, speed));
			}
			fromRes.add(new Pair<Calendar, Double>(ubTime, -1.0));
			String fromAvgSpQuery = avgSpQueryTemplate.replace("##LINK_ID##",
					fromSensor);
			fromOrs = (OracleResultSet) fromSpStm.executeQuery(fromAvgSpQuery);
			if (fromOrs.next())
				avgFromSp = fromOrs.getDouble(1);
			if (avgFromSp == 0)
				avgFromSp = 45;
			fromOrs.close();
			fromSpStm.close();

			Statement toSpStm = Util.conn.createStatement();
			String toSpQuery = spQueryTemplate.replace("##LINK_ID##", toSensor);
			OracleResultSet toOrs = (OracleResultSet) toSpStm
					.executeQuery(toSpQuery);
			ArrayList<Pair<Calendar, Double>> toRes = new ArrayList<Pair<Calendar, Double>>();
			toRes.add(new Pair<Calendar, Double>(lbTime, -1.0));
			while (toOrs.next()) {
				Calendar time = Calendar.getInstance();
				time.setTime(toOrs.getTimestamp(1));
				double speed = toOrs.getDouble(2);
				toRes.add(new Pair<Calendar, Double>(time, speed));
			}
			toRes.add(new Pair<Calendar, Double>(ubTime, -1.0));
			String toAvgSpQuery = avgSpQueryTemplate.replace("##LINK_ID##",
					toSensor);
			toOrs = (OracleResultSet) toSpStm.executeQuery(toAvgSpQuery);
			if (toOrs.next())
				avgToSp = toOrs.getDouble(1);
			if (avgToSp == 0)
				avgToSp = 45;
			toOrs.close();
			toSpStm.close();

			int fromIndex = 0;
			int toIndex = 0;
			double fromSp, toSp;

			ArrayList<Integer> tt = new ArrayList<Integer>();
			// int j = -1;
			for (Calendar currentTime : currentTimes) {
				// j++;
				// gbw.write(String.format("CurrentTime[%d] = %s\n", j,
				// oracleDF.format(currentTime.getTime())));
				if (!currentTime.before(fromRes.get(fromIndex + 1).getFirst())) {
					fromIndex++;
				}
				if (!currentTime.before(toRes.get(toIndex + 1).getFirst())) {
					toIndex++;
				}
				fromSp = fromRes.get(fromIndex).getSecond();
				fromSp = (fromSp <= 0) ? avgFromSp : fromSp;
				toSp = toRes.get(toIndex).getSecond();
				toSp = (toSp <= 0) ? avgToSp : toSp;
				double speed = (fromSp + toSp) / 2;
				int seconds = (int) ((distance / speed) * 3600);
				tt.add(seconds);
				// gbw.write(String.format("from: %f\tto: %f\tspeed: %f\tdistance= %f\ttime: %d\n",
				// fromSp, toSp, speed, distance, seconds));
			}
			for (int i = 0; i < currentTimes.size(); ++i) {
				// gbw.write(String.format("CurrentTime[%d] = %s\ttt[%d] = %d\n",
				// i, oracleDF.format(currentTimes.get(i).getTime()), i,
				// tt.get(i)));
				currentTimes.get(i).add(Calendar.SECOND, tt.get(i));
				// gbw.write(String.format("CurrentTime[%d] = %s\n", i,
				// oracleDF.format(currentTimes.get(i).getTime())));
			}
		}
		return currentTimes;
	}

}
