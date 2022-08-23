package com.oubiti.saltdroid;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.Message;

import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.widget.Toast;

/**
 * this is an example of a service that prompts itself to a foreground service with a persistent
 * notification.  Which is now required by Oreo otherwise, a background service without an app will be killed.
 */

public class SaltDroidService extends Service {


    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private final static String TAG = "SaltDroidService";

    public SaltDroidService() {
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            //promote to foreground and create persistent notification.
            //in Oreo we only have a few seconds to do this or the service is killed.
            Notification notification = getNotification("SaltDroid is running");
            startForeground(msg.arg1, notification);  //the ID as same as the notification id.  can't be zero.

            Log.d(TAG, "should be foreground now. id number is " + msg.arg1);
            // Normally we would do some work here, like download a file.
            // For our example, we just sleep for 5 seconds then display toasts.
            //setup how many messages
            String salt_master = "";

            Bundle configuration = msg.getData();
            if (configuration != null) {
                salt_master = configuration.getString("salt_master", "0.0.0.0");  //default is one
            }
            synchronized (this) {
                try {
                    String config_message = "Connecting salt-minion to " + salt_master;
                    Log.d("intentServer", config_message);
                    toast(config_message);
                    wait(5000);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.toString());
                }
            }
            String message = "Salt-Minion connected";
            Log.d("intentServer", message);
            toast(message);
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);  //notification will go away as well.
        }
    }

    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SaltDroidService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;//needed for stop.

        if (intent != null) {
            msg.setData(intent.getExtras());
            mServiceHandler.sendMessage(msg);
        } else {
            Toast.makeText(SaltDroidService.this, "The Intent to start is null?!", Toast.LENGTH_SHORT).show();
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    // build a persistent notification and return it.
    public Notification getNotification(String message) {

        return new NotificationCompat.Builder(getApplicationContext(), MainActivity.channel_id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)  //persistent notification!
                .setChannelId(MainActivity.channel_id)
                .setContentTitle("Service")   //Title message top row.
                .setContentText(message)  //message when looking at the notification, second row
                .build();  //finally build and return a Notification.
    }
}
