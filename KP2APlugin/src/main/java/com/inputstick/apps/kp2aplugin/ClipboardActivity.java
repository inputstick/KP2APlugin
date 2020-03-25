package com.inputstick.apps.kp2aplugin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.hid.HIDKeycodes;

public class ClipboardActivity extends Activity {

    private TextView textViewCipboardContent;
    private Button buttonClipboardType;
    private Button buttonClipboardTypeEnter;

    private Handler handler = new Handler();


    private final BroadcastReceiver clipboardRemainingTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(Const.BROADCAST_CLIPBOARD_REMAINING_TIME)) {
                    int remainingTime = intent.getIntExtra(Const.EXTRA_CLIPBOARD_REMAINING_TIME, 0);
                    if (remainingTime == 0) {
                        finish();
                    } else {
                        String title = getString(R.string.title_activity_clipboard) + " (" + (remainingTime/1000) + "s)";
                        setTitle(title);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipboard);

        Intent intent = getIntent();
        final TypingParams params = new TypingParams(intent);

        textViewCipboardContent = findViewById(R.id.textViewCipboardContent);

        Button buttonClipboardRefresh = findViewById(R.id.buttonClipboardRefresh);
        buttonClipboardRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        buttonClipboardType = findViewById(R.id.buttonClipboardType);
        buttonClipboardType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type(params, false);

            }
        });

        buttonClipboardTypeEnter = findViewById(R.id.buttonClipboardTypeEnter);
        buttonClipboardTypeEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type(params, true);
            }
        });

        IntentFilter filter;
        filter = new IntentFilter();
        filter.addAction(Const.BROADCAST_CLIPBOARD_REMAINING_TIME);
        registerReceiver(clipboardRemainingTimeReceiver, filter);

        textViewCipboardContent.setText(R.string.clipboard_please_wait);
        buttonClipboardType.setEnabled(false);
        buttonClipboardTypeEnter.setEnabled(false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(clipboardRemainingTimeReceiver);
        super.onDestroy();
    }

    private void type(TypingParams params, boolean enter) {
        String toType = textViewCipboardContent.getText().toString();
        new ItemToExecute(toType, params).sendToService(ClipboardActivity.this, true);
        if (enter) {
            new ItemToExecute((byte)0, HIDKeycodes.KEY_ENTER, params).sendToService(ClipboardActivity.this, false);
        }

        if (InputStickHID.isReady()) {
            //stop clipboard monitoring in service:
            Intent stopIntent = new Intent(ClipboardActivity.this, InputStickService.class);
            stopIntent.setAction(Const.ACTION_CLIPBOARD_STOP);
            startService(stopIntent);

            finish();
        }
    }

    private void refresh() {
        handler.removeCallbacksAndMessages(null);

        String text = null;
        final ClipboardManager clipboardManager = (ClipboardManager)getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        final ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData != null) {
            final ClipDescription desc = clipData.getDescription();
            boolean hasText;

            if (Build.VERSION.SDK_INT >= 16) {
                hasText = (desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) || (desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML));
            } else {
                hasText = desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
            }

            if (hasText) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null) {
                    CharSequence cs = item.getText();
                    if (cs != null) {
                        text = cs.toString();
                    }
                }
            }
        }

        if (text != null) {
            textViewCipboardContent.setText(text);
            buttonClipboardType.setEnabled(true);
            buttonClipboardTypeEnter.setEnabled(true);
        } else {
            textViewCipboardContent.setText(R.string.clipboard_empty);
            buttonClipboardType.setEnabled(false);
            buttonClipboardTypeEnter.setEnabled(false);
        }
    }

}
