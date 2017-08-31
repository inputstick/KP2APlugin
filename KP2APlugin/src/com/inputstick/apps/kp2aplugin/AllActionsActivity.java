package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;

import android.app.Activity;
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

public class AllActionsActivity extends Activity {
	
	private long lastActionTime;
	private long maxTime;

	private boolean isSecondaryLayoutEnabled;
	private String primaryLayoutCode;
	private String secondaryLayoutCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setTheme( android.R.style.Theme_Holo_Dialog);
		setContentView(R.layout.activity_all_actions);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		isSecondaryLayoutEnabled = PreferencesHelper.isSecondaryLayoutEnabled(prefs);
		primaryLayoutCode = PreferencesHelper.getPrimaryLayoutCode(prefs);
		secondaryLayoutCode = PreferencesHelper.getSecondaryLayoutCode(prefs);		
		
		Intent intent = getIntent();
		final EntryData entryData = new EntryData(intent);
		maxTime = intent.getLongExtra(Const.EXTRA_MAX_TIME, 0);
		lastActionTime = System.currentTimeMillis();

		ListView listViewActions = (ListView) findViewById(R.id.listViewActions);
		ArrayList<String> list = new ArrayList<String>();
		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.row, list);
		
		listAdapter.add(getActionString(R.string.action_open_settings, Const.LAYOUT_NONE));
		listAdapter.add(getActionString(R.string.action_connect, Const.LAYOUT_NONE));
		listAdapter.add(getActionString(R.string.action_disconnect, Const.LAYOUT_NONE));
		listAdapter.add(getActionString(R.string.action_open_mac_setup, Const.LAYOUT_NONE));
		listAdapter.add(getActionString(R.string.action_type_tab, Const.LAYOUT_NONE));
		listAdapter.add(getActionString(R.string.action_type_enter, Const.LAYOUT_NONE));
		listAdapter.add(getActionString(R.string.action_macro_add_edit, Const.LAYOUT_NONE));
		listAdapter.add(getActionString(R.string.action_template_manage, Const.LAYOUT_NONE));
		
		listAdapter.add(getActionString(R.string.action_type_user_tab_pass, Const.LAYOUT_PRIMARY));
		listAdapter.add(getActionString(R.string.action_type_user_tab_pass_enter, Const.LAYOUT_PRIMARY));
		listAdapter.add(getActionString(R.string.action_masked_password, Const.LAYOUT_PRIMARY));
		listAdapter.add(getActionString(R.string.action_macro_run, Const.LAYOUT_PRIMARY));
		listAdapter.add(getActionString(R.string.action_template_run, Const.LAYOUT_PRIMARY));
		listAdapter.add(getActionString(R.string.action_clipboard, Const.LAYOUT_PRIMARY));
		
		if (isSecondaryLayoutEnabled) {
			listAdapter.add(getActionString(R.string.action_type_user_tab_pass, Const.LAYOUT_SECONDARY));
			listAdapter.add(getActionString(R.string.action_type_user_tab_pass_enter, Const.LAYOUT_SECONDARY));
			listAdapter.add(getActionString(R.string.action_masked_password, Const.LAYOUT_SECONDARY));
			listAdapter.add(getActionString(R.string.action_macro_run, Const.LAYOUT_SECONDARY));
			listAdapter.add(getActionString(R.string.action_template_run, Const.LAYOUT_SECONDARY));
			listAdapter.add(getActionString(R.string.action_clipboard, Const.LAYOUT_SECONDARY));
		}
		
		listViewActions.setAdapter(listAdapter);
		listViewActions.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
				long now = System.currentTimeMillis();
				if (now > maxTime) {
					Toast.makeText(AllActionsActivity.this, R.string.text_locked, Toast.LENGTH_LONG).show();
				} else {
					maxTime += (now - lastActionTime);
					lastActionTime = now;
					boolean finish = true;
					
					Intent serviceIntent = new Intent(AllActionsActivity.this, InputStickService.class);
					serviceIntent.setAction(Const.SERVICE_ENTRY_ACTION); 
					
					switch (pos) {
						//general:
						case 0:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_SETTINGS);
							break;
						case 1:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_CONNECT);
							break;
						case 2:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_DISCONNECT);   
							break;						
						case 3:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MAC_SETUP);
							break;		
						case 4:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_TAB);   
							finish = false;
							break;		
						case 5:							
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_ENTER);
							finish = false;
							break;
						case 6:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MACRO_ADDEDIT);  
							break;
						case 7:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_TEMPLATE_MANAGE);
							break;							
						//entry, primary layout
						case 8:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_USER_PASS);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);
							break;				
						case 9:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_USER_PASS_ENTER);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);
							break;	
						case 10:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MASKED_PASSWORD);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);		
							break;		
						case 11:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MACRO_RUN);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);		
							break;	
						case 12:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_TEMPLATE_RUN);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);	
							break;															
						case 13:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_CLIPBOARD);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);
							break;
						//entry, secondary layout
						case 14:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_USER_PASS);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
							break;				
						case 15:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_USER_PASS_ENTER);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
							break;	
						case 16:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MASKED_PASSWORD);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
							break;		
						case 17:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MACRO_RUN);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
							break;
						case 18:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_TEMPLATE_RUN);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
							break;								
						case 19:
							serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_CLIPBOARD);
							serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
							break;			
						default:
							serviceIntent = null;							
					}
					
					if (serviceIntent != null) {
						serviceIntent.putExtras(entryData.getBundle());
						AllActionsActivity.this.startService(serviceIntent);
					}
					
					if (finish){
						finish();
					}
				}
			}			
		});
	}
	
	private String getActionString(int resId, int actionLayoutType) {
		String s = getString(resId);		
		if (actionLayoutType == Const.LAYOUT_PRIMARY && isSecondaryLayoutEnabled) {
			s += " (" + primaryLayoutCode + ")";
		}
		if (actionLayoutType == Const.LAYOUT_SECONDARY) {
			s += " (" + secondaryLayoutCode + ")";
		}
		return s;
	}
	
	
}
