package com.inputstick.apps.kp2aplugin.slides;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.inputstick.apps.kp2aplugin.R;

public class AlertWindowSlide extends Fragment {

    private Button buttonRequestPermission;

    public AlertWindowSlide() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getView();
        final View view = inflater.inflate(R.layout.slide_alert_window, container, false);

        final Activity activity = requireActivity();

        buttonRequestPermission = view.findViewById(R.id.buttonRequestPermission);
        buttonRequestPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                startActivity(permissionIntent);
            }
        });

        return view;
    }

    @Override
    public void  onResume() {
        super.onResume();
        checkPermission(requireActivity());
    }

    private void checkPermission(Context context) {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            hasPermission = Settings.canDrawOverlays(context);
        }

        if (hasPermission) {
            buttonRequestPermission.setText(R.string.slide_button_done);
            buttonRequestPermission.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
        } else {
            buttonRequestPermission.setText(R.string.slide_button_request_permission);
            buttonRequestPermission.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_permission, 0);
        }
    }

}