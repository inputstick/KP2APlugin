package com.inputstick.apps.kp2aplugin;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.inputstick.api.hid.HIDKeycodes;

@SuppressWarnings("WeakerAccess")
public class MacroHelper {
	
	public static final String MACRO_ACTION_URL = "url";
	public static final String MACRO_ACTION_USER_NAME = "user";
	public static final String MACRO_ACTION_PASSWORD = "pass";
	public static final String MACRO_ACTION_PASSWORD_MASKED = "masked_pass";
	public static final String MACRO_ACTION_CLIPBOARD = "clipboard";
	public static final String MACRO_ACTION_TYPE = "type";
	public static final String MACRO_ACTION_KEY = "key";
	public static final String MACRO_ACTION_DELAY = "delay";
	public static final String MACRO_ACTION_BACKGROUND = "background";
	
	public static final String MACRO_BACKGROUND_EXEC_STRING = "%" + MACRO_ACTION_BACKGROUND;
	
	private static class KeyLabel {		
		byte keyCode;
		String primary;
		String secondary;
		
		public KeyLabel(byte keyCode, String primary, String secondary) {
			this.keyCode = keyCode;
			this.primary = primary;
			this.secondary = secondary;
		}

	}
	
	private static final KeyLabel[] modLUT = {
		
		new KeyLabel(HIDKeycodes.ALT_LEFT, 		"Alt", "LAlt"),
		new KeyLabel(HIDKeycodes.CTRL_LEFT, 	"Ctrl", "LCtrl"),
		new KeyLabel(HIDKeycodes.SHIFT_LEFT, 	"Shift", "LShift"),
		new KeyLabel(HIDKeycodes.GUI_LEFT, 		"GUI", "Win"),
		
		new KeyLabel(HIDKeycodes.ALT_RIGHT, 	"RAlt", "AltGr"),
		new KeyLabel(HIDKeycodes.CTRL_RIGHT, 	"RCtrl", null),
		new KeyLabel(HIDKeycodes.SHIFT_RIGHT, 	"RShift", null),
		new KeyLabel(HIDKeycodes.GUI_RIGHT, 	"RGUI", "RWin"),
	};
	
