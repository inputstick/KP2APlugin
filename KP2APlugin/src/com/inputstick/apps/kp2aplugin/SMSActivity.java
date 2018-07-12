package com.inputstick.apps.kp2aplugin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.inputstick.api.hid.HIDKeycodes;

public class SMSActivity extends PluginPopupActivity {

	private TextView textViewSMSContent;
	private TextView textViewSMSSender;
	
	private Button buttonSMSEsc;
	private Button buttonSMSTab;
	private Button buttonSMSLeft;
	private Button buttonSMSRight;
	private Button buttonSMSEnter;	
	
	private Button buttonSMSDone;
	private Button buttonSMSTypeAll;	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_sms);
		
		Intent intent = getIntent();
		final TypingParams params = new TypingParams(intent);	
		final String message = intent.getStringExtra(Const.EXTRA_TEXT);
		final String sender = intent.getStringExtra(Const.EXTRA_SMS_SENDER);		
		
		textViewSMSContent = (TextView)findViewById(R.id.textViewSMSContent);
		textViewSMSSender = (TextView)findViewById(R.id.textViewSMSSender);
		
		buttonSMSEsc = (Button)findViewById(R.id.buttonSMSEsc);
		buttonSMSTab = (Button)findViewById(R.id.buttonSMSTab);
		buttonSMSLeft = (Button)findViewById(R.id.buttonSMSLeft);
		buttonSMSRight = (Button)findViewById(R.id.buttonSMSRight);
		buttonSMSEnter = (Button)findViewById(R.id.buttonSMSEnter);		
		
		buttonSMSDone = (Button)findViewById(R.id.buttonSMSDone);
		buttonSMSTypeAll = (Button)findViewById(R.id.buttonSMSTypeAll);
		
		buttonSMSEsc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ESCAPE, params).sendToService(SMSActivity.this, true);			
			}
		});	
		buttonSMSTab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_TAB, params).sendToService(SMSActivity.this, true);			
			}
		});	
		buttonSMSLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ARROW_LEFT, params).sendToService(SMSActivity.this, true);			
			}
		});	
		buttonSMSRight.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ARROW_RIGHT, params).sendToService(SMSActivity.this, true);			
			}
		});	
		buttonSMSEnter.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ENTER, params).sendToService(SMSActivity.this, true);			
			}
		});	
		
		
		buttonSMSDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent serviceIntent = new Intent(SMSActivity.this, InputStickService.class);
				serviceIntent.setAction(Const.SERVICE_DISMISS_SMS); 
				startService(serviceIntent);
				
				finish();		
			}
		});						
		
		buttonSMSTypeAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute(message, params).sendToService(SMSActivity.this, true);	
			}
		});
		
		SpannableString spannableText = new SpannableString(message);
		boolean markNext = false;
		int length;
		int pos = 0;
		String[] splitted = message.split(" ");
		
		for (String s : splitted) {
			length = s.length();
			if ((s.endsWith(":")) || (s.endsWith(",")) || (s.endsWith(";")) || (s.endsWith("."))) {
				length -= 1;
			}			
			if (length > 3) {				
				final String txt = s.substring(0, length);
				ClickableSpan clickableSpan = new ClickableSpan() {  
			        @Override  
			        public void onClick(View view) {  
			        	new ItemToExecute(txt, params).sendToService(SMSActivity.this, true);			
			        }  
			    };  
			    spannableText.setSpan(clickableSpan, pos, pos + length, 0);			    				
				if (markNext) {
					spannableText.setSpan(new ForegroundColorSpan(Color.GREEN), pos, pos + length, 0);				
				}
			}
			markNext = false;
			
			if (s.endsWith(":")) {
				markNext = true;
			}
			pos += s.length() + 1;
		}   				
		
		textViewSMSContent.setMovementMethod(LinkMovementMethod.getInstance()); 
		textViewSMSContent.setText(spannableText);		
		textViewSMSSender.setText(sender);
	}	
	
}
