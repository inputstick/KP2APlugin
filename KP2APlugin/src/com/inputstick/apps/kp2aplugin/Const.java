package com.inputstick.apps.kp2aplugin;


public class Const {
	
	public static final String PACKAGE_PLUGIN_OLD = "keepass2android.plugin.inputstick";
	public static final String PACKAGE_KP2A = "keepass2android.keepass2android";
	public static final String PACKAGE_KP2A_NO_NET = "keepass2android.keepass2android_nonet";
	public static final String PACKAGE_UTILITY = "com.inputstick.apps.inputstickutility";	
	
	public static final int MASKED_PASSWORD_TIMEOUT_MS = 120000;  //2min 
	public static final int CLIPBOARD_TIMEOUT_MS = 30000; //30s
	public static final int DEFAULT_AUTOCONNECT_TIMEOUT_MS = 600000; //10min
	public static final int ACTIVITY_LOCK_TIMEOUT_MS = 180000; //3min, 
	
	public static final String SERVICE_CONNECT = "connect";
	public static final String SERVICE_DISCONNECT = "disconnect";
	public static final String SERVICE_EXEC = "exec";
	
	
	public static final String EXTRA_ACTION = "action";
	
	public static final String SELECTED_UI_ACTION = "action";
	public static final String EXTRA_TEXT = "text";
	public static final String EXTRA_LAYOUT = "layout";
	public static final String EXTRA_REPORT_MULTIPLIER = "multiplier";
	public static final String EXTRA_DELAY = "delay";
	public static final String EXTRA_MODIFIER = "mod";
	public static final String EXTRA_KEY = "key";
	public static final String EXTRA_MACRO = "macro";
	public static final String EXTRA_ENTRY_ID = "entry_id";
	public static final String EXTRA_LAUNCHED_FROM_KP2A = "kp2a_launch";
	public static final String EXTRA_MACRO_RUN_BUT_EMPTY = "macro_run_empty";	
	public static final String EXTRA_MACRO_ACTIONS = "macro_actions";		
	public static final String EXTRA_MACRO_TEMPLATE_MODE = "macro_template_mode";
	public static final String EXTRA_TEMPLATE_ID = "template_id";
	public static final String EXTRA_TEMPLATE_MANAGE = "template_manage";	
			
	
	
	public static final String EXTRA_MAX_TIME = "max_time";
	
	public static final String EXTRA_NOTIFICATION_ACTION = "notification_action";
	
	
	public static final String EXTRA_TYPE_SLOW = "params";
	public static final String EXTRA_SHOW_CHANGELOG = "changelog";
	
	public static final String ACTION_TYPE = "type";
	public static final String ACTION_DELAY = "delay";
	public static final String ACTION_KEY_PRESS = "key";
	
	public static final int SLOW_TYPING_MULTIPLIER= 10;

	public static final String MACRO_PREF_PREFIX = "m_";
	public static final String TEMPLATE_PREF_PREFIX = "t_";
	public static final String TEMPLATE_NAME_PREF_PREFIX = "tn_";
	
	public static final String TEMPLATE_DEFAULT_NAME_PREF_PREFIX = "TEMPLATE: ";
	
}
