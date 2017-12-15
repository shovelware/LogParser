package implemented;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import interfaces.ReportSummary;
import interfaces.TestCase;

public class HTMLFileWriter implements interfaces.HTMLWriter{

	String overviewFile_ = "overviewtemp";
	String tableFile_ = "tabletemp";
	String detailsFile_ = "detailtemp";
	
	DateTimeFormatter dateFormat_ = DateTimeFormatter.ofPattern("YYYY-MM-dd | HH:mm:ss");
	DateTimeFormatter timestampFormat_ = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	String path_;
	
	String fileName_;
	String fileDate_;
	String fileType_;
	
	int testCount_;

	boolean openForTests_;
	boolean readyToWrite_;

	HTMLFileTurtle overview_;
	HTMLFileTurtle table_;
	HTMLFileTurtle details_;
	ReportSummary summary_;
	
	Map<String, String> languagePack_;	
	LinkedList<String> flags_;
	
	public HTMLFileWriter() {
		openForTests_ = false;
		
		fileName_ = "NO_SUMMARY";
		fileDate_ = "00NO00_0DATE0";
		fileType_ = ".html";
		
		testCount_ = 1;
		flags_ = new LinkedList<String>();
		
		setupLanguage("NL");
	}

	@Override
	public void setFlag(String flag) {
		flags_.add(flag);
	}

	@Override
	public void setOption(String option, String value) {
		if (option.equalsIgnoreCase("language")) {
			if (value.equalsIgnoreCase("EN")) { setupLanguage("EN"); }
			else if (value.equalsIgnoreCase("NL")) { setupLanguage("NL"); }
			else setupLanguage("NL");
		}
	}
	
	public void setupLanguage(String language) {
		languagePack_ = new HashMap<String, String>();
		
		if (language.equalsIgnoreCase("NL"))
		{
			languagePack_.put("runtime", "Totale Looptijd");
			languagePack_.put("gentime", "Rapport Gegenereerd");
			languagePack_.put("starttime", "Testing Begonnen");
			
			languagePack_.put("testcase", "TestCase");
			languagePack_.put("status", "Status");
			
			languagePack_.put("percent", "Percentage Geslaagd");
			languagePack_.put("donut", "Test Resultaten");
			
			languagePack_.put("timestamp", "Tijd");
			languagePack_.put("level", "Level");
			languagePack_.put("source", "Bron");
			languagePack_.put("log", "Log");
			
			languagePack_.put("pass", "Geslaagd");
			languagePack_.put("skip", "Overgeslagen");
			languagePack_.put("error", "Fout");
			languagePack_.put("fail", "Niet Geslaagd");
			languagePack_.put("warn", "Waarschuwingen");
			languagePack_.put("unknown", "Onbekend");
			
			languagePack_.put("nologs", "Er zijn geen .log files gevonden!");
			languagePack_.put("nosum", "Algemeen file ontbreekt!");
		}
		
		else
		{
			languagePack_.put("runtime", "Total Runtime");
			languagePack_.put("gentime", "Report Generated");
			languagePack_.put("starttime", "Testing Started");
			
			languagePack_.put("testcase", "TestCase");
			languagePack_.put("status", "Status");
			
			languagePack_.put("percent", "Pass Percentage");
			languagePack_.put("donut", "Test Results");
			
			languagePack_.put("timestamp", "Time");
			languagePack_.put("level", "Level");
			languagePack_.put("source", "Source");
			languagePack_.put("log", "Log");
			
			languagePack_.put("pass", "Pass");
			languagePack_.put("skip", "Skipped");
			languagePack_.put("error", "Error");
			languagePack_.put("fail", "Fail");
			languagePack_.put("warn", "Warning");
			languagePack_.put("unknown", "Unknown");
			
			languagePack_.put("nologs", "No .log files found!");
			languagePack_.put("nosum", "No summary .log found!");
		}
	}
	
	public String getString(String key) {
		String value = languagePack_.get(key);
		
		if (value == null) value = key;//{ value = "I18N ERROR"; System.out.println("K: " +key);}
		
		return value;
	}
	
