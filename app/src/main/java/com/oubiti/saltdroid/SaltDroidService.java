package com.oubiti.saltdroid;

import static com.oubiti.saltdroid.AssetsManager.copyAssetFolder;
import static com.oubiti.saltdroid.UnixManager.*;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;


/**
 * this is an example of a service that prompts itself to a foreground service with a persistent
 * notification.  Which is now required by Oreo otherwise, a background service without an app will be killed.
 */

public class SaltDroidService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    public final static String TAG = "SaltDroidService";

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
            startForeground(msg.arg1, notification);

            Log.d(TAG, "Saltdroid service in foreground. ID: " + msg.arg1);
            String salt_master = "";

            Bundle configuration = msg.getData();
            if (configuration != null) {
                salt_master = configuration.getString("salt_master", "0.0.0.0");
            }
            synchronized (this) {
                // FIRST TRY WITH A ZIP FILE, FAILED UNZIPPING SYMLINKS
                //InputStream inputStream = getAssets().open("venv-salt-minion.zip");
                //Path filepath = Paths.get(getFilesDir().getPath() + "/venv-salt-minion.zip");
                //Files.copy(inputStream, filepath, StandardCopyOption.REPLACE_EXISTING);
                //execCommand(new String[] {"/bin/sh", "-c", "unzip " + filepath, "-d venv-salt-minion"}, getFilesDir());

                // SECOND TRY COPYING ALL FILES FROM ASSETS FOLDER, FAILED RUNNING PROGRAMS
                // THE FILES IN ASSETS CONTAINS THE UNCOMPRESSED FILES FROM a venv-salt-minion.rpm
                copyAssetFolder(getAssets(), "usr", getFilesDir().getPath() );
                copyAssetFolder(getAssets(), "etc", getFilesDir().getPath() );
                toast(execCommandVerbose(
                        new String[] {"/bin/sh", "-c", "/bin/sh usr/lib/venv-salt-minion/bin/python.original --version"},
                        new String[]{"PATH=usr/bin:usr/sbin:usr/lib/venv-salt-minion:usr/lib/venv-salt-minion/bin"},
                        getFilesDir()));
            }
            String message = "Salt-Minion connected";
            Log.d(TAG, message);
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
                Log.d(TAG, String.valueOf(text));
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
        Toast.makeText(this, "SaltDroid service stopped", Toast.LENGTH_SHORT).show();
    }

    // build a persistent notification and return it.
    public Notification getNotification(String message) {

        return new NotificationCompat.Builder(getApplicationContext(), MainActivity.channel_id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)  //persistent notification!
                .setChannelId(MainActivity.channel_id)
                .setContentTitle("SaltDroid Service")   //Title message top row.
                .setContentText(message)  //message when looking at the notification, second row
                .build();  //finally build and return a Notification.
    }

}
