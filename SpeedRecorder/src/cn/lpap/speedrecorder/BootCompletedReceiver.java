package cn.lpap.speedrecorder;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		LogUtil.logTimestamp(TAG, "receive boot completed ...");  
        context.startService(new Intent(context, GpsService.class)); 
	}

}
