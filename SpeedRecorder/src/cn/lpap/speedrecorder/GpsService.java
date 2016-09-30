package cn.lpap.speedrecorder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Notification;
import android.app.PendingIntent;
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
import android.os.PowerManager;
import android.util.Log;

public class GpsService extends Service {
	private static final String TAG = "GpsService";
	
	public interface GpsServiceListener {
		public void onLocationChanged(Location location);
		public void resetSpeed(final float oldSpeed);
	}
	
	private static boolean sStarted = false;
	
	public static boolean issStarted() {
		return sStarted;
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
	PowerManager.WakeLock mWl = null;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		LogUtil.logTimestamp(TAG, "onCreate enter. thread name = " + Thread.currentThread().getName());
		
		sStarted = true;
		
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getPackageName());
		/*
		 *  On redmi, do not acqurie wake lock do not affact gps work when screen off for a little while.
		 *  Case of long time screen off, do not test.
		 *  So ensure gps work when screen off, acqurie wake lock.
		 *  TODO: test whether gps work or not, when long time screen off
		 *  PS: When screen off, power save mode must turn off, otherwise Gps certain not work. 
		 */
		mWl.acquire();
		
		mBinder = new MyBinder();
		
		mDb = new GpsDatabaseHelper(this);
		
		mLm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean added = mLm.addGpsStatusListener(mGpsStatListener);
        LogUtil.logTimestamp(TAG, "added = " + added);
        
