package cn.lpap.speedrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		LogUtil.log(TAG, "recevie boot completed ... ");  
        context.startService(new Intent(context, GpsService.class)); 
	}

}
