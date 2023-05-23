package com.inputstick.apps.kp2aplugin.remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.utils.remote.KeyboardSupport;
import com.inputstick.api.utils.remote.ModifiersSupport;
import com.inputstick.api.utils.remote.MousePadSupport;
import com.inputstick.api.utils.remote.MousePadView;
import com.inputstick.api.utils.remote.MouseScrollView;
import com.inputstick.api.utils.remote.RemoteSupport;
import com.inputstick.apps.kp2aplugin.Const;
import com.inputstick.apps.kp2aplugin.EntryData;
import com.inputstick.apps.kp2aplugin.InputStickService;
import com.inputstick.apps.kp2aplugin.MacroHelper;
import com.inputstick.apps.kp2aplugin.PreferencesHelper;
import com.inputstick.apps.kp2aplugin.R;

public class RemoteActivity extends Activity implements InputStickStateListener {	

	private RelativeLayout relativeLayoutMouse;
	private MousePadView viewMousePad;
	private View buttonMouseL;
	private View buttonMouseM;
	private View buttonMouseR;	
	private MouseScrollView viewMouseScroll;
	private ImageView imageViewMouseConfigure;	
	
	private LinearLayout linearLayoutModifiers;
	private ToggleButton toggleButtonCtrl;
	private ToggleButton toggleButtonShift;
	private ToggleButton toggleButtonAlt;
	private ToggleButton toggleButtonGui;
	private ToggleButton toggleButtonAltGr;
	private Button buttonContext;	
	
	private Button buttonShowKeyboard;
	private Button buttonConnection;
	private Button buttonFunctionKeys;
	private Button buttonMore;
	
	private boolean autoResetModifiers;
	
	private RemoteSupport mRemote;
	private KeyboardSupport mKeyboard;
	private KP2ARemotePreferences mRemotePreferences;
	private MousePadSupport mMouse;
	private ModifiersSupport mModifiers;
	
	//keep service alive (in case KP2A db is closed/locked)
	//notify service than an action was performed to avoid disconnecting (reaching max idle time)
	private final Handler mHandler = new Handler();
	private final Runnable tick = new Runnable() {
	    public void run() {
	    	if (InputStickHID.isReady() && mRemote != null) {
	    		long lastActionTime = mRemote.getLastActionTime();
	    		long time = System.currentTimeMillis();
	    		if (time - lastActionTime < 2000) {	    				    			
	    			InputStickService.onHIDAction();
	    		}
	    	}
	    	InputStickService.extendServiceKeepAliveTime(1000);
	    	mHandler.postDelayed(this, 1000); 
	    }
	};
	
