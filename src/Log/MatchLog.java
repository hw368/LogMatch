package Log;
/**
 * class for matched log
 * @author Administrator
 *
 */
public class MatchLog {
	private LogData login;
	
	private LogData logout;
	
	public MatchLog(){
		
	}

	public MatchLog(LogData login, LogData logout) {
		super();
		this.login = login;
		this.logout = logout;
	}

	public LogData getLogin() {
		return login;
	}

	public void setLogin(LogData login) {
		this.login = login;
	}

	public LogData getLogout() {
		return logout;
	}

	public void setLogout(LogData logout) {
		this.logout = logout;
	}
	
	public String toString(){
		return login+"|"+logout;
	}
}








