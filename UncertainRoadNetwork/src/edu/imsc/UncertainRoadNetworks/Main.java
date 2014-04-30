package edu.imsc.UncertainRoadNetworks;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.imsc.UncertainRoadNetworks.Util.PredictionMethod;

public class Main {
	private static int[] targetDays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY}; 
	private static int startMinute = 0;
	private static int k = 5; 
	
	public static HashMap<String, ArrayList<Double>> results;
	
	public static void main(String[] args) {
		int[] startHours = new int[] {8, 11, 14, 17};
		int[] predictionTimes = new int[] {15, 30, 60, 90, 120};
		//int[] predictionTimes = new int[] {20};
		
		/*try {
			FileReader fr = new FileReader("links.txt");
			BufferedReader br = new BufferedReader(fr);
			results = new HashMap<String, ArrayList<Double>>();
			String link = "";
			int pathN = 100;
			while ((link = br.readLine()) != null) {
				pathN++;
				Util.path = link;
				Util.pathNumber = Integer.toString(pathN);
				//Util.Initialize();
				results.put(Util.path, new ArrayList<Double>());
				for (int predictionTime : predictionTimes) {
					for (int startHour : startHours) {
						RunExperiment(startHour, PredictionMethod.Historic, predictionTime);
						System.out.println(String.format("Finished Path%d at startHour %d prediction time %d" , pathN, startHour, predictionTime));
					}
				}
				WriteResultsToFile(results, "results3_links_Historic_discrete.csv");
			}
			br.close();
			fr.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			FileReader fr = new FileReader("links.txt");
			BufferedReader br = new BufferedReader(fr);
			results = new HashMap<String, ArrayList<Double>>();
			String link = "";
			int pathN = 100;
			while ((link = br.readLine()) != null) {
				pathN++;
				Util.path = link;
				Util.pathNumber = Integer.toString(pathN);
				//Util.Initialize();
				results.put(Util.path, new ArrayList<Double>());
				//double[] simThresholds = new double[] {0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1};
				double[] simThresholds = new double[] {0.3};
				for (int predictionTime : predictionTimes) {
					for (double similarity : simThresholds) {
						for (int startHour : startHours) {
							Util.similarityThreshold = similarity;
							RunExperiment(startHour, PredictionMethod.Filtered, predictionTime);
							System.out.println(String.format("Finished Path%d at startHour %d with threshold %f and predictionTime %d" , pathN, startHour, similarity, predictionTime));
						}
					}
				}
				WriteResultsToFile(results, "results3_links_Filtered_discrete.csv");
			}
			br.close();
			fr.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}*/
		try {
			FileReader fr = new FileReader("links.txt");
			BufferedReader br = new BufferedReader(fr);
			results = new HashMap<String, ArrayList<Double>>();
			String link = "";
			int pathN = 0;
			while ((link = br.readLine()) != null) {
				pathN++;
				Util.path = link;
				Util.pathNumber = Integer.toString(pathN);
				//Util.Initialize();
				results.put(Util.path, new ArrayList<Double>());
				//int[] timeHorizons = new int[] {10, 15, 20, 25, 30, 40, 50, 60};
				double[] alphas = new double[] {0, 0.25, 0.5, 0.75, 1};
				for (int predictionTime : predictionTimes) {
					/*for (int timeHorizon : timeHorizons) {
						if (predictionTime <= timeHorizon) { 
							for (int startHour : startHours) {
								Util.alpha = 1 - ((double)predictionTime/timeHorizon);
								if (Util.alpha < 0.0) Util.alpha = 0.0;
								RunExperiment(startHour, PredictionMethod.Interpolated, predictionTime);
								System.out.println(String.format("Finished Path%d at startHour %d with timeHorizof %d and predictionTime %d" , pathN, startHour, timeHorizon, predictionTime));
							}
						}
					}*/
					for (double alpha : alphas) {
						for (int startHour : startHours) {
							Util.alpha = alpha;
							RunExperiment(startHour, PredictionMethod.Interpolated, predictionTime);
							System.out.println(String.format("Finished Path%d at startHour %d with alpha %f and predictionTime %d" , pathN, startHour, alpha, predictionTime));
						}
					}
					
				}
				WriteResultsToFile(results, "results5_links_Interpolated_discrete.csv");
			}
			br.close();
			fr.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void WriteResultsToFile(HashMap<String, ArrayList<Double>> results, String filePath){
		try {
			//FileWriter fw = new FileWriter("results_links_Historic_discrete.csv");
			FileWriter fw = new FileWriter(filePath);
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
	
	public static void RunExperiment(int startHour, Util.PredictionMethod predMethod, int predictionTime) {
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
			PredictionMethod[] values = new PredictionMethod[] {predMethod};
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
							if (l != i)
								if (j < allStartTimes.get(l).size())
									modelDays.add(allStartTimes.get(l).get(j).get(Calendar.DAY_OF_YEAR));
						}
					}
					PMF modelDist;
					Double actualTime;
					switch (Util.predictionMethod) {
					case Historic:
						Calendar todCal = Calendar.getInstance();
						todCal.setTime(Util.timeOfDayDF.parse(timeOfDay));
						todCal.add(Calendar.MINUTE, predictionTime);
						String todStr = Util.timeOfDayDF.format(todCal.getTime());
						modelDist = Approach2.GenerateModel(sensorList, todStr, modelDays, null);
						for (Calendar startTime : testDays) {
							Calendar predictionCal = Calendar.getInstance();
							predictionCal.setTime(startTime.getTime());
							predictionCal.add(Calendar.MINUTE, predictionTime);
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)predictionCal.clone());
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
							Calendar predictionCal = Calendar.getInstance();
							//predictionCal.setTime(Util.timeOfDayDF.parse(timeOfDay));
							predictionCal.setTime(startTime.getTime());
							predictionCal.add(Calendar.MINUTE, predictionTime);
							String predictionTOD = Util.timeOfDayDF.format(predictionCal.getTime());
							ArrayList<Integer> filteredDays = Util.FilterDays(modelDays, sensorList[0], (Calendar)startTime.clone());
							if (filteredDays.size() > 0)
								modelDist = Approach2.GenerateModel(sensorList, predictionTOD, filteredDays, null);
							else
								modelDist = Approach2.GenerateModel(sensorList, predictionTOD, modelDays, null);
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)predictionCal.clone());
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
							//System.out.println(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write("\n");
						}
						break;
					case Interpolated:
						for (Calendar startTime : testDays) {
							Calendar predictionCal = Calendar.getInstance();
							//predictionCal.setTime(Util.timeOfDayDF.parse(timeOfDay));
							predictionCal.setTime(startTime.getTime());
							predictionCal.add(Calendar.MINUTE, predictionTime);
							String predictionTOD = Util.timeOfDayDF.format(predictionCal.getTime());
							String temp = Util.oracleDF.format(startTime.getTime());
							String temp2 = Util.oracleDF.format(predictionCal.getTime());
							modelDist = Approach2.GenerateModel(sensorList, predictionTOD, modelDays, (Calendar)startTime.clone());
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)predictionCal.clone());
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
							//System.out.println(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write(Approach1.GetResults((Calendar)startTime.clone(), actualTime, score));
							//bw.write("\n");
						}
						break;
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
