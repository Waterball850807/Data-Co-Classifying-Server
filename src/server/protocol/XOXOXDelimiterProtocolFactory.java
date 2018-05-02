package server.protocol;

import java.util.Map;

public class XOXOXDelimiterProtocolFactory implements ProtocolFactory{

	@Override
	public Protocol createProtocol(String event, String status, Map<String, String> parameters, String data) {
		return new XOXOXDelimiterProtocol(event, parameters, status, data);
	}

	@Override
	public Protocol createProtocol(String rawData) {
		return new XOXOXDelimiterProtocol(rawData);
	}

}
