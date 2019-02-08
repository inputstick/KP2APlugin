package com.inputstick.apps.kp2aplugin;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.basic.InputStickKeyboard;
import com.inputstick.api.hid.HIDTransaction;
import com.inputstick.api.hid.KeyboardReport;

public class ItemToExecute {
	
	public static final int ITEM_TYPE_NOT_DEFINED = 				0;
	public static final int ITEM_TYPE_TEXT = 						1;
	public static final int ITEM_TYPE_KEY = 						2;
	public static final int ITEM_TYPE_DELAY = 						3;
	
	//macro actions:
	public static final int ITEM_TYPE_MASKED_PASSWORD_BACKGROUND =	4;  //clear activity stack
	public static final int ITEM_TYPE_MASKED_PASSWORD_FOREGROUND =	5;  //do not clear activity stack
	public static final int ITEM_TYPE_CLIPBOARD_TYPING =			6;
	
	public static final String KEY_TYPE = 			"ite_KEY_TYPE";
	public static final String KEY_TEXT = 			"ite_KEY_TEXT";
	public static final String KEY_LAYOUT_CODE = 	"ite_KEY_LAYOUT_CODE";
	public static final String KEY_TYPING_SPEED = 	"ite_KEY_TYPING_SPEED";
	public static final String KEY_KEY = 			"ite_KEY_KEY";
	public static final String KEY_MODIFIERS = 		"ite_KEY_MODIFIERS";
	public static final String KEY_DELAY_KEYS = 	"ite_KEY_DELAY_KEYS";
	public static final String KEY_CLEAR_QUEUE = 	"ite_KEY_CLEAR_QUEUE";

	private int mType;
	private TypingParams mParams;
	
	private String mText;
	
	private byte mKey;
	private byte mModifiers;
	
	private int mDelayKeys;
	
	private boolean canClearQueue;
	
	public ItemToExecute(String text, TypingParams params) {
		this(ITEM_TYPE_TEXT, params, text, (byte)0, (byte)0, 0);
	}
	
	public ItemToExecute(byte modifiers, byte key, TypingParams params) {
		this(ITEM_TYPE_KEY, params, null, key, modifiers, 0);
	}
	
	public ItemToExecute(int delayKeys) {
		this(ITEM_TYPE_DELAY, null, null, (byte)0, (byte)0, delayKeys);
	}		
	
	public ItemToExecute(int type, TypingParams params, String text, byte key, byte modifiers, int delayKeys) {
		mType = type;
		mText = text;
		mParams = params;
		mKey = key;
		mModifiers = modifiers;
		mDelayKeys = delayKeys;
	}
	
	
	public ItemToExecute(Bundle b) {
		if (b != null) {
			mType = b.getInt(KEY_TYPE, ITEM_TYPE_NOT_DEFINED);			
			mText = b.getString(KEY_TEXT, null);
			mParams = new TypingParams(b);		
			mKey = b.getByte(KEY_KEY);
			mModifiers = b.getByte(KEY_MODIFIERS);			
			mDelayKeys = b.getInt(KEY_DELAY_KEYS, 0);
			canClearQueue = b.getBoolean(KEY_CLEAR_QUEUE, false);
		}
	}
	
	public boolean execute(Context ctx) {			
		if (InputStickHID.getState() == ConnectionManager.STATE_READY) {					
			switch (mType) {
				case ITEM_TYPE_TEXT:
					InputStickKeyboard.type(mText, mParams.getLayoutCode(), mParams.getTypingSpeed());
					return true;
				case ITEM_TYPE_KEY:
					InputStickKeyboard.pressAndRelease(mModifiers, mKey, mParams.getTypingSpeed());
					return true;
				case ITEM_TYPE_DELAY:
					HIDTransaction t = new HIDTransaction();
					for (int i = 0; i < mDelayKeys * 3; i++) {  // 1 keypress = 3 HID reports (modifier, modifier+mKey, all released)
						t.addReport(new KeyboardReport((byte)0x00, (byte)0x00));
					}
					InputStickHID.addKeyboardTransaction(t);
					return true;
				//macro actions:			
				case ITEM_TYPE_MASKED_PASSWORD_BACKGROUND:
					ActionHelper.startMaskedPasswordActivity(ctx, mText, mParams, true);
					return false;
				case ITEM_TYPE_MASKED_PASSWORD_FOREGROUND:
					ActionHelper.startMaskedPasswordActivity(ctx, mText, mParams, false);
					return false;			
				case ITEM_TYPE_CLIPBOARD_TYPING:
					ActionHelper.startClipboardTyping(ctx, mParams);
					return false;				
			}	
		}
		return false;
	}	
	
	
	public Bundle getBundle() {
		Bundle b = new Bundle();
		b.putInt(KEY_TYPE, mType);
		b.putAll(mParams.getBundle());
		b.putString(KEY_TEXT, mText);				
		b.putByte(KEY_MODIFIERS, mModifiers);
		b.putByte(KEY_KEY, mKey);		
		b.putInt(KEY_DELAY_KEYS, mDelayKeys);
		b.putBoolean(KEY_CLEAR_QUEUE, canClearQueue);
		return b;
	}
	
	public boolean canClearQueue() {
		return canClearQueue;
	}
	
	public void setCanClearQueue(boolean canClearQueue) {
		this.canClearQueue = canClearQueue;
	}
	
	//canClearQueue - clears queue when not connected (to avoid executing multiple actions)
	public void sendToService(Context ctx, boolean canClearQueue) {
		this.canClearQueue = canClearQueue;
		Intent serviceIntent = new Intent(ctx, InputStickService.class);
		serviceIntent.setAction(Const.SERVICE_QUEUE_ITEM); 
		serviceIntent.putExtras(getBundle());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			ctx.startForegroundService(serviceIntent);
		} else {
			ctx.startService(serviceIntent);
		}
	}	
	
}
