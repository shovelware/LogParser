package implemented;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import interfaces.HTMLWriter;
import interfaces.ReportSummary;
import interfaces.TestCase;

public class RawWriter implements HTMLWriter {

	String path_;
	String filename_;
	String filedate_;
	
	BufferedWriter output_;
	
	boolean readyToWrite_;
	DateTimeFormatter dateFormat_ = DateTimeFormatter.ofPattern("YYYY-MM-dd | HH:mm:ss");
	DateTimeFormatter timestampFormat_ = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	public RawWriter() {
		readyToWrite_ = false;
		filename_ = "RawReport";
		filedate_ = "000000_000000";
	}

	@Override
	public void beginReport(String path, ReportSummary summary) {
		try {
			path_ = path;
			filedate_ =  "_" + summary.getGenerationTime().format(DateTimeFormatter.ofPattern("ddMMYY_HHmmss")); 
			
			output_ = new BufferedWriter(new FileWriter(path_ + filename_ + ".txt"));
			readyToWrite_ = true;
			
			writeLine("********************");
			
			writeLine(summary.getTitle());
			
			write("Generation time: ");
			writeLine(summary.getGenerationTime().format(dateFormat_));
		
			write("Start time: ");
			writeLine(summary.getStartTime().format(dateFormat_));
			
			write("Runtime: ");
			writeLine(formatRuntime(summary.getRunTime()));
			
			write("Total: ");
			writeLine("" + summary.getTotalTests());
			
			write("Active: ");
			writeLine("" + summary.getTotalActiveTests());
			
			write("Pass: ");
			writeLine("" + summary.getPassTests());
			
			write("Skip: ");
			writeLine("" + summary.getSkipTests());
			
			write("Warn: ");
			writeLine("" + summary.getWarnTests());

			write("Fail: ");
			writeLine("" + summary.getTotalTests());
			
			write("Error: ");
			writeLine("" + summary.getTotalTests());
			
			writeLine("********************");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeTest(TestCase testcase) {
		if (readyToWrite_) {
			try {
				write("Name: ");
				writeLine(testcase.getName());
	
				write("Status: ");
				writeLine(testcase.getStatus());
	
				write("Start Time: ");
				writeLine(testcase.getStartTime().format(timestampFormat_));
	
				write("Runtime: ");
				writeLine(formatRuntime(testcase.getRunTime()));
	
				write("Log: ");
				writeLine("" + testcase.hasLog());
	
				write("Pics: ");
				writeLine("" + testcase.hasPics());
				writeLine("====================");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void endReport() {
		try {
			output_.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void write(String line) throws IOException {
		if (readyToWrite_)
		{
			output_.write(line);
		}
	}
	public void writeLine(String line) throws IOException {
		if (readyToWrite_) {
			output_.write(line);
			output_.newLine();
		}
	}
	
	private String formatRuntime(Duration duration)	{
		String dur = "";
		
		long difference = duration.getSeconds();
		
		long secondsInMinute = 60;
		long minutesInHour = secondsInMinute * 60;

		long elapsedHours = difference / minutesInHour;
		difference = difference % minutesInHour;

		long elapsedMinutes = difference / secondsInMinute;
		difference = difference % secondsInMinute;

		long elapsedSeconds = difference;
		
		dur += String.format("%02d", elapsedHours) + ":" + String.format("%02d", elapsedMinutes) + ":" + String.format("%02d", elapsedSeconds);
		
		return dur;
	}
}
