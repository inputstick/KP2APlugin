package com.inputstick.apps.kp2aplugin;

@SuppressWarnings("WeakerAccess")
public class Const {
	
	public static final String SKIP_CHANGELOG_APP_VERSION = "1.44";
	
	//public static final int IC = R.drawable.ic_launcher;

	public static final String NOTIFICATION_STATUS_CHANNEL_ID = "KP2AInputStickPluginChannelID";
	public static final CharSequence NOTIFICATION_STATUS_CHANNEL_NAME = "Status notifications";
	public static final String NOTIFICATION_ACTION_CHANNEL_ID = "KP2AInputStickPluginActionChannelID";
	public static final CharSequence NOTIFICATION_ACTION_CHANNEL_NAME = "Type from SMS/clipboard";
	public static final String NOTIFICATION_IMPORTANT_CHANNEL_ID = "KP2AInputStickPluginImportantChannelID";
	public static final CharSequence NOTIFICATION_IMPORTANT_CHANNEL_NAME = "Important notifications";
	
	public static final int INPUTSTICK_SERVICE_NOTIFICATION_ID = 1;
	public static final int CLIPBOARD_TYPING_NOTIFICATION_ID = 2;
	public static final int UPDATE_NOTIFICATION_ID = 3;
	public static final int SMS_NOTIFICATION_ID = 4;
	public static final int PERMISSION_NOTIFICATION_ID = 5;

	public static final int REQUEST_CODE_ENABLE_PLUGIN = 123;
	public static final int REQUEST_CODE_SELECT_APP = 124;
	public static final int REQUEST_CODE_SMS_PROXY_ACTIVATE = 125;
	public static final int REQUEST_CODE_NOTIFICATIONS_PERMISSION = 126;

	
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
	
	//service
	public static final int SERVICE_CAPSLOCK_WARNING_TIMEOUT = 10 * 1000;	
	public static final int SERVICE_FAILSAFE_PERIOD = 10 * 60 * 1000;	//10min; stop plugin after FAILSAFE_PERIOD inactivity in case KP2A crashes
	
	public static final int SMS_TIMEOUT_MS = 60 * 1000;  //1min
	public static final int CLIPBOARD_INITIAL_TIMEOUT_MS = 60 * 1000; //1min, change strings if modified!
	public static final int CLIPBOARD_MAX_TIMEOUT_MS = 999 * 1000; //999s
	public static final int CLIPBOARD_TIMEOUT_EXTEND_MS = 3 * 60 * 1000; //3min, change notification text if modified
	public static final int CLIPBOARD_MAX_LENGTH = 64; 
	
	//popup activities
	public static final int POPUP_MAX_KEEP_ALIVE_EXTENSION_TIME = 10 * 60 * 1000; //do not allow to increase keep alive time by more than this
	public static final int POPUP_REMAINING_TIME_INITIAL_VALUE = 60 * 1000;
	public static final int POPUP_REMAINING_TIME_DISPLAY_THRESHOLD = 15 * 1000; //display time left as title when below this value
	
	//activities
	public static final String BROADCAST_FORCE_FINISH_ALL = "BROADCAST_FORCE_FINISH_ALL";
	public static final String BROADCAST_FORCE_FINISH_SECURE = "BROADCAST_FORCE_FINISH_SECURE";
	//clibpaord
	public static final String BROADCAST_CLIPBOARD_REMAINING_TIME = "BROADCAST_CLIPBOARD_REMAINING_TIME";
	
	//service
	public static final String SERVICE_FORCE_STOP = "service_force_stop";	
	public static final String SERVICE_START = "service_open_entry"; //entry opened, but service is not running
	public static final String SERVICE_START_BACKGROUND = "service_start_background"; //start service in background (not launched from KP2A)
	public static final String SERVICE_RESTART = "service_action_selected";  //action was selected, but service is not running	
	
	public static final String SERVICE_QUEUE_ITEM = "queue_item"; 
	public static final String SERVICE_ENTRY_ACTION = "entry_action";	
	public static final String SERVICE_DISMISS_SMS = "dismiss_sms";

	//Help
    public static final String HELP_URL = "http://www.inputstick.com/help";
	
