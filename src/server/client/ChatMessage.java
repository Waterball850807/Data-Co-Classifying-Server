package server.client;

public class ChatMessage {
	private String id;
	private User sender;
	private String content;
	
	public ChatMessage(String id, User sender, String content) {
		this.id = id;
		this.sender = sender;
		this.content = content;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public User getSender() {
		return sender;
	}
	public void setSender(User sender) {
		this.sender = sender;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	
}
