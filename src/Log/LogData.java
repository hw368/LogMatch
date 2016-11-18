package Log;
/**
 * class for a single log in the log file
 * @author Administrator
 *
 */
public class LogData {
	
	public static final short TYPE_LOGIN=7;
	
	public static final short TYPE_LOGOUT=8;
	
	public static final int PID_OFFSET=68;
	
	public static final int TYPE_OFFSET=72;
	
	public static final int TIME_OFFSET=80;
	
	public static final int HOST_OFFSET=114;
	
	public static final int LOG_LENGTH=372;
	
	/**
	 * user initial position in one log
	 */
	public static final int USER_OFFSET=0;
	
	public static final int USER_LENGTH=32;
	
	public static final int HOST_LENGTH=258;
	
	private String username;

	private int pid;

	private short type;
	
	private int time;
	
	private String host;
	
	public LogData(){
		
	}

	public LogData(String user, int pid, short type, int time, String host) {
		super();
		this.username = user;
		this.pid = pid;
		this.type = type;
		this.time = time;
		this.host = host;
	}
	public String getUser() {
		return username;
	}

	public void setUser(String user) {
		this.username = user;
	}

	public int getPid() {
		return pid;
	}
	
	public void setTime(int time) {
		this.time = time;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public int getTime() {
		return time;
	}

	public String toString(){
		return username+","+pid+","+
	           type+","+time+","+
			   host;
	}
	
	public LogData(String str){
		String[] data = str.split(",");
		username = data[0];
		pid = Integer.parseInt(data[1]);
		type = Short.parseShort(data[2]);
		time = Integer.parseInt(data[3]);
		host = data[4];
	}
	
	
}