	//AllActions 
	public static final String EXTRA_ACTION = "selected_action";	
	public static final String EXTRA_LAYOUT = "layout";
	//MaskedPassword & SMS
	public static final String EXTRA_TEXT = "text";
	//SMS
	public static final String EXTRA_SMS_SENDER = "sms_sender";

	//SMSProxy app
	public static final String SMS_PROXY_PACKAGE = "com.inputstick.apps.smsproxy";
	public static final String SMS_PROXY_SERVICE = "com.inputstick.apps.smsproxy.SMSService";

	public static final String SMS_PROXY_URL_INFO_AND_DOWNLOAD = "http://inputstick.com/sms-proxy";
	public static final String SMS_PROXY_URL_SOURCE = "https://github.com/inputstick/SMSProxy";

	public static final String SMS_PROXY_EXTRA_KP2A_KEY = "smsproxy_kp2a_key";
	public static final String SMS_PROXY_EXTRA_SMS_TEXT = "smsproxy_sms_text";
	public static final String SMS_PROXY_EXTRA_SMS_SENDER = "smsproxy_sms_sender";
	public static final String SMS_PROXY_EXTRA_HMAC = "smsproxy_hmac";

	public static final String SMS_PROXY_ACTION_KP2A_SMS_RELAY = "com.inputstick.apps.smsproxy.ACTION_KP2A_SMS_RELAY";

	public static final String SMS_PROXY_ACTION_ACTIVATE = "com.inputstick.apps.smsproxy.ACTION_ACTIVATE";
	public static final String SMS_PROXY_ACTION_DEACTIVATE = "com.inputstick.apps.smsproxy.ACTION_DEACTIVATE";
	public static final String SMS_PROXY_ACTION_FORCE_STOP = "com.inputstick.apps.smsproxy.ACTION_FORCE_STOP";
	public static final String SMS_PROXY_ACTION_KEEP_ALIVE = "com.inputstick.apps.smsproxy.ACTION_KEEP_ALIVE";
	
	//Macro/Template
	public static final String EXTRA_MACRO_DATA = "macro_data";	
	public static final String EXTRA_ENTRY_ID = "entry_id";	
	public static final String EXTRA_MACRO_RUN_BUT_EMPTY = "macro_run_empty";	
	public static final String EXTRA_MACRO_TEMPLATE_MODE = "macro_template_mode";
	public static final String EXTRA_TEMPLATE_ID = "template_id";
	public static final String EXTRA_TEMPLATE_MANAGE = "template_manage";					
	
	public static final String EXTRA_LAUNCHED_FROM_KP2A = "kp2a_launch";	
		
	public static final String EXTRA_TYPE_SLOW = "type_slow";
	public static final String EXTRA_TYPE_MASKED = "type_masked";
	public static final String EXTRA_ADD_KEY = "add_key";
	public static final String EXTRA_SHOW_CHANGELOG = "show_changelog";
	public static final String EXTRA_SHOW_SCOPE = "show_scope"; 	
	
	
	public static final int TYPING_SPEED_DEFAULT = 1;
	public static final int TYPING_SPEED_SLOW = 10;

	public static final String MACRO_PREF_PREFIX = "m_";
	public static final String TEMPLATE_PREF_PREFIX = "t_";
	public static final String TEMPLATE_NAME_PREF_PREFIX = "tn_";
	
	public static final String TEMPLATE_DEFAULT_NAME_PREF_PREFIX = "TEMPLATE: ";

	//Clibpaord
	public static final String EXTRA_CLIPBOARD_REMAINING_TIME = "remaining_time";
	
	
	//ITEMS:
	//general
	public static final String ITEM_SETTINGS = "settings";
	public static final String ITEM_CONNECTION = "con_disc";
	public static final String ITEM_MAC_SETUP = "osx";
	public static final String ITEM_TAB_ENTER = "tab_enter";
	public static final String ITEM_MACRO = "macro";
	public static final String ITEM_RUN_TEMPLATE = "run_template";
	public static final String ITEM_TEMPLATE_MANAGE = "manage_template";
	public static final String ITEM_REMOTE = "remote";
	
	
	
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
	public static final String ITEM_TYPE_MASKED = "type_masked";
	
	
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
	public static final String ACTION_REMOTE = "com.inputstick.apps.kp2aplugin.remote";
	
	
	public static final String ACTION_MACRO_ADDEDIT = "com.inputstick.apps.kp2aplugin.macro_addedit";	
	public static final String ACTION_CLIPBOARD = "com.inputstick.apps.kp2aplugin.clipboard";	
	public static final String ACTION_MACRO_RUN = "com.inputstick.apps.kp2aplugin.macro_run";
	public static final String ACTION_TEMPLATE_RUN = "com.inputstick.apps.kp2aplugin.template_run";
	public static final String ACTION_TEMPLATE_MANAGE = "com.inputstick.apps.kp2aplugin.template_manage";	
	
