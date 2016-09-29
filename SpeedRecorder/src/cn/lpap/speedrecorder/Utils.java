package cn.lpap.speedrecorder;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

public class Utils {
	private static final String EXPORT_FOLDER = "cn_lpap_speedrecorder";
	private static String sExportFolder = null;

	public static final long HOUR_IN_MILLI_SECOND = 60 * 60 * 1000;
	public static final long DAY_IN_MILLI_SECOND = 24 * HOUR_IN_MILLI_SECOND;
	
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
	
	public static String getUniqueHourName() {
        SimpleDateFormat sdf = new SimpleDateFormat("_yyyy_MM_dd_HH");
		final String dateDesc = sdf.format(new Date());
		return dateDesc;
	}
	
	public static String getExportFolder() {
		if(null == sExportFolder) {
			synchronized (Utils.class) {
				if(null == sExportFolder) {
					sExportFolder = Environment.getExternalStorageDirectory() 
							+ File.separator + Utils.EXPORT_FOLDER;
					File exportFile = new File(sExportFolder);
					if(!exportFile.exists()) {
						exportFile.mkdir();
					}
				}
			}
		}
		return sExportFolder;
	}
}
