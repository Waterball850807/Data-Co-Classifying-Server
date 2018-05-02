package server.client;

public interface Client {

	public String getId();
	public boolean isActive();
	public void broadcast(String msg);

}