	@Override
	public void beginReport(String path, ReportSummary summary) {
		path_ = path;
		summary_ = summary;

		if (summary_.getTitle() != null) {
			
			if (summary_.getTitle().equalsIgnoreCase("$REPORT_TITLE$")) {
				fileName_ = "ERROR_REPORT";
			}
			
			else {
				fileName_ = summary_.getTitle(); 
			}
		}
		
		if (summary_.getGenerationTime() != null) { 
			fileDate_ =  "_" + summary_.getGenerationTime().format(DateTimeFormatter.ofPattern("ddMMYY_HHmmss")); 
		}
		
		//Open files
		try {
			overview_ = new HTMLFileTurtle(new BufferedWriter(new FileWriter(path_ + overviewFile_ + fileType_)));
			table_ = new HTMLFileTurtle(new BufferedWriter(new FileWriter(path_ + tableFile_ + fileType_)));
			details_ = new HTMLFileTurtle(new BufferedWriter(new FileWriter(path_ + detailsFile_ + fileType_)));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error opening temp file.");
		}
		readyToWrite_ = true;
		
		//Write to files
		try {
			openTable();
			openDetails();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error writing temp file.");
		}
		
		openForTests_ = true;
	}
	
	@Override
	public void writeTest(TestCase testcase) {
		if (readyToWrite_ && openForTests_) {
			//write to overview
			String line = "<tr><td class=\"testname\"><a class=\"testlink\" href=\"#testcard" + testCount_ +"\">" + testcase.getName();
			line += "</a></td><td class=\"result " + getHTMLbg(testcase.getStatus()) + "\">"; 
			line += getHTMLStatus(testcase.getStatus()) + "</td></tr>";
			
			try {
				table_.addLine(line);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error adding test to overview");
			}
			
			//write to details
			try {
				boolean hasData = testcase.hasLog() || testcase.hasPics();
				
				//Fix up some time calculations		
				String startTime = testcase.getStartTime().format(timestampFormat_);
				String runTime = formatRuntime(testcase.getRunTime());
				String collapseElement = "";
	
				if (hasData)
				{
					collapseElement = " data-toggle=\"collapse\" href=\"#testlog" + testCount_ + "\" aria-expanded=\"false\" aria-controls=\"testlog" +testCount_+ "\"";
				}
				
				details_.addLine("<div class=\"testwrapper\"><div id=\"testcard" + testCount_ + "\" class=\"card testentry\">");
				details_.indent();
				details_.addLine("<div class=\"cardheader " + getHTMLbg(testcase.getStatus()) + "\" "+ collapseElement+">");
				details_.indent();
				if (hasData) {
					//test.addLine("<span class=\"glyphicon glyphicon-plus expandarrow\" aria-hidden=\"true\" style=\"margin-left: 0.25em; float: left; clear: left;\"></span>");
				}
				details_.addLine("<span class=\"title\">" + testcase.getName() + "</span>");
				details_.addLine("<span class=\"time\"><span class=\"glyphicon glyphicon-time\" aria-hidden=\"true\"></span> " + startTime + "</span><br/>");
				details_.addLine("<span class=\"time\"><span class=\"glyphicon glyphicon-repeat\" aria-hidden=\"true\"></span> " + runTime + "</span><br/>");
				details_.dedent();	
				details_.addLine("</div>");
				
				if (hasData)
				{
					details_.addLine("<div class=\"collapse\" id=\"testlog" + testCount_ +"\">");
					details_.indent();
					
					if (testcase.hasLog())
					{
						details_.addLine("<div class=\"log\"><table>");
						details_.indent();
						
						interfaces.LogEntry[] log = testcase.getLog().clone();
						Set<String> keys = log[0].getData().keySet();
						
						String headers = "<tr>";
						for (String s : keys)
						{
							String is = getString(s);
							
							if (is != null) { s = is; }
							
							headers += "<th>" + s + "</th>";
						}
						headers += "</tr>";
						
						details_.addLine(headers);
						
						String lastDate = null;
						for (interfaces.LogEntry l : log)
						{
							if (l.getData().isEmpty() == false) 
							{
								details_.addLine(processLogLine(l, lastDate));
							}
							
							lastDate = l.getData().get("timestamp");
						}
						
						details_.dedent();
						details_.addLine("</table></div>");
					}
					
					if (testcase.hasPics()) {
						//If pics
						details_.addLine("<div class=\"gallery\">");
						details_.indent();
						
						for (String filename : testcase.getPics()) {
							details_.addLine("<img class=\"galleryimage\" src=\"imgs/" + filename + "\"/>");
						}
						
						details_.dedent();
						details_.addLine("</div>");
					}
					
					details_.dedent();
					details_.addLine("</div>");
					details_.dedent();
				}
				
				details_.addLine("</div></div>");
				details_.newLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error adding test to details");
			}
		}
		
		testCount_++;
		
		summary_.addTest(testcase.getName(), testcase.getStatus(), testcase.getRunTime());
	}

