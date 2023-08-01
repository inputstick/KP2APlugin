package com.inputstick.apps.kp2aplugin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickError;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.basic.InputStickKeyboard;
import com.inputstick.api.hid.HIDKeycodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import keepass2android.pluginsdk.KeepassDefs;
import keepass2android.pluginsdk.Strings;

public class InputStickService extends Service implements InputStickStateListener {

	private static final String _TAG = "KP2AINPUTSTICK SERVICE";
	private static final int TIMER_INTERVAL_MS = 1000;
    private static final int CONNECTION_LOCK_DURATION_MS = 5000;
	
	private static final int ACTION_HID = 0;
	private static final int ACTION_OTHER = 1;

	private static SharedPreferences prefs;
	private static boolean addEnterAfterURL;
	private static int defaultTypingSpeed;
	private static int autoConnect;
	private static int maxIdlePeriod;
	private static boolean neverStopPlugin;


	//SMS:
	private static boolean smsEnabled;
	private static boolean smsReceiverRegistered;
	private static String smsText;
	private static String smsSender;
	private static int smsRemainingTime;
	
	//Clipboard
	private static ClipboardManager mClipboardManager;
	private static int clipboardRemainingTime;
	private static TypingParams mClipboardTypingParams;

	public static boolean isRunning;
	private static long dbClosedTime;		
	private static long lastActionTime;
	private static long serviceKeepAliveTime;			//if kp2a database was closed/locked but plugin is being kept alive (received SMS, typing from clipboard)
	private static long autoDisconnectTime;	//if user enabled auto-disconnect in settings

	private static final ArrayList<ItemToExecute> items = new ArrayList<>();
	private static long connectionAttemptLockTime; //prevents multiple connecton attempts (until conection state update is received)
	
	private static boolean addDummyKeys;
	private static int cnt;

	private static long lastCapsLockWarningTime;	

	private static NotificationManager mNotificationManager;
	private static NotificationCompat.Builder mPluginNotificationBuilder;
	private static NotificationCompat.Builder mSMSNotificationBuilder;
	private static NotificationCompat.Builder mClipboardNotificationBuilder;


	//************************************************************************************
	//************************************************************************************
	//stopping plugin / terminating InputStick connection:

	private static final Handler mHandler = new Handler();
	private final Runnable mTimerTask = new Runnable() {
		public void run() {
			final long time = System.currentTimeMillis();
			//update SMS notification & extend keep alive for service and InputStick connection
			if (smsRemainingTime > 0) {		
				extendConnectionTime(TIMER_INTERVAL_MS);
				extendServiceKeepAliveTime(TIMER_INTERVAL_MS);								
				smsRemainingTime -= TIMER_INTERVAL_MS;
				if (smsRemainingTime > 0) {
					updateSMSNotification();
				} else {
					clearSMS();
				}
			}
			//same for clipboard typing
			if (clipboardRemainingTime > 0) {		
				extendConnectionTime(TIMER_INTERVAL_MS);
				extendServiceKeepAliveTime(TIMER_INTERVAL_MS);							
				clipboardRemainingTime -= TIMER_INTERVAL_MS;
				if (clipboardRemainingTime > 0) {
					updateClipboardNotification();					
				} else {
					stopClipboardMonitoring(true);
				}
			}			
			
			//auto disconnect?
			if (InputStickHID.isConnected()) {																	
				if ((maxIdlePeriod > 0) && (time > autoDisconnectTime)) {
					Log.d(_TAG, "disconnect (inactivity)");
					InputStickHID.disconnect();
				}				
			}

			//stop plugin?
			if (time > serviceKeepAliveTime) {
				if ( !neverStopPlugin) {
					if (dbClosedTime > 0) {
						stopPlugin();
					} else if (time > lastActionTime + Const.SERVICE_FAILSAFE_PERIOD) {
						//fail safe, in case kp2a crashes
						stopPlugin();
					}
				}
			}													
			mHandler.postDelayed(mTimerTask, TIMER_INTERVAL_MS);
		}
	};
	
	//make sure InputStick connection will not be terminated within next %duration ms from now
	public static void extendConnectionTime(int duration) {
		if (isRunning) {
			long tmp = System.currentTimeMillis() + duration;
			if (tmp > autoDisconnectTime) {
				autoDisconnectTime = tmp;
			}
		}
	}
	
	//make sure the service will not be terminated within next %duration ms from now
	public static void extendServiceKeepAliveTime(int duration) {
		if (isRunning) {
			long tmp = System.currentTimeMillis() + duration;
			if (tmp > serviceKeepAliveTime) {
				serviceKeepAliveTime = tmp;
			}
		}
	}
	
	public static void onHIDAction() {
		if (isRunning) {
			lastActionTime = System.currentTimeMillis();
			autoDisconnectTime = lastActionTime + maxIdlePeriod;
		}
	}
	
