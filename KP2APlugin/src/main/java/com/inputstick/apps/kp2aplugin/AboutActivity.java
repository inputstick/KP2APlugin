package com.inputstick.apps.kp2aplugin;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import sheetrock.panda.changelog.ChangeLog;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		Button button = findViewById(R.id.buttonShowChangeLog);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ChangeLog(AboutActivity.this).getFullLogDialog().show();
			}
		});
	}
}