	@Override
	public void endReport() {
		//First add table to overview
		try {
			endTable();
			table_.close();
			openOverview();
			
			BufferedReader table = new BufferedReader(new FileReader(path_ + tableFile_ + fileType_));
			
			for(String line; (line = table.readLine()) != null; ) {
				overview_.addLine(line);
			}
			
			table.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error appending table to overview");
		}
		
		//Write end of files
		try {
			endOverview();
			endDetails();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error closing temp file.");
		}
		openForTests_ = false;
		
		//Close file writers		
		try {
			overview_.close();
			details_.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error closing file writers");
		}
	
		stitchFiles();
	}
	
	private void stitchFiles() {
		//Build full report
		try {
			BufferedReader overview = new BufferedReader(new FileReader(path_ + overviewFile_ + fileType_));
			BufferedReader details = new BufferedReader(new FileReader(path_ + detailsFile_ + fileType_));
			BufferedWriter finalReport = new BufferedWriter(new FileWriter(path_ + fileName_ + fileDate_ + fileType_));
			
			for(String line; (line = overview.readLine()) != null; ) {
				finalReport.write(line);
				finalReport.newLine();
			}
			for(String line; (line = details.readLine()) != null; ) {
				finalReport.write(line);
				finalReport.newLine();
			}
			
			overview.close();
			details.close();
			finalReport.close();
			
		} catch (IOException e) {
			System.out.println("Error finalizing report");
			e.printStackTrace();
		}
		
		//delete temps
		try {
			Files.deleteIfExists(Paths.get(path_ + overviewFile_ + fileType_));
			Files.deleteIfExists(Paths.get(path_ + detailsFile_ + fileType_));
			Files.deleteIfExists(Paths.get(path_ + tableFile_ + fileType_));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error deleting temp files");
		}
	}
	
	//
	private String processLogLine(interfaces.LogEntry line, String lastTime) {
		String row = "";
		String rowType = "";
		
		LinkedHashMap<String, String> data = line.getData();
		row += "<tr&&ROWTYPE&&>";
		
		for (String s : data.keySet())
		{
			String value = data.get(s).trim();
			//System.out.println(value);
			//System.out.println(row);
			
			if (s.equalsIgnoreCase("Timestamp") || s.equalsIgnoreCase("Datum"))
			{
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSS");
				LocalTime entryTime = LocalTime.parse(value, formatter);
				LocalTime lastTimeLT;
				
				if (lastTime == null) { lastTimeLT = entryTime; }
				else lastTimeLT = LocalTime.parse(lastTime, formatter);
				
				String timeBetween = getTimeBetween(lastTimeLT, entryTime);
				
				row += "<td class=\"timestamp\">" + value + "<div class=\"hovertext\"><div class=\"hoverwrap\">"+ timeBetween+"</div></div></td>";
				
				//row += "<td>" + value + "</td>";
			}
			
			else if (s.equalsIgnoreCase("Severity") || s.equalsIgnoreCase("Level"))
			{
				//Only highlight non-info rows
				rowType = (value.equals("Info") ? "" :  " class=\"" + getHTMLbg(value) + "\"");
				row += "<td>" + value + "</td>";
			}
			
			else if (s.equalsIgnoreCase("Source") || s.equalsIgnoreCase("Bron"))
			{
				if (value.equalsIgnoreCase("BasisScreenshot")) {

					row += "<td><span class=\"screenshot\">" + value + "</span></td>";
				}
				
				else row+="<td>" + value + "</td>";
			}
			
			else if (s.equalsIgnoreCase("Message") || s.equalsIgnoreCase("Log"))
			{
				//Screenshot link parsing
				if (value.contains("Screenshot gemaakt en opgeslagen"))
				{
					int beginI = value.indexOf("[");
					int nameI = value.lastIndexOf("\\");
					int endI = value.lastIndexOf("]");
					
					if (beginI != -1 && endI != -1 && nameI != -1)
					{
						String msgBegin ="";
						String msgEnd ="";
						String filename = "";
						
						msgBegin = value.substring(0, beginI+1);
						msgEnd = value.substring(endI, endI + 1);
						filename = value.substring(nameI + 1, endI);
						
						String imglink = "<a class=\"screenshot\" href=\"imgs/";
						imglink += filename;
						imglink += "\" target=\"_blank\">" + filename + "</a>";
												
						value = msgBegin + imglink + msgEnd;
					}
				}
				
				//Actually add msg to row
				row += "<td class=\"msg\">" + value + "</td>";
			}
				
			else row += "<td>" + value + "</td>";
		}
		
		row += "</tr>";
		row = row.replace("&&ROWTYPE&&", rowType);
		return row;
	}
	