	private void onAction(int actionType) {
		lastActionTime = System.currentTimeMillis();
		if (actionType == ACTION_HID) {
			autoDisconnectTime = lastActionTime + maxIdlePeriod;
		}
	}
	
	private void sendForceFinishBroadcast(boolean finishAll) {
		try {
			Intent intent = new Intent();
			if (finishAll) {
				intent.setAction(Const.BROADCAST_FORCE_FINISH_ALL);
			} else {
				intent.setAction(Const.BROADCAST_FORCE_FINISH_SECURE);
			}
			sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private final BroadcastReceiver kp2aReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.d(_TAG, "received action " + action);
			if (action != null) {				
				if (action.equals(Strings.ACTION_OPEN_ENTRY)) {
					dbClosedTime = 0;
					onEntryOpened();
				} else if (action.equals(Strings.ACTION_ENTRY_ACTION_SELECTED)) {
					if (PreferencesHelper.showDebugMessages(prefs)) {
						Toast.makeText(context, "KP2A-IS: Entry action", Toast.LENGTH_SHORT).show();
					}
					dbClosedTime = 0;
					actionSelectedAction(intent);
				} else if (action.equals(Strings.ACTION_LOCK_DATABASE) || action.equals(Strings.ACTION_CLOSE_DATABASE)) {
					dbClosedTime = System.currentTimeMillis();
					sendForceFinishBroadcast(false);
				}
			}
		}
	};
	
	
	//************************************************************************************
	//************************************************************************************
	//SMS:

