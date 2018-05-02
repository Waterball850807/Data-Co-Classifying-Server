package server.protocol;

import java.util.Map;

public interface ProtocolFactory {

	public Protocol createProtocol(String event, 
			String status, Map<String,String> parameters, String data);

	public Protocol createProtocol(String rawData);

}
