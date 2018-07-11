package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import keepass2android.pluginsdk.KeepassDefs;
import keepass2android.pluginsdk.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.AutoScrollHelper;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickError;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.basic.InputStickKeyboard;
import com.inputstick.api.hid.HIDKeycodes;

public class InputStickService extends Service implements InputStickStateListener {

	private static final String _TAG = "KP2AINPUTSTICK SERVICE"
			;
	private static final long CAPSLOCK_WARNING_TIMEOUT = 10000;
	
	private static final int FAILSAFE_PERIOD = 60000;	//TODO 10min?
	
	private static final int ACTION_HID = 0;
	private static final int ACTION_KP2A = 1;
	private static final int ACTION_SERVICE = 2;

	private static SharedPreferences prefs;
	private static boolean addEnterAfterURL;
	private static int defaultTypingSpeed;
	private static int autoConnect;
	private static int maxIdlePeriod;
	
	private static boolean smsEnabled;
	private static String smsText;
	private static String smsSender;

	public static boolean isRunning;
	private static long dbClosedTime;		
	private static long lastActionTime;
	private static long serviceKeepAliveTime;			//if kp2a database was closed/locked but plugin is being kept alive (received SMS, typing from clipboard)
	private static long autoDisconnectTime;	//if user enabled auto-disconnect in settings

	private static ArrayList<ItemToExecute> items = new ArrayList<ItemToExecute>();
	
	private static boolean addDummyKeys;
	private static int cnt;

	private static long lastCapsLockWarningTime;
	

	private static NotificationManager mNotificationManager;
	private static NotificationCompat.Builder mBuilder;

