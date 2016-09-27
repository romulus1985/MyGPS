package cn.lpap.speedrecorder;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

public class Utils {
	public static final String EXPORT_FOLDER = "cn_lpap_speedrecorder";
	/*
	 * m/s to km/s
	 */
	public static final float SPEED_TRANSFORM_RATE = 3.6f;
	
	public static float getSpeed(final float speedIn) {
		final float speedOrig = speedIn * Utils.SPEED_TRANSFORM_RATE;

		BigDecimal   b   =   new   BigDecimal(speedOrig);
		final float speed  =  b.setScale(2,   RoundingMode.HALF_UP).floatValue();
		return speed;
	}
	
	public static String getUniqueName() {
        SimpleDateFormat sdf = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
		final String dateDesc = sdf.format(new Date());
		return dateDesc;
	}
	
	public static String getExportFolder() {
		final String exportPath = Environment.getExternalStorageDirectory() 
				+ File.separator + Utils.EXPORT_FOLDER;
		File exportFolder = new File(exportPath);
		if(!exportFolder.exists()) {
			exportFolder.mkdir();
		}
		return exportPath;
	}
}
