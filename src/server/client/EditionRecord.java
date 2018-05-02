package server.client;

import java.util.Map;

public class EditionRecord {

	private Map<String, String[]> records;

	
	public EditionRecord(Map<String, String[]> records) {
		this.records = records;
	}


	public Map<String, String[]> getRecords() {
		return records;
	}
	
}
