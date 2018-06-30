package com.inputstick.apps.kp2aplugin;

import com.inputstick.apps.kp2aplugin.remote.RemoteActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public abstract class ActionHelper {

	public static void forceStopService(Context ctx) {
		Intent serviceIntent = new Intent(ctx, InputStickService.class);
		serviceIntent.setAction(Const.SERVICE_FORCE_STOP);
		ctx.startService(serviceIntent);	
	}
	
	public static void startSettingsActivityAction(Context ctx) {
		Intent i = new Intent(ctx, SettingsActivity.class);
		i.putExtra(Const.EXTRA_LAUNCHED_FROM_KP2A, true);				
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);			
	}
	
	public static void startShowAllActivityAction(Context ctx, EntryData entryData) {
		Intent i = new Intent(ctx, AllActionsActivity.class);		
		i.putExtras(entryData.getBundle());
		i.putExtra(Const.EXTRA_MAX_TIME, System.currentTimeMillis() + Const.ACTIVITY_LOCK_TIMEOUT_MS);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);		
	}
	
	public static void startMacSetupActivityAction(Context ctx) {		
		Intent i = new Intent(ctx, MacSetupActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);				
	}
	
	public static void startRemoteActivityAction(Context ctx) {		
		Intent i = new Intent(ctx, RemoteActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);				
	}	
	
	
	public static void startSMSActivityAction(Context ctx, String smsText, String smsSender, TypingParams params) {
		Intent i = new Intent(ctx, SMSActivity.class);
		i.putExtras(params.getBundle());
		i.putExtra(Const.EXTRA_TEXT, smsText);
		i.putExtra(Const.EXTRA_SMS_SENDER, smsSender);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);				
	}
	
	
	
	
	public static void startSelectTemplateActivityAction(Context ctx, EntryData entryData, TypingParams params, boolean manage) {
		Intent i = new Intent(ctx, SelectTemplateActivity.class);	
		i.putExtras(params.getBundle());
		i.putExtras(entryData.getBundle());
		i.putExtra(Const.EXTRA_MAX_TIME, System.currentTimeMillis() + Const.ACTIVITY_LOCK_TIMEOUT_MS);
		i.putExtra(Const.EXTRA_TEMPLATE_MANAGE, manage);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);				
	}		
	
	public static void startMaskedPasswordActivity(Context ctx, String password, TypingParams params, boolean addClearFlags) {		
		Intent i = new Intent(ctx, MaskedPasswordActivity.class);
		i.putExtras(params.getBundle());
		i.putExtra(Const.EXTRA_TEXT, password);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (addClearFlags) {
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		ctx.startActivity(i);		
	}
	
	
	public static void startClipboardTypingService(Context ctx, TypingParams params) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (PreferencesHelper.isClipboardLaunchAuthenticator(prefs)) {
			Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(Const.PACKAGE_AUTHENTICATOR);
			if (launchIntent != null) {
				ctx.getApplicationContext().startActivity(launchIntent);
			} else {
				Toast.makeText(ctx, R.string.text_authenticator_app_not_found, Toast.LENGTH_LONG).show();
			}						
		} else if (PreferencesHelper.isClipboardLaunchCustomApp(prefs)) {
			String customPackage = PreferencesHelper.getClipboardCustomAppPackage(prefs);
			if ("none".equals(customPackage)) {
				Toast.makeText(ctx, R.string.text_custom_app_not_specified, Toast.LENGTH_LONG).show();
			} else {
				Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(customPackage);
				if (launchIntent != null) {
					ctx.getApplicationContext().startActivity(launchIntent);
				} else {
					String message = ctx.getString(R.string.text_custom_app_not_found) + " (" + customPackage + ")";
					Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
				}		
			}
		}		
		
		Intent i = new Intent(ctx, ClipboardService.class);
		i.putExtras(params.getBundle());
		ctx.startService(i);
	}		

	
	public static boolean runMacroAction(Context ctx, EntryData entryData, TypingParams params) {
		boolean executed = false;
		String macro = MacroHelper.loadMacro(PreferenceManager.getDefaultSharedPreferences(ctx), entryData.getEntryId());		
		if ((macro != null) && (macro.length() > 0)) {
			executeMacro(ctx, entryData, params, macro);
			executed = true;
		} else {
			addEditMacroAction(ctx, entryData, true);
		}
		return executed;
	}
	
	public static void executeMacro(Context ctx, EntryData entryData, TypingParams params, String macroData) {		
		if ((macroData != null) && (macroData.length() > 0)) {
			boolean runInBackground = macroData.startsWith(MacroHelper.MACRO_BACKGROUND_EXEC_STRING);			
			if (runInBackground) {
				EntryMacro m = new EntryMacro(macroData, entryData, params, true);
				m.executeInBackground(ctx);
			} else {
				Intent i = new Intent(ctx, MacroExecuteActivity.class);				
				i.putExtras(params.getBundle());
				i.putExtras(entryData.getBundle());
				i.putExtra(Const.EXTRA_MACRO_DATA, macroData);
				i.putExtra(Const.EXTRA_MAX_TIME, System.currentTimeMillis() + Const.ACTIVITY_LOCK_TIMEOUT_MS);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.getApplicationContext().startActivity(i);	
			}			
		}
	}
	
	public static void addEditMacroAction(Context ctx, EntryData entryData, boolean showEmptyMacroError) {
		Intent i = new Intent(ctx, MacroActivity.class);
		i.putExtra(Const.EXTRA_ENTRY_ID, entryData.getEntryId());		
		if (showEmptyMacroError) {
			i.putExtra(Const.EXTRA_MACRO_RUN_BUT_EMPTY, true);		
		}		
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.getApplicationContext().startActivity(i);			
	}	
	
	public static void addEditTemplateAction(Context ctx, int templateId) {
		Intent i = new Intent(ctx, MacroActivity.class);
		i.putExtra(Const.EXTRA_MACRO_TEMPLATE_MODE, true);		
		i.putExtra(Const.EXTRA_TEMPLATE_ID, templateId);	
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.getApplicationContext().startActivity(i);			
	}	
	
}
