package cn.lpap.antioverspeed;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class TimeSelect extends Activity {
    private EditText dateEt = null;
    private EditText timeEt = null;
    private EditText endTimeEt = null;
    
    private int year = 0;
    private int month = 0;
    private int day = 0;
    
    private long startTime = 0;
    private long endTime = 0;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_select);
        dateEt=(EditText)findViewById(R.id.dateEt);
        timeEt=(EditText)findViewById(R.id.timeEt);
        endTimeEt = (EditText)findViewById(R.id.endTimeEt);
        
        DatePicker datePicker=(DatePicker)findViewById(R.id.datePicker);
        TimePicker timePicker=(TimePicker)findViewById(R.id.timePicker);
        TimePicker endTimePicker=(TimePicker)findViewById(R.id.endTimePicker);
        
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(year, month, day, hour, minute, 0);
        startTime = calendar2.getTimeInMillis();
        
        calendar2.set(year, month, day, hour, minute + 1, 0);
        endTime = calendar2.getTimeInMillis();
               
        //Date date = new Date(startTime);
        Date date = new Date(endTime);
        Toast.makeText(this, date.toLocaleString(), Toast.LENGTH_LONG).show();
        
        datePicker.init(year, month, day, new OnDateChangedListener(){

            public void onDateChanged(DatePicker view, int year,
                    int monthOfYear, int dayOfMonth) {
                dateEt.setText("您选择的日期是："+year+"年"+(monthOfYear+1)+"月"+dayOfMonth+"日。");
                TimeSelect.this.year = year;
                month = monthOfYear;
                day = dayOfMonth;
            }
            
        });
        
        timePicker.setOnTimeChangedListener(new OnTimeChangedListener(){

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeEt.setText("您选择的开始时间是："+hourOfDay+"时"+minute+"分。");
            }
            
        });
        
        endTimePicker.setOnTimeChangedListener(new OnTimeChangedListener(){

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            	endTimeEt.setText("您选择的结束时间是："+hourOfDay+"时"+minute+"分。");
            }
            
        });
    }
}
