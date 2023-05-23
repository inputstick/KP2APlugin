package com.inputstick.apps.kp2aplugin;

import android.content.Intent;
import android.os.Bundle;

@SuppressWarnings("WeakerAccess")
public class TypingParams {
	
	private static final String KEY_LAYOUT_CODE = "_PARAM_LAYOUT_CODE";
	private static final String KEY_TYPING_SPEED = "_PARAM_TYPING_SPEED";
	
	private final String mLayoutCode;
	private final int mTypingSpeed;
	
	public TypingParams(String layoutCode, int typingSpeed) {
		mLayoutCode = layoutCode;
		mTypingSpeed = typingSpeed;		
	}

	public TypingParams(Intent intent) {
		mLayoutCode = intent.getStringExtra(KEY_LAYOUT_CODE);
		mTypingSpeed = intent.getIntExtra(KEY_TYPING_SPEED, Const.TYPING_SPEED_DEFAULT);		
	}
	
	public TypingParams(Bundle bundle) {
		mLayoutCode = bundle.getString(KEY_LAYOUT_CODE);
		mTypingSpeed = bundle.getInt(KEY_TYPING_SPEED, Const.TYPING_SPEED_DEFAULT);	
	}
	
	public Bundle getBundle() {
		Bundle b = new Bundle();
		b.putString(KEY_LAYOUT_CODE, mLayoutCode);
		b.putInt(KEY_TYPING_SPEED, mTypingSpeed);
		return b;
	}
	
	public String getLayoutCode() {
		return mLayoutCode;
	}
	
	public int getTypingSpeed() {
		return mTypingSpeed;
	}

}
