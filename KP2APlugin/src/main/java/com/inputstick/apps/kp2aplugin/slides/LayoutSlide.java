package com.inputstick.apps.kp2aplugin.slides;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.inputstick.api.Util;
import com.inputstick.api.layout.KeyboardLayout;
import com.inputstick.apps.kp2aplugin.Const;
import com.inputstick.apps.kp2aplugin.ItemToExecute;
import com.inputstick.apps.kp2aplugin.PreferencesHelper;
import com.inputstick.apps.kp2aplugin.R;
import com.inputstick.apps.kp2aplugin.TypingParams;

import java.util.Arrays;

public class LayoutSlide extends Fragment {

    public LayoutSlide() {    	
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }    

    @Nullable
    @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    	getView();
		final View view = inflater.inflate(R.layout.slide_layout, container, false);
		//TODO get localeof the deivce?

		final String[] layoutValues = Util.convertToStringArray(KeyboardLayout.getLayoutCodes());
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());		
		int selectedLayout = Arrays.asList(layoutValues).indexOf(PreferencesHelper.getPrimaryLayoutCode(prefs));	
		
		final EditText editTextTest = (EditText)view.findViewById(R.id.editTextTest);		
		
		final Spinner spinnerLayout = (Spinner)view.findViewById(R.id.spinnerLayout);	
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this.getContext(), android.R.layout.simple_spinner_item, KeyboardLayout.getLayoutNames(true));
		spinnerLayout.setAdapter(adapter);
		spinnerLayout.setSelection(selectedLayout, false);		
		spinnerLayout.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				PreferencesHelper.setPrimaryLayoutCode(PreferenceManager.getDefaultSharedPreferences(getActivity()), layoutValues[position]);				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
			
		});
		
		Button buttonTest = (Button)view.findViewById(R.id.buttonTest);	
		buttonTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TypingParams params = new TypingParams(layoutValues[spinnerLayout.getSelectedItemPosition()], Const.TYPING_SPEED_DEFAULT);
				//ItemToExecute.sendTextToService(getActivity(), editTextTest.getText().toString(), params); //TODO
				new ItemToExecute(editTextTest.getText().toString(), params).sendToService(getActivity(), true);
			}			
		});		
		
		return view;
	}



}