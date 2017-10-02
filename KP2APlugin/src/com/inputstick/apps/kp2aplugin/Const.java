package com.inputstick.apps.kp2aplugin;


public class Const {
	
	public static final String SKIP_CHANGELOG_APP_VERSION = "1.11";
	
	public static final int IC = R.drawable.ic_launcher;
	
	public static final int INPUTSTICK_SERVICE_NOTIFICATION_ID = 1;
	public static final int CLIPBOARD_SERVICE_NOTIFICATION_ID = 2;
	
	public static final String PACKAGE_PLUGIN_OLD = "keepass2android.plugin.inputstick";
	public static final String PACKAGE_KP2A = "keepass2android.keepass2android";
	public static final String PACKAGE_KP2A_NO_NET = "keepass2android.keepass2android_nonet";
	public static final String PACKAGE_UTILITY = "com.inputstick.apps.inputstickutility";	
	public static final String PACKAGE_AUTHENTICATOR = "com.google.android.apps.authenticator2";	
	
	public static final int LAYOUT_NONE = 0;
	public static final int LAYOUT_PRIMARY = 1;
	public static final int LAYOUT_SECONDARY = 2;	
	
	public static final int AUTO_CONNECT_DISABLED = 0;
	public static final int AUTO_CONNECT_ALWAYS = 1;
	public static final int AUTO_CONNECT_SMART = 2;
	
	public static final int AUTO_DISCONNECT_IDLE_LOCKED = 0;
	public static final int AUTO_DISCONNECT_CLOSED_IDLE_LOCKED = 1;
	
	
	public static final int MASKED_PASSWORD_TIMEOUT_MS = 120000;  //2min 
	public static final int CLIPBOARD_TIMEOUT_MS = 30000; //30s
	public static final int ACTIVITY_LOCK_TIMEOUT_MS = 180000; //3min, 
	
	
	//service
	public static final String SERVICE_FORCE_STOP = "service_force_stop";	
	public static final String SERVICE_START = "service_open_entry"; //entry opened, but service is not running
	public static final String SERVICE_RESTART = "service_action_selected";  //action was selected, but service is not running	
	
	public static final String SERVICE_QUEUE_ITEM = "queue_item"; 
	public static final String SERVICE_ENTRY_ACTION = "entry_action";	
	
	//AllActions 
	public static final String EXTRA_ACTION = "selected_action";	
	public static final String EXTRA_LAYOUT = "layout";
	//MaskedPassword
	public static final String EXTRA_TEXT = "text";
	//Macro/Template
	public static final String EXTRA_MACRO_DATA = "macro_data";	
	public static final String EXTRA_ENTRY_ID = "entry_id";	
	public static final String EXTRA_MACRO_RUN_BUT_EMPTY = "macro_run_empty";	
	public static final String EXTRA_MACRO_TEMPLATE_MODE = "macro_template_mode";
	public static final String EXTRA_TEMPLATE_ID = "template_id";
	public static final String EXTRA_TEMPLATE_MANAGE = "template_manage";					
	
	public static final String EXTRA_MAX_TIME = "max_time";	
	public static final String EXTRA_LAUNCHED_FROM_KP2A = "kp2a_launch";
	//Clipboard 
	public static final String EXTRA_NOTIFICATION_ACTION = "notification_action";  //clipboardservice
		
	public static final String EXTRA_TYPE_SLOW = "type_slow";
	public static final String EXTRA_ADD_KEY = "add_key";
	public static final String EXTRA_SHOW_CHANGELOG = "show_changelog";
	public static final String EXTRA_SHOW_SCOPE = "show_scope"; 
	public static final String EXTRA_SHOW_NOTIFICATION_INFO = "show_notification_info";
	
	
	
	public static final int TYPING_SPEED_DEFAULT= 1;
	public static final int TYPING_SPEED_SLOW= 10;

	public static final String MACRO_PREF_PREFIX = "m_";
	public static final String TEMPLATE_PREF_PREFIX = "t_";
	public static final String TEMPLATE_NAME_PREF_PREFIX = "tn_";
	
	public static final String TEMPLATE_DEFAULT_NAME_PREF_PREFIX = "TEMPLATE: ";
	
	
	
