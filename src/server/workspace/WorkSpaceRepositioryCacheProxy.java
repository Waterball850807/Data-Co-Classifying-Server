package server.workspace;

import java.util.HashMap;
import java.util.Map;

import model.Activity;
import server.client.ActivityRecord;
import server.client.EditionRecord;
import server.client.EditionRecord.Item;
import server.client.User;

public class WorkSpaceRepositioryCacheProxy implements WorkSpaceRepository{
	public WorkSpaceRepository workSpaceRepository;
	private Map<String, Integer> pointCache = new HashMap<>();  //<user's id, id>
	
	public WorkSpaceRepositioryCacheProxy(WorkSpaceRepository workSpaceRepository) {
		this.workSpaceRepository = workSpaceRepository;
	}

	@Override
	public int getUserContributionPoint(User user) {
		if (!pointCache.containsKey(user.getId()))
			pointCache.put(user.getId(), workSpaceRepository.getUserContributionPoint(user));
		return pointCache.get(user.getId());
	}

	@Override
	public void addOrUpdateRecord(ActivityRecord rc) {
		
	}

	@Override
	public EditionRecord getEditionRecord(int activityId) {
		return null;
	}

	@Override
	public void addEditRecordItem(Activity activity, Item item) {
		// TODO Auto-generated method stub
		
	}

}
