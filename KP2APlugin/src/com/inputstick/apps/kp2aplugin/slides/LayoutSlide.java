package com.inputstick.apps.kp2aplugin.slides;

import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
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
import com.inputstick.apps.kp2aplugin.InputStickService;
import com.inputstick.apps.kp2aplugin.R;
import com.inputstick.apps.kp2aplugin.SetupWizardActivity;

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
		int selectedLayout = Arrays.asList(layoutValues).indexOf(SlidesUtils.getLayout());	
		
		final EditText editTextTest = (EditText)view.findViewById(R.id.editTextTest);		
		
		final Spinner spinnerLayout = (Spinner)view.findViewById(R.id.spinnerLayout);	
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this.getContext(), android.R.layout.simple_spinner_item, KeyboardLayout.getLayoutNames(true));
		spinnerLayout.setAdapter(adapter);
		spinnerLayout.setSelection(selectedLayout, false);		
		spinnerLayout.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SlidesUtils.setLayout(layoutValues[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
			
		});
		
		Button buttonTest = (Button)view.findViewById(R.id.buttonTest);	
		buttonTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				Bundle b = new Bundle();
				b.putString(Const.EXTRA_ACTION, Const.ACTION_TYPE);		
				b.putString(Const.EXTRA_TEXT, editTextTest.getText().toString());
				b.putString(Const.EXTRA_LAYOUT, layoutValues[spinnerLayout.getSelectedItemPosition()]);
				Intent serviceIntent = new Intent(getActivity(), InputStickService.class);
				serviceIntent.setAction(Const.SERVICE_EXEC);
				serviceIntent.putExtras(b);
				getActivity().startService(serviceIntent); 	
				SetupWizardActivity.shouldDisconnect();
			}			
		});		
		
		return view;
	}



}