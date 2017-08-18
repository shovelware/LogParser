package implemented;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import interfaces.TestCase;
import interfaces.ReportSummary;

public class HTMLWriterImpl implements interfaces.HTMLWriter {

	TestCase[] entries_;
	ReportSummary summary_;

	DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("YYYY-MM-dd | HH:mm:ss");
	
	public HTMLWriterImpl() {
	}

	@Override
	public void setSummary(ReportSummary data) {
		summary_ = data;
		
		if (summary_.getTitle() == null) { summary_.setTitle("Report"); }
	}
	
	@Override
	public void setData(TestCase[] testEntries) {
		entries_ = testEntries.clone();
	}
 
	@Override
	public boolean writeReport(String path) {
		boolean success = false;

		outputHTML(path);		
		
		return success;
	}
	
	private boolean outputHTML(String path) {
		boolean success = false;
		BufferedWriter bw = null;
		
		HTMLTurtle docTop = documentTop();
		
		HTMLTurtle testTop = new HTMLTurtle(docTop);
		testTop.addLine(openContainer());
		testTop.addLine(openRow());
		
		LinkedList<String> tests = processTests(docTop);
		
		HTMLTurtle testBottom = new HTMLTurtle(docTop);
		testBottom.addLine(closeContainer());
		testBottom.addLine(closeRow());
		
		HTMLTurtle docBottom = documentBottom(docTop);
		
		try {
			String filename = summary_.getTitle();
			filename +=  "_" + summary_.getGenerationTime().format(DateTimeFormatter.ofPattern("ddMMYY_HHmmss"));
			System.out.println(filename);
					
			bw = new BufferedWriter(new FileWriter(path + filename + ".html"));
			
			bw.write(docTop.getHTML());
			
			bw.write(testTop.getHTML());
			for (String t : tests)
			{
				bw.write(t);
			}
			bw.write(testBottom.getHTML());
			
			bw.write(docBottom.getHTML());
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	private HTMLTurtle documentTop(){
		HTMLTurtle docTop = new HTMLTurtle();
		
		docTop.addLine("<!DOCTYPE HTML>");
		docTop.addLine("<html lang=\"en\">");
		
		docTop.indent();
		docTop.addText(header(docTop));
		docTop.dedent();
		
		docTop.addLine("<body>");
		docTop.indent();
		
		docTop.appendChild(modalDisplay(docTop));
		
		
		docTop.addLine(openContainer());
		docTop.indent();
		
		docTop.addLine(openRow());
		docTop.indent();
		docTop.appendChild(testOverview(docTop));
		docTop.newLine();
		
		docTop.appendChild(donutChart(docTop));
		docTop.dedent();
		docTop.addLine(closeRow());
		docTop.newLine();
		
		docTop.addLine(openRow());
		docTop.indent();
		docTop.appendChild(percentChart(docTop));
		docTop.dedent();
		docTop.addLine(closeRow());
		
		docTop.dedent();
		docTop.addLine(closeContainer());
		docTop.newLine();
		
		docTop.addLine("<hr/>");
		docTop.newLine();
		
		return docTop;
	}

	
	private LinkedList<String> processTests(HTMLTurtle parent) {
		LinkedList<String> testHTML = new LinkedList<String>();

		HTMLTurtle test = new HTMLTurtle(parent);
		int testCount = 1;
		
		for (TestCase t : entries_)
		{
			boolean hasData = t.hasLog() || t.hasPics();
			test.clear();
			test.setParent(parent);
			
			//Fix up some time calculations
			DateTimeFormatter timestampFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
			
			String startTime = t.getStartTime().format(timestampFormat);
			String runTime = formatRuntime(t.getRunTime());
			String collapseElement = "";

			if (hasData)
			{
				collapseElement = " data-toggle=\"collapse\" href=\"#testlog" + testCount + "\" aria-expanded=\"false\" aria-controls=\"testlog" +testCount+ "\"";
			}
			
			test.addLine("<div class=\"highlight\"><div class=\"card testEntry\" id=\"testcard" + testCount + "\">");
			test.indent();
			test.addLine("<div class=\"cardheader " +  getHTMLStatus(t.getStatus()) + "bg\" "+ collapseElement+">");
			test.indent();
			if (hasData) {
				//test.addLine("<span class=\"glyphicon glyphicon-plus expandarrow\" aria-hidden=\"true\" style=\"margin-left: 0.25em; float: left; clear: left;\"></span>");
			}
			test.addLine("<span class=\"title\">" + t.getName() + "</span>");
			//test.addLine("<span class=\"status " + getHTMLStatus(t.getStatus()) + "\">" + t.getStatus() + "</span>");
			test.addLine("<span class=\"time start\"><span class=\"glyphicon glyphicon-time\" aria-hidden=\"true\"></span> " + startTime + "</span><br/>");
			test.addLine("<span class=\"time run\"><span class=\"glyphicon glyphicon-repeat\" aria-hidden=\"true\"></span> " + runTime + "</span><br/>");
			test.dedent();	
			test.addLine("</div>");
			test.newLine();
			
			if (hasData)
			{
				test.addLine("<div class=\"collapse\" id=\"testlog" + testCount +"\">");
				test.indent();
				//test.addLine("<button class=\"btn btn-block\" data-toggle=\"collapse\" href=\"#testlog" + testCount +"\" aria-expanded=\"false\" aria-controls=\"testlog" + testCount + "\">Toggle Details</button>");
				test.newLine();
				
				if (t.hasLog())
				{
					test.addLine("<div class=\"log\"><table>");
					test.indent();
					
					interfaces.LogEntry[] log = t.getLog().clone();
					Set<String> keys = log[0].getData().keySet();
					
					String headers = "<tr>";
					for (String s : keys)
					{
						headers += "<th>" + s + "</th>";
					}
					headers += "</tr>";
					
					test.addLine(headers);
					
					for (interfaces.LogEntry l : log)
					{
						if (l.getData().isEmpty() == false) 
						{
							test.addLine(processLogLine(l));
						}
					}
					
					test.dedent();
					test.addLine("</table></div>");
					test.newLine();
				}
				
				if (t.hasPics()) {
					//If pics
					test.addLine("<div class=\"gallery\">");
					test.indent();
					
					for (String filename : t.getPics()) {
						test.addLine("<img class=\"galleryimage\" src=\"imgs/" + filename + "\"/>");
					}
					
					test.dedent();
					test.addLine("</div>");
					test.newLine();
				}
				
				test.dedent();
				test.addLine("</div></div>");
				//test.addLine("<button class=\"btn btn-block\" data-toggle=\"collapse\" href=\"#testlog" + testCount + "\" aria-expanded=\"false\" aria-controls=\"testlog" +testCount+ "\">Toggle Details</button>");
				
				test.dedent();
			}
			
			test.addLine("</div>");
			test.newLine();
			testCount++;
			
			testHTML.add(test.getHTML());
		}
		
		return testHTML;
	}
	
	private HTMLTurtle documentBottom(HTMLTurtle parent) {
		HTMLTurtle docBottom = new HTMLTurtle(parent);
		
		docBottom.appendChild(footer(docBottom));
		
		docBottom.addLine("<!--SCRIPTS-->");
		docBottom.appendChild(blinkScript(docBottom));
		docBottom.appendChild(modalScript(docBottom));
		docBottom.appendChild(donutScript(docBottom));
		docBottom.appendChild(percentScript(docBottom));
		
		docBottom.dedent();
		docBottom.addLine("</body>");
		
		docBottom.addLine("</html>");
		
		return docBottom;
	}
	
	private String header(HTMLTurtle parent) {		
		HTMLTurtle ht = new HTMLTurtle(parent);
		
		ht.addLine("<head>");
		ht.indent();
		ht.addLine("<meta charset=\"UTF8\">");
		ht.addLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
		ht.addLine("<link rel=\"stylesheet\" href=\"scripts/bootstrap/css/bootstrap.css\">");
		ht.addLine("<script src=\"scripts/jquery-3.2.1.js\"></script>");
		ht.addLine("<script src=\"scripts/bootstrap/js/bootstrap.js\"></script>");
		ht.addLine("<script src=\"scripts/chart.js\"></script>");
		ht.addLine(String.format("<title>%s</title>", summary_.getTitle()));
		ht.addLine("<style>" + css() + "</style>");
		ht.dedent();
		ht.addLine("</head>");
		
		
		return ht.getHTML();
	}
	
	private String css() {
		return ".highlight{background:magenta}.log,.titlecard,body{overflow-y:scroll}.logotext,.testEntry .title,.time,.titlecard .result{font-weight:700}body{font-size:150%;margin:0 auto;padding:40px 40px 65px 25px;background-color:#d1d4d8}.titlecard .logo{float:right;max-height:50px}.titlecard .testname{text-align:left;font-weight:700}td,th{text-align:center}.card{background-color:#fff;box-shadow:2px 2px 1px 0 #888;margin-bottom:1rem;padding:.75rem;border-radius:0}table,td{border:1px solid #000}.titlecard{height:315px}.testEntry .title{font-size:2em;margin-right:0;float:left;display:inline}.testEntry .time{padding-right:.5em;float:right}.log{max-height:400px}table{border-collapse:collapse;background:#CCC;width:100%}th{color:#fff;background:#2f4f4f}td.msg{text-align:left}.gallery{padding:10px;max-width:100%;max-height:150px;overflow-x:scroll;white-space:nowrap;background-color:#EEE}.galleryimage{max-width:160px;max-height:120px}.imagepreview{max-width:720px;max-height:auto}.modal-content{text-align:center;overflow:auto}.logoimg{max-height:45px;width:auto;padding:5px,0}.logotext{display:inline-block;vertical-align:middle;line-height:normal} .skipbg{background:#5A6EE6 !important}.passbg{background:#50C850 !important}.warnbg{background:#FFE664 !important}.errorbg{background:#FF5032 !important}.failbg{background:#FFAF32 !important}";	
		}
	
	private HTMLTurtle modalDisplay(HTMLTurtle parent) {
		HTMLTurtle modalPop = new HTMLTurtle(parent);
		
		modalPop.addLine("<!--MODAL-->");
		modalPop.addLine("<div class=\"modal fade\" id=\"imagemodal\" tabindex=\"-1\" role=\"dialog\" aria-hidden=\"true\">");
		modalPop.indent();
		modalPop.addLine("<div class=\"modal-dialog modal-lg\"><div class=\"modal-content\"><div class=\"modal-body\">");
		modalPop.indent();
		modalPop.addLine("<a href=\"\" class=\"imagelink\" target=\"_blank\">");
		modalPop.indent();
		modalPop.addLine("<img src=\"\" class=\"imagepreview img-fluid\">");
		modalPop.dedent();
		modalPop.addLine("</a>");
		modalPop.dedent();
		modalPop.addLine("</div></div></div>");
		modalPop.dedent();
		modalPop.addLine("</div>");
		modalPop.newLine();
		
		return modalPop;
	}
	private HTMLTurtle testOverview(HTMLTurtle parent) {
		HTMLTurtle overview = new HTMLTurtle(parent);

		
		String generationDate = summary_.getGenerationTime().format(dateFormat);
		String startDate = summary_.getStartTime().format(dateFormat);
		String runTime = formatRuntime(summary_.getRunTime());
		
		overview.addLine("<div class=\"col-md-6\"><div class=\"titlecard card\"><div class=\"titlebox\">");
		overview.indent();
		overview.addLine("<span class=\"logo\"><img src=\"imgs/logoklant.png\"></span>");
		overview.addLine(String.format("<span class=\"title\"><h1>%s</h1></span><br/>", summary_.getTitle()));
		overview.newLine();
		overview.addLine("<span class=\"glyphicon glyphicon-file\" aria-hidden=\"true\"></span> Report generated:");
		overview.addLine(String.format("<span class=\"time\">%s</span><br/>", generationDate));
		
		overview.addLine("<span class=\"glyphicon glyphicon-time\" aria-hidden=\"true\"></span> Testing started:");
		overview.addLine(String.format("<span class=\"time startdate\">%s</span><br/>", startDate));
		
		overview.addLine("<span class=\"glyphicon glyphicon-repeat\" aria-hidden=\"true\"></span> Total runtime:");
		overview.addLine(String.format("<span class=\"time runtime\">%s</span><br/><br/>", runTime));
		
		overview.newLine();
		overview.addLine("<div><table>");
		overview.addLine("<tr><th>Testcase</th><th>Status</th></tr>");
		
		int testCount = 1;
		for (TestCase t : entries_)
		{			
			overview.addLine("<tr><td class=\"testname\"><a class=\"testlink\" href=\"#testcard" + testCount +"\">" + t.getName() + "</a></td><td class=\"result " + getHTMLStatus(t.getStatus()) + "bg\">" + getStatusText(t.getStatus()) + "</td></tr>");
			testCount++;
		}
		
		overview.addLine("</table></div>");
		overview.dedent();
		overview.addLine("</div></div></div>");
		
		return overview;
	}
	
	private String getHTMLStatus(String status)
	{
		String ret = "";

		if (status.equals("Pass")) { ret = "pass"; }
		else if (status.equals("Skip")) { ret = "skip"; }
		else if (status.equals("Error")) { ret = "error"; }
		else if (status.equals("Fail")) { ret = "fail"; }
		else if (status.equals("Warn") || status.equals("Warning")) { ret = "warn"; }
		
		
		return ret;
	}
	
	private String getStatusText(String status)
	{
		String ret = status;
		
		return ret;
	}
	
	private String formatRuntime(Duration duration)
	{
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
	
	private HTMLTurtle donutChart(HTMLTurtle parent) {
		HTMLTurtle donut = new HTMLTurtle(parent);
		
		donut.addLine("<div class=\"col-md-6\"><div class=\"card\"><div class=\"graph\">");
		donut.indent();
		donut.addLine("<canvas id=\"donut\" height=\"300\"></canvas>");
		donut.dedent();
		donut.addLine("</div></div></div>");
		
		return donut;
		
	}
	
	private HTMLTurtle percentChart(HTMLTurtle parent) {
		HTMLTurtle percent = new HTMLTurtle(parent);
		
		percent.addLine("<div class=\"col-md-12\"><div class=\"card\"><div class=\"graph\">");
		percent.indent();
		percent.addLine("<canvas id=\"bar\" height=\"100\"></canvas>");
		percent.dedent();
		percent.addLine("</div></div></div>");
		
		return percent;		
	}
	
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

	private HTMLTurtle footer(HTMLTurtle parent) {
		HTMLTurtle footer = new HTMLTurtle(parent);
		footer.addLine("<div class=\"navbar navbar-default navbar-bottom\">");
		footer.indent();
		
		footer.addLine("<div class=\"pull-left\">");
		footer.indent();
		//Left Footer content here
		footer.dedent();
		footer.addLine("</div>");
		
		footer.addLine("<div class=\"pull-right\">");
		footer.indent();
		footer.addLine("<span class=\"logotext\"></span>");
		footer.addLine("<a href=\"http://www.cerios.nl\" target=\"_blank\"><img class=\"logoimg\" src=\"imgs/were-cerios.png\"></a>");
		footer.dedent();
		footer.addLine("</div>");
		
		footer.dedent();
		footer.addLine("</div>");
		
		return footer;
	}
	
	private String openContainer() {		
		return "<div class=\"container-fluid\">";
	}
	
	private String closeContainer() {
		return "</div><!--END CONTAINER-->";
	}
	
	private String openRow() {
		return "<div class=\"row-fluid\">";
	}
	
	private String closeRow() {
		return "</div><!--END ROW-->";
	}
	
	private HTMLTurtle modalScript(HTMLTurtle parent) {
		HTMLTurtle modal = new HTMLTurtle(parent);
		
		modal.addLine("<script \"modal\">");
		modal.addLine("$(function() {");
		modal.indent();
		modal.addLine("$('.galleryimage').on('click', function() {");
		modal.indent();
		modal.addLine("$('.imagepreview').attr('src', $(this).attr('src'));");
		modal.addLine("$('.imagelink').attr('href', $(this).attr('src'));");
		modal.addLine("$('#imagemodal').modal('show');");
		modal.dedent();
		modal.addLine("});");
		modal.dedent();
		modal.addLine("});");
		modal.addLine("</script>");
		
		return modal;
	}
	
	private HTMLTurtle blinkScript(HTMLTurtle parent) {
		HTMLTurtle blink = new HTMLTurtle(parent);
		
		blink.addLine("<script \"blinkDiv\">");
		blink.addLine("$(function() {");
		blink.indent();
		blink.addLine("$('.testlink').on('click', function() {");
		blink.indent();
		blink.addLine("$($(this).attr('href')).fadeTo(100, 0.3, function() { $(this).fadeTo(500, 1.0, function() { $(this).fadeTo(100, 0.3, function() { $(this).fadeTo(500, 1.0); }); }); });");
		blink.dedent();
		blink.addLine("});");
		blink.dedent();
		blink.addLine("});");
		blink.addLine("	</script>");
		
		return blink;
	}
		
	private HTMLTurtle donutScript(HTMLTurtle parent){
		HTMLTurtle donut = new HTMLTurtle(parent);
		
		donut.addLine("<script \"donut\">");
		donut.indent();
		
		donut.addLine("var ctx = document.getElementById(\"donut\");");
		donut.addLine("var myChart = new Chart(ctx, {");
		donut.indent();
		donut.addLine("type: 'doughnut',");
		
		donut.addLine("data: {");
		donut.indent();
		donut.addLine("labels: [\"Skip\", \"Pass\", \"Warn\", \"Fail\", \"Error\"],");
		donut.addLine("datasets: [{");
		donut.addLine(String.format("data: [%d, %d, %d, %d, %d],", summary_.getSkipTests(),  summary_.getPassTests(), summary_.getWarnTests(), summary_.getFailTests(), summary_.getErrorTests()));
		donut.addLine("backgroundColor: [");
		donut.indent();
		donut.addLine("'rgba(90, 110, 230, 1)',");
		donut.addLine("'rgba(80, 200, 80, 1)',");
		donut.addLine("'rgba(255, 230, 100, 1)',");
		donut.addLine("'rgba(255, 175, 50, 1)',");
		donut.addLine("'rgba(255, 80, 50, 1)'");
		donut.dedent();
		donut.addLine("],");
		donut.addLine("borderWidth: 2");
		donut.addLine("}]");
		donut.dedent();
		donut.addLine("},");
		
		donut.addLine("options: {");
		donut.indent();	
		donut.addLine("responsive: true,");
		donut.addLine("maintainAspectRatio: false,");
		donut.addLine("cutoutPercentage: 70,");
		donut.addLine("rotation: (0.5 * Math.PI),");
		donut.addLine("legend: { position: 'right' },");
		donut.addLine("title: { display: false, text: 'Test Results' },");
		donut.addLine("tooltips: {	position: 'nearest' }");
		donut.dedent();
		
		donut.addLine("}");
		donut.dedent();
		donut.addLine("});");
		donut.dedent();
		donut.addLine("</script>");
		
		return donut;
	};
	
	private HTMLTurtle percentScript(HTMLTurtle parent){
		HTMLTurtle percent = new HTMLTurtle(parent);

		DecimalFormat decimal = new DecimalFormat("0.00");
		float totalActiveTests = summary_.getTotalActiveTests();
		float passTests = summary_.getPassTests();
		
		float passPercent = 100.f *(passTests / totalActiveTests);
		float failPercent = 100.f - passPercent;
		
		percent.addLine("<script \"bar\">");
		percent.indent();
		percent.addLine("var ctx = document.getElementById(\"bar\");");
		percent.addLine("var myBarChart = new Chart(ctx, {");
		percent.indent();
		percent.addLine("type: 'horizontalBar',");
		percent.addLine("data: {");
		percent.indent();
		percent.addLine("labels: [\"\"],");
		percent.addLine("datasets: [{");
		percent.addLine("stack: \"tests\",");
		percent.addLine("//label: \"Passed\",");
		percent.addLine(String.format("data: [%s],", decimal.format(passPercent)));
		percent.addLine("backgroundColor: ['rgba(80, 200, 80, 1)']");
		percent.addLine("},");
		percent.addLine("{");
		percent.addLine("stack: \"tests\",");
		percent.addLine("//label: \"Failed\",");
		percent.addLine(String.format("data: [%s],",decimal.format(failPercent)));
		percent.addLine("backgroundColor:['rgba(200, 200, 200, 1)']");
		percent.addLine("}]");
		percent.dedent();
		percent.addLine("},");
		percent.addLine("options: {");
		percent.indent();
		percent.addLine("responsive: true,");
		percent.addLine("maintainAspectRatio: false,");
		percent.addLine("title: { display: true,text: 'Pass percentage' },");
		percent.addLine("scales: { xAxes: [{ stacked: 'true'}] },");
		percent.addLine("legend: { display: false, position: 'left' }");
		percent.dedent();
		percent.addLine("}");
		percent.dedent();
		percent.addLine("});");
		percent.addLine("</script>");
		
		return percent;
	}

}

class HTMLTurtle {
	protected String html;
	protected int dent;
	protected static String newline = System.getProperty("line.separator");
	
	void newLine(){ html += newline; }	
	void indent() { dent = (dent + 1); }
	void dedent() { dent = (dent - 1 >= 0? dent - 1 : 0); }
	
	HTMLTurtle(){ html = new String(); dent = 0; }
	HTMLTurtle(HTMLTurtle parent) { html = new String(); setParent(parent); }
	
	void setParent(HTMLTurtle parent) { this.dent = (parent != null ? parent.getIndent() : 0); }
	
	void addText(String text) {
		for (int i = 0; i < dent; ++i)
		{
			html+="\t";
		}
		
		html += text;
	}
	
	void addLine(String text) {
		addText(text);
		html += newline;
	}
	
	void appendChild(HTMLTurtle child) {
		html += child.getHTML();
	}
	
	int getIndent() { return dent; }
	
	String getHTML() { return html; }
	
	void clear() { html = new String(); dent = 0; }
}
