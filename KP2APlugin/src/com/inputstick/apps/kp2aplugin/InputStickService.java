package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickError;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.basic.InputStickKeyboard;
import com.inputstick.api.hid.HIDTransaction;
import com.inputstick.api.hid.KeyboardReport;

public class InputStickService extends Service implements InputStickStateListener {
	
	private static final String _TAG = "KP2AINPUTSTICK";	
	
	private ArrayList<ItemToExecute> items = new ArrayList<ItemToExecute>(); 

	
	@Override
	public void onCreate() {
		InputStickHID.addStateListener(this);
		super.onCreate();
	}	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		if (intent != null) {
			String action = intent.getAction();
			if (Const.SERVICE_DISCONNECT.equals(action)) {
				Log.d(_TAG, "disconnecting");
				try {
					int state = InputStickHID.getState();
					switch (state) {
						case ConnectionManager.STATE_CONNECTED:
						case ConnectionManager.STATE_CONNECTING:
						case ConnectionManager.STATE_READY:
							InputStickHID.disconnect();	
							break;
						case ConnectionManager.STATE_DISCONNECTED:
						case ConnectionManager.STATE_FAILURE:	
							break;
						default:
							InputStickHID.disconnect();	
					}
				} catch (NullPointerException e) {
					Log.d(_TAG, "couldn't disconnect. Probably we never connected.");
				}
				stopSelf();
				return Service.START_NOT_STICKY;
			} else if (Const.SERVICE_CONNECT.equals(action)) {
				if ( !InputStickHID.isConnected()) {
					InputStickHID.connect(getApplication());
				}			
			} else if (Const.SERVICE_EXEC.equals(action)) {
				int state = InputStickHID.getState();		
				Bundle b = intent.getExtras();
				//Log.d(_TAG, "type params: "+params);			
				switch (state) {
					case ConnectionManager.STATE_CONNECTED:
					case ConnectionManager.STATE_CONNECTING:
						synchronized (items) {
							items.add(new ItemToExecute(b));
						}						
						break;
					case ConnectionManager.STATE_READY:
						new ItemToExecute(b).execute();
						break;
					case ConnectionManager.STATE_DISCONNECTED:
					case ConnectionManager.STATE_FAILURE:	
						synchronized (items) {
							items.add(new ItemToExecute(b));
						}										
						Log.d(_TAG, "trigger connect");
						InputStickHID.connect(getApplication());					
						break;											
				}				
			} else {
				//unknown action
			}
		}		
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		InputStickHID.removeStateListener(this);
		items.clear();
		super.onDestroy();
	}

	@Override
	public void onStateChanged(int state) {
		Log.d(_TAG, "state changed: "+state);
		switch (state) {
			case ConnectionManager.STATE_READY:		
				executeQueue();
				break;
			case ConnectionManager.STATE_DISCONNECTED:
				Log.d(_TAG, "stopping service. State = "+state);
				stopSelf();
				break;
			case ConnectionManager.STATE_FAILURE:
				Log.d(_TAG, "stopping service. State = "+state);				
				
				// can't use: AlertDialog ad = InputStickHID.getDownloadDialog(this.getApplicationContext()); - badtoken exception				
				if (InputStickHID.getErrorCode() == InputStickError.ERROR_ANDROID_NO_UTILITY_APP) {
					Toast.makeText(this, R.string.text_missing_utility_app, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, R.string.text_connection_failed, Toast.LENGTH_LONG).show();
				}
				
				stopSelf();
				break;		
			default:
				break;
		}			
	}
			
	
	private void executeQueue() {
		dummyKeyPresses(15);
		synchronized (items) {
			for (ItemToExecute itt : items) {
				Log.d(_TAG, "executing (after callback) ");
				itt.execute();
			}
			items.clear();
		}
	}
	
	//short delay achieved by sending empty keyboard reports 
	private void dummyKeyPresses(int keys) {		
		HIDTransaction t = new HIDTransaction();
		for (int i = 0; i < keys * 3; i++) {  // 1 keypress = 3 HID reports (modifier, modifier+key, all released)
			t.addReport(new KeyboardReport((byte)0x00, (byte)0x00));
		}
		InputStickHID.addKeyboardTransaction(t);
	}
	
	/*private String reverseCase(String input) {
		if (input != null) {			
			char c;
			char[] reversed = new char[input.length()];
			for (int i = 0; i < input.length(); i++) {
				c = input.charAt(i);
				if (Character.isLetter(c)) {
					if (Character.isUpperCase(c)) {
						reversed[i] = Character.toLowerCase(c);
					} else if (Character.isLowerCase(c)) {
						reversed[i] = Character.toUpperCase(c);
					}				
				} else {
					reversed[i] = c;
				}
			}
			return new String(reversed);
		} else {
			return null;
		}		
	}*/
	
	private long lastCapsLockWatningTime;
	private static final long CAPSLOCK_WATNING_TIMEOUT = 10000;
	
	private class ItemToExecute {
		public Bundle mBundle;
		ItemToExecute(Bundle b) {
			mBundle = b;
		}
		
		public void execute() {
			if ((InputStickHID.getState() == ConnectionManager.STATE_READY) && (mBundle != null)) {				
				String action = mBundle.getString(Const.EXTRA_ACTION);
				InputStickHID.setKeyboardReportMultiplier(mBundle.getInt(Const.EXTRA_REPORT_MULTIPLIER, 1));				
				
				if (Const.ACTION_TYPE.equals(action)) {
					String text = mBundle.getString(Const.EXTRA_TEXT, "");
					String layout = mBundle.getString(Const.EXTRA_LAYOUT);
					if (InputStickKeyboard.isCapsLock()) {
						long now = System.currentTimeMillis();
						if (now > lastCapsLockWatningTime + CAPSLOCK_WATNING_TIMEOUT) {
							lastCapsLockWatningTime = now;
							Toast.makeText(InputStickService.this, R.string.text_capslock_warning, Toast.LENGTH_LONG).show();
						}
					}
					InputStickKeyboard.type(text, layout);							
				} else if (Const.ACTION_KEY_PRESS.equals(action)) {
					byte modifier = mBundle.getByte(Const.EXTRA_MODIFIER);
					byte key = mBundle.getByte(Const.EXTRA_KEY);					
					InputStickKeyboard.pressAndRelease(modifier, key);
				} else if (Const.ACTION_DELAY.equals(action)) {
					int reports = mBundle.getInt(Const.EXTRA_DELAY, 0) / 4; //1 report / 4ms
					dummyKeyPresses(reports);
				} else {
					//unknown action type!
				}				
				InputStickHID.setKeyboardReportMultiplier(1);			
			}
		}
	}


}
