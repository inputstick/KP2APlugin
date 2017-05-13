package com.inputstick.apps.kp2aplugin;

import keepass2android.pluginsdk.PluginAccessException;
import keepass2android.pluginsdk.Strings;
import sheetrock.panda.changelog.ChangeLog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.hid.HIDKeycodes;

public class ActionReceiver extends keepass2android.pluginsdk.PluginActionBroadcastReceiver {
	
	private static final int LAYOUT_NONE = 0;
	private static final int LAYOUT_PRIMARY = 1;
	private static final int LAYOUT_SECONDARY = 2;	
	
	private static final String ACTION_SHOW_ALL = "com.inputstick.apps.kp2aplugin.show_all";
	private static final String ACTION_MASKED_PASSWORD = "com.inputstick.apps.kp2aplugin.masked_password";
	private static final String ACTION_SETTINGS = "com.inputstick.apps.kp2aplugin.settings";
	private static final String ACTION_CONNECT = "com.inputstick.apps.kp2aplugin.connect";
	private static final String ACTION_DISCONNECT = "com.inputstick.apps.kp2aplugin.disconnect";
	private static final String ACTION_USER_PASS = "com.inputstick.apps.kp2aplugin.user_pass";
	private static final String ACTION_USER_PASS_ENTER = "com.inputstick.apps.kp2aplugin.user_pass_enter";
	private static final String ACTION_MAC_SETUP = "com.inputstick.apps.kp2aplugin.mac_setup";
	private static final String ACTION_TAB = "com.inputstick.apps.kp2aplugin.tab";
	private static final String ACTION_ENTER = "com.inputstick.apps.kp2aplugin.enter";
	
	private static final String ACTION_MACRO_ADDEDIT = "com.inputstick.apps.kp2aplugin.macro_addedit";	
	private static final String ACTION_CLIPBOARD = "com.inputstick.apps.kp2aplugin.clipboard";	
	private static final String ACTION_MACRO_RUN = "com.inputstick.apps.kp2aplugin.macro_run";
	private static final String ACTION_TEMPLATE_RUN = "com.inputstick.apps.kp2aplugin.template_run";
	private static final String ACTION_TEMPLATE_MANAGE = "com.inputstick.apps.kp2aplugin.template_manage";	
	
	private static final String ACTION_FIELD_TYPE_PRIMARY = "com.inputstick.apps.kp2aplugin.type";
	private static final String ACTION_FIELD_TYPE_SLOW_PRIMARY = "com.inputstick.apps.kp2aplugin.type_slow";
	private static final String ACTION_FIELD_TYPE_SECONDARY = "com.inputstick.apps.kp2aplugin.types_econdary";
	private static final String ACTION_FIELD_TYPE_SLOW_SECONDARY = "com.inputstick.apps.kp2aplugin.type_slow_secondary";
	
	private static final int IC = R.drawable.ic_launcher;

	private static ActionManager actionManager;
	