	private final BroadcastReceiver finishReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(RemoteActivity.this, R.string.text_activity_closed, Toast.LENGTH_SHORT).show(); 
			finish();
		}
	};
	
	private void sendToService(String action) {
		Intent serviceIntent = new Intent(RemoteActivity.this, InputStickService.class);
		serviceIntent.setAction(Const.SERVICE_ENTRY_ACTION); 
		serviceIntent.putExtra(Const.EXTRA_ACTION, action);
		serviceIntent.putExtras(EntryData.getDummyBundle());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(serviceIntent);
		} else {
			startService(serviceIntent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_remote);
		
		relativeLayoutMouse = (RelativeLayout)findViewById(R.id.relativeLayoutMouse);
		
		buttonShowKeyboard = (Button)findViewById(R.id.buttonShowKeyboard);
		buttonShowKeyboard.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);		
			}        	
        });	
		
		buttonConnection = (Button)findViewById(R.id.buttonConnection);
		buttonConnection.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {		
				switch (InputStickHID.getState()) {
					case ConnectionManager.STATE_DISCONNECTED:
					case ConnectionManager.STATE_FAILURE:
						sendToService(Const.ACTION_CONNECT);
						break;
					case ConnectionManager.STATE_READY:
					case ConnectionManager.STATE_CONNECTED:
					case ConnectionManager.STATE_CONNECTING:
						sendToService(Const.ACTION_DISCONNECT);
						break;												
				}
			}        	
        });
		
		buttonFunctionKeys = findViewById(R.id.buttonFunctionKeys);
		buttonFunctionKeys.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				KeyboardSupport.getFunctionKeysDialog(RemoteActivity.this, mRemote, mModifiers, "Fn Keys").show();
			}        	
        });	
		
		buttonMore = findViewById(R.id.buttonMore);
		buttonMore.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				getMoreDialog().show();
			}        	
        });	
		
		relativeLayoutMouse = findViewById(R.id.relativeLayoutMouse);
		buttonMouseL = findViewById(R.id.buttonMouseL);
		buttonMouseM = findViewById(R.id.buttonMouseM);
		buttonMouseR = findViewById(R.id.buttonMouseR);
		viewMousePad = findViewById(R.id.viewMousePad);
		viewMouseScroll = findViewById(R.id.viewMouseScroll);
		imageViewMouseConfigure = findViewById(R.id.imageViewMouseConfigure);
		
		linearLayoutModifiers = findViewById(R.id.linearLayoutModifiers);
		toggleButtonCtrl = findViewById(R.id.toggleButtonCtrl);
		toggleButtonShift = findViewById(R.id.toggleButtonShift);
		toggleButtonAlt = findViewById(R.id.toggleButtonAlt);
		toggleButtonGui = findViewById(R.id.toggleButtonGui);
		toggleButtonAltGr = findViewById(R.id.toggleButtonAltGr);
		buttonContext = findViewById(R.id.buttonContext);

		mRemotePreferences = new KP2ARemotePreferences(); 
		mRemote = new RemoteSupport(mRemotePreferences);
		mKeyboard = new KeyboardSupport(mRemote);		
		mMouse = new MousePadSupport(mRemote, relativeLayoutMouse, viewMousePad, buttonMouseL, buttonMouseM, buttonMouseR, viewMouseScroll);
		mModifiers = new ModifiersSupport(mRemote, linearLayoutModifiers, toggleButtonCtrl, toggleButtonShift, toggleButtonAlt, toggleButtonGui, toggleButtonAltGr, buttonContext); 

		//used to switch between mouse and touch-screen modes for mousepad area
		imageViewMouseConfigure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Context ctx = RemoteActivity.this;
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
				Editor editor = sharedPref.edit();
				if (mRemotePreferences.isInTouchScreenMode()) {	
					mRemote.goOutOfRange();					
					editor.putString(Const.PREF_REMOTE_MOUSE_MODE, "mouse");
					Toast.makeText(ctx, R.string.remote_mouse_mode, Toast.LENGTH_SHORT).show();	
				} else {					
					editor.putString(Const.PREF_REMOTE_MOUSE_MODE, "touchscreen");
					Toast.makeText(ctx, R.string.remote_touchscreen_mode, Toast.LENGTH_SHORT).show();	
				}
				editor.apply();	
				mRemotePreferences.reload(sharedPref); //will put mousepad into selected mode								
				manageUI(InputStickHID.getState()); //reload UI to display correct icon
			}
		});
		
		
		//manage height of mousepad, depending on whether soft-keyboard is visible or hidden
		final Window mRootWindow = getWindow();
		final View mRootView = mRootWindow.getDecorView().findViewById(android.R.id.content);
		mRootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					boolean skipNext; 

					public void onGlobalLayout() {						
						if (skipNext) {
							skipNext = false;							
						} else {						
							//skip next callback that will be performed due to modifications performed later on in this method
							skipNext = true;
							
							Rect r = new Rect();
							View view = mRootWindow.getDecorView();
							view.getWindowVisibleDisplayFrame(r);
							
							int[] loc = new int[2];
							relativeLayoutMouse.getLocationOnScreen(loc);
	
							RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)relativeLayoutMouse.getLayoutParams();
							params.height = r.bottom - loc[1] - 10;
							relativeLayoutMouse.setLayoutParams(params);
						}
					}
				});						
		
		IntentFilter filter;
		filter = new IntentFilter();
		filter.addAction(Const.BROADCAST_FORCE_FINISH_ALL);
		registerReceiver(finishReceiver, filter);				
	}
	
	@Override
	protected void onDestroy() {	      
	      unregisterReceiver(finishReceiver);
	      mHandler.removeCallbacks(tick);
	      super.onDestroy();
	}
	
	private void manageUI(int state) {
		if (mRemotePreferences.isInTouchScreenMode()) {
			imageViewMouseConfigure.setImageResource(R.drawable.ic_touch);
		} else {
			imageViewMouseConfigure.setImageResource(R.drawable.ic_mouse);
		}		
		
			switch (state){ 
			case ConnectionManager.STATE_DISCONNECTED:
			case ConnectionManager.STATE_FAILURE:
				buttonConnection.setText(R.string.connect); 
				break;
			case ConnectionManager.STATE_READY:
				buttonConnection.setText(R.string.disconnect);
				break;
			case ConnectionManager.STATE_CONNECTED:
			case ConnectionManager.STATE_CONNECTING:
				buttonConnection.setText(R.string.cancel);
				break;		
		}
		
		mMouse.manageUI(state);
		mModifiers.manageUI(state);
	}
	
	@Override
	public void onStateChanged(int state) {
		manageUI(state);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		InputStickHID.addStateListener(this);
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		mRemotePreferences.reload(sharedPref);
		autoResetModifiers = false; //TODO				
		
		manageUI(InputStickHID.getState()); 			
		//((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);				
		mHandler.post(tick);
	}
	
	@Override
	protected void onPause() {	
		mModifiers.resetModifiers();
		mRemote.resetHIDInterfaces();		
		//((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
		mHandler.removeCallbacks(tick);
	    InputStickHID.removeStateListener(this);	    
	    super.onPause();		
	}	
	
    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {    	
    	mKeyboard.onKeyMultiple(keyCode, repeatCount, event);
		if (autoResetModifiers) {
			mModifiers.resetModifiers();
		}
        return true;
    }    

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		super.onBackPressed(); 
    		return false;
    	} else {    	 
    		mKeyboard.onKeyDown(mModifiers.getModifiers(), keyCode, event);
			if (autoResetModifiers) {
				mModifiers.resetModifiers();
			}
	        return true;        
    	}        
    }
    
    
    //private static final int ACTION_SETTINGS = 0; TODO quick settings for adjusting mousepad/scroll sensitivity
    private static final int ACTION_QUICK_SHORTCUT_1 = 1;
    private static final int ACTION_QUICK_SHORTCUT_2 = 2;
    private static final int ACTION_QUICK_SHORTCUT_3 = 3;
    private static final int ACTION_LAYOUT_PRIMARY = 4;
    private static final int ACTION_LAYOUT_SECONDARY = 5;


    private AlertDialog getMoreDialog() {
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);    	
    	int qcCnt = PreferencesHelper.getEnabledQuickShortcuts(prefs);
    	boolean secLayout = PreferencesHelper.isSecondaryLayoutEnabled(prefs);
    	int actionsCnt = qcCnt;
    	//actionsCnt++; //SETTINGS
    	if (secLayout) {
    		actionsCnt += 2;
    	}
    	
    	CharSequence[] options = new CharSequence[actionsCnt];
    	final int[] actionIDs = new int[actionsCnt];
    	
    	int i = 0;
    	
		/*options[i] = "Settings";
		actionIDs[i] = ACTION_SETTINGS;
		i++;*/
    	
    	if (qcCnt >= 1) {
    		options[i] = getString(R.string.quickshortcut_1) + " (" + PreferencesHelper.getQuickShortcut(prefs, 1) + ")";
    		actionIDs[i] = ACTION_QUICK_SHORTCUT_1;
    		i++;
    	}
    	if (qcCnt >= 2) {
    		options[i] = getString(R.string.quickshortcut_2) + " (" + PreferencesHelper.getQuickShortcut(prefs, 2) + ")";
    		actionIDs[i] = ACTION_QUICK_SHORTCUT_2;
    		i++;
    	}
    	if (qcCnt >= 3) {
    		options[i] = getString(R.string.quickshortcut_3) + " (" + PreferencesHelper.getQuickShortcut(prefs, 3) + ")";
    		actionIDs[i] = ACTION_QUICK_SHORTCUT_3;
    		i++;
    	}
    	
    	if (secLayout) {
    		options[i] = getString(R.string.remote_set_layout) + PreferencesHelper.getPrimaryLayoutCode(prefs);
    		actionIDs[i] = ACTION_LAYOUT_PRIMARY;
    		i++;
    		
    		options[i] = getString(R.string.remote_set_layout) + PreferencesHelper.getSecondaryLayoutCode(prefs);
    		actionIDs[i] = ACTION_LAYOUT_SECONDARY;
    		i++;
    	}
    	

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.remote_more);
		builder.setItems(options, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	int actionId = actionIDs[which];
		        switch (actionId) { 	
	        		/*case ACTION_SETTINGS:		 	        			
	        			break;*/
		        	case ACTION_QUICK_SHORTCUT_1:		 
		        		executeQuickShortcut(prefs, 1);      		
		        		break;
		        	case ACTION_QUICK_SHORTCUT_2:	
		        		executeQuickShortcut(prefs, 2); 
		        		break;
		        	case ACTION_QUICK_SHORTCUT_3:		  
		        		executeQuickShortcut(prefs, 3); 
		        		break;
		        	case ACTION_LAYOUT_PRIMARY:		
		        		setLayout(prefs, true);
		        		break;
		        	case ACTION_LAYOUT_SECONDARY:	
		        		setLayout(prefs, false);
		        		break;		        		
		        }		        
		    }
		});
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();			
    }
    
    private void executeQuickShortcut(SharedPreferences prefs, int id) {
		String param = PreferencesHelper.getQuickShortcut(prefs, id);
		byte modifiers = MacroHelper.getModifiers(param);
		byte key = MacroHelper.getKey(param);
		mRemote.pressAndRelease(modifiers, key);
		Toast.makeText(RemoteActivity.this, param, Toast.LENGTH_SHORT).show();
    }
    
    private void setLayout(SharedPreferences prefs, boolean usePrimaryLayout) {
		prefs.edit().putBoolean(Const.PREF_REMOTE_USE_PRIMARY_LAYOUT, usePrimaryLayout).apply();
		String code = null;
		if (usePrimaryLayout) {
			code = PreferencesHelper.getPrimaryLayoutCode(prefs);
		} else {
			code = PreferencesHelper.getSecondaryLayoutCode(prefs);
		}
		Toast.makeText(RemoteActivity.this, code, Toast.LENGTH_SHORT).show();
    }
    
}