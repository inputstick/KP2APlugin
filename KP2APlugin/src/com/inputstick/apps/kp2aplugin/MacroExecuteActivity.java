package com.inputstick.apps.kp2aplugin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MacroExecuteActivity extends PluginDialogActivity {
	
	private static final String INDEX_KEY = "index";
	
	private int index;
	
	private EntryMacro macro;
	
	private Button buttonActionExecute;
	private Button buttonActionPrev;
	private Button buttonActionNext;
	private TextView textViewActionPreview;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setOptions(true, true, true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_macro_execute);
		
		Intent intent = getIntent();		
		final TypingParams params = new TypingParams(intent);
		final EntryData entryData = new EntryData(intent);
		final String macroData = intent.getStringExtra(Const.EXTRA_MACRO_DATA);
		
		if (macroData == null) {
			finish();
		} else {
			macro = new EntryMacro(macroData, entryData, params, false);
		}
		
		textViewActionPreview = (TextView)findViewById(R.id.textViewActionPreview);
		
		buttonActionExecute = (Button) findViewById(R.id.buttonActionExecute);
		buttonActionExecute.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (index >= macro.getActionsCount()) {
					finish();
				} else {
					macro.executeActionAtIndex(MacroExecuteActivity.this, index);  
					goToNext();
				}
			}
		});
		
		buttonActionPrev = (Button)findViewById(R.id.buttonActionPrev);
		buttonActionPrev.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				goToPrev();
			}
		});	
		
		buttonActionNext = (Button)findViewById(R.id.buttonActionNext);
		buttonActionNext.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				goToNext();
			}
		});	

		if (savedInstanceState == null) {			
			index = 0;
		} else {				
			index = savedInstanceState.getInt(INDEX_KEY);
		}
		manageUI();		
	}	


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(INDEX_KEY, index);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	private void goToPrev() {
		if (index > 0) {
			index--;
			manageUI();
		}			
	}
	
	private void goToNext() {
		if (index < macro.getActionsCount()) {
			index++;
			manageUI();
		}		
	}
	
	private void manageUI() {
		if (index >= macro.getActionsCount()) {
			textViewActionPreview.setText(R.string.end);
			buttonActionExecute.setText(R.string.done);
			buttonActionNext.setEnabled(false);
		} else {			
			textViewActionPreview.setText(getString(R.string.current_position) + " " + (index + 1) + "/" + macro.getActionsCount());			
			textViewActionPreview.append("\n" + getString(R.string.preview) + "\n" + macro.getPreviewAtIndex(index));
			buttonActionExecute.setText(R.string.execute);
			buttonActionNext.setEnabled(true);
		}
		if (index == 0) {
			buttonActionPrev.setEnabled(false);
		} else {
			buttonActionPrev.setEnabled(true);
		}
	}
	
}
