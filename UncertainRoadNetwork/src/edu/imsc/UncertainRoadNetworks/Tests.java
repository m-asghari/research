package edu.imsc.UncertainRoadNetworks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Statement;
import java.util.ArrayList;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;

import org.apache.commons.math3.distribution.NormalDistribution;

public class Tests {

	public static void main(String[] args) {
		try {
			FileReader fr = new FileReader("rpaths_700.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			ArrayList<String> sensorList = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				String[] sensors = line.split("-");
				for (int i = 0; i < sensors.length; ++i) {
					sensorList.add(sensors[i]);
				}
			}
			StringBuilder sb = new StringBuilder();
			String insTmp = "INSERT INTO TEMP VALUES (%S);";
			for (String sensor : sensorList) {
				sb.append(String.format(insTmp, sensor));
				sb.append("\n");
			}
			String result = sb.toString();
			System.out.println(result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void CompareScoreFunctions() {
		int numOfRuns = 52;
		Double exp = 10.0;
		Double std = 0.1;
		
		Double sumNormal = 0.0;
		Double sumPMF = 0.0;
		
		for (int i = 0; i < numOfRuns; ++i) {
			NormalDistribution dist = new NormalDistribution(exp, std);
		
			NormalDist myNormal = new NormalDist(exp, Math.pow(std, 2));
		
			double[] samples = dist.sample(200);
			PMF myPMF = new PMF(samples);
		
			Double obs = dist.sample();
		
			sumNormal += myNormal.GetScore(obs);
			sumPMF += myPMF.GetScore(obs);
		}
		
		Double normalScore = sumNormal/numOfRuns;
		Double pmfScore = sumPMF/numOfRuns;
		
		System.out.println(String.format("Normal Score: %f, PMF Score: %f", normalScore, pmfScore));
	}
	
	public static void GenerateLinkData() {
		try {
			FileReader fr = new FileReader("links.txt");
			BufferedReader br = new BufferedReader(fr);
			
			FileWriter fw = new FileWriter("link_values.csv");
			BufferedWriter bw = new BufferedWriter(fw);			
			String tod = "08:00:00";
			int linkNum = 1;
			String link = "";
			OracleConnection conn = Util.conn;
			while ((link = br.readLine()) != null) {
				String[] nodes = link.split("-");
				Statement stm2 = conn.createStatement();
				String query2 = String.format("SELECT DISTANCE FROM PATH%d_EDGES WHERE \"FROM\" = %s AND \"TO\" = %s", linkNum, nodes[0], nodes[1]);
				OracleResultSet ors2 = (OracleResultSet) stm2.executeQuery(query2);
				Double dist = 1.0;
				if (ors2.next()) dist = ors2.getDouble(1);
				ors2.close();
				stm2.close();
				Statement stm1 = conn.createStatement();
				String query1 = String.format("SELECT T1.SPEED, T2.SPEED FROM PATH%d_SPEED_PATTERNS T1, PATH%d_SPEED_PATTERNS T2 WHERE T1.TIME = T2.TIME AND T1.LINK_ID = %s AND T2.LINK_ID = %s AND T1.TIME_OF_DAY = '%s'", linkNum, linkNum, nodes[0], nodes[1], tod);
				OracleResultSet ors1 = (OracleResultSet) stm1.executeQuery(query1);
				StringBuilder sb1 = new StringBuilder();
				sb1.append(",,,,");sb1.append(nodes[0]);sb1.append(",");
				StringBuilder sb2 = new StringBuilder();
				sb2.append(",,,,");sb2.append(nodes[1]);sb2.append(",");
				StringBuilder sb3 = new StringBuilder();
				sb3.append(",,,,,");
				StringBuilder sb4 = new StringBuilder();
				sb4.append(",,,,,");
				StringBuilder sb5 = new StringBuilder();
				sb5.append(",,,,,");
				StringBuilder sb6 = new StringBuilder();
				sb6.append(",,,,,");
				StringBuilder sb7 = new StringBuilder();
				sb7.append(",,,,,");
				while (ors1.next()) {
					Double sp1 = ors1.getDouble(1);
					Double sp2 = ors1.getDouble(2);
					Double travelTime = (dist*2*3600)/(sp1+sp2);
					Integer base1 = Util.RoundDouble(travelTime, 1);
					Integer base5 = Util.RoundDouble(travelTime, 5);
					Integer base15 = Util.RoundDouble(travelTime, 15);
					sb1.append(Double.toString(sp1));sb1.append(",");
					sb2.append(Double.toString(sp2));sb2.append(",");
					sb3.append(Double.toString(dist));sb3.append(",");
					sb4.append(Double.toString(travelTime));sb4.append(",");
					sb5.append(Integer.toString(base1));sb5.append(",");
					sb6.append(Integer.toString(base5));sb6.append(",");
					sb7.append(Integer.toString(base15));sb7.append(",");
				}
				ors1.close();
				stm1.close();
				bw.write(sb1.toString());
				bw.write("\n");
				bw.write(sb2.toString());
				bw.write("\n");
				bw.write(sb3.toString());
				bw.write("\n");
				bw.write(sb4.toString());
				bw.write("\n");
				bw.write(sb5.toString());
				bw.write("\n");
				bw.write(sb6.toString());
				bw.write("\n");
				bw.write(sb7.toString());
				bw.write("\n\n");
				linkNum++;
				System.out.println("Done with link" + Integer.toString(linkNum));
			}
			bw.close();
			fw.close();
			br.close();
			fr.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
