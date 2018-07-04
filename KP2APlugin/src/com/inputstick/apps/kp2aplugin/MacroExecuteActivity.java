package com.inputstick.apps.kp2aplugin;

import keepass2android.pluginsdk.Strings;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MacroExecuteActivity extends Activity {
	
	private static final String INDEX_KEY = "index";
	
	private long lastActionTime;
	private long maxTime;	
	private int index;
	
	private EntryMacro macro;
	
	private Button buttonActionExecute;
	private Button buttonActionPrev;
	private Button buttonActionNext;
	private TextView textViewActionPreview;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(MacroExecuteActivity.this, R.string.text_activity_closed, Toast.LENGTH_LONG).show(); 
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setTheme( android.R.style.Theme_Holo_Dialog);
		setContentView(R.layout.activity_macro_execute);
		
		Intent intent = getIntent();		
		final TypingParams params = new TypingParams(intent);
		final EntryData entryData = new EntryData(intent);
		maxTime = intent.getLongExtra(Const.EXTRA_MAX_TIME, 0);
		final String macroData = intent.getStringExtra(Const.EXTRA_MACRO_DATA);
		
		if (macroData == null) {
			finish();
		} else {
			macro = new EntryMacro(macroData, entryData, params, false);
		}
		
		lastActionTime = System.currentTimeMillis();
		
		textViewActionPreview = (TextView)findViewById(R.id.textViewActionPreview);
		
		buttonActionExecute = (Button) findViewById(R.id.buttonActionExecute);
		buttonActionExecute.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (index >= macro.getActionsCount()) {
					finish();
				} else {
					if (checkTime()) {
						macro.executeActionAtIndex(MacroExecuteActivity.this, index);  
						goToNext();
					}
				}
			}
		});
		
		buttonActionPrev = (Button)findViewById(R.id.buttonActionPrev);
		buttonActionPrev.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				if (checkTime()) {
					goToPrev();
				}
			}
		});	
		
		buttonActionNext = (Button)findViewById(R.id.buttonActionNext);
		buttonActionNext.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				if (checkTime()) {
					goToNext();
				}
			}
		});	

		if (savedInstanceState == null) {			
			index = 0;
		} else {				
			index = savedInstanceState.getInt(INDEX_KEY);
		}
		manageUI();		
		
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


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(INDEX_KEY, index);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	private void goToPrev() {
		if (index > 0) {
			index--;
			manageUI();
		}			
	}
	
	private void goToNext() {
		if (index < macro.getActionsCount()) {
			index++;
			manageUI();
		}		
	}
	
	private void manageUI() {
		if (index >= macro.getActionsCount()) {
			textViewActionPreview.setText(R.string.end);
			buttonActionExecute.setText(R.string.done);
			buttonActionNext.setEnabled(false);
		} else {			
			textViewActionPreview.setText(getString(R.string.current_position) + " " + (index + 1) + "/" + macro.getActionsCount());			
			textViewActionPreview.append("\n" + getString(R.string.preview) + "\n" + macro.getPreviewAtIndex(index));
			buttonActionExecute.setText(R.string.execute);
			buttonActionNext.setEnabled(true);
		}
		if (index == 0) {
			buttonActionPrev.setEnabled(false);
		} else {
			buttonActionPrev.setEnabled(true);
		}
	}
	
	private boolean checkTime() {
		long now = System.currentTimeMillis();
		if (now > maxTime) {
			Toast.makeText(this, R.string.text_locked, Toast.LENGTH_LONG).show();
			return false;
		} else {
			maxTime += (now - lastActionTime);
			lastActionTime = now;
			return true;
		}
	}
	
}
