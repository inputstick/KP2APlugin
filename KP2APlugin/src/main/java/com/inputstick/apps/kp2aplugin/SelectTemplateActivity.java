package com.inputstick.apps.kp2aplugin;

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

import java.util.ArrayList;

public class SelectTemplateActivity extends PluginDialogActivity {

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

		ListView listViewActions = findViewById(R.id.listViewTemplates);
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
				if (manageMode) {
					ActionHelper.addEditTemplateAction(SelectTemplateActivity.this, pos);
					finish();
				} else {					
					String macroData = TemplateHelper.loadTemplate(prefs, pos); 										
					if (macroData.length() > 0) {
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
		});		
	}
	
}
