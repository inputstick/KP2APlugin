package com.inputstick.apps.kp2aplugin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.inputstick.api.hid.HIDKeycodes;

public class MaskedPasswordActivity extends Activity {
	
	private static final String OFFSET_KEY = "offset";
	private static final String CLICKED_KEY = "clicked";
		
	private static final int BUTTONS_CNT = 16;
	
	private boolean[] wasClicked;
	private int offset;
	
	private MyButtonOnClickListener listener = new MyButtonOnClickListener();
	
	private String password;
	private TypingParams params;
			
	private Button buttonPrev;
	private Button buttonNext;
	
	private CheckBox checkBoxShowPassword;
	private Button[] buttons;
	private int[] buttonIds = {
			R.id.buttonChar1,
			R.id.buttonChar2,
			R.id.buttonChar3,
			R.id.buttonChar4,
			R.id.buttonChar5,
			R.id.buttonChar6,
			R.id.buttonChar7,
			R.id.buttonChar8,
			R.id.buttonChar9,
			R.id.buttonChar10,
			R.id.buttonChar11,
			R.id.buttonChar12,
			R.id.buttonChar13,
			R.id.buttonChar14,
			R.id.buttonChar15,
			R.id.buttonChar16,
	};	
	
	private Button buttonMaskedPassEsc;
	private Button buttonMaskedPassTab;
	private Button buttonMaskedPassLeft;
	private Button buttonMaskedPassRight;
	private Button buttonMaskedPassEnter;
	
	private Button buttonMaskedClose;
	
	private final BroadcastReceiver finishReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(MaskedPasswordActivity.this, R.string.text_activity_closed, Toast.LENGTH_SHORT).show(); 
			finish();
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		super.setTheme( android.R.style.Theme_Holo_Dialog);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,  WindowManager.LayoutParams.FLAG_SECURE);

		setContentView(R.layout.activity_masked_password);
		
		Intent intent = getIntent();
		params = new TypingParams(intent);		
		password = intent.getStringExtra(Const.EXTRA_TEXT);
		if (password == null) {
			password = "";
		}
		wasClicked = new boolean[password.length()];
		
		checkBoxShowPassword = (CheckBox)findViewById(R.id.checkBoxShowPassword);
		buttonPrev = (Button)findViewById(R.id.buttonPrev);
		buttonNext = (Button)findViewById(R.id.buttonNext);
		
		checkBoxShowPassword.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				refreshButtons();
			}
		});

		buttonPrev.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (offset >= BUTTONS_CNT) {
					offset -= BUTTONS_CNT;
					refreshButtons();
				}
			}
		});
		
		buttonNext.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				offset += BUTTONS_CNT;
				refreshButtons();				
			}
		});		
		
		buttons = new Button[16];
		for (int i = 0; i < 16; i++) {
			buttons[i] = (Button)findViewById(buttonIds[i]);
			buttons[i].setOnClickListener(listener);
		}
		
		
		buttonMaskedPassEsc = (Button)findViewById(R.id.buttonMaskedPassEsc);
		buttonMaskedPassTab = (Button)findViewById(R.id.buttonMaskedPassTab);
		buttonMaskedPassLeft = (Button)findViewById(R.id.buttonMaskedPassLeft);
		buttonMaskedPassRight = (Button)findViewById(R.id.buttonMaskedPassRight);
		buttonMaskedPassEnter = (Button)findViewById(R.id.buttonMaskedPassEnter);
		buttonMaskedClose = (Button)findViewById(R.id.buttonMaskedClose);
		
		buttonMaskedPassEsc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ESCAPE, params).sendToService(MaskedPasswordActivity.this, true);			
			}
		});	
		buttonMaskedPassTab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_TAB, params).sendToService(MaskedPasswordActivity.this, true);			
			}
		});	
		buttonMaskedPassLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ARROW_LEFT, params).sendToService(MaskedPasswordActivity.this, true);			
			}
		});	
		buttonMaskedPassRight.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ARROW_RIGHT, params).sendToService(MaskedPasswordActivity.this, true);			
			}
		});	
		buttonMaskedPassEnter.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				new ItemToExecute((byte)0, HIDKeycodes.KEY_ENTER, params).sendToService(MaskedPasswordActivity.this, true);			
			}
		});	
				
		buttonMaskedClose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {			
				finish();		
			}
		});	
		
	
		if (savedInstanceState != null) {			
			offset = savedInstanceState.getInt(OFFSET_KEY);	
			wasClicked = savedInstanceState.getBooleanArray(CLICKED_KEY);		
		}
		
		IntentFilter filter;
		filter = new IntentFilter();
		filter.addAction(Const.BROADCAST_FORCE_FINISH_ALL);
		filter.addAction(Const.BROADCAST_FORCE_FINISH_SECURE);
		registerReceiver(finishReceiver, filter);	
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putInt(OFFSET_KEY, offset);
	    savedInstanceState.putBooleanArray(CLICKED_KEY, wasClicked);
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
		refreshButtons();
	}
	
	@Override
	protected void onDestroy() {		
		unregisterReceiver(finishReceiver);
		super.onDestroy();
	}	
	
	private void drawButton(int i) {
		Button b = buttons[i];
		int index = i + offset;
		boolean enabled = true;
		int color = Color.WHITE;
		
		if (index >= password.length()) {
			b.setText("");
			enabled = false;			
		} else {
			if (checkBoxShowPassword.isChecked()) {
				b.setText(String.valueOf(password.charAt(index)));
			} else {
				b.setText(String.valueOf(index + 1));
			}
			
			if (wasClicked[index]) {
				color = Color.GREEN;
			} 
		}
		
		b.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY );
		b.setEnabled(enabled);
	}
	
	private void refreshButtons() {
		if ((offset + BUTTONS_CNT) >= password.length()) {
			buttonNext.setEnabled(false);
		} else {
			buttonNext.setEnabled(true);
		}
		if (offset == 0) {
			buttonPrev.setEnabled(false);
		} else {
			buttonPrev.setEnabled(true);
		}		
		
		for (int i = 0; i < BUTTONS_CNT; i++) {
			drawButton(i);
		}
	}
	
	private void type(int n) {
		if (password.length() >= n) {
			int index = n;
			if (index < 0) return;
			char c = password.charAt(index);
			String toType = String.valueOf(c);
			new ItemToExecute(toType, params).sendToService(this, true);
		}
	}
	
	private class MyButtonOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			for (int i = 0; i < 16; i++) {
				if (buttons[i].equals(v)) {
					type(i + offset);
					wasClicked[i + offset] = true;
					v.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY );
				}
			}			
		}		
	}

}
