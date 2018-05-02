package model;

import java.util.List;

public interface ActivityRepository {
	public Activity getActivity(int id);
	public Activity createActivity(Activity activity);
	public List<Activity> getActivities();
	public void attachTagsToActivity(int activityId, int[] tagIds);
	public ActivityTag[] getActivityTags();
	public ActivityTag getActivityTag(int tagId);
}
