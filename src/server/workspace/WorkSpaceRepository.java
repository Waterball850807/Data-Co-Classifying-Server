package server.workspace;

import model.Activity;
import server.client.ActivityRecord;
import server.client.EditionRecord;
import server.client.EditionRecord.Item;
import server.client.User;

public interface WorkSpaceRepository {

	public int getUserContributionPoint(User user);
	public void addOrUpdateRecord(ActivityRecord rc);
	public EditionRecord getEditionRecord(int activityId);
	void addEditRecordItem(Activity activity, Item item);

}
