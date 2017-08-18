package interfaces;

import interfaces.TestCase;
import interfaces.ReportSummary;

public interface HTMLWriter {
	void setSummary(ReportSummary data);
	void setData(TestCase[] tests);
	boolean writeReport(String path);
}
