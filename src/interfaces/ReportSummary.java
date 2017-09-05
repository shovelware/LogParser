package interfaces;

import java.time.Duration;
import java.time.LocalDateTime;

public interface ReportSummary {
	public void setTitle(String title);
	public String getTitle();
	
	public void setGenerationTime(LocalDateTime time);
	public LocalDateTime getGenerationTime();
	
	public void setStartTime(LocalDateTime time);
	public LocalDateTime getStartTime();
	
	public void setRunTime(long time);
	public Duration getRunTime();
	
	public void addTest(String title, String status, Duration seconds);
	public String[] getTests();
	
	public int getSkipTests();
	public int getPassTests();
	public int getWarnTests();
	public int getErrorTests();
	public int getFailTests();
	
	public int getTotalTests();
	public int getTotalActiveTests();
}
