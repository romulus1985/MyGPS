package cn.lpap.antioverspeed;

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class TimeSelect extends Activity {
    private EditText dateEt = null;
    private EditText timeEt = null;
    private EditText endTime = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_select);
        dateEt=(EditText)findViewById(R.id.dateEt);
        timeEt=(EditText)findViewById(R.id.timeEt);
        endTime = (EditText)findViewById(R.id.endTimeEt);
        
        DatePicker datePicker=(DatePicker)findViewById(R.id.datePicker);
        TimePicker timePicker=(TimePicker)findViewById(R.id.timePicker);
        TimePicker endTimePicker=(TimePicker)findViewById(R.id.endTimePicker);
        
        Calendar calendar = Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int monthOfYear=calendar.get(Calendar.MONTH);
        int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
        datePicker.init(year, monthOfYear, dayOfMonth, new OnDateChangedListener(){

            public void onDateChanged(DatePicker view, int year,
                    int monthOfYear, int dayOfMonth) {
                dateEt.setText("您选择的日期是："+year+"年"+(monthOfYear+1)+"月"+dayOfMonth+"日。");
            }
            
        });
        
        timePicker.setOnTimeChangedListener(new OnTimeChangedListener(){

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeEt.setText("您选择的开始时间是："+hourOfDay+"时"+minute+"分。");
            }
            
        });
        
        endTimePicker.setOnTimeChangedListener(new OnTimeChangedListener(){

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            	endTime.setText("您选择的结束时间是："+hourOfDay+"时"+minute+"分。");
            }
            
        });
    }
}
