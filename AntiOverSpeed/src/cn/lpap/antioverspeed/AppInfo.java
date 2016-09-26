package cn.lpap.antioverspeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AppInfo extends Activity {
    private Button mQueryPositions = null;
    private Button mSelectTime = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_info);
		
		mQueryPositions = (Button)findViewById(R.id.query_positions);
		mQueryPositions.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent positionsList = new Intent(AppInfo.this, PositionsList.class);
				startActivity(positionsList);
			}
		});
		
		mSelectTime = (Button)findViewById(R.id.select_time);
		mSelectTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent selectTime = new Intent(AppInfo.this, TimeSelect.class);
				startActivity(selectTime);
			}
		});
	}
}
