package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;

public class EntryMacro {

	private ArrayList<ItemToExecute> listItems;
	private ArrayList<String> listRawData;
	
	@SuppressLint("DefaultLocale")
	//runInBackground: true - add clear flags for masked password; false - skip delays
	public EntryMacro(String macroData, EntryData entryData, TypingParams params, boolean runInBackground) {		
		listItems = new ArrayList<ItemToExecute>();
		listRawData = new ArrayList<String>();
		String actionParam, tmp;
		ItemToExecute ite;
		if ((macroData != null) && (macroData.length() > 0)) {
			String actionData[] = macroData.split("%");
			for (String action : actionData) {			
				ite = null;
				tmp = action.toLowerCase();
				//no parameter
				if (tmp.startsWith(MacroHelper.MACRO_ACTION_PASSWORD)) {				
					ite = new ItemToExecute(entryData.getPassword(), params);
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_USER_NAME)) {
					ite = new ItemToExecute(entryData.getUserName(), params);
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_URL)) {
					ite = new ItemToExecute(entryData.getURL(), params);
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_PASSWORD_MASKED)) {
					if (runInBackground) {
						ite = new ItemToExecute(ItemToExecute.ITEM_TYPE_MASKED_PASSWORD_BACKGROUND, params, entryData.getPassword(), (byte)0, (byte)0, 0);
					} else {
						ite = new ItemToExecute(ItemToExecute.ITEM_TYPE_MASKED_PASSWORD_FOREGROUND, params, entryData.getPassword(), (byte)0, (byte)0, 0);
					}
					
				} else if (tmp.startsWith(MacroHelper.MACRO_ACTION_CLIPBOARD)) {
					ite = new ItemToExecute(ItemToExecute.ITEM_TYPE_CLIPBOARD_TYPING, params, null, (byte)0, (byte)0, 0);
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
							if ( !runInBackground) {
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
					listRawData.add(action);
				}
			}
		}			
	}
	
	public void executeInBackground(Context ctx) {
		for (ItemToExecute ite : listItems) {
			ite.execute(ctx);
		}
	}
	
	public void executeActionAtIndex(Context ctx, int index) {
		ItemToExecute ite = listItems.get(index);
		ite.execute(ctx);
	}
	
	public ItemToExecute getActionAtIndex(Context ctx, int index) {
		return listItems.get(index);
	}
	
	public int getActionsCount() {
		return listItems.size();
	}
	
	
	public String getPreviewAtIndex(int index) {
		return listRawData.get(index);
	}

}
