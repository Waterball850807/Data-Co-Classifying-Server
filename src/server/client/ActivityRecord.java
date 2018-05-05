package server.client;

import model.Activity;

public class ActivityRecord {
	private Activity activity;
	private EditionRecord editionRecords;
	
	public ActivityRecord(Activity activity, EditionRecord editionRecords) {
		this.activity = activity;
		this.editionRecords = editionRecords;
	}
	
	public Activity getActivity() {
		return activity;
	}
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	public EditionRecord getEditionRecord() {
		return editionRecords;
	}
	public void setEditionRecords(EditionRecord editionRecords) {
		this.editionRecords = editionRecords;
	}
}