	public static final String ACTION_QUICK_SHORTCUT_1 = "com.inputstick.apps.kp2aplugin.quick_shortcut_1";
	public static final String ACTION_QUICK_SHORTCUT_2 = "com.inputstick.apps.kp2aplugin.quick_shortcut_2";
	public static final String ACTION_QUICK_SHORTCUT_3 = "com.inputstick.apps.kp2aplugin.quick_shortcut_3";
	
	public static final String ACTION_FIELD_TYPE_PRIMARY = "com.inputstick.apps.kp2aplugin.type";	
	public static final String ACTION_FIELD_TYPE_ENTER_PRIMARY = "com.inputstick.apps.kp2aplugin.type_enter";
	public static final String ACTION_FIELD_TYPE_TAB_PRIMARY = "com.inputstick.apps.kp2aplugin.type_tab";
	public static final String ACTION_FIELD_TYPE_SLOW_PRIMARY = "com.inputstick.apps.kp2aplugin.type_slow";
	public static final String ACTION_FIELD_TYPE_MASKED_PRIMARY = "com.inputstick.apps.kp2aplugin.type_masked";	
	
	public static final String ACTION_FIELD_TYPE_SECONDARY = "com.inputstick.apps.kp2aplugin.type_secondary";	
	public static final String ACTION_FIELD_TYPE_ENTER_SECONDARY = "com.inputstick.apps.kp2aplugin.type_enter_secondary";
	public static final String ACTION_FIELD_TYPE_TAB_SECONDARY = "com.inputstick.apps.kp2aplugin.type_tab_secondary";
	public static final String ACTION_FIELD_TYPE_SLOW_SECONDARY = "com.inputstick.apps.kp2aplugin.type_slow_secondary";
	public static final String ACTION_FIELD_TYPE_MASKED_SECONDARY = "com.inputstick.apps.kp2aplugin.type_masked_secondary";
	
	//notification actions:
	public static final String ACTION_SMS = "com.inputstick.apps.kp2aplugin.sms";	
	public static final String ACTION_CLIPBOARD_EXTEND = "com.inputstick.apps.kp2aplugin.clipboard_extend";	
	public static final String ACTION_CLIPBOARD_STOP = "com.inputstick.apps.kp2aplugin.clipboard_stop";


	//PREFERENCES:	
	
	public static final String PREF_AUTO_CONNECT = "connection_auto_connect";
	public static final int PREF_AUTO_CONNECT_VALUE = AUTO_CONNECT_DISABLED;
	
	public static final String PREF_SMART_AUTO_CONNECT = "connection_smart_auto_connect";
	public static final boolean PREF_SMART_AUTO_CONNECT_VALUE = true;
	
	public static final String PREF_MAX_IDLE_PERIOD = "connection_max_idle_period";
	public static final int PREF_MAX_IDLE_PERIOD_VALUE = 180000;				
	
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
	public static final String PREF_ITEMS_GENERAL_VALUE = "settings|osx|tab_enter|macro|remote";		

	public static final String PREF_ITEMS_ENTRY_PRIMARY = "items_entry_primary";
	public static final String PREF_ITEMS_ENTRY_PRIMARY_VALUE = "username_and_password|username_password_enter|masked_password|macro|run_template|clipboard";
	
	public static final String PREF_ITEMS_FIELD_PRIMARY = "items_field_primary";
	public static final String PREF_ITEMS_FIELD_PRIMARY_VALUE = "type_normal|type_enter|type_slow|type_masked";
	
