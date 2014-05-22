package edu.imsc.UncertainRoadNetworks;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class StartTimeGenerator {

	public HashMap<Integer, Integer> nextDay;
	public int hour, minute;
	public Calendar startTime;
	public int k;
	
	public StartTimeGenerator(Calendar startTime, int k) {
		this.startTime = (Calendar) startTime.clone();
		this.k = k;
		this.hour = startTime.get(Calendar.HOUR_OF_DAY);
		this.minute = startTime.get(Calendar.MINUTE);
		nextDay = new HashMap<Integer, Integer>();
		this.nextDay.put(Calendar.SATURDAY, 1);
		this.nextDay.put(Calendar.SUNDAY, 6);
		this.nextDay.put(Calendar.MONDAY, 1);
		this.nextDay.put(Calendar.TUESDAY, 1);
		this.nextDay.put(Calendar.WEDNESDAY, 1);
		this.nextDay.put(Calendar.THURSDAY, 1);
		this.nextDay.put(Calendar.FRIDAY, 3);
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
	
	public ArrayList<ArrayList<Calendar>> GetStartTimes() 
			throws ParseException{
		ArrayList<ArrayList<Calendar>> startTimes = new ArrayList<ArrayList<Calendar>>();
		for (int i = 0; i < k; ++i)
			startTimes.add(new ArrayList<Calendar>());
		Calendar start = Calendar.getInstance();
		start.setTime(this.startTime.getTime());
		Calendar end = Calendar.getInstance();
		end.setTime(Util.oracleDF.parse("01-JAN-14 00.00.00.000 AM"));
		int counter = 0;
		while (start.before(end)) {
			Util.days.add(start.get(Calendar.DAY_OF_YEAR));
			startTimes.get(counter++ % k).add((Calendar)start.clone());
			start.add(Calendar.DAY_OF_YEAR, nextDay.get(start.get(Calendar.DAY_OF_WEEK)));
		}
		return startTimes;
	}
}
