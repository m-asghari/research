package edu.imsc.UncertainRoadNetworks;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;

public class GenerateLinkCorrelations {
	
	private static Pair<Integer, Integer> f2fPair = new Pair<Integer, Integer>(0, 0);
	private static Pair<Integer, Integer> f2tPair = new Pair<Integer, Integer>(0, 1);
	private static Pair<Integer, Integer> t2fPair = new Pair<Integer, Integer>(1, 0);
	private static Pair<Integer, Integer> t2tPair = new Pair<Integer, Integer>(1, 1);

	public static void main(String[] args) {
		OracleConnection conn = Util.getConnection();
		String path = "768701-774344-770599-768297-768283-770587-770012-770024-770036-770354-770048-770331-770544-770061-770556-770076-771202-770089-770103-770475-770487-770116-769895-769880-769866-769847-767610-767598-718076-767471-718072-767454-762329-767621-767573-718066-767542-718064-767495-716955-716949-760650-718045-760643-760635-774671-718166";
		String[] edges = path.split("-");
		String pathNumber = "2";
		try {
			CreateTable(conn, pathNumber);
			HashMap<String, ArrayList<Double>> allTravelTimes = new HashMap<String, ArrayList<Double>>();
			HashMap<String, Double> means = new HashMap<String, Double>();
			//HashMap<String, Double> stds = new HashMap<String, Double>();
			for (String edge : edges) {
				ArrayList<Double> travelTimes = GetEdgeTravelTimes(conn, pathNumber, edge);
				Double mean = GetMean(travelTimes);
				allTravelTimes.put(edge, travelTimes);
				means.put(edge, mean);
				//stds.put(edge, GetSTD(travelTimes, mean));
			}
			for (int i = 0; i < edges.length; ++i) {
				String link1 = edges[i];
				for (int j = i; j < edges.length; ++j) {
					String link2 = edges[j];
					Double cont = GetContLinkCorr(allTravelTimes.get(link1), means.get(link1),
							allTravelTimes.get(link2), means.get(link2));
					InsertCont(conn, pathNumber, link1, link2, cont);
					InsertCont(conn, pathNumber, link2, link1, cont);
				}
			}
			for (int i = 1; i < edges.length - 1; ++i) {
				String prevEdge = edges[i-1];
				String edge = edges[i];
				ArrayList<Pair<Integer, Integer>> congChanges = GetCongestions(conn, pathNumber, prevEdge, edge);
				HashMap<String, Double> probs = GetTransitionProbs(congChanges);				
				UpdateCong(conn, pathNumber, prevEdge, edge, probs);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		

	}

	private static HashMap<String, Double> GetTransitionProbs(
			ArrayList<Pair<Integer, Integer>> congChanges) {
		HashMap<String, Double> retMap = new HashMap<String, Double>();
		Double f2fCnt = 0.0, f2tCnt = 0.0, t2fCnt = 0.0, t2tCnt = 0.0, fCnt = 0.0, tCnt = 0.0;
		for (Pair<Integer, Integer> pair : congChanges) {
			if (pair.equals(f2fPair)) {
				f2fCnt++;
				fCnt++;
			}
			if (pair.equals(f2tPair)) {
				f2tCnt++;
				fCnt++;
			}
			if (pair.equals(t2fPair)) {
				t2fCnt++;
				tCnt++;
			}
			if (pair.equals(t2tPair)) {
				t2tCnt++;
				tCnt++;
			}
		}
		fCnt = (fCnt == 0) ? 1 : fCnt;
		tCnt = (tCnt == 0) ? 1 : tCnt;
		
		retMap.put("f2f", f2fCnt/fCnt);
		retMap.put("f2t", f2tCnt/fCnt);
		retMap.put("t2f", t2fCnt/tCnt);
		retMap.put("t2t", t2tCnt/tCnt);
		return retMap;
	}

	private static Double GetContLinkCorr(ArrayList<Double> link1TTs, Double link1Mean, 
			ArrayList<Double> link2TTs, Double link2Mean) {
		int size = Math.min(link1TTs.size(), link2TTs.size());
		Double sum = 0.0;
		for (int i = 0; i < size; ++i)
			sum += (link1TTs.get(i)-link1Mean)*(link2TTs.get(i)-link2Mean);
		return sum/(size - 1);
	}

	public static void CreateTable(OracleConnection conn, String pathNumber) throws SQLException{
		Statement stm = conn.createStatement();
		String query = Util.readQuery("QueryTemplates\\LinkCorrelations\\CreateTable.sql")
				.replace("##PATH_NUM##", pathNumber);
		stm.execute(query);
		stm.close();		
	}	
	
	/*private static ArrayList<String> GetAllEdges(OracleConnection conn, String pathNumber) throws SQLException {
		ArrayList<String> retList = new ArrayList<String>();
		Statement stm = conn.createStatement();
		String query = Util.readQuery("QueryTemplates\\LinkCorrelations\\SelectEdges.sql")
				.replace("##PATH_NUM##", pathNumber);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		while (ors.next())
			retList.add(ors.getString(1));
		return retList;
	}*/
	
	private static ArrayList<Double> GetEdgeTravelTimes(OracleConnection conn, String pathNumber,
			String from) throws SQLException {
		ArrayList<Double> retList = new ArrayList<Double>();
		Statement stm = conn.createStatement();
		String query = Util.readQuery("QueryTemplates\\LinkCorrelations\\SelectEdgeTravelTimes.sql")
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		while (ors.next()) 
			retList.add(ors.getDouble(1));
		return retList;
	}
	
	private static void InsertCont(OracleConnection conn, String pathNumber,
			String link1, String link2, Double cont) throws SQLException {
		Statement stm = conn.createStatement();
		String query = Util.readQuery("QueryTemplates\\LinkCorrelations\\InsertCont.sql")
				.replace("##PATH_NUM##", pathNumber)
				.replace("##LINK1##", link1)
				.replace("##LINK2##", link2)
				.replace("##CONT##", Double.toString(cont));
		stm.execute(query);
		stm.close();
	}
	
	private static ArrayList<Pair<Integer, Integer>> GetCongestions(OracleConnection conn,
			String pathNumber, String link1, String link2) throws SQLException {
		ArrayList<Pair<Integer, Integer>> retList = new ArrayList<Pair<Integer,Integer>>();
		Statement stm = conn.createStatement();
		String query = Util.readQuery("QueryTemplates\\LinkCorrelations\\SelectCong.sql")
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM1##", link1)
				.replace("##FROM2##", link2);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		while (ors.next()) {
			Integer status1 = (ors.getString(1).equals("TRUE")) ? 1 : 0;
			Integer status2 = (ors.getString(2).equals("TRUE")) ? 1 : 0;
			retList.add(new Pair<Integer, Integer>(status1, status2));
		}
		return retList;
	}
	
	private static void UpdateCong(OracleConnection conn, String pathNumber,
			String link1, String link2, HashMap<String, Double> probs) throws SQLException {
		Statement stm = conn.createStatement();
		String query = Util.readQuery("QueryTemplates//LinkCorrelations//UpdateCong.sql")
				.replace("##PATH_NUM##", pathNumber)
				.replace("##LINK1##", link1)
				.replace("##LINK2##", link2)
				.replace("##F2F##", Double.toString(probs.get("f2f")))
				.replace("##F2T##", Double.toString(probs.get("f2t")))
				.replace("##T2F##", Double.toString(probs.get("t2f")))
				.replace("##T2T##", Double.toString(probs.get("t2t")));
		stm.execute(query);
		stm.close();		
	}
	
	private static Double GetMean(ArrayList<Double> input) {
		Double sum = 0.0;
		for (Double d : input)
			sum += d;
		int size = (input.size() == 0) ? 1 : input.size();
		return sum/size;
	}
	
	/*private static Double GetSTD(ArrayList<Double> input, Double mean) {
		Double sum = 0.0;
		for (Double d : input)
			sum += Math.pow((d-mean), 2);
		int size = (input.size() == 0) ? 1 : input.size();
		return Math.sqrt(sum/size);
	}*/

}
