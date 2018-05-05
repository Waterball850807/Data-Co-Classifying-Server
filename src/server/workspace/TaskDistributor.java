package server.workspace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import model.Activity;
import model.ActivityRepository;
import model.JdbcActivityRepository;
import model.JdbcProxy;
import server.client.User;

public class TaskDistributor {
	private ActivityRepository activityRepository;
	private WorkSpaceRepository workSpaceRepository;
	private Map<Integer, String> activityToUserMap = new HashMap<>();  //<activity's id, user id>
	private Map<String, Integer> userToActivityMap = new HashMap<>();  //<user id, activity's id>
	
	public TaskDistributor(ActivityRepository activityRepository, WorkSpaceRepository workSpaceRepository) {
		this.activityRepository = activityRepository;
		this.workSpaceRepository = workSpaceRepository;
	}

	public int getWorkingActivityId(String userId){
		if (!userToActivityMap.containsKey(userId))
			return -1;
		return userToActivityMap.get(userId);
	}
	
	public Activity getWorkingActivity(String userId){
		int workingAId = getWorkingActivityId(userId);
		return activityRepository.getActivity(workingAId);
	}
	
	public boolean isActivityHolded(int activityId){
		return activityToUserMap.get(activityId) != null;
	}
	
	public Activity moveToNext(String userId, boolean clean){
		List<Activity> activities = activityRepository.getActivities();
		int fromId = getWorkingActivityId(userId);
		
		int fromIndex = IntStream.range(0, activities.size())
									.filter(i -> activities.get(i).getId() == fromId)
									.findFirst()
									.orElse(-1);
		int toIndex = 0;
		
		synchronized(this){
			
			for (int i = 1 ; i < activities.size(); i ++)
			{
				toIndex = (fromIndex + i) % activities.size();
				int activityId = activities.get(toIndex).getId();
				if (!isActivityHolded(activityId) && (!clean || isActivityClean(activityId)))
					break;
			}
			
			int toId = activities.get(toIndex).getId();
			moveUser(userId, fromId, toId);
		}
		
		return activities.get(toIndex);
	}
	
	public Activity moveToActivity(String userId, int activityId){
		Activity activity = activityRepository.getActivity(activityId);
		int fromId = userToActivityMap.get(userId);
		moveUser(userId, fromId, activityId);
		return activity;
	}
	
	public boolean isActivityClean(int activityId){
		return workSpaceRepository.getEditionRecord(activityId) == null;
	}
	
	public synchronized void moveUser(String userId, int fromId, int toId){
		userToActivityMap.put(userId, toId);
		activityToUserMap.put(fromId, null);
		activityToUserMap.put(toId, userId);
	}
	
	public synchronized void removeUser(String userId){
		if (userToActivityMap.containsKey(userId))
		{
			int activityId = getWorkingActivityId(userId);
			userToActivityMap.remove(userId);
			activityToUserMap.put(activityId, null);
		}
	}
	
	public static void main(String[] argv){
		User u1 = new User("1", "u1");
		User u2 = new User("2", "u2");
		ActivityRepository activityRepository = new JdbcProxy(new JdbcActivityRepository());
		WorkSpaceRepository workSpaceRepository = new JsonBasedWorkSpaceRepository();
		TaskDistributor workStage = new TaskDistributor(activityRepository, workSpaceRepository);
		System.out.println(workStage.moveToNext(u1.getId(), false).getId());
		System.out.println(workStage.moveToNext(u1.getId(), false).getId());
		System.out.println(workStage.moveToNext(u1.getId(), false).getId());
		System.out.println(workStage.moveToNext(u1.getId(), false).getId());
		System.out.println(workStage.moveToNext(u1.getId(), false).getId());
		System.out.println(workStage.moveToNext(u1.getId(), false).getId());
		
		System.out.println(workStage.moveToNext(u2.getId(), false).getId());
		System.out.println(workStage.moveToNext(u2.getId(), false).getId());
		
		System.out.println(workStage.moveToNext("3", false).getId());
		System.out.println(workStage.moveToNext("3", false).getId());
		workStage.removeUser("3");
		System.out.println(workStage.moveToNext(u2.getId(), false).getId());
	}
}
