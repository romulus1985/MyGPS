package cn.lpap.speedrecorder;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GpsDatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "Gps"; //数据库名称  
    
    private static final int version = 1; //数据库版本  
    private static final String TABLE_NAME = "position";
    private static final String POS_TIME = "time";
    private static final String POS_TIME_DESC = "timeDesc";
    private static final String POS_SPEED = "speed";
    private static final String POS_SPEED_DESC = "speedDesc";
    private static final String POS_LONG = "longitude";
    private static final String POS_LAN = "latitude";

	public GpsDatabaseHelper(Context context) {  
		  
        //第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类,设置为null,代表使用系统默认的工厂类  

          super(context, DB_NAME, null, version);  

   }  

	  
    @Override  
    public void onCreate(SQLiteDatabase db) {  

          db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + 
        		  " (positionid integer primary key autoincrement, "
        		  + POS_TIME + " INT, "
        		  + POS_TIME_DESC + " varchar(30), "
        		  + POS_SPEED + " varchar(20), "
        		  + POS_SPEED_DESC + " varchar(20), "
        		  + POS_LONG+ " varchar(20), " 
        		  + POS_LAN + " varchar(20))");     

     }  

    @Override   
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

     }
    
    public class Position {
    	long time;
    	String speed;
    }
    public List<Position> queryLastest(final int maxCount) {
    	Cursor c = null; 
    	List<Position> positions = new ArrayList<GpsDatabaseHelper.Position>();
    	try {
        	c = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
	        if(0 < c.getCount()) {
	        	c.moveToLast();
	        	final int timeIndex = c.getColumnIndex(POS_TIME);
	        	final int speedIndex = c.getColumnIndex(POS_SPEED);
	        	int i = 0;
	        	do{
	        		Position pos = new Position();
	        		pos.time = c.getLong(timeIndex);
	        		pos.speed = c.getString(speedIndex);
	        		positions.add(pos);
	        		c.moveToPrevious();
	        		i++;
	        	} while(!c.isBeforeFirst()
	        			&& i < maxCount);
        	}
		} finally {
			if(null != c) {
				c.close();
			}
		}
    	return positions;
    }
    
    public List<Position> query(final long startTime,
    		final long endTime) {
    	Cursor c = null; 
    	List<Position> positions = new ArrayList<GpsDatabaseHelper.Position>();
    	try {
	        c = getReadableDatabase().query(TABLE_NAME, 
	        		null, 
	        		POS_TIME + " > ? and " + POS_TIME + " < ?", 
	        		new String[] {String.valueOf(startTime), String.valueOf(endTime)}, 
	        		null, 
	        		null, 
	        		null);
	        if(0 < c.getCount()) {
	        	c.moveToFirst();
	        	final int timeIndex = c.getColumnIndex(POS_TIME);
	        	final int speedIndex = c.getColumnIndex(POS_SPEED);
	        	while(!c.isAfterLast()){
	        		Position pos = new Position();
	        		pos.time = c.getLong(timeIndex);
	        		pos.speed = c.getString(speedIndex);
	        		positions.add(pos);
	        		c.moveToNext();
	        	}
        	}
		} finally {
			if(null != c) {
				c.close();
			}
		}
    	return positions;
    }
    
    public void insert(final long time, 
    		final String timeDesc,
    		final String speed, 
    		final String speedDesc,
    		final String longitude, 
    		final String latitude) {  
        ContentValues cv=new ContentValues();  
                      
        cv.put(POS_TIME, time);  
        cv.put(POS_TIME_DESC, timeDesc);
        cv.put(POS_SPEED, speed);  
        cv.put(POS_SPEED_DESC, speedDesc);
        cv.put(POS_LONG, longitude);  
        cv.put(POS_LAN, latitude);
          
        getWritableDatabase().insert(TABLE_NAME, null, cv);  
    } 
}
