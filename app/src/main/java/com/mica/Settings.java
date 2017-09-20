package com.mica;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.Toast;

public class Settings {
	public static String configSet;
	public static String serverAddress;
	public static String username;
	public static String password;
	public static boolean onlineSending;
	public static boolean offlineLog;
	public static String stepCalib;

	private static final String prefName = "PersPos";
	private static final String CONFIG_SET = "CONFIG_SET";
	private static final String SERVER_KEY = "SERVER";
	private static final String USERNAME_KEY = "USERNAME";
	private static final String PASSWORD_KEY = "PASSWORD";
	private static final String ONLINE_SENDING_KEY = "ONLINE_SENDING";
	private static final String OFFLINE_LOG_KEY = "OFFLINE_LOG";
	private static final String STEPCALIB = "STEP_CALIB";

	public static void Load() {
		SharedPreferences prefs = PersPos.app.getSharedPreferences(prefName, Activity.MODE_PRIVATE);
		configSet = prefs.getString(CONFIG_SET, "mica-psi");
		serverAddress = prefs.getString(SERVER_KEY, "psi.mica.edu.vn/PersPos/beta");
		username = prefs.getString(USERNAME_KEY, "");
		password = prefs.getString(PASSWORD_KEY, "");
		stepCalib = prefs.getString(STEPCALIB, "3");
		onlineSending = prefs.getBoolean(ONLINE_SENDING_KEY, false);
		offlineLog = prefs.getBoolean(OFFLINE_LOG_KEY, true);
				
		StepDetector.stepSensivity = Float.parseFloat(stepCalib);
		
	}

	public static void Update(String _configSet, String _serverAddress, String _username,
			String _password, String _stepCalib, boolean _onlineSending, boolean _offlineLog) {
		configSet = _configSet;
		serverAddress = _serverAddress;
		username = _username;
		password = _password;
		stepCalib = _stepCalib;
		onlineSending = _onlineSending;
		offlineLog = _offlineLog;

		StepDetector.stepSensivity = Float.parseFloat(stepCalib);

		SharedPreferences prefs = PersPos.app.getSharedPreferences(prefName, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(CONFIG_SET, configSet);
		editor.putString(SERVER_KEY, serverAddress);
		editor.putString(USERNAME_KEY, username);
		editor.putString(STEPCALIB, stepCalib);
		editor.putString(PASSWORD_KEY, password);
		editor.putBoolean(ONLINE_SENDING_KEY, onlineSending);
		editor.putBoolean(OFFLINE_LOG_KEY, offlineLog);
		editor.commit();

		Toast.makeText(PersPos.app.getBaseContext(), "Settings updated", Toast.LENGTH_SHORT).show();
	}

}
