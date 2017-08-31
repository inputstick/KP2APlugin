package com.inputstick.apps.kp2aplugin;

import java.util.Arrays;

import keepass2android.pluginsdk.AccessManager;
import keepass2android.pluginsdk.Strings;
import sheetrock.panda.changelog.ChangeLog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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


@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private static final String DISMISSED_KEY = "dismissed";	
	private static final String DISPLAY_RELOAD_INFO_KEY = "displayReloadInfo";
	
	private static final int REQUEST_CODE_ENABLE_PLUGIN = 123;
	private static final int REQUEST_CODE_SELECT_APP = 124;
	
	private static final int TIP_TYPING_SPEED = 1;	
	
	private SharedPreferences prefs;	
	private boolean setupCompleted;	
	private Preference prefShowSecondary;
	private Preference prefSecondaryKbdLayout;	
	private Preference prefUiEntrySecondary;
	private Preference prefUiFieldSecondary;
	private CheckBoxPreference prefLaunchAuthenticator;
	private CheckBoxPreference prefLaunchCustomApp;
	private Preference prefCustomAppPackage;
	
	private boolean dismissed;	
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
		prefs = PreferenceManager.getDefaultSharedPreferences(this);		
		addPreferencesFromResource(R.layout.activity_settings);
		setupSimplePreferencesScreen();		
		
		if (savedInstanceState != null) {
			dismissed = savedInstanceState.getBoolean(DISMISSED_KEY);
			displayReloadInfo = savedInstanceState.getBoolean(DISPLAY_RELOAD_INFO_KEY);
		} 
		
		ChangeLog cl = new ChangeLog(this);
		setupCompleted = prefs.getBoolean(Const.PREF_SETUP_COMPLETED, false);
		if ( !setupCompleted) {
			//start setup only if the plugin is not enabled in kp2a; otherwise assume it was already completed manually somehow
			if (AccessManager.getAllHostPackages(SettingsActivity.this).isEmpty()) { 
				Intent intent = new Intent(this, SetupWizardActivity.class);
				startActivity(intent);
			}
		} else {	
			Intent intent = getIntent();
			try {
				// this version launched for the very first time, from kp2a
				if (intent.getBooleanExtra(Const.EXTRA_SHOW_CHANGELOG, false)) {
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
			
			if ((intent.getBooleanExtra(Const.EXTRA_SHOW_SCOPE, false)) && ( !dismissed)) {
				showRequestDbScopeDialog();
			}		
			
			if ((intent.getBooleanExtra(Const.EXTRA_SHOW_NOTIFICATION_INFO, false)) && ( !dismissed)) {
				showNotificationInfoDialog();
			}	
			
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putBoolean(DISMISSED_KEY, dismissed);
	    savedInstanceState.putBoolean(DISPLAY_RELOAD_INFO_KEY, displayReloadInfo);
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == REQUEST_CODE_SELECT_APP) && (resultCode == RESULT_OK)) {	
			String packageName = data.getStringExtra(SelectAppActivity.RESULT_PACKAGE);
			String name = getNameForPackage(packageName);
			Editor edit = prefs.edit();
			edit.putString(Const.PREF_CLIPBOARD_CUSTOM_APP_PACKAGE, packageName).apply();
			edit.putString(Const.PREF_CLIPBOARD_CUSTOM_APP_NAME, name).apply();
			edit.apply();									
			setCustomAppPackageSummary();
		}		
	}

	
	private void setupSimplePreferencesScreen() {
		Preference pref;	
		
		ListPreference listPref;
		listPref = (ListPreference)findPreference(Const.PREF_PRIMARY_LAYOUT);
		listPref.setEntries(KeyboardLayout.getLayoutNames(true));
		listPref.setEntryValues(KeyboardLayout.getLayoutCodes());
		listPref = (ListPreference)findPreference(Const.PREF_SECONDARY_LAYOUT);
		listPref.setEntries(KeyboardLayout.getLayoutNames(true));
		listPref.setEntryValues(KeyboardLayout.getLayoutCodes());
		
		setListSummary(Const.PREF_PRIMARY_LAYOUT);
		setListSummary(Const.PREF_SECONDARY_LAYOUT);
		setListSummary(Const.PREF_TYPING_SPEED);
		
		setListSummary(Const.PREF_AUTO_CONNECT);    
		setListSummary(Const.PREF_MAX_IDLE_PERIOD);
		
        		
		pref = findPreference(Const.PREF_ENABLE_PLUGIN_PREF);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				enableAsPlugin();
				return true;
			}
		});
		
		pref = (Preference) findPreference(Const.PREF_SHOW_CHANGELOG_PREFERENCE_KEY);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {				
				new ChangeLog(SettingsActivity.this).getFullLogDialog().show();
				return true;
			}
		});
		
		pref = (Preference)findPreference(Const.PREF_SHOW_HELP_WEBPAGE_KEY);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.inputstick.com/help")));			
				return true;
			}
		});		
		
		pref = (Preference)findPreference(Const.PREF_SHOW_ABOUT_KEY);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {				
				startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
				return true;
			}
		});				

		//typing:		
		findPreference(Const.PREF_PRIMARY_LAYOUT).setOnPreferenceClickListener(reloadInfoListener);
		prefShowSecondary = (Preference) findPreference(Const.PREF_SHOW_SECONDARY_LAYOUT);
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
					tmp = prefs.getString(Const.PREF_ITEMS_FIELD_SECONDARY, null);
					if ((tmp != null) && (tmp.length() > 1)) {
						showMessage = false;
					}
					tmp = prefs.getString(Const.PREF_ITEMS_ENTRY_SECONDARY, null);
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
		prefSecondaryKbdLayout = findPreference(Const.PREF_SECONDARY_LAYOUT);
		prefSecondaryKbdLayout.setOnPreferenceClickListener(reloadInfoListener);	
		
		//clipboard:
		prefLaunchAuthenticator = (CheckBoxPreference)findPreference(Const.PREF_CLIPBOARD_LAUNCH_AUTHENTICATOR);
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
		
		prefLaunchCustomApp = (CheckBoxPreference)findPreference(Const.PREF_CLIPBOARD_LAUNCH_CUSTOM_APP);
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
		
		
		prefCustomAppPackage = (Preference)findPreference(Const.PREF_CLIPBOARD_CUSTOM_APP_PACKAGE);
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
		findPreference(Const.PREF_DISPLAY_IS_TEXT).setOnPreferenceClickListener(reloadInfoListener);
		findPreference(Const.PREF_ITEMS_GENERAL).setOnPreferenceClickListener(reloadInfoListener);
		findPreference(Const.PREF_ITEMS_ENTRY_PRIMARY).setOnPreferenceClickListener(reloadInfoListener);
		findPreference(Const.PREF_ITEMS_FIELD_PRIMARY).setOnPreferenceClickListener(reloadInfoListener);
		prefUiEntrySecondary = findPreference(Const.PREF_ITEMS_ENTRY_SECONDARY);
		prefUiEntrySecondary.setOnPreferenceClickListener(reloadInfoListener);
		prefUiFieldSecondary = findPreference(Const.PREF_ITEMS_FIELD_SECONDARY);
		prefUiFieldSecondary.setOnPreferenceClickListener(reloadInfoListener);						
		
		//enable/disable preferences
		setSecondaryLayoutEnabled(PreferencesHelper.isSecondaryLayoutEnabled(prefs));			
		setLaunchCustomAppEnabled(PreferencesHelper.isClipboardLaunchCustomApp(prefs));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);		
		displayReloadInfo = false;
		Preference enablePref = findPreference(Const.PREF_ENABLE_PLUGIN_PREF);
		if (AccessManager.getAllHostPackages(SettingsActivity.this).isEmpty()) {
			enablePref.setSummary(R.string.not_configured);
		} else {
			enablePref.setSummary(R.string.enabled);
			if ( !setupCompleted) {
				setupCompleted = true;				
				PreferencesHelper.setSetupCompleted(prefs);
			}
		}
		
		//handle layout change made in setup wizard
		String layoutCode = PreferencesHelper.getPrimaryLayoutCode(prefs);
		String[] layoutValues = Util.convertToStringArray(KeyboardLayout.getLayoutCodes());	
		String[] layoutNames = Util.convertToStringArray(KeyboardLayout.getLayoutNames(true));	
		int selectedLayout = Arrays.asList(layoutValues).indexOf(layoutCode);
		Preference pref  = findPreference(Const.PREF_PRIMARY_LAYOUT);
		pref.setSummary(layoutNames[selectedLayout]);		
	}
	
	@Override
	protected void onPause() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();				
	}
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Const.PREF_TYPING_SPEED)) {
        	if (PreferencesHelper.getTypingSpeed(sharedPreferences) == 0) {
        		displayTip(TIP_TYPING_SPEED);
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
			String summary = listPref.getEntry().toString();
			int tmp = summary.indexOf('\n');
			if (tmp > 0) {
				summary = summary.substring(0, tmp);
			}			
			pref.setSummary(summary);
			//pref.setSummary(listPref.getEntry());
		}
	}
	
	private void setCustomAppPackageSummary() {
		String appPackage = PreferencesHelper.getClipboardCustomAppPackage(prefs);
		if (Const.PREF_CLIPBOARD_CUSTOM_APP_PACKAGE_VALUE.equals(appPackage)) {
			prefCustomAppPackage.setSummary(R.string.clipboard_custom_app_not_selected);
		} else {
			prefCustomAppPackage.setSummary(getNameForPackage(appPackage));
		}
	}
	
	
	@Override
	public void onBackPressed() {
		boolean kp2a = getIntent().getBooleanExtra(Const.EXTRA_LAUNCHED_FROM_KP2A, false); //show warning only if activity was launched from kp2a app, 
		boolean showWarning = prefs.getBoolean(Const.PREF_SHOW_RELOAD_WARNING, Const.PREF_SHOW_RELOAD_WARNING_VALUE); //show warning only if user did not checked "do not show again" before
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
							SharedPreferences.Editor editor = prefs.edit();
							editor.putBoolean(Const.PREF_SHOW_RELOAD_WARNING, false);
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
	
	private void enableAsPlugin() {
		try {
			Intent i = new Intent(Strings.ACTION_EDIT_PLUGIN_SETTINGS);
			i.putExtra(Strings.EXTRA_PLUGIN_PACKAGE, SettingsActivity.this.getPackageName());
			startActivityForResult(i, REQUEST_CODE_ENABLE_PLUGIN);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private void showRequestDbScopeDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.missing_access_scope_title);
		alert.setMessage(R.string.missing_access_scope_message);
		alert.setPositiveButton(R.string.yes, new OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissed = true;
				enableAsPlugin();					
			}
		});
		alert.setNegativeButton(R.string.never, new OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissed = true;
				PreferencesHelper.disableDbScopeDialog(prefs);
			}
		});
		alert.setNeutralButton(R.string.later, new OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissed = true;
			}
		});
		alert.show();		
	}
	
	private void showNotificationInfoDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.app_name);
		alert.setMessage(R.string.text_notification_info);
		alert.setPositiveButton(R.string.ok, new OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissed = true;			
			}
		});
		alert.setNegativeButton(R.string.text_stop_plugin, new OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismissed = true;
				ActionHelper.forceStopService(SettingsActivity.this);
			}
		});
		alert.show();		
	}
	
	
	private void displayTip(int tipId) {
		//String key = "show_tip_" + tipId; 		
		int resId = R.string.tip_empty;
		switch (tipId) {
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
