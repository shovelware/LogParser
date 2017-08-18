package interfaces;

public interface FileReader {
	void readDir(String path);
	TestCase[] getEntries();
	ReportSummary getReport();
}
