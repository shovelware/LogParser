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

import interfaces.ReportSummary;
import interfaces.LogEntry;
import interfaces.TestCase;

public class LogFileReader implements interfaces.FileReader {

	List<interfaces.TestCase> testEntries_;
	List<interfaces.TestCase> skipEntries_;
	ReportSummary summary_;
	
	public LogFileReader() {
	}

	@Override
	public TestCase[] getEntries() {
		TestCase[] ret = new TestCase[testEntries_.size()];
		return testEntries_.toArray(ret);
	}
	
	@Override
	public ReportSummary getReport() {
		return summary_;
	}
	
	@Override
	public void readDir(String path) {
		List<String> fileList = new ArrayList<String>();
		ArrayList<String> imagelist = new ArrayList<String>();
		List<String> cleanFileList = new ArrayList<String>();
		
		File f = new File(path);
		
		testEntries_ = new ArrayList<interfaces.TestCase>();
		skipEntries_ = new ArrayList<interfaces.TestCase>();
		
		summary_ = new ReportSummaryImpl();
		summary_.setGenerationTime(LocalDateTime.now());
		
		//Get files in path
		fileList = new ArrayList<String>(Arrays.asList(f.list()));
		
		//Filter for .log files
		for (String s : fileList) {	
			//skip dirs
			if (new File("" + path + s).isDirectory()) {
				continue;
			}	
			
			String filename = s.split("\\.(?=[^\\.]+$)")[0];
			String extension = s.split("\\.(?=[^\\.]+$)")[1];
			
			if (filename.substring(0, filename.indexOf("_")).equals("Algemeen"))
			{
				System.out.println("Summary: " + filename);
				summary_.setTitle(filename.split("_")[1].trim());
				System.out.println(summary_.getTitle());
				generateOverview(path + s);
			}
			
			else if (extension.equals("log") == true) {
				cleanFileList.add(s);
			}
			
			else if (extension.equals("jpg") == true ||
					 extension.equals("png") == true) {
				imagelist.add(s);
			}
		}
		
		//Parse each file
		String lastTest = "";
		for (String n : cleanFileList) {
			
			String testName = n.split("_Run")[0];
			System.out.println(testName);
			if (lastTest.equals(testName) == false) {
				lastTest = testName;
				
				interfaces.TestCase newTest = new implemented.TestCaseImpl();
				newTest.setName(testName);
				
				for (String file : imagelist) {
					String imageOwner = file.split("_Run")[0];
					System.out.println(imageOwner + " : " + testName );
					if (imageOwner.equals(testName)) {
						newTest.addPic(file);
					}
				}
				
				testEntries_.add(newTest);
			}
			
			interfaces.TestCase curTest = testEntries_.get(testEntries_.size() - 1);
			
			//Pull log entries into testEntry
			try(BufferedReader br = new BufferedReader(new FileReader(path + n))) {
				
				boolean firstLine = true;
				String lastLine = "";
				
			    for(String line; (line = br.readLine()) != null; ) {
			    	if (firstLine) {
			    		if (line.contains("Logging Gestart")) { continue; }
			    		startTest(curTest, line);
			    		firstLine = false;
			    	}
			    	
			    	LogEntry l = processLine(line);
			    	
			    	updateStatus(curTest, l);
			    	
			    	curTest.addLog((interfaces.LogEntry) l);
			    	lastLine = line;
			    }
			    
			    endTest(curTest, lastLine);
			    summary_.addTest(curTest.getName(), curTest.getStatus());
			}
			catch (FileNotFoundException e) {
				System.console().writer().println("Error: Could not find file.");
				e.printStackTrace();
			}
			catch (IOException e) {
				System.console().writer().println("Error: Problem reading file.");
				e.printStackTrace();
			}
			
		}
		
		//testEntries_.addAll(skipEntries_);
	}

	public void generateOverview(String filepath) {
		try(BufferedReader br = new BufferedReader(new FileReader(filepath))) {			
		    boolean startDate = false;
			for(String line; (line = br.readLine()) != null; ) {
				
		    	String[] splits = line.split(" \\| ");
				
				if (splits.length > 1)
				{
					for (String s : splits)
					{
						String[] info = s.split(" = ", 2);
						
						if (startDate == false && info[0].equals("Date"))
						{
							LocalDateTime start = formatDate(info[1]);
							summary_.setStartTime(start);
							startDate = true;
						}
						
						if (info[0].equals("Message"))
						{
							if (info[1].contains("uitgesloten")) 
							{
								interfaces.TestCase tc = new TestCaseImpl();
								tc.setName(info[1].split(" is")[0]);
								tc.setRunTime(0);
								tc.setStatus("Skip");
								tc.setStartTime(summary_.getGenerationTime());
								skipEntries_.add(tc);
							    summary_.addTest(tc.getName(), tc.getStatus());
							}
						}
					}
				}
		    }
		    
		}
		catch (FileNotFoundException e) {
			System.console().writer().println("Error: Could not find file.");
			e.printStackTrace();
		}
		catch (IOException e) {
			System.console().writer().println("Error: Problem reading file.");
			e.printStackTrace();
		}
	}
	
	private LogEntry processLine(String text)
	{
		LogEntry l = new LogEntryImpl();
		
		String[] splits = text.split(" \\| ");
		
		//Protect against newlines breaking everything, xml solves
		if (splits.length > 1)
		{
			for (String s : splits)
			{
				String[] info = s.split(" = ", 2);
				
				//Log filtering here
				if (info[0].equals("ThreadId")) { continue; }
				if (info[0].equals("Source")) { info[1] = info[1].substring(info[1].lastIndexOf('.') + 1); }
				if (info[0].equals("Date")) { info[1] = info[1].substring(info[1].lastIndexOf(' ') + 1); }
				
				l.addData(info[0], info[1]);
			}
		}
		
		return l;
	}
	
	private void updateStatus(interfaces.TestCase entry, interfaces.LogEntry log) {
		if (log.getData().containsKey("Severity"))
		{			
			if (log.getData().get("Source").equals("STOP"))
			{
				String status = log.getData().get("Message");
				status = status.substring(status.indexOf(": ") + 2);
				
				if (status.equals("GESLAAGD")) entry.setStatus("Pass");
				if (status.equals("WAARSCHUWINGEN")) entry.setStatus("Warn");
				if (status.equals("FOUT")) entry.setStatus("Error");
				if (status.equals("NIET GESLAAGD")) entry.setStatus("Fail");
			}
		}
	}
	
	private void startTest(interfaces.TestCase entry, String line)
	{
		String[] splits = line.split(" \\| ");
		
		if (splits.length > 1)
		{
			for (String s : splits)
			{
				String[] info = s.split(" = ", 2);
				
				if (info[0].equals("Date")) {
					LocalDateTime testDate = formatDate(info[1]);
					entry.setStartTime(testDate);
					
				}
				
				else if (info[0].equals("Message"))	{
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
				if (info[0].equals("Date"))
				{
					LocalDateTime endTime = formatDate(info[1]);
					long testTime = Duration.between(entry.getStartTime(), endTime).getSeconds();
					
				    entry.setRunTime(testTime);
				    
				    summary_.setRunTime(summary_.getRunTime().getSeconds() + testTime);
					//also status
				}
			}
		}	
	}
	
	private LocalDateTime formatDate(String date)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
		return dateTime;
	}
}