	//ITEMS:
	//general
	public static final String ITEM_SETTINGS = "settings";
	public static final String ITEM_CONNECTION = "con_disc";
	public static final String ITEM_MAC_SETUP = "osx";
	public static final String ITEM_TAB_ENTER = "tab_enter";
	public static final String ITEM_MACRO = "macro";
	public static final String ITEM_RUN_TEMPLATE = "run_template";
	public static final String ITEM_TEMPLATE_MANAGE = "manage_template";
	
	
	//entry
	public static final String ITEM_USER_PASSWORD = "username_and_password";
	public static final String ITEM_USER_PASSWORD_ENTER = "username_password_enter";
	public static final String ITEM_MASKED = "masked_password";
	public static final String ITEM_CLIPBOARD = "clipboard";
	
	//field
	public static final String ITEM_TYPE = "type_normal";	
	public static final String ITEM_TYPE_ENTER = "type_enter";
	public static final String ITEM_TYPE_TAB = "type_tab";
	public static final String ITEM_TYPE_SLOW = "type_slow";
	
	
	//ACTIONS	
	public static final String ACTION_SHOW_ALL = "com.inputstick.apps.kp2aplugin.show_all";
	public static final String ACTION_MASKED_PASSWORD = "com.inputstick.apps.kp2aplugin.masked_password";
	public static final String ACTION_SETTINGS = "com.inputstick.apps.kp2aplugin.settings";
	public static final String ACTION_CONNECT = "com.inputstick.apps.kp2aplugin.connect";
	public static final String ACTION_DISCONNECT = "com.inputstick.apps.kp2aplugin.disconnect";
	public static final String ACTION_USER_PASS = "com.inputstick.apps.kp2aplugin.user_pass";
	public static final String ACTION_USER_PASS_ENTER = "com.inputstick.apps.kp2aplugin.user_pass_enter";
	public static final String ACTION_MAC_SETUP = "com.inputstick.apps.kp2aplugin.mac_setup";
	public static final String ACTION_TAB = "com.inputstick.apps.kp2aplugin.tab";
	public static final String ACTION_ENTER = "com.inputstick.apps.kp2aplugin.enter";
	
	public static final String ACTION_MACRO_ADDEDIT = "com.inputstick.apps.kp2aplugin.macro_addedit";	
	public static final String ACTION_CLIPBOARD = "com.inputstick.apps.kp2aplugin.clipboard";	
	public static final String ACTION_MACRO_RUN = "com.inputstick.apps.kp2aplugin.macro_run";
	public static final String ACTION_TEMPLATE_RUN = "com.inputstick.apps.kp2aplugin.template_run";
	public static final String ACTION_TEMPLATE_MANAGE = "com.inputstick.apps.kp2aplugin.template_manage";	
	
	public static final String ACTION_FIELD_TYPE_PRIMARY = "com.inputstick.apps.kp2aplugin.type";	
	public static final String ACTION_FIELD_TYPE_ENTER_PRIMARY = "com.inputstick.apps.kp2aplugin.type_enter";
	public static final String ACTION_FIELD_TYPE_TAB_PRIMARY = "com.inputstick.apps.kp2aplugin.type_tab";
	public static final String ACTION_FIELD_TYPE_SLOW_PRIMARY = "com.inputstick.apps.kp2aplugin.type_slow";
	
	public static final String ACTION_FIELD_TYPE_SECONDARY = "com.inputstick.apps.kp2aplugin.type_secondary";	
	public static final String ACTION_FIELD_TYPE_ENTER_SECONDARY = "com.inputstick.apps.kp2aplugin.type_enter_secondary";
	public static final String ACTION_FIELD_TYPE_TAB_SECONDARY = "com.inputstick.apps.kp2aplugin.type_tab_secondary";
	public static final String ACTION_FIELD_TYPE_SLOW_SECONDARY = "com.inputstick.apps.kp2aplugin.type_slow_secondary";
	
	
	//PREFERENCES:	
	
	public static final String PREF_AUTO_CONNECT = "connection_auto_connect";
	public static final int PREF_AUTO_CONNECT_VALUE = AUTO_CONNECT_DISABLED;
	
	public static final String PREF_SMART_AUTO_CONNECT = "connection_smart_auto_connect";
	public static final boolean PREF_SMART_AUTO_CONNECT_VALUE = true;
	
	public static final String PREF_MAX_IDLE_PERIOD = "connection_max_idle_period";
	public static final int PREF_MAX_IDLE_PERIOD_VALUE = 180000;	
	
	public static final String PREF_SHOW_NOTIFICATION = "show_notification";
	public static final boolean PREF_SHOW_NOTIFICATION_VALUE = true;				
	
