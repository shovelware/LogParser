package implemented;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import interfaces.LogEntry;
import interfaces.ReportSummary;
import interfaces.TestCase;

public class LogFileReader implements interfaces.FileReader {
	List<String> fileList_;
	List<String> imageList_;
	
	List<String> skippedList_;
	
	int fileIndex_;
	int imageIndex_;
	
	String path_;
	String summaryFile_;
	
	boolean debug_;
	
	public LogFileReader() {
		fileList_ = new ArrayList<String>();
		imageList_ = new ArrayList<String>();
		
		fileIndex_ = 0;
		imageIndex_ = 0;
		
		summaryFile_ = "";
		
		skippedList_ = new ArrayList<String>();
		
		debug_ = true;
	}

	@Override
	public void scanDir(String path) {
		path_ = path;
		
		File f = new File(path_);
		
		List<String> fileList = new ArrayList<String>(Arrays.asList(f.list()));
		
		//Filter for .log files
		for (String s : fileList) {	
			//skip dirs
			if (new File("" + path_ + s).isDirectory()) {
				continue;
			}	
			
			String filename = s.split("\\.(?=[^\\.]+$)")[0];
			String extension = s.split("\\.(?=[^\\.]+$)")[1];
			
			if (filename.substring(0, filename.indexOf("_")).equals("Algemeen"))
			{
				if (summaryFile_.equals(""))
				{
					summaryFile_ = filename;
				}
				
				else
				{
					System.out.print("Multiple summaries found in folder");
				}
			}
			
			else if (extension.equals("log") == true) {
				fileList_.add(s);
			}
			
			else if (extension.equals("jpg") == true ||
					 extension.equals("png") == true) {
				imageList_.add(s);
			}
		}
		
		if (summaryFile_.equalsIgnoreCase(""))
		{
			System.out.println("ERROR: NO SUMMARY FOUND");
		}
		
		if (fileList_.size() == 0)
		{
			System.out.println("ERROR: NO LOGS FOUND");
		}
	}

	@Override
	public ReportSummary parseSummary() {
		ReportSummary sum = new ReportSummaryImpl();
		
		System.out.println("Summary file: " + summaryFile_);
		
		if (summaryFile_.equals("")) {
			return sum;
		}
		
		String title = summaryFile_.split("_", 2)[1].split("_\\d+{2}_\\d+{2}_\\d+{4}", 2)[0].trim();
		
		sum.setTitle(title);
		
		System.out.println("Summary: " + sum.getTitle());	
		
		try(BufferedReader br = new BufferedReader(new FileReader(path_ + summaryFile_ + ".log"))) {			
		    boolean startDate = false;
			for(String line; (line = br.readLine()) != null; ) {
				
		    	String[] splits = line.split(" \\| ");
				
				if (splits.length > 1)
				{
					for (String s : splits)
					{
						String[] info = s.split(" = ", 2);
						
						if (startDate == false && 
							(info[0].equalsIgnoreCase("Date") || info[0].equalsIgnoreCase("Datum")))
						{
							LocalDateTime start = formatDate(info[1]);
							sum.setStartTime(start);
							startDate = true;
						}
						
						if (info[0].equalsIgnoreCase("Message") || info[0].equalsIgnoreCase("Log"))
						{
							if (info[1].contains("uitgesloten")) 
							{
								//interfaces.TestCase tc = new TestCaseImpl();
								//tc.setName(info[1].split(" is")[0]);
								//tc.setRunTime(0);
								//tc.setStatus("Skip");
								//tc.setStartTime(summary_.getGenerationTime());
							    //summary_.addTest(tc.getName(), tc.getStatus());
							    
							    skippedList_.add(info[1].split(" is")[0]);
							    sum.addTest("", "Skip", Duration.ofSeconds(0));
							}
						}
					}
				}
		    }
		}
		catch (FileNotFoundException e) {
			System.out.println("Error: Could not find file: " + path_ + summaryFile_);
			e.printStackTrace();
		}
		catch (IOException e) {
			System.out.println("Error: Problem reading file.");
			e.printStackTrace();
		}
		
		return sum;
	}

