package com.inputstick.apps.kp2aplugin;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressWarnings("WeakerAccess")
public abstract class PreferencesHelper {
	
	//plugin setup:
	public static void setSetupCompleted(SharedPreferences prefs) {
		prefs.edit().putBoolean(Const.PREF_SETUP_COMPLETED, true).apply();
	}
	
	public static boolean canShowDbScopeDialog(SharedPreferences prefs) {
		return !prefs.getBoolean(Const.PREF_DO_NOT_REQUEST_DB_SCOPE, Const.PREF_DO_NOT_REQUEST_DB_VALUE);
	}	
	
	public static void disableDbScopeDialog(SharedPreferences prefs) {
		prefs.edit().putBoolean(Const.PREF_DO_NOT_REQUEST_DB_SCOPE, true).apply();
	}
	
	//connection
	public static int getAutoConnect(SharedPreferences prefs) {
		int tmp;
		try {
			tmp = Integer.parseInt(prefs.getString(Const.PREF_AUTO_CONNECT, String.valueOf(Const.PREF_AUTO_CONNECT_VALUE)));
		} catch (Exception e) {
			tmp = Const.PREF_AUTO_CONNECT_VALUE;
		}
		return tmp;		
	}	
	
	public static int getMaxIdlePeriod(SharedPreferences prefs) {
		int tmp;
		try {
			tmp = Integer.parseInt(prefs.getString(Const.PREF_MAX_IDLE_PERIOD, String.valueOf(Const.PREF_MAX_IDLE_PERIOD_VALUE)));
		} catch (Exception e) {
			tmp = Const.PREF_MAX_IDLE_PERIOD_VALUE;
		}
		return tmp;	
	}
	
