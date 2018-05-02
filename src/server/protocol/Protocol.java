package server.protocol;

public interface Protocol {

	public String getEvent();

	public String getStatus();

	public String getParameter(String key);

	public String getData();

}
