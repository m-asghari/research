import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class StartTimeGenerator {

	public HashMap<Integer, Integer> nextDay;
	public int hour, minute;
	public Calendar startTime;
	
	public StartTimeGenerator(Calendar startTime) {
		this.startTime = (Calendar) startTime.clone();
		this.hour = startTime.get(Calendar.HOUR_OF_DAY);
		this.minute = startTime.get(Calendar.MINUTE);
		nextDay = new HashMap<Integer, Integer>();
		this.nextDay.put(Calendar.SATURDAY, 8);
		this.nextDay.put(Calendar.SUNDAY, 6);
		this.nextDay.put(Calendar.MONDAY, 2);
		this.nextDay.put(Calendar.TUESDAY, 2);
		this.nextDay.put(Calendar.WEDNESDAY, 6);
		this.nextDay.put(Calendar.THURSDAY, 4);
		this.nextDay.put(Calendar.FRIDAY, 14);
	}
	
	public void SetNextDays(int[] input) {
		if (input.length != 7) return;
		this.nextDay.put(Calendar.SATURDAY, input[0]);
		this.nextDay.put(Calendar.SUNDAY, input[1]);
		this.nextDay.put(Calendar.MONDAY, input[2]);
		this.nextDay.put(Calendar.TUESDAY, input[3]);
		this.nextDay.put(Calendar.WEDNESDAY, input[4]);
		this.nextDay.put(Calendar.THURSDAY, input[5]);
		this.nextDay.put(Calendar.FRIDAY, input[6]);
	}
	
	public ArrayList<Calendar> GetStartTimes(Calendar startFrom) 
			throws ParseException{
		ArrayList<Calendar> startTimes = new ArrayList<Calendar>();
		Calendar start = Calendar.getInstance();
		start.setTime(startFrom.getTime());
		Calendar end = Calendar.getInstance();
		end.setTime(Util.oracleDF.parse("01-JAN-14 00.00.00.000 AM"));
		while (start.before(end)) {
			startTimes.add((Calendar)start.clone());
			start.add(Calendar.DAY_OF_YEAR, nextDay.get(start.get(Calendar.DAY_OF_WEEK)));
		}
		return startTimes;
	}
}
