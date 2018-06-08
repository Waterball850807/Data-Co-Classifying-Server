package server.workspace;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import model.Activity;
import model.ActivityRepository;
import model.ActivityTag;
import server.client.ActivityRecord;
import server.client.ChatMessage;
import server.client.Client;
import server.client.EditionRecord;
import server.client.EditionRecord.Item;
import server.client.ErrorMessage;
import server.client.User;
import server.protocol.Protocol;
import server.protocol.ProtocolFactory;

public class Workspace {
	private static Logger log = LoggerFactory.getLogger(Workspace.class);
	public static String SUCCESS = "success";
	public static String ERROR = "error";
	public static String EV_SIGNIN = "signIn";
	public static String EV_CHAT = "chat";
	public static String EV_ACTIVITY = "activity";
	public static String EV_TAG = "tag";
	public static String EV_GET_TAGS = "getTags";
	private static int WORKING_QUEUE_SIZE = 10000; 
	private static Gson gson = new Gson();
	
	private ActivityRepository activityRepository;
	private ProtocolFactory protocolFactory;
	private WorkSpaceRepository workSpaceRepository;
	private Map<String, Client> clients = Collections.synchronizedMap(new HashMap<>());  //<client's id, client>
	private Map<String, User> users = Collections.synchronizedMap(new HashMap<>()); //<client's id, user>
	private TaskDistributor taskDistributor;
	
	public Workspace(ActivityRepository activityRepository, 
					ProtocolFactory protocolFactory,
					WorkSpaceRepository workSpaceRepository) {
		this.workSpaceRepository = workSpaceRepository;
		this.activityRepository = activityRepository;
		this.protocolFactory = protocolFactory;
		this.taskDistributor = new TaskDistributor(activityRepository, workSpaceRepository);
	}

	public void addClient(Client client) {
		log.debug("New client [" + client.getId() + "] accessed." );
		clients.put(client.getId(), client);
	}

	public void signIn(String id, String name){
		try{
			Client client = clients.get(id);
			synchronized (client) 
			{
				User user  = new User(client.getId(), name);
				users.put(client.getId(), user);
				String userJson = gson.toJson(user);
				Protocol success = protocolFactory.createProtocol(EV_SIGNIN, SUCCESS, null, userJson);
				sendChat(client.getId(), name + " 登入了！");
				client.broadcast(success.toString());
			}
		}catch (Exception e) {
			broadcastErrorToClient(id, ERR_UNKNOWN, e.getMessage());
		}
	}

	public void removeClient(Client client) {
		synchronized (client) {
			clients.remove(client.getId());
			if (users.containsKey(client.getId()))
			{
				User user = users.get(client.getId());
				sendChat(client.getId(), user.getName() + " 登出了！");
				taskDistributor.removeUser(user.getId());
				users.remove(user.getId());
				log.debug("Client [" + client.getId() + "] : " + user.getName() + "  signed out." );
			}
			else
				log.debug("Client [" + client.getId() + "] signed out." );
		}
	}

	public void sendChat(String senderId, String msg) {
		try{
			if (validateSignIn(senderId))
			{
				String uuid = UUID.randomUUID().toString();
				User sender = users.get(senderId);
				ChatMessage chatMessage = new ChatMessage(uuid, sender, msg);
				chatMessage.setSender(sender);
				String json = gson.toJson(chatMessage);
				Protocol signIn = protocolFactory.createProtocol(EV_CHAT, SUCCESS, null, json);
				broadcastToAll(signIn.toString());
			}
		}catch (Exception e) {
			broadcastErrorToClient(senderId, ERR_UNKNOWN, e.getMessage());
		}
	}

	public void attachTags(String senderId, int activityId, int[] tagIds) {
		try{
			if (validateSignIn(senderId))
			{
				if (tagIds == null || tagIds.length == 0)
					broadcastErrorToClient(senderId, ERR_PARAMETERS_INVALID, "tags should not be empty.");
				else
				{
					Client client = clients.get(senderId);
					synchronized (client) 
					{
						User user = users.get(senderId);
						log.debug("Attach tags request from user: " + user.getName() );
						ActivityTag[] activityTags = new ActivityTag[tagIds.length];
						Activity activity = null;
						
						try{
							activity = activityRepository.getActivity(activityId);
						}catch (IllegalArgumentException e) {
							broadcastErrorToClient(senderId, ERR_ACTIVITY_ID_NOT_EXISTS, e.getMessage());
							return;
						}
						
						activityRepository.attachTagsToActivity(activityId, tagIds);
						StringBuilder tagNamesStrb = new StringBuilder();
						for (int i = 0 ; i < tagIds.length ; i ++)
						{
							ActivityTag tag = activityRepository.getActivityTag(tagIds[i]);
							activityTags[i] = tag;
							String tagName = tag.getName();
							tagNamesStrb.append(tagName).append(",");
						}
						workSpaceRepository.addEditRecordItem(activity, new Item(user, activityTags));
						
						String tagNames = tagNamesStrb.substring(0, tagNamesStrb.length()-1);
						sendChat(senderId, user.getName() + " 把 活動[" + activity.getTitle() + 
										"] 分類成  [" +tagNames + "]");

						log.debug("Tags attached on activity " + activityId + " <- " + tagNames);
						Protocol success = protocolFactory.createProtocol(EV_TAG, SUCCESS, null, null);
						client.broadcast(success.toString());
					}
				}
			}
		}catch (Exception e) {
			broadcastErrorToClient(senderId, ERR_UNKNOWN, e.getMessage());
		}
	}
	