	protected void openOverview() throws IOException {		
		overview_.addLine("<!DOCTYPE HTML>");
		overview_.addLine("<html lang=\"en\">");
		
		//Header
		overview_.indent();
		overview_.addLine("<head>");
		overview_.indent();
		overview_.addLine("<meta charset=\"UTF8\">");
		overview_.addLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
		overview_.addLine("<link rel=\"stylesheet\" href=\"scripts/bootstrap/css/bootstrap.css\">");
		overview_.addLine("<script src=\"scripts/jquery-3.2.1.js\"></script>");
		overview_.addLine("<script src=\"scripts/bootstrap/js/bootstrap.js\"></script>");
		overview_.addLine("<script src=\"scripts/chart.js\"></script>");
		overview_.addLine(String.format("<title>%s</title>", fileName_));
		overview_.addLine("<link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/base.css\"/>");
		overview_.dedent();
		overview_.addLine("</head>");
		
		//Open body
		overview_.addLine("<body>");
		overview_.indent();
		
		//Modal Display
		overview_.addLine("<div class=\"modal fade\" id=\"imagemodal\" tabindex=\"-1\" role=\"dialog\" aria-hidden=\"true\">");
		overview_.indent();
		overview_.addLine("<div class=\"modal-dialog modal-lg\"><div class=\"modal-content\"><div class=\"modal-body\">");
		overview_.indent();
		overview_.addLine("<a href=\"\" class=\"imagelink\" target=\"_blank\">");
		overview_.indent();
		overview_.addLine("<img src=\"\" class=\"imagepreview img-fluid\">");
		overview_.dedent();
		overview_.addLine("</a>");
		overview_.dedent();
		overview_.addLine("</div></div></div>");
		overview_.dedent();
		overview_.addLine("</div>");
		overview_.newLine();
	
		//Open Overview container
		overview_.addLine("<div class=\"container-fluid\">");
		
		overview_.indent();
		
		String generationDate = summary_.getGenerationTime().format(dateFormat_);
		String startDate = summary_.getStartTime().format(dateFormat_);
		String runTime = formatRuntime(summary_.getRunTime());
		
		//Summary card
		overview_.addLine("<div class=\"col-md-8 titlecard card\">");
		overview_.indent();
		overview_.addLine("<span class=\"logo\"><img src=\"imgs/logoklant.png\"></span>");
		overview_.addLine(String.format("<span class=\"title\"><h1>%s</h1></span><br/>", summary_.getTitle()));
		overview_.newLine();
		overview_.addLine("<span class=\"glyphicon glyphicon-file\" aria-hidden=\"true\"></span> " + getString("gentime") + ":");
		overview_.addLine(String.format("<span class=\"time\">%s</span><br/>", generationDate));
		overview_.addLine("<span class=\"glyphicon glyphicon-time\" aria-hidden=\"true\"></span> " + getString("starttime") + ":");
		overview_.addLine(String.format("<span class=\"time startdate\">%s</span><br/>", startDate));
		overview_.addLine("<span class=\"glyphicon glyphicon-repeat\" aria-hidden=\"true\"></span> " + getString("runtime") + ":");
		overview_.addLine(String.format("<span class=\"time runtime\">%s</span><br/><br/>", runTime));
		overview_.newLine();
	}
	
