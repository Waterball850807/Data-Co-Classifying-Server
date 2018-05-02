package server.workspace;

import server.client.ActivityRecord;
import server.client.EditionRecord;

public interface WorkSpaceRepository {

	public void addOrUpdateRecord(ActivityRecord rc);
	public EditionRecord getEditionRecord(int activityId);

}
