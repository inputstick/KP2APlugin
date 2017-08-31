package com.inputstick.apps.kp2aplugin;

import java.util.HashMap;
import java.util.Iterator;

import keepass2android.pluginsdk.KeepassDefs;
import keepass2android.pluginsdk.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;

public class EntryData {
	
	private static final String KEY_ID = "_ENTRYDATA_ID";
	private static final String KEY_PASSWORD = "_ENTRYDATA_PASSWORD";
	private static final String KEY_USERNAME = "_ENTRYDATA_USERNAME";
	private static final String KEY_URL = "_ENTRYDATA_URL";
	
	private String entryId;
	private String password;
	private String userName;
	private String url;


	public EntryData(Intent intent) {
		if (intent != null) {
			if (intent.hasExtra(KEY_ID)) {
				//from onStartCommand or internal intent
				entryId = intent.getStringExtra(KEY_ID);
				password = intent.getStringExtra(KEY_PASSWORD);
				userName = intent.getStringExtra(KEY_USERNAME);
				url = intent.getStringExtra(KEY_URL);
			} else {
				//from broadcast receiver
				entryId = intent.getStringExtra(Strings.EXTRA_ENTRY_ID);
				HashMap<String, String> res = new HashMap<String, String>();
				try {
					JSONObject json = new JSONObject(intent.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA));
					for(Iterator<String> iter = json.keys();iter.hasNext();) {
					    String key = iter.next();
					    String value = json.get(key).toString();
					    res.put(key, value);
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				} 
				
				password = res.get(KeepassDefs.PasswordField);
				userName = res.get(KeepassDefs.UserNameField);
				url = res.get(KeepassDefs.UrlField);				
			}
		} 			
	}
	
	public Bundle getBundle() {
		Bundle b = new Bundle();
		b.putString(KEY_ID, entryId);
		b.putString(KEY_PASSWORD, password);
		b.putString(KEY_USERNAME, userName);
		b.putString(KEY_URL, url);
		return b;
	}
	
	public static Bundle getBundle(String entryId, HashMap<String, String> entryFields) {
		Bundle b = new Bundle();		
		b.putString(KEY_ID, entryId);
		b.putString(KEY_PASSWORD, entryFields.get(KeepassDefs.PasswordField));
		b.putString(KEY_USERNAME, entryFields.get(KeepassDefs.UserNameField));
		b.putString(KEY_URL, entryFields.get(KeepassDefs.UrlField));		
		return b;		
	}
	
	
	public String getEntryId() {
		return entryId;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getURL() {
		return url;
	}	
	
}
