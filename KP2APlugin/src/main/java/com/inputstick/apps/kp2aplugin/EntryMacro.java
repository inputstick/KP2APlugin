package com.inputstick.apps.kp2aplugin;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;

public class EntryMacro {

	private ArrayList<ItemToExecute> listItems;
	private ArrayList<String> listPreviewData;
	
	@SuppressLint("DefaultLocale")
	//runInBackground: true - add clear flags for masked password; false - skip delays
	public EntryMacro(String macroData, EntryData entryData, TypingParams params, boolean runInBackground) {		
		listItems = new ArrayList<ItemToExecute>();
		listPreviewData = new ArrayList<String>();
		String actionParam, tmp;
		ItemToExecute ite;
		if ((macroData != null) && (macroData.length() > 0)) {
			String actionData[] = macroData.split("%");
			for (String action : actionData) {
				ite = null;
				tmp = action.toLowerCase();
				String preview = action;
				//no parameter
				if (tmp.startsWith(MacroHelper.MACRO_ACTION_PASSWORD)) {				
					ite = new ItemToExecute(entryData.getPassword(), params);
					preview = "%password";
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_USER_NAME)) {
					ite = new ItemToExecute(entryData.getUserName(), params);
					preview = "%user_name";
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_URL)) {
					ite = new ItemToExecute(entryData.getURL(), params);
					preview = "%URL";
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_PASSWORD_MASKED)) {
					if (runInBackground) {
						ite = new ItemToExecute(ItemToExecute.ITEM_TYPE_MASKED_PASSWORD_BACKGROUND, params, entryData.getPassword(), (byte)0, (byte)0, 0);
					} else {
						ite = new ItemToExecute(ItemToExecute.ITEM_TYPE_MASKED_PASSWORD_FOREGROUND, params, entryData.getPassword(), (byte)0, (byte)0, 0);
					}
					preview = "Masked password";
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_CLIPBOARD)) {
					ite = new ItemToExecute(ItemToExecute.ITEM_TYPE_CLIPBOARD_TYPING, params, null, (byte)0, (byte)0, 0);
					preview = "Type from clipboard";
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_BACKGROUND)) {
					//shouldRunInBackground = true;
				} else {
					//get parameter
					actionParam = MacroHelper.getParam(action); //!use s; tmp is lower case!
					if ((actionParam != null) && (actionParam.length() > 0)) { 					
						if (tmp.startsWith(MacroHelper.MACRO_ACTION_TYPE)) {		
							ite = new ItemToExecute(actionParam, params);
						}
						if (tmp.startsWith(MacroHelper.MACRO_ACTION_DELAY)) {
							if (runInBackground) {
								ite = new ItemToExecute(MacroHelper.getDelay(actionParam));
							}
						}
						if (tmp.startsWith(MacroHelper.MACRO_ACTION_KEY)) {						
							ite = new ItemToExecute(MacroHelper.getModifiers(actionParam), MacroHelper.getKey(actionParam), params);
						}		
					}
				}
				if (ite != null) {
					listItems.add(ite);
					listPreviewData.add(preview);
				}
			}
		}			
	}
	
	public void executeInBackground(Context ctx) {
		boolean executedHIDAction = false;
		for (ItemToExecute ite : listItems) {
			if (ite.execute(ctx)) {
				executedHIDAction = true;
			}
		}
		if (executedHIDAction) {
			InputStickService.onHIDAction();
		}

	}
	
	public void executeActionAtIndex(Context ctx, int index) {
		ItemToExecute ite = listItems.get(index);
		if (ite.execute(ctx)) {
			InputStickService.onHIDAction();
		}
	}
	
	public ItemToExecute getActionAtIndex(Context ctx, int index) {
		return listItems.get(index);
	}
	
	public int getActionsCount() {
		return listItems.size();
	}
	
	
	public String getActionPreviewAtIndex(int index) {
		return listPreviewData.get(index);
	}

}
