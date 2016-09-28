package cn.lpap.speedrecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class LogUtil {
	private static final String LOG_PREFIX = "log";
	private static final String LOG_EXT = ".txt";
	private static StringBuffer sBuffer = new StringBuffer();
	private static int BUFF_LEN = 1024;
	
	private static long sLogTime = System.currentTimeMillis();
	private static long LOG_INTERVAL = 1000 * 10;
	public static void log(final String msg) {
		sBuffer.append(msg + "\n");
		long cur = System.currentTimeMillis();
		if(BUFF_LEN < sBuffer.length()
				|| LOG_INTERVAL < (cur - sLogTime)) {
			Date date = new Date();
			String path = Utils.getExportFolder() 
					+ File.separator + LOG_PREFIX + Utils.getUniqueHourName()
					+ LOG_EXT;
			File log = new File(path);
			FileOutputStream output = null;
			BufferedWriter buff = null;
			try {
				if(!log.exists()) {
					log.createNewFile();
				}
				output = new FileOutputStream(log, true);
				buff = new BufferedWriter(new OutputStreamWriter(output));
				buff.write(sBuffer.toString());
				buff.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if(null != buff) {
						buff.close();
					}
					if(null != output) {
						output.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sLogTime = System.currentTimeMillis();
			// clear
			sBuffer.delete(0, sBuffer.length() -1);
		}
	}
}
