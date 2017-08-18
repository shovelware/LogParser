package implemented;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import interfaces.LogEntry;

public class TestCaseImpl implements interfaces.TestCase {
	private String name_;
	private String status_;
	
	private List<LogEntry> log_;
	private List<String> pics_;
	
	private LocalDateTime startTime_;
	private Duration runTime_;
	
	public TestCaseImpl() {
		log_ = new LinkedList<LogEntry>();
		pics_ = new LinkedList<String>();
		status_ = "";
		startTime_ = LocalDateTime.now();
		runTime_ = Duration.ofSeconds(0);
	}

	@Override
	public void setName(String name) {
			name_ = name != null ? name : name_;
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getStatus() {
		return status_;
	}

	@Override
	public LocalDateTime getStartTime() {
		return startTime_;
	}

	@Override
	public Duration getRunTime() {
		return runTime_;
	}

	@Override
	public void addPic(String filepath) {
		pics_.add(filepath);
	}
	
	@Override
	public boolean hasPics() {
		return (pics_.size() != 0);
	}
	
	@Override
	public String[] getPics() {
		String[] ret = new String[pics_.size()];
		return pics_.toArray(ret);
	}
	
	@Override
	public void addLog(LogEntry newLine) {
		log_.add(newLine);
	}

	@Override
	public boolean hasLog() {
		return (log_.size() != 0);
	}
	
	@Override
	public LogEntry[] getLog() {
		LogEntry[] ret = new LogEntry[log_.size()];
		return log_.toArray(ret);
	}

	@Override
	public void setStatus(String status) {
		status_ = status;
	}

	@Override
	public void setStartTime(LocalDateTime time) {
		startTime_ = time;
	}

	@Override
	public void setRunTime(long seconds) {
		runTime_ = Duration.ofSeconds(seconds);
	}

}
