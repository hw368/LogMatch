package Client_Server;

import java.io.BufferedReader;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


/**
 * Server side receives all matching logs from client and save them into a file
 * @author Administrator
 *
 */
public class Server {

	private ServerSocket server;
	
	private ExecutorService threadPool;
	
	private File serverLogFile;
	
	private BlockingQueue<String> message;
	
	
	/**
	 * Server begin to work
	 * @throws Exception
	 */
	public void begin() throws Exception{
		try {
			SaveHandler saveHandler
				= new SaveHandler();
			Thread t = new Thread(saveHandler);
			t.start();
			
			while(true){
				Socket socket = server.accept();
				Handler handler= new Handler(socket);
				threadPool.execute(handler);				
			}
		} catch (Exception e) {
			System.out.println("Server running error!");
			throw e;
		}
	}
	
	/**
	 * Pick every matching log from message queue
	 * @author Administrator
	 *
	 */
	private class SaveHandler implements Runnable{
		public void run(){
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(serverLogFile);
				
				while(true){
					if(message.size()>0){
						String log = message.poll();
						pw.println(log);
					}else{
						pw.flush();
						Thread.sleep(300);
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				if(pw != null){
					pw.close();
				}
			}
		}
	}
	/**
	 * Constructor
	 * @throws Exception
	 */
	public Server() throws Exception{
		try {
			threadPool = Executors.newFixedThreadPool(20);
			
			serverLogFile = new File("log.txt");
			
			server = new ServerSocket(8080);
			
			message = new LinkedBlockingQueue<String>();
			
		} catch (Exception e) {
			System.out.println("Server Initilization failed!");
			throw e;
		}
	}
	
	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.begin();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handle client matching logs
	 * @author Administrator
	 *
	 */
	private class Handler 
					implements Runnable{
		private Socket socket;
		
		public Handler(Socket socket){
			this.socket = socket;
		}
		
		public void run(){
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(
					new OutputStreamWriter(
						socket.getOutputStream(),"UTF-8")	
				);
				
				BufferedReader br
					= new BufferedReader(
					new InputStreamReader(
						socket.getInputStream(),"UTF-8"));
				String line = null;
				while((line=br.readLine())!=null){
					if("Over".equals(line)){
						break;
					}
					message.offer(line);
				}
				pw.println("Ok");
				pw.flush();
				
			} catch (Exception e) {
				e.printStackTrace();
				pw.println("ERROR");
				pw.flush();
			} finally{
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}