	@Override
	protected void openEntry(OpenEntryAction oe) {
		Context ctx = null;
		UserPreferences userPrefs = null;
		try {			
			ctx = oe.getContext();
			actionManager = ActionManager.getInstance(ctx, oe.getEntryId(), oe.getEntryFields());
			userPrefs = actionManager.getUserPrefs();	
			
			for (String field: oe.getEntryFields().keySet()) {
				//primary layout
				if (userPrefs.isShowType(true)) {
					addEntryFieldTypeAction(oe, ACTION_FIELD_TYPE_PRIMARY, Strings.PREFIX_STRING + field, false, LAYOUT_PRIMARY);
				}
				if (userPrefs.isShowTypeSlow(true)) {
					addEntryFieldTypeAction(oe, ACTION_FIELD_TYPE_SLOW_PRIMARY, Strings.PREFIX_STRING + field, true, LAYOUT_PRIMARY);
				}					
				//secondary layout
				if (userPrefs.isShowType(false)) {			
					addEntryFieldTypeAction(oe, ACTION_FIELD_TYPE_SECONDARY, Strings.PREFIX_STRING + field, false, LAYOUT_SECONDARY);
				}
				if (userPrefs.isShowTypeSlow(false)) {					
					addEntryFieldTypeAction(oe, ACTION_FIELD_TYPE_SLOW_SECONDARY, Strings.PREFIX_STRING + field, true, LAYOUT_SECONDARY);
				}
			}
			
			//always add "all actions"
			addEntryAction(oe, R.string.action_show_all, ACTION_SHOW_ALL, LAYOUT_NONE);		
			
			//general items
			if (userPrefs.isShowSettings()) {
				addEntryAction(oe, R.string.action_open_settings, ACTION_SETTINGS, LAYOUT_NONE);		
			}
			if (userPrefs.isShowConnectionOptions()) {
				addEntryAction(oe, R.string.action_connect, ACTION_CONNECT, LAYOUT_NONE);		
				addEntryAction(oe, R.string.action_disconnect, ACTION_DISCONNECT, LAYOUT_NONE);		
			}		
			
			if (userPrefs.isShowMacSetup()) {
				addEntryAction(oe, R.string.action_open_mac_setup, ACTION_MAC_SETUP, LAYOUT_NONE);		
			}			
			if (userPrefs.isShowTabEnter()) {
				addEntryAction(oe, R.string.action_type_tab, ACTION_TAB, LAYOUT_NONE);						
				addEntryAction(oe, R.string.action_type_enter, ACTION_ENTER, LAYOUT_NONE);		
			}
			if (userPrefs.isShowMacroAddEdit()) {
				addEntryAction(oe, R.string.action_macro_add_edit, ACTION_MACRO_ADDEDIT, LAYOUT_NONE);		
			}
			if (userPrefs.isShowTemplateManage()) {
				addEntryAction(oe, R.string.action_template_manage, ACTION_TEMPLATE_MANAGE, LAYOUT_NONE);		
			}				

			//entry items, primary layout 
			if (userPrefs.isShowUserPass(true)) {
				addEntryAction(oe, R.string.action_type_user_tab_pass, ACTION_USER_PASS, LAYOUT_PRIMARY);		
			}			
			if (userPrefs.isShowUserPassEnter(true)) {
				addEntryAction(oe, R.string.action_type_user_tab_pass_enter, ACTION_USER_PASS_ENTER, LAYOUT_PRIMARY);		
			}				
			if (userPrefs.isShowMasked(true)) {
				addEntryAction(oe, R.string.action_masked_password, ACTION_MASKED_PASSWORD, LAYOUT_PRIMARY);		
			}	
			if (userPrefs.isShowMacro(true)) {
				addEntryAction(oe, R.string.action_macro_run, ACTION_MACRO_RUN, LAYOUT_PRIMARY);		
			}
			if (userPrefs.isShowRunTemplate(true)) {
				addEntryAction(oe, R.string.action_template_run, ACTION_TEMPLATE_RUN, LAYOUT_PRIMARY);		
			} 
			
			if (userPrefs.isShowClipboard(true)) {
				addEntryAction(oe, getTextForClipboardAction(ctx, userPrefs), ACTION_CLIPBOARD, LAYOUT_PRIMARY);		
			}				
			
			//entry items, secondary layout 
			if (userPrefs.isShowUserPass(false)) {
				addEntryAction(oe, R.string.action_type_user_tab_pass, ACTION_USER_PASS, LAYOUT_SECONDARY);				
			}
			if (userPrefs.isShowUserPassEnter(false)) {
				addEntryAction(oe, R.string.action_type_user_tab_pass_enter, ACTION_USER_PASS_ENTER, LAYOUT_SECONDARY);	
			}				
			if (userPrefs.isShowMasked(false)) {
				addEntryAction(oe, R.string.action_masked_password, ACTION_MASKED_PASSWORD, LAYOUT_SECONDARY);			
			}		
			if (userPrefs.isShowMacro(false)) {
				addEntryAction(oe, R.string.action_macro_run, ACTION_MACRO_RUN, LAYOUT_SECONDARY);
			}	
			if (userPrefs.isShowRunTemplate(false)) {
				addEntryAction(oe, R.string.action_template_run, ACTION_TEMPLATE_RUN, LAYOUT_SECONDARY);
			}			
			if (userPrefs.isShowClipboard(false)) {				
				addEntryAction(oe, getTextForClipboardAction(ctx, userPrefs), ACTION_CLIPBOARD, LAYOUT_SECONDARY);	
			}			
		} catch (PluginAccessException e) {
			e.printStackTrace();
		}				

		if (ctx != null) {
			if (userPrefs != null) {
				if (userPrefs.isAutoConnect()) {
					actionManager.connect();
				} else {	
					if ((ActionManager.lastActivityTime != 0) && ((System.currentTimeMillis() - userPrefs.getAutoConnectTimeout()) < ActionManager.lastActivityTime)) {
						actionManager.connect();
					} 
				}				
			}
			
			
			ChangeLog cl = new ChangeLog(ctx.getApplicationContext());
			if (cl.firstRun()) {
				Intent i = new Intent(ctx.getApplicationContext(), SettingsActivity.class);
				i.putExtra(Const.EXTRA_SHOW_CHANGELOG, true);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.getApplicationContext().startActivity(i);			
		    }
		}
	}	
	
