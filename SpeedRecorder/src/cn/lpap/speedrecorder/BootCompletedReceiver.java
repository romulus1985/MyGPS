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
		Date date = new Date();
		LogUtil.log(TAG, "recevie boot completed ... at " + date.toLocaleString());  
        context.startService(new Intent(context, GpsService.class)); 
	}

}
