package cn.lpap.speedrecorder;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utils {
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
}
