package Client_Server;
import Log.LogData;
import Log.MatchLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 *   Runs on server which provides Unix service.
 *   Function: periodic analysis system log file wtmpx and match the information in the
 *   log file by log in and log out of client. Then send match logs to DMS system server
 *   finishing the information collection.
 *   Three Steps:
 *   1. Analysing wtmpx(unix system log file) file
 *   2. Matching logs
 *   3. upload matched logs in server side to save
 *   
 *       
 * @author Administrator
 *
 */
public class Client {
	
	//Unix system log file
	private File logFile;
	
	//File to save analyzed logs 
	private File LogFiletext;
	
	//file to save matched logs
	private File logMatchFile;
	
	//file to save unmatched logs
	private File loginLogFile;
	
	//marker file
	private File marker;
	
	//number of logs analyzed once
	private int numOnce;
	
	//server address
	private String serverHost;
	
	//server port
	private int serverPort;
	
	public Client() throws Exception{
		try {
			Map<String,String> config = loadConfig();
			init(config);
		} catch (Exception e) {
			System.out.println("Client Initialization Failed!");
			throw e;
		}
	}
	/**
	 * Initialize client with config file
	 * @param config
	 */
	private void init(Map<String,String> config){
		
		logFile = new File(config.get("logfile"));
		LogFiletext = new File(config.get("textlogfile"));
		marker = new File(config.get("lastpositionfile"));
		numOnce = Integer.parseInt(config.get("numOnce"));
		
		logMatchFile = new File(config.get("logMatchFile"));
		loginLogFile = new File(config.get("loginlogfile"));
		
		serverHost = config.get("serverhost");
		
		serverPort = Integer.parseInt(config.get("serverport"));
		
	}
	
	/**
	 * Load config file and save content into Map.
	 * @return
	 */
	private Map<String,String> loadConfig()throws Exception{
		Map<String,String> configtable = new HashMap<String,String>();
		
		SAXReader reader = new SAXReader();
		
		Document doc
			= reader.read(
				new File("configuration.xml")
			);
		
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		for(Element ele : list){
			String key = ele.getName();
			String value = ele.getTextTrim();
			configtable.put(key, value);
		}
		
		return configtable;
	}
	
	
	/**
	 * Upload matching logs
	 * @return
	 */
	private boolean sendLogs(){
		Socket socket = null;
		try {
			if(!logMatchFile.exists()){
				System.out.println(logMatchFile+"does not exist!");
				return false;
			}
			List<String> matches 
				= IOU.readLogRec(logMatchFile);
			
			//testing
			//for(String log : matches){
				//System.out.println(log);
			//}
			socket = new Socket(
				serverHost,serverPort
			);
		
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream(),"UTF-8"));
			
			for(String log : matches){
				pw.println(log);
			}
			
			pw.println("Over");
			pw.flush();
			
			BufferedReader br
				= new BufferedReader(
				new InputStreamReader(socket.getInputStream(),"UTF-8"));
			
			String resp = br.readLine();
			
			if("Ok".equals(resp)){
				logMatchFile.delete();
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	/**
	 * Client begins to work
	 */
	public void begin(){
		try{
			while(true){
				if(!parseL()){
					continue;
				}
				if(!matchLogs()){
					continue;
				}
				sendLogs();
			}
		}catch(Exception e){
			System.out.println("Client Running Failed!");
		}
	}
	/**
	 * Log Matching
	 * @return
	 */
	private boolean matchLogs(){
		try {
			
			if(logMatchFile.exists()){
				return true;
			}
			
			if(!LogFiletext.exists()){
				System.out.println(LogFiletext+"does not exist!");
				return false;
			}
			
			List<LogData> list = IOU.readLog(LogFiletext);
			//testing
//			for(LogData log : list){
//			System.out.println(log);
//			}
			
			if(loginLogFile.exists()){
				list.addAll(IOU.readLog(loginLogFile));
			}
			
			Map<String,LogData> loginMap = new HashMap<String,LogData>();
			
			Map<String,LogData> logoutMap = new HashMap<String,LogData>();
			
			List<MatchLog> matches = new ArrayList<MatchLog>();
			for(LogData log : list){
				String key 
					= log.getUser()+","+ log.getPid()+","+log.getHost();
				if(log.getType()==LogData.TYPE_LOGIN){
					loginMap.put(key, log);
				}else if(log.getType()==LogData.TYPE_LOGOUT){
					logoutMap.put(key, log);
				}
			}
			
			//testing
//			System.out.println("login map:");
//			System.out.println(loginMap);
//			
//			System.out.println("logout map:");
//			System.out.println(logoutMap);
			
			for(Entry<String,LogData> e:logoutMap.entrySet()){
				String key = e.getKey();
				LogData logout = e.getValue();
				LogData login = loginMap.remove(key);
				
				MatchLog logRec = new MatchLog(login,logout);
				matches.add(logRec);			
			}
			
			IOU.saveCollection(matches, logMatchFile);
			
			IOU.saveCollection(loginMap.values(), loginLogFile);
			
			LogFiletext.delete();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Analysis Unix system logs
	 * @return 
	 */
	private boolean parseL(){
		RandomAccessFile ranFile = null;
		try {
			if(LogFiletext.exists()){
				return true;
			}
			if(!logFile.exists()){
				System.out.println(logFile+"does not exist!");
				return false;
			}
			long lastPosition = hasLogs();
			if(lastPosition<0){
				System.out.println("End of file!");
				return false;
			}
			//testing
//			System.out.println(
//				"lastPosition:"+lastPosition
//			);
			
			ranFile = new RandomAccessFile(
				logFile,"r"
			);
			ranFile.seek(lastPosition);
			List<LogData> list
				= new ArrayList<LogData>();
			
			for(int i=0;i<numOnce;i++){
				
				if(logFile.length()-lastPosition<LogData.LOG_LENGTH){
					break;
				}
				ranFile.seek(lastPosition+LogData.USER_OFFSET);
				String user = IOU.readStr(ranFile, LogData.USER_LENGTH).trim();
				
				ranFile.seek(lastPosition+LogData.PID_OFFSET);
				int pid = ranFile.readInt();
				
				//type
				ranFile.seek(lastPosition+LogData.TYPE_OFFSET);
				short type = ranFile.readShort();
				
				//time
				ranFile.seek(lastPosition+LogData.TIME_OFFSET);
				int time = ranFile.readInt();
				
				//host
				ranFile.seek(lastPosition+LogData.HOST_OFFSET);
				String host = IOU.readStr(ranFile, LogData.HOST_LENGTH).trim();
				
				LogData logData = new LogData(user, pid, type, time, host);
				list.add(logData);
				lastPosition = ranFile.getFilePointer();
			}
			
			//testing
//			for(LogData log : list){
//				System.out.println(log);
//			}
			IOU.saveCollection(list, LogFiletext);
			IOU.saveLong(lastPosition, marker);
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(ranFile!=null){
				try {
					ranFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	/**
	 * Judge whether there is a marker or not.
	 * @return
	 * @throws Exception 
	 */
	private long hasLogs() throws Exception{
		try {
			if(!marker.exists()){
				return 0;
			}
			
			long lasPosition 
				= IOU.readLong(marker);
			
			if(logFile.length()-lasPosition>=LogData.LOG_LENGTH){
				return lasPosition;
			}
			
		} catch (Exception e) {
			System.out.println("Further abnormal logs!");
			throw e;
		}
		return -1;
	}
	
	public static void main(String[] args) {
		try{
			Client client = new Client();
			client.begin();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}











