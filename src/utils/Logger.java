package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * @author Waterball
 * A Logger logs all the messages out into the log.txt and err.txt file with the
 * producer-consumer pattern.
 */
public class Logger implements Runnable, AutoCloseable{
	private String logFileName;
	private String errFileName;
	private File logFile;
	private File errFile;
	private boolean start = false;
	private BlockingQueue<Message> msgQueue = new ArrayBlockingQueue<>(100);
	
	private static Logger logger = new Logger("server.log", "error.log");
	public static Logger getLogger(){
		return logger;
	}
	
	public Logger(String logFileName, String errFileName) {
		this.logFileName = logFileName;
		this.errFileName = errFileName;
		logFile = new File(logFileName);
		errFile = new File(errFileName);
		createFilesIfNotExists();
	}
	
	private void createFilesIfNotExists(){
		try{
			if (!logFile.exists())
				logFile.createNewFile();
			if (!errFile.exists())
				errFile.createNewFile();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start(){
		new Thread(this).start();
	}


	@Override
	public void run() {
		start = true;
		while(start)
		{
			try {
				Message message = msgQueue.take();
				writeToFile(message);
			} catch (InterruptedException e) {}
		}
	}	
	
	private void writeToFile(Message message){
		File file;
		if (message.type.equals(Message.ERR))
			file = errFile;
		else
			file = logFile;
		
		try(FileWriter fw = new FileWriter(file, true);
			    BufferedWriter bw = new BufferedWriter(fw);)
		{
			bw.write(message.msg + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void log(Class clazz, String msg){
		try {
			msg = "[" + clazz.getSimpleName() + "] " + new Date() +  "// " + msg;
			System.out.println(msg);
			msgQueue.put(new Message(Message.LOG, msg));
		} catch (InterruptedException e) {}
	}

	public void err(Class clazz, String msg, Exception exception){
		err(clazz, msg + ", Strace [" + exception + "]");
	}
	
	public void err(Class clazz, String msg){
		try {
			msg = "[" + clazz.getSimpleName() + "] " + new Date() +  "// " + msg;
			System.out.println(msg);
			msgQueue.put(new Message(Message.ERR, msg));
		} catch (InterruptedException e) {}
	}
	

	public String getLogFileName() {
		return logFileName;
	}

	public String getErrFileName() {
		return errFileName;
	}
	
	private class Message{
		static final String ERR = "err", LOG = "log";
		String type;
		String msg;
		public Message(String type, String msg) {
			this.type = type;
			this.msg = msg;
		}
	}

	@Override
	public void close(){
		start = false;
	}
}
