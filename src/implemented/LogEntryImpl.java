package implemented;

import java.util.LinkedHashMap;

public class LogEntryImpl implements interfaces.LogEntry {

	private LinkedHashMap<String, String> data_;
	
	public LogEntryImpl() {
		data_ = new LinkedHashMap<String, String>();
	}

	@Override
	public void addData(String col, String row) {
		data_.put(col, row);
	}

	@Override
	public LinkedHashMap<String, String> getData() {
		return data_;
	}

}
