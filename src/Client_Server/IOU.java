package Client_Server;

import java.io.BufferedReader;
import java.io.File;
import Log.LogData;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Provide several read/write methods 
 * @author Administrator
 *
 */
public class IOU {
	
	/**
	 * reading characters in given length
	 * @param raf
	 * @param length
	 * @return
	 * @throws Exception 
	 */
	public static String readStr(RandomAccessFile raf,int length)
								throws Exception{
		try {
			byte[] data = new byte[length];
			raf.read(data);
			return new String(data,"ISO8859-1");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * read one line string from given file and return a list of matching log
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static List<String> readLogRec(File file)throws Exception{
		BufferedReader br = null;
		try {
			br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file))	
			);
			List<String> list 
				= new ArrayList<String>();
			
			String line = null;
			while((line=br.readLine())!=null){
				list.add(line);
			}
			return list;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally{
			if(br != null){
				br.close();
			}
		}
	}
	
		public static List<LogData> readLog(File file)throws Exception{
		BufferedReader br = null;
		try {
			br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file))	
			);
			List<LogData> list = new ArrayList<LogData>();
	
			String line = null;
			while((line=br.readLine())!=null){
				LogData logData = new LogData(line);
				list.add(logData);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally{
			if(br != null){
				br.close();
			}
		}
	}
	
	/**
	 * write to file
	 * @param l
	 * @param file
	 * @throws Exception
	 */
	public static void saveLong(long l,File file)throws Exception{
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			pw.println(l);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally{
			if(pw != null){
				pw.close();
			}
		}
	}

	public static void saveCollection(Collection o,File file)
						  throws Exception{
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			for(Object obj : o){
				pw.println(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally{
			if(pw != null){
				pw.close();
			}
		}
	}
	
	/**
	 *reading one line of characters in given file and return as long 
	 * @param file
	 * @return
	 * @throws Exception 
	 */
	public static long readLong(File file) 
						   throws Exception{
		BufferedReader br = null;
		try {
			br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file))	
			);
			String line = br.readLine().trim();
			return Long.parseLong(line);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally{
			if(br != null){
				br.close();
			}
		}
	}
}


