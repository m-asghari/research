import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleResultSet;

public class Main {
	
	
	
	public static void main(String[] args) {
		String[] paths = {"768701-774344-718405-759858-759850-759835-759844-759822-718393-718392-759250-759228-717006-717004-715996-764151-717002-717614-716573-717613-716571-761070-717612-717610-717608-764101-768066-717490-717489-717488-764766-717486-769405-769403-717484-769388-717481-717479-769373-717472-717468-717466-717462-717461-717458-716339-717453-717450-717446-716331-716328-764853-718173-760643-760635-774671-718166-764037",
				"768701-774344-770599-768297-768283-770587-770012-770024-770036-770354-770048-770331-770544-770061-770556-770076-771202-770089-770103-771636-770475-770487-770116-769895-769880-769866-769847-768230-767610-767598-718076-767471-718072-767454-762329-767621-767573-718066-767542-718064-767495-718375-716955-718370-716949-760650-718045-718173-760643-760635-774671-718166-764037"};
		
		int pathNum = 0;
		for (String path : paths) {
			pathNum++;
			String pathNumber = Integer.toString(pathNum);
			String[] sensorList = path.split("-");
			try {
				DataPreparation.ClearPathSensorTable(pathNumber);
				DataPreparation.PopulatePathSensorTable(pathNumber, sensorList);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}
}
