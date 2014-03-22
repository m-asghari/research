import java.util.ArrayList;
import java.util.Calendar;

public class Main {
	public static void main(String[] args) {
		String path = "768701-774344-770599-768297-768283-770587-770012-770024-770036-770354-770048-770331-770544-770061-770556-770076-771202-770089-770103-771636-770475-770487-770116-769895-769880-769866-769847-768230-767610-767598-718076-767471-718072-767454-762329-767621-767573-718066-767542-718064-767495-718375-716955-718370-716949-760650-718045-718173-760643-760635-774671-718166-764037";
		String[] sensorList = path.split("-");
		int pathNum = 2;
		String pathNumber = Integer.toString(pathNum);
		try {
			Calendar modelStartTime = Calendar.getInstance();
			modelStartTime.setTime(Util.oracleDF.parse("07-JAN-13 09.00.00.0 AM"));
			String timeOfDay = Util.timeOfDayDF.format(modelStartTime.getTime());
			StartTimeGenerator stg = new StartTimeGenerator(modelStartTime);
			ArrayList<Calendar> modelStartTimes = stg.GetStartTimes();
			ArrayList<Integer> days = new ArrayList<Integer>();
			for (Calendar startTime : modelStartTimes)
				days.add(startTime.get(Calendar.DAY_OF_YEAR));
			Pair<PMF, PMF> model = Approach6.GenerateModel(pathNumber, sensorList, timeOfDay, days);
			PMF test = Approach6.GenerateActual(pathNumber, sensorList, modelStartTimes);
			System.out.print(model.toString());
			System.out.print(test.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