	protected void endOverview() throws IOException {
		//Close summary card
		overview_.dedent();
		overview_.addLine("</div>");
		overview_.newLine();	
		
		//Donut canvas
		overview_.addLine("<div class=\"col-md-4 card\"><div class=\"graph\">");
		overview_.indent();
		overview_.addLine("<canvas id=\"donut\" height=\"300\"></canvas>");
		overview_.dedent();
		overview_.addLine("</div></div>");
		overview_.dedent();
		overview_.newLine();
		
		//Bar canvas
		overview_.indent();	
		overview_.addLine("<div class=\"col-md-12 card\"><div class=\"graph\">");
		overview_.indent();
		overview_.addLine("<canvas id=\"bar\" height=\"100\"></canvas>");
		overview_.dedent();
		overview_.addLine("</div></div>");
		overview_.dedent();
		
		//Close overview div
		overview_.addLine( "</div>");
		overview_.newLine();
		
		//We pick up and display errors here, once we have information in the report
		boolean err = false;
		if (summary_.getTitle().equalsIgnoreCase("$REPORT_TITLE$")) {
			overview_.addLine("<div class=\"card errorbg\">");
			overview_.addLine(getString("nosum"));
			overview_.addLine("</div>");
			err = true;
		}
		
		//We're closing but we haven't written any tests!
		if (testCount_ < 2)
		{
			overview_.addLine("<div class=\"card errorbg\">");
			overview_.addLine(getString("nologs"));
			overview_.addLine("</div>");
			err = true;
		}
		
		//Divider
		overview_.addLine("<hr/>");
		overview_.newLine();
	}
	
	protected void openTable() throws IOException {
		table_.addLine("<div><table>");
		table_.addLine("<tr><th>"+ getString("testcase") + "</th><th>"+ getString("status") + "</th></tr>");
		table_.indent();
	}
	
	protected void endTable() throws IOException {
		table_.dedent();
		table_.addLine("</table>");
		table_.dedent();
		table_.addLine("</div>");
		table_.dedent();
	}
	
	protected void openDetails() throws IOException {
		//Open container
		details_.indent();
		details_.indent();
		details_.addLine("<div class=\"container-fluid\">");		
	}
	
