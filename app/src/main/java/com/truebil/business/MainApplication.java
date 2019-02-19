package com.truebil.business;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;

import io.fabric.sdk.android.Fabric;

public class MainApplication extends Application {

    public static final String TAG = "MainApplication";

    public void onCreate() {
        super.onCreate();

        // Initialize Crashlytics
        Fabric.with(this, new Crashlytics());

        // Initialise InstaCart Time
        if (Helper.isNetworkConnected(getApplicationContext())) {
            if (!TrueTime.isInitialized()) {
                TrueTimeAsyncTask trueTime = new TrueTimeAsyncTask(getApplicationContext());
                trueTime.execute();
            }
        }

        createNotificationChannel();
    }

    /*
     * AsyncTask Class for initialising TrueTime
     * Source: https://en.proft.me/2017/10/17/how-synchronize-clock-ntp-android/
     */
    public class TrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context context;

        private TrueTimeAsyncTask (Context context){
            this.context = context;
        }

        protected Void doInBackground(Void... params) {
            try {
                TrueTime.build()
                        .withSharedPreferencesCache(context)
                        .withNtpHost("time.google.com")
                        .withLoggingEnabled(false)
                        .withConnectionTimeout(31_428)
                        .initialize();
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Exception when trying to get TrueTime", e);
            }
            return null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getString(R.string.auction_channel_ID), getString(R.string.auction_channel_name), importance);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
