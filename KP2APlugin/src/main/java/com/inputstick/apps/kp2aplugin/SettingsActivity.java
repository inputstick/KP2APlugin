package com.inputstick.apps.kp2aplugin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.inputstick.api.Util;
import com.inputstick.api.hid.HIDKeycodes;
import com.inputstick.api.layout.KeyboardLayout;
import com.inputstick.apps.kp2aplugin.remote.RemoteActivity;

import java.security.SecureRandom;
import java.util.Arrays;

import keepass2android.pluginsdk.AccessManager;
import keepass2android.pluginsdk.Strings;
import sheetrock.panda.changelog.ChangeLog;


@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private static final String DISMISSED_KEY = "dismissed";	
	private static final String DISPLAY_RELOAD_INFO_KEY = "displayReloadInfo";
	
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
	
	private Preference prefQuickShortcut1;
	private Preference prefQuickShortcut2;
	private Preference prefQuickShortcut3;
	
	private Preference prefSMSProxy;
	private static String smsProxyTmpKey;
	
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
		addPreferencesFromResource(R.xml.preferences);
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
		if (requestCode == Const.REQUEST_CODE_SMS_PROXY_ACTIVATE) {
			if ((resultCode == RESULT_OK)) {
				PreferencesHelper.setSMSProxyKey(prefs, smsProxyTmpKey);
				prefSMSProxy.setSummary(R.string.sms_smsproxy_summary_enabled);
			} else {
				showSMSProxyErrorDialog();
			}
		}
		if (requestCode == Const.REQUEST_CODE_SELECT_APP) {
			if ((resultCode == RESULT_OK)) {
				String packageName = data.getStringExtra(SelectAppActivity.RESULT_PACKAGE);
				String name = getNameForPackage(packageName);
				Editor edit = prefs.edit();
				edit.putString(Const.PREF_CLIPBOARD_CUSTOM_APP_PACKAGE, packageName).apply();
				edit.putString(Const.PREF_CLIPBOARD_CUSTOM_APP_NAME, name).apply();
				edit.apply();
				setCustomAppPackageSummary();
			}
		}
	}

	
	private void setupSimplePreferencesScreen() {
		Preference pref;			
		PackageManager mgr = getPackageManager();
		
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
		
		setListSummary(Const.PREF_ENABLED_QUICK_SHORTCUTS);
        		
		pref = findPreference(Const.PREF_ENABLE_PLUGIN_PREF);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				enableAsPlugin();
				return true;
			}
		});		
		
		pref = (Preference)findPreference(Const.PREF_RUN_REMOTE);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(SettingsActivity.this, RemoteActivity.class));
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

		//tweaks:
		pref = (Preference)findPreference(Const.PREF_TWEAKS_NEVER_STOP_PLUGIN);
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean enabled = (Boolean)newValue;
				Intent intent = new Intent(SettingsActivity.this, InputStickService.class);
				if (enabled) {
					intent.setAction(Const.SERVICE_START_BACKGROUND);
				} else {
					intent.setAction(Const.SERVICE_FORCE_STOP);
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					startForegroundService(intent);
				} else {
					startService(intent);
				}
				return true;
			}
		});

		//SMS		
		prefSMSProxy = findPreference(Const.PREF_SMS_SMSPROXY);
		prefSMSProxy.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (PluginHelper.isPackageInstalled(SettingsActivity.this, Const.SMS_PROXY_PACKAGE)) {
					if (PreferencesHelper.isSMSProxyEnabled(prefs)) {
						PreferencesHelper.setSMSProxyKey(prefs, null);
                        Intent intent = new Intent(Const.SMS_PROXY_ACTION_DEACTIVATE);
                        startActivity(intent);
					} else {
						smsProxyTmpKey = generateSMSProxyTmpKey();
                        Intent intent = new Intent(Const.SMS_PROXY_ACTION_ACTIVATE);
                        intent.putExtra(Const.SMS_PROXY_EXTRA_KP2A_KEY, smsProxyTmpKey);
                        startActivityForResult(intent, Const.REQUEST_CODE_SMS_PROXY_ACTIVATE);
					}
				} else {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Const.SMS_PROXY_URL_INFO_AND_DOWNLOAD));
					startActivity(browserIntent);
				}
				return true;
			}
		});

		//disable if SMS is not supported
		if ( !mgr.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
			prefSMSProxy.setEnabled(false);
			pref = findPreference(Const.PREF_SMS_INFO);
			pref.setSummary(R.string.sms_not_supported);	
		}
	
		
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
		
		//quick shortcuts
		prefQuickShortcut1 = findPreference(Const.PREF_QUICK_SHORTCUT_1);
		prefQuickShortcut1.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				editShortcut(1, PreferencesHelper.getQuickShortcut(prefs, 1));
				return true;
			}
		});
		
		prefQuickShortcut2 = findPreference(Const.PREF_QUICK_SHORTCUT_2);
		prefQuickShortcut2.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				editShortcut(2, PreferencesHelper.getQuickShortcut(prefs, 2));
				return true;
			}
		});
		
		prefQuickShortcut3 = findPreference(Const.PREF_QUICK_SHORTCUT_3);
		prefQuickShortcut3.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				editShortcut(3, PreferencesHelper.getQuickShortcut(prefs, 3));
				return true;
			}
		});		
				
		manageQuickShortcuts(null);
		
		pref = findPreference(Const.PREF_ENABLED_QUICK_SHORTCUTS);
		pref.setOnPreferenceClickListener(reloadInfoListener);
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				manageQuickShortcuts((String)newValue);
        		return true;
			}
   		     });
		
		
		//UI:
		findPreference(Const.PREF_DISPLAY_IS_TEXT).setOnPreferenceClickListener(reloadInfoListener);
		findPreference(Const.PREF_ITEMS_GENERAL).setOnPreferenceClickListener(reloadInfoListener);
		findPreference(Const.PREF_ITEMS_ENTRY_PRIMARY).setOnPreferenceClickListener(reloadInfoListener);
		findPreference(Const.PREF_ITEMS_FIELD_PRIMARY).setOnPreferenceClickListener(reloadInfoListener);
		prefUiEntrySecondary = findPreference(Const.PREF_ITEMS_ENTRY_SECONDARY);
		prefUiEntrySecondary.setOnPreferenceClickListener(reloadInfoListener);
		prefUiFieldSecondary = findPreference(Const.PREF_ITEMS_FIELD_SECONDARY);
		prefUiFieldSecondary.setOnPreferenceClickListener(reloadInfoListener);					
		
		//remote:
		setListSummary(Const.PREF_REMOTE_MOUSE_MODE);
		
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
		Preference pref;
		pref = findPreference(Const.PREF_PRIMARY_LAYOUT);
		pref.setSummary(layoutNames[selectedLayout]);		


		pref  = findPreference(Const.PREF_SMS_INFO);
		if (pref.isEnabled()) { //device supports SMS
			if (PluginHelper.isPackageInstalled(this, Const.SMS_PROXY_PACKAGE)) {
				if (PreferencesHelper.isSMSProxyEnabled(prefs)) {
					prefSMSProxy.setSummary(R.string.sms_smsproxy_summary_enabled);
				} else {
					prefSMSProxy.setSummary(R.string.sms_smsproxy_summary_disabled);
				}
			} else {
				prefSMSProxy.setSummary(R.string.sms_smsproxy_summary_not_installed);
				//SMS Proxy is not installed but key is still saved -> remove key
				if (PreferencesHelper.isSMSProxyEnabled(prefs)) {
					PreferencesHelper.setSMSProxyKey(prefs, null);
				}
			}
		}
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
		if (pref instanceof MultiSelectListPreference) {
			return;
		}
		if (pref instanceof ListPreference) {
			listPref = (ListPreference) pref;
			String summary = listPref.getEntry().toString();
			int tmp = summary.indexOf('\n');
			if (tmp > 0) {
				summary = summary.substring(0, tmp);
			}			
			pref.setSummary(summary);
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
	
	private void manageQuickShortcuts(String value) {
		int cnt = 0;
		if (value == null) {
			cnt = PreferencesHelper.getEnabledQuickShortcuts(prefs);
		} else {
			try {
				cnt = Integer.parseInt(value);
			} catch (Exception e) {				
			}
		}
		String summary;
		
		if (cnt > 0) {
			prefQuickShortcut1.setEnabled(true);
			summary = PreferencesHelper.getQuickShortcut(prefs, 1);	
			prefQuickShortcut1.setSummary(summary);
		} else {
			prefQuickShortcut1.setEnabled(false);
			prefQuickShortcut1.setSummary(R.string.quickshortcut_disabled);
		}
		if (cnt > 1) {
			prefQuickShortcut2.setEnabled(true);
			summary = PreferencesHelper.getQuickShortcut(prefs, 2);
			prefQuickShortcut2.setSummary(summary);
		} else {
			prefQuickShortcut2.setEnabled(false);
			prefQuickShortcut2.setSummary(R.string.quickshortcut_disabled);
		}		
		if (cnt > 2) {
			prefQuickShortcut3.setEnabled(true);
			summary = PreferencesHelper.getQuickShortcut(prefs, 3);
			prefQuickShortcut3.setSummary(summary);
		} else {
			prefQuickShortcut3.setEnabled(false);
			prefQuickShortcut3.setSummary(R.string.quickshortcut_disabled);
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
			startActivityForResult(i, Const.REQUEST_CODE_ENABLE_PLUGIN);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}


	private String generateSMSProxyTmpKey() {
		final String charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder(16);
		for( int i = 0; i < 16; i++ ) {
			sb.append(charset.charAt(rnd.nextInt(charset.length())));
		}
		return sb.toString();
	}

	private void showSMSProxyErrorDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.error);
		alert.setMessage(R.string.sms_smsproxy_text_denied);
		alert.setNeutralButton(R.string.ok, null);
		alert.show();
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
	
	
	private void editShortcut(final int id, final String value) {
		final Context ctx = SettingsActivity.this;
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
		switch (id) {
			case 1:
				alert.setTitle(R.string.quickshortcut_1);
				break;
			case 2:
				alert.setTitle(R.string.quickshortcut_2);
				break;
			case 3:
				alert.setTitle(R.string.quickshortcut_3);
				break;
		}
		
		final LinearLayout lin= new LinearLayout(ctx);
		lin.setOrientation(LinearLayout.VERTICAL);
		
		final TextView tvInfo = new TextView(ctx);				
		tvInfo.setText(R.string.custom_key_message);		
		final TextView tvLayoutInfo = new TextView(ctx);				
		tvLayoutInfo.setText(R.string.custom_key_layout_message);
		
		
		final Spinner spinner = new Spinner(ctx);				
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, MacroHelper.getKeyList());
		spinner.setAdapter(adapter);
		
		final CheckBox cbCtrlLeft = new CheckBox(ctx);
		cbCtrlLeft.setText("Ctrl");
		final CheckBox cbShiftLeft = new CheckBox(ctx);
		cbShiftLeft.setText("Shift");
		final CheckBox cbAltLeft = new CheckBox(ctx);
		cbAltLeft.setText("Alt");
		final CheckBox cbGuiLeft = new CheckBox(ctx);
		cbGuiLeft.setText("GUI (Win key)");
		final CheckBox cbAltRight = new CheckBox(ctx);
		cbAltRight.setText("AltGr (right)");
		
		lin.addView(tvInfo);		
		lin.addView(spinner);
		lin.addView(cbCtrlLeft);
		lin.addView(cbShiftLeft);	
		lin.addView(cbAltLeft);	
		lin.addView(cbGuiLeft);	
		lin.addView(cbAltRight);
		lin.addView(tvLayoutInfo);
				
		if (value != null) {
			byte modifiers = MacroHelper.getModifiers(value);
			byte key = MacroHelper.getKey(value);
			cbCtrlLeft.setChecked((modifiers & HIDKeycodes.CTRL_LEFT) != 0);
			cbShiftLeft.setChecked((modifiers & HIDKeycodes.SHIFT_LEFT) != 0);
			cbAltLeft.setChecked((modifiers & HIDKeycodes.ALT_LEFT) != 0);
			cbGuiLeft.setChecked((modifiers & HIDKeycodes.GUI_LEFT) != 0);
			cbAltRight.setChecked((modifiers & HIDKeycodes.ALT_RIGHT) != 0);
			int selectedPosition = MacroHelper.getIndexForKey(key);
			if (selectedPosition >= 0) {
				spinner.setSelection(selectedPosition);
			} else {
				spinner.setSelection(0);
			}
		}				
		
		alert.setView(lin);
		
		alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			private String param;
			
			private void add(String toAdd) {
				if (param.length() > 0) {
					param += "+";
				}
				param += toAdd;
			}
			
			public void onClick(DialogInterface dialog, int whichButton) {
				param = "";
				if (cbCtrlLeft.isChecked()) add("Ctrl");
				if (cbShiftLeft.isChecked()) add("Shift");
				if (cbAltLeft.isChecked()) add("Alt");
				if (cbGuiLeft.isChecked()) add("GUI");
				if (cbAltRight.isChecked()) add("AltGr");	
				add((String)spinner.getSelectedItem());				
				//store value & update summary fields				
				PreferencesHelper.setQuickShortcut(prefs, id, param);
				manageQuickShortcuts(null);
				//entry must be reloaded to show new value
				displayReloadInfo = true;
			}
		});
		alert.setNegativeButton(R.string.cancel, null);
		alert.show();		
	}
	
		
	//selecting app for clipboard action:
	
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
			startActivityForResult(i, Const.REQUEST_CODE_SELECT_APP);
		}
		
	}
	
}
