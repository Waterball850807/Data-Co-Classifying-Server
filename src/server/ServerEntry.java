package server;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import model.ActivityRepository;
import model.JdbcActivityRepository;
import model.JdbcProxy;
import server.protocol.ProtocolFactory;
import server.protocol.XOXOXDelimiterProtocolFactory;
import server.workspace.Workspace;

public class ServerEntry {

	public static void main( String[] args ) throws IOException
    {
        IoAcceptor acceptor = new NioSocketAcceptor();

        acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );
        acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ))));

        ProtocolFactory protocolFactory = new XOXOXDelimiterProtocolFactory();
        ActivityRepository activityRepository = new JdbcProxy(new JdbcActivityRepository());
        Workspace workspace = new Workspace(activityRepository, protocolFactory);
        ServerHandler serverHandler = new ServerHandler(protocolFactory, workspace);
        acceptor.setHandler(serverHandler);

        acceptor.getSessionConfig().setReadBufferSize( 2048 );
    }
}