	public static final String PREF_ITEMS_ENTRY_SECONDARY = "items_entry_secondary";
	public static final String PREF_ITEMS_ENTRY_SECONDARY_VALUE = "username_and_password";
	
	public static final String PREF_ITEMS_FIELD_SECONDARY = "items_field_secondary";
	public static final String PREF_ITEMS_FIELD_SECONDARY_VALUE = "type_normal";



	public static final String PREF_CLIPBOARD_SUMMARY = "summary_clipboard";

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
	//public static final String PREF_ENABLE_PLUGIN = "enable_plugin_pref";
	//public static final String PREF_ALERT_WINDOW_PERMISSION = "alert_window_permission_pref";
	public static final String PREF_PERMISSIONS = "permissions_pref";
	public static final String PREF_SHOW_ABOUT_KEY = "show_about_key";
	public static final String PREF_SHOW_HELP_WEBPAGE_KEY = "show_help_webpage_key";
	public static final String PREF_SHOW_CHANGELOG_PREFERENCE_KEY = "show_changelog_preference_key";
	public static final String PREF_RUN_REMOTE = "run_remote_key";

	public static final String CATEGORY_HELP_AND_INFO = "category_help_and_info";
    //public static final String CATEGORY_GENERAL = "category_general";
	public static final String CATEGORY_CONFIG = "category_config";
    public static final String CATEGORY_TYPING = "category_typing";
    public static final String CATEGORY_CONNECTION = "category_connection";
    public static final String CATEGORY_SMS = "category_sms";
    public static final String CATEGORY_CLIPBOARD = "category_clipboard";
    public static final String CATEGORY_QUICK_SHORTCUTS = "category_quick_shortcuts";
    public static final String CATEGORY_UI = "category_ui";
    public static final String CATEGORY_REMOTE = "category_remote";
    public static final String CATEGORY_TWEAKS = "category_tweaks";
	
	
	public static final String PREF_DO_NOT_REQUEST_DB_SCOPE = "db_scope_disabled";
	public static final boolean PREF_DO_NOT_REQUEST_DB_VALUE = false;		
	
	public static final String PREF_ENABLED_QUICK_SHORTCUTS = "enabled_quick_shortcuts";
	public static final String PREF_ENABLED_QUICK_SHORTCUTS_VALUE = "0";
	
	public static final String PREF_QUICK_SHORTCUT_1 = "quick_shortcut_1";
	public static final String PREF_QUICK_SHORTCUT_2 = "quick_shortcut_2";
	public static final String PREF_QUICK_SHORTCUT_3 = "quick_shortcut_3";
	public static final String PREF_QUICK_SHORTCUT_VALUE = "none";
	
	
	public static final String PREF_REMOTE_MOUSE_MODE = "remote_mouse_mode";
	public static final String PREF_REMOTE_MOUSE_MODE_VALUE = "mouse";
	
	public static final String PREF_REMOTE_MOUSE_SENSITIVITY = "remote_mouse_mode";
	public static final String PREF_REMOTE_MOUSE_SENSITIVITY_VALUE = "50";
	
	public static final String PREF_REMOTE_SCROLL_SENSITIVITY = "remote_mouse_mode";
	public static final String PREF_REMOTE_SCROLL_SENSITIVITY_VALUE = "50";
	
	public static final String PREF_REMOTE_USE_PRIMARY_LAYOUT = "remote_use_primary_layout";
	public static final boolean PREF_REMOTE_USE_PRIMARY_LAYOUT_VALUE = true;
	
	public static final String PREF_SMS_SMSPROXY = "sms_smsproxy";
    public static final String PREF_SMS_SMSPROXY_KEY = "smsproxy_key";
	public static final String PREF_SMS_INFO = "sms_info";

	public static final String PREF_TWEAKS_NEVER_STOP_PLUGIN = "never_stop_plugin";
	public static final boolean PREF_TWEAKS_NEVER_STOP_PLUGIN_VALUE = false;

	public static final String PREF_TWEAKS_SHOW_DEBUG_MESSAGES = "show_debug_messages";
	public static final boolean PREF_TWEAKS_SHOW_DEBUG_MESSAGES_VALUE = false;

	
}
