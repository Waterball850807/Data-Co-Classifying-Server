package server.client;

public class ErrorMsg {
	public static int UNKNOWN_ERROR = 0;
	public static int ACTIVITY_HAS_BEEN_HOLDED = 1;
	public static int ACTIVITY_ID_NOT_EXISTS = 2;
	private int errorNumber;
	private String message;
	
	public ErrorMsg(int errorNumber, String message) {
		this.errorNumber = errorNumber;
		this.message = message;
	}

	public int getErrorNumber() {
		return errorNumber;
	}

	public void setErrorNumber(int errorNumber) {
		this.errorNumber = errorNumber;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