	private static final KeyLabel[] keyLUT = {
			new KeyLabel(HIDKeycodes.NONE, 			"None", null),
								
			new KeyLabel(HIDKeycodes.KEY_Q, 		"Q", null),
			new KeyLabel(HIDKeycodes.KEY_W, 		"W", null),
			new KeyLabel(HIDKeycodes.KEY_E, 		"E", null),
			new KeyLabel(HIDKeycodes.KEY_R, 		"R", null),
			new KeyLabel(HIDKeycodes.KEY_T, 		"T", null),
			new KeyLabel(HIDKeycodes.KEY_Y, 		"Y", null),
			new KeyLabel(HIDKeycodes.KEY_U, 		"U", null),
			new KeyLabel(HIDKeycodes.KEY_I, 		"I", null),
			new KeyLabel(HIDKeycodes.KEY_O, 		"O", null),
			new KeyLabel(HIDKeycodes.KEY_P, 		"P", null),
			new KeyLabel(HIDKeycodes.KEY_LEFT_BRACKET, 	"[", "{"),
			new KeyLabel(HIDKeycodes.KEY_RIGHT_BRACKET, "]", "}"),			
						
			new KeyLabel(HIDKeycodes.KEY_A, 		"A", null),
			new KeyLabel(HIDKeycodes.KEY_S, 		"S", null),
			new KeyLabel(HIDKeycodes.KEY_D, 		"D", null),
			new KeyLabel(HIDKeycodes.KEY_F, 		"F", null),
			new KeyLabel(HIDKeycodes.KEY_G, 		"G", null),
			new KeyLabel(HIDKeycodes.KEY_H, 		"H", null),
			new KeyLabel(HIDKeycodes.KEY_J, 		"J", null),
			new KeyLabel(HIDKeycodes.KEY_K, 		"K", null),
			new KeyLabel(HIDKeycodes.KEY_L, 		"L", null),
			new KeyLabel(HIDKeycodes.KEY_SEMICOLON, ";", ":"),
			new KeyLabel(HIDKeycodes.KEY_APOSTROPHE,"'", "\""),
			
			new KeyLabel(HIDKeycodes.KEY_Z, 		"Z", null),
			new KeyLabel(HIDKeycodes.KEY_X, 		"X", null),
			new KeyLabel(HIDKeycodes.KEY_C, 		"C", null),
			new KeyLabel(HIDKeycodes.KEY_V, 		"V", null),
			new KeyLabel(HIDKeycodes.KEY_B, 		"B", null),
			new KeyLabel(HIDKeycodes.KEY_N, 		"N", null),
			new KeyLabel(HIDKeycodes.KEY_M, 		"M", null),
			new KeyLabel(HIDKeycodes.KEY_COMA, 		",", "<"),
			new KeyLabel(HIDKeycodes.KEY_DOT, 		".", ">"),
			new KeyLabel(HIDKeycodes.KEY_SLASH, 	"/", "?"),
			new KeyLabel(HIDKeycodes.KEY_BACKSLASH,	"\\", "|"),
			
			new KeyLabel(HIDKeycodes.KEY_GRAVE,		"`", "~"),
			new KeyLabel(HIDKeycodes.KEY_1, 		"1", "!"),
			new KeyLabel(HIDKeycodes.KEY_2, 		"2", "@"),
			new KeyLabel(HIDKeycodes.KEY_3, 		"3", "#"),
			new KeyLabel(HIDKeycodes.KEY_4, 		"4", "$"),
			new KeyLabel(HIDKeycodes.KEY_5, 		"5", "%"),
			new KeyLabel(HIDKeycodes.KEY_6, 		"6", "^"),
			new KeyLabel(HIDKeycodes.KEY_7, 		"7", "&"),
			new KeyLabel(HIDKeycodes.KEY_8, 		"8", "*"),
			new KeyLabel(HIDKeycodes.KEY_9, 		"9", "("),
			new KeyLabel(HIDKeycodes.KEY_0, 		"0", ")"),
			new KeyLabel(HIDKeycodes.KEY_MINUS, 	"-", "_"),
			new KeyLabel(HIDKeycodes.KEY_EQUALS, 	"=", "+"),
			
			new KeyLabel(HIDKeycodes.KEY_BACKSPACE, "Backspace", null),
			new KeyLabel(HIDKeycodes.KEY_ENTER, "Enter", null),
			new KeyLabel(HIDKeycodes.KEY_TAB,		"Tab", null),
			new KeyLabel(HIDKeycodes.KEY_SPACEBAR, "Space", null),
			new KeyLabel(HIDKeycodes.KEY_CAPS_LOCK, "Capslock", "Caps"),							
			new KeyLabel(HIDKeycodes.KEY_ESCAPE, "Esc", "Escape"),
			new KeyLabel(HIDKeycodes.KEY_APPLICATION, "Application", "App"),
			
			new KeyLabel(HIDKeycodes.KEY_F1, "F1", null),
			new KeyLabel(HIDKeycodes.KEY_F2, "F2", null),
			new KeyLabel(HIDKeycodes.KEY_F3, "F3", null),
			new KeyLabel(HIDKeycodes.KEY_F4, "F4", null),
			new KeyLabel(HIDKeycodes.KEY_F5, "F5", null),
			new KeyLabel(HIDKeycodes.KEY_F6, "F6", null),
			new KeyLabel(HIDKeycodes.KEY_F7, "F7", null),
			new KeyLabel(HIDKeycodes.KEY_F8, "F8", null),
			new KeyLabel(HIDKeycodes.KEY_F9, "F9", null),
			new KeyLabel(HIDKeycodes.KEY_F10, "F10", null),
			new KeyLabel(HIDKeycodes.KEY_F11, "F11", null),
			new KeyLabel(HIDKeycodes.KEY_F12, "F12", null),
			
			new KeyLabel(HIDKeycodes.KEY_PRINT_SCREEN, "PrintScrn", "Printscreen"),
			new KeyLabel(HIDKeycodes.KEY_SCROLL_LOCK, "ScrollLock", "scroll"),
			new KeyLabel(HIDKeycodes.KEY_PASUE, "Pause", "Break"),
			
			new KeyLabel(HIDKeycodes.KEY_INSERT, "Insert", "Ins"),
			new KeyLabel(HIDKeycodes.KEY_HOME, "Home", null),
			new KeyLabel(HIDKeycodes.KEY_PAGE_UP, "PageUp", "PgUp"),
			new KeyLabel(HIDKeycodes.KEY_DELETE, "Delete", "Del"),
			new KeyLabel(HIDKeycodes.KEY_END, "End", null),
			new KeyLabel(HIDKeycodes.KEY_PAGE_DOWN, "PageDown", "PgDn"),
			
			new KeyLabel(HIDKeycodes.KEY_ARROW_LEFT, "Left", null),
			new KeyLabel(HIDKeycodes.KEY_ARROW_RIGHT, "Right", null),
			new KeyLabel(HIDKeycodes.KEY_ARROW_UP, "Up", null),
			new KeyLabel(HIDKeycodes.KEY_ARROW_DOWN, "Down", null),

			new KeyLabel(HIDKeycodes.KEY_NUM_1, 	"Num_1", "Num_end"),
			new KeyLabel(HIDKeycodes.KEY_NUM_2, 	"Num_2", "Num_down"),
			new KeyLabel(HIDKeycodes.KEY_NUM_3, 	"Num_3", "Num_pagedown"),
			new KeyLabel(HIDKeycodes.KEY_NUM_4, 	"Num_4", "Num_left"),
			new KeyLabel(HIDKeycodes.KEY_NUM_5, 	"Num_5", "Num_center"),
			new KeyLabel(HIDKeycodes.KEY_NUM_6, 	"Num_6", "Num_right"),
			new KeyLabel(HIDKeycodes.KEY_NUM_7, 	"Num_7", "Num_home"),
			new KeyLabel(HIDKeycodes.KEY_NUM_8, 	"Num_8", "Num_up"),
			new KeyLabel(HIDKeycodes.KEY_NUM_9, 	"Num_9", "Num_pageup"),
			new KeyLabel(HIDKeycodes.KEY_NUM_0, 	"Num_0", "Num_insert"),			
			new KeyLabel(HIDKeycodes.KEY_NUM_ENTER, "Num_enter", null),
			new KeyLabel(HIDKeycodes.KEY_NUM_DOT, 	"Num_dot", "Num_delete"),
			new KeyLabel(HIDKeycodes.KEY_NUM_PLUS, 	"Num_plus", null),
			new KeyLabel(HIDKeycodes.KEY_NUM_MINUS, "Num_minus", null),
			new KeyLabel(HIDKeycodes.KEY_NUM_STAR, 	"Num_star", null),
			new KeyLabel(HIDKeycodes.KEY_NUM_SLASH, "Num_slash", null),
	};


