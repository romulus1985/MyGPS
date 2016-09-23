package cn.lpap.antioverspeed;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GpsDatabaseHelper extends SQLiteOpenHelper {
    private static final String name = "Gps"; //数据库名称  
    
    private static final int version = 1; //数据库版本  
    private static final String TABLE_NAME = "position";
    private static final String POS_TIME = "time";
    private static final String POS_SPEED = "speed";

	public GpsDatabaseHelper(Context context) {  
		  
        //第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类,设置为null,代表使用系统默认的工厂类  

          super(context, name, null, version);  

   }  

	  
    @Override  
    public void onCreate(SQLiteDatabase db) {  

          db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + 
        		  " (positionid integer primary key autoincrement, "
        		  + POS_TIME + " varchar(20), "
        		  + POS_SPEED + " varchar(20), longitude varchar(20), latitude varchar(20))");     

     }  

    @Override   
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

     }
    
    public class Position {
    	String time;
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
	        		pos.time = c.getString(timeIndex);
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
    
    public void insert(final String time, 
    		final String speed, 
    		final String longitude, 
    		final String latitude) {  
        ContentValues cv=new ContentValues();  
                      
        cv.put("time", time);  
        cv.put("speed", speed);  
        cv.put("longitude", longitude);  
        cv.put("latitude", latitude);
          
        getWritableDatabase().insert(TABLE_NAME, null, cv);  
    } 
}
