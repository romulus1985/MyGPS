package cn.lpap.speedrecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.lpap.speedrecorder.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AppInfo extends BaseActivity {
    private Button mQuerySpeeds = null;
    private Button mSelectTime = null;
    private Button mExportDB = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_info);
		
		mQuerySpeeds = (Button)findViewById(R.id.query_speeds);
		mQuerySpeeds.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent positionsList = new Intent(AppInfo.this, SpeedsList.class);
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
		mExportDB = (Button)findViewById(R.id.export_db);
		mExportDB.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final String oldPath = "/data/data/" + getPackageName() + "/databases/" + GpsDatabaseHelper.DB_NAME;
				final String newPath = Utils.getExportFolder()
						+ File.separator + GpsDatabaseHelper.DB_NAME + Utils.getUniqueName();
				copyFile(oldPath, newPath);
			}
		});
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
