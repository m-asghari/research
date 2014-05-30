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
	
	public static HashMap<String, ArrayList<Double>> crps_results;
	public static HashMap<String, ArrayList<Double>> ev_results;
	
	public static void main(String[] args) {

		pathNums.put(new Pair<String, String>("p", "links"), new Pair<Integer, Integer>(0, 50));
		pathNums.put(new Pair<String, String>("r", "links"), new Pair<Integer, Integer>(50, 100));
		pathNums.put(new Pair<String, String>("p", "paths"), new Pair<Integer, Integer>(100, 110));
		//pathNums.put(new Pair<String, String>("r", "paths"), new Pair<Integer, Integer>(110, 120));
		pathNums.put(new Pair<String, String>("r", "paths"), new Pair<Integer, Integer>(120, 125));
		String linkType = "r";
		String pathType = "paths";
		String distType = "Approach4";
		int minPath = pathNums.get(new Pair<String, String>(linkType, pathType)).getFirst();
		int maxPath = pathNums.get(new Pair<String, String>(linkType, pathType)).getSecond();
		//int[] startHours = new int[] {7, 8, 15, 16, 17};
		int[] startHours = new int[] {8, 9, 16, 17, 18};
		//int[] predictionTimes = new int[] {5, 10, 15, 30, 60, 90, 120, 150, 180, 210, 240};
		int[] predictionTimes = new int[] {0, 5, 10, 15, 30, 60, 90, 115};
		//int[] predictionTimes = new int[] {0};
		
		
		/*try {
			HashMap<Integer, Pair<FileReader, BufferedReader>> inputFiles = new HashMap<Integer, Pair<FileReader,BufferedReader>>();
			for (int startHour : startHours) {
				String filename = String.format("%s_%s%d00.txt", pathType, linkType, startHour-1);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				inputFiles.put(startHour, new Pair<FileReader, BufferedReader>(fr, br));
			}
			crps_results = new HashMap<String, ArrayList<Double>>();
			ev_results = new HashMap<String, ArrayList<Double>>();
			int pathN = minPath;
			while (pathN < maxPath) {
				pathN++;
				for (int startHour : startHours) {
					Util.path = inputFiles.get(startHour).getSecond().readLine();
					Util.pathNumber = String.format("%d00%d", startHour-1, pathN);
					crps_results.put(Util.pathNumber, new ArrayList<Double>());
					ev_results.put(Util.pathNumber, new ArrayList<Double>());
					if (startHour == 8 || startHour == 16) {
						PathData.LoadEdgePatterns();
						PathData.LoadEdgeCorrelations();
					}
					for (int predictionTime : predictionTimes) {
						RunExperiment(startHour, PredictionMethod.NoPrediction, predictionTime);
						System.out.println(String.format("Finished Path%d at startHour %d prediction time %d" , pathN, startHour, predictionTime));
					}
					if (startHour == 9 || startHour == 18) {
						PathData.Reset();
					}
				}				
			}
			WriteResultsToFile(crps_results, String.format("crps_results_%s_%s_Historic_%s.csv", pathType, linkType, distType));
			WriteResultsToFile(ev_results, String.format("ev_results_%s_%s_Historic_%s.csv", pathType, linkType, distType));
			WriteStatsToFile(String.format("stats_%s_%s_Historic_%s.csv", pathType, linkType, distType));
			for (int startHour : startHours) {
				inputFiles.get(startHour).getSecond().close();
				inputFiles.get(startHour).getFirst().close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}*/
		
		Util.p_passedMillis = 0; Util.l_passedMillis = 0; Util.pl_passedMillis = 0;
		Util.p_timeCounter = 0; Util.l_timeCounter = 0; Util.pl_timeCounter = 0;
		try {
			HashMap<Integer, Pair<FileReader, BufferedReader>> inputFiles = new HashMap<Integer, Pair<FileReader,BufferedReader>>();
			for (int startHour : startHours) {
				String filename = String.format("%s_%s%d00.txt", pathType, linkType, startHour-1);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				inputFiles.put(startHour, new Pair<FileReader, BufferedReader>(fr, br));
			}
			crps_results = new HashMap<String, ArrayList<Double>>();
			ev_results = new HashMap<String, ArrayList<Double>>();
			int pathN = minPath;
			while (pathN < maxPath) {
				pathN++;
				for (int startHour : startHours) {
					Util.path = inputFiles.get(startHour).getSecond().readLine();
					Util.pathNumber = String.format("%d00%d", startHour-1, pathN);
					crps_results.put(Util.pathNumber, new ArrayList<Double>());
					ev_results.put(Util.pathNumber, new ArrayList<Double>());
					if (startHour == 8 || startHour == 16) {
						PathData.LoadEdgePatterns();
						//PathData.LoadEdgeCorrelations();
					}
					//Util.Initialize();
					for (int predictionTime : predictionTimes) {
						RunExperiment(startHour, PredictionMethod.Historic, predictionTime);
						System.out.println(String.format("Finished Path%d at startHour %d prediction time %d" , pathN, startHour, predictionTime));
					}
					if (startHour == 9 || startHour == 18) {
						PathData.Reset();
					}
				}				
			}
			WriteResultsToFile(crps_results, String.format("crps_results_%s_%s_Historic_%s.csv", pathType, linkType, distType));
			WriteResultsToFile(ev_results, String.format("ev_results_%s_%s_Historic_%s.csv", pathType, linkType, distType));
			WriteStatsToFile(String.format("stats_%s_%s_Historic_%s.csv", pathType, linkType, distType));
			for (int startHour : startHours) {
				inputFiles.get(startHour).getSecond().close();
				inputFiles.get(startHour).getFirst().close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Util.p_passedMillis = 0; Util.l_passedMillis = 0; Util.pl_passedMillis = 0;
		Util.p_timeCounter = 0; Util.l_timeCounter = 0; Util.pl_timeCounter = 0;
		try {
			HashMap<Integer, Pair<FileReader, BufferedReader>> inputFiles = new HashMap<Integer, Pair<FileReader,BufferedReader>>();
			for (int startHour : startHours) {
				String filename = String.format("%s_%s%d00.txt", pathType, linkType, startHour-1);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				inputFiles.put(startHour, new Pair<FileReader, BufferedReader>(fr, br));
			}
			double[] simThresholds = new double[] {0.01, 0.02, 0.03, 0.04, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1};
			crps_results = new HashMap<String, ArrayList<Double>>();
			ev_results = new HashMap<String, ArrayList<Double>>();
			int pathN = minPath;
			while (pathN < maxPath) {
				pathN++;
				for (int startHour : startHours) {
					Util.path = inputFiles.get(startHour).getSecond().readLine();
					Util.pathNumber = String.format("%d00%d", startHour-1, pathN);
					crps_results.put(Util.pathNumber, new ArrayList<Double>());
					ev_results.put(Util.pathNumber, new ArrayList<Double>());
					if (startHour == 8 || startHour == 16) {
						PathData.LoadEdgePatterns();
						//PathData.LoadEdgeCorrelations();
					}
					//Util.Initialize();
					for (int predictionTime : predictionTimes) {
						Double similarity = 0.1;
						//for (double similarity : simThresholds) {
							Util.similarityThreshold = similarity;
							RunExperiment(startHour, PredictionMethod.Filtered, predictionTime);
							System.out.println(String.format("Finished Path%d at startHour %d with threshold %f and predictionTime %d" , pathN, startHour, similarity, predictionTime));
						//}
					}
					if (startHour == 9 || startHour == 18) {
						PathData.Reset();
					}
				}				
			}
			WriteResultsToFile(crps_results, String.format("crps_results_%s_%s_Filtered_%s.csv", pathType, linkType, distType));
			WriteResultsToFile(ev_results, String.format("ev_results_%s_%s_Filtered_%s.csv", pathType, linkType, distType));
			WriteStatsToFile(String.format("stats_%s_%s_Filtered_%s.csv", pathType, linkType, distType));
			for (int startHour : startHours) {
				inputFiles.get(startHour).getSecond().close();
				inputFiles.get(startHour).getFirst().close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Util.p_passedMillis = 0; Util.l_passedMillis = 0; Util.pl_passedMillis = 0;
		Util.p_timeCounter = 0; Util.l_timeCounter = 0; Util.pl_timeCounter = 0;
		try {
			HashMap<Integer, Pair<FileReader, BufferedReader>> inputFiles = new HashMap<Integer, Pair<FileReader,BufferedReader>>();
			for (int startHour : startHours) {
				String filename = String.format("%s_%s%d00.txt", pathType, linkType, startHour-1);
				FileReader fr = new FileReader(filename);
				BufferedReader br = new BufferedReader(fr);
				inputFiles.put(startHour, new Pair<FileReader, BufferedReader>(fr, br));
			}
			double[] alphas = new double[] {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
			crps_results = new HashMap<String, ArrayList<Double>>();
			ev_results = new HashMap<String, ArrayList<Double>>();
			int pathN = minPath;
			while (pathN < maxPath) {
				pathN++;
				for (int startHour : startHours) {
					Util.path = inputFiles.get(startHour).getSecond().readLine();
					Util.pathNumber = String.format("%d00%d", startHour-1, pathN);
					crps_results.put(Util.pathNumber, new ArrayList<Double>());
					ev_results.put(Util.pathNumber, new ArrayList<Double>());
					if (startHour == 8 || startHour == 16) {
						PathData.LoadEdgePatterns();
						//PathData.LoadEdgeCorrelations();
					}
					//Util.Initialize();
					for (int predictionTime : predictionTimes) {
						/*for (double alpha : alphas) {
							Util.alpha = alpha;
							Util.Log(String.format("Finished Path%d at startHour %d with alpha %f and predictionTime %d" , pathN, startHour, alpha, predictionTime));
							RunExperiment(startHour, PredictionMethod.Interpolated, predictionTime);
							System.out.println(String.format("Finished Path%d at startHour %d with alpha %f and predictionTime %d" , pathN, startHour, alpha, predictionTime));
						}*/
						Util.timeHorizon = 60.0;
						Double alpha = (Util.timeHorizon - predictionTime) / Util.timeHorizon;
						if (alpha < 0.0) alpha = 0.0;
						Util.alpha = alpha;
						RunExperiment(startHour, PredictionMethod.Interpolated, predictionTime);
						System.out.println(String.format("Finished Path%d at startHour %d with alpha %f and predictionTime %d" , pathN, startHour, alpha, predictionTime));
					}
					if (startHour == 9 || startHour == 18) {
						PathData.Reset();
					}
				}				
			}
			WriteResultsToFile(crps_results, String.format("crps_results_%s_%s_Interpolated_%s.csv", pathType, linkType, distType));
			WriteResultsToFile(ev_results, String.format("ev_results_%s_%s_Interpolated_%s.csv", pathType, linkType, distType));
			WriteStatsToFile(String.format("stats_%s_%s_Interpolated_%s.csv", pathType, linkType, distType));
			for (int startHour : startHours) {
				inputFiles.get(startHour).getSecond().close();
				inputFiles.get(startHour).getFirst().close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void WriteStatsToFile(String filePath) {
		try {
			double p_computationTime = ((double)Util.p_passedMillis)/Util.p_timeCounter;
			double l_computationTime = ((double)Util.l_passedMillis)/Util.l_timeCounter;
			double pl_computationTime = ((double)Util.pl_passedMillis)/Util.pl_timeCounter;
			FileWriter fw = new FileWriter(filePath);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(String.format("p_passedMillis: %d, p_timeCounter: %d, p_computationTime: %f", Util.p_passedMillis, Util.p_timeCounter, p_computationTime));
			bw.write("\n");
			bw.write(String.format("l_passedMillis: %d, l_timeCounter: %d, l_computationTime: %f", Util.l_passedMillis, Util.l_timeCounter, l_computationTime));
			bw.write("\n");
			bw.write(String.format("pl_passedMillis: %d, pl_timeCounter: %d, pl_computationTime: %f", Util.pl_passedMillis, Util.pl_timeCounter, pl_computationTime));
			bw.write("\n");
			bw.close();
			fw.close();
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
	
	public static void RunExperiment(int startHour, Util.PredictionMethod predictionMethod, int predictionTime) {
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
			Util.predictionMethod = predictionMethod;
			Double totalCRPSScore = 0.0, totalEVScore = 0.0;
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
				case NoPrediction:
					for (Calendar startTime : testDays) {
						Calendar queryTime = Calendar.getInstance();
						queryTime.setTime(startTime.getTime());
						queryTime.add(Calendar.MINUTE, -predictionTime);
						modelDist = Approach4.GenerateModel(sensorList, null, null, queryTime);
						if (modelDist == null) continue;
						actualTime = Approach4.GenerateActual(sensorList, (Calendar)startTime.clone());
						if (actualTime == null) continue;
						Double score = modelDist.GetScore(actualTime);
						totalCRPSScore += score;
						totalEVScore += Math.abs(modelDist.mean - actualTime);
						totalCount++;
					}
					break;
				case Historic:					
					//Calendar todCal = Calendar.getInstance();
					//todCal.setTime(Util.timeOfDayDF.parse(timeOfDay));
					//todCal.add(Calendar.MINUTE, predictionTime);
					//String todStr = Util.timeOfDayDF.format(todCal.getTime());
					modelDist = Approach4.GenerateModel(sensorList, timeOfDay, modelDays, null);
					if (modelDist == null) {
						//Util.no_model++;
						continue;
					}
					//Util.model++;
					for (Calendar startTime : testDays) {
						//Calendar predictionCal = Calendar.getInstance();
						//predictionCal.setTime(startTime.getTime());
						//predictionCal.add(Calendar.MINUTE, predictionTime);
						actualTime = Approach4.GenerateActual(sensorList, (Calendar)startTime.clone());
						if (actualTime == null) {
							//Util.no_actual++;
							continue;
						}
						//Util.actual++;
						Double score = modelDist.GetScore(actualTime);
						totalCRPSScore += score;
						totalEVScore += Math.abs(modelDist.mean - actualTime);
						totalCount++;
					}
					break;
				case Filtered:
					for (Calendar startTime : testDays) {
						String str = Util.oracleDF.format(startTime.getTime());
						Calendar queryTime = Calendar.getInstance();
						queryTime.setTime(startTime.getTime());
						queryTime.add(Calendar.MINUTE, -predictionTime);
						//String predictionTOD = Util.timeOfDayDF.format(predictionCal.getTime());
						//ArrayList<Integer> filteredDays = Util.FilterDays(modelDays, sensorList[0], (Calendar)queryTime.clone());
						modelDist = Approach4.GenerateModel(sensorList, timeOfDay, modelDays, (Calendar)queryTime.clone());
						if (modelDist == null) continue;
						actualTime = Approach4.GenerateActual(sensorList, (Calendar)startTime.clone());
						if (actualTime == null) continue;
						Double score = modelDist.GetScore(actualTime);
						totalCRPSScore += score;
						totalEVScore += Math.abs(modelDist.mean - actualTime);
						totalCount++;
					}
					break;
				case Interpolated:
					for (Calendar startTime : testDays) {
						Calendar queryTime = Calendar.getInstance();
						queryTime.setTime(startTime.getTime());
						queryTime.add(Calendar.MINUTE, -predictionTime);
						//String predictionTOD = Util.timeOfDayDF.format(predictionCal.getTime());
						modelDist = Approach4.GenerateModel(sensorList, timeOfDay, modelDays, (Calendar)queryTime.clone());
						if (modelDist == null) {
							Util.model++;
							continue;
						}
						actualTime = Approach4.GenerateActual(sensorList, (Calendar)startTime.clone());
						if (actualTime == null) {
							Util.actual++;
							continue;
						}
						Double score = modelDist.GetScore(actualTime);
						totalCRPSScore += score;
						totalEVScore += Math.abs(modelDist.mean - actualTime);
						totalCount++;
					}
					break;
				default:
					break;
				}
			}
			crps_results.get(Util.pathNumber).add(totalCRPSScore/totalCount);
			ev_results.get(Util.pathNumber).add(totalEVScore/totalCount);
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
