package edu.imsc.UncertainRoadNetworks;
import java.util.ArrayList;
import java.util.Calendar;

public class Main {
	private static int[] targetDays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY}; 
	private static int startHour = 7, startMinute = 0;
	private static int k = 5; 
	
	public static void main(String[] args) {
		String path = "768701-774344-770599-768297-768283-770587-770012-770024-770036-770354-770048-770331-770544-770061-770556-770076-771202-770089-770103-771636-770475-770487-770116-769895-769880-769866-769847-768230-767610-767598-718076-767471-718072-767454-762329-767621-767573-718066-767542-718064-767495-718375-716955-718370-716949-760650-718045-718173-760643-760635-774671-718166-764037";
		String[] sensorList = path.split("-");
		int pathNum = 2;
		String pathNumber = Integer.toString(pathNum);
		try {
			Calendar initialStartTime = Calendar.getInstance();
			initialStartTime.setTime(Util.oracleDF.parse("01-JAN-13 00.00.00.0 AM"));
			initialStartTime.set(Calendar.HOUR_OF_DAY, startHour);
			initialStartTime.set(Calendar.MINUTE, startMinute);
			int dayOfWeek = initialStartTime.get(Calendar.DAY_OF_WEEK);
			while (!IsTargetDay(dayOfWeek)) {
				initialStartTime.add(Calendar.DAY_OF_YEAR, 1);
				dayOfWeek = initialStartTime.get(Calendar.DAY_OF_WEEK);
			}
			String timeOfDay = Util.timeOfDayDF.format(initialStartTime.getTime());
			StartTimeGenerator stg = new StartTimeGenerator(initialStartTime, k);
			ArrayList<ArrayList<Calendar>> allStartTimes = stg.GetStartTimes();
			Double score = 0.0;
			for (int i = 0; i < k; ++i) {
				@SuppressWarnings("unchecked")
				ArrayList<Calendar> testDays = (ArrayList<Calendar>) allStartTimes.get(i).clone();
				ArrayList<Integer> modelDays = new ArrayList<Integer>();
				for (int j = 0; j < k; ++j) 
					if (j != i) 
						for (Calendar day : allStartTimes.get(j))
							modelDays.add(day.get(Calendar.DAY_OF_YEAR));
				NormalDist modelDist;
				Double actualTime;
				switch (Util.predictionMethod) {
				case Historic:
					modelDist = Approach1.GenerateModel(pathNumber, sensorList, timeOfDay, modelDays, null);
					for (Calendar startTime : testDays) {
						actualTime = Approach1.GenerateActual(pathNumber, sensorList, (Calendar)startTime.clone());
						score += modelDist.GetScore(actualTime);
					}
					break;
				case Filtered:
					for (Calendar startTime : testDays) {
						ArrayList<Integer> filteredDays = Util.FilterDays(pathNumber, modelDays, sensorList[0], (Calendar)startTime.clone());
						modelDist = Approach1.GenerateModel(pathNumber, sensorList, timeOfDay, filteredDays, null);
						actualTime = Approach1.GenerateActual(pathNumber, sensorList, (Calendar)startTime.clone());
						score += modelDist.GetScore(actualTime);
					}
				case Interpolated:
					for (Calendar startTime : testDays) {
						modelDist = Approach1.GenerateModel(pathNumber, sensorList, timeOfDay, modelDays, (Calendar)startTime.clone());
						actualTime = Approach1.GenerateActual(pathNumber, sensorList, (Calendar)startTime.clone());
						score += modelDist.GetScore(actualTime);
					}
				default:
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean IsTargetDay(int dayOfWeek) {
		for (int targetDay : targetDays)
			if (dayOfWeek == targetDay) return true;
		return false;
	}
}