	private String getTextForClipboardAction(Context ctx, UserPreferences userPrefs) {
		String actionName = ctx.getString(R.string.action_clipboard);
		if (userPrefs.isClipboardLaunchAuthenticator()) {
			actionName += "/Authenticator";
		} else if (userPrefs.isClipboardLaunchCustomApp()) {
			actionName += "/" + userPrefs.getClipboardCustomAppName();
		}
		return actionName;
	}

	private void addEntryAction(OpenEntryAction oe, String actionText, String action, int layoutType) throws PluginAccessException {
		actionManager = ActionManager.getInstance(oe.getContext(), oe.getEntryId(), oe.getEntryFields());
		UserPreferences userPrefs = actionManager.getUserPrefs();
		
		Bundle b = new Bundle();
		b.putString(Const.SELECTED_UI_ACTION, action);		
		String displayText;
		if (layoutType == LAYOUT_PRIMARY) {
			b.putString(Const.EXTRA_LAYOUT, userPrefs.getLayoutPrimary());
			displayText = actionManager.getActionStringForPrimaryLayout(actionText, true);
		} else if (layoutType == LAYOUT_SECONDARY) {
			b.putString(Const.EXTRA_LAYOUT, userPrefs.getLayoutSecondary());
			displayText = actionManager.getActionStringForSecondaryLayout(actionText, true);
		} else {
			displayText = actionManager.getActionString(actionText, true);
		}
		oe.addEntryAction(displayText, IC, b);
	}
	
	private void addEntryAction(OpenEntryAction oe, int nameResId, String action, int layoutType) throws PluginAccessException {
		addEntryAction(oe, oe.getContext().getString(nameResId), action, layoutType);
		/*actionManager = ActionManager.getInstance(oe.getContext(), oe.getEntryId(), oe.getEntryFields());
		UserPreferences userPrefs = actionManager.getUserPrefs();
		
		Bundle b = new Bundle();
		b.putString(Const.SELECTED_UI_ACTION, action);		
		String displayText;
		if (layoutType == LAYOUT_PRIMARY) {
			b.putString(Const.EXTRA_LAYOUT, userPrefs.getLayoutPrimary());
			displayText = actionManager.getActionStringForPrimaryLayout(nameResId, true);
		} else if (layoutType == LAYOUT_SECONDARY) {
			b.putString(Const.EXTRA_LAYOUT, userPrefs.getLayoutSecondary());
			displayText = actionManager.getActionStringForSecondaryLayout(nameResId, true);
		} else {
			displayText = actionManager.getActionString(nameResId, true);
		}
		oe.addEntryAction(displayText, IC, b);	*/
	}
	
	private void addEntryFieldTypeAction(OpenEntryAction oe, String actionId, String fieldId, boolean slowTyping, int layoutType) throws PluginAccessException {
		actionManager = ActionManager.getInstance(oe.getContext(), oe.getEntryId(), oe.getEntryFields());
		UserPreferences userPrefs = actionManager.getUserPrefs();		
		
		int nameResId;		
		Bundle b = new Bundle();	
		if (slowTyping) {
			b.putBoolean(Const.EXTRA_TYPE_SLOW, true);
			nameResId = R.string.action_type_slow;
		} else {
			nameResId = R.string.action_type;
		}
		String displayText;
		if (layoutType == LAYOUT_SECONDARY) {
			b.putString(Const.EXTRA_LAYOUT, userPrefs.getLayoutSecondary());
			displayText = actionManager.getActionStringForSecondaryLayout(nameResId, true);
		} else {
			b.putString(Const.EXTRA_LAYOUT, userPrefs.getLayoutPrimary());
			displayText = actionManager.getActionStringForPrimaryLayout(nameResId, true);
		}
		oe.addEntryFieldAction(actionId, fieldId, displayText, IC, b);
	}
	
	
	
	@Override 
	protected void closeEntryView(CloseEntryViewAction closeEntryView) {
		actionManager = ActionManager.getInstance(closeEntryView.getContext());
		try {
			if ( !InputStickHID.isConnected()) {
				ActionManager.lastActivityTime = 0;			
			} 
			
			UserPreferences userPrefs = actionManager.getUserPrefs();		
			if ((userPrefs != null) && (userPrefs.isDisconnectOnClose())) {		
				actionManager.disconnect();
			}	
		} catch (Exception e) {			
		}
		actionManager.onEntryClosed();
	};
	
