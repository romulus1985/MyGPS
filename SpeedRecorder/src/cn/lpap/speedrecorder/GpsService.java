package cn.lpap.speedrecorder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class GpsService extends Service {
	private static final String TAG = "GpsService";
	
	public interface GpsServiceListener {
		public void onLocationChanged(Location location);
		public void resetSpeed(final float oldSpeed);
	}
	private MyBinder mBinder;	
	private LocationManager mLm = null;
	private static final long MIN_TIME = 1000;
	private Set<GpsServiceListener> mListeners = new HashSet<GpsServiceListener>();
	private GpsDatabaseHelper mDb = null;
    private float mSpeed = 0;

	private static final int MSG_RESET = 0;
	private static final long DELAY_RESET = 2 * MIN_TIME;
	private Handler mUI = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_RESET:
				if(mSpeed > 0) {
					resetSpeed(mSpeed);
					mSpeed = 0;
				}
				//updateView(null);
				break;

			default:
				break;
			}
		};
	};
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mBinder = new MyBinder();
		
		mDb = new GpsDatabaseHelper(this);
		
		mLm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
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
	
	public void reqLastKnownLocation(){
		final String bestProvider = mLm.getBestProvider(getCriteria(), true);  
        Location location = mLm.getLastKnownLocation(bestProvider); 
        notifyListeners(location);
	}
	
	private void notifyListeners(Location location) {
		Iterator<GpsServiceListener> listeners = mListeners.iterator();
		while(listeners.hasNext()) {
			GpsServiceListener l = listeners.next();
			l.onLocationChanged(location);
		}
	}
	
	private void resetSpeed(final float oldSpeed) {
		Iterator<GpsServiceListener> listeners = mListeners.iterator();
		while(listeners.hasNext()) {
			GpsServiceListener l = listeners.next();
			l.resetSpeed(oldSpeed);
		}
	}
	
	public void addListener(final GpsServiceListener listener) {
		mListeners.add(listener);
	}
	
	public void removeListener(final GpsServiceListener listener) {
		mListeners.remove(listener);
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
	
	public class MyBinder extends Binder {
		public GpsService getService() {
			return GpsService.this;
		}
	}


	
	// 位置监听  
    private LocationListener mLocationListener = new LocationListener() {  
  
        /** 
         * 位置信息变化时触发 
         */  
        public void onLocationChanged(Location location) {  
        	notifyListeners(location);
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
                //showToast("当前GPS状态为可见状态");
                break;  
            // GPS状态为服务区外时  
            case LocationProvider.OUT_OF_SERVICE:  
                Log.i(TAG, "当前GPS状态为服务区外状态"); 
                //showToast("当前GPS状态为服务区外状态"); 
                break;  
            // GPS状态为暂停服务时  
            case LocationProvider.TEMPORARILY_UNAVAILABLE:  
                Log.i(TAG, "当前GPS状态为暂停服务状态");   
                //showToast("当前GPS状态为暂停服务状态"); 
                break;  
            }  
        }  
  
        /** 
         * GPS开启时触发 
         */  
        public void onProviderEnabled(String provider) {  
            Location location = mLm.getLastKnownLocation(provider);  
            //updateView(location);  
            notifyListeners(location);
        }  
  
        /** 
         * GPS禁用时触发 
         */  
        public void onProviderDisabled(String provider) {  
            //updateView(null);  
            notifyListeners(null);
        }  
  
    }; 
    
 // 状态监听  
    GpsStatus.Listener mGpsStatListener = new GpsStatus.Listener() {  
        public void onGpsStatusChanged(int event) {  
            switch (event) {  
            // 第一次定位  
            case GpsStatus.GPS_EVENT_FIRST_FIX:  
                Log.i(TAG, "第一次定位");   
                //showToast("第一次定位"); 
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
                //showToast("定位启动"); 
                break;  
            // 定位结束  
            case GpsStatus.GPS_EVENT_STOPPED:  
                Log.i(TAG, "定位结束");  
                //showToast("定位结束"); 
                break;  
            }  
        };  
    };  
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mLm.removeUpdates(mLocationListener); 
	}
}
