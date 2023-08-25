package com.inputstick.apps.kp2aplugin;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import com.inputstick.api.hid.HIDKeycodes;

import java.util.HashMap;
import java.util.Set;

import keepass2android.pluginsdk.KeepassDefs;
import keepass2android.pluginsdk.PluginAccessException;
import keepass2android.pluginsdk.Strings;
import sheetrock.panda.changelog.ChangeLog;

public class ActionReceiver extends keepass2android.pluginsdk.PluginActionBroadcastReceiver {
	
	private static final String _TAG = "KP2AINPUTSTICK ACTIONRECEIVER";			 

	private static boolean displayInputStickText; 
	private static boolean isSecondaryLayoutEnabled;
	private static String primaryLayoutCode;
	private static String secondaryLayoutCode;
	
	private void loadPreferences(SharedPreferences prefs) {
		displayInputStickText = PreferencesHelper.inputStickTextEnabled(prefs);	
		isSecondaryLayoutEnabled = PreferencesHelper.isSecondaryLayoutEnabled(prefs);			
		primaryLayoutCode = PreferencesHelper.getPrimaryLayoutCode(prefs);
		secondaryLayoutCode = PreferencesHelper.getSecondaryLayoutCode(prefs);
	}
	
	@Override
	protected void openEntry(OpenEntryAction oe) {
		Context ctx = oe.getContext();
		String scope = null;
		SharedPreferences prefs = null;

		//special case for Android 33+ and no POST_NOTIFICATIONS permission (it is not possible to startForegroundService)
		if ( !canStartService(ctx)) {
			try {
				String token = oe.getAccessTokenForCurrentEntryScope();
				Bundle b;
				if (canDisplayActivity(ctx)) {
					//it is possible to open PermissionsActivity
					for (String field : oe.getEntryFields().keySet()) {
						oe.addEntryFieldAction(Const.ACTION_NOTIFICATIONS_PERMISSION, Strings.PREFIX_STRING + field, ctx.getString(R.string.permissions_message_config), R.drawable.ic_config, null, token);
					}
					//b = new Bundle();
					//b.putString(Const.EXTRA_ACTION, Const.ACTION_NOTIFICATIONS_PERMISSION);
					oe.addEntryAction(ctx.getString(R.string.permissions_message_config), R.drawable.ic_config, null, token);
				} else {
					//not possible to start PermissionsActivity - as a workaround ask user to open the app manually. Text must be split into multiple actions to show entire message
					for (String field : oe.getEntryFields().keySet()) {
						oe.addEntryFieldAction(Const.ACTION_NOTIFICATIONS_PERMISSION + "1", Strings.PREFIX_STRING + field, ctx.getString(R.string.permissions_message_line1), R.drawable.ic_step_1, null, token);
						oe.addEntryFieldAction(Const.ACTION_NOTIFICATIONS_PERMISSION + "2", Strings.PREFIX_STRING + field, ctx.getString(R.string.permissions_message_line2), R.drawable.ic_step_2, null, token);
						oe.addEntryFieldAction(Const.ACTION_NOTIFICATIONS_PERMISSION + "3", Strings.PREFIX_STRING + field, ctx.getString(R.string.permissions_message_line3), R.drawable.ic_step_3, null, token);
					}
					oe.addEntryAction(ctx.getString(R.string.permissions_message_line1), R.drawable.ic_step_1, null, token);
					oe.addEntryAction(ctx.getString(R.string.permissions_message_line2), R.drawable.ic_step_2, null, token);
					oe.addEntryAction(ctx.getString(R.string.permissions_message_line3), R.drawable.ic_step_3, null, token);
				}
			} catch (PluginAccessException e) {
				e.printStackTrace();
			}
			return; //IMPORTANT! startForegroundService() will throw an exception
		}

		try {
			prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			scope = oe.getScope();
			String tmp, tmpSecondary;			
			
			loadPreferences(prefs);
			String token = oe.getAccessTokenForCurrentEntryScope();

			//show debug toast?
			if (PreferencesHelper.showDebugMessages(prefs)) {
				Toast.makeText(ctx, "KP2A-IS: Open entry", Toast.LENGTH_SHORT).show();
			}
			
			//primary layout:			
			tmp = PreferencesHelper.getFieldItemsForPrimaryLayout(prefs);
			tmpSecondary = PreferencesHelper.getFieldItemsForSecondaryLayout(prefs);

			for (String field : oe.getEntryFields().keySet()) {
				//primary layout
				if (PreferencesHelper.isTypeActionEnabled(tmp)) {
					addFieldAction(oe, Const.ACTION_FIELD_TYPE_PRIMARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE, Const.LAYOUT_PRIMARY, token);
				}
				if (PreferencesHelper.isTypeAndEnterActionEnabled(tmp)) {
					addFieldAction(oe, Const.ACTION_FIELD_TYPE_ENTER_PRIMARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE_ENTER, Const.LAYOUT_PRIMARY, token);
				}	
				if (PreferencesHelper.isTypeAndTabActionEnabled(tmp)) {
					addFieldAction(oe, Const.ACTION_FIELD_TYPE_TAB_PRIMARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE_TAB, Const.LAYOUT_PRIMARY, token);
				}				
				if (PreferencesHelper.isTypeSlowActionEnabled(tmp)) {
					addFieldAction(oe, Const.ACTION_FIELD_TYPE_SLOW_PRIMARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE_SLOW, Const.LAYOUT_PRIMARY, token);
				}		
				//for password field only
				if (KeepassDefs.PasswordField.equals(field) && PreferencesHelper.isTypeMaskedActionEnabled(tmp)) {
					addFieldAction(oe, Const.ACTION_FIELD_TYPE_MASKED_PRIMARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE_MASKED, Const.LAYOUT_PRIMARY, token);
				}
				//secondary layout
				if (isSecondaryLayoutEnabled) {					
					if (PreferencesHelper.isTypeActionEnabled(tmpSecondary)) {	
						addFieldAction(oe, Const.ACTION_FIELD_TYPE_SECONDARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE, Const.LAYOUT_SECONDARY, token);
					}
					if (PreferencesHelper.isTypeAndEnterActionEnabled(tmpSecondary)) {
						addFieldAction(oe, Const.ACTION_FIELD_TYPE_ENTER_SECONDARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE_ENTER, Const.LAYOUT_SECONDARY, token);
					}	
					if (PreferencesHelper.isTypeAndTabActionEnabled(tmpSecondary)) {
						addFieldAction(oe, Const.ACTION_FIELD_TYPE_TAB_SECONDARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE_TAB, Const.LAYOUT_SECONDARY, token);
					}					
					if (PreferencesHelper.isTypeSlowActionEnabled(tmpSecondary)) {
						addFieldAction(oe, Const.ACTION_FIELD_TYPE_SLOW_SECONDARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE_SLOW, Const.LAYOUT_SECONDARY, token);
					}
					//for password field only
					if (KeepassDefs.PasswordField.equals(field) && PreferencesHelper.isTypeMaskedActionEnabled(tmpSecondary)) {
						addFieldAction(oe, Const.ACTION_FIELD_TYPE_MASKED_SECONDARY, Strings.PREFIX_STRING + field, Const.ITEM_TYPE_MASKED, Const.LAYOUT_SECONDARY, token);
					}
				}
			}
			
			//always add "all actions"
			addEntryAction(oe, R.string.action_show_all, Const.ACTION_SHOW_ALL, Const.LAYOUT_NONE, token);		
			
			
			//general items
			tmp = PreferencesHelper.getGeneralItems(prefs);
			
			if (PreferencesHelper.isSettingsActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_open_settings, Const.ACTION_SETTINGS, Const.LAYOUT_NONE, token);		
			}
			if (PreferencesHelper.isConnectionOptionsActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_connect, Const.ACTION_CONNECT, Const.LAYOUT_NONE, token);		
				addEntryAction(oe, R.string.action_disconnect, Const.ACTION_DISCONNECT, Const.LAYOUT_NONE, token);		
			}			
			if (PreferencesHelper.isRemoteActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_remote, Const.ACTION_REMOTE, Const.LAYOUT_NONE, token);		
			}
			if (PreferencesHelper.isMacSetupActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_open_mac_setup, Const.ACTION_MAC_SETUP, Const.LAYOUT_NONE, token);		
			}			
			if (PreferencesHelper.isTabEnterActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_type_tab, Const.ACTION_TAB, Const.LAYOUT_NONE, token);						
				addEntryAction(oe, R.string.action_type_enter, Const.ACTION_ENTER, Const.LAYOUT_NONE, token);		
			}
			if (PreferencesHelper.isMacroAddEditActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_macro_add_edit, Const.ACTION_MACRO_ADDEDIT, Const.LAYOUT_NONE, token);		
			}
			if (PreferencesHelper.isTemplateManageActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_template_manage, Const.ACTION_TEMPLATE_MANAGE, Const.LAYOUT_NONE, token);		
			}				

			//entry items, primary layout 
			tmp = PreferencesHelper.getEntryItemsForPrimaryLayout(prefs);
			
			if (PreferencesHelper.isUserPassActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_type_user_tab_pass, Const.ACTION_USER_PASS, Const.LAYOUT_PRIMARY, token);		
			}			
			if (PreferencesHelper.isUserPassEnterActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_type_user_tab_pass_enter, Const.ACTION_USER_PASS_ENTER, Const.LAYOUT_PRIMARY, token);		
			}				
			if (PreferencesHelper.isMaskedActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_masked_password, Const.ACTION_MASKED_PASSWORD, Const.LAYOUT_PRIMARY, token);		
			}	
			if (PreferencesHelper.isMacroActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_macro_run, Const.ACTION_MACRO_RUN, Const.LAYOUT_PRIMARY, token);		
			}
			if (PreferencesHelper.isRunTemplateActionEnabled(tmp)) {
				addEntryAction(oe, R.string.action_template_run, Const.ACTION_TEMPLATE_RUN, Const.LAYOUT_PRIMARY, token);		
			} 
			if (PreferencesHelper.isClipboardActionEnabled(tmp)) {
				addEntryAction(oe, getTextForClipboardAction(ctx, prefs), Const.ACTION_CLIPBOARD, Const.LAYOUT_PRIMARY, token);		
			}			
			
			//entry items, secondary layout 
			if (isSecondaryLayoutEnabled) {
			tmpSecondary = PreferencesHelper.getEntryItemsForSecondaryLayout(prefs);			
				if (PreferencesHelper.isUserPassActionEnabled(tmpSecondary)) {
					addEntryAction(oe, R.string.action_type_user_tab_pass, Const.ACTION_USER_PASS, Const.LAYOUT_SECONDARY, token);				
				}
				if (PreferencesHelper.isUserPassEnterActionEnabled(tmpSecondary)) {
					addEntryAction(oe, R.string.action_type_user_tab_pass_enter, Const.ACTION_USER_PASS_ENTER, Const.LAYOUT_SECONDARY, token);	
				}				
				if (PreferencesHelper.isMaskedActionEnabled(tmpSecondary)) {
					addEntryAction(oe, R.string.action_masked_password, Const.ACTION_MASKED_PASSWORD, Const.LAYOUT_SECONDARY, token);			
				}		
				if (PreferencesHelper.isMacroActionEnabled(tmpSecondary)) {
					addEntryAction(oe, R.string.action_macro_run, Const.ACTION_MACRO_RUN, Const.LAYOUT_SECONDARY, token);
				}	
				if (PreferencesHelper.isRunTemplateActionEnabled(tmpSecondary)) {
					addEntryAction(oe, R.string.action_template_run, Const.ACTION_TEMPLATE_RUN, Const.LAYOUT_SECONDARY, token);
				}			
				if (PreferencesHelper.isClipboardActionEnabled(tmpSecondary)) {			
					addEntryAction(oe, getTextForClipboardAction(ctx, prefs), Const.ACTION_CLIPBOARD, Const.LAYOUT_SECONDARY, token);	
				}			
			}
			
			//quick shortcuts; layout independent			
			int cnt = PreferencesHelper.getEnabledQuickShortcuts(prefs);
			if (cnt > 0) {
				tmp = PreferencesHelper.getQuickShortcut(prefs, 1);
				addEntryAction(oe, tmp, Const.ACTION_QUICK_SHORTCUT_1, Const.LAYOUT_NONE, token);
			}
			if (cnt > 1) {
				tmp = PreferencesHelper.getQuickShortcut(prefs, 2);
				addEntryAction(oe, tmp, Const.ACTION_QUICK_SHORTCUT_2, Const.LAYOUT_NONE, token);
			}
			if (cnt > 2) {
				tmp = PreferencesHelper.getQuickShortcut(prefs, 3);
				addEntryAction(oe, tmp, Const.ACTION_QUICK_SHORTCUT_3, Const.LAYOUT_NONE, token); 
			}						
		} catch (PluginAccessException e) {
			e.printStackTrace();
		}				


		if (ctx != null) {					
			if ( !InputStickService.isRunning) {
				Intent serviceIntent = new Intent(ctx, InputStickService.class);
				serviceIntent.setAction(Const.SERVICE_START);
				serviceIntent.putExtras(EntryData.getBundle(oe.getEntryId(), oe.getEntryFields()));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					ctx.startForegroundService(serviceIntent);
				} else {
					ctx.startService(serviceIntent);
				}
			}

			ChangeLog cl = new ChangeLog(ctx.getApplicationContext());
			if (cl.firstRun()) {				
				if (cl.getThisVersion().equals(Const.SHOW_CHANGELOG_APP_VERSION)) {
					Intent i = new Intent(ctx.getApplicationContext(), SettingsActivity.class);
					i.putExtra(Const.EXTRA_SHOW_CHANGELOG, true);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					ctx.getApplicationContext().startActivity(i);			
				}
			} else {
		    	//missing DB actions scope?
		    	if ((scope != null) && ( !scope.contains(Strings.SCOPE_DATABASE_ACTIONS))) {
		    		if (PreferencesHelper.canShowDbScopeDialog(prefs)) {
						Intent i = new Intent(ctx.getApplicationContext(), SettingsActivity.class);
						i.putExtra(Const.EXTRA_SHOW_SCOPE, true);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						ctx.getApplicationContext().startActivity(i);	
		    		}
		    	}
		    }
		}
	}	
	
	private String getTextForClipboardAction(Context ctx, SharedPreferences prefs) {
		String actionName = ctx.getString(R.string.action_clipboard);
		if (PreferencesHelper.isClipboardLaunchAuthenticator(prefs)) {
			actionName += "/Authenticator";
		} else if (PreferencesHelper.isClipboardLaunchCustomApp(prefs)) {
			actionName += "/" + PreferencesHelper.getClipboardCustomAppName(prefs);
		}
		return actionName;
	}

	private void addEntryAction(OpenEntryAction oe, String actionText, String action, int layoutType, String accessToken) throws PluginAccessException {		
		Bundle b = new Bundle();
		b.putString(Const.EXTRA_ACTION, action);		
		String displayText;
		if (layoutType == Const.LAYOUT_PRIMARY) {
			b.putString(Const.EXTRA_LAYOUT, primaryLayoutCode);
			displayText = getActionString(actionText, Const.LAYOUT_PRIMARY, true);
		} else if (layoutType == Const.LAYOUT_SECONDARY) {
			b.putString(Const.EXTRA_LAYOUT, secondaryLayoutCode);
			displayText = getActionString(actionText, Const.LAYOUT_SECONDARY, true);
		} else {
			displayText = getActionString(actionText, Const.LAYOUT_NONE, true);
		}
		oe.addEntryAction(displayText, R.drawable.ic_entry_action, b, accessToken);
	}
	
	private void addEntryAction(OpenEntryAction oe, int nameResId, String action, int layoutType, String accessToken) throws PluginAccessException {
		addEntryAction(oe, oe.getContext().getString(nameResId), action, layoutType, accessToken);
	}
	
	private void addFieldAction(OpenEntryAction oe, String actionId, String fieldId, String fieldItemType, int layoutType, String accessToken) throws PluginAccessException {
		int nameResId;		
		Bundle b = new Bundle();
		
		if (fieldItemType.equals(Const.ITEM_TYPE)) {
			nameResId = R.string.action_field_type;
		} else if (fieldItemType.equals(Const.ITEM_TYPE_ENTER)) {			
			b.putByte(Const.EXTRA_ADD_KEY, HIDKeycodes.KEY_ENTER);
			nameResId = R.string.action_field_type_enter;
		} else if (fieldItemType.equals(Const.ITEM_TYPE_TAB)) {
			b.putByte(Const.EXTRA_ADD_KEY, HIDKeycodes.KEY_TAB);
			nameResId = R.string.action_field_type_tab;			
		} else if (fieldItemType.equals(Const.ITEM_TYPE_SLOW)) {
			b.putBoolean(Const.EXTRA_TYPE_SLOW, true);
			nameResId = R.string.action_field_type_slow;
		} else if (fieldItemType.equals(Const.ITEM_TYPE_MASKED)) { 
			b.putBoolean(Const.EXTRA_TYPE_MASKED, true);
			nameResId = R.string.action_field_type_masked;
		} else {
			nameResId = R.string.action_field_type;
		}
		
		String actionText = oe.getContext().getString(nameResId);
		String displayText = null;		
		if (layoutType == Const.LAYOUT_PRIMARY) {
			b.putString(Const.EXTRA_LAYOUT, primaryLayoutCode);
			displayText = getActionString(actionText, Const.LAYOUT_PRIMARY, true);
		} else if (layoutType == Const.LAYOUT_SECONDARY) {
			b.putString(Const.EXTRA_LAYOUT, secondaryLayoutCode);
			displayText = getActionString(actionText, Const.LAYOUT_SECONDARY, true);
		}
		oe.addEntryFieldAction(actionId, fieldId, displayText, R.drawable.ic_filed_action, b, accessToken); //Const.IC
	}
	
	
	private String getActionString(String actionText, int actionLayoutType, boolean allowInputStickText) {
		String s = actionText;		
		if (actionLayoutType == Const.LAYOUT_PRIMARY && isSecondaryLayoutEnabled) {
			s += " (" + primaryLayoutCode + ")";
		}
		if (actionLayoutType == Const.LAYOUT_SECONDARY) {
			s += " (" + secondaryLayoutCode + ")";
		}
		if (allowInputStickText && displayInputStickText) {
			s += " (IS)";
		}
		return s;
	}
	
	
	@Override
	protected void actionSelected(ActionSelectedAction actionSelected) {
		//Log.d(_TAG, "actionSelected");
		Context ctx = actionSelected.getContext();
		if (canStartService(ctx)) {
			if ( !InputStickService.isRunning) {
				Intent serviceIntent = new Intent(actionSelected.getContext(), InputStickService.class);
				serviceIntent.setAction(Const.SERVICE_RESTART);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					ctx.startForegroundService(serviceIntent);
				} else {
					ctx.startService(serviceIntent);
				}
			}
		} else {
			//if possible, show activity to request POST_NOTIFICATIONS permission, otherwise nothing can be done
			if (canDisplayActivity(ctx)) {
				Intent i = new Intent(ctx.getApplicationContext(), PermissionsActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.getApplicationContext().startActivity(i);
			}
		}
	}		

		

	@Override
	protected void entryOutputModified(EntryOutputModifiedAction eom) {
		try {				
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(eom.getContext());			
			loadPreferences(prefs);
			String token = eom.getAccessTokenForCurrentEntryScope();
			String fieldId = eom.getModifiedFieldId();
			//primary layout:			
			String tmp = PreferencesHelper.getFieldItemsForPrimaryLayout(prefs);
			if (PreferencesHelper.isTypeActionEnabled(tmp)) {
				addFieldAction(eom, Const.ACTION_FIELD_TYPE_PRIMARY, fieldId, Const.ITEM_TYPE, Const.LAYOUT_PRIMARY, token);
			}		
			if (PreferencesHelper.isTypeAndEnterActionEnabled(tmp)) {
				addFieldAction(eom, Const.ACTION_FIELD_TYPE_ENTER_PRIMARY, fieldId, Const.ITEM_TYPE_ENTER, Const.LAYOUT_PRIMARY, token);
			}	
			if (PreferencesHelper.isTypeAndTabActionEnabled(tmp)) {
				addFieldAction(eom, Const.ACTION_FIELD_TYPE_TAB_PRIMARY, fieldId, Const.ITEM_TYPE_TAB, Const.LAYOUT_PRIMARY, token);
			}	
			if (PreferencesHelper.isTypeSlowActionEnabled(tmp)) {
				addFieldAction(eom, Const.ACTION_FIELD_TYPE_SLOW_PRIMARY, fieldId, Const.ITEM_TYPE_SLOW, Const.LAYOUT_PRIMARY, token);
			}
			//for password field only
			if (KeepassDefs.PasswordField.equals(fieldId) && PreferencesHelper.isTypeMaskedActionEnabled(tmp)) {
				addFieldAction(eom, Const.ACTION_FIELD_TYPE_MASKED_PRIMARY, fieldId, Const.ITEM_TYPE_MASKED, Const.LAYOUT_PRIMARY, token);
			}
			//secondary layout:
			if (isSecondaryLayoutEnabled) {
				tmp = PreferencesHelper.getFieldItemsForSecondaryLayout(prefs);
				if (PreferencesHelper.isTypeActionEnabled(tmp)) {
					addFieldAction(eom, Const.ACTION_FIELD_TYPE_SECONDARY, fieldId, Const.ITEM_TYPE, Const.LAYOUT_SECONDARY, token);
				}
				if (PreferencesHelper.isTypeAndEnterActionEnabled(tmp)) {
					addFieldAction(eom, Const.ACTION_FIELD_TYPE_ENTER_SECONDARY, fieldId, Const.ITEM_TYPE_ENTER, Const.LAYOUT_SECONDARY, token);
				}	
				if (PreferencesHelper.isTypeAndTabActionEnabled(tmp)) {
					addFieldAction(eom, Const.ACTION_FIELD_TYPE_TAB_SECONDARY, fieldId, Const.ITEM_TYPE_TAB, Const.LAYOUT_SECONDARY, token);
				}
				if (PreferencesHelper.isTypeSlowActionEnabled(tmp)) {
					addFieldAction(eom, Const.ACTION_FIELD_TYPE_SLOW_SECONDARY, fieldId, Const.ITEM_TYPE_SLOW, Const.LAYOUT_SECONDARY, token);				
				}
				//for password field only
				if (KeepassDefs.PasswordField.equals(fieldId) && PreferencesHelper.isTypeMaskedActionEnabled(tmp)) {
					addFieldAction(eom, Const.ACTION_FIELD_TYPE_MASKED_SECONDARY, fieldId, Const.ITEM_TYPE_MASKED, Const.LAYOUT_SECONDARY, token);
				}
			}
		} catch (PluginAccessException e) {
			e.printStackTrace();
		}
	}



	private boolean canStartService(Context ctx) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			return notificationManager.areNotificationsEnabled();
		}
		return true;
	}

	private boolean canDisplayActivity(Context ctx) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			return Settings.canDrawOverlays(ctx);
		}
		return true;
	}

}