        //reqLocationUpdates();
        //lm.requestLocationUpdates(bestProvider, 1000, 1, locationListener);
	}
	
	boolean reqLocationUpdates = false;
	private void reqLocationUpdates() {
		mLm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 1, mLocationListener);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		 Notification notification = new Notification(R.drawable.ic_launcher,
				 getString(R.string.app_name), System.currentTimeMillis());
		
		 PendingIntent pendingintent = PendingIntent.getActivity(this, 
				 0,
				 new Intent(this, MainActivity.class), 
				 0);
		 notification.setLatestEventInfo(this, 
				 "GpsService", 
				 "Speed recorder contextText",
				 pendingintent);
		 startForeground(0x111, notification);
		 return super.onStartCommand(intent, flags, startId);
	}
	
	public void reqLastKnownLocation(){
		LogUtil.logTimestamp(TAG, "reqLastKnownLocation enter.");
		final String bestProvider = mLm.getBestProvider(getCriteria(), true);  
        Location location = mLm.getLastKnownLocation(bestProvider); 
        //notifyListeners(location);
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
     * ���ز�ѯ���� 
     *  
     * @return 
     */  
    private Criteria getCriteria() {  
        Criteria criteria = new Criteria();  
        // ���ö�λ��ȷ�� Criteria.ACCURACY_COARSE�Ƚϴ��ԣ�Criteria.ACCURACY_FINE��ȽϾ�ϸ  
        criteria.setAccuracy(Criteria.ACCURACY_FINE);  
        // �����Ƿ�Ҫ���ٶ�  
        //criteria.setSpeedRequired(false);
        criteria.setSpeedRequired(true);  
        // �����Ƿ�������Ӫ���շ�  
        criteria.setCostAllowed(false);  
        // �����Ƿ���Ҫ��λ��Ϣ  
        //criteria.setBearingRequired(false);  
        criteria.setBearingRequired(true);  
        // �����Ƿ���Ҫ������Ϣ  
        criteria.setAltitudeRequired(false);  
        // ���öԵ�Դ������  
        criteria.setPowerRequirement(Criteria.POWER_LOW);  
        return criteria;  
    }  
	
	public class MyBinder extends Binder {
		public GpsService getService() {
			return GpsService.this;
		}
	}

	boolean mFirstLocation = false;
	
	// λ�ü���  
    private LocationListener mLocationListener = new LocationListener() {  
  
        /** 
         * λ����Ϣ�仯ʱ���� 
         */  
        public void onLocationChanged(Location location) {  
        	
        	if(!mFirstLocation) {
        		mFirstLocation = true;
        		LogUtil.logTimestamp(TAG, "onLocationChanged first.");
        	}
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
            mDb.insert(cal.getTimeInMillis(), 
            		dateDesc,
            		location.getSpeed(), 
            		String.valueOf(speedManual),
            		String.valueOf(location.getLongitude()),
            		String.valueOf(location.getLatitude()));
            Log.i(TAG, "ʱ�䣺" + location.getTime());  
            Log.i(TAG, "���ȣ�" + location.getLongitude());  
            Log.i(TAG, "γ�ȣ�" + location.getLatitude());  
            Log.i(TAG, "���Σ�" + location.getAltitude());  
        }  
  
        /** 
         * GPS״̬�仯ʱ���� 
         */  
        public void onStatusChanged(String provider, int status, Bundle extras) {  
            switch (status) {  
            // GPS״̬Ϊ�ɼ�ʱ  
            case LocationProvider.AVAILABLE:
            	LogUtil.log("LocationProvider AVAILABLE");
                break;  
            // GPS״̬Ϊ��������ʱ  
            case LocationProvider.OUT_OF_SERVICE:
            	LogUtil.log("LocationProvider  OUT_OF_SERVICE");
                break;  
            // GPS״̬Ϊ��ͣ����ʱ  
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
            	LogUtil.log("LocationProvider  TEMPORARILY_UNAVAILABLE");
                break;  
            }  
        }  
  
        /** 
         * GPS����ʱ���� 
         */  
        public void onProviderEnabled(String provider) {  
        	LogUtil.log("onProviderEnabled: provider = " + provider);
            Location location = mLm.getLastKnownLocation(provider);  
            //updateView(location);  
            notifyListeners(location);
        }  
  
        /** 
         * GPS����ʱ���� 
         */  
        public void onProviderDisabled(String provider) {  
        	LogUtil.log("onProviderDisabled: provider = " + provider);
            //updateView(null);  
            notifyListeners(null);
        }  
  
    }; 
    
 // ״̬����  
    GpsStatus.Listener mGpsStatListener = new GpsStatus.Listener() {  
        public void onGpsStatusChanged(int event) {  
            switch (event) {  
            // ��һ�ζ�λ  
            case GpsStatus.GPS_EVENT_FIRST_FIX:  
                LogUtil.logTimestamp(TAG, "gps first fix");
                break;  
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
            	// if not requestLocationUpdates, will not receive GPS_EVENT_SATELLITE_STATUS
                //LogUtil.logTimestamp(TAG, "gps satellite status.");
                GpsStatus gpsStatus = mLm.getGpsStatus(null);
                int maxSatellites = gpsStatus.getMaxSatellites();
                Iterator<GpsSatellite> iters = gpsStatus.getSatellites()  
                        .iterator();  
                int count = 0;  
                int used = 0;
                while (iters.hasNext() && count <= maxSatellites) {  
                    GpsSatellite s = iters.next();
                    if(s.usedInFix()) {
                    	used++;
                    }
                    count++;  
                }
                if(used > 0) {
                	LogUtil.log(TAG, "get " + count + " satellites" + ", used = " + used 
                			+ ", TimeToFirstFix = " + gpsStatus.getTimeToFirstFix());
                	if(!reqLocationUpdates) {
                		/*
                		 *  TODO: if not requestLocationUpdates, will not receive GPS_EVENT_SATELLITE_STATUS
                		 *  change sequence to invoke following sentences.
                		 */
                		LogUtil.log("reqLocationUpdates, thread name = " + Thread.currentThread().getName());
                		reqLocationUpdates = true;
                		reqLocationUpdates();
                	}
                }
                break;  
            // ��λ����  
            case GpsStatus.GPS_EVENT_STARTED:
            	LogUtil.logTimestamp(TAG, "gps started");
                break;  
            // ��λ����  
            case GpsStatus.GPS_EVENT_STOPPED:
            	LogUtil.logTimestamp(TAG, "gps stopped");
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
		stopForeground(true);
		mWl.release();
	}
}
