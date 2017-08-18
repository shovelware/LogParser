package interfaces;

import java.util.LinkedHashMap;

public interface LogEntry {
	public void addData(String col, String row);
	public LinkedHashMap<String, String> getData();
}
