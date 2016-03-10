package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setTheme( android.R.style.Theme_Holo_Dialog);
		setContentView(R.layout.activity_select_template);
		
		final String layoutName = getIntent().getStringExtra(Const.EXTRA_LAYOUT);		
		final boolean manageMode = getIntent().getBooleanExtra(Const.EXTRA_TEMPLATE_MANAGE, false);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		
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
						ActionManager.addEditMacro(false, true, pos);
						finish();
					} else {					
						String macro = TemplateHelper.loadTemplate(prefs, pos); 										
						if ((macro != null) && (macro.length() > 0)) {
							ActionManager.runMacro(layoutName, macro);
							finish();
						} else {
							AlertDialog.Builder alert = new AlertDialog.Builder(SelectTemplateActivity.this);
							alert.setTitle(R.string.template_empty_title);
							alert.setMessage(R.string.template_empty_message);
							alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									ActionManager.addEditMacro(false, true, pos);
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