	@Override
	protected void actionSelected(ActionSelectedAction actionSelected) {
		actionManager = ActionManager.getInstance(actionSelected.getContext(), actionSelected.getEntryId(), actionSelected.getEntryFields());
		UserPreferences userPrefs = actionManager.getUserPrefs();
		
		String layoutName = actionSelected.getActionData().getString(Const.EXTRA_LAYOUT, "en-US");		
		if (actionSelected.isEntryAction()) {
			String text = actionSelected.getActionData().getString(Const.SELECTED_UI_ACTION);
						
			if (ACTION_MASKED_PASSWORD.equals(text)) {
				actionManager.openMaskedPassword(layoutName, true);
			} else if (ACTION_SETTINGS.equals(text)) {
				actionManager.startSettingsActivity();
			} else if (ACTION_SHOW_ALL.equals(text)) {
				actionManager.startShowAllActivity();
			} else if (ACTION_USER_PASS.equals(text)) {
				actionManager.typeUsernameAndPassword(layoutName, false);
			} else if (ACTION_USER_PASS_ENTER.equals(text)) {
				actionManager.typeUsernameAndPassword(layoutName, true);
			} else if (ACTION_MAC_SETUP.equals(text)) {
				actionManager.startMacSetupActivity();
			} else if (ACTION_MACRO_ADDEDIT.equals(text)) {	
				actionManager.addEditMacro(false, false, 0);
			} else if (ACTION_CLIPBOARD.equals(text)) {	
				actionManager.clipboardTyping(layoutName);
			} else if (ACTION_MACRO_RUN.equals(text)) {
				actionManager.runMacro(layoutName);
			} else if (ACTION_TAB.equals(text)) {
				actionManager.queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_TAB);
			} else if (ACTION_ENTER.equals(text)) {
				actionManager.queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER);
			} else if (ACTION_CONNECT.equals(text)) {
				actionManager.connect();
			} else if (ACTION_DISCONNECT.equals(text)) {
				actionManager.disconnect();
			} else if (ACTION_TEMPLATE_RUN.equals(text)) {
				actionManager.startSelectTemplateActivity(layoutName, false);
			} else if (ACTION_TEMPLATE_MANAGE.equals(text)) {
				actionManager.startSelectTemplateActivity(layoutName, true);
			} 
			
		} else {
			//field actions: type/type slow
			boolean typeSlow = actionSelected.getActionData().getBoolean(Const.EXTRA_TYPE_SLOW, false);
			String fieldKey = actionSelected.getFieldId().substring(Strings.PREFIX_STRING.length());
			String text = actionSelected.getEntryFields().get(fieldKey);
			if (typeSlow) {
				actionManager.queueText(text, layoutName, Const.SLOW_TYPING_MULTIPLIER);
			} else {
				actionManager.queueText(text, layoutName);
			}
			
			if ((userPrefs.isEnterAfterURL()) && ("URL".equals(fieldKey))) {
				actionManager.queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER);
			}			
		}
	}		

		

	@Override
	protected void entryOutputModified(EntryOutputModifiedAction eom) {	
		actionManager = ActionManager.getInstance(eom.getContext(), eom.getEntryId(), eom.getEntryFields());
		UserPreferences userPrefs = actionManager.getUserPrefs();
		
		try {						
			//primary layout:
			if (userPrefs.isShowType(true)) {
				addEntryFieldTypeAction(eom, ACTION_FIELD_TYPE_PRIMARY, eom.getModifiedFieldId(), false, LAYOUT_PRIMARY);
			}			
			if (userPrefs.isShowTypeSlow(true)) {
				addEntryFieldTypeAction(eom, ACTION_FIELD_TYPE_SLOW_PRIMARY, eom.getModifiedFieldId(), true, LAYOUT_PRIMARY);
			}
			//secondary layout:
			if (userPrefs.isShowType(false)) {
				addEntryFieldTypeAction(eom, ACTION_FIELD_TYPE_SECONDARY, eom.getModifiedFieldId(), false, LAYOUT_SECONDARY);
			}
			if (userPrefs.isShowTypeSlow(false)) {
				addEntryFieldTypeAction(eom, ACTION_FIELD_TYPE_SLOW_SECONDARY, eom.getModifiedFieldId(), true, LAYOUT_SECONDARY);				
			}
		} catch (PluginAccessException e) {
			e.printStackTrace();
		}
	}


}
