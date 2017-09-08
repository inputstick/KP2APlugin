package com.inputstick.apps.kp2aplugin;

import keepass2android.pluginsdk.PluginAccessException;
import keepass2android.pluginsdk.Strings;
import sheetrock.panda.changelog.ChangeLog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

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
		Context ctx = null;		
		String scope = null;
		SharedPreferences prefs = null; 
		try {			
			ctx = oe.getContext();
			prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			scope = oe.getScope();
			String tmp, tmpSecondary;			
			
			loadPreferences(prefs);
			String token = oe.getAccessTokenForCurrentEntryScope();
			
			//primary layout:			
			tmp = PreferencesHelper.getFieldItemsForPrimaryLayout(prefs);
			tmpSecondary = PreferencesHelper.getFieldItemsForSecondaryLayout(prefs);
			for (String field: oe.getEntryFields().keySet()) {
				//primary layout
				if (PreferencesHelper.isTypeActionEnabled(tmp)) {
					addFieldAction(oe, Const.ACTION_FIELD_TYPE_PRIMARY, Strings.PREFIX_STRING + field, false, Const.LAYOUT_PRIMARY, token);
				}
				if (PreferencesHelper.isTypeSlowActionEnabled(tmp)) {
					addFieldAction(oe, Const.ACTION_FIELD_TYPE_SLOW_PRIMARY, Strings.PREFIX_STRING + field, true, Const.LAYOUT_PRIMARY, token);
				}					
				//secondary layout
				if (isSecondaryLayoutEnabled) {
					if (PreferencesHelper.isTypeActionEnabled(tmpSecondary)) {	
						addFieldAction(oe, Const.ACTION_FIELD_TYPE_SECONDARY, Strings.PREFIX_STRING + field, false, Const.LAYOUT_SECONDARY, token);
					}
					if (PreferencesHelper.isTypeSlowActionEnabled(tmpSecondary)) {
						addFieldAction(oe, Const.ACTION_FIELD_TYPE_SLOW_SECONDARY, Strings.PREFIX_STRING + field, true, Const.LAYOUT_SECONDARY, token);
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
		} catch (PluginAccessException e) {
			e.printStackTrace();
		}				

		if (ctx != null) {					
			if ( !InputStickService.isRunning) {
				Intent serviceIntent = new Intent(ctx, InputStickService.class);
				serviceIntent.setAction(Const.SERVICE_START);
				serviceIntent.putExtras(EntryData.getBundle(oe.getEntryId(), oe.getEntryFields()));
				ctx.startService(serviceIntent);
			}

			ChangeLog cl = new ChangeLog(ctx.getApplicationContext());
			if (cl.firstRun()) {				
				if ( !cl.getThisVersion().equals(Const.SKIP_CHANGELOG_APP_VERSION)) {
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
		oe.addEntryAction(displayText, Const.IC, b, accessToken);
	}
	
	private void addEntryAction(OpenEntryAction oe, int nameResId, String action, int layoutType, String accessToken) throws PluginAccessException {
		addEntryAction(oe, oe.getContext().getString(nameResId), action, layoutType, accessToken);
	}
	
	private void addFieldAction(OpenEntryAction oe, String actionId, String fieldId, boolean slowTyping, int layoutType, String accessToken) throws PluginAccessException {
		int nameResId;		
		Bundle b = new Bundle();	
		if (slowTyping) {
			b.putBoolean(Const.EXTRA_TYPE_SLOW, true);
			nameResId = R.string.action_type_slow;
		} else {
			nameResId = R.string.action_type;
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
		oe.addEntryFieldAction(actionId, fieldId, displayText, Const.IC, b, accessToken);
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
		//in case service was stopped after opening entry but before selecting an action
		Log.d(_TAG, "actionSelected");
		if ( !InputStickService.isRunning) {
			Intent serviceIntent = new Intent(actionSelected.getContext(), InputStickService.class);
			serviceIntent.setAction(Const.SERVICE_RESTART);
			actionSelected.getContext().startService(serviceIntent);		
		}
	}		

		

	@Override
	protected void entryOutputModified(EntryOutputModifiedAction eom) {
		try {				
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(eom.getContext());			
			loadPreferences(prefs);
			String token = eom.getAccessTokenForCurrentEntryScope();
			
			//primary layout:			
			String tmp = PreferencesHelper.getFieldItemsForPrimaryLayout(prefs);
			if (PreferencesHelper.isTypeActionEnabled(tmp)) {
				addFieldAction(eom, Const.ACTION_FIELD_TYPE_PRIMARY, eom.getModifiedFieldId(), false, Const.LAYOUT_PRIMARY, token);
			}			
			if (PreferencesHelper.isTypeSlowActionEnabled(tmp)) {
				addFieldAction(eom, Const.ACTION_FIELD_TYPE_SLOW_PRIMARY, eom.getModifiedFieldId(), true, Const.LAYOUT_PRIMARY, token);
			}
			//secondary layout:
			if (isSecondaryLayoutEnabled) {
				tmp = PreferencesHelper.getFieldItemsForSecondaryLayout(prefs);
				if (PreferencesHelper.isTypeActionEnabled(tmp)) {
					addFieldAction(eom, Const.ACTION_FIELD_TYPE_SECONDARY, eom.getModifiedFieldId(), false, Const.LAYOUT_SECONDARY, token);
				}
				if (PreferencesHelper.isTypeSlowActionEnabled(tmp)) {
					addFieldAction(eom, Const.ACTION_FIELD_TYPE_SLOW_SECONDARY, eom.getModifiedFieldId(), true, Const.LAYOUT_SECONDARY, token);				
				}
			}
		} catch (PluginAccessException e) {
			e.printStackTrace();
		}
	}


}
