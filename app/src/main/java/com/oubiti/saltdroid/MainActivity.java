package com.oubiti.saltdroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Main activity start the Saltdroid service and then finish.
 * In oreo to run a background service when the app is not running it must
 * startForegroundService(Intent)  in the activity
 * in service, make a notification low or higher. persistent.
 * and startForground (int id, Notification notification )
 */

public class MainActivity extends AppCompatActivity {
    public static String channel_id = "saltdroid_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createChannel();  //needed for the persistent notification created in service.


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), SaltDroidService.class);
                EditText mTextSaltMaster = findViewById(R.id.TextSaltMaster);
                intent.putExtra("salt_master", mTextSaltMaster.getText().toString());
                startForegroundService(intent);
                finish();
            }
        });
    }

    /**
     * for API 26+ create notification channels
     */
    private void createChannel() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(channel_id, getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_MIN);
        // Configure the notification channel.
        mChannel.setDescription(getString(R.string.channel_description));
        mChannel.setShowBadge(true);
        nm.createNotificationChannel(mChannel);
    }

}
