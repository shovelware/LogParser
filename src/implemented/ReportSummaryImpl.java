package implemented;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;

import interfaces.ReportSummary;

public class ReportSummaryImpl implements ReportSummary {
	protected String title_;
	protected LocalDateTime generationTime_;
	protected LocalDateTime startTime_;
	protected Duration runTime_;
	
	protected LinkedList<String> testList_;
	
	protected int skipTests_;
	protected int passTests_;
	protected int warnTests_;
	protected int errorTests_;
	protected int failTests_;
	
	public ReportSummaryImpl() {
		testList_ = new LinkedList<String>();
		generationTime_ = LocalDateTime.now();
		runTime_ = Duration.ofSeconds(0L);
	}

	@Override
	public void setTitle(String title) {
		title_ = title;
	}

	@Override
	public String getTitle() {
		return title_;
	}

	@Override
	public void setGenerationTime(LocalDateTime time) {
		generationTime_ = time;
	}

	@Override
	public LocalDateTime getGenerationTime() {
		return generationTime_;
	}

	@Override
	public void setStartTime(LocalDateTime time) {
		startTime_ = time;
	}

	@Override
	public LocalDateTime getStartTime() {
		return startTime_;
	}

	@Override
	public void setRunTime(long time) {
		runTime_ = Duration.ofSeconds(time);
	}

	@Override
	public Duration getRunTime() {
		return runTime_;
	}

	@Override
	public void addTest(String title, String status) {
		if (status.equalsIgnoreCase("pass")) { passTests_++; }
		else if (status.equalsIgnoreCase("skip")) { skipTests_++; }
		else if (status.equalsIgnoreCase("warn")) { warnTests_++; }
		else if (status.equalsIgnoreCase("error")) { errorTests_++; }
		else if (status.equalsIgnoreCase("fail")) { failTests_++; }
		
		testList_.add(title + "%=%" + status.trim());
	}

	@Override
	public String[] getTests() {
		String[] ret = new String[testList_.size()];
		return testList_.toArray(ret);
	}

	@Override
	public int getSkipTests() {
		return skipTests_;
	}
	
	@Override
	public int getPassTests() {
		return passTests_;
	}

	@Override
	public int getWarnTests() {
		return warnTests_;
	}

	@Override
	public int getErrorTests() {
		return errorTests_;
	}
	
	@Override
	public int getFailTests() {
		return failTests_;
	}

	@Override
	public int getTotalTests() {
		return skipTests_ + passTests_ + warnTests_ + errorTests_;
	}

	@Override
	public int getTotalActiveTests() {
		return passTests_ + warnTests_ + errorTests_ + failTests_;
	}

}