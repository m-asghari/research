package edu.imsc.UncertainRoadNetworks;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Statement;

import oracle.jdbc.driver.OracleResultSet;


public class GenerateKMLFile {

	private static String kmlTemplate = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" + 
			"\t<Document>\n" +
			"\t\t<name>sensors.kml</name>\n" +
			"\t\t<open>1</open>\n" +
			"\t\t<Style id=\"exampleStyleDocument\">\n" +
			"\t\t\t<LabelStyle>\n" +
			"\t\t\t\t<color>ff0000cc</color>\n" +
			"\t\t\t</LabelStyle>\n" +
			"\t\t</Style>\n" +
				"##PLACEMARKS##\n" +
			"\t</Document>\n" +
			"</kml>";
		
		private static String placeMarkTmplate =
			"\t\t<Placemark>\n" +
			"\t\t\t<name/>\n" + 
			"\t\t\t<description>\n" +
			"\t\t\t\tLink ID: ##LINK_ID##\n" +
			"\t\t\t\tDirection: ##DIRECTION##\n" +
			"\t\t\t</description>\n" + 
			"\t\t\t<Point>\n" + 
			"\t\t\t\t<coordinates>##LONGITUDE##,##LATITUDE##,0</coordinates>\n" + 
			"\t\t\t</Point>\n" + 
			"\t\t</Placemark>\n";

	public static void main(String[] args) {
		String query = QueryTemplates.KMLGenerationQuery;
		OracleResultSet ors = null;
		try {
			Statement stm = Util.conn.createStatement();
			ors = (OracleResultSet) stm.executeQuery(query);
			StringBuilder sb = new StringBuilder();
			while (ors.next()) {
				String dir = "";
				switch (ors.getString(4)) {
				case "0": dir = "Norht"; break;
				case "1": dir = "South"; break;
				case "2": dir = "East"; break;
				case "3": dir = "West"; break;
				}
				sb.append(placeMarkTmplate
						.replace("##LINK_ID##", ors.getString(1))
						.replace("##LONGITUDE##", Double.toString(ors.getDouble(3)))
						.replace("##LATITUDE##", Double.toString(ors.getDouble(2)))
						.replace("##DIRECTION##", dir));
			}
			String kmlString = kmlTemplate.replace("##PLACEMARKS##", sb.toString());
			FileWriter fw = new FileWriter("sensors.kml");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(kmlString);
			bw.close();
			fw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
