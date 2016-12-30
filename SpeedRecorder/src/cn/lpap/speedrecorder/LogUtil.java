package cn.lpap.speedrecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class LogUtil {
	private static final String TAG = "LogUtil";
	
	private static final String LOG_PREFIX = "log";
	private static final String LOG_EXT = ".txt";
	private static StringBuffer sBuffer = new StringBuffer();
	private static int BUFF_LEN = 1024;
	
	private static long sLogTime = System.currentTimeMillis();
	private static long LOG_INTERVAL = 1000 * 10;
	
	private volatile static boolean flushStarted = false;
	private static final int MSG_FLUSH = 0;
	private static final long DELAY_FLUSH = 60 * 1000;
	private static Handler sLog = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_FLUSH:
				logTimestamp(true, TAG, "flush log...");
				sLog.removeMessages(MSG_FLUSH);
				sLog.sendEmptyMessageDelayed(MSG_FLUSH, DELAY_FLUSH);
				break;

			default:
				break;
			}
		};
	};
	
	public static void logTimestamp(final boolean flush, final String TAG, final String msg) {
		Date date = new Date();
		log(flush, TAG + " at " + date.toLocaleString(), msg);
	}
	
	public static void logTimestamp(final String TAG, final String msg) {
		Date date = new Date();
		log(TAG + " at " + date.toLocaleString(), msg);
	}
	
	public static void log(final boolean flush, final String TAG, final String msg) {
		log(flush, TAG + ":: " + msg);
	}
	
	public static void log(final String TAG, final String msg) {
		log(TAG + ":: " + msg);
	}
	

	public static void log(final String msg) {
		log(false, msg);
	}
	
	public static void toast(Context context, String msg) {
		if(Constant.DEBUG) {
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
	}
	
	public static void log(final boolean flush, final String msg) {
		sBuffer.append(msg + "\n");
		long cur = System.currentTimeMillis();
		// release case
		if(!Constant.DEBUG
				&& !flushStarted) {
			flushStarted = true;
			sLog.sendEmptyMessageDelayed(MSG_FLUSH, DELAY_FLUSH);
		}
		
		if(Constant.DEBUG
				|| flush
				|| BUFF_LEN < sBuffer.length()
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
