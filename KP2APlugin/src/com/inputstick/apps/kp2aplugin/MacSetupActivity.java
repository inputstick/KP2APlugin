package com.inputstick.apps.kp2aplugin;

import keepass2android.pluginsdk.Strings;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.hid.HIDKeycodes;
import com.inputstick.api.layout.KeyboardLayout;

public class MacSetupActivity extends Activity {
	
	private boolean nonUS;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(MacSetupActivity.this, R.string.text_activity_closed, Toast.LENGTH_SHORT).show(); 
			finish();
		}
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setTheme( android.R.style.Theme_Holo_Dialog);
		setContentView(R.layout.activity_mac_setup);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String primaryLayoutCode = PreferencesHelper.getPrimaryLayoutCode(prefs);				
		KeyboardLayout primaryLayout = KeyboardLayout.getLayout(primaryLayoutCode);
		final TypingParams params = new TypingParams(primaryLayoutCode, Const.TYPING_SPEED_DEFAULT);		
		
		TextView textViewLayoutInfo = (TextView)findViewById(R.id.textViewLayoutInfo);
		textViewLayoutInfo.append(" " + primaryLayoutCode);
		
		Button buttonNextToShiftLeft;
		Button buttonNextToShiftRight;
		buttonNextToShiftLeft = (Button)findViewById(R.id.buttonNextToShiftLeft);		
		buttonNextToShiftLeft.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				if (InputStickHID.getState() == ConnectionManager.STATE_READY) {
					if (nonUS) {
						new ItemToExecute(HIDKeycodes.NONE, HIDKeycodes.KEY_BACKSLASH_NON_US, params).sendToService(MacSetupActivity.this, true);
					} else {
						new ItemToExecute(HIDKeycodes.NONE, HIDKeycodes.KEY_Z, params).sendToService(MacSetupActivity.this, true);
					}
				} else {				
					Toast.makeText(MacSetupActivity.this, R.string.not_ready, Toast.LENGTH_SHORT).show();
				}
			}
		});
		buttonNextToShiftRight = (Button)findViewById(R.id.buttonNextToShiftRight);		
		buttonNextToShiftRight.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				if (InputStickHID.getState() == ConnectionManager.STATE_READY) {
					new ItemToExecute(HIDKeycodes.NONE, HIDKeycodes.KEY_SLASH, params).sendToService(MacSetupActivity.this, true);
				} else {				
					Toast.makeText(MacSetupActivity.this, R.string.not_ready, Toast.LENGTH_SHORT).show();
				}
			}
		});		
		
		//check if non-us backslash key is used by this layout:				
		int[][] lut = primaryLayout.getLUT();
		int tmp = lut[0x56][1];
		nonUS = true;
		for (int i = 0; i < 0x40; i++) {
			for (int j = 1; j < 6; j++) {
				if (lut[i][j] == tmp) {
					nonUS = false;
					break;
				}
			}
		}
		if (nonUS) {
			//non-US ISO
			buttonNextToShiftLeft.setText(String.valueOf(primaryLayout.getChar(KeyboardLayout.hidToScanCode(HIDKeycodes.KEY_BACKSLASH_NON_US), false, false, false)));
		} else {
			//US ANSI
			buttonNextToShiftLeft.setText(String.valueOf(primaryLayout.getChar(KeyboardLayout.hidToScanCode(HIDKeycodes.KEY_Z), false, false, false)));
		}
		buttonNextToShiftRight.setText(String.valueOf(primaryLayout.getChar(KeyboardLayout.hidToScanCode(HIDKeycodes.KEY_SLASH), false, false, false)));
		
		IntentFilter filter;
		filter = new IntentFilter();
		filter.addAction(Strings.ACTION_CLOSE_DATABASE);
		filter.addAction(Strings.ACTION_LOCK_DATABASE);
		registerReceiver(receiver, filter);	
	}
	
	@Override
	protected void onDestroy() {		
		unregisterReceiver(receiver);
		super.onDestroy();
	}	


}
