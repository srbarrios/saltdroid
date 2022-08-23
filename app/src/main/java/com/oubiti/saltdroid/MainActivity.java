package com.oubiti.saltdroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

/**
 * Main activity doesn't really do much, but start the service and then finish.
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

        createchannel();  //needed for the persistent notification created in service.


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), SaltDroidService.class);
                startForegroundService(intent);
                finish();
            }
        });
    }

    /**
     * for API 26+ create notification channels
     */
    private void createchannel() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(channel_id, getString(R.string.channel_name),  //name of the channel
            NotificationManager.IMPORTANCE_LOW);   //importance level
        //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
        // Configure the notification channel.
        mChannel.setDescription(getString(R.string.channel_description));
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
        mChannel.setShowBadge(true);
        nm.createNotificationChannel(mChannel);
    }

}
