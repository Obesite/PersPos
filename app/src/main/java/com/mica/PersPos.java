package com.mica;


import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.*;

public class PersPos extends Activity {
	
	public static PersPos app;
	public static boolean started = false;

	static int marker = 0;
	static int RFID_num = 1;

	TextView StatusTxt;
	Spinner TimeSpinner, rfidSpinner;
	CheckBox WifiChk, sensorsBtnChk, BluetoothChk;
	RadioButton ServiceStateRadio;
	
	TextView elapsedTxt;
	TextView countWifiTxt;
	TextView countGPSTxt;
	TextView previousCheckPointTxt;
	TextView nextCheckPointTxt;
	
	// nextCheckPoints
	int nextCheckPoints;
	int nCheckPoints;
	ArrayList<CheckPoint> listCheckPoints;
	
	public PersPos() {
		app = this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Settings.Load();

		StatusTxt = (TextView)findViewById(R.id.StatusTxt);

		ServiceStateRadio = (RadioButton)findViewById(R.id.ServiceState);
		ServiceStateRadio.setChecked(PersPosService.started);
		ServiceStateRadio.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				ServiceStateRadio.setChecked(PersPosService.started);
			}
		});

		Button buttonStart = (Button)findViewById(R.id.StartService);
		buttonStart.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				ServiceStateRadio.setChecked(true);
				
				WifiChk = (CheckBox) findViewById(R.id.WifiChk);
				PersPosService.wifiChk = WifiChk.isChecked();
				
				startService(new Intent(getApplicationContext(), PersPosService.class));
				
				nextCheckPoints = 0;
				resetCheckPointText();
			}
		});
		Button buttonStop = (Button)findViewById(R.id.StopService);
		buttonStop.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (!PersPosService.started) return;
				
				ServiceStateRadio.setChecked(false);
				stopService(new Intent(getApplicationContext(), PersPosService.class));
				PersPosService.service.stopWifi();
				PersPosService.service.stopBluetooth();
				PersPosService.service.stopReadingInertialSensors();
				
				resetCheckPointText();
			}
		});
		
		//time scan spinner
		TimeSpinner = (Spinner) findViewById(R.id.TimeSpinner);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
				this, R.array.time, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		TimeSpinner.setAdapter(adapter2);
		TimeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
				PersPosService.timeScan = Integer.parseInt(
						TimeSpinner.getItemAtPosition(pos).toString());
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		WifiChk = (CheckBox) findViewById(R.id.WifiChk);
		WifiChk.setChecked(PersPosService.wifiChk);
		WifiChk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean chk = WifiChk.isChecked();
				PersPosService.wifiChk = chk;
				if (PersPosService.started) {
					if (chk) PersPosService.service.startWifi();
					else PersPosService.service.stopWifi();
				}
			}
		});
		
		BluetoothChk = (CheckBox) findViewById(R.id.BluetoothChk);
		BluetoothChk.setChecked(PersPosService.bluetoothChk);
		BluetoothChk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean chk = BluetoothChk.isChecked();
				PersPosService.bluetoothChk  = chk;
				if (PersPosService.started) {
					if (chk) PersPosService.service.startBluetooth();
					else PersPosService.service.stopBluetooth();
				}
			}
		});
	
		sensorsBtnChk = (CheckBox) findViewById(R.id.SensChk);
		sensorsBtnChk.setChecked(PersPosService.sensorsChk);
		sensorsBtnChk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean chk = sensorsBtnChk.isChecked();
				PersPosService.sensorsChk  = chk;
				if (PersPosService.started) {
					if (chk) PersPosService.service.startReadingInertialSensors();
					else PersPosService.service.stopReadingInertialSensors();
				}
			}
		});
		
		Button btnArriveCheckpoints = (Button)findViewById(R.id.reachNextStop);
		btnArriveCheckpoints.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				updatedNextCheckpoint();
			}
		});
		

		// Clear the text field
		
		elapsedTxt = (TextView)findViewById(R.id.timeElapsedSecond);
		countWifiTxt = (TextView)findViewById(R.id.nWifiPackages);
		countGPSTxt = (TextView)findViewById(R.id.nGPSPackages);

		Button buttonClear = (Button)findViewById(R.id.ClearText);
		buttonClear.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				PersPosUtils.clearLog();
				
				elapsedTxt.setText("0 seconds");
				countWifiTxt.setText("0 packages");
				countGPSTxt.setText("0 packages");
			}
		});
		
		previousCheckPointTxt = (TextView)findViewById(R.id.previousCheckPoints);
		nextCheckPointTxt = (TextView)findViewById(R.id.nextCheckPoints);

		//listCheckPoints = MovingPath.getPath1();
		//listCheckPoints = MovingPath.getPath2_user1();
		//listCheckPoints = MovingPath.getPath2_user2();
		//listCheckPoints = MovingPath.getPath3_user1();
		//listCheckPoints = MovingPath.getPath3_user2();
		listCheckPoints = MovingPath.getPath();
		nextCheckPoints = 0;
		
		started = true;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		started = false;
		super.onDestroy();
	}

	public void sendMarker() {
		String msg = String.format(Locale.US, "Marker: %d", marker);
		PersPosUtils.showLog(msg);

		String xml = "<Marker>";
		xml += "<time>" + PersPosUtils.getTime() + "</time>";
		xml += "<number>" + marker + "</number>";
		xml += "</Marker>";
		
		if (Settings.onlineSending)
			PersPosUtils.sendScanInfo(xml);
		if (Settings.offlineLog)
			PersPosUtils.writeOfflineLog(xml);

		marker++;
	}
	
	public void updatedNextCheckpoint() {
		if (nextCheckPoints < listCheckPoints.size()) {
			CheckPoint p = listCheckPoints.get(nextCheckPoints);
			String xml = "<checkpoint>";
			xml += "<time>" + PersPosUtils.getTime() + "</time>";
			xml += "<name>" + p.name + "</name>";
			xml += "<X>" + p.xlocal + "</X>";
			xml += "<Y>" + p.ylocal + "</Y>";			
			xml += "<Z>" + p.zlocal + "</Z>";
			xml += "</checkpoint>";
			
			PersPosUtils.writeOfflineLog(xml);
			PersPosUtils.showLog("Arrived at " + p.getName());
			
			previousCheckPointTxt.setText(p.name);
			nextCheckPoints += 1;
			
			if (nextCheckPoints < listCheckPoints.size()) {
				nextCheckPointTxt.setText(listCheckPoints.get(nextCheckPoints).getName());				
			}
			else {
				nextCheckPointTxt.setText("Stop");								
			}
		}
	}
	
	public void resetCheckPointText() {
		previousCheckPointTxt.setText("Start");
		nextCheckPointTxt.setText(listCheckPoints.get(0).getName());
	}
	
	public void sendRFIDCode() {
		String msg = String.format(Locale.US, "RFID: %d", RFID_num);
		int virtual_rfid = RFID_num + 100;
		String code = "";
		
		code = "00000000";
		if (Settings.username.toString().equals("kien")) {
			code = "11111111";
		} 
		
		String xml = "<RfidScan>";
		xml += "<code>" + code + "</code>";
		xml += "<reader>" +   virtual_rfid  + "</reader>";
		xml += "</RfidScan>";
		PersPosUtils.showLog(msg);
		
		if (Settings.onlineSending)
			PersPosUtils.sendScanInfo(xml);
		if (Settings.offlineLog)
			PersPosUtils.writeOfflineLog(xml);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 1, "Reset Data");
		menu.add(0, 2, 2, "Clear Log");
		menu.add(0, 3, 3, "Settings");
		menu.add(0, 4, 4, "Exit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			marker = 0;
			PersPosUtils.clearOfflineLog();
			return true;
		case 2:
			PersPosUtils.clearLog();
			return true;
		case 3:
			startActivity(new Intent("com.mica.SETTINGS"));
			return true;
		case 4:
			finish();
			return true;
		}
		return false;
	}
	
	
}
