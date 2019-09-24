package com.inputstick.apps.kp2aplugin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.inputstick.api.hid.HIDKeycodes;

public class SMSActivity extends PluginDialogActivity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms);
		
		Intent intent = getIntent();
		final TypingParams params = new TypingParams(intent);	
		final String message = intent.getStringExtra(Const.EXTRA_TEXT);
		final String sender = intent.getStringExtra(Const.EXTRA_SMS_SENDER);

		Button buttonSMSEsc = findViewById(R.id.buttonSMSEsc);
		buttonSMSEsc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ESCAPE, params).sendToService(SMSActivity.this, true);			
			}
		});

		Button buttonSMSTab = findViewById(R.id.buttonSMSTab);
		buttonSMSTab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_TAB, params).sendToService(SMSActivity.this, true);			
			}
		});

		Button buttonSMSLeft = findViewById(R.id.buttonSMSLeft);
		buttonSMSLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ARROW_LEFT, params).sendToService(SMSActivity.this, true);			
			}
		});

		Button buttonSMSRight = findViewById(R.id.buttonSMSRight);
		buttonSMSRight.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ARROW_RIGHT, params).sendToService(SMSActivity.this, true);			
			}
		});

		Button buttonSMSEnter = findViewById(R.id.buttonSMSEnter);
		buttonSMSEnter.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ENTER, params).sendToService(SMSActivity.this, true);			
			}
		});

		Button buttonSMSDone = findViewById(R.id.buttonSMSDone);
		buttonSMSDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent serviceIntent = new Intent(SMSActivity.this, InputStickService.class);
				serviceIntent.setAction(Const.SERVICE_DISMISS_SMS);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					startForegroundService(serviceIntent);
				} else {
					startService(serviceIntent);
				}
				finish();		
			}
		});

		Button buttonSMSTypeAll = findViewById(R.id.buttonSMSTypeAll);
		buttonSMSTypeAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute(message, params).sendToService(SMSActivity.this, true);	
			}
		});
		
		SpannableString spannableText = new SpannableString(message);
		boolean markNext = false;
		int length;
		int pos = 0;
		String[] splitted = message.split("[ \n\t]");
		
		for (String s : splitted) {
			length = s.length();
			if ((s.endsWith(":")) || (s.endsWith(",")) || (s.endsWith(";")) || (s.endsWith("."))) {
				length -= 1;
			}			
			if (length > 3) {				
				final String txt = s.substring(0, length);
				ClickableSpan clickableSpan = new ClickableSpan() {  
			        @Override  
			        public void onClick(@NonNull View view) {
			        	new ItemToExecute(txt, params).sendToService(SMSActivity.this, true);			
			        }  
			    };  
			    spannableText.setSpan(clickableSpan, pos, pos + length, 0);			    				
				if (markNext) {
					spannableText.setSpan(new ForegroundColorSpan(Color.GREEN), pos, pos + length, 0);				
				}
			}

			markNext = s.endsWith(":");
			pos += s.length() + 1;
		}

		TextView textViewSMSContent = findViewById(R.id.textViewSMSContent);
		TextView textViewSMSSender = findViewById(R.id.textViewSMSSender);
		textViewSMSContent.setMovementMethod(LinkMovementMethod.getInstance()); 
		textViewSMSContent.setText(spannableText);		
		textViewSMSSender.setText(sender);
	}	
	
}
