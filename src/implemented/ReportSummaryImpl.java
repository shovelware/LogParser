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
	protected int unkownTests_;
	
	public ReportSummaryImpl() {
		title_ = "$REPORT_TITLE$";
		generationTime_ = LocalDateTime.now();
		startTime_ = LocalDateTime.of(1990, 1, 1, 12, 00, 00);
		runTime_ = Duration.ofSeconds(0L);
		testList_ = new LinkedList<String>();
		
		skipTests_ = 0;
		passTests_ = 0;
		warnTests_ = 0;
		errorTests_ = 0;
		failTests_ = 0;
	}

	@Override
	public void setTitle(String title) {
		title_ = title;
	}

	@Override
	public String getTitle() {
		return title_  == null ? "$MISSING_TITLE$" : title_;
	}

	@Override
	public void setGenerationTime(LocalDateTime time) {
		generationTime_ = time;
	}

	@Override
	public LocalDateTime getGenerationTime() {
		return generationTime_ == null ? LocalDateTime.of(1990, 1, 1, 12, 00, 00) : generationTime_;
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
		return runTime_== null ? Duration.ofSeconds(999999) : runTime_;
	}

	@Override
	public void addTest(String title, String status, Duration seconds) {
		if (status.equalsIgnoreCase("pass")) { passTests_++; }
		else if (status.equalsIgnoreCase("skip")) { skipTests_++; }
		else if (status.equalsIgnoreCase("warn")) { warnTests_++; }
		else if (status.equalsIgnoreCase("error")) { errorTests_++; }
		else if (status.equalsIgnoreCase("fail")) { failTests_++; }
		if (status.equalsIgnoreCase("unknown")) { unkownTests_++; }
		
		//Fallback of fallbacks, testList is never read
		else testList_.add(title + "%=%" + status.trim());
		
		runTime_ = runTime_.plus(seconds);
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

	@Override
	public int getUnkownTests() {
		return unkownTests_;
	}

}
