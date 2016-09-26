package cn.lpap.antioverspeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SimpleAdapter;

public class PositionsList extends ListActivity {
	private static final String KEY_TIME = "time";
	private static final String KEY_SPEED = "speed";
	private static final int DEFAULT_ROWS = 100;
	private GpsDatabaseHelper mDB = null;
	
    private ArrayList<HashMap<String, Object>>   listItems;    //存放文字、图片信息
    private SimpleAdapter listItemAdapter;           //适配器    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mDB = new GpsDatabaseHelper(this);
		initListView();
	}
	
    private void initListView()   {   
    	List<GpsDatabaseHelper.Position> postions = mDB.queryLastest(DEFAULT_ROWS);
    	GpsDatabaseHelper.Position pos = null;
    	final int size = postions.size();
        listItems = new ArrayList<HashMap<String, Object>>();
        for(int i = 0; i < DEFAULT_ROWS; i++)    {   
            HashMap<String, Object> map = new HashMap<String, Object>();
            if(i < size) {
	            pos = postions.get(i);
	            map.put(KEY_TIME, getString(R.string.time_title) + getTime(pos.time));
	            map.put(KEY_SPEED, getString(R.string.speed_title) + getSpeed(pos.speed) + getString(R.string.speed_km_unit));  
	            listItems.add(map);   
            }
        }   
        //生成适配器的Item和动态数组对应的元素   
        listItemAdapter = new SimpleAdapter(this,listItems,   // listItems数据源    
                R.layout.list_item,  //ListItem的XML布局实现  
                new String[] {KEY_TIME, KEY_SPEED},     //动态数组与ImageItem对应的子项         
                new int[ ] {R.id.time, R.id.speed}      //list_item.xml布局文件里面的一个ImageView的ID,一个TextView 的ID  
        );   
        setListAdapter(listItemAdapter);  
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
