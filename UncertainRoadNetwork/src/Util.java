import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import oracle.jdbc.OracleDriver;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Util {
	
	private static final String host = "gd.usc.edu";
	private static final String port = "1521";
	private static final String service = "adms";
	private static final String username = "sch_sensor";
	private static final String password = "phe334";
	
	public static DateFormat oracleDF = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
	public static DateFormat timeOfDayDF = new SimpleDateFormat("HH:mm:ss");
	
	private static String pmfQueryTemplate = readQuery("QueryTemplates\\pmfQuery.sql");
	
	public static OracleConnection getConnection()
	{
		OracleConnection conn = null;
		
		String url = String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, service);
		
		try
		{
			DriverManager.registerDriver(new OracleDriver());
			conn = (OracleConnection) DriverManager.getConnection(url, username, password);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
		return conn;
	}
	
	public static Calendar RoundTimeDown(Calendar input) {
		int minutes = input.get(Calendar.MINUTE);
		int offset = minutes % 5;
		input.add(Calendar.MINUTE, -offset);
		input.set(Calendar.SECOND, 0);
		return input;
	}
	
	public static Calendar RoundTimeUp(Calendar input) {
		int minutes = input.get(Calendar.MINUTE);
		int offset = minutes % 5;
		input.add(Calendar.MINUTE, 5 - offset);
		input.set(Calendar.SECOND, 0);
		return input;
	}
	
	public static String readQuery(String fileName)
	{
		String query = new String();
		try
		{
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null)
			{
				sb.append(line);
				sb.append("\n");
			}
			query = sb.toString();
			br.close();
			fr.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return query;
	}
	
	public static Document toXML(OracleResultSet ors)
	{
		Document doc = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}		
		
		Element results = doc.createElement("Results");
		doc.appendChild(results);
		
		ResultSetMetaData rsmt = null;
		int colCount;
		try
		{
			rsmt = ors.getMetaData();
			colCount = rsmt.getColumnCount(); 	
			while (ors.next())
			{
				Element row = doc.createElement("Row");
				results.appendChild(row);
				
				for (int i = 1; i <= colCount; i++)
				{
					String columnName = rsmt.getColumnName(i);
					Object value = ors.getObject(i);
					
					Element node = doc.createElement(columnName);
					node.appendChild(doc.createTextNode((value != null) ? value.toString() : "null"));
					row.appendChild(node);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(doc), new StreamResult(os));
			System.out.println(os.toString("UTF-8"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
		return doc;
	}
	
	private static ArrayList<Double> getTravelTimes(String pathNumber, String from, String to, Calendar time,
			ArrayList<Integer> days) throws SQLException, ParseException {
		OracleConnection conn = getConnection();
		Statement stm = conn.createStatement();
		String startTime = timeOfDayDF.format(RoundTimeDown((Calendar)time.clone()));
		String endTime = timeOfDayDF.format(RoundTimeUp((Calendar)time.clone()));
		//SELECT TIME, TRAVEL_TIME FROM PATH#_EDGE_PATTERNS WHERE FROM, TO, START <= TOD AND END >= TOD
		String query = pmfQueryTemplate
				.replace("##PATH_NUM##", pathNumber)
				.replace("##FROM##", from)
				.replace("##TO##", to)
				.replace("##START_TIME##", startTime)
				.replace("##END_TIME##", endTime);
		OracleResultSet ors = (OracleResultSet) stm.executeQuery(query);
		ArrayList<Double> travelTimes = new ArrayList<Double>();
		int daysIdx = 0;
		while (ors.next()) {
			Calendar dayCal = Calendar.getInstance();
			dayCal.setTime(oracleDF.parse(ors.getString(1)));
			Integer day = dayCal.get(Calendar.DAY_OF_YEAR);
			while (days.get(daysIdx) < day) 
				if (daysIdx < days.size() - 1) 
					daysIdx++;
			if (days.get(daysIdx) == day)
				travelTimes.add(ors.getDouble(2));
		}
		ors.close();
		stm.close();
		conn.close();
		return travelTimes;
	}
	public static NormalDist getNormalDist(String pathNumber, String from, String to, Calendar time,
			ArrayList<Integer> days) throws SQLException, ParseException {
		ArrayList<Double> travelTimes = getTravelTimes(pathNumber, from, to, time, days);
		return new NormalDist(travelTimes);
	}
	
	public static PMF getPMF(String pathNumber, String from, String to, Calendar time,
			ArrayList<Integer> days) throws SQLException, ParseException{
		ArrayList<Double> travelTimes = getTravelTimes(pathNumber, from, to, time, days);
		return new PMF(travelTimes);
	}
}
