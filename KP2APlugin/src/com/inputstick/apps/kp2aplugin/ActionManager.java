package com.inputstick.apps.kp2aplugin;

import java.util.HashMap;

import keepass2android.pluginsdk.KeepassDefs;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.inputstick.api.broadcast.InputStickBroadcast;
import com.inputstick.api.hid.HIDKeycodes;

public class ActionManager {
	
	private static ActionManager instance;
	
	private static UserPreferences mUserPrefs;
	private static Context mCtx;
	
	private static HashMap<String, String> mEntryFields;
	private static String mEntryId;
	protected static long lastActivityTime;
	
    private ActionManager() {
    }
    
    public static ActionManager getInstance(Context ctx) {
    	return getInstance(ctx, null, null);
    }
	
	public static ActionManager getInstance(Context ctx, String entryId, HashMap<String, String> entryFields) {
		if (instance == null) {
			instance = new ActionManager();
		}		
		
		mCtx = ctx;		
		if (mUserPrefs == null) {
			mUserPrefs = new UserPreferences(PreferenceManager.getDefaultSharedPreferences(ctx));			
		}				
		
		if (entryFields != null) {
			mEntryFields = entryFields;
		} else {
			if (mEntryFields == null) {
				mEntryFields = new HashMap<String, String>();
			}
		}
		
		if (entryId != null) {
			mEntryId = entryId;
		} else {
			if (mEntryId == null) {
				mEntryId = "";
			}
		}
		
		return instance;
	}

	
	public void reloadPreferences(SharedPreferences prefs) {
		if (mUserPrefs != null) {
			mUserPrefs.reload(prefs);
		} else {
			mUserPrefs = new UserPreferences(prefs);
		}
	}
	
	public UserPreferences getUserPrefs() {
		return mUserPrefs;
	}
	
