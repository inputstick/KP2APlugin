package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SelectTemplateActivity extends PluginPopupActivity {
	
	private long lastActionTime;
	private long maxTime;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setOptions(true, true, true);
		super.onCreate(savedInstanceState);
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