	private static Handler mHandler = new Handler();
	private Runnable mTimerTask = new Runnable() {

		public void run() {
			final long time = System.currentTimeMillis();
			
			//auto disconnect?
			if (InputStickHID.isConnected()) {																	
				if ((maxIdlePeriod > 0) && (time > autoDisconnectTime)) {
					Log.d(_TAG, "disconnect (inactivity)");
					Toast.makeText(InputStickService.this, "auto-DISC", Toast.LENGTH_SHORT).show(); //TODO remove
					InputStickHID.disconnect();
				}				
			}
			
			//stop plugin?
			if (time > serviceKeepAliveTime) {
				if (dbClosedTime > 0) {
					stopPlugin("auto");
				} else if (time > lastActionTime + FAILSAFE_PERIOD) {
					//fail safe, in case kp2a crashes
					stopPlugin("failsafe");
				}
			}					
									
			mHandler.postDelayed(mTimerTask, 1000);
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
	
	public static void onRemoteAction() {
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
	

	private final BroadcastReceiver kp2aReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.d(_TAG, "received action " + action);
			if (action != null) {
				dbClosedTime = 0;
				if (action.equals(Strings.ACTION_OPEN_ENTRY)) {
					onEntryOpened();
				} else if (action.equals(Strings.ACTION_CLOSE_ENTRY_VIEW)) {
					//
				} else if (action.equals(Strings.ACTION_ENTRY_ACTION_SELECTED)) {
					actionSelectedAction(intent);
				} else if (action.equals(Strings.ACTION_LOCK_DATABASE) || action.equals(Strings.ACTION_CLOSE_DATABASE)) {
					dbClosedTime = System.currentTimeMillis();
				}
			}
		}
	};
	
	
	private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
	        Object[] pdus = (Object[]) bundle.get("pdus");
	        for(int i=0; i<pdus.length; i++){
	            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
	            smsText = smsMessage.getMessageBody();
	            smsSender = smsMessage.getDisplayOriginatingAddress();
	            showSMSNotification(true);
	        }
		}
	};
	

	private final OnSharedPreferenceChangeListener mSharedPrefsListener = new OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			loadPreference(key);
		}
	};
	

	private void onEntryOpened() {
		if (autoConnect == Const.AUTO_CONNECT_ALWAYS) {
			connectAction();
		} else if (autoConnect == Const.AUTO_CONNECT_SMART) {
			if (PreferencesHelper.canSmartAutoConnect(prefs)) {
				connectAction();
			}
		}
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
			
			HashMap<String, String> res = new HashMap<String, String>();
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
				connectAction();
				ActionHelper.startMaskedPasswordActivity(this, text, params, true);
			} else {						
				queueText(text, params, true);				
				if (keyAfterTyping != 0) {
					queueDelay(5, false);
					queueKey(HIDKeycodes.NONE, keyAfterTyping, params, false);
				} else {
					if ((KeepassDefs.UrlField.equals(fieldKey) && addEnterAfterURL)) {
						queueDelay(5, false);
						queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER, params, false);
					}
				}
			}
		}
	}

	private void entryAction(String uiAction, EntryData entryData, TypingParams params) {
		Log.d(_TAG, "entryAction: " + uiAction);

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
			ActionHelper.startClipboardTypingService(this, params);
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
	}
	
	private void executeQuickAction(int id, TypingParams params) {
		String param = PreferencesHelper.getQuickShortcut(prefs, id);
		byte modifiers = MacroHelper.getModifiers(param);
		byte key = MacroHelper.getKey(param);
		queueKey(modifiers, key, params, false);
	}

	private void typeUserNameAndPasswordFields(EntryData entryData, TypingParams params, boolean addEnter) {
		queueText(entryData.getUserName(), params, true);
		queueDelay(15, false);
		queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_TAB, params, false);
		queueDelay(15, false);
		queueText(entryData.getPassword(), params, false);
		if (addEnter) {
			queueDelay(15, false);
			queueKey(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER, params, false);
		}
	}

	private void connectAction() {
		int state = InputStickHID.getState();
		if (state == ConnectionManager.STATE_DISCONNECTED || state == ConnectionManager.STATE_FAILURE) {		
			InputStickHID.connect(getApplication());
		}
	}

	private void stopPlugin(String s) {
		Toast.makeText(InputStickService.this, "STOP: " + s, Toast.LENGTH_LONG).show(); //TODO remove
		stopForeground(true);
		stopSelf();
	}

	public static boolean isRunning() {
		return isRunning;
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
		filter.addAction(Strings.ACTION_CLOSE_ENTRY_VIEW);
		filter.addAction(Strings.ACTION_CLOSE_DATABASE);
		filter.addAction(Strings.ACTION_LOCK_DATABASE);
		registerReceiver(kp2aReceiver, filter);		

		mHandler.removeCallbacksAndMessages(null);
		mHandler.postDelayed(mTimerTask, 1000);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(mSharedPrefsListener);

		//notification:
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle(getString(R.string.app_name));		
		mBuilder.setContentText(getString(R.string.notification_text)); 
		mBuilder.setSmallIcon(R.drawable.ic_notification);
		mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);	

		Intent openServiceIntent = new Intent(this, SettingsActivity.class);
		mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, openServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		Intent forceStopIntent = new Intent(this, InputStickService.class);
		forceStopIntent.setAction(Const.SERVICE_FORCE_STOP);
		mBuilder.addAction(0, getString(R.string.text_stop_plugin), PendingIntent.getService(this, 0, forceStopIntent, PendingIntent.FLAG_CANCEL_CURRENT));
					
		startForeground(Const.INPUTSTICK_SERVICE_NOTIFICATION_ID, mBuilder.build());		
		
		loadPreference(null);
		if (smsEnabled) {
			setupSMS();
		}
	}
	
	private void setupSMS() {
		if (ContextCompat.checkSelfPermission(InputStickService.this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
			IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
			intentFilter.setPriority(100);
			registerReceiver(smsReceiver, intentFilter);
		}
	}

	private void updateNotification() {
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
		
		mBuilder.setContentText(contentText);
		mNotificationManager.notify(Const.INPUTSTICK_SERVICE_NOTIFICATION_ID, mBuilder.build());
	}
	
	private void showSMSNotification(boolean show) {
		if (show) {
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
			mBuilder.setContentTitle(getString(R.string.app_name));
			mBuilder.setContentText(getString(R.string.notification_sms) + " (" + smsSender + ")"); 
			mBuilder.setSmallIcon(R.drawable.ic_sms);
	
			Intent smsIntent = new Intent(this, InputStickService.class);
			smsIntent.setAction(Const.SERVICE_ENTRY_ACTION);
			smsIntent.putExtra(Const.EXTRA_ACTION, Const.ACTION_SMS);
			smsIntent.putExtra(Const.EXTRA_LAYOUT, PreferencesHelper.getPrimaryLayoutCode(prefs));
			mBuilder.setContentIntent(PendingIntent.getService(this, 0, smsIntent, PendingIntent.FLAG_CANCEL_CURRENT));
			
		    Intent dismissIntent = new Intent(this, InputStickService.class);
		    dismissIntent.setAction(Const.SERVICE_DISMISS_SMS); 
		    mBuilder.setDeleteIntent(PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT));					
	
			mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
			mNotificationManager.notify(Const.SMS_NOTIFICATION_ID, mBuilder.build());
		} else {			
			smsText = null;
			smsSender = null;
			mNotificationManager.cancel(Const.SMS_NOTIFICATION_ID);
		}
	}

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
		
		if (key == null || Const.PREF_SMS.equals(key)) {
			smsEnabled = PreferencesHelper.isSMSEnabled(prefs);
			if (key != null) {
				if (smsEnabled) {
					setupSMS();
				} else {
					unregisterReceiver(smsReceiver);
					showSMSNotification(false);
				}
			}
		}
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onAction(ACTION_SERVICE);
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
				stopPlugin("manual");
			} else if (Const.SERVICE_DISMISS_SMS.equals(action)) {				 				
				showSMSNotification(false);				
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
		if (smsEnabled) {
			unregisterReceiver(smsReceiver);
			showSMSNotification(false);
		}
		prefs.unregisterOnSharedPreferenceChangeListener(mSharedPrefsListener);
		mHandler.removeCallbacksAndMessages(null);
		items.clear();
		InputStickHID.removeStateListener(this);
		InputStickHID.disconnect();
		super.onDestroy();
	}

	@Override
	public void onStateChanged(int state) {
		Log.d(_TAG, "InputStick connection state changed: " + state);
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
		
		updateNotification();	
	}

	private void queueText(String text, TypingParams params, boolean canClearQueue) {
		ItemToExecute item = new ItemToExecute(text, params);
		item.setCanClearQueue(canClearQueue);
		queueItem(item);
		if (InputStickKeyboard.isCapsLock()) {
			long now = System.currentTimeMillis();
			if (now > lastCapsLockWarningTime + CAPSLOCK_WARNING_TIMEOUT) {
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

	private void queueDelay(int value, boolean canClearQueue) {
		ItemToExecute item = new ItemToExecute(value);
		item.setCanClearQueue(canClearQueue);
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
				Log.d(_TAG, "trigger connect");
				InputStickHID.connect(getApplication());
			}
		} else {
			//connected & ready
			item.execute(this);
			onAction(ACTION_HID);
		}
	}
	

	private void executeQueue() {
		Log.d(_TAG, "executeQueue");
		if (addDummyKeys) {
			new ItemToExecute(15).execute(this); // 15 dummy keys delay; in some cases it will prevent missing characters when typing
			addDummyKeys = false;
		}
		synchronized (items) {
			for (ItemToExecute item : items) {
				item.execute(this);
			}
			items.clear();
		}
		onAction(ACTION_HID);	}

}
