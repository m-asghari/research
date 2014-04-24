package edu.imsc.UncertainRoadNetworks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logging {
	private FileWriter fw;
	private BufferedWriter bw;

	private DateFormat sdf = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
	
	public Logging() {
		try {
			this.fw = new FileWriter("logs.log");
			this.bw = new BufferedWriter(fw);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Logging(String filename) {
		try {
			this.fw = new FileWriter(filename);
			this.bw = new BufferedWriter(fw);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.bw.close();
		this.fw.close();
		super.finalize();
	}
	
	public void Add(String log) {
		try {
			String time = sdf.format(Calendar.getInstance().getTime());
			String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			this.bw.write(String.format("%s %s.%s(%d): ", time, className, methodName, lineNumber));
			this.bw.write(log);
			this.bw.write("\n");
			this.bw.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