	protected void endDetails() throws IOException {
		//Close tests container
		details_.addLine("</div>");
		details_.addLine("<div class=\"container-fluid\">");
		
		//Footer
		details_.addLine("<div class=\"navbar navbar-default navbar-bottom\">");
		details_.indent();
		
		details_.addLine("<div class=\"pull-left\">");
		details_.indent();
		String flags = "";
		int i = 0;
		for(String s : flags_) { flags += (++i < flags_.size() ? s + "  | " : s);} 
		details_.addLine("<span class=\"note\">" + flags + "</span>");
		details_.dedent();
		details_.addLine("</div>");
		
		details_.addLine("<div class=\"pull-right\">");
		details_.indent();
		details_.addLine("<span class=\"logotext\"></span>");
		details_.addLine("<a href=\"http://www.cerios.nl\" target=\"_blank\"><img class=\"logoimg\" src=\"imgs/were-cerios.png\"></a>");
		details_.dedent();
		details_.addLine("</div>");
		
		details_.dedent();
		details_.addLine("</div>");
		details_.addLine("</div>");
		
		//Scripts
		details_.addLine("<!--SCRIPTS-->");
		
		//Blink script
		details_.addLine("<script>");
		details_.addLine("$(function() {");
		details_.indent();
		details_.addLine("$('.testlink').on('click', function() {");
		details_.indent();
		details_.addLine("$($(this).attr('href')).fadeTo(100, 0.3, function() { $(this).fadeTo(500, 1.0, function() { $(this).fadeTo(100, 0.3, function() { $(this).fadeTo(500, 1.0); }); }); });");
		details_.dedent();
		details_.addLine("});");
		details_.dedent();
		details_.addLine("});");
		details_.dedent();
		details_.addLine("	</script>");
		
		//Modal script
		details_.addLine("<script>");
		details_.addLine("$(function() {");
		details_.indent();
		details_.addLine("$('.galleryimage').on('click', function() {");
		details_.indent();
		details_.addLine("$('.imagepreview').attr('src', $(this).attr('src'));");
		details_.addLine("$('.imagelink').attr('href', $(this).attr('src'));");
		details_.addLine("$('#imagemodal').modal('show');");
		details_.dedent();
		details_.addLine("});");
		details_.dedent();
		details_.addLine("});");
		details_.addLine("</script>");
		
		//Donut script
		details_.addLine("<script>");
		details_.indent();
		
		details_.addLine("var ctx = document.getElementById(\"donut\");");
		details_.addLine("var myChart = new Chart(ctx, {");
		details_.indent();
		details_.addLine("type: 'doughnut',");
		
		details_.addLine("data: {");
		details_.indent();
		details_.addLine("labels: [\"" + getString("skip") + "\", \"" + getString("pass") + "\", \"" + getString("warn") + "\", \"" + getString("fail") + "\", \"" + getString("error") + "\", \"" + getString("unknown") + "\"],");
		details_.addLine("datasets: [{");
		
		details_.addLine(String.format("data: [%d, %d, %d, %d, %d, %d],", summary_.getSkipTests(),  summary_.getPassTests(), summary_.getWarnTests(), summary_.getFailTests(), summary_.getErrorTests(), summary_.getUnkownTests()));

		details_.addLine("backgroundColor: [");
		details_.indent();
		details_.addLine("'rgba(90, 110, 230, 1)',");
		details_.addLine("'rgba(80, 200, 80, 1)',");
		details_.addLine("'rgba(255, 230, 100, 1)',");
		details_.addLine("'rgba(255, 175, 50, 1)',");
		details_.addLine("'rgba(255, 80, 50, 1)',");
		details_.addLine("'rgba(180, 0, 255, 1)'");
		details_.dedent();
		details_.addLine("],");
		details_.addLine("borderWidth: 2");
		details_.addLine("}]");
		details_.dedent();
		details_.addLine("},");
		
		details_.addLine("options: {");
		details_.indent();	
		details_.addLine("responsive: true,");
		details_.addLine("maintainAspectRatio: false,");
		details_.addLine("cutoutPercentage: 70,");
		details_.addLine("rotation: (0.5 * Math.PI),");
		details_.addLine("legend: { position: 'right' },");
		details_.addLine("title: { display: false, text: '"+ getString("results") + "' },");
		details_.addLine("tooltips: {	position: 'nearest' }");
		details_.dedent();
		
		details_.addLine("}");
		details_.dedent();
		details_.addLine("});");
		details_.dedent();
		details_.addLine("</script>");
		
		//Percent script
		DecimalFormat decimal = new DecimalFormat("0.00");
		
		float totalActiveTests = summary_.getTotalActiveTests();
		float passTests = summary_.getPassTests();
		
		float passPercent = 100.f *(passTests / (totalActiveTests == 0 ? 1 : totalActiveTests));
		float failPercent = 100.f - passPercent;
		
		details_.addLine("<script>");
		details_.indent();
		details_.addLine("var ctx = document.getElementById(\"bar\");");
		details_.addLine("var myBarChart = new Chart(ctx, {");
		details_.indent();
		details_.addLine("type: 'horizontalBar',");
		details_.addLine("data: {");
		details_.indent();
		details_.addLine("labels: [\"\"],");
		details_.addLine("datasets: [{");
		details_.addLine("stack: \"tests\",");
		details_.addLine("//label: \"" + getString("Pass") + "\",");
		details_.addLine(String.format("data: [%s],", decimal.format(passPercent)));
		details_.addLine("backgroundColor: ['rgba(80, 200, 80, 1)']");
		details_.addLine("},");
		details_.addLine("{");
		details_.addLine("stack: \"tests\",");
		details_.addLine("//label: \"" + getString("Fail") + "\",");
		details_.addLine(String.format("data: [%s],",decimal.format(failPercent)));
		details_.addLine("backgroundColor:['rgba(200, 200, 200, 1)']");
		details_.addLine("}]");
		details_.dedent();
		details_.addLine("},");
		details_.addLine("options: {");
		details_.indent();
		details_.addLine("responsive: true,");
		details_.addLine("maintainAspectRatio: false,");
		details_.addLine("title: { display: true,text: '" + getString("percent") + "' },");
		details_.addLine("scales: { xAxes: [{ stacked: 'true'}] },");
		details_.addLine("legend: { display: false, position: 'left' }");
		details_.dedent();
		details_.addLine("}");
		details_.dedent();
		details_.addLine("});");
		details_.dedent();
		details_.addLine("</script>");
	}
	
