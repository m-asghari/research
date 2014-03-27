package edu.imsc.UncertainRoadNetworks;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

public class Main {
	private static int[] targetDays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY}; 
	private static int startMinute = 0;
	private static int k = 5; 
	
	public static HashMap<String, ArrayList<Double>> results;
	
	public static void main(String[] args) {
		try {
			FileReader fr = new FileReader("links.txt");
			BufferedReader br = new BufferedReader(fr);
			results = new HashMap<String, ArrayList<Double>>();
			String link = "";
			int pathN = 0;
			while ((link = br.readLine()) != null) {
				pathN++;
				if (pathN == 7)
					System.out.println("hello");
				Util.path = link;
				Util.pathNumber = Integer.toString(pathN);
				results.put(Util.path, new ArrayList<Double>());
				int[] startHours = new int[] {8, 11, 14, 17, 20};
				for (int startHour : startHours) {
					double[] simThresholds = new double[] {0.2, 0.6, 1.0};
					for (double similarity : simThresholds) {
						Util.similarityThreshold = similarity;
						RunExperiment(startHour);
						System.out.println(String.format("Finished Path%d at startHour %d for similarity %f with score %f", pathN, startHour, similarity, results.get(link).get(results.get(link).size()-1)));
					}
				}
				WriteResultsToFile(results);
			}
			br.close();
			fr.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void WriteResultsToFile(HashMap<String, ArrayList<Double>> results){
		try {
			FileWriter fw = new FileWriter("results_links_similarPattern_continuous2.csv");
			BufferedWriter bw = new BufferedWriter(fw);
			for (Entry<String, ArrayList<Double>> e : results.entrySet()) {
				bw.write(e.getKey()+",");
				for (Double d : e.getValue()) {
					bw.write(Double.toString(d) + ",");
				}
				bw.write("\n");
			}
			bw.close();
			fw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void RunExperiment(int startHour) {
		//Util.Initialize();
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
			PredictionMethod[] values = new PredictionMethod[] {PredictionMethod.Filtered};
			for (PredictionMethod predictionMethod : values ) {
				Util.predictionMethod = predictionMethod;
				//FileWriter fw = new FileWriter(String.format("Approach1_%s.txt", predictionMethod.toString()));
				//BufferedWriter bw = new BufferedWriter(fw);
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
					NormalDist modelDist;
					Double actualTime;
					switch (Util.predictionMethod) {
					case Historic:
						modelDist = Approach1.GenerateModel(sensorList, timeOfDay, modelDays, null);
						for (Calendar startTime : testDays) {
							actualTime = Approach1.GenerateActual(sensorList, (Calendar)startTime.clone());
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
							//System.out.println(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write("\n");
						}
						break;
					case Filtered:
						for (Calendar startTime : testDays) {
							ArrayList<Integer> filteredDays = Util.FilterDays(modelDays, sensorList[0], (Calendar)startTime.clone());
							if (filteredDays.size() > 0)
								modelDist = Approach1.GenerateModel(sensorList, timeOfDay, filteredDays, null);
							else
								modelDist = Approach1.GenerateModel(sensorList, timeOfDay, modelDays, null);
							actualTime = Approach1.GenerateActual(sensorList, (Calendar)startTime.clone());
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
							//System.out.println(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write("\n");
						}
					case Interpolated:
						for (Calendar startTime : testDays) {
							modelDist = Approach1.GenerateModel(sensorList, timeOfDay, modelDays, (Calendar)startTime.clone());
							actualTime = Approach1.GenerateActual(sensorList, (Calendar)startTime.clone());
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
							//System.out.println(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write("\n");
						}
					default:
						break;
					}
				}
				//System.out.println(String.format("\n\nTotal Score for Approach1 %s: %f\n\n", predictionMethod, totalScore/totalCount));
				//bw.write(String.format("\n\nTotal Score for Approach1 %s: %f\n\n", predictionMethod, totalScore/totalCount));
				//bw.close();
				//fw.close();
				results.get(Util.path).add(totalScore/totalCount);
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