	public void nextActivityRequest(String clientId, int activityId, boolean clean) {
		try{
			if (validateSignIn(clientId)) 
			{
				Client client = clients.get(clientId);
				synchronized (client) 
				{
					User user = users.get(clientId);
					log.debug("Next activity request from user: " + user.getName() );
					Activity activity = null;
					try{
						if (activityId == -1)  //next as default
							activity = taskDistributor.moveToNext(clientId, clean);
						else
						{
							if (taskDistributor.isActivityHolded(activityId))
							{
								broadcastErrorToClient(clientId, ERR_ACTIVITY_BEING_HOLDED, null);
								return;
							}
							else
								activity = taskDistributor.moveToActivity(clientId, activityId);
						}
					}catch (IllegalArgumentException e) {
						broadcastErrorToClient(clientId, ERR_ACTIVITY_ID_NOT_EXISTS, e.getMessage());
						return;
					}
					
					EditionRecord editionRecord = workSpaceRepository.getEditionRecord(activity.getId());
					ActivityRecord activityRecord = new ActivityRecord(activity, editionRecord);
					String json = gson.toJson(activityRecord);
					Protocol protocol = protocolFactory.createProtocol(EV_ACTIVITY, SUCCESS, null, json);
					client.broadcast(protocol.toString());
				}
			}
		}catch (Exception e) {
			broadcastErrorToClient(clientId, ERR_UNKNOWN, e.getMessage());
		}
	}
	
	public void loadAllActivityTags(String clientId){
		Client client = clients.get(clientId);
		try{
			ActivityTag[] activityTags = activityRepository.getActivityTags();
			String json = gson.toJson(activityTags);
			Protocol success = protocolFactory.createProtocol(EV_GET_TAGS, SUCCESS, null, json);
			client.broadcast(success.toString());
		}catch (Exception e) {
			broadcastErrorToClient(clientId, 0, e.getMessage());
		}
	}
	
	private void broadcastToAll(String msg){
		log.debug("Broadcasting to all: " + msg );
		clients.values().parallelStream().forEach(c -> {
			if (c.isActive() && clients.containsKey(c.getId())) 
				c.broadcast(msg);
		});
	}
	
	public static final int ERR_UNKNOWN = 0;
	public static final int ERR_ACTIVITY_BEING_HOLDED = 1;
	public static final int ERR_ACTIVITY_ID_NOT_EXISTS = 2;
	public static final int ERR_SIGN_IN_FIRST = 3;
	public static final int ERR_PARAMETERS_INVALID = 4;
	private static Map<Integer, ErrorMessage> errMessagesMap = new HashMap<>();
	static{
		errMessagesMap.put(ERR_UNKNOWN, new ErrorMessage(ERR_UNKNOWN, "Unknown error occurs."));
		errMessagesMap.put(ERR_ACTIVITY_BEING_HOLDED, new ErrorMessage(ERR_ACTIVITY_BEING_HOLDED, "The activity is being holded by another."));
		errMessagesMap.put(ERR_ACTIVITY_ID_NOT_EXISTS, new ErrorMessage(ERR_ACTIVITY_ID_NOT_EXISTS, "The id doesn't exist, please choose the valid activity's id."));
		errMessagesMap.put(ERR_SIGN_IN_FIRST, new ErrorMessage(ERR_SIGN_IN_FIRST, "You should sign in first."));
		errMessagesMap.put(ERR_PARAMETERS_INVALID, new ErrorMessage(ERR_PARAMETERS_INVALID, "Parameters are not valid."));
	}
	
	private boolean validateSignIn(String clientId){
		if(clients.containsKey(clientId))
		{
			Client client = clients.get(clientId);
			synchronized (client) {
				if (!users.containsKey(clientId))
				{
					broadcastErrorToClient(clientId, ERR_SIGN_IN_FIRST, null);
					return false;
				}
				return true;
			}
		}
		else
			return false;
	}
	
	private void broadcastErrorToClient(String clientId, int errorNumber, String additionalMsg){
		Client client = clients.get(clientId);
		ErrorMessage erm = errMessagesMap.get(errorNumber);
		if (additionalMsg != null)
			erm.setErrorMessage(erm.getErrorMessage() + "[" + additionalMsg + "]");
		log.debug("Error occurs on client [" + clientId + "] with error msg: " + erm.getErrorMessage() );
		String json = gson.toJson(erm);
		Protocol error = protocolFactory.createProtocol(ERROR, ERROR, null, json);
		client.broadcast(error.toString());
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