	@Override
	public TestCase parseNextTest() {
		String filename = fileList_.get(fileIndex_);
		String testName = filename.split("_Run")[0];
		System.out.println(testName);
		
		interfaces.TestCase newTest = new implemented.TestCaseImpl();
		newTest.setName(testName);
		
		for (String file : imageList_) {
			String imageOwner = file.split("_Run")[0];
			//System.out.println(imageOwner + " : " + testName + " = " + imageOwner.equals(testName));
			if (imageOwner.equals(testName)) {
				newTest.addPic(file);
			}
		}
	
		//Pull log entries into testEntry
		try(BufferedReader br = new BufferedReader(new FileReader(path_ + filename))) {
				
		boolean firstLine = true;
		String lastLine = "";
			
		for(String line; (line = br.readLine()) != null; ) {
		   		if (firstLine) {
		   			if (!line.contains("Logging Gestart") & line.contains("FunctiesTA.START")) { 
		   				startTest(newTest, line);
		   				firstLine = false;
		   			}
		   		}
			    	
		   			LogEntry l = processLine(line);

		   			if (l != null)
		   			{
		   				newTest.addLog((interfaces.LogEntry) l);
		   			}
		   			
		   			lastLine = line;
		   		}
			    
			    endTest(newTest, lastLine);
			    //summary_.addTest(newTest.getName(), newTest.getStatus());
			    
			    //Catch unstatused tests
			    if (newTest.getStatus() == "") { newTest.setStatus("unknown"); }
			}
			catch (FileNotFoundException e) {
				System.console().writer().println("Error: Could not find file.");
				e.printStackTrace();
			}
			catch (IOException e) {
				System.console().writer().println("Error: Problem reading file.");
				e.printStackTrace();
			}
			
			finally {
				fileIndex_++;
			}
		
			return newTest;
		}
		

	@Override
	public boolean hasNextTest() {
		return fileIndex_ < fileList_.size();
	}
		
	private LocalDateTime formatDate(String date)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS");
		LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
		return dateTime;
	}
	
	private LogEntry processLine(String text)
	{
		LogEntry l = new LogEntryImpl();
		
		String[] splits = text.split(" \\| ");
		boolean addLine = false;
		
		if (splits.length > 1)
		{
			//For deciding if we want this line
			addLine = true;
			
			//Step through each column
			for (String s : splits)
			{
				String[] info = s.split(" = ", 2);
				
				//Log filtering here
				if (info[0].equals("ThreadId")) { continue; }
				
				if (info[0].equalsIgnoreCase("Date") || info[0].equalsIgnoreCase("Datum")) {
					info[1] = info[1].substring(info[1].lastIndexOf(' ') + 1);
					info[0] = "timestamp";
				}
				
				if (info[0].equalsIgnoreCase("Severity") || info[0].equalsIgnoreCase("Level")) {
					info[0] = "level";
					//Debug off, don't include lines with debug		
					if (!debug_ && info[1].trim().equalsIgnoreCase("Debug"))
					{
						addLine = false;;
					}
				}
				
				if (info[0].equalsIgnoreCase("Source") || info[0].equalsIgnoreCase("Bron")) { 
					info[1] = info[1].substring(info[1].lastIndexOf('.') + 1);
					info[0] = "source";
				}
				
				
				if (info[0].equalsIgnoreCase("Message") || info[0].equalsIgnoreCase("Log")) {
					info[0] = "log";
				}
				
				l.addData(info[0], info[1]);
			}
		}
		
		if (addLine)
		{
			return l;
		}
		
		else return null;
	}
	
	private void startTest(interfaces.TestCase entry, String line)
	{
		String[] splits = line.split(" \\| ");
		
		if (splits.length > 1)
		{
			for (String s : splits)
			{
				String[] info = s.split(" = ", 2);
				
				if (info[0].equalsIgnoreCase("Date") || info[0].equalsIgnoreCase("Datum")) {
					LocalDateTime testDate = formatDate(info[1]);
					entry.setStartTime(testDate);
				}
				
				else if (info[0].equalsIgnoreCase("Message") || info[0].equalsIgnoreCase("Log"))	{
					entry.setName(info[1]);
				}
			}
		}	
	}
	
	private void endTest(interfaces.TestCase entry, String line)
	{
		String[] splits = line.split(" \\| ");
		
		if (splits.length > 1)
		{
			for (String s : splits)
			{
				String[] info = s.split(" = ", 2);
				if (info[0].equalsIgnoreCase("Date") || info[0].equalsIgnoreCase("Datum"))
				{
					LocalDateTime endTime = formatDate(info[1]);
					long testTime = Duration.between(entry.getStartTime(), endTime).getSeconds();
					
				    entry.setRunTime(testTime);
				    
				    //summary_.setRunTime(summary_.getRunTime().getSeconds() + testTime);				    
				}
				
				if (info[0].equalsIgnoreCase("Message") || info[0].equalsIgnoreCase("Log"))
				{
					String status = info[1];
					status = status.substring(status.indexOf(": ") + 2);
					
					if (status.equalsIgnoreCase("GESLAAGD")) entry.setStatus("Pass");
					else if (status.equalsIgnoreCase("WAARSCHUWINGEN")) entry.setStatus("Warn");
					else if (status.equalsIgnoreCase("FOUT")) entry.setStatus("Error");
					else if (status.equalsIgnoreCase("NIET GESLAAGD")) entry.setStatus("Fail");
					else entry.setStatus("Unknown");
				}
			}
		}	
	}
}
