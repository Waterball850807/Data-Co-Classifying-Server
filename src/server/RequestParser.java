package server;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.gson.Gson;

import server.client.ChatMessage;
import server.protocol.Protocol;
import server.protocol.ProtocolFactory;
import server.workspace.Workspace;
import utils.GsonUtils;

public class RequestParser {
	private Map<String, BiConsumer<String, Protocol>> apiMatchingMap = new HashMap<>();
	private Workspace workspace;
	private Gson gson = GsonUtils.getGson();
	
	public RequestParser(Workspace workspace){
		this.workspace = workspace;
		initApiMatching();
	}
	
	private void initApiMatching() {
		apiMatchingMap.put(Workspace.EV_SIGNIN, (id, protocol) -> signIn(id, protocol));
		apiMatchingMap.put(Workspace.EV_ACTIVITY, (id, protocol) -> getActivity(id, protocol));
		apiMatchingMap.put(Workspace.EV_CHAT, (id, protocol) -> sendChat(id, protocol));
		apiMatchingMap.put(Workspace.EV_TAG, (id, protocol) -> attachActivityTags(id, protocol));
		apiMatchingMap.put(Workspace.EV_GET_TAGS, (id, protocol) -> workspace.loadAllActivityTags(id));
	}

	private void signIn(String id, Protocol protocol){
		String name = protocol.getParameter("name");
		workspace.signIn(id, name);
	}
	
	private void getActivity(String id, Protocol protocol){
		int activityId = Integer.parseInt(protocol.getParameter("id"));
		boolean clean = Boolean.parseBoolean(protocol.getParameter("clean"));
		workspace.nextActivityRequest(id, activityId, clean);
	}
	
	private void sendChat(String id, Protocol protocol){
		ChatMessage msg =  gson.fromJson(protocol.getData(), ChatMessage.class);
		workspace.sendChat(id, msg.getContent());
	}
	
	public void attachActivityTags(String id, Protocol protocol){
		int activityId = Integer.parseInt(protocol.getParameter("activityId"));
		String[] tagIdSegments = protocol.getParameter("tagIds").split(",");
		int[] tagIds = new int[tagIdSegments.length];
		for (int i = 0 ; i < tagIdSegments.length ; i ++)
			tagIds[i] = Integer.parseInt(tagIdSegments[i].trim());
		workspace.attachTags(id, activityId, tagIds);
	}

	public void execute(String clientId, String rawData, ProtocolFactory protocolFactory) {
		Protocol protocol = protocolFactory.createProtocol(rawData);
		apiMatchingMap.get(protocol.getEvent())
						.accept(clientId, protocol);
	}

	public static void main(String[] argv){
		ChatMessage msg =  GsonUtils.getGson().fromJson("{\"content\":\"hello everyone!\"}", ChatMessage.class);
		System.out.println(msg.getContent());
	}
}
