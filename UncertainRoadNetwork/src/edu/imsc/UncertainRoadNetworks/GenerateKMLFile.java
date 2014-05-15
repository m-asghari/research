package edu.imsc.UncertainRoadNetworks;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Statement;
import java.util.Calendar;

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
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		CreateKMLFile(cal);
		for (int hour = 7; hour < 21; ++hour) {
			cal.set(Calendar.HOUR_OF_DAY, hour);
			//String tod = Util.timeOfDayDF.format(cal.getTime());
			CreateKMLFile((Calendar)cal.clone());
		}
	}

	private static void CreateKMLFile(Calendar cal) {
		String tod = Util.timeOfDayDF.format(cal.getTime());
		cal.add(Calendar.MINUTE, 5);
		String m5 = Util.timeOfDayDF.format(cal.getTime());
		cal.add(Calendar.MINUTE, 5);
		String m10 = Util.timeOfDayDF.format(cal.getTime());
		cal.add(Calendar.MINUTE, 5);
		String m15 = Util.timeOfDayDF.format(cal.getTime());
		cal.add(Calendar.MINUTE, 15);
		String m30 = Util.timeOfDayDF.format(cal.getTime());
		cal.add(Calendar.MINUTE, 30);
		String m60 = Util.timeOfDayDF.format(cal.getTime());
		cal.add(Calendar.MINUTE, 30);
		String m90 = Util.timeOfDayDF.format(cal.getTime());
		cal.add(Calendar.MINUTE, 30);
		String m120 = Util.timeOfDayDF.format(cal.getTime());
		String query = QueryTemplates.KMLGenerationQuery
				.replace("##5MIN##", m5)
				.replace("##10MIN##", m10)
				.replace("##15MIN##", m15)
				.replace("##30MIN##", m30)
				.replace("##60MIN##", m60)
				.replace("##90MIN##", m90)
				.replace("##120MIN##", m120);
		//query = "SELECT DISTINCT SC.LINK_ID, t.Y, t.X, SC.DIRECTION FROM SENSOR_CONFIG SC, table(SDO_UTIL.GETVERTICES(SC.START_LAT_LONG)) t WHERE SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '15:05') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '15:10') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '15:15') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '15:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '16:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '16:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '17:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '16:05') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '16:10') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '16:15') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '16:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '17:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '17:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '18:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '17:05') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '17:10') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '17:15') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '17:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '18:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '18:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE STD_DEV_SPEED > 10 AND \"COUNT\" > 200 AND TOD = '19:00') ORDER BY SC.LINK_ID";
		//query = "SELECT DISTINCT SC.LINK_ID, t.Y, t.X, SC.DIRECTION FROM SENSOR_CONFIG SC, table(SDO_UTIL.GETVERTICES(SC.START_LAT_LONG)) t WHERE SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '15:05') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '15:10') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '15:15') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '15:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '16:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '16:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '17:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '16:05') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '16:10') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '16:15') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '16:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '17:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '17:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '18:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '17:05') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '17:10') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '17:15') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '17:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '18:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '18:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '19:00') ORDER BY SC.LINK_ID";
		query = "SELECT DISTINCT SC.LINK_ID, t.Y, t.X, SC.DIRECTION FROM SENSOR_CONFIG SC, table(SDO_UTIL.GETVERTICES(SC.START_LAT_LONG)) t WHERE SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '07:05') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '07:10') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '07:15') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '07:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '08:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '08:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '09:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '08:05') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '08:10') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '08:15') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '08:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '09:00') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '09:30') AND SC.LINK_ID IN ( SELECT LINK_ID FROM SENSOR_TOD_STAT WHERE \"COUNT\" > 200 AND TOD = '10:00') ORDER BY SC.LINK_ID";
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
			//FileWriter fw = new FileWriter(String.format("psensors_%s.kml", tod.substring(0, 2)));
			FileWriter fw = new FileWriter("sensors_7_8.kml");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(kmlString);
			bw.close();
			fw.close();
			ors.close();
			stm.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
