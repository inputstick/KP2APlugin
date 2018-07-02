package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;

import keepass2android.pluginsdk.Strings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SelectTemplateActivity extends Activity {
	
	private long lastActionTime;
	private long maxTime;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(SelectTemplateActivity.this, R.string.text_activity_closed, Toast.LENGTH_SHORT).show(); 
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setTheme( android.R.style.Theme_Holo_Dialog);
		setContentView(R.layout.activity_select_template);
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Intent intent = getIntent();	
		final TypingParams params = new TypingParams(intent);
		final EntryData entryData = new EntryData(intent);		
		final boolean manageMode = intent.getBooleanExtra(Const.EXTRA_TEMPLATE_MANAGE, false);		
		
		maxTime = getIntent().getLongExtra(Const.EXTRA_MAX_TIME, 0);
		lastActionTime = System.currentTimeMillis();

		ListView listViewActions = (ListView) findViewById(R.id.listViewTemplates);
		ArrayList<String> list = new ArrayList<String>();
		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.row, list);

		CharSequence[] names = TemplateHelper.getTemplateNames(prefs);
		for (CharSequence name : names) {
			listAdapter.add(name.toString());
		}			
		
		listViewActions.setAdapter(listAdapter);
		listViewActions.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, final int pos, long arg3) {
				if (checkTime()) {
					if (manageMode) {
						ActionHelper.addEditTemplateAction(SelectTemplateActivity.this, pos);
						finish();
					} else {					
						String macroData = TemplateHelper.loadTemplate(prefs, pos); 										
						if ((macroData != null) && (macroData.length() > 0)) {
							ActionHelper.executeMacro(SelectTemplateActivity.this, entryData, params, macroData);
							finish();
						} else {
							AlertDialog.Builder alert = new AlertDialog.Builder(SelectTemplateActivity.this);
							alert.setTitle(R.string.template_empty_title);
							alert.setMessage(R.string.template_empty_message);
							alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									ActionHelper.addEditTemplateAction(SelectTemplateActivity.this, pos);									
								}
							});
							alert.setNegativeButton(R.string.cancel, null);
							alert.show();						
						}		
					}
				}
			}
		});		
		
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
