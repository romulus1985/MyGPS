package cn.lpap.speedrecorder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.lpap.speedrecorder.R;

import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SpeedsList extends BaseActivity {
	private ListView mSpeedList = null;
	public static final String KEY_START_TIME = "key_start_time";
	public static final String KEY_END_TIME = "key_end_time";
	private Button mSpeedBtn = null;
	private Button mTimeBtn = null;
	private long mStartTime = 0;
	private long mEndTime = 0;
	
	private static final String KEY_TIME = "time";
	private static final String KEY_SPEED = "speed";
	private GpsDatabaseHelper mDB = null;
	List<GpsDatabaseHelper.Position> mPostions = null;
	// asc/desc
	// smaller to bigger, asc; otherwise desc
	private boolean mTimeAsc = false;
	private boolean mSpeedAsc = true;
	private String mOrderBy = null; //GpsDatabaseHelper.POS_TIME + " asc, " + GpsDatabaseHelper.POS_SPEED + " asc";
	{
		mOrderBy = getTimeOrder()
				+ ", " 
				+ getSpeedOrder();
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speeds_list);
		
		mDB = new GpsDatabaseHelper(this);
		initViews();
		initListView();
	}
	
	private void initViews() {
		mTimeBtn = (Button)findViewById(R.id.time);
		mTimeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mTimeAsc = !mTimeAsc;
				mOrderBy = getTimeOrder()
						+ ", " 
						+ getSpeedOrder();
				refreshList();
			}
		});
		mSpeedBtn = (Button)findViewById(R.id.speed);
		mSpeedBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mSpeedAsc = !mSpeedAsc;
				mOrderBy = getSpeedOrder()
						+ ", " 
						+ getTimeOrder();
				refreshList();
			}
		});
	}
	
	private String getTimeOrder() {
		return GpsDatabaseHelper.POS_TIME 
				+ " " + (mTimeAsc ? "asc" : "desc");
	}
	
	private String getSpeedOrder() {
		return GpsDatabaseHelper.POS_SPEED 
				+ " " + (mSpeedAsc ? "asc" : "desc");
	}
	
    private void initListView() {
    	mSpeedList = (ListView)findViewById(R.id.speed_list);
    	mStartTime = getIntent().getLongExtra(KEY_START_TIME, -1);
    	mEndTime = getIntent().getLongExtra(KEY_END_TIME, -1);
    	refreshList();
    }

	private void refreshList() {
		mPostions = mDB.query(mStartTime, mEndTime, mOrderBy);
        mSpeedList.setAdapter(new SpeedAdapter());
	}
    
    class ItemWrapper {
    	TextView time;
    	TextView speed;
    }
    
    class SpeedAdapter extends BaseAdapter {
    	private LayoutInflater mInflater = null;
    	public SpeedAdapter() {
    		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}
    	
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mPostions.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ItemWrapper wrapper = null;
			if(null == arg1) {
				arg1 = mInflater.inflate(R.layout.speed_list_item, null, false);
				wrapper = new ItemWrapper();
				wrapper.time = (TextView)arg1.findViewById(R.id.time);
				wrapper.speed = (TextView)arg1.findViewById(R.id.speed);
				arg1.setTag(wrapper);
			} else {
				wrapper = (ItemWrapper) arg1.getTag();
			}
			
			GpsDatabaseHelper.Position pos = mPostions.get(arg0);
			if(null != pos) {
				wrapper.time.setText((arg0 + 1) + ".  " + getTime(pos.time));
				wrapper.speed.setText(getSpeed(pos.speed) + getString(R.string.speed_km_unit));
			}
			return arg1;
		}
    	
    }
    
    private String getTime(final long timeIn) {
    	String timeOut = "";
    	try {
	    	Date date = new Date(timeIn);
	    	timeOut = date.toLocaleString();
    	} catch (NumberFormatException e) {
    		e.printStackTrace();
    	}
    	return timeOut;
    }
    
    private String getSpeed(final float speedIn) {
    	float speedOut = 0;
    	try {
	    	speedOut = Utils.getSpeed(speedIn);
    	} catch (NumberFormatException e) {
    		e.printStackTrace();
    	}
    	return String.valueOf(speedOut);
    }
}
