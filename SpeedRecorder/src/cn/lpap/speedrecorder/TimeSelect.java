package cn.lpap.speedrecorder;

import java.util.Calendar;
import java.util.Date;

import cn.lpap.speedrecorder.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class TimeSelect extends BaseActivity {
    private EditText dateEt = null;
    private EditText timeEt = null;
    private EditText endTimeEt = null;
    private Button mQuery = null;
    
    private int mYear = 0;
    private int month = 0;
    private int day = 0;
    private int startHour = 0;
    private int startMinute = 0;
    private int endHour = 0;
    private int endMinute = 0;
    
    private long startTime = 0;
    private long endTime = 0;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_select);
        //dateEt=(EditText)findViewById(R.id.dateEt);
        //timeEt=(EditText)findViewById(R.id.timeEt);
        //endTimeEt = (EditText)findViewById(R.id.endTimeEt);
        
        DatePicker datePicker=(DatePicker)findViewById(R.id.datePicker);
        TimePicker timePicker=(TimePicker)findViewById(R.id.timePicker);
        TimePicker endTimePicker=(TimePicker)findViewById(R.id.endTimePicker);
        mQuery = (Button)findViewById(R.id.query);
        mQuery.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent query = new Intent(TimeSelect.this, SpeedsList.class);
				query.putExtra(SpeedsList.KEY_START_TIME, startTime);
				query.putExtra(SpeedsList.KEY_END_TIME, endTime);
				
				/*Date date = new Date(startTime);
				showToast("start = " + date);
				date = new Date(endTime);
				showToast("end = " + date);*/
				
				startActivity(query);
			}
		});
        
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        calendar.set(mYear, month, day, hour, minute, 0);
        startHour = hour;
        startMinute = minute;
        startTime = calendar.getTimeInMillis();
        //Date date = new Date(startTime);
        //Toast.makeText(this, "start = " + date.toLocaleString(), Toast.LENGTH_LONG).show();
        
        calendar.set(mYear, month, day, hour, minute + 1, 0);
        endHour = hour;
        endMinute = minute + 1;
        endTime = calendar.getTimeInMillis();
        //date = new Date(endTime);
        //Toast.makeText(this, "end = " + date.toLocaleString(), Toast.LENGTH_LONG).show();
        
        datePicker.init(mYear, month, day, new OnDateChangedListener(){

            public void onDateChanged(DatePicker view, int year,
                    int monthOfYear, int dayOfMonth) {
                //dateEt.setText("您选择的日期是："+year+"年"+(monthOfYear+1)+"月"+dayOfMonth+"日。");
            	mYear = year;
                month = monthOfYear;
                day = dayOfMonth;
                calculateTime();
            }
            
        });
        
        timePicker.setOnTimeChangedListener(new OnTimeChangedListener(){

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                //timeEt.setText("您选择的开始时间是："+hourOfDay+"时"+minute+"分。");
            	startHour = hourOfDay;
            	startMinute = minute;
                calculateTime();
            }
            
        });
        
        endTimePicker.setOnTimeChangedListener(new OnTimeChangedListener(){

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            	//endTimeEt.setText("您选择的结束时间是："+hourOfDay+"时"+minute+"分。");
            	endHour = hourOfDay;
            	endMinute = minute;
                calculateTime();
            }
            
        });
    }
    
    private void calculateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, month, day, startHour, startMinute, 0);
        startTime = calendar.getTimeInMillis();

        calendar.set(mYear, month, day, endHour, endMinute, 0);
        endTime = calendar.getTimeInMillis();
    }
    
	private void showToast(final String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
}
