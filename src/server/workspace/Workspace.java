package server.workspace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import model.Activity;
import model.ActivityRepository;
import server.client.ActivityRecord;
import server.client.ChatMessage;
import server.client.Client;
import server.client.EditionRecord;
import server.client.User;
import server.protocol.Protocol;
import server.protocol.ProtocolFactory;

public class Workspace {
	public static String SUCCESS = "success";
	public static String ERROR = "error";
	public static String EV_SIGNIN = "signIn";
	public static String EV_CHAT = "chat";
	public static String EV_ACTIVITY = "activity";
	public static String EV_TAG = "tag";
	private static int WORKING_QUEUE_SIZE = 10000; 
	private static Gson gson = new Gson();
	
	private ActivityRepository activityRepository;
	private ProtocolFactory protocolFactory;
	private WorkSpaceRepository workSpaceRepository;
	private Map<String, Client> clients = new HashMap<>();  //<client's id, client>
	private Map<String, User> users = new HashMap<>(); //<client's id, user>
	private List<String> workingStage = new ArrayList<>(WORKING_QUEUE_SIZE);
	
	public Workspace(ActivityRepository activityRepository, 
					ProtocolFactory protocolFactory,
					WorkSpaceRepository workSpaceRepository) {
		this.workSpaceRepository = workSpaceRepository;
		this.activityRepository = activityRepository;
		this.protocolFactory = protocolFactory;
	}

	public void addClient(Client client) {
		clients.put(client.getId(), client);
	}

	public void signIn(String id, String name){
		Client client = clients.get(id);
		User user  = new User(client.getId(), name);
		users.put(client.getId(), user);
		sendChat(client.getId(), name + " 登入了！");
	}

	public void removeClient(Client client) {
		synchronized (client) {
			clients.remove(client.getId());
		}
		
		User user = users.get(client.getId());
		sendChat(client.getId(), user.getName() + " 登出了！");
	}

	public void sendChat(String senderId, String msg) {
		String uuid = UUID.randomUUID().toString();
		User sender = users.get(senderId);
		ChatMessage chatMessage = new ChatMessage(uuid, sender, msg);
		String json = gson.toJson(chatMessage);
		Protocol signIn = protocolFactory.createProtocol(EV_CHAT, SUCCESS, null, json);
		broadcastToAll(signIn.toString());
	}

	public void attachTags(String senderId, int activityId, int[] tagIds) {
		User user = users.get(senderId);
		Activity activity = activityRepository.getActivity(activityId);
		activityRepository.attachTagsToActivity(activityId, tagIds);
		StringBuilder tagNamesStrb = new StringBuilder();
		for (int tagId : tagIds)
		{
			String tagName = activityRepository.getActivityTag(tagId).getName();
			tagNamesStrb.append(tagName).append(",");
		}
		String tagNames = tagNamesStrb.substring(0, tagNamesStrb.length()-1);
		sendChat(senderId, user.getName() + " 把 活動[" + activity.getTitle() + 
						"] 分類成  [" +tagNames + "]");
	}

	public void nextActivityRequest(String clientId, int activityId, boolean clean) {
		Client client = clients.get(clientId);
		List<Activity> activities = activityRepository.getActivities();
		int curIndex = 0;
		int moveToIndex = 0;
		synchronized (workingStage) {
			for (int i = 0 ; i < workingStage.size();  i ++)
				if (workingStage.get(i).equals(clientId))
				{
					curIndex = i;
					break;
				}
			for (int i = curIndex; i < activities.size(); i ++) 
			{
				if (workingStage.get(i) == null)
				{
					moveToIndex = i;
					break;
				}
			}
			workingStage.set(curIndex, null);
			workingStage.set(moveToIndex, clientId);
		}
		Activity activity = activities.get(moveToIndex);
		EditionRecord editionRecord = workSpaceRepository.getEditionRecord(activity.getId());
		ActivityRecord activityRecord = new ActivityRecord(activity, editionRecord);
		String json = gson.toJson(activityRecord);
		Protocol protocol = protocolFactory.createProtocol(EV_ACTIVITY, SUCCESS, null, json);
		client.broadcast(protocol.toString());
	}
	
	private void broadcastToAll(String msg){
		clients.values().parallelStream().forEach(c -> {
			synchronized (c) {
				if (c.isActive() && clients.containsKey(c.getId())) 
					c.broadcast(msg);
			}
		});
	}
	
	public static class Params{
		private Map<String, String> params = new HashMap<>();
		public Params add(String key, String value){
			params.put(key, value);
			return this;
		}
		
		public Map<String, String> get(){
			return params;
		}
	}
}
