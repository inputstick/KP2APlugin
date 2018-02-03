package com.inputstick.apps.kp2aplugin.remote;

import android.content.SharedPreferences;

import com.inputstick.api.layout.KeyboardLayout;
import com.inputstick.api.utils.remote.RemotePreferences;
import com.inputstick.apps.kp2aplugin.PreferencesHelper;

public class KP2ARemotePreferences extends RemotePreferences {
	
	@Override
	public void reload(SharedPreferences sharedPref) {
		String code = PreferencesHelper.getRemoteLayoutCode(sharedPref);
		layout = KeyboardLayout.getLayout(code);
		typingSpeed = PreferencesHelper.getTypingSpeed(sharedPref);
		showModifiers = true;
		
		showMouse = true;
		touchScreenMode = !PreferencesHelper.isRemoteInMouseMode(sharedPref);
		ratio = 0;
		tapToClick = true;
		mouseSensitivity = PreferencesHelper.getRemoteMouseSensitivity(sharedPref);
		scrollSensitivity = PreferencesHelper.getRemoteScrollSensitivity(sharedPref);
		proximityThreshold = 0;	
		tapInterval = 500;
	}

}
