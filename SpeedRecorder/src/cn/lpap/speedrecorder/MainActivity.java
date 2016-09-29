package cn.lpap.speedrecorder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import cn.lpap.speedrecorder.R;
import cn.lpap.speedrecorder.GpsService.GpsServiceListener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends BaseActivity implements GpsServiceListener {
	private static final String TAG = "MainActivity";
	private Button mAppInfo = null;
	private TextView mSpeedTv = null;
	private TextView mPostion = null;
    private EditText mOtherInfo = null;
    
    private boolean mResumed = false;
    GpsService gpsService = null;
    ServiceConnection sc = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			gpsService.removeListener(MainActivity.this);
			gpsService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			GpsService.MyBinder binder = (GpsService.MyBinder)service;
			gpsService = binder.getService();
			gpsService.addListener(MainActivity.this);
			gpsService.reqLastKnownLocation();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		if(!GpsService.issStarted()) {
			Intent start = new Intent(this, GpsService.class);
			startService(start);
		}
		
		initView();
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {  
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();  
            // 返回开启GPS导航设置界面  
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
            startActivityForResult(intent, 0);
            return;  
        } 
        // 获取位置信息  
        // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER  
        //updateView(location); 
		Intent service = new Intent(this, GpsService.class);
		bindService(service, sc, Context.BIND_AUTO_CREATE);
	}
	
	private void initView() {
		mSpeedTv = (TextView)findViewById(R.id.speed);
		initSpeed();
		mPostion = (TextView)findViewById(R.id.position);
		mOtherInfo = (EditText) findViewById(R.id.other_info);
		mAppInfo = (Button)findViewById(R.id.app_info);
		mAppInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent appInfo = new Intent(MainActivity.this, AppInfo.class);
				startActivity(appInfo);
			}
		});
	}
    
    private String formatTime(long time) {
    	Date date = new Date(time);
    	//System.out.println(date);
    	return date.toLocaleString();
    }
    
    /** 
     * 实时更新文本内容 
     *  
     * @param location 
     */  
    private void updateView(Location location) {
    	final String speed = getSpeed(location);
        mSpeedTv.setText(speed);
        String time = null;
        mPostion.setText(getString(R.string.position_title) + getPosition(location));
        if (location != null) {
            mOtherInfo.setText(getString(R.string.long_title));
            mOtherInfo.append(String.valueOf(location.getLongitude()) + "\n");  
            mOtherInfo.append(getString(R.string.lan_title));  
            mOtherInfo.append(String.valueOf(location.getLatitude()) + "\n");  
            
            mOtherInfo.append(getString(R.string.time_title));
            time = getTime(location);
            mOtherInfo.append(time + "\n");
        } else {  
            // 清空EditText对象  
            mOtherInfo.getEditableText().clear();  
        }  
        LogUtil.log("updateView: " + " speed = " + speed + ", time = " + time);
    }  
    
    private String getPosition(final Location location) {
    	String position = getString(R.string.blank);
    	if(null != location) {
    	}
    	return position;
    }
    
    private String getSpeed(final Location location) {
    	float speed = 0;
    	if(null != location) {
    		speed = Utils.getSpeed(location.getSpeed());
    	}
    	return String.valueOf(speed) + getString(R.string.speed_km_unit);
    }
    
    private String getTime(final Location location) {
        //long time = location.getTime();
    	long time = Calendar.getInstance().getTimeInMillis();
        return formatTime(time);
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mResumed = true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mResumed = false;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy(); 
		unbindService(sc);
	}
	
	private void showToast(final String msg) {
		if(mResumed) {
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		}
	}
	
	private void log(String msg) {
		Log.d(TAG, msg);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
        updateView(location);
	}

	@Override
	public void resetSpeed(final float oldSpeed) {
		// TODO Auto-generated method stub
		LogUtil.log("resetSpeed: oldSpeed = " + oldSpeed);
		showToast(getString(R.string.location_change_error, oldSpeed));
		initSpeed();
	}

	private void initSpeed() {
		mSpeedTv.setText("0" + getString(R.string.speed_km_unit));
	}
}
