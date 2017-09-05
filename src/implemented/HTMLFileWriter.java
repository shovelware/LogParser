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
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
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
	
	public HTMLFileWriter() {
		openForTests_ = false;
		
		fileName_ = "Report";
		fileDate_ = "00NO00_0DATE0";
		fileType_ = ".html";
		
		testCount_ = 1;
	}

	@Override
	public void beginReport(String path, ReportSummary summary) {
		path_ = path;
		summary_ = summary;
		
		if (summary_.getTitle() != null)	{ fileName_ = summary_.getTitle(); }
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
			line += "</a></td><td class=\"result " + getHTMLStatus(testcase.getStatus()).toLowerCase() + "bg\">"; 
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
				
				details_.addLine("<div class=\"highlight\"><div class=\"card testEntry\" id=\"testcard" + testCount_ + "\">");
				details_.indent();
				details_.addLine("<div class=\"cardheader " + getHTMLStatus(testcase.getStatus()).toLowerCase() + "bg\" "+ collapseElement+">");
				details_.indent();
				if (hasData) {
					//test.addLine("<span class=\"glyphicon glyphicon-plus expandarrow\" aria-hidden=\"true\" style=\"margin-left: 0.25em; float: left; clear: left;\"></span>");
				}
				details_.addLine("<span class=\"title\">" + testcase.getName() + "</span>");
				//test.addLine("<span class=\"status " + getHTMLStatus(t.getStatus()) + "\">" + t.getStatus() + "</span>");
				details_.addLine("<span class=\"time start\"><span class=\"glyphicon glyphicon-time\" aria-hidden=\"true\"></span> " + startTime + "</span><br/>");
				details_.addLine("<span class=\"time run\"><span class=\"glyphicon glyphicon-repeat\" aria-hidden=\"true\"></span> " + runTime + "</span><br/>");
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
							headers += "<th>" + s + "</th>";
						}
						headers += "</tr>";
						
						details_.addLine(headers);
						
						for (interfaces.LogEntry l : log)
						{
							if (l.getData().isEmpty() == false) 
							{
								details_.addLine(processLogLine(l));
							}
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
	private String processLogLine(interfaces.LogEntry line) {
		String row = "";
		String rowType = "";
		
		LinkedHashMap<String, String> data = line.getData();
		row += "<tr&&ROWTYPE&&>";
		
		for (String s : data.keySet())
		{
			String value = data.get(s).trim();
			
			if (s.equals("Severity"))
			{
				//Only highlight non-info rows
				rowType = (value.equals("Info") ? "" :  " class = \"" + getHTMLStatus(value) + "bg\"");
			}
			
			if (s.equals("Message"))
			{
				row += "<td class=\"msg\">" + value + "</td>";
				
				if (value.equals("De test is succesful afgerond zonder fouten."))  { rowType = " class = \"pass\"";}
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
		overview_.addLine("<style>" + css() + "</style>");
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
		overview_.addLine("<span class=\"glyphicon glyphicon-file\" aria-hidden=\"true\"></span> Report generated:");
		overview_.addLine(String.format("<span class=\"time\">%s</span><br/>", generationDate));
		overview_.addLine("<span class=\"glyphicon glyphicon-time\" aria-hidden=\"true\"></span> Testing started:");
		overview_.addLine(String.format("<span class=\"time startdate\">%s</span><br/>", startDate));
		overview_.addLine("<span class=\"glyphicon glyphicon-repeat\" aria-hidden=\"true\"></span> Total runtime:");
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
		
		//Divider
		overview_.addLine("<hr/>");
		overview_.newLine();
	}
	
	protected void openTable() throws IOException {
		table_.addLine("<div><table>");
		table_.addLine("<tr><th>Testcase</th><th>Status</th></tr>");
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
		details_.addLine("labels: [\"Skip\", \"Pass\", \"Warn\", \"Fail\", \"Error\"],");
		details_.addLine("datasets: [{");
		
		//TODO:
		details_.addLine(String.format("data: [%d, %d, %d, %d, %d],", summary_.getSkipTests(),  summary_.getPassTests(), summary_.getWarnTests(), summary_.getFailTests(), summary_.getErrorTests()));
		//details_.addLine(String.format("data: [%d, %d, %d, %d, %d],", 10,20,30,40,50));
		
		details_.addLine("backgroundColor: [");
		details_.indent();
		details_.addLine("'rgba(90, 110, 230, 1)',");
		details_.addLine("'rgba(80, 200, 80, 1)',");
		details_.addLine("'rgba(255, 230, 100, 1)',");
		details_.addLine("'rgba(255, 175, 50, 1)',");
		details_.addLine("'rgba(255, 80, 50, 1)'");
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
		details_.addLine("title: { display: false, text: 'Test Results' },");
		details_.addLine("tooltips: {	position: 'nearest' }");
		details_.dedent();
		
		details_.addLine("}");
		details_.dedent();
		details_.addLine("});");
		details_.dedent();
		details_.addLine("</script>");
		
		//Percent script
		DecimalFormat decimal = new DecimalFormat("0.00");
		
		//TODO:
		float totalActiveTests = summary_.getTotalActiveTests();
		float passTests = summary_.getPassTests();
		
		float passPercent = 100.f *(passTests / (totalActiveTests == 0 ? 1 : totalActiveTests));
		float failPercent = 100.f - passPercent;
		
		//System.out.println(passPercent + " : " + decimal.format(passPercent));
		//System.out.println(failPercent + " : " + decimal.format(failPercent));
		
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
		details_.addLine("//label: \"Passed\",");
		details_.addLine(String.format("data: [%s],", decimal.format(passPercent)));
		details_.addLine("backgroundColor: ['rgba(80, 200, 80, 1)']");
		details_.addLine("},");
		details_.addLine("{");
		details_.addLine("stack: \"tests\",");
		details_.addLine("//label: \"Failed\",");
		details_.addLine(String.format("data: [%s],",decimal.format(failPercent)));
		details_.addLine("backgroundColor:['rgba(200, 200, 200, 1)']");
		details_.addLine("}]");
		details_.dedent();
		details_.addLine("},");
		details_.addLine("options: {");
		details_.indent();
		details_.addLine("responsive: true,");
		details_.addLine("maintainAspectRatio: false,");
		details_.addLine("title: { display: true,text: 'Pass percentage' },");
		details_.addLine("scales: { xAxes: [{ stacked: 'true'}] },");
		details_.addLine("legend: { display: false, position: 'left' }");
		details_.dedent();
		details_.addLine("}");
		details_.dedent();
		details_.addLine("});");
		details_.dedent();
		details_.addLine("</script>");
	}
	
	private String css() {
		return ".highlight{background:magenta}.log,.titlecard,body{overflow-y:scroll}.logotext,.testEntry .title,.time,.titlecard .result{font-weight:700}body{font-size:150%;margin:0 auto;padding:40px 40px 65px 25px;background-color:#d1d4d8}.titlecard .logo{float:right;max-height:50px}.titlecard .testname{text-align:left;font-weight:700}td,th{text-align:center}.card{background-color:#fff;box-shadow:2px 2px 1px 0 #888;margin-bottom:1rem;padding:.75rem;border-radius:0}table,td{border:1px solid #000}.titlecard{height:315px}.testEntry .title{font-size:2em;margin-right:0;float:left;display:inline}.testEntry .time{padding-right:.5em;float:right}.log{max-height:400px}table{border-collapse:collapse;background:#CCC;width:100%}th{color:#fff;background:#2f4f4f}td.msg{text-align:left}.gallery{padding:10px;max-width:100%;max-height:150px;overflow-x:scroll;white-space:nowrap;background-color:#EEE}.galleryimage{max-width:160px;max-height:120px}.imagepreview{max-width:720px;max-height:auto}.modal-content{text-align:center;overflow:auto}.logoimg{max-height:45px;width:auto;padding:5px,0}.logotext{display:inline-block;vertical-align:middle;line-height:normal} .skipbg{background:#5A6EE6 !important}.passbg{background:#50C850 !important}.warnbg{background:#FFE664 !important}.errorbg{background:#FF5032 !important}.failbg{background:#FFAF32 !important}";	
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
	
	private String getHTMLStatus(String status)
	{
		String ret = "";

		if (status.equalsIgnoreCase("Pass")) { ret = "Pass"; }
		else if (status.equalsIgnoreCase("Skip")) { ret = "Skip"; }
		else if (status.equalsIgnoreCase("Error")) { ret = "Error"; }
		else if (status.equalsIgnoreCase("Fail")) { ret = "Fail"; }
		else if (status.equalsIgnoreCase("Warn") || status.equals("Warning")) { ret = "Warn"; }
		
		else ret="Error";
		
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