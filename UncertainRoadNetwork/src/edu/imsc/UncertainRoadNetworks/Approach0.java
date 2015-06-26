package edu.imsc.UncertainRoadNetworks;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class Approach0 {
	
	public static NormalDist GenerateModel(String[] sensorList, String timeOfDay,
			ArrayList<Integer> days, Calendar queryTime) throws SQLException, ParseException{
		Double mean = Util.GetSnapshotTravelTime(sensorList, queryTime);
		if (mean == null) return null;
		return new NormalDist(mean, 0);
	}

	public static Double GenerateActual(String[] sensorList, 
			Calendar queryTime) throws SQLException, ParseException {
		return Util.GetActualTravelTime(sensorList, queryTime);
	}

}