	private String formatRuntime(Duration duration)	{
		String durationString = "";
		
		long difference = duration.getSeconds();
		
		long secondsInMinute = 60;
		long minutesInHour = secondsInMinute * 60;

		long elapsedHours = difference / minutesInHour;
		difference = difference % minutesInHour;

		long elapsedMinutes = difference / secondsInMinute;
		difference = difference % secondsInMinute;

		long elapsedSeconds = difference;
		
		durationString += String.format("%02d", elapsedHours) + ":" + String.format("%02d", elapsedMinutes) + ":" + String.format("%02d", elapsedSeconds);
		
		return durationString;
	}
	
	private String getTimeBetween(LocalTime start, LocalTime until) {
		String time = "CALCERR";
		
	    long millis = ChronoUnit.MICROS.between(start, until) / 100;
	    long seconds = ChronoUnit.SECONDS.between(start, until);
	    long minutes = ChronoUnit.MINUTES.between(start, until);
	    
		time = String.format("%02d", minutes) +":"+ String.format("%02d", seconds)+":"+ String.format("%04d", millis);

		return time;
	}
	
	private String getHTMLStatus(String status)
	{
		String ret = "";

		ret = getString(status.toLowerCase());
		
		return ret == null ? "ERROR" : ret;
	}
	
	private String getHTMLbg(String status){
		String ret = "unknownbg";

		if (status.equalsIgnoreCase("pass") || status.equalsIgnoreCase("GESLAAGD")) { ret = "passbg"; }
		else if (status.equalsIgnoreCase("skip") || status.equalsIgnoreCase("OVERSLAAN")) { ret = "skipbg"; }
		else if (status.equalsIgnoreCase("error") || status.equalsIgnoreCase("FOUT")) { ret = "errorbg"; }
		else if (status.equalsIgnoreCase("fail") || status.equalsIgnoreCase("GEFAALD")){ ret = "failbg"; }
		else if (status.equalsIgnoreCase("warn") || status.equalsIgnoreCase("WAARSCHUWING")) { ret = "warnbg"; }
		
		return ret;
	}
}

class HTMLFileTurtle {
	protected int dent;
	protected static String newline = System.getProperty("line.separator");
	protected BufferedWriter writer;
	
	void indent() { dent = (dent + 1); }
	void dedent() { dent = (dent - 1 >= 0? dent - 1 : 0); }
	
	HTMLFileTurtle(BufferedWriter bw){ writer = bw; dent = 0; }
	HTMLFileTurtle(BufferedWriter bw, HTMLFileTurtle parent) { writer = bw; this.dent = (parent != null ? parent.getIndent() : 0); }
	HTMLFileTurtle(BufferedWriter bw, int indent) { writer = bw; dent = indent; }
	
	void newLine() throws IOException{ writer.write(newline); }	

	void addLine(String text) throws IOException {
		String html = new String();
		
		for (int i = 0; i < dent; ++i)
		{
			html+="\t";
		}
		
		html += text;
		html += newline;
		
		writer.write(html);
	}
	
	int getIndent() { return dent; }
	
	void close() throws IOException { writer.close(); }
}