	private final BroadcastReceiver smsProxyReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean error = true;
			try {
				smsText = intent.getStringExtra(Const.SMS_PROXY_EXTRA_SMS_TEXT);
				smsSender = intent.getStringExtra(Const.SMS_PROXY_EXTRA_SMS_SENDER);
				byte[] hmacCmp = intent.getByteArrayExtra(Const.SMS_PROXY_EXTRA_HMAC);
				smsRemainingTime = Const.SMS_TIMEOUT_MS;
				String key = PreferencesHelper.getSMSProxyKey(prefs);

				//vefify if broadcasts comes from SMS Proxy app (previously activated with generated key)
				Mac sha256_HMAC;
				sha256_HMAC = Mac.getInstance("HmacSHA256");
				sha256_HMAC.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
				sha256_HMAC.update(smsText.getBytes());
				sha256_HMAC.update(smsSender.getBytes());
				byte[] hmac = sha256_HMAC.doFinal();

				if (Arrays.equals(hmac, hmacCmp)) {
					error = false;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (error) {
				Toast.makeText(InputStickService.this, "SMS Proxy Error", Toast.LENGTH_SHORT).show();
			} else {
				//notification:
				mSMSNotificationBuilder = new NotificationCompat.Builder(InputStickService.this, Const.NOTIFICATION_ACTION_CHANNEL_ID);
				mSMSNotificationBuilder.setContentTitle(getString(R.string.app_name));
				mSMSNotificationBuilder.setContentText(getString(R.string.notification_sms) + " (" + smsSender + ")" + " (" + (smsRemainingTime/1000) + "s)");
				mSMSNotificationBuilder.setSmallIcon(R.drawable.ic_sms);

				Intent smsIntent = new Intent(InputStickService.this, InputStickService.class);
				smsIntent.setAction(Const.SERVICE_ENTRY_ACTION);
				smsIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_SMS);
				smsIntent.putExtra(Const.EXTRA_LAYOUT, PreferencesHelper.getPrimaryLayoutCode(prefs));
				mSMSNotificationBuilder.setContentIntent(PendingIntent.getService(InputStickService.this, 0, smsIntent, getPendingIntentFlags()));

				Intent dismissIntent = new Intent(InputStickService.this, InputStickService.class);
				dismissIntent.setAction(Const.SERVICE_DISMISS_SMS);
				mSMSNotificationBuilder.setDeleteIntent(PendingIntent.getService(InputStickService.this, 0, dismissIntent, getPendingIntentFlags()));

				mSMSNotificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
				mNotificationManager.notify(Const.SMS_NOTIFICATION_ID, mSMSNotificationBuilder.build());
			}


		}
	};

	private void startSMSProxy() {
		if ( !smsReceiverRegistered) {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(Const.SMS_PROXY_PACKAGE, Const.SMS_PROXY_SERVICE));
			startService(intent);

			IntentFilter intentFilter = new IntentFilter(Const.SMS_PROXY_ACTION_KP2A_SMS_RELAY);
			intentFilter.setPriority(100);
			registerReceiver(smsProxyReceiver, intentFilter);
			smsReceiverRegistered = true;
		}
	}

	private void keepAliveSMSProxy() {
        if (smsReceiverRegistered) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(Const.SMS_PROXY_PACKAGE,Const.SMS_PROXY_SERVICE));
            intent.setAction(Const.SMS_PROXY_ACTION_KEEP_ALIVE);
            startService(intent);
        }
    }

	private void stopSMSProxy() {
		if (smsReceiverRegistered) {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(Const.SMS_PROXY_PACKAGE,Const.SMS_PROXY_SERVICE));
			stopService(intent);

			try {
				unregisterReceiver(smsProxyReceiver);
				smsReceiverRegistered = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		clearSMS();
	}
	
	private void updateSMSNotification() {
		mSMSNotificationBuilder.setContentText(getString(R.string.notification_sms) + " (" + smsSender + ")" + " (" + (smsRemainingTime/1000) + "s)"); 
		mNotificationManager.notify(Const.SMS_NOTIFICATION_ID, mSMSNotificationBuilder.build());
	}
	
	private void clearSMS() {
		smsText = null;
		smsSender = null;
		smsRemainingTime = 0;
		mNotificationManager.cancel(Const.SMS_NOTIFICATION_ID);
	}
	
	
	//************************************************************************************
	//************************************************************************************
	//Clipboard
	
	ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
	    public void onPrimaryClipChanged() {
	        final ClipData clipData = mClipboardManager.getPrimaryClip();
	        if (clipData != null) {
                final ClipDescription desc = clipData.getDescription();
                boolean hasText;
                if (Build.VERSION.SDK_INT >= 16) {
                    hasText = (desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) || (desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML));
                } else {
                    hasText = desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                }

                if (hasText) {
                    String text = null;
                    ClipData.Item item = clipData.getItemAt(0);
                    if (item != null) {
                        CharSequence cs = item.getText();
                        if (cs != null) {
                            text = cs.toString();
                        }
                    }

                    if (text != null) {
                        if ((text.length() > Const.CLIPBOARD_MAX_LENGTH) && (PreferencesHelper.isClipboardCheckLength(prefs))) {
                            Toast.makeText(InputStickService.this, R.string.text_clipboard_too_long, Toast.LENGTH_LONG).show();
                        } else {
                            queueText(text, mClipboardTypingParams, true);
                            queueDelay(15);
                            if (PreferencesHelper.isClipboardAutoEnter(prefs)) {
                                // do not clear queue - this would remove previous item (typing text form clipboard)!
                                queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER, mClipboardTypingParams, false);
                            }
                            if (PreferencesHelper.isClipboardAutoDisable(prefs)) {
                                stopClipboardMonitoring(true);
                            }
                        }
                    }
                }
            }
	    }
	};
	
	private void startClipboardMonitoring(TypingParams params) {
		clipboardRemainingTime = Const.CLIPBOARD_INITIAL_TIMEOUT_MS; 
		mClipboardTypingParams = params;
		if (mClipboardManager == null) {
			mClipboardManager = (ClipboardManager)getSystemService(android.content.Context.CLIPBOARD_SERVICE);
			mClipboardManager.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
		}

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			Toast.makeText(this, R.string.text_clipboard_copy_now_android10, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, R.string.text_clipboard_copy_now, Toast.LENGTH_LONG).show();
		}
		
		mClipboardNotificationBuilder = new NotificationCompat.Builder(this, Const.NOTIFICATION_ACTION_CHANNEL_ID);
		
		mClipboardNotificationBuilder.setContentTitle(getString(R.string.app_name));		
		mClipboardNotificationBuilder.setContentText(getString(R.string.text_clipboard_notification_info) + " (" + (clipboardRemainingTime/1000) + "s)");
		mClipboardNotificationBuilder.setSmallIcon(R.drawable.ic_notification);

		PendingIntent disableActionPendingIntent;
		Intent disableActionIntent = new Intent(this, InputStickService.class);
		disableActionIntent.setAction(Const.ACTION_CLIPBOARD_STOP);
		disableActionPendingIntent = PendingIntent.getService(this, 0, disableActionIntent, getPendingIntentFlags());

		PendingIntent extendActionPendingIntent;
		Intent extendActionIntent = new Intent(this, InputStickService.class);
		extendActionIntent.setAction(Const.ACTION_CLIPBOARD_EXTEND);
		extendActionPendingIntent = PendingIntent.getService(this, 0, extendActionIntent, getPendingIntentFlags());


		Intent clipboardIntent = new Intent(InputStickService.this, ClipboardActivity.class);
		clipboardIntent.putExtras(mClipboardTypingParams.getBundle());
		mClipboardNotificationBuilder.setContentIntent(PendingIntent.getActivity(InputStickService.this, 0, clipboardIntent, getPendingIntentFlags()));
		
		mClipboardNotificationBuilder.addAction(0, getString(R.string.disable), disableActionPendingIntent);
		mClipboardNotificationBuilder.addAction(0, "+3min", extendActionPendingIntent);
		
		mClipboardNotificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

		mNotificationManager.notify(Const.CLIPBOARD_TYPING_NOTIFICATION_ID, mClipboardNotificationBuilder.build());				
	}	
	
	private void updateClipboardNotification() {
		mClipboardNotificationBuilder.setContentText(getString(R.string.text_clipboard_notification_info) + " (" + (clipboardRemainingTime/1000) + "s)");
		mNotificationManager.notify(Const.CLIPBOARD_TYPING_NOTIFICATION_ID, mClipboardNotificationBuilder.build());
		sendClipboardRemainingTimeBroacdast();
	}
	
	private void stopClipboardMonitoring(boolean showToast) {
		if (showToast) {
			Toast.makeText(this, R.string.text_clipboard_disabled, Toast.LENGTH_SHORT).show();
		}
		if (mClipboardManager != null) {
			mClipboardManager.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
			mClipboardManager = null;
		}		
		clipboardRemainingTime = 0;
		mNotificationManager.cancel(Const.CLIPBOARD_TYPING_NOTIFICATION_ID);
		sendClipboardRemainingTimeBroacdast();
	}
	
	private void extendClipboardTime() {
		if (clipboardRemainingTime > 0) {
			clipboardRemainingTime += Const.CLIPBOARD_TIMEOUT_EXTEND_MS;
			if (clipboardRemainingTime > Const.CLIPBOARD_MAX_TIMEOUT_MS) {
				clipboardRemainingTime = Const.CLIPBOARD_MAX_TIMEOUT_MS;
			}
			updateClipboardNotification();
		}		
	}

	//send broadcast to CliboardActivity
	private void sendClipboardRemainingTimeBroacdast() {
		Intent intent = new Intent();
		intent.setAction(Const.BROADCAST_CLIPBOARD_REMAINING_TIME);
		intent.putExtra(Const.EXTRA_CLIPBOARD_REMAINING_TIME, clipboardRemainingTime);
		sendBroadcast(intent);
	}
	
	
	//************************************************************************************
	//************************************************************************************
	//Preferences		

	private final OnSharedPreferenceChangeListener mSharedPrefsListener = new OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			loadPreference(key);
		}
	};
	
	// if key == null load all preferences; if not, only single preference was changed
	private void loadPreference(String key) {
		if (key == null || Const.PREF_ENTER_AFTER_URL.equals(key)) {
			addEnterAfterURL = PreferencesHelper.addEnterAfterURL(prefs);
		}
		if (key == null || Const.PREF_TYPING_SPEED.equals(key)) {
			defaultTypingSpeed = PreferencesHelper.getTypingSpeed(prefs);
		}
		if (key == null || Const.PREF_AUTO_CONNECT.equals(key)) {
			autoConnect = PreferencesHelper.getAutoConnect(prefs);
		}
		if (key == null || Const.PREF_MAX_IDLE_PERIOD.equals(key)) {
			maxIdlePeriod = PreferencesHelper.getMaxIdlePeriod(prefs);
		}
				
		if (key == null || Const.PREF_SMS_SMSPROXY_KEY.equals(key)) {
			smsEnabled = PreferencesHelper.isSMSProxyEnabled(prefs);
			//if user changed preferences
			if (key != null) { 
				if (smsEnabled) {
                    startSMSProxy();
				} else {
                    stopSMSProxy();
				}
			}
		}

		if (key == null || Const.PREF_TWEAKS_NEVER_STOP_PLUGIN.equals(key)) {
			neverStopPlugin = PreferencesHelper.isNeverStopPlugin(prefs);
		}
	}

	
	
	//************************************************************************************
	//************************************************************************************
	//Plugin actions:

	private void onEntryOpened() {
		if (autoConnect == Const.AUTO_CONNECT_ALWAYS) {
			connectAction();
		} else if (autoConnect == Const.AUTO_CONNECT_SMART) {
			if (PreferencesHelper.canSmartAutoConnect(prefs)) {
				connectAction();
			}
		}
		keepAliveSMSProxy();
	}

	private void actionSelectedAction(Intent intent) {
		String fieldId = intent.getStringExtra(Strings.EXTRA_FIELD_ID);
		Bundle actionDataBundle = intent.getBundleExtra(Strings.EXTRA_ACTION_DATA);
		if (actionDataBundle == null) {
			return;
		}		
		String layoutCode = actionDataBundle.getString(Const.EXTRA_LAYOUT, Const.PREF_LAYOUT_VALUE);
		TypingParams params = new TypingParams(layoutCode, defaultTypingSpeed);
		EntryData entryData = new EntryData(intent);

		if (fieldId == null) {
			// ENTRY ACTION
			String uiAction = actionDataBundle.getString(Const.EXTRA_ACTION);
			entryAction(uiAction, entryData, params);
		} else {
			// FIELD ACTION
			boolean typeSlow = actionDataBundle.getBoolean(Const.EXTRA_TYPE_SLOW, false);
			boolean typeMasked = actionDataBundle.getBoolean(Const.EXTRA_TYPE_MASKED, false);
			String fieldKey = fieldId.substring(Strings.PREFIX_STRING.length());
			byte keyAfterTyping = actionDataBundle.getByte(Const.EXTRA_ADD_KEY, (byte)0);
			
			HashMap<String, String> res = new HashMap<>();
			try {
				JSONObject json = new JSONObject(intent.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA));
				for (Iterator<String> iter = json.keys(); iter.hasNext();) {
					String key = iter.next();
					String value = json.get(key).toString();
					// Log.d("KP2APluginSDK", "received " + key+"/"+value);
					res.put(key, value);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			String text = res.get(fieldKey);
			if (typeSlow) {
				params = new TypingParams(layoutCode, Const.TYPING_SPEED_SLOW);
			}
			if (typeMasked) {
				if (hasPermissionToExecuteAction(Const.ACTION_MASKED_PASSWORD)) {
					connectAction();
					ActionHelper.startMaskedPasswordActivity(this, text, params, true);
				} else {
					showMissingPermissionNotification();
				}
			} else {						
				queueText(text, params, true);				
				if (keyAfterTyping != 0) {
					queueDelay(5);
					queueKey(HIDKeycodes.NONE, keyAfterTyping, params, false);
				} else {
					if ((KeepassDefs.UrlField.equals(fieldKey) && addEnterAfterURL)) {
						queueDelay(5);
						queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER, params, false);
					}
				}
			}
		}
	}

	private void entryAction(String uiAction, EntryData entryData, TypingParams params) {
		Log.d(_TAG, "entryAction: " + uiAction);

		if (hasPermissionToExecuteAction(uiAction)) {
			if (Const.ACTION_MASKED_PASSWORD.equals(uiAction)) {
				connectAction();
				ActionHelper.startMaskedPasswordActivity(this, entryData.getPassword(), params, true);
			} else if (Const.ACTION_SETTINGS.equals(uiAction)) {
				ActionHelper.startSettingsActivityAction(this);
			} else if (Const.ACTION_SHOW_ALL.equals(uiAction)) {
				ActionHelper.startShowAllActivityAction(this, entryData);
			} else if (Const.ACTION_USER_PASS.equals(uiAction)) {
				typeUserNameAndPasswordFields(entryData, params, false);
			} else if (Const.ACTION_USER_PASS_ENTER.equals(uiAction)) {
				typeUserNameAndPasswordFields(entryData, params, true);
			} else if (Const.ACTION_MAC_SETUP.equals(uiAction)) {
				connectAction();
				ActionHelper.startMacSetupActivityAction(this);
			} else if (Const.ACTION_MACRO_ADDEDIT.equals(uiAction)) {
				ActionHelper.addEditMacroAction(this, entryData, false);
			} else if (Const.ACTION_CLIPBOARD.equals(uiAction)) {
				connectAction();
				startClipboardMonitoring(params);
				ActionHelper.startClipboardApp(this);
			} else if (Const.ACTION_MACRO_RUN.equals(uiAction)) {
				connectAction();
				if (ActionHelper.runMacroAction(this, entryData, params)) {
					onAction(ACTION_HID); // macro was  executed
				}
			} else if (Const.ACTION_TAB.equals(uiAction)) {
				queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_TAB, params, true);
			} else if (Const.ACTION_ENTER.equals(uiAction)) {
				queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER, params, true);
			} else if (Const.ACTION_CONNECT.equals(uiAction)) {
				connectAction();
			} else if (Const.ACTION_DISCONNECT.equals(uiAction)) {
				InputStickHID.disconnect();
			} else if (Const.ACTION_TEMPLATE_RUN.equals(uiAction)) {
				connectAction();
				ActionHelper.startSelectTemplateActivityAction(this, entryData, params, false);
			} else if (Const.ACTION_TEMPLATE_MANAGE.equals(uiAction)) {
				ActionHelper.startSelectTemplateActivityAction(this, entryData, params, true);
			} else if (Const.ACTION_QUICK_SHORTCUT_1.equals(uiAction)) {
				executeQuickAction(1, params);
			} else if (Const.ACTION_QUICK_SHORTCUT_2.equals(uiAction)) {
				executeQuickAction(2, params);
			} else if (Const.ACTION_QUICK_SHORTCUT_3.equals(uiAction)) {
				executeQuickAction(3, params);
			} else if (Const.ACTION_REMOTE.equals(uiAction)) {
				ActionHelper.startRemoteActivityAction(this);
			} else if (Const.ACTION_SMS.equals(uiAction)) {
				ActionHelper.startSMSActivityAction(this, smsText, smsSender, params);
			}
		} else {
			showMissingPermissionNotification();
		}
	}
	
	private void executeQuickAction(int id, TypingParams params) {
		String param = PreferencesHelper.getQuickShortcut(prefs, id);
		byte modifiers = MacroHelper.getModifiers(param);
		byte key = MacroHelper.getKey(param);
		queueKey(modifiers, key, params, false);
	}

	private void typeUserNameAndPasswordFields(EntryData entryData, TypingParams params, boolean addEnter) {
		queueText(entryData.getUserName(), params, true);
		queueDelay(15);
		queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_TAB, params, false);
		queueDelay(15);
		queueText(entryData.getPassword(), params, false);
		if (addEnter) {
			queueDelay(15);
			queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER, params, false);
		}
	}

	private void connectAction() {
		int state = InputStickHID.getState();
		if (state == ConnectionManager.STATE_DISCONNECTED || state == ConnectionManager.STATE_FAILURE) {		
			InputStickHID.connect(getApplication());
		}
	}


	

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(_TAG, "onCreate");

		isRunning = true;
		cnt = 0;
		
		dbClosedTime = 0;
		lastActionTime = 0;
		serviceKeepAliveTime = 0;
		autoDisconnectTime = 0;

		InputStickHID.addStateListener(this);

		IntentFilter filter;
		filter = new IntentFilter();
		filter.addAction(Strings.ACTION_ENTRY_ACTION_SELECTED);
		filter.addAction(Strings.ACTION_OPEN_ENTRY);
		//filter.addAction(Strings.ACTION_CLOSE_ENTRY_VIEW); interferes with dbClosedTime; is called after ACTION_LOCK_DATABASE
		filter.addAction(Strings.ACTION_CLOSE_DATABASE);
		filter.addAction(Strings.ACTION_LOCK_DATABASE);
		registerReceiver(kp2aReceiver, filter);		

		mHandler.removeCallbacksAndMessages(null);
		mHandler.postDelayed(mTimerTask, TIMER_INTERVAL_MS);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(mSharedPrefsListener);

		//notification:
		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		//notification channel
		if (Build.VERSION.SDK_INT >= 26) {
			NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(Const.NOTIFICATION_STATUS_CHANNEL_ID, Const.NOTIFICATION_STATUS_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
			mNotificationManager.createNotificationChannel(notificationChannel);
            notificationChannel = new NotificationChannel(Const.NOTIFICATION_ACTION_CHANNEL_ID, Const.NOTIFICATION_ACTION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(notificationChannel);
			notificationChannel = new NotificationChannel(Const.NOTIFICATION_IMPORTANT_CHANNEL_ID, Const.NOTIFICATION_IMPORTANT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
			mNotificationManager.createNotificationChannel(notificationChannel);
		}
				
		mPluginNotificationBuilder = new NotificationCompat.Builder(this, Const.NOTIFICATION_STATUS_CHANNEL_ID);
		mPluginNotificationBuilder.setContentTitle(getString(R.string.app_name));
		mPluginNotificationBuilder.setContentText(getString(R.string.notification_text)); 
		mPluginNotificationBuilder.setSmallIcon(R.drawable.ic_notification);
		mPluginNotificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);	

		Intent openServiceIntent = new Intent(this, SettingsActivity.class);
		mPluginNotificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, openServiceIntent, getPendingIntentFlags()));

		Intent forceStopIntent = new Intent(this, InputStickService.class);
		forceStopIntent.setAction(Const.SERVICE_FORCE_STOP);
		mPluginNotificationBuilder.addAction(0, getString(R.string.text_stop_plugin), PendingIntent.getService(this, 0, forceStopIntent, getPendingIntentFlags()));
					
		startForeground(Const.INPUTSTICK_SERVICE_NOTIFICATION_ID, mPluginNotificationBuilder.build());		
		
		loadPreference(null);
		smsEnabled = PreferencesHelper.isSMSProxyEnabled(prefs);
		if (smsEnabled) {
            startSMSProxy();
		}
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onAction(ACTION_OTHER);
		Log.d(_TAG, "onStartCommand");
		if (intent != null) {
			final String action = intent.getAction();
			Log.d(_TAG, "received action " + action);

			if (Const.SERVICE_QUEUE_ITEM.equals(action)) {
				ItemToExecute item = new ItemToExecute(intent.getExtras());
				queueItem(item);
			} else if (Const.SERVICE_START.equals(action)) {
				if (cnt == 0) {
					onEntryOpened(); // only if just created, otherwise it will be handled by already registered broadcast receiver
				}
			} else if (Const.SERVICE_START_BACKGROUND.equals(action)) {
				//nothing to do here
			} else if (Const.SERVICE_ENTRY_ACTION.equals(action)) {
				String uiAction = intent.getStringExtra(Const.EXTRA_ACTION);
				String layoutCode = intent.getStringExtra(Const.EXTRA_LAYOUT);
				TypingParams params = new TypingParams(layoutCode, defaultTypingSpeed);
				EntryData entryData = new EntryData(intent);
				entryAction(uiAction, entryData, params);
				// Toast.makeText(this, R.string.text_plugin_restarted, Toast.LENGTH_LONG).show();
			} else if (Const.SERVICE_RESTART.equals(action)) {
				if (cnt == 0) {
					Toast.makeText(this, R.string.text_plugin_restarted, Toast.LENGTH_LONG).show();
				}
			} else if (Const.SERVICE_FORCE_STOP.equals(action)) {
				stopPlugin();
			} else if (Const.SERVICE_DISMISS_SMS.equals(action)) {				 				
				clearSMS();
			} else if (Const.ACTION_CLIPBOARD_STOP.equals(action)) {				 				
				stopClipboardMonitoring(false);
			} else if (Const.ACTION_CLIPBOARD_EXTEND.equals(action)) {				 				
				extendClipboardTime();
			}			
			
			cnt++;
		}

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d(_TAG, "onDestroy");
		isRunning = false;
		unregisterReceiver(kp2aReceiver);
        stopSMSProxy();
		stopClipboardMonitoring(false);
		prefs.unregisterOnSharedPreferenceChangeListener(mSharedPrefsListener);
		mHandler.removeCallbacksAndMessages(null);
		items.clear();
		InputStickHID.removeStateListener(this);
		InputStickHID.disconnect();
		super.onDestroy();
	}
	
	
	private void stopPlugin() {
		sendForceFinishBroadcast(true);
		stopForeground(true);
		stopSelf();
	}

	private void updatePluginNotification() {
		int resId;
		int state = InputStickHID.getState();
		switch (state) {
		case ConnectionManager.STATE_READY:
			resId = R.string.notification_state_ready;
			break;
		case ConnectionManager.STATE_CONNECTED:
			resId = R.string.notification_state_connected;
			break;
		case ConnectionManager.STATE_CONNECTING:
			resId = R.string.notification_state_connecting;
			break;
		default:
			resId = R.string.notification_state_not_connected;
			break;
		}
		String contentText =  getString(R.string.notification_text) + " (" + getString(resId) + ")";
		
		mPluginNotificationBuilder.setContentText(contentText);
		mNotificationManager.notify(Const.INPUTSTICK_SERVICE_NOTIFICATION_ID, mPluginNotificationBuilder.build());
	}
	
	
	//************************************************************************************
	//************************************************************************************
	//InputStick connection:

	@Override
	public void onStateChanged(int state) {
		Log.d(_TAG, "InputStick connection state changed: " + state);
        connectionAttemptLockTime = 0;
		int messageResId = 0;
		switch (state) {
            case ConnectionManager.STATE_CONNECTED:
                onAction(ACTION_HID);
                break;
            case ConnectionManager.STATE_READY:
                addDummyKeys = true;
                executeQueue();
                // re-enable smart auto-connect?
                if (autoConnect == Const.AUTO_CONNECT_SMART) {
                    if (!PreferencesHelper.canSmartAutoConnect(prefs)) {
                        PreferencesHelper.setSmartAutoConnect(prefs, true);
                        messageResId = R.string.text_auto_connect_reenabled;
                    }
                }

                break;
            case ConnectionManager.STATE_DISCONNECTED:
                if (autoConnect == Const.AUTO_CONNECT_SMART && PreferencesHelper.canSmartAutoConnect(prefs)) {
                    int reasonCode = InputStickHID.getDisconnectReason();
                    // disable smart auto-connect? yes, if user was asked to select device, but dismissed/cancelled dialog or cancelled connection attempt
                    if (reasonCode == ConnectionManager.DISC_REASON_UTILITY_CANCELLED) {
                        PreferencesHelper.setSmartAutoConnect(prefs, false);
                        messageResId = R.string.text_auto_connect_msg_cancelled;
                    }
                }
                break;
            case ConnectionManager.STATE_FAILURE:
                int errorCode = InputStickHID.getErrorCode();
                Log.d(_TAG, "InputStick connection error: " + errorCode);
                if (errorCode == InputStickError.ERROR_ANDROID_NO_UTILITY_APP) {
                    messageResId = R.string.text_missing_utility_app;
                } else {
                    messageResId = R.string.text_connection_failed;

                    // disable smart auto-connect? yes, if connection failed or user did not allow to turn on BT
                    if (autoConnect == Const.AUTO_CONNECT_SMART && PreferencesHelper.canSmartAutoConnect(prefs)) {
                        if (errorCode == InputStickError.ERROR_BLUETOOTH_CONNECTION_FAILED) {
                            PreferencesHelper.setSmartAutoConnect(prefs, false);
                            messageResId = R.string.text_auto_connect_msg_failed;
                        }
                        if (errorCode == InputStickError.ERROR_BLUETOOTH_NOT_ENABLED) {
                            PreferencesHelper.setSmartAutoConnect(prefs, false);
                            messageResId = R.string.text_auto_connect_msg_cancelled;
                        }
                    }
                }
                items.clear();
                break;
            default:
                break;
		}
		
		if (messageResId != 0) {
			Toast.makeText(this, messageResId, Toast.LENGTH_LONG).show();
		}
		
		updatePluginNotification();	
	}
	
	
	//************************************************************************************
	//************************************************************************************
	//HID actions:

	private void queueText(String text, TypingParams params, boolean canClearQueue) {
		ItemToExecute item = new ItemToExecute(text, params);
		item.setCanClearQueue(canClearQueue);
		queueItem(item);
		if (InputStickKeyboard.isCapsLock()) {
			long now = System.currentTimeMillis();
			if (now > lastCapsLockWarningTime + Const.SERVICE_CAPSLOCK_WARNING_TIMEOUT) {
				lastCapsLockWarningTime = now;
				Toast.makeText(InputStickService.this, R.string.text_capslock_warning, Toast.LENGTH_LONG).show();
			}
		}
	}

	private void queueKey(byte modifiers, byte key, TypingParams params, boolean canClearQueue) {		
		ItemToExecute item = new ItemToExecute(modifiers, key, params);
		item.setCanClearQueue(canClearQueue);
		queueItem(item);
	}

	private void queueDelay(int value) {
		ItemToExecute item = new ItemToExecute(value);
		item.setCanClearQueue(false);
		queueItem(item);		
	}

	private void queueItem(ItemToExecute item) {
		int state = InputStickHID.getState();
		
		//if not ready, queue only last action - clear all previous actions
		if (state != ConnectionManager.STATE_READY) {
			synchronized (items) {
				//does not allow to queue multiple actions when not ready to type - that could lead to executing an action multiple times (example: type password twice etc.) 
				if (item.canClearQueue()) {
					items.clear();
				}
				items.add(item);
			}

			if (state == ConnectionManager.STATE_DISCONNECTED || state == ConnectionManager.STATE_FAILURE) {
                long time = System.currentTimeMillis();
                if ((connectionAttemptLockTime == 0) || (time > connectionAttemptLockTime + CONNECTION_LOCK_DURATION_MS)) {
                    Log.d(_TAG, "trigger connect");
                    connectionAttemptLockTime = time;
                    InputStickHID.connect(getApplication());
                }
			}
		} else {
			//connected & ready
			if (item.execute(this)) {
				onAction(ACTION_HID);
			}
		}
	}
	

	private void executeQueue() {
		Log.d(_TAG, "executeQueue");		
		if (addDummyKeys) {
			new ItemToExecute(15).execute(this); // 15 dummy keys delay; in some cases it will prevent missing characters when typing
			addDummyKeys = false;
		}
		boolean executedHIDAction = false;
		synchronized (items) {
			for (ItemToExecute item : items) {
				if (item.execute(this)) {
					executedHIDAction = true;
				}
			}
			items.clear();
		}
		if (executedHIDAction) {
			onAction(ACTION_HID);	
		}
	}


	//************************************************************************************
	//************************************************************************************
	//SYSTEM_ALERT_WINDOW permission (required by Android 10 and later to start activity when app has no visible activity):

	private boolean hasPermissionToExecuteAction(String uiAction) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			//check if uiAction may need to start activity
			if (Const.ACTION_MASKED_PASSWORD.equals(uiAction)
					|| Const.ACTION_SETTINGS.equals(uiAction)
					|| Const.ACTION_SHOW_ALL.equals(uiAction)
					|| Const.ACTION_MAC_SETUP.equals(uiAction)
					|| Const.ACTION_MACRO_ADDEDIT.equals(uiAction)
					|| Const.ACTION_MACRO_RUN.equals(uiAction)
					|| Const.ACTION_TEMPLATE_RUN.equals(uiAction)
					|| Const.ACTION_TEMPLATE_MANAGE.equals(uiAction)
					|| Const.ACTION_CLIPBOARD.equals(uiAction)
					|| Const.ACTION_REMOTE.equals(uiAction)) {
				return Settings.canDrawOverlays(this);
			}
		}
		return true;
	}

	private void showMissingPermissionNotification() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(InputStickService.this, Const.NOTIFICATION_ACTION_CHANNEL_ID);
            builder.setContentTitle(getString(R.string.app_name));
            builder.setContentText(getString(R.string.notification_permission));
            builder.setSmallIcon(R.drawable.ic_permission); 
            builder.setAutoCancel(true);
            builder.setTimeoutAfter(3 * 60 * 1000);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);

            Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
			builder.setContentIntent(PendingIntent.getActivity(this, 0, permissionIntent, getPendingIntentFlags()));

            mNotificationManager.notify(Const.PERMISSION_NOTIFICATION_ID, builder.build());

            Toast.makeText(this, R.string.toast_permission, Toast.LENGTH_LONG).show();
        }
	}

	private static int getPendingIntentFlags() {
		if (Build.VERSION.SDK_INT >= 23) {
			return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
		} else {
			return PendingIntent.FLAG_UPDATE_CURRENT;
		}
	}

}
