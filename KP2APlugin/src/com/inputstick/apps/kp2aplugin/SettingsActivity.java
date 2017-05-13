package com.inputstick.apps.kp2aplugin;

import java.util.Arrays;

import keepass2android.pluginsdk.AccessManager;
import keepass2android.pluginsdk.Strings;
import sheetrock.panda.changelog.ChangeLog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.inputstick.api.Util;
import com.inputstick.api.layout.KeyboardLayout;
import com.inputstick.apps.kp2aplugin.slides.SlidesUtils;


@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {	
	
	private static final int REQUEST_CODE_ENABLE_PLUGIN = 123;
	private static final int REQUEST_CODE_SELECT_APP = 124;
	
	private static final int TIP_DATA_TRANSFER_METHOD = 1;
	private static final int TIP_TYPING_SPEED = 2;
	
	
	public static final String ITEMS_GENERAL = "items_general";
	public static final String ITEMS_ENTRY_PRIMARY = "items_entry_primary";
	public static final String ITEMS_FIELD_PRIMARY = "items_field_primary";
	public static final String ITEMS_ENTRY_SECONDARY = "items_entry_secondary";
	public static final String ITEMS_FIELD_SECONDARY = "items_field_secondary";
	
	//IMPORTANT: checked using .contains() !
	//username_password_only
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
	public static final String ITEM_TYPE_SLOW = "type_slow";
		
	
	private SharedPreferences sharedPref;
	
	private boolean setupCompleted;
	
	private Preference prefShowSecondary;
	private Preference prefSecondaryKbdLayout;
	
	private Preference prefAutoconnectTimeout;	
	private Preference prefUiEntrySecondary;
	private Preference prefUiFieldSecondary;
	private CheckBoxPreference prefLaunchAuthenticator;
	private CheckBoxPreference prefLaunchCustomApp;
	private Preference prefCustomAppPackage;
	
	private boolean displayReloadInfo;
	private OnPreferenceClickListener reloadInfoListener = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			displayReloadInfo = true;
			return false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);		
		addPreferencesFromResource(R.layout.activity_settings);
		setupSimplePreferencesScreen();		
		
		ChangeLog cl = new ChangeLog(this);
		setupCompleted = sharedPref.getBoolean("setup_completed", false);
		if ( !setupCompleted) {
			//start setup only if the plugin is not enabled in kp2a; otherwise assume it was already completed manually somehow
			if (AccessManager.getAllHostPackages(SettingsActivity.this).isEmpty()) { 
				Intent intent = new Intent(this, SetupWizardActivity.class);
				startActivity(intent);
			}
		} else {			
			try {
				// this version launched for the very first time, from kp2a
				if (getIntent().getBooleanExtra(Const.EXTRA_SHOW_CHANGELOG, false)) {
					cl.getLogDialog().show();
				} else {
					// this version launched for the very first time, from launcher					
					if (( !cl.firstRunEver()) && (cl.firstRun())) {
						cl.getLogDialog().show();
					}
				}
				cl.updateVersionInPreferences();
			} catch (Exception e) {
				Toast.makeText(this, "Couldn't show changelog!", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == REQUEST_CODE_SELECT_APP) && (resultCode == RESULT_OK)) {	
			String packageName = data.getStringExtra(SelectAppActivity.RESULT_PACKAGE);
			String name = getNameForPackage(packageName);
			Editor edit = sharedPref.edit();
			edit.putString("clipboard_custom_app_package", packageName).apply();
			edit.putString("clipboard_custom_app_name", name).apply();
			edit.apply();									
			setCustomAppPackageSummary();
		}		
	}

	
	private void setupSimplePreferencesScreen() {
		Preference pref;	
		
		ListPreference listPref;
		listPref = (ListPreference)findPreference("kbd_layout");
		listPref.setEntries(KeyboardLayout.getLayoutNames(true));
		listPref.setEntryValues(KeyboardLayout.getLayoutCodes());
		listPref = (ListPreference)findPreference("secondary_kbd_layout");
		listPref.setEntries(KeyboardLayout.getLayoutNames(true));
		listPref.setEntryValues(KeyboardLayout.getLayoutCodes());
		
		setListSummary("kbd_layout");
		setListSummary("secondary_kbd_layout");
		setListSummary("typing_speed");
		setListSummary("autoconnect_timeout");		
		setListSummary("transfer_method");
		
        		
		pref = findPreference("enable_plugin_pref");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try {
					Intent i = new Intent(Strings.ACTION_EDIT_PLUGIN_SETTINGS);
					i.putExtra(Strings.EXTRA_PLUGIN_PACKAGE, SettingsActivity.this.getPackageName());
					startActivityForResult(i, REQUEST_CODE_ENABLE_PLUGIN);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		});
		
		pref = (Preference) findPreference("show_changelog_preference_key");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {				
				new ChangeLog(SettingsActivity.this).getFullLogDialog().show();
				return true;
			}
		});
		
		pref = (Preference)findPreference("show_help_webpage_key");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.inputstick.com/help")));			
				return true;
			}
		});		
		
		pref = (Preference)findPreference("show_about_key");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {				
				startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
				return true;
			}
		});					

		//typing:		
		findPreference("kbd_layout").setOnPreferenceClickListener(reloadInfoListener);
		prefShowSecondary = (Preference) findPreference("show_secondary");
		prefShowSecondary.setOnPreferenceClickListener(reloadInfoListener);
		prefShowSecondary.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean enabled = (Boolean)newValue;
				setSecondaryLayoutEnabled(enabled);
				if (enabled) {
					//check if at least one action is enabled
					boolean showMessage = true;
					String tmp;
					tmp = sharedPref.getString(SettingsActivity.ITEMS_FIELD_SECONDARY, null);
					if ((tmp != null) && (tmp.length() > 1)) {
						showMessage = false;
					}
					tmp = sharedPref.getString(SettingsActivity.ITEMS_ENTRY_SECONDARY, null);
					if ((tmp != null) && (tmp.length() > 1)) {
						showMessage = false;
					}					
					if (showMessage) {
						AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
						
						alert.setTitle(R.string.configuration_title);
						alert.setMessage(R.string.secondary_layout_action_reminder_message);
						alert.setPositiveButton(R.string.ok, null);
						alert.show();
					}
				}
        		return true;
			}
        });
		prefSecondaryKbdLayout = findPreference("secondary_kbd_layout");
		prefSecondaryKbdLayout.setOnPreferenceClickListener(reloadInfoListener);	
		
		//connection:
		pref = (Preference) findPreference("autoconnect");
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				setAutoconnectTimeoutEnabled((Boolean)newValue);
				displayReloadInfo = true;
        		return true;
			}
        });
		
		prefAutoconnectTimeout = (Preference) findPreference("autoconnect_timeout");
		prefAutoconnectTimeout.setOnPreferenceClickListener(reloadInfoListener);		
		
		pref = (Preference)findPreference("transfer_method");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				return displayTip(TIP_DATA_TRANSFER_METHOD, false);
			}
		});
		
		
		//clipboard:
		prefLaunchAuthenticator = (CheckBoxPreference)findPreference("clipboard_launch_authenticator");
		prefLaunchAuthenticator.setOnPreferenceClickListener(reloadInfoListener);
		prefLaunchAuthenticator.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean enabled = (Boolean)newValue;
				if (enabled) {
					prefLaunchCustomApp.setChecked(false);
					setLaunchCustomAppEnabled(false);
				}
        		return true;
			}
        });
		
		prefLaunchCustomApp = (CheckBoxPreference)findPreference("clipboard_launch_custom_app");
		prefLaunchCustomApp.setOnPreferenceClickListener(reloadInfoListener);
		prefLaunchCustomApp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean enabled = (Boolean)newValue;
				setLaunchCustomAppEnabled(enabled);
				if (enabled) {
					prefLaunchAuthenticator.setChecked(false);
				}
        		return true;
			}
        });
		
		
		prefCustomAppPackage = (Preference)findPreference("clipboard_custom_app_package");
		prefCustomAppPackage.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//loading list of all installed apps can take a few seconds
				new LoadAppsTask().execute();
				return true;
			}
		});
		setCustomAppPackageSummary();		
		
		
		//UI:
		findPreference("display_inputstick_text").setOnPreferenceClickListener(reloadInfoListener);
		findPreference("items_general").setOnPreferenceClickListener(reloadInfoListener);
		findPreference("items_entry_primary").setOnPreferenceClickListener(reloadInfoListener);
		findPreference("items_field_primary").setOnPreferenceClickListener(reloadInfoListener);
		prefUiEntrySecondary = findPreference("items_entry_secondary");
		prefUiEntrySecondary.setOnPreferenceClickListener(reloadInfoListener);
		prefUiFieldSecondary = findPreference("items_field_secondary");
		prefUiFieldSecondary.setOnPreferenceClickListener(reloadInfoListener);						
		
		//enable/disable preferences
		setSecondaryLayoutEnabled(sharedPref.getBoolean("show_secondary", false));		
		setAutoconnectTimeoutEnabled(sharedPref.getBoolean("autoconnect", true));		
		setLaunchCustomAppEnabled(sharedPref.getBoolean("clipboard_launch_custom_app", false));		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);		
		displayReloadInfo = false;
		Preference enablePref = findPreference("enable_plugin_pref");
		if (AccessManager.getAllHostPackages(SettingsActivity.this).isEmpty()) {
			enablePref.setSummary(R.string.not_configured);
		} else {
			enablePref.setSummary(R.string.enabled);
			if ( !setupCompleted) {
				setupCompleted = true;
				SlidesUtils.init(this);
				SlidesUtils.setAsCompleted();
			}
		}
		
		//handle layout change made in setup wizard
		String layout = sharedPref.getString("kbd_layout", "en-US");				
		String[] layoutValues = Util.convertToStringArray(KeyboardLayout.getLayoutCodes());	
		String[] layoutNames = Util.convertToStringArray(KeyboardLayout.getLayoutNames(true));	
		int selectedLayout = Arrays.asList(layoutValues).indexOf(layout);
		Preference pref  = findPreference("kbd_layout");
		pref.setSummary(layoutNames[selectedLayout]);		
	}
	
	@Override
	protected void onPause() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		ActionManager actionManager = ActionManager.getInstance(this);
		actionManager.reloadPreferences(sharedPref);
		super.onPause();				
	}
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("typing_speed")) {
        	if (sharedPreferences.getString("typing_speed", "1").equals("0")) {        		
        		displayTip(TIP_TYPING_SPEED, false);
        	}
        }
        
        setListSummary(key);
    }
	
	private void setListSummary(String key) {
		Preference pref;		
		ListPreference listPref;
		pref = findPreference(key);
		if (pref instanceof ListPreference) {
			listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
	}
	
	private void setCustomAppPackageSummary() {
		String packageName = sharedPref.getString("clipboard_custom_app_package", "none");
		if ("none".equals(packageName)) {
			prefCustomAppPackage.setSummary(R.string.clipboard_custom_app_not_selected);
		} else {
			prefCustomAppPackage.setSummary(getNameForPackage(packageName));
		}
	}
	
	
	@Override
	public void onBackPressed() {
		boolean kp2a = getIntent().getBooleanExtra(Const.EXTRA_LAUNCHED_FROM_KP2A, false); //show warning only if activity was launched from kp2a app, 
		boolean showWarning = sharedPref.getBoolean("show_reload_warning", true); //show warning only if user did not checked "do not show again" before
		//show warning only if it is necessary to reload entry
		if (kp2a && displayReloadInfo) {
			if (showWarning) {
				displayReloadInfo = false;
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(R.string.important_title);
				alert.setMessage(R.string.entry_reload_message);	
				
				final CheckBox cb = new CheckBox(this);
				cb.setText(R.string.do_not_remind);
				cb.setChecked(false);
				alert.setView(cb);
				
				alert.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						SettingsActivity.this.onBackPressed();	
						if (cb.isChecked()) {
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putBoolean("show_reload_warning", false);
							editor.apply();
						}
					}
					});
				alert.show();
			} else {
				//just toast, used does not want to see dialog msg
				Toast.makeText(this, R.string.entry_reload_message, Toast.LENGTH_LONG).show();
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}
	
	
	private void setAutoconnectTimeoutEnabled(boolean enabled) {
		prefAutoconnectTimeout.setEnabled( !enabled); // show this pref only if autoconnect is DISABLED
	}
	
	private void setSecondaryLayoutEnabled(boolean enabled) {
		prefUiEntrySecondary.setEnabled(enabled);
		prefUiFieldSecondary.setEnabled(enabled);		
		prefSecondaryKbdLayout.setEnabled(enabled);
	}	
	
	private void setLaunchCustomAppEnabled(boolean enabled) {
		prefCustomAppPackage.setEnabled(enabled);
	}
	
	private String getNameForPackage(String packageName) {
		final PackageManager pm = getPackageManager();	
		try {
		    return pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
		} catch (final NameNotFoundException e) {
			return getString(R.string.clipboard_custom_app_unknown);
		}	
	}
	
	
	private boolean displayTip(int tipId, boolean displayOnce) {
		String key = "show_tip_" + tipId; 		
		if (sharedPref.getBoolean(key, true)) {
			if (displayOnce) {
				//tip was not displayed yet, display it now and never again
				/*Editor editor = sharedPref.edit();
				editor.putBoolean(key, false);
				editor.apply();*/
			}
			
			int resId = R.string.tip_empty;
			switch (tipId) {
				case TIP_DATA_TRANSFER_METHOD:
					resId = R.string.tip_data_transfer_method; 
					break;
				case TIP_TYPING_SPEED:
					resId = R.string.tip_typing_speed; 
					break;					
			}						
			String message = getString(resId);			
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.tip_title);
			alert.setMessage(message);			
			alert.setNeutralButton(R.string.ok, null);	
			alert.show();				
			return true;
		} else {
			return false;
		}
	}
	
	
	
	
	
	class LoadAppsTask extends AsyncTask<String, String, String> {

		private ProgressDialog progDailog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDailog = new ProgressDialog(SettingsActivity.this);
			progDailog.setMessage(getString(R.string.text_please_wait));
			progDailog.setIndeterminate(true);
			progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progDailog.setCancelable(false);
			progDailog.show();
		}

		@Override
		protected String doInBackground(String... aurl) {
			SelectAppActivity.getInstalledApps(SettingsActivity.this);
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			super.onPostExecute(unused);
			progDailog.dismiss();
			Intent i = new Intent(SettingsActivity.this, SelectAppActivity.class);
			startActivityForResult(i, REQUEST_CODE_SELECT_APP);
		}
		
	}
	
}
