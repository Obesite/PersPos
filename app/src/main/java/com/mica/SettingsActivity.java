package com.mica;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class SettingsActivity extends Activity {
	EditText ConfigSetEdit, ServerEdit, UsernameEdit, PasswordEdit, StepCalib;
	CheckBox OnlineSendingChk, OfflineLogChk;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		ConfigSetEdit = (EditText)findViewById(R.id.ConfigSetEdit);
		ServerEdit = (EditText)findViewById(R.id.ServerEdit);
		UsernameEdit = (EditText)findViewById(R.id.UsernameEdit);
		PasswordEdit = (EditText)findViewById(R.id.PasswordEdit);
		StepCalib = (EditText)findViewById(R.id.StepCalib);
		OnlineSendingChk = (CheckBox)findViewById(R.id.OnlineSendingChk);
		OfflineLogChk = (CheckBox)findViewById(R.id.OfflineLogChk);

		((Button)findViewById(R.id.OKBtn)).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Settings.Update(
					ConfigSetEdit.getText().toString(),
					ServerEdit.getText().toString(),
					UsernameEdit.getText().toString(),
					PasswordEdit.getText().toString(),
					StepCalib.getText().toString(),
					OnlineSendingChk.isChecked(),
					OfflineLogChk.isChecked());
				
				finish();
			}
		});

		((Button)findViewById(R.id.CancelBtn)).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

	}
	
	@Override
	public void onResume() {
		super.onResume();
		ConfigSetEdit.setText(Settings.configSet);
		ServerEdit.setText(Settings.serverAddress);
		UsernameEdit.setText(Settings.username);
		PasswordEdit.setText(Settings.password);
		StepCalib.setText(Settings.stepCalib);
		OnlineSendingChk.setChecked(Settings.onlineSending);
		OfflineLogChk.setChecked(Settings.offlineLog);
	}

}
