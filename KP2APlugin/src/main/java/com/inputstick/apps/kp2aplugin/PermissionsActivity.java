package com.inputstick.apps.kp2aplugin;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import keepass2android.pluginsdk.AccessManager;
import keepass2android.pluginsdk.Strings;

public class PermissionsActivity extends AppCompatActivity {

    private NotificationManager notificationManager;

    private TextView textViewPermissionsKP2APluginStatus;
    private TextView textViewPermissionsNotificationsStatus;
    private TextView textViewPermissionsAlertWindowStatus;

    private Button buttonPermissionsKP2APlugin;
    private Button buttonPermissionsNotifications;
    private Button buttonPermissionsAlertWindow;

    private int defaultTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        CardView cardView;
        cardView = findViewById(R.id.cardViewPermissionsNotifications);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            cardView.setVisibility(View.GONE);
        }

        cardView = findViewById(R.id.cardViewPermissionsAlertWindow);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            cardView.setVisibility(View.GONE);
        }

        textViewPermissionsKP2APluginStatus = findViewById(R.id.textViewPermissionsKP2APluginStatus);
        textViewPermissionsNotificationsStatus = findViewById(R.id.textViewPermissionsNotificationsStatus);
        textViewPermissionsAlertWindowStatus = findViewById(R.id.textViewPermissionsAlertWindowStatus);

        defaultTextColor = textViewPermissionsAlertWindowStatus.getCurrentTextColor();

        buttonPermissionsKP2APlugin = findViewById(R.id.buttonPermissionsKP2APlugin);
        buttonPermissionsKP2APlugin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Strings.ACTION_EDIT_PLUGIN_SETTINGS);
                    i.putExtra(Strings.EXTRA_PLUGIN_PACKAGE, PermissionsActivity.this.getPackageName());
                    startActivityForResult(i, Const.REQUEST_CODE_ENABLE_PLUGIN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonPermissionsNotifications = findViewById(R.id.buttonPermissionsNotifications);
        buttonPermissionsNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(PermissionsActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, Const.REQUEST_CODE_NOTIFICATIONS_PERMISSION);
                }
            }
        });

        buttonPermissionsAlertWindow = findViewById(R.id.buttonPermissionsAlertWindow);
        buttonPermissionsAlertWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivity(permissionIntent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AccessManager.getAllHostPackages(this).isEmpty()) {
            textViewPermissionsKP2APluginStatus.setText(R.string.status_disabled);
            textViewPermissionsKP2APluginStatus.setTextColor(Color.RED);
            buttonPermissionsKP2APlugin.setText(R.string.enable);
        } else {
            textViewPermissionsKP2APluginStatus.setText(R.string.status_enabled);
            textViewPermissionsKP2APluginStatus.setTextColor(defaultTextColor);
            buttonPermissionsKP2APlugin.setText(R.string.disable);
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (notificationManager.areNotificationsEnabled()) {
                textViewPermissionsNotificationsStatus.setText(R.string.status_allowed);
                textViewPermissionsNotificationsStatus.setTextColor(defaultTextColor);
                buttonPermissionsNotifications.setVisibility(View.GONE);
            } else {
                textViewPermissionsNotificationsStatus.setText(R.string.status_disabled);
                textViewPermissionsNotificationsStatus.setTextColor(Color.RED);
                buttonPermissionsNotifications.setVisibility(View.VISIBLE);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (Settings.canDrawOverlays(this)) {
                textViewPermissionsAlertWindowStatus.setText(R.string.status_allowed);
                textViewPermissionsAlertWindowStatus.setTextColor(defaultTextColor);
                buttonPermissionsAlertWindow.setVisibility(View.GONE);
            } else {
                textViewPermissionsAlertWindowStatus.setText(R.string.status_disabled);
                textViewPermissionsAlertWindowStatus.setTextColor(Color.RED);
                buttonPermissionsAlertWindow.setVisibility(View.VISIBLE);
            }
        }
    }

}
