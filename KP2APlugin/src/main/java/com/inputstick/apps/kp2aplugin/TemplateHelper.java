package com.inputstick.apps.kp2aplugin;

import android.content.SharedPreferences;

@SuppressWarnings("WeakerAccess")
public class TemplateHelper {
	
	public static CharSequence[] getTemplateNames(SharedPreferences prefs) {
		return new CharSequence[] {getTemplateName(prefs, 0), getTemplateName(prefs, 1), getTemplateName(prefs, 2), getTemplateName(prefs, 3), getTemplateName(prefs, 4)};
	}
	
	public static String getTemplateName(SharedPreferences prefs, int id) {
		return prefs.getString(Const.TEMPLATE_NAME_PREF_PREFIX + id, getTemplateDefaultName(id));
	}
	
	public static String getTemplateDefaultName(int id) {
		return Const.TEMPLATE_DEFAULT_NAME_PREF_PREFIX + id;
	}
	
	public static void saveTemplate(SharedPreferences prefs, int id, String name, String macro) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Const.TEMPLATE_NAME_PREF_PREFIX + id, name);
		editor.putString(Const.TEMPLATE_PREF_PREFIX + id, macro);
		editor.apply();			
	}
	
	public static String loadTemplate(SharedPreferences prefs, int id) {
		return prefs.getString(Const.TEMPLATE_PREF_PREFIX + id, "");			
	}	

}