	public static boolean canSmartAutoConnect(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_SMART_AUTO_CONNECT, Const.PREF_SMART_AUTO_CONNECT_VALUE);			
	}
	
	public static void setSmartAutoConnect(SharedPreferences prefs, boolean enabled) {
		prefs.edit().putBoolean(Const.PREF_SMART_AUTO_CONNECT, enabled).apply();
	}
	
	
	//keyboard layout:

	public static boolean isSecondaryLayoutEnabled(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_SHOW_SECONDARY_LAYOUT, Const.PREF_SHOW_SECONDARY_LAYOUT_VALUE);	
	}
	
	public static String getPrimaryLayoutCode(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_PRIMARY_LAYOUT, Const.PREF_LAYOUT_VALUE);
	}
	
	public static void setPrimaryLayoutCode(SharedPreferences prefs, String layoutCode) {
		prefs.edit().putString(Const.PREF_PRIMARY_LAYOUT, layoutCode).apply();
	}
	
	public static String getSecondaryLayoutCode(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_SECONDARY_LAYOUT, Const.PREF_LAYOUT_VALUE);
	}
	
	
	//typing options
	public static boolean addEnterAfterURL(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_ENTER_AFTER_URL, Const.PREF_ENTER_AFTER_URL_VALUE);
	}

	public static int getTypingSpeed(SharedPreferences prefs) {
		int tmp;
		try {
			tmp = Integer.parseInt(prefs.getString(Const.PREF_TYPING_SPEED, String.valueOf(Const.PREF_TYPING_SPEED_VALUE)));
		} catch (Exception e) {
			tmp = Const.PREF_TYPING_SPEED_VALUE;
		}
		if (tmp > 10) {
			tmp = 10;
		}
		return tmp;
	}

	
	//display options
	
	public static boolean inputStickTextEnabled(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_DISPLAY_IS_TEXT, Const.PREF_DISPLAY_IS_TEXT_VALUE);
	}
	
	public static void addDisplayMenuItem(SharedPreferences prefs, String key, String defValue, String item) {		
		String tmp = prefs.getString(key, defValue);
		if ( !tmp.contains(item)) {
			if (tmp.length() > 1) {
				tmp += "|";
			}
			tmp += item;
			prefs.edit().putString(key, tmp).apply();
		}
	}
	
	//enabled actions
	
	public static String getGeneralItems(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_ITEMS_GENERAL, Const.PREF_ITEMS_GENERAL_VALUE);
	}		
	
	public static String getEntryItemsForPrimaryLayout(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_ITEMS_ENTRY_PRIMARY, Const.PREF_ITEMS_ENTRY_PRIMARY_VALUE);
	}
	
	public static String getEntryItemsForSecondaryLayout(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_ITEMS_ENTRY_SECONDARY, Const.PREF_ITEMS_ENTRY_SECONDARY_VALUE);
	}
	
	public static String getFieldItemsForPrimaryLayout(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_ITEMS_FIELD_PRIMARY, Const.PREF_ITEMS_FIELD_PRIMARY_VALUE);
	}
	
	public static String getFieldItemsForSecondaryLayout(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_ITEMS_FIELD_SECONDARY, Const.PREF_ITEMS_FIELD_SECONDARY_VALUE);
	}
	
	//visibility - general
	
	public static boolean isSettingsActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_SETTINGS);
	}
	
	public static boolean isRemoteActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_REMOTE);
	}

	public static boolean isConnectionOptionsActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_CONNECTION);
	}
	public static boolean isMacSetupActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_MAC_SETUP);
	}
	public static boolean isTabEnterActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_TAB_ENTER);
	}
	
	public static boolean isMacroAddEditActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_MACRO);
	}
	public static boolean isTemplateManageActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_TEMPLATE_MANAGE);
	}
	
	
	//visibility - entry
	
	public static boolean isUserPassActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_USER_PASSWORD);
	}
	public static boolean isUserPassEnterActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_USER_PASSWORD_ENTER);
	}
	public static boolean isMaskedActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_MASKED);
	}
	public static boolean isMacroActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_MACRO);
	}
	public static boolean isRunTemplateActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_RUN_TEMPLATE);
	}
	public static boolean isClipboardActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_CLIPBOARD);
	}
	
	//visibility - field
	
	public static boolean isTypeActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_TYPE);
	}
	
	public static boolean isTypeSlowActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_TYPE_SLOW);
	}
	
	public static boolean isTypeAndEnterActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_TYPE_ENTER);
	}
	
	public static boolean isTypeAndTabActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_TYPE_TAB);
	}
	
	public static boolean isTypeMaskedActionEnabled(String enabledActions) {
		return enabledActions.contains(Const.ITEM_TYPE_MASKED);
	}

	
	
	//clipboard typing
	
	public static boolean isClipboardLaunchAuthenticator(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_CLIPBOARD_LAUNCH_AUTHENTICATOR, Const.PREF_CLIPBOARD_LAUNCH_AUTHENTICATOR_VALUE);
	}
	
	public static boolean isClipboardLaunchCustomApp(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_CLIPBOARD_LAUNCH_CUSTOM_APP, Const.PREF_CLIPBOARD_LAUNCH_CUSTOM_APP_VALUE);
	}
	
	public static String getClipboardCustomAppPackage(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_CLIPBOARD_CUSTOM_APP_PACKAGE, Const.PREF_CLIPBOARD_CUSTOM_APP_PACKAGE_VALUE);
	}
	
	public static String getClipboardCustomAppName(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_CLIPBOARD_CUSTOM_APP_NAME, Const.PREF_CLIPBOARD_CUSTOM_APP_NAME_VALUE);
	}
	
	public static boolean isClipboardAutoDisable(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_CLIPBOARD_AUTO_DISABLE, Const.PREF_CLIPBOARD_AUTO_DISABLE_VALUE);
	}
	
	public static boolean isClipboardAutoEnter(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_CLIPBOARD_AUTO_ENTER, Const.PREF_CLIPBOARD_AUTO_ENTER_VALUE);
	}
	
	public static boolean isClipboardCheckLength(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_CLIPBOARD_CHECK_LENGTH, Const.PREF_CLIPBOARD_CHECK_LENGTH_VALUE);
	}
	
	
	//quick shortcuts
	public static int getEnabledQuickShortcuts(SharedPreferences prefs) {
		String tmp = prefs.getString(Const.PREF_ENABLED_QUICK_SHORTCUTS, Const.PREF_ENABLED_QUICK_SHORTCUTS_VALUE);
		try {
			return Integer.parseInt(tmp);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static String getQuickShortcut(SharedPreferences prefs, int id) {
		switch (id) {
			case 1:
				return prefs.getString(Const.PREF_QUICK_SHORTCUT_1, Const.PREF_QUICK_SHORTCUT_VALUE);
			case 2:
				return prefs.getString(Const.PREF_QUICK_SHORTCUT_2, Const.PREF_QUICK_SHORTCUT_VALUE);
			case 3:
				return prefs.getString(Const.PREF_QUICK_SHORTCUT_3, Const.PREF_QUICK_SHORTCUT_VALUE);
			default:
				return "";
		}		
	}
	
	public static void setQuickShortcut(SharedPreferences prefs, int id, String value) {
		Editor editor = prefs.edit();
		switch (id) {
			case 1:
				editor.putString(Const.PREF_QUICK_SHORTCUT_1, value);		
				break;
			case 2:
				editor.putString(Const.PREF_QUICK_SHORTCUT_2, value);
				break;
			case 3:
				editor.putString(Const.PREF_QUICK_SHORTCUT_3, value);
				break;
		}	
		editor.apply();		
	}
	
	//setup
	
	
	//remote
	
	public static String getRemoteLayoutCode(SharedPreferences prefs) {
		if (isSecondaryLayoutEnabled(prefs)) {
			if (isRemoteUsingPrimaryLayout(prefs)) {
				return getPrimaryLayoutCode(prefs);
			} else {
				return getSecondaryLayoutCode(prefs);
			}
		} else {
			return getPrimaryLayoutCode(prefs);
		}
	}
	
	public static boolean isRemoteUsingPrimaryLayout(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_REMOTE_USE_PRIMARY_LAYOUT, Const.PREF_REMOTE_USE_PRIMARY_LAYOUT_VALUE);	
	}
	
	public static boolean isRemoteInMouseMode(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_REMOTE_MOUSE_MODE, Const.PREF_REMOTE_MOUSE_MODE_VALUE).equals(Const.PREF_REMOTE_MOUSE_MODE_VALUE);
	}
	
	public static int getRemoteMouseSensitivity(SharedPreferences prefs) {
		String tmp = prefs.getString(Const.PREF_REMOTE_MOUSE_SENSITIVITY, Const.PREF_REMOTE_MOUSE_SENSITIVITY_VALUE);
		try {
			return Integer.parseInt(tmp);
		} catch (Exception e) {
			return 50;
		}
	}
	
	public static int getRemoteScrollSensitivity(SharedPreferences prefs) {
		String tmp = prefs.getString(Const.PREF_REMOTE_SCROLL_SENSITIVITY, Const.PREF_REMOTE_SCROLL_SENSITIVITY_VALUE);
		try {
			return Integer.parseInt(tmp);
		} catch (Exception e) {
			return 50;
		}
	}
	
	
	//SMS
	
	public static boolean isSMSProxyEnabled(SharedPreferences prefs) {
        return prefs.contains(Const.PREF_SMS_SMSPROXY_KEY);
	}

	public static String getSMSProxyKey(SharedPreferences prefs) {
		return prefs.getString(Const.PREF_SMS_SMSPROXY_KEY, null);
	}

	public static void setSMSProxyKey(SharedPreferences prefs, String value) {
        Editor editor = prefs.edit();
        if (value == null) {
        	editor.remove(Const.PREF_SMS_SMSPROXY_KEY);
		} else {
			editor.putString(Const.PREF_SMS_SMSPROXY_KEY, value);
		}
		editor.apply();
	}


	//Tweaks

	public static boolean isNeverStopPlugin(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_TWEAKS_NEVER_STOP_PLUGIN, Const.PREF_TWEAKS_NEVER_STOP_PLUGIN_VALUE);
	}

	public static boolean showDebugMessages(SharedPreferences prefs) {
		return prefs.getBoolean(Const.PREF_TWEAKS_SHOW_DEBUG_MESSAGES, Const.PREF_TWEAKS_SHOW_DEBUG_MESSAGES_VALUE);
	}

}
