package server.client;

import org.apache.mina.core.session.IoSession;

public class ClientUser implements Client{
	private IoSession ioSession;
	
	public ClientUser(IoSession ioSession) {
		this.ioSession = ioSession;
	}

	@Override
	public void broadcast(String msg) {
		ioSession.write(msg);
	}

	@Override
	public String getId() {
		return String.valueOf(ioSession.getId());
	}

	@Override
	public boolean isActive() {
		return ioSession.isActive();
	}

}
