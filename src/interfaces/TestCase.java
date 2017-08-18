package interfaces;

import java.time.Duration;
import java.time.LocalDateTime;

public interface TestCase {
	public void setName(String name);	
	public String getName();

	public void setStatus(String status);
	public String getStatus();
	
	public void setStartTime(LocalDateTime time);
	public LocalDateTime getStartTime();
	
	public void setRunTime(long seconds);
	public Duration getRunTime();
	
	public void addPic(String filepath);
	public boolean hasPics();
	public String[] getPics();
	
	public void addLog(LogEntry newLine);
	public boolean hasLog();
	public LogEntry[] getLog();
}