	public static final String PREF_TYPING_SPEED = "typing_speed";
	public static final int PREF_TYPING_SPEED_VALUE = 1;
	
	public static final String PREF_PRIMARY_LAYOUT = "kbd_layout";
	public static final String PREF_SECONDARY_LAYOUT = "secondary_kbd_layout";
	public static final String PREF_LAYOUT_VALUE = "en-US";
	
	public static final String PREF_SHOW_SECONDARY_LAYOUT = "show_secondary";
	public static final boolean PREF_SHOW_SECONDARY_LAYOUT_VALUE = false;
	
	public static final String PREF_ENTER_AFTER_URL = "enter_after_url";
	public static final boolean PREF_ENTER_AFTER_URL_VALUE = false;
		
	public static final String PREF_DISPLAY_IS_TEXT = "display_inputstick_text";
	public static final boolean PREF_DISPLAY_IS_TEXT_VALUE = true;
	
	public static final String PREF_ITEMS_GENERAL = "items_general";
	public static final String PREF_ITEMS_GENERAL_VALUE = "settings|osx|tab_enter|macro";		

	public static final String PREF_ITEMS_ENTRY_PRIMARY = "items_entry_primary";
	public static final String PREF_ITEMS_ENTRY_PRIMARY_VALUE = "username_and_password|username_password_enter|masked_password|macro|run_template|clipboard";
	
	public static final String PREF_ITEMS_FIELD_PRIMARY = "items_field_primary";
	public static final String PREF_ITEMS_FIELD_PRIMARY_VALUE = "type_normal|type_enter|type_slow";
	
	public static final String PREF_ITEMS_ENTRY_SECONDARY = "items_entry_secondary";
	public static final String PREF_ITEMS_ENTRY_SECONDARY_VALUE = "username_and_password";
	
	public static final String PREF_ITEMS_FIELD_SECONDARY = "items_field_secondary";
	public static final String PREF_ITEMS_FIELD_SECONDARY_VALUE = "type_normal";
	
	
	
	public static final String PREF_CLIPBOARD_LAUNCH_AUTHENTICATOR = "clipboard_launch_authenticator";
	public static final boolean PREF_CLIPBOARD_LAUNCH_AUTHENTICATOR_VALUE = true;
	
	public static final String PREF_CLIPBOARD_LAUNCH_CUSTOM_APP = "clipboard_launch_custom_app";
	public static final boolean PREF_CLIPBOARD_LAUNCH_CUSTOM_APP_VALUE = false;
	
	public static final String PREF_CLIPBOARD_CUSTOM_APP_PACKAGE = "clipboard_custom_app_package";
	public static final String PREF_CLIPBOARD_CUSTOM_APP_PACKAGE_VALUE = "none";
	
	public static final String PREF_CLIPBOARD_CUSTOM_APP_NAME = "clipboard_custom_app_name";
	public static final String PREF_CLIPBOARD_CUSTOM_APP_NAME_VALUE = "Unknown app";
	
	public static final String PREF_CLIPBOARD_AUTO_DISABLE = "clipboard_auto_disable";
	public static final boolean PREF_CLIPBOARD_AUTO_DISABLE_VALUE = true;
	
	public static final String PREF_CLIPBOARD_AUTO_ENTER = "clipboard_auto_enter";
	public static final boolean PREF_CLIPBOARD_AUTO_ENTER_VALUE = false;
	
	public static final String PREF_CLIPBOARD_CHECK_LENGTH = "clipboard_check_length";
	public static final boolean PREF_CLIPBOARD_CHECK_LENGTH_VALUE = true;		
		
	public static final String PREF_SHOW_RELOAD_WARNING = "show_reload_warning";
	public static final boolean PREF_SHOW_RELOAD_WARNING_VALUE = true;		
	
	public static final String PREF_SETUP_COMPLETED = "setup_completed";
	public static final String PREF_ENABLE_PLUGIN_PREF = "enable_plugin_pref";
	public static final String PREF_SHOW_ABOUT_KEY = "show_about_key";
	public static final String PREF_SHOW_HELP_WEBPAGE_KEY = "show_help_webpage_key";
	public static final String PREF_SHOW_CHANGELOG_PREFERENCE_KEY = "show_changelog_preference_key";
	
	public static final String PREF_DO_NOT_REQUEST_DB_SCOPE = "db_scope_disabled";
	public static final boolean PREF_DO_NOT_REQUEST_DB_VALUE = false;		
	
}
