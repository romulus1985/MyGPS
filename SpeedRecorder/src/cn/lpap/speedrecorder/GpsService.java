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
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sStarted = true;
		
		mBinder = new MyBinder();
		
		mDb = new GpsDatabaseHelper(this);
		
		mLm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLm.addGpsStatusListener(mGpsStatListener);
        
        // �󶨼�����4������  
        // ����1���豸����GPS_PROVIDER��NETWORK_PROVIDER����  
        // ����2��λ����Ϣ�������ڣ���λ����  
        // ����3��λ�ñ仯��С���룺��λ�þ���仯�����ֵʱ��������λ����Ϣ  
        // ����4������  
        // ��ע������2��3��������3��Ϊ0�����Բ���3Ϊ׼������3Ϊ0����ͨ��ʱ������ʱ���£�����Ϊ0������ʱˢ��  
  
        // 1�����һ�Σ�����Сλ�Ʊ仯����1�׸���һ�Σ�  
        // ע�⣺�˴�����׼ȷ�ȷǳ��ͣ��Ƽ���service��������һ��Thread����run��sleep(10000);Ȼ��ִ��handler.sendMessage(),����λ��  
        mLm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 1, mLocationListener);
        //lm.requestLocationUpdates(bestProvider, 1000, 1, locationListener);
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
				 "�뱣�ֳ����ں�̨����",
				 pendingintent);
		 startForeground(0x111, notification);
		 return super.onStartCommand(intent, flags, startId);
	}
	
	public void reqLastKnownLocation(){
		LogUtil.log("reqLastKnownLocation enter.");
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


	
	// λ�ü���  
    private LocationListener mLocationListener = new LocationListener() {  
  
        /** 
         * λ����Ϣ�仯ʱ���� 
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
                Log.i(TAG, "��ǰGPS״̬Ϊ�ɼ�״̬"); 
                //showToast("��ǰGPS״̬Ϊ�ɼ�״̬");
                break;  
            // GPS״̬Ϊ��������ʱ  
            case LocationProvider.OUT_OF_SERVICE:  
                Log.i(TAG, "��ǰGPS״̬Ϊ��������״̬"); 
                //showToast("��ǰGPS״̬Ϊ��������״̬"); 
                break;  
            // GPS״̬Ϊ��ͣ����ʱ  
            case LocationProvider.TEMPORARILY_UNAVAILABLE:  
                Log.i(TAG, "��ǰGPS״̬Ϊ��ͣ����״̬");   
                //showToast("��ǰGPS״̬Ϊ��ͣ����״̬"); 
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
                Log.i(TAG, "��һ�ζ�λ");   
                //showToast("��һ�ζ�λ"); 
                break;  
            // ����״̬�ı�  
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Log.i(TAG, "����״̬�ı�");  
                //showToast("����״̬�ı�"); 
                // ��ȡ��ǰ״̬  
                GpsStatus gpsStatus = mLm.getGpsStatus(null);  
                // ��ȡ���ǿ����Ĭ�����ֵ  
                int maxSatellites = gpsStatus.getMaxSatellites();  
                // ����һ�������������������  
                Iterator<GpsSatellite> iters = gpsStatus.getSatellites()  
                        .iterator();  
                int count = 0;  
                while (iters.hasNext() && count <= maxSatellites) {  
                    GpsSatellite s = iters.next();  
                    count++;  
                }  
                System.out.println("��������" + count + "������");   
                //showToast("��������" + count + "������"); 
                break;  
            // ��λ����  
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(TAG, "��λ����");  
                //showToast("��λ����"); 
                break;  
            // ��λ����  
            case GpsStatus.GPS_EVENT_STOPPED:  
                Log.i(TAG, "��λ����");  
                //showToast("��λ����"); 
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
	}
}