	private String getMacro() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		return prefs.getString(Const.MACRO_PREF_PREFIX + mEntryId, null);
	}
	
	
	public String getActionStringForPrimaryLayout(int id, boolean allowInputStickText) {
		return getActionString(id, mUserPrefs.getLayoutPrimaryDisplayCode(), allowInputStickText);
	}
	
	public String getActionStringForPrimaryLayout(String actionText, boolean allowInputStickText) {
		return getActionString(actionText, mUserPrefs.getLayoutPrimaryDisplayCode(), allowInputStickText);
	}
	
	
	public String getActionStringForSecondaryLayout(int id, boolean allowInputStickText) {
		return getActionString(id, mUserPrefs.getLayoutSecondaryDisplayCode(), allowInputStickText);
	}
		
	public String getActionStringForSecondaryLayout(String actionText, boolean allowInputStickText) {
		return getActionString(actionText, mUserPrefs.getLayoutSecondaryDisplayCode(), allowInputStickText);
	}
	
	
	public String getActionString(int id, boolean allowInputStickText) {
		return getActionString(id, null, allowInputStickText);
	}	
	
	public String getActionString(String actionText, boolean allowInputStickText) {
		return getActionString(actionText, null, allowInputStickText);
	}	
	
	
	private String getActionString(int id, String layoutCode, boolean allowInputStickText) {
		return getActionString(mCtx.getString(id), layoutCode, allowInputStickText);
	}
	
	private String getActionString(String actionText, String layoutCode, boolean allowInputStickText) {
		String s = actionText;
		if (layoutCode != null) {
			s += " (" + layoutCode + ")";
		}
		if ((allowInputStickText) && (mUserPrefs.isDisplayInputStickText())) {
			s += " (IS)";
		}
		return s;
	}
	
	public void onEntryClosed() {
		mEntryFields = null;
		mEntryId = null;
	}
		
	
	public void startSettingsActivity() {
		Intent i = new Intent(mCtx.getApplicationContext(), SettingsActivity.class);
		i.putExtra(Const.EXTRA_LAUNCHED_FROM_KP2A, true);				
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.getApplicationContext().startActivity(i);			
	}
	public void startShowAllActivity() {
		Intent i = new Intent(mCtx.getApplicationContext(), AllActionsActivity.class);		
		i.putExtra(Const.EXTRA_MAX_TIME, System.currentTimeMillis() + Const.ACTIVITY_LOCK_TIMEOUT_MS);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.getApplicationContext().startActivity(i);		
	}
	public void startMacSetupActivity() {
		connect();
		Intent i = new Intent(mCtx.getApplicationContext(), MacSetupActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.getApplicationContext().startActivity(i);			
	}
	
	public void startSelectTemplateActivity(String layoutName, boolean manage) {
		Intent i = new Intent(mCtx.getApplicationContext(), SelectTemplateActivity.class);	
		i.putExtra(Const.EXTRA_LAYOUT, layoutName);
		i.putExtra(Const.EXTRA_MAX_TIME, System.currentTimeMillis() + Const.ACTIVITY_LOCK_TIMEOUT_MS);
		i.putExtra(Const.EXTRA_TEMPLATE_MANAGE, manage);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.getApplicationContext().startActivity(i);		
	}	
	
	

	
	
	
	public void connect() {
		if (mUserPrefs.getUseBroadcasts()) {
			InputStickBroadcast.requestConnection(mCtx);
		} else {
			Intent serviceIntent = new Intent(mCtx, InputStickService.class);
			serviceIntent.setAction(Const.SERVICE_CONNECT);
			mCtx.startService(serviceIntent);
		}
	}
	
	public void disconnect() {
		if (mUserPrefs.getUseBroadcasts()) {
			InputStickBroadcast.releaseConnection(mCtx);
		} else {
			Intent serviceIntent = new Intent(mCtx, InputStickService.class);
			serviceIntent.setAction(Const.SERVICE_DISCONNECT);
			mCtx.startService(serviceIntent);
		}

	}	
	
	public void queueText(String text, String layout, int reportMultiplier) {
		if (mUserPrefs.getUseBroadcasts()) {
			lastActivityTime = System.currentTimeMillis();
			InputStickBroadcast.type(mCtx, text, layout, reportMultiplier);
		} else {
			Bundle b = new Bundle();
			b.putString(Const.EXTRA_ACTION, Const.ACTION_TYPE);		
			b.putString(Const.EXTRA_TEXT, text);
			b.putString(Const.EXTRA_LAYOUT, layout);
			b.putInt(Const.EXTRA_REPORT_MULTIPLIER, reportMultiplier);		
			sendToService(b);
		}
	}

	public void queueText(String text, String layout) {
		queueText(text, layout, mUserPrefs.getReportMultiplier());
	}
	
	public void queueDelay(int value) {	
		if (mUserPrefs.getUseBroadcasts()) {
			lastActivityTime = System.currentTimeMillis();
			//TODO at this time it is not possible to add queue delays
			for (int i = 0; i < value; i++) {
				InputStickBroadcast.pressAndRelease(mCtx, (byte)0x00, (byte)0x00);
			}
		} else {
			Bundle b = new Bundle();
			b.putString(Const.EXTRA_ACTION, Const.ACTION_DELAY);		
			b.putInt(Const.EXTRA_DELAY, value);
			sendToService(b);
		}

	}
	
	public void queueKey(byte modifier, byte key) {
		if (mUserPrefs.getUseBroadcasts()) {
			lastActivityTime = System.currentTimeMillis();
			InputStickBroadcast.pressAndRelease(mCtx, modifier, key);
		} else {
			Bundle b = new Bundle();
			b.putString(Const.EXTRA_ACTION, Const.ACTION_KEY_PRESS);		
			b.putByte(Const.EXTRA_MODIFIER, modifier);
			b.putByte(Const.EXTRA_KEY, key);
			b.putInt(Const.EXTRA_REPORT_MULTIPLIER, mUserPrefs.getReportMultiplier());	
			sendToService(b);
		}		
	}
	
	
	public void sendToService(Bundle b) {
		lastActivityTime = System.currentTimeMillis();
		Intent serviceIntent = new Intent(mCtx, InputStickService.class);
		serviceIntent.setAction(Const.SERVICE_EXEC);
		serviceIntent.putExtras(b);
		mCtx.startService(serviceIntent);
	}

			
	
	public void typeUsernameAndPassword(String layoutName, boolean addEnter) {
		queueText(mEntryFields.get(KeepassDefs.UserNameField), layoutName);
		queueDelay(5);
		queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_TAB);
		queueDelay(5);
		queueText(mEntryFields.get(KeepassDefs.PasswordField), layoutName);
		if (addEnter) {
			queueDelay(5);
			queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER);
		}
	}
	
	
	public void runMacro(String layoutName) {	
		String macro = getMacro();	
		if ((macro != null) && (macro.length() > 0)) {
			runMacro(layoutName, macro);
		} else {
			addEditMacro(true, false, 0);
		}
	}
	
	public void runMacro(String layoutName, String macro) {		
		if ((macro != null) && (macro.length() > 0)) {
			boolean runInBackground = macro.startsWith(MacroHelper.MACRO_BACKGROUND_EXEC_STRING);
			String actions[] = macro.split("%");
			connect();
			if (runInBackground) {	
				for (String s : actions) {
					runMacroAction(layoutName, s);
				}								
			} else {
				Intent i = new Intent(mCtx.getApplicationContext(), MacroExecuteActivity.class);
				i.putExtra(Const.EXTRA_MAX_TIME, System.currentTimeMillis() + Const.ACTIVITY_LOCK_TIMEOUT_MS);
				i.putExtra(Const.EXTRA_MACRO_ACTIONS, actions);
				i.putExtra(Const.EXTRA_LAYOUT, layoutName);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				mCtx.getApplicationContext().startActivity(i);	
			}			
		}
	}
	
	
	
	
	@SuppressLint("DefaultLocale")
	public void runMacroAction(String layoutName, String s) {
		String tmp, param;
		if ((s != null) && (s.length() > 0)) {
			tmp = s.toLowerCase();
			//no parameter
			if (tmp.startsWith(MacroHelper.MACRO_ACTION_PASSWORD)) {
				queueText(mEntryFields.get(KeepassDefs.PasswordField), layoutName);
			} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_USER_NAME)) {
				queueText(mEntryFields.get(KeepassDefs.UserNameField), layoutName);
			} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_URL)) {
				queueText(mEntryFields.get(KeepassDefs.UrlField), layoutName);
			} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_PASSWORD_MASKED)) {
				openMaskedPassword(layoutName, false);
			} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_CLIPBOARD)) {
				clipboardTyping(layoutName);
			} else {
				//get parameter
				param = MacroHelper.getParam(s);
				if ((param != null) && (param.length() > 0)) { 					
					if (tmp.startsWith(MacroHelper.MACRO_ACTION_TYPE)) {						
						queueText(param, layoutName);
					}
					if (tmp.startsWith(MacroHelper.MACRO_ACTION_DELAY)) {
						queueDelay(MacroHelper.getDelay(param));
					}
					if (tmp.startsWith(MacroHelper.MACRO_ACTION_KEY)) {
						queueKey(MacroHelper.getModifiers(param), MacroHelper.getKey(param));
					}		
				}
			}
		}
	}
	
	
	
	public void clipboardTyping(String layoutName) {
		connect(); //in case not connected already
		if (mUserPrefs.isClipboardLaunchAuthenticator()) {
			Intent launchIntent = mCtx.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.authenticator2");
			if (launchIntent != null) {
				mCtx.getApplicationContext().startActivity(launchIntent);
			} else {
				Toast.makeText(mCtx, R.string.text_authenticator_app_not_found, Toast.LENGTH_LONG).show();
			}						
		} else if (mUserPrefs.isClipboardLaunchCustomApp()) {
			String customPackage = mUserPrefs.getClipboardCustomAppPackage();
			if ("none".equals(customPackage)) {
				Toast.makeText(mCtx, R.string.text_custom_app_not_specified, Toast.LENGTH_LONG).show();
			} else {
				Intent launchIntent = mCtx.getPackageManager().getLaunchIntentForPackage(customPackage);
				if (launchIntent != null) {
					mCtx.getApplicationContext().startActivity(launchIntent);
				} else {
					String message = mCtx.getString(R.string.text_custom_app_not_found) + " (" + customPackage + ")";
					Toast.makeText(mCtx, message, Toast.LENGTH_LONG).show();
				}		
			}
		}
		
		
		Intent i = new Intent(mCtx, ClipboardService.class);
		i.putExtra(Const.EXTRA_LAYOUT, layoutName);
		mCtx.startService(i);
	}
	
	
	public void addEditMacro(boolean showEmptyMacroError, boolean templateMode, int templateId) {
		Intent i = new Intent(mCtx.getApplicationContext(), MacroActivity.class);
		i.putExtra(Const.EXTRA_MACRO, getMacro());
		i.putExtra(Const.EXTRA_ENTRY_ID, mEntryId);		
		if (showEmptyMacroError) {
			i.putExtra(Const.EXTRA_MACRO_RUN_BUT_EMPTY, true);		
		}
		if (templateMode) {
			i.putExtra(Const.EXTRA_MACRO_TEMPLATE_MODE, true);		
			i.putExtra(Const.EXTRA_TEMPLATE_ID, templateId);					
		}		
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.getApplicationContext().startActivity(i);			
	}
	
	public void openMaskedPassword(String layoutName, boolean addClearFlags) {
		connect(); //in case not connected already
		Intent i = new Intent(mCtx.getApplicationContext(), MaskedPasswordActivity.class);
		i.putExtra(Const.EXTRA_TEXT, mEntryFields.get(KeepassDefs.PasswordField));
		i.putExtra(Const.EXTRA_LAYOUT, layoutName);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (addClearFlags) {
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		mCtx.getApplicationContext().startActivity(i);		
	}	
	
}
