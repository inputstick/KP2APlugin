package com.inputstick.apps.kp2aplugin.slides;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.inputstick.apps.kp2aplugin.Const;
import com.inputstick.apps.kp2aplugin.R;

public class NotificationsSlide extends Fragment {

    private Button buttonRequestPermission;
    private NotificationManager notificationManager;

    public NotificationsSlide() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getView();
        final View view = inflater.inflate(R.layout.slide_notifications, container, false);

        final Activity activity = requireActivity();

        notificationManager = (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);

        buttonRequestPermission = view.findViewById(R.id.buttonRequestPermission);
        buttonRequestPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.POST_NOTIFICATIONS}, Const.REQUEST_CODE_NOTIFICATIONS_PERMISSION);
            }
        });

        return view;
    }

    @Override
    public void  onResume() {
        super.onResume();
        checkPermission();
    }

    private void checkPermission() {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission = notificationManager.areNotificationsEnabled();
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