package cn.lpap.speedrecorder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import cn.lpap.speedrecorder.R;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends BaseActivity {
	private static final String TAG = "MainActivity";
	private static final long MIN_TIME = 1000;
	
	private GpsDatabaseHelper mDb = null;
	private Button mAppInfo = null;
	private TextView mSpeedTv = null;
	private TextView mPostion = null;
    private EditText mOtherInfo = null;
    private float mSpeed = 0;
    
    private boolean mResumed = false;
	
	private LocationManager mLm = null;
	
	private static final int MSG_RESET = 0;
	
	private static final long DELAY_RESET = 2 * MIN_TIME;
	private Handler mUI = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_RESET:
				if(mSpeed > 0) {
					mSpeed = 0;
					showToast(getString(R.string.location_change_error, mSpeed));
					mSpeedTv.setText("0" + getString(R.string.speed_km_unit));
				}
				//updateView(null);
				break;

			default:
				break;
			}
		};
	};
	
	// 位置监听  
    private LocationListener mLocationListener = new LocationListener() {  
  
        /** 
         * 位置信息变化时触发 
         */  
        public void onLocationChanged(Location location) {  
            updateView(location);
            if(null != location) {
            	mSpeed = location.getSpeed();
            }
            
            mUI.removeMessages(MSG_RESET);
            mUI.sendEmptyMessageDelayed(MSG_RESET, DELAY_RESET);
            
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    		String dateDesc = sdf.format(cal.getTime());
    		final float speed = location.getSpeed();
    		final float speedManual = Utils.getSpeed(speed);
            mDb.insert(String.valueOf(cal.getTimeInMillis()), 
            		dateDesc,
            		String.valueOf(location.getSpeed()), 
            		String.valueOf(speedManual),
            		String.valueOf(location.getLongitude()),
            		String.valueOf(location.getLatitude()));
            Log.i(TAG, "时间：" + location.getTime());  
            Log.i(TAG, "经度：" + location.getLongitude());  
            Log.i(TAG, "纬度：" + location.getLatitude());  
            Log.i(TAG, "海拔：" + location.getAltitude());  
        }  
  
        /** 
         * GPS状态变化时触发 
         */  
        public void onStatusChanged(String provider, int status, Bundle extras) {  
            switch (status) {  
            // GPS状态为可见时  
            case LocationProvider.AVAILABLE:  
                Log.i(TAG, "当前GPS状态为可见状态"); 
                showToast("当前GPS状态为可见状态");
                break;  
            // GPS状态为服务区外时  
            case LocationProvider.OUT_OF_SERVICE:  
                Log.i(TAG, "当前GPS状态为服务区外状态"); 
                showToast("当前GPS状态为服务区外状态"); 
                break;  
            // GPS状态为暂停服务时  
            case LocationProvider.TEMPORARILY_UNAVAILABLE:  
                Log.i(TAG, "当前GPS状态为暂停服务状态");   
                showToast("当前GPS状态为暂停服务状态"); 
                break;  
            }  
        }  
  
        /** 
         * GPS开启时触发 
         */  
        public void onProviderEnabled(String provider) {  
            Location location = mLm.getLastKnownLocation(provider);  
            updateView(location);  
        }  
  
        /** 
         * GPS禁用时触发 
         */  
        public void onProviderDisabled(String provider) {  
            updateView(null);  
        }  
  
    }; 
    
 // 状态监听  
    GpsStatus.Listener mGpsStatListener = new GpsStatus.Listener() {  
        public void onGpsStatusChanged(int event) {  
            switch (event) {  
            // 第一次定位  
            case GpsStatus.GPS_EVENT_FIRST_FIX:  
                Log.i(TAG, "第一次定位");   
                showToast("第一次定位"); 
                break;  
            // 卫星状态改变  
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Log.i(TAG, "卫星状态改变");  
                //showToast("卫星状态改变"); 
                // 获取当前状态  
                GpsStatus gpsStatus = mLm.getGpsStatus(null);  
                // 获取卫星颗数的默认最大值  
                int maxSatellites = gpsStatus.getMaxSatellites();  
                // 创建一个迭代器保存所有卫星  
                Iterator<GpsSatellite> iters = gpsStatus.getSatellites()  
                        .iterator();  
                int count = 0;  
                while (iters.hasNext() && count <= maxSatellites) {  
                    GpsSatellite s = iters.next();  
                    count++;  
                }  
                System.out.println("搜索到：" + count + "颗卫星");   
                //showToast("搜索到：" + count + "颗卫星"); 
                break;  
            // 定位启动  
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(TAG, "定位启动");  
                showToast("定位启动"); 
                break;  
            // 定位结束  
            case GpsStatus.GPS_EVENT_STOPPED:  
                Log.i(TAG, "定位结束");  
                showToast("定位结束"); 
                break;  
            }  
        };  
    };  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		mDb = new GpsDatabaseHelper(this);
		
		initView();
		
		mLm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		if (!mLm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {  
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();  
            // 返回开启GPS导航设置界面  
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
            startActivityForResult(intent, 0);
            return;  
        } 
		final String bestProvider = mLm.getBestProvider(getCriteria(), true);  
        // 获取位置信息  
        // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER  
        Location location = mLm.getLastKnownLocation(bestProvider); 
        updateView(location); 
        mLm.addGpsStatusListener(mGpsStatListener);
        
        // 绑定监听，有4个参数  
        // 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种  
        // 参数2，位置信息更新周期，单位毫秒  
        // 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息  
        // 参数4，监听  
        // 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新  
  
        // 1秒更新一次，或最小位移变化超过1米更新一次；  
        // 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置  
        mLm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 1, mLocationListener);
        //lm.requestLocationUpdates(bestProvider, 1000, 1, locationListener);
	}
	
	private void initView() {
		mSpeedTv = (TextView)findViewById(R.id.speed);
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
	
	/** 
     * 返回查询条件 
     *  
     * @return 
     */  
    private Criteria getCriteria() {  
        Criteria criteria = new Criteria();  
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细  
        criteria.setAccuracy(Criteria.ACCURACY_FINE);  
        // 设置是否要求速度  
        //criteria.setSpeedRequired(false);
        criteria.setSpeedRequired(true);  
        // 设置是否允许运营商收费  
        criteria.setCostAllowed(false);  
        // 设置是否需要方位信息  
        //criteria.setBearingRequired(false);  
        criteria.setBearingRequired(true);  
        // 设置是否需要海拔信息  
        criteria.setAltitudeRequired(false);  
        // 设置对电源的需求  
        criteria.setPowerRequirement(Criteria.POWER_LOW);  
        return criteria;  
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
        mSpeedTv.setText(getSpeed(location));
        mPostion.setText(getString(R.string.position_title) + getPosition(location));
        if (location != null) {
            mOtherInfo.setText(getString(R.string.long_title));
            mOtherInfo.append(String.valueOf(location.getLongitude()) + "\n");  
            mOtherInfo.append(getString(R.string.lan_title));  
            mOtherInfo.append(String.valueOf(location.getLatitude()) + "\n");  
            
            mOtherInfo.append(getString(R.string.time_title));
            mOtherInfo.append(getTime(location) + "\n");
        } else {  
            // 清空EditText对象  
            mOtherInfo.getEditableText().clear();  
        }  
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
		mLm.removeUpdates(mLocationListener);  
	}
	
	private void showToast(final String msg) {
		if(mResumed) {
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		}
	}
	
	private void log(String msg) {
		Log.d(TAG, msg);
	}
}