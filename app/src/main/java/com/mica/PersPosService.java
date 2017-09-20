package com.mica;

import java.util.*;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PersPosService extends Service implements SensorEventListener{
	public class GpsScanInfo {
		public double longitude;
		public double latitude;
		public double altitude;
		public double precision;
		public int numSat;
	}

	public class WifiScanInfo {
		public String mac;
		public String ssid;
		public double sigstr;
	}
	
	public class BluetoothScanInfo {
		public String name;
		public String mac;
		public int sigstr;
	}

	private static final String TAG = "PersPos";
	
	private static final long GPS_MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long GPS_MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
	private static final int NOTIFICATION_ID = 1;

	public static PersPosService service = null;

	SensorManager mSensorManager;
	Sensor mSensorAccelerometer, mSensorOrientation, mSensorMagnetic;

	boolean wifiScanning = false;
	WifiManager wifiMan;
	BroadcastReceiver wifiReceiver, bluetoothReceiver;
	BluetoothAdapter bluetoothAdapter;
	LocationManager locationMan;
	LocationListener locationListener;

	static boolean wifiChk = false, sensorsChk = false, bluetoothChk = false;
	static boolean started = false;

	Runnable timerScanTask;
	Handler scanTimerHandler = new Handler();
	public static int timeScan = 10;
	public int bluetooth_counting;
	
	private Runnable elapsedTimer;
	private long startTime;
	private long elapsedTime;
	private final int REFRESH_RATE = 100;
	Handler elapsedHandler = new Handler();
	
	int countWifi, countBt;
	
	public PersPosService() {
		service = this;
	}
	
	@Override
	public void onCreate() {
		wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifiMan == null)
			PersPosUtils.showWarning("No Wifi manager found");
		else {
			wifiReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context c, Intent intent) {
					List<ScanResult> results = service.wifiMan.getScanResults();
					if (results.size()==0) return;
					
					List<WifiScanInfo> wifiInf = new ArrayList<WifiScanInfo>();
					for (ScanResult result : results) {
						WifiScanInfo inf = new WifiScanInfo();
						inf.mac = result.BSSID;
						inf.ssid = result.SSID;
						inf.sigstr = result.level;
						wifiInf.add(inf);
					}

					sendWifiInfo(wifiInf);
					wifiScanning = false;
				}
			};
		}
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null)
			PersPosUtils.showWarning("No bluetooth adapter");
		else {
			bluetoothReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context c, Intent intent) {
					String action = intent.getAction();
					if (BluetoothDevice.ACTION_FOUND.equals(action)) {
						BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						
						BluetoothScanInfo inf = new BluetoothScanInfo();
						inf.mac = device.getAddress();
						inf.name = device.getName();
						inf.sigstr = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
						sendBluetoothInfo(inf);
					}
				}
			};
		}

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		timerScanTask = new Runnable() {
			public void run() {
				sendAndScan();
				scanTimerHandler.postDelayed(this, timeScan * 1000);
			}
		};
		
		startTime = System.currentTimeMillis();
		elapsedTimer = new Runnable() {
			
			public void run() {
				elapsedTime = System.currentTimeMillis() - startTime;
				PersPosUtils.updateTime(elapsedTime);
				elapsedHandler.postDelayed(this, REFRESH_RATE);
			}
		};
		elapsedHandler.removeCallbacks(elapsedTimer);
		elapsedHandler.postDelayed(elapsedTimer, 0);
		
		countWifi = 0;
		countBt = 0;
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (started) return 0;
		started = true;
		bluetooth_counting = 0;
		
		PersPosUtils.generate_file_name();
		
		if (wifiMan != null && !wifiMan.isWifiEnabled()) PersPosUtils.showLog("Wifi is disabled");
		if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) PersPosUtils.showLog("Bluetooth is disabled");
		
		if (wifiChk) startWifi();
		if (bluetoothChk) startBluetooth();
		if (sensorsChk) startReadingInertialSensors();

		scanTimerHandler.postDelayed(timerScanTask, 0);

		PersPosUtils.showLog("Service started");
		PersPosUtils.printStartTime();

		// Notification
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.icon)
		        .setContentTitle("PersPos service is running!")
		        .setOngoing(true);
		
		Intent notificationIntent = new Intent(PersPos.app, PersPos.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(PersPos.app, 0, notificationIntent, 0);
		mBuilder.setContentIntent(pendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		return 0;
	}

	@Override
	public void onDestroy() {
		if (!started) return;
		
		scanTimerHandler.removeCallbacks(timerScanTask);
		
		if (wifiChk) stopWifi();
		if (bluetoothChk) stopBluetooth();
		if (sensorsChk) stopReadingInertialSensors();

		started = false;
		//PersPosUtils.closeFile();
		PersPosUtils.showLog("Stop recording data");
		PersPosUtils.showLog("Service stopped");
		
		elapsedHandler.removeCallbacks(elapsedTimer);

		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}

	public void startWifi() {
		wifiChk = true;
		if (!started) return;
		
		if (wifiMan != null) {
			if (!wifiMan.isWifiEnabled()) {
				wifiMan.setWifiEnabled(true);
				PersPosUtils.showLog("Enable Wifi, due to user's option");
			}
			registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		}
	}

	public void stopWifi() {
		wifiChk = false;
		if (!started) return;

		if (wifiMan != null) {
			try {
				unregisterReceiver(wifiReceiver);
			} catch(IllegalArgumentException e) {				
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void startReadingInertialSensors() {
		sensorsChk = true;
		// SENSOR_DELAY_FASTEST
		if (!started) return;
		
		    mSensorManager.registerListener(this,
		        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),		        
		        SensorManager.SENSOR_DELAY_UI);
		 
		    mSensorManager.registerListener(this,
		        mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
		        SensorManager.SENSOR_DELAY_UI);
		 
		    mSensorManager.registerListener(this,
		        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
		        SensorManager.SENSOR_DELAY_UI);
	}

	public void stopReadingInertialSensors() {
		sensorsChk = false;
		if (!started) return;
		
		mSensorManager.unregisterListener(this);
    }
	
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
            	writeLogAccelerometer(event.values[0], event.values[1], event.values[2]);
                break;

            case Sensor.TYPE_ORIENTATION:
            	writeLogGyroscope(event.values[0], event.values[1], event.values[2]);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
            	writeLogMagnetic(event.values[0], event.values[1], event.values[2]);
                break;
        }
    }	
    
	public void startGps() {
	}

	public void stopGps() {
	}
	
	public void startBluetooth() {
		bluetoothChk = true;
		if (!started) return;
		
		if (bluetoothAdapter != null) {
			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				PersPos.app.startActivityForResult(enableBtIntent, 1);
			}

			registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
	}
	
	public void stopBluetooth() {
		bluetoothChk = false;
		if (!started) return;

		if (bluetoothAdapter != null) {
			try {
				unregisterReceiver(bluetoothReceiver);
			} catch(IllegalArgumentException e) {				
			}
			
			if (bluetoothAdapter.isDiscovering())
				bluetoothAdapter.cancelDiscovery();
		}
	}

	public void sendAndScan() {
		if (!wifiChk && !sensorsChk && !bluetoothChk)
			return; // nothing to do

		if (wifiChk && wifiMan != null && !wifiScanning) {
			wifiScanning = true;
			if (!wifiMan.startScan())
				wifiScanning = false;
		}

		if (bluetoothChk && bluetoothAdapter != null) {
			// Check is discoverable, and set it again
			
			if (!bluetoothAdapter.isDiscovering())
				bluetoothAdapter.startDiscovery();
		}
	}

	private void sendWifiInfo(List<WifiScanInfo> infWifi) {
		countWifi += 1;
		PersPosUtils.updateWifitxt(countWifi);
		
		// Update GPS location base on countWifi

		String msg = "Wifi: ";
		String xml = "<WifiScan>";
		xml += "<time>" + PersPosUtils.getTime() + "</time>";
		for (int i = 0; i < infWifi.size(); i++) {
			WifiScanInfo inf = infWifi.get(i);
			msg += String.format(" [mac %s, ssid %s, sigstr %g]", inf.mac,
					inf.ssid, inf.sigstr);

			xml += "<network>";
			xml += "<mac>" + inf.mac + "</mac>";
			xml += "<ssid>" + inf.ssid + "</ssid>";
			xml += "<sigstr>" + inf.sigstr + "</sigstr>";
			xml += "</network>";
		}
		xml += "</WifiScan>";

		//PersPosUtils.showLog(msg);
		Log.i(TAG, msg);

		if (Settings.offlineLog)
			PersPosUtils.writeOfflineLog(xml);
	}
	
	public void sendBluetoothInfo(BluetoothScanInfo inf) {
		countBt += 1;
		PersPosUtils.updateGPStxt(countBt);
		
		String msg = "Bluetooth: ";
		String xml = "<BluetoothScan>";
		xml += "<time>" + PersPosUtils.getTime() + "</time>";
		msg += String.format(" [name %s, address %s, rssi %d]", inf.name, inf.mac, inf.sigstr);

		xml += "<device>";
		xml += "<name>" + inf.name + "</name>";
		xml += "<mac>" + inf.mac + "</mac>";
		xml += "<sigstr>" + inf.sigstr +"</sigstr>";
		
		xml += "</device>";
		xml += "</BluetoothScan>";

		PersPosUtils.showLog(msg);
		Log.i(TAG, msg);

		if (Settings.offlineLog)
			PersPosUtils.writeOfflineLog(xml);
	}

	public void writeLogGyroscope(float x, float y, float z) {
		String xml = "<G>";
		xml += "<t>" + PersPosUtils.getTime() + "</t>";
		xml += "<X>" + x + "</X>";
		xml += "<Y>" + y + "</Y>";
		xml += "<Z>" + z + "</Z>";
		xml += "</G>";
		
		if (Settings.offlineLog)
			PersPosUtils.writeOfflineLog(xml);
	}


	public void writeLogAccelerometer(float x, float y, float z) {
		String xml = "<A>";
		xml += "<t>" + PersPosUtils.getTime() + "</t>";
		xml += "<X>" + x + "</X>";
		xml += "<Y>" + y + "</Y>";
		xml += "<Z>" + z + "</Z>";
		xml += "</A>";
		
		if (Settings.offlineLog)
			PersPosUtils.writeOfflineLog(xml);
	}
	

	public void writeLogMagnetic(float x, float y, float z) {
		String xml = "<M>";
		xml += "<t>" + PersPosUtils.getTime() + "</t>";
		xml += "<X>" + x + "</X>";
		xml += "<Y>" + y + "</Y>";
		xml += "<Z>" + z + "</Z>";
		xml += "</M>";
		
		if (Settings.offlineLog)
			PersPosUtils.writeOfflineLog(xml);
	}

	private void updateGpsInfo(Location loc) {
		if (loc == null) return;

		GpsScanInfo inf = new GpsScanInfo();
		inf.longitude = loc.getLongitude();
		inf.latitude = loc.getLatitude();
		inf.altitude = loc.hasAltitude() ? loc.getAltitude() : 0;
		inf.precision = loc.hasAccuracy() ? loc.getAccuracy() : 0;

		Bundle b = loc.getExtras();
		if (b == null)
			inf.numSat = -1;
		else inf.numSat = b.getInt("satellites", -1);
		
		Log.i(TAG, String.format("GPS received: (%f, %f, %f, %f), %d",
				inf.longitude, inf.latitude, inf.altitude,
				inf.precision, inf.numSat));
		
	}

	private void sendGpsInfo(GpsScanInfo inf) {
		String msg = String.format(Locale.US,
				"GPS: long %g, lat %g, alt %g, prec %g, numsat %d",
				inf.longitude, inf.latitude, inf.altitude,
				inf.precision, inf.numSat);
		Log.i(TAG, msg);

		String xml = "<GpsScan>";
		xml += "<time>" + PersPosUtils.getTime() + "</time>";
		xml += "<lat>" + inf.latitude + "</lat>";
		xml += "<lon>" + inf.longitude + "</lon>";
		xml += "<alt>" + inf.altitude + "</alt>";
		xml += "<prec>" + inf.precision + "</prec>";
		xml += "<numsat>" + inf.numSat + "</numsat>";
		xml += "</GpsScan>";

		if (Settings.onlineSending)
			PersPosUtils.sendScanInfo(xml);
		if (Settings.offlineLog)
			PersPosUtils.writeOfflineLog(xml);
		
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		// do nothing
		return null;
	}


}
