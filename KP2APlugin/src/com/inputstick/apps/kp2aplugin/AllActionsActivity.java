package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.inputstick.api.hid.HIDKeycodes;

public class AllActionsActivity extends Activity {
	
	private long lastActionTime;
	private long maxTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setTheme( android.R.style.Theme_Holo_Dialog);
		setContentView(R.layout.activity_all_actions);
		
		final ActionManager actionManager = ActionManager.getInstance(this);
		final UserPreferences userPrefs = actionManager.getUserPrefs();
		maxTime = getIntent().getLongExtra(Const.EXTRA_MAX_TIME, 0);
		lastActionTime = System.currentTimeMillis();

		ListView listViewActions = (ListView) findViewById(R.id.listViewActions);
		ArrayList<String> list = new ArrayList<String>();
		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.row, list);
		
		listAdapter.add(actionManager.getActionString(R.string.action_open_settings, false));
		listAdapter.add(actionManager.getActionString(R.string.action_connect, false));
		listAdapter.add(actionManager.getActionString(R.string.action_disconnect, false));
		listAdapter.add(actionManager.getActionString(R.string.action_open_mac_setup, false));
		listAdapter.add(actionManager.getActionString(R.string.action_type_tab, false));
		listAdapter.add(actionManager.getActionString(R.string.action_type_enter, false));
		listAdapter.add(actionManager.getActionString(R.string.action_macro_add_edit, false));
		listAdapter.add(actionManager.getActionString(R.string.action_template_manage, false));
		
		listAdapter.add(actionManager.getActionStringForPrimaryLayout(R.string.action_type_user_tab_pass, false));
		listAdapter.add(actionManager.getActionStringForPrimaryLayout(R.string.action_type_user_tab_pass_enter, false));
		listAdapter.add(actionManager.getActionStringForPrimaryLayout(R.string.action_masked_password, false));
		listAdapter.add(actionManager.getActionStringForPrimaryLayout(R.string.action_macro_run, false));
		listAdapter.add(actionManager.getActionStringForPrimaryLayout(R.string.action_template_run, false));
		listAdapter.add(actionManager.getActionStringForPrimaryLayout(R.string.action_clipboard, false));
		
		if (userPrefs.isShowSecondary()) {
			listAdapter.add(actionManager.getActionStringForSecondaryLayout(R.string.action_type_user_tab_pass, false));
			listAdapter.add(actionManager.getActionStringForSecondaryLayout(R.string.action_type_user_tab_pass_enter, false));
			listAdapter.add(actionManager.getActionStringForSecondaryLayout(R.string.action_masked_password, false));
			listAdapter.add(actionManager.getActionStringForSecondaryLayout(R.string.action_macro_run, false));
			listAdapter.add(actionManager.getActionStringForSecondaryLayout(R.string.action_template_run, false));
			listAdapter.add(actionManager.getActionStringForSecondaryLayout(R.string.action_clipboard, false));
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
					
					switch (pos) {
						//general:
						case 0:
							actionManager.startSettingsActivity();
							break;
						case 1:
							actionManager.connect();
							break;
						case 2:
							actionManager.disconnect();
							break;						
						case 3:
							actionManager.startMacSetupActivity();
							break;		
						case 4:
							actionManager.queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_TAB);
							break;		
						case 5:
							actionManager.queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER);
							break;
						case 6:
							actionManager.addEditMacro(false, false, 0);
							break;
						case 7:
							actionManager.startSelectTemplateActivity(userPrefs.getLayoutPrimary(), true);
							break;							
						//entry, primary layout
						case 8:
							actionManager.typeUsernameAndPassword(userPrefs.getLayoutPrimary(), false);
							break;				
						case 9:
							actionManager.typeUsernameAndPassword(userPrefs.getLayoutPrimary(), true);
							break;	
						case 10:
							actionManager.openMaskedPassword(userPrefs.getLayoutPrimary(), true);
							break;		
						case 11:
							actionManager.runMacro(userPrefs.getLayoutPrimary());
							break;	
						case 12:
							actionManager.startSelectTemplateActivity(userPrefs.getLayoutPrimary(), false);
							break;															
						case 13:
							actionManager.clipboardTyping(userPrefs.getLayoutPrimary());
							break;
						//entry, secondary layout
						case 14:
							actionManager.typeUsernameAndPassword(userPrefs.getLayoutSecondary(), false);
							break;				
						case 15:
							actionManager.typeUsernameAndPassword(userPrefs.getLayoutSecondary(), true);
							break;	
						case 16:
							actionManager.openMaskedPassword(userPrefs.getLayoutSecondary(), true);
							break;		
						case 17:
							actionManager.runMacro(userPrefs.getLayoutSecondary());
							break;
						case 18:
							actionManager.startSelectTemplateActivity(userPrefs.getLayoutSecondary(), false);
							break;								
						case 19:
							actionManager.clipboardTyping(userPrefs.getLayoutSecondary());
							break;							
					}
				}
			}			
		});
	}
	
	
}
