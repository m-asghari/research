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
	private static int[] targetDays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY}; 
	private static int startMinute = 0;
	private static int k = 5;
	private static HashMap<Pair<String, String>, Pair<Integer, Integer>> pathNums = new HashMap<Pair<String,String>, Pair<Integer,Integer>>();
	
	public static HashMap<String, ArrayList<Double>> results;
	
	public static void main(String[] args) {
		pathNums.put(new Pair<String, String>("p", "links"), new Pair<Integer, Integer>(0, 50));
		pathNums.put(new Pair<String, String>("r", "links"), new Pair<Integer, Integer>(50, 100));
		pathNums.put(new Pair<String, String>("p", "paths"), new Pair<Integer, Integer>(100, 110));
		pathNums.put(new Pair<String, String>("r", "paths"), new Pair<Integer, Integer>(110, 120));
		String linkType = "r";
		String pathType = "links";
		String distType = "continuous";
		int minPath = pathNums.get(new Pair<String, String>(linkType, pathType)).getFirst();
		int maxPath = pathNums.get(new Pair<String, String>(linkType, pathType)).getSecond();
		int[] startHours = new int[] {7, 8, 15, 16, 17};
		int[] predictionTimes = new int[] {5, 10, 15, 30, 60, 90, 120};
		
		try {
			HashMap<Integer, Pair<FileReader, BufferedReader>> inputFiles = new HashMap<Integer, Pair<FileReader,BufferedReader>>();
			for (int startHour : startHours) {
				String filename = String.format("%s_%s%d00.txt", pathType, linkType, startHour);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				inputFiles.put(startHour, new Pair<FileReader, BufferedReader>(fr, br));
			}
			results = new HashMap<String, ArrayList<Double>>();
			int pathN = minPath;
			while (pathN < maxPath) {
				pathN++;
				for (int startHour : startHours) {
					Util.path = inputFiles.get(startHour).getSecond().readLine();
					Util.pathNumber = String.format("%d00%d", startHour, pathN);
					results.put(Util.pathNumber, new ArrayList<Double>());
					PathData.LoadEdgePatterns();
					//Util.Initialize();
					for (int predictionTime : predictionTimes) {
						RunExperiment(startHour, PredictionMethod.Historic, predictionTime);
						System.out.println(String.format("Finished Path%d at startHour %d prediction time %d" , pathN, startHour, predictionTime));
					}
					PathData.Reset();
				}				
			}
			WriteResultsToFile(results, String.format("results_%s_%s_Historic_%s.csv", pathType, linkType, distType));
			for (int startHour : startHours) {
				inputFiles.get(startHour).getSecond().close();
				inputFiles.get(startHour).getFirst().close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			HashMap<Integer, Pair<FileReader, BufferedReader>> inputFiles = new HashMap<Integer, Pair<FileReader,BufferedReader>>();
			for (int startHour : startHours) {
				String filename = String.format("%s_%s%d00.txt", pathType, linkType, startHour);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				inputFiles.put(startHour, new Pair<FileReader, BufferedReader>(fr, br));
			}
			double[] simThresholds = new double[] {0.01, 0.02, 0.03, 0.04, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1};
			results = new HashMap<String, ArrayList<Double>>();
			int pathN = minPath;
			while (pathN < maxPath) {
				pathN++;
				for (int startHour : startHours) {
					Util.path = inputFiles.get(startHour).getSecond().readLine();
					Util.pathNumber = String.format("%d00%d", startHour, pathN);
					results.put(Util.pathNumber, new ArrayList<Double>());
					PathData.LoadEdgePatterns();
					//Util.Initialize();
					for (int predictionTime : predictionTimes) {
						for (double similarity : simThresholds) {
							Util.Log(String.format("Path%d at startHour %d with threshold %f and predictionTime %d" , pathN, startHour, similarity, predictionTime));
							Util.similarityThreshold = similarity;
							RunExperiment(startHour, PredictionMethod.Filtered, predictionTime);
							System.out.println(String.format("Finished Path%d at startHour %d with threshold %f and predictionTime %d" , pathN, startHour, similarity, predictionTime));
						}
					}
					PathData.Reset();
				}				
			}
			WriteResultsToFile(results, String.format("results_%s_%s_Filtered_%s.csv", pathType, linkType, distType));
			for (int startHour : startHours) {
				inputFiles.get(startHour).getSecond().close();
				inputFiles.get(startHour).getFirst().close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			HashMap<Integer, Pair<FileReader, BufferedReader>> inputFiles = new HashMap<Integer, Pair<FileReader,BufferedReader>>();
			for (int startHour : startHours) {
				String filename = String.format("%s_%s%d00.txt", pathType, linkType, startHour);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				inputFiles.put(startHour, new Pair<FileReader, BufferedReader>(fr, br));
			}
			double[] alphas = new double[] {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
			results = new HashMap<String, ArrayList<Double>>();
			int pathN = minPath;
			while (pathN < maxPath) {
				pathN++;
				for (int startHour : startHours) {
					Util.path = inputFiles.get(startHour).getSecond().readLine();
					Util.pathNumber = String.format("%d00%d", startHour, pathN);
					results.put(Util.pathNumber, new ArrayList<Double>());
					PathData.LoadEdgePatterns();
					//Util.Initialize();
					for (int predictionTime : predictionTimes) {
						for (double alpha : alphas) {
							Util.alpha = alpha;
							Util.Log(String.format("Finished Path%d at startHour %d with alpha %f and predictionTime %d" , pathN, startHour, alpha, predictionTime));
							RunExperiment(startHour, PredictionMethod.Interpolated, predictionTime);
							System.out.println(String.format("Finished Path%d at startHour %d with alpha %f and predictionTime %d" , pathN, startHour, alpha, predictionTime));
						}
					}
					PathData.Reset();
				}				
			}
			WriteResultsToFile(results, String.format("results_%s_%s_Interpolated_%s.csv", pathType, linkType, distType));
			for (int startHour : startHours) {
				inputFiles.get(startHour).getSecond().close();
				inputFiles.get(startHour).getFirst().close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void WriteResultsToFile(HashMap<String, ArrayList<Double>> results, String filePath){
		try {
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
						if (modelDist == null) continue;
						for (Calendar startTime : testDays) {
							Calendar predictionCal = Calendar.getInstance();
							predictionCal.setTime(startTime.getTime());
							predictionCal.add(Calendar.MINUTE, predictionTime);
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)predictionCal.clone());
							if (actualTime == null) continue;
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
						}
						break;
					case Filtered:
						for (Calendar startTime : testDays) {
							Calendar predictionCal = Calendar.getInstance();
							predictionCal.setTime(startTime.getTime());
							predictionCal.add(Calendar.MINUTE, predictionTime);
							String predictionTOD = Util.timeOfDayDF.format(predictionCal.getTime());
							ArrayList<Integer> filteredDays = Util.FilterDays(modelDays, sensorList[0], (Calendar)startTime.clone());
							if (filteredDays.size() > 0) {
								modelDist = Approach2.GenerateModel(sensorList, predictionTOD, filteredDays, null);
								if (modelDist == null) continue;
							}
							else
								continue;
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)predictionCal.clone());
							if (actualTime == null) continue;
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
						}
						break;
					case Interpolated:
						for (Calendar startTime : testDays) {
							Calendar predictionCal = Calendar.getInstance();
							predictionCal.setTime(startTime.getTime());
							predictionCal.add(Calendar.MINUTE, predictionTime);
							String predictionTOD = Util.timeOfDayDF.format(predictionCal.getTime());
							modelDist = Approach2.GenerateModel(sensorList, predictionTOD, modelDays, (Calendar)startTime.clone());
							if (modelDist == null) continue;
							actualTime = Approach2.GenerateActual(sensorList, (Calendar)predictionCal.clone());
							if (actualTime == null) continue;
							Double score = modelDist.GetScore(actualTime);
							totalScore += score;
							totalCount++;
						}
						break;
					default:
						break;
					}
				}
				results.get(Util.pathNumber).add(totalScore/totalCount);
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
