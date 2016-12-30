package cn.lpap.speedrecorder;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";
	
	private static final String ACTION_START_SERVICE = "com.lpap.speedrecorder.startService";
	
	private static int MSG_START_SVC = 1;
	//private static final long DELAY_START_SVC = 3 * 60 * 1000;
	//private static final long DELAY_START_SVC = (107 - 70) * 1000;
	private static final long DELAY_START_SVC = 10 * 1000;
	private Handler mUI = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {
			LogUtil.logTimestamp(true, TAG, "startService from boot completed.");
			Context context = (Context) msg.obj;
			Intent intent = new Intent(context, GpsService.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent); 
		};
	};
	/*
	 * make sure device not in power save mode to support GPS work in background
	 * After reboot in room, start service to request GPS update, and do not update GPS data; Then outside, 
	 * GPS become update data.
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@SuppressLint("NewApi") @Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		long elapsed = SystemClock.elapsedRealtime() / 1000;
		/*
		 *   message received 79 seconds(71s, 77s) elapsed after about boot completed for redmi 3s
		 *   After reinstall app in same device, directly reboot device without start app manually,
		 *   will receive boot completed message.
		 */
		
		String action = arg1.getAction();
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			LogUtil.logTimestamp(true, TAG, "receive boot completed ...... elapsed = " + elapsed);
			LogUtil.toast(context, "boot completed");
	        //context.startService(new Intent(context, GpsService.class)); 
			Message msg = mUI.obtainMessage(MSG_START_SVC);
			LogUtil.log("Looper.getMainLooper() = " + Looper.getMainLooper());
			msg.obj = context;
			//mUI.sendMessageDelayed(msg, DELAY_START_SVC);
			
			Intent intent =new Intent(context, BootCompletedReceiver.class);  
	        intent.setAction(ACTION_START_SERVICE);  
	        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);  
	        // For background AlarmManager can work in power save mode, but handler can not work 
	        AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);  
	        final int apiLevel = Build.VERSION.SDK_INT;
	        LogUtil.log("apiLevel = " + apiLevel);
	        final long triggerAtMillis = SystemClock.elapsedRealtime() + DELAY_START_SVC;
	        //if(Build.VERSION_CODES.KITKAT <= apiLevel) {
	        if(false) {
	        	alarm.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, sender);
	        } else {
	        	//alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, DELAY_START_SVC, sender);
	        	alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, sender);
	        }
		} else if(ACTION_START_SERVICE.equals(action)) {
			LogUtil.toast(context, "startService");
			LogUtil.logTimestamp(TAG, "action = " + ACTION_START_SERVICE);
			Intent intent = new Intent(context, GpsService.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent); 
		}
		
	}

}
