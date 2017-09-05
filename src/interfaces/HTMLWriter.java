package interfaces;

import interfaces.TestCase;

public interface HTMLWriter {
	void beginReport(String path, ReportSummary summary);
	
	void writeTest(TestCase testcase);
	
	void endReport();
}