package edu.imsc.UncertainRoadNetworks;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

public class Main {
	private static int[] targetDays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY}; 
	private static int startHour = 9, startMinute = 0;
	private static int k = 5; 
	
	public static void main(String[] args) {
		Util.Initialize();
		String[] sensorList = Util.path.split("-");
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
			PredictionMethod[] values = new PredictionMethod[] {PredictionMethod.Historic, PredictionMethod.Filtered, PredictionMethod.Interpolated};
			for (PredictionMethod predictionMethod : values ) {
				Util.predictionMethod = predictionMethod;
				FileWriter fw = new FileWriter(String.format("Approach2_%s.txt", predictionMethod.toString()));
				BufferedWriter bw = new BufferedWriter(fw);
				Double totalScore = 0.0;
				int totalCount = 0;
				for (int i = 0; i < k; ++i) {
					@SuppressWarnings("unchecked")
					ArrayList<Calendar> testDays = (ArrayList<Calendar>) allStartTimes.get(i).clone();
					ArrayList<Integer> modelDays = new ArrayList<Integer>();
					for (int j = 0; j < allStartTimes.get(0).size(); ++j) {
						for (int l = 0; l < k; ++l) {
							if (j != i)
								if (j < allStartTimes.get(l).size())
									modelDays.add(allStartTimes.get(l).get(j).get(Calendar.DAY_OF_YEAR));
						}
					}
					PMF modelDist;
					Double actualTime;
					switch (Util.predictionMethod) {
					case Historic:
						modelDist = Approach2.GenerateModel(sensorList, timeOfDay, modelDays, null);
						for (Calendar startTime : testDays) {
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)startTime.clone());
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
							System.out.println(Approach2.GetResults((Calendar)startTime.clone(), actualTime, score));
							bw.write(Approach2.GetResults((Calendar)startTime.clone(), actualTime, score));
							bw.write("\n");
						}
						break;
					case Filtered:
						for (Calendar startTime : testDays) {
							ArrayList<Integer> filteredDays = Util.FilterDays(modelDays, sensorList[0], (Calendar)startTime.clone());
							if (filteredDays.size() > 0)
								modelDist = Approach2.GenerateModel(sensorList, timeOfDay, filteredDays, null);
							else
								modelDist = Approach2.GenerateModel(sensorList, timeOfDay, modelDays, null);
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)startTime.clone());
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
							System.out.println(Approach2.GetResults((Calendar)startTime.clone(), actualTime, score));
							bw.write(Approach2.GetResults((Calendar)startTime.clone(), actualTime, score));
							bw.write("\n");
						}
					case Interpolated:
						for (Calendar startTime : testDays) {
							modelDist = Approach2.GenerateModel(sensorList, timeOfDay, modelDays, (Calendar)startTime.clone());
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)startTime.clone());
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
							System.out.println(Approach2.GetResults((Calendar)startTime.clone(), actualTime, score));
							bw.write(Approach2.GetResults((Calendar)startTime.clone(), actualTime, score));
							bw.write("\n");
						}
					default:
						break;
					}
				}
				System.out.println(String.format("\n\nTotal Score for Approach2 %s: %f\n\n", predictionMethod, totalScore/totalCount));
				bw.write(String.format("\n\nTotal Score for Approach2 %s: %f\n\n", predictionMethod, totalScore/totalCount));
				bw.close();
				fw.close();
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
