package cn.lpap.speedrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		Log.d("BootCompletedReceiver", "recevie boot completed ... ");  
        context.startService(new Intent(context, GpsService.class)); 
	}

}