	public static String[] getKeyList() {
		String[] result = new String[keyLUT.length];		
		for (int i = 0; i < keyLUT.length; i++) {
			result[i] = keyLUT[i].primary;
		}
		return result;
	}
	
	public static String getParam(String s) {
		if ((s != null) && (s.length() > 0)) {
			int index = s.indexOf("=");
			if (index > 0) {
				return s.substring(index + 1);
			}
		}
		return null;
	}
	
	//returns first found non-modifier key only!
	public static byte getKey(String param) {
		byte key = 0x00;
		String[] keys = prepareSearchArray(param);
		for (String s : keys) {
			if ((s != null) && (s.length() > 0)) {
				key = findKey(s);
				if (key != 0) {
					return key;
				}
			}
		}
		return key;
	}

	public static byte getModifiers(String param) {
		byte modifiers = 0x00;
		String[] keys = prepareSearchArray(param);
		for (String s : keys) {
			if ((s != null) && (s.length() > 0)) {
				modifiers |= findMod(s);
			}
		}
		return modifiers;
	}
	
	public static int getDelay(String s) {
		int delay;
		try {
			delay = Integer.parseInt(s);
		} catch (Exception e) {	
			delay = 0;
		}
		return delay;
	}
	
	@SuppressLint("DefaultLocale")
	private static String[] prepareSearchArray(String param) {
		param = param.toLowerCase();
		param = param.replace(" ", ""); //remove spaces!
		param = param.replace("++", "+="); //handle special case!
		return param.split("\\+");
	}
	
	private static byte findMod(String str) {
		return searchLUT(str, modLUT);
	}
	
	private static byte findKey(String str) {
		return searchLUT(str, keyLUT);
	}
	
	private static byte searchLUT(String str, KeyLabel[] lut) {
		if (str != null) {
			for (KeyLabel l : lut) {
				if (l.primary != null) {
					if (str.equalsIgnoreCase(l.primary)) {
						return l.keyCode;
					}
				}
				if (l.secondary != null) {
					if (str.equalsIgnoreCase(l.secondary)) {
						return l.keyCode;
					}
				}
			}
		}
		return 0;
	}
	
	public static int getIndexForKey(byte key) {		
		for (int i = 0; i < keyLUT.length; i++) {			
			if (keyLUT[i].keyCode == key) {
				return i;
			}
		}
		return -1;
	}
	

	public static String loadMacro(SharedPreferences prefs, String entryId) {
		return prefs.getString(Const.MACRO_PREF_PREFIX + entryId, null);
	}
	
	public static void saveMacro(SharedPreferences prefs, String entryId, String macroData) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Const.MACRO_PREF_PREFIX + entryId, macroData);
		editor.apply();	
	}
	
	public static void deleteMacro(SharedPreferences prefs, String entryId) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Const.MACRO_PREF_PREFIX + entryId);
		editor.apply();
	}
	
}
