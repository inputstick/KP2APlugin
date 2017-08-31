package com.inputstick.apps.kp2aplugin;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MacroActivity extends Activity {
	
	private SharedPreferences prefs;
	
	private String macro;
	private String entryId;
	
	private EditText editTextMacro;
	private EditText editTextString;
	private Spinner spinnerDelay;
	private Button buttonDelete;
	private Button buttonSave;
	private Button buttonAddFromField;
	private RadioButton radioButtonBackground;
	private RadioButton radioButtonShowControls;
	private TextView textViewTemplate;		
	private Button buttonTemplateSave;
	private Button buttonTemplateLoad;		
	
	private boolean templateMode;
	private int templateId;
	private String savedTemplate;
	
	private ValueAnimator va;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_macro);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		editTextMacro = (EditText)findViewById(R.id.editTextMacro);
		editTextString = (EditText)findViewById(R.id.editTextString);
		spinnerDelay = (Spinner)findViewById(R.id.spinnerDelay);		
		
		textViewTemplate = (TextView)findViewById(R.id.textViewTemplate);		
		
		radioButtonBackground = (RadioButton)findViewById(R.id.radioButtonBackground);
		radioButtonBackground.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if (isChecked) {
					setExecutionMode(true);
				}				
			}			
		});
		radioButtonShowControls = (RadioButton)findViewById(R.id.radioButtonShowControls);
		radioButtonShowControls.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if (isChecked) {
					setExecutionMode(false);
				}				
			}			
		});	
		
		editTextMacro.addTextChangedListener(new TextWatcher() {            
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					buttonSave.setEnabled(true);
				} else {
					buttonSave.setEnabled(false);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		
		
		buttonDelete = (Button)findViewById(R.id.buttonDelete);		
		buttonDelete.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MacroActivity.this);
				alert.setTitle(R.string.delete_title);
				alert.setMessage(R.string.delete_message);
				alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						deleteMacro();
					}
				});
				alert.setNegativeButton(R.string.cancel, null);
				alert.show();				
			}
		});
		

		buttonSave = (Button)findViewById(R.id.buttonSave);		
		buttonSave.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				saveMacro();
			}
		});
		
		Button button;
		button = (Button)findViewById(R.id.buttonHelp);		
		button.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MacroActivity.this);
				alert.setTitle(R.string.help);
				alert.setMessage(R.string.macro_help);	
				alert.setNeutralButton(R.string.ok, null);
				alert.show();	
			}
		});
		
		buttonAddFromField = (Button)findViewById(R.id.buttonAddFromField);		
		buttonAddFromField.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {							
				CharSequence options[] = new CharSequence[] {MacroActivity.this.getString(R.string.user_name), 
															MacroActivity.this.getString(R.string.password), 
															MacroActivity.this.getString(R.string.url),
															MacroActivity.this.getString(R.string.password_masked), 															
															MacroActivity.this.getString(R.string.clipboard_authenticator)};
				

				AlertDialog.Builder builder = new AlertDialog.Builder(MacroActivity.this);
				builder.setTitle(R.string.add_from_field);
				builder.setItems(options, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which) { 
				        	case 0:
				        		addAction(MacroHelper.MACRO_ACTION_USER_NAME, null);
				        		break;
				        	case 1:
				        		addAction(MacroHelper.MACRO_ACTION_PASSWORD, null);
				        		break;
				        	case 2:
				        		addAction(MacroHelper.MACRO_ACTION_URL, null);				        		
				        		break;
				        	case 3:
				        		addAction(MacroHelper.MACRO_ACTION_PASSWORD_MASKED, null);
				        		break;	
				        	case 4:
				        		addAction(MacroHelper.MACRO_ACTION_CLIPBOARD, null);
				        		break;					        		
				        }
				    }
				});
				builder.show();				
			}
		});
		
		button = (Button)findViewById(R.id.buttonAddEnter);		
		button.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				addAction(MacroHelper.MACRO_ACTION_KEY, "enter");
			}
		});
		button = (Button)findViewById(R.id.buttonAddTab);		
		button.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				addAction(MacroHelper.MACRO_ACTION_KEY, "tab");
			}
		});
		button = (Button)findViewById(R.id.buttonAddCustom);		
		button.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MacroActivity.this);
				alert.setTitle(R.string.custom_key_title);		
				
				final LinearLayout lin= new LinearLayout(MacroActivity.this);
				lin.setOrientation(LinearLayout.VERTICAL);
				
				final TextView tvInfo = new TextView(MacroActivity.this);				
				tvInfo.setText(R.string.custom_key_message);
				
				final Spinner spinner = new Spinner(MacroActivity.this);				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(MacroActivity.this, android.R.layout.simple_spinner_item, MacroHelper.getKeyList());
				spinner.setAdapter(adapter);
				
				final CheckBox cbCtrlLeft = new CheckBox(MacroActivity.this);
				cbCtrlLeft.setText("Ctrl");
				final CheckBox cbShiftLeft = new CheckBox(MacroActivity.this);
				cbShiftLeft.setText("Shift");
				final CheckBox cbAltLeft = new CheckBox(MacroActivity.this);
				cbAltLeft.setText("Alt");
				final CheckBox cbGuiLeft = new CheckBox(MacroActivity.this);
				cbGuiLeft.setText("GUI (Win key)");
				final CheckBox cbAltRight = new CheckBox(MacroActivity.this);
				cbAltRight.setText("AltGr (right)");
				
				lin.addView(tvInfo);
				lin.addView(spinner);
				lin.addView(cbCtrlLeft);
				lin.addView(cbShiftLeft);	
				lin.addView(cbAltLeft);	
				lin.addView(cbGuiLeft);	
				lin.addView(cbAltRight);	
				alert.setView(lin);
				
				alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					private String param;
					
					private void add(String toAdd) {
						if (param.length() > 0) {
							param += "+";
						}
						param += toAdd;
					}
					
					public void onClick(DialogInterface dialog, int whichButton) {
						param = "";
						if (cbCtrlLeft.isChecked()) add("Ctrl");
						if (cbShiftLeft.isChecked()) add("Shift");
						if (cbAltLeft.isChecked()) add("Alt");
						if (cbGuiLeft.isChecked()) add("Gui");
						if (cbAltRight.isChecked()) add("AltGr");	
						add((String)spinner.getSelectedItem());
						addAction(MacroHelper.MACRO_ACTION_KEY, param);
					}
				});
				alert.setNegativeButton(R.string.cancel, null);
				alert.show();
			}
		});
		button = (Button)findViewById(R.id.buttonAddString);		
		button.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				String s = editTextString.getText().toString();
				if ((s != null) && (s.length() > 0)) {
					if (s.contains("%")) {
						Toast.makeText(MacroActivity.this, R.string.illegal_character_toast, Toast.LENGTH_LONG).show();
					} else {
						addAction(MacroHelper.MACRO_ACTION_TYPE, s);
						editTextString.setText("");
					}
				}								
			}
		});
		button = (Button)findViewById(R.id.buttonAddDelay);		
		button.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				addAction(MacroHelper.MACRO_ACTION_DELAY, String.valueOf(spinnerDelay.getSelectedItem()));
			}
		});		
		
		buttonTemplateSave = (Button) findViewById(R.id.buttonTemplateSave);
		buttonTemplateSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSaveTemplateDialog();
			}
		});
		buttonTemplateLoad = (Button)findViewById(R.id.buttonTemplateLoad);		
		buttonTemplateLoad.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				showLoadTemplateDialog();
			}
		});
		
		
		Bundle b = getIntent().getExtras();
		//macro = b.getString(Const.EXTRA_MACRO_DATA, null);
		entryId = b.getString(Const.EXTRA_ENTRY_ID, null);
		macro = MacroHelper.loadMacro(prefs, entryId);
		
		if (b.getBoolean(Const.EXTRA_MACRO_RUN_BUT_EMPTY, false)) {
			Toast.makeText(MacroActivity.this, R.string.no_macro_create_new, Toast.LENGTH_LONG).show();
		}
		
		templateMode = b.getBoolean(Const.EXTRA_MACRO_TEMPLATE_MODE, false);
		if (templateMode) {
			templateId = b.getInt(Const.EXTRA_TEMPLATE_ID, -1);
			macro = TemplateHelper.loadTemplate(prefs, templateId); //is never null!
			savedTemplate = macro;			
			buttonSave.setVisibility(View.INVISIBLE);
			buttonDelete.setVisibility(View.INVISIBLE);			
			textViewTemplate.setVisibility(View.INVISIBLE);
			buttonTemplateSave.setVisibility(View.INVISIBLE);
			buttonTemplateLoad.setVisibility(View.INVISIBLE);			
		}
		
		if (macro == null) {
			macro = "";
			setTitle(R.string.add_macro_title);
			buttonDelete.setEnabled(false);
			buttonSave.setEnabled(false);
		} else {
			setTitle(R.string.edit_macro_title);
			editTextMacro.setText(macro);	
		}
		
		//overwrite title
		if (templateMode) {
			if (macro.equals("")) {
				setTitle(R.string.add_template_title);
			} else {
				setTitle(R.string.edit_template_title);
			}
		}
		
		manageUI();
	}
	
	@Override
	public void onPause() {
		if (va != null) {
			va.end();
		}		
	    super.onPause(); 	    
	}	
	
	@Override
	public void onResume() {
	    super.onResume(); 	  	    
        if (editTextMacro.getText().length() < 1) {   
	        int end = Color.rgb(0x00, 0x00, 0x00);
	        int start = Color.rgb(0x00, 128, 255);
	        va = ObjectAnimator.ofInt(findViewById(R.id.buttonAddFromField), "textColor", start, end);
	        va.setDuration(750);
	        va.setEvaluator(new ArgbEvaluator());
	        va.setRepeatCount(ValueAnimator.INFINITE);
	        va.setRepeatMode(ValueAnimator.REVERSE);
	        va.start();  
        } else {
        	buttonAddFromField.setTextColor(0xFF000000);           	 
        }
	}	
	
	private void manageUI() {
		if (macro != null) {
			if (macro.startsWith(MacroHelper.MACRO_BACKGROUND_EXEC_STRING)) {
				radioButtonBackground.setChecked(true);
			}		
		}
	}
	
	private void showSaveTemplateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MacroActivity.this);
		builder.setTitle(R.string.save_as);
		builder.setItems(TemplateHelper.getTemplateNames(prefs), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getSaveTemplateDialog(which).show();
			}
		});
		builder.show();		
	}
	
	private void showLoadTemplateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MacroActivity.this);
		builder.setTitle(R.string.load_from);
		builder.setItems(TemplateHelper.getTemplateNames(prefs), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String tmp = TemplateHelper.loadTemplate(prefs, which);				
				if ((tmp != null) && ( !tmp.equals(""))) {						
					editTextMacro.setText(tmp);
					macro = tmp;
					manageUI();
					saveMacro();
				} else {
					Toast.makeText(MacroActivity.this, R.string.empty, Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.show();		
	}
	
	private void setExecutionMode(boolean isBackground) {
		String tmp = editTextMacro.getText().toString();		
		if (isBackground) {
			if ((tmp != null) && ( !tmp.startsWith(MacroHelper.MACRO_BACKGROUND_EXEC_STRING))) {				
				editTextMacro.setText(MacroHelper.MACRO_BACKGROUND_EXEC_STRING + tmp);
			}					
		} else {
			if ((tmp != null) && (tmp.startsWith(MacroHelper.MACRO_BACKGROUND_EXEC_STRING))) {				
				editTextMacro.setText(tmp.substring(MacroHelper.MACRO_BACKGROUND_EXEC_STRING.length()));
			}	
		}
	}
	
	private void addAction(String action, String param) {
		String tmp = "%" + action;
		if (param != null) {
			tmp += "=" + param;
		}
		
		String m = editTextMacro.getText().toString();		
		m += tmp;
		Toast.makeText(this, getString(R.string.added) + " " + tmp, Toast.LENGTH_SHORT).show();
		if (radioButtonBackground.isChecked()) {
			if ( !m.startsWith(MacroHelper.MACRO_BACKGROUND_EXEC_STRING)) {
				m = MacroHelper.MACRO_BACKGROUND_EXEC_STRING + m;
			}
		}
		
		editTextMacro.setText(m);
		
		if (va != null) {
			va.end();
		}	
	}
	
	private void saveMacro() {
		macro = editTextMacro.getText().toString();		
		if ("".equals(macro)) {
			deleteMacro();
		} else {
			MacroHelper.saveMacro(prefs, entryId, macro);
			buttonDelete.setEnabled(true);
			Toast.makeText(MacroActivity.this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
		}		
	}
	
	private void deleteMacro() {
		macro = "";
		MacroHelper.deleteMacro(prefs, entryId);
		editTextMacro.setText("");
		buttonDelete.setEnabled(false);
		Toast.makeText(MacroActivity.this, R.string.deleted_toast, Toast.LENGTH_SHORT).show();
	}
	
	
	private boolean shouldShowSaveDialog() {
		if (templateMode) {			
			return ( !editTextMacro.getText().toString().equals(savedTemplate));
		} else {		
			return ( !editTextMacro.getText().toString().equals(macro)); 
		}
	}
	
	
	@Override
	public void onBackPressed() {
		if (shouldShowSaveDialog()) {
			AlertDialog.Builder alert = new AlertDialog.Builder(MacroActivity.this);			
			if (templateMode) {			
				alert.setMessage(R.string.template_not_saved);
				alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						getSaveTemplateDialog(templateId).show();
					}
				});
			} else {
				alert.setMessage(R.string.save_message);
				alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						saveMacro();
						finish();			
					}
				});
			}			
			//common:
			alert.setTitle(R.string.save_title);
			alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			});		
			alert.setNeutralButton(R.string.cancel, null);
			alert.show();
		} else {
			super.onBackPressed();
		}
	}
	
	public AlertDialog getSaveTemplateDialog(final int id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);		
		alert.setTitle(R.string.template_name); 		

		final EditText editTextName = new EditText(this);
		//display current name if exists, if not, leave empty
		if (prefs.contains(Const.TEMPLATE_NAME_PREF_PREFIX + id)) {
			editTextName.setText(TemplateHelper.getTemplateName(prefs, id));
		} 
		final LinearLayout lin= new LinearLayout(this);
		lin.setOrientation(LinearLayout.VERTICAL);
		lin.addView(editTextName);
		alert.setView(lin);
		
		alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = editTextName.getText().toString();
				//use default name if no name was provided
				if ("".equals(name)) {
					name = TemplateHelper.getTemplateDefaultName(id);
				}	
				templateId = id; //just in case it is manually changed
				savedTemplate = editTextMacro.getText().toString();
				TemplateHelper.saveTemplate(prefs, id, name, savedTemplate);
				Toast.makeText(MacroActivity.this, R.string.saved_toast, Toast.LENGTH_SHORT).show();				
			}
		});
		alert.setNegativeButton(R.string.cancel, null);
		return alert.show();		
	}		

}
