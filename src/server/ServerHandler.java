package server;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import server.client.Client;
import server.client.ClientUser;
import server.protocol.ProtocolFactory;
import server.workspace.Workspace;

public class ServerHandler extends IoHandlerAdapter{
	private RequestParser requestParser;
	private ProtocolFactory protocolFactory;
	private Workspace workspace;
	
	public ServerHandler(ProtocolFactory protocolFactory, Workspace workspace) {
		this.protocolFactory = protocolFactory;
		this.workspace = workspace;
		requestParser = new RequestParser(workspace);
	}

	@Override
    public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
    {
		if (session.isActive())
		{
			cause.printStackTrace();
			session.write("Error: " + cause.getMessage() + ".");
		}
    }

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		Client client = new ClientUser(session);
		workspace.addClient(client);
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		Client client = new ClientUser(session);
		workspace.removeClient(client);
	}
	
    @Override
    public void messageReceived( IoSession session, Object message ) throws Exception
    {
    	requestParser.execute(String.valueOf(session.getId()), 
    										message.toString(),
    										protocolFactory);
    }

}
