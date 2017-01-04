package cn.lpap.speedrecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.lpap.speedrecorder.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AppInfo extends BaseActivity {
	private static final String TAG = "AppInfo";
    private Button mQuerySpeeds = null;
    private Button mQuerySpeedsHour = null;
    private Button mSelectTime = null;
    private Button mExportDB = null;
    private Button mExit = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LogUtil.log(true, TAG, "onCreate enter.");
		setContentView(R.layout.app_info);
		
		mQuerySpeeds = (Button)findViewById(R.id.query_speeds);
		mQuerySpeeds.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startQuery(Utils.DAY_IN_MILLI_SECOND);
			}
		});
		mQuerySpeedsHour = (Button)findViewById(R.id.query_speeds_hour);
		mQuerySpeedsHour.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startQuery(Utils.HOUR_IN_MILLI_SECOND);
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
		mExportDB = (Button)findViewById(R.id.export_db);
		mExportDB.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final String oldPath = "/data/data/" + getPackageName() + "/databases/" + GpsDatabaseHelper.DB_NAME;
				final String newPath = Utils.getExportFolder()
						+ File.separator + GpsDatabaseHelper.DB_NAME + Utils.getUniqueName() + ".db";
				copyFile(oldPath, newPath);
			}
		});
		mExit = (Button)findViewById(R.id.exit);
		mExit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopService(new Intent(AppInfo.this, GpsService.class));
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
	}

	private void startQuery(final long interval) {
		Intent positionsList = new Intent(AppInfo.this, SpeedsList.class);
    	final long cur = System.currentTimeMillis() + 1;
    	final long start = cur - interval;
		positionsList.putExtra(SpeedsList.KEY_START_TIME, start);
		positionsList.putExtra(SpeedsList.KEY_END_TIME, cur);
		startActivity(positionsList);
	}
	
	private void copyFile(final String oldPath,
			final String newPath) {
		int byteSum = 0;
		int byteRead = 0;
		InputStream input = null;
		OutputStream output = null;
		try{
			File oldFile = new File(oldPath);
			File newFile = new File(newPath);
			if(newFile.exists()) {
				newFile.delete();
			} 
			
			newFile.createNewFile();
			if(!oldFile.exists()) {
				return;
			}
			input = new FileInputStream(oldFile);
			output = new FileOutputStream(newFile);
			byte[] buffer = new byte[1024];
			while(-1 != (byteRead = input.read(buffer))) {
				byteSum += byteRead;
				output.write(buffer, 0, byteRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(null != input) {
				input.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Toast.makeText(this, R.string.export_db_ok, Toast.LENGTH_SHORT).show();
	}
}
