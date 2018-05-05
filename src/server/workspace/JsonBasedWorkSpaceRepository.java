package server.workspace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import model.Activity;
import model.ActivityTag;
import server.client.ActivityRecord;
import server.client.EditionRecord;
import server.client.User;
import server.client.EditionRecord.Item;
import utils.GsonUtils;

public class JsonBasedWorkSpaceRepository implements WorkSpaceRepository{
	private File recordsFile = new File("records.json");
	private Gson gson = GsonUtils.getGson();
	
	public JsonBasedWorkSpaceRepository() {
		if(!recordsFile.exists())
			try {
				recordsFile.createNewFile();
				FileUtils.writeStringToFile(recordsFile, "[]", "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	@Override
	public synchronized void addEditRecordItem(Activity activity, EditionRecord.Item item){
		EditionRecord editionRecord = getEditionRecord(activity.getId());
		if (editionRecord == null || editionRecord.getItems() == null)
		{
			List<EditionRecord.Item> items = new ArrayList<>();
			items.add(item);
			editionRecord = new EditionRecord(items);
		}
		else
			editionRecord.addOrUpdateItem(item);
		addOrUpdateRecord(new ActivityRecord(activity, editionRecord));
	}
	
	@Override
	public synchronized void addOrUpdateRecord(ActivityRecord rc) {
		List<ActivityRecord> records = parseActivityRecordsFromFile();
		Optional<ActivityRecord> updated = records.stream()
						.filter(r -> r.getActivity() != null)
						.filter(r -> r.getActivity().getId() == rc.getActivity().getId())
						.findFirst();
		if (updated.isPresent()) 
			records.remove(updated.get());		
		records.add(rc);
		saveActivityRecordsToFile(records);
	}

	@Override
	public EditionRecord getEditionRecord(int activityId) {
		List<ActivityRecord> records = parseActivityRecordsFromFile();
		return records.stream()
				.filter(r -> r.getActivity().getId() == activityId)
				.findFirst()
				.map(o -> o.getEditionRecord())
				.orElse(null);
	}

	@Override
	public int getUserContributionPoint(User user) {
		List<ActivityRecord> records = parseActivityRecordsFromFile();
		return (int) records.stream()
							.map(a -> a.getEditionRecord())
							.filter(r -> r.containsUser(user))
							.count();
	}

	private synchronized List<ActivityRecord> parseActivityRecordsFromFile(){
		try {
			String json = FileUtils.readFileToString(recordsFile, "utf-8");
			json = json.isEmpty() ? "[]" : json;  //make it a json array as default
			return GsonUtils.parseActivityRecords(json);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private synchronized void saveActivityRecordsToFile(List<ActivityRecord> records){
		try {
			String json = gson.toJson(records);
			FileUtils.writeStringToFile(recordsFile, json, "utf-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] argv){
		User testUser = new User("1", "testUser");
		User user2 = new User("u2", "ja");
		WorkSpaceRepository wRepository = new JsonBasedWorkSpaceRepository();
		Activity activity = new Activity();
		activity.setId(1);
		activity.setTitle("test");
		Activity activity2 = new Activity();
		activity2.setId(2);
		activity2.setTitle("test2");
		Activity activity3 = new Activity();
		activity3.setId(3);
		activity3.setTitle("test3");
		ActivityTag tag1 = new ActivityTag(1, "®È¹C1");
		ActivityTag tag2 = new ActivityTag(2, "®È¹C2");
		ActivityTag tag3 = new ActivityTag(3, "®È¹C3");
		wRepository.addEditRecordItem(activity, new Item(testUser, new ActivityTag[]{tag1}));
		wRepository.addEditRecordItem(activity, new Item(user2, new ActivityTag[]{tag1}));
		wRepository.addEditRecordItem(activity2, new Item(user2, new ActivityTag[]{tag1}));
		wRepository.addEditRecordItem(activity3, new Item(user2, new ActivityTag[]{tag2,tag3}));
		wRepository.addEditRecordItem(activity3, new Item(testUser, new ActivityTag[]{tag2}));
		int point = wRepository.getUserContributionPoint(testUser);
		System.out.println(point);
		System.out.println( wRepository.getUserContributionPoint(user2));
		
		EditionRecord record = wRepository.getEditionRecord(1);
		System.out.println(record.getItems().get(0).user.getName());
	}
	
	
	
}
