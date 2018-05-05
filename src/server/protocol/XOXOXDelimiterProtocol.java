package server.protocol;

import java.util.HashMap;
import java.util.Map;

import utils.Logger;

public class XOXOXDelimiterProtocol implements Protocol{
	static final String DELIMITER = "XOXOX";
	private Logger logger = Logger.getLogger();
	private String content;
	private Map<String, String> parameters;
	private String event;
	private String status;
	private String data;
	
	
	public XOXOXDelimiterProtocol(String event, Map<String, String> parameters, String status, String data) {
		this.parameters = parameters;
		this.event = event;
		this.status = status;
		this.data = data;
		this.content = event + "?" + paramtersToString(parameters) + DELIMITER + status + DELIMITER + data;
	}

	public XOXOXDelimiterProtocol(String content) {
		this.content = content = content.trim();
		
		//e.g. event?key=value&key=valueXOXOXstatusXOXOX{data}
		String[] snippets = content.split(DELIMITER);
		String[] firstSegment = snippets[0].split("\\?") ;
		event = firstSegment[0];
		parameters = firstSegment.length >= 2 ? parseParameters(firstSegment[1]) : null;
		status = snippets[1];
		data = snippets[2];
		logger.log(getClass(), getLogText());
	}
	
	public String getLogText(){
		StringBuilder paramStrb = new StringBuilder();
		paramStrb.append("-");
		if (parameters != null)
			for (String key : parameters.keySet())
				paramStrb.append("    ").append(key).append(" : ")
					.append(parameters.get(key)).append("\n");
		return String.format("%nEvent: %s%n Parameters: %n%s%n Status: %s%n Data: %s%n", 
							event, paramStrb.toString(), status, data);
	}
	
	public static String paramtersToString(Map<String, String> parameters){
		if (parameters == null)
			return null;
		StringBuilder paramStrb = new StringBuilder();
		for (String key : parameters.keySet())
			paramStrb.append(key).append("=").append(parameters.get(key)).append("&");
		return 	paramStrb.substring(0, paramStrb.length()-1);
	}
	
	private Map<String, String> parseParameters(String query) {
		Map<String, String> parameters = new HashMap<>();
		String[] paramSegments = query.split("&");
		for (String param : paramSegments)
		{
			String[] paramSplitted = param.split("="); //key=value
			parameters.put(paramSplitted[0], paramSplitted[1]);
		}
		return parameters;
	}
	
	@Override
	public String getParameter(String key) {
		if (parameters == null)
			return null;
		return parameters.get(key);
	}
	
	@Override
	public String getEvent() {
		return event;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public String getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return content;
	}
	
	public static void main(String[] argv){
		String request = "event?key1=val1&key2=val2&kexo3=keyxoxoXOXOXsuccessXOXOX{\"data\":\"123\"}";
		Protocol protocol = new XOXOXDelimiterProtocol(request);
		
		Map<String, String> myParams = new HashMap<>();
		myParams.put("key1", "val1");
		myParams.put("key2", "val2");
		myParams.put("key3", "val3");
		System.out.println(new XOXOXDelimiterProtocol("event", myParams, "status", "{\"data\":\"123\"}"));
		
	}
}
