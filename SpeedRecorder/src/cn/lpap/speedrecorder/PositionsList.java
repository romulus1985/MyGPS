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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class PositionsList extends BaseActivity {
	private ListView mSpeedList = null;
	
	private static final String KEY_TIME = "time";
	private static final String KEY_SPEED = "speed";
	private static final int DEFAULT_ROWS = 100;
	private GpsDatabaseHelper mDB = null;
	
    private ArrayList<HashMap<String, String>>   listItems;    //存放文字、图片信息
    private SimpleAdapter listItemAdapter;           //适配器    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speeds_list);
		
		mDB = new GpsDatabaseHelper(this);
		initListView();
	}
	
    private void initListView() {
    	mSpeedList = (ListView)findViewById(R.id.speed_list);
    	List<GpsDatabaseHelper.Position> postions = mDB.queryLastest(DEFAULT_ROWS);
    	GpsDatabaseHelper.Position pos = null;
    	final int size = postions.size();
        listItems = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < DEFAULT_ROWS; i++)    {   
            HashMap<String, String> map = new HashMap<String, String>();
            if(i < size) {
	            pos = postions.get(i);
	            /*map.put(KEY_TIME, getString(R.string.time_title) + getTime(pos.time));
	            map.put(KEY_SPEED, getString(R.string.speed_title) + getSpeed(pos.speed) + getString(R.string.speed_km_unit));*/
	            map.put(KEY_TIME, getTime(pos.time));
	            map.put(KEY_SPEED, getSpeed(pos.speed) + getString(R.string.speed_km_unit));
	            listItems.add(map);   
            }
        }   
        /*//生成适配器的Item和动态数组对应的元素   
        listItemAdapter = new SimpleAdapter(this,listItems,   // listItems数据源    
                R.layout.list_item,  //ListItem的XML布局实现  
                new String[] {KEY_TIME, KEY_SPEED},     //动态数组与ImageItem对应的子项         
                new int[ ] {R.id.time, R.id.speed}      //list_item.xml布局文件里面的一个ImageView的ID,一个TextView 的ID  
        );   */
        //setListAdapter(listItemAdapter);
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
			return listItems.size();
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
			
			HashMap<String, String> map = listItems.get(arg0);
			if(null != map) {
				final String time = map.get(KEY_TIME);
				wrapper.time.setText(time);
				final String speed = map.get(KEY_SPEED);
				wrapper.speed.setText(speed);
			}
			return arg1;
		}
    	
    }
    
    private String getTime(final String timeIn) {
    	String timeOut = "";
    	long time = 0;
    	try {
	    	time = Long.valueOf(timeIn);
	    	Date date = new Date(time);
	    	timeOut = date.toLocaleString();
    	} catch (NumberFormatException e) {
    		e.printStackTrace();
    	}
    	return timeOut;
    }
    
    private String getSpeed(final String speedIn) {
    	float speedOut = 0;
    	try {
	    	float speed = Float.valueOf(speedIn);
	    	speedOut = Utils.getSpeed(speed);
    	} catch (NumberFormatException e) {
    		e.printStackTrace();
    	}
    	return String.valueOf(speedOut);
    }
}
