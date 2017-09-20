package com.mica;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class StepDetector implements SensorEventListener {
	private final static String TAG = "StepDetector";
	//private float mLimit = (float) 2.96;
	private float mLastValues[] = new float[3 * 2];
	private float mScale[] = new float[2];
	private float mYOffset;

	private float mLastDirections[] = new float[3 * 2];
	private float mLastExtremes[][] = { new float[3 * 2], new float[3 * 2] };
	private float mLastDiff[] = new float[3 * 2];
	private int mLastMatch = -1;
	private int stepCount;
	private int azimut, pitch, roll;
	private int number;
	
	public static float stepSensivity;

	public StepDetector() {
		int h = 480; // TODO: remove this constant
		mYOffset = h * 0.5f;
		mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
		mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
		stepCount = 0;
		number = 0;
	}

	/*public void setSensitivity(float sensitivity) {
		mLimit = stepSensivity; // 1.97 2.96 4.44 6.66 10.00 15.00 22.50 33.75
									// 50.62
		// 22.5
	}*/

	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		   switch(event.sensor.getType()) {
		    case Sensor.TYPE_ACCELEROMETER:
		        // copy new accelerometer data into accel array
		        // then calculate new orientation
				PersPosService.service.writeLogAccelerometer(event.values[0], event.values[1], event.values[2]);
		        break;
		 
		    case Sensor.TYPE_ORIENTATION:
		        // process gyro data
				PersPosService.service.writeLogGyroscope(event.values[0], event.values[1], event.values[2]);
		        break;
		 
		    case Sensor.TYPE_MAGNETIC_FIELD:
		        // copy new magnetometer data into magnet array
		    	PersPosService.service.writeLogMagnetic(event.values[0], event.values[1], event.values[2]);
		        break;
		    }		
		//PersPosService.service.writeLogGyroscope(azimut, pitch, roll);
		//PersPosService.service.writeLogAccelerometer(event.values[0], event.values[1], event.values[2]);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
}
