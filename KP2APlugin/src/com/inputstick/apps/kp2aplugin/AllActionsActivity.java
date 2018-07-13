package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AllActionsActivity extends PluginPopupActivity {
	
	public enum ActionId {
		OPEN_SETTINGS,
		CONNECT,
		DISCONNECT,
		REMOTE,
		MAC_SETUP, 
		TYPE_TAB,
		TYPE_ENTER,
		MACRO_ADD_EDIT,
		TEMPLATE_MANAGE,

		QUICK_SHORTCUT_1,
		QUICK_SHORTCUT_2,
		QUICK_SHORTCUT_3,

		USER_TAB_PASS_PRIMARY,
		USER_TAB_PASS_ENTER_PRIMARY,
		MASKED_PASS_PRIMARY,
		MACRO_RUN_PRIMARY,
		TEMPLATE_RUN_PRIMARY,
		CLIPBOARD_PRIMARY,
		
		USER_TAB_PASS_SECONDARY,
		USER_TAB_PASS_ENTER_SECONDARY,
		MASKED_PASS_SECONDARY,
		MACRO_RUN_SECONDARY,
		TEMPLATE_RUN_SECONDARY,
		CLIPBOARD_SECONDARY
	}		

	private boolean isSecondaryLayoutEnabled;
	private String primaryLayoutCode;
	private String secondaryLayoutCode;
	
	private ArrayList<String> list;
	private ArrayAdapter<String> listAdapter;
	private List<ActionId> actionsLUT;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setOptions(false, true, true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_actions);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		isSecondaryLayoutEnabled = PreferencesHelper.isSecondaryLayoutEnabled(prefs);
		primaryLayoutCode = PreferencesHelper.getPrimaryLayoutCode(prefs);
		secondaryLayoutCode = PreferencesHelper.getSecondaryLayoutCode(prefs);		
		
		Intent intent = getIntent();
		final EntryData entryData = new EntryData(intent);

		ListView listViewActions = (ListView) findViewById(R.id.listViewActions);
		list = new ArrayList<String>();
		listAdapter = new ArrayAdapter<String>(this, R.layout.row, list);
		actionsLUT = new ArrayList<ActionId>();
		
		addAction(getActionString(R.string.action_open_settings, Const.LAYOUT_NONE), ActionId.OPEN_SETTINGS);
		addAction(getActionString(R.string.action_connect, Const.LAYOUT_NONE), ActionId.CONNECT);
		addAction(getActionString(R.string.action_disconnect, Const.LAYOUT_NONE), ActionId.DISCONNECT);
		addAction(getActionString(R.string.action_remote, Const.LAYOUT_NONE), ActionId.REMOTE);
		addAction(getActionString(R.string.action_open_mac_setup, Const.LAYOUT_NONE), ActionId.MAC_SETUP);
		addAction(getActionString(R.string.action_type_tab, Const.LAYOUT_NONE), ActionId.TYPE_TAB);
		addAction(getActionString(R.string.action_type_enter, Const.LAYOUT_NONE), ActionId.TYPE_ENTER);
		addAction(getActionString(R.string.action_macro_add_edit, Const.LAYOUT_NONE), ActionId.MACRO_ADD_EDIT);
		addAction(getActionString(R.string.action_template_manage, Const.LAYOUT_NONE), ActionId.TEMPLATE_MANAGE);
		
		int cnt = PreferencesHelper.getEnabledQuickShortcuts(prefs);
		if (cnt > 0) {
			addAction(PreferencesHelper.getQuickShortcut(prefs, 1), ActionId.QUICK_SHORTCUT_1);
		}
		if (cnt > 1) {
			addAction(PreferencesHelper.getQuickShortcut(prefs, 2), ActionId.QUICK_SHORTCUT_2);
		}
		if (cnt > 2) {
			addAction(PreferencesHelper.getQuickShortcut(prefs, 3), ActionId.QUICK_SHORTCUT_3);			
		}	
		
		addAction(getActionString(R.string.action_type_user_tab_pass, Const.LAYOUT_PRIMARY), ActionId.USER_TAB_PASS_PRIMARY);
		addAction(getActionString(R.string.action_type_user_tab_pass_enter, Const.LAYOUT_PRIMARY), ActionId.USER_TAB_PASS_ENTER_PRIMARY);
		addAction(getActionString(R.string.action_masked_password, Const.LAYOUT_PRIMARY), ActionId.MASKED_PASS_PRIMARY);
		addAction(getActionString(R.string.action_macro_run, Const.LAYOUT_PRIMARY), ActionId.MACRO_RUN_PRIMARY);
		addAction(getActionString(R.string.action_template_run, Const.LAYOUT_PRIMARY), ActionId.TEMPLATE_RUN_PRIMARY);
		addAction(getActionString(R.string.action_clipboard, Const.LAYOUT_PRIMARY), ActionId.CLIPBOARD_PRIMARY);
		
		if (isSecondaryLayoutEnabled) {
			addAction(getActionString(R.string.action_type_user_tab_pass, Const.LAYOUT_SECONDARY), ActionId.USER_TAB_PASS_SECONDARY);
			addAction(getActionString(R.string.action_type_user_tab_pass_enter, Const.LAYOUT_SECONDARY), ActionId.USER_TAB_PASS_ENTER_SECONDARY);
			addAction(getActionString(R.string.action_masked_password, Const.LAYOUT_SECONDARY), ActionId.MASKED_PASS_SECONDARY);
			addAction(getActionString(R.string.action_macro_run, Const.LAYOUT_SECONDARY), ActionId.MACRO_RUN_SECONDARY);
			addAction(getActionString(R.string.action_template_run, Const.LAYOUT_SECONDARY), ActionId.TEMPLATE_RUN_SECONDARY);
			addAction(getActionString(R.string.action_clipboard, Const.LAYOUT_SECONDARY), ActionId.CLIPBOARD_SECONDARY);
		}
		
		listViewActions.setAdapter(listAdapter);
		listViewActions.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
				boolean finish = true;
				
				Intent serviceIntent = new Intent(AllActionsActivity.this, InputStickService.class);
				serviceIntent.setAction(Const.SERVICE_ENTRY_ACTION); 
				
				ActionId actionId = actionsLUT.get(pos);
				
				
				
				switch (actionId) {
					//general:
					case OPEN_SETTINGS:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_SETTINGS);
						break;
					case CONNECT:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_CONNECT);
						break;
					case DISCONNECT:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_DISCONNECT);   
						break;		
					case REMOTE:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_REMOTE);   
						break;								
					case MAC_SETUP:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MAC_SETUP);
						break;		
					case TYPE_TAB:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_TAB);   
						finish = false;
						break;		
					case TYPE_ENTER:							
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_ENTER);
						finish = false;
						break;
					case MACRO_ADD_EDIT:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MACRO_ADDEDIT);  
						break;
					case TEMPLATE_MANAGE:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_TEMPLATE_MANAGE);
						break;							
					//quick shortcuts
					case QUICK_SHORTCUT_1:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_QUICK_SHORTCUT_1);
						finish = false;
						break;
					case QUICK_SHORTCUT_2:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_QUICK_SHORTCUT_2);
						finish = false;
						break;
					case QUICK_SHORTCUT_3:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_QUICK_SHORTCUT_3);
						finish = false;
						break;							
					//entry, primary layout
					case USER_TAB_PASS_PRIMARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_USER_PASS);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);
						break;				
					case USER_TAB_PASS_ENTER_PRIMARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_USER_PASS_ENTER);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);
						break;	
					case MASKED_PASS_PRIMARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MASKED_PASSWORD);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);		
						break;		
					case MACRO_RUN_PRIMARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MACRO_RUN);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);		
						break;	
					case TEMPLATE_RUN_PRIMARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_TEMPLATE_RUN);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);	
						break;															
					case CLIPBOARD_PRIMARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_CLIPBOARD);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, primaryLayoutCode);
						break;
					//entry, secondary layout							
					case USER_TAB_PASS_SECONDARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_USER_PASS);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
						break;				
					case USER_TAB_PASS_ENTER_SECONDARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_USER_PASS_ENTER);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
						break;	
					case MASKED_PASS_SECONDARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MASKED_PASSWORD);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
						break;		
					case MACRO_RUN_SECONDARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_MACRO_RUN);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
						break;
					case TEMPLATE_RUN_SECONDARY:
						serviceIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_TEMPLATE_RUN);
						serviceIntent.putExtra(Const.EXTRA_LAYOUT, secondaryLayoutCode);
						break;								
					case CLIPBOARD_SECONDARY:
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
		});		
	}

	
	private void addAction(String label, ActionId actionId) {
		listAdapter.add(label);
		actionsLUT.add(actionId);
		
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
