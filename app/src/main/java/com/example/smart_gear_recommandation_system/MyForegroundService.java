package com.example.smart_gear_recommandation_system;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Random;

public class MyForegroundService extends Service {

    private static final String TAG = "MyForegroundService";
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "MyForegroundServiceChannel";
    private static final long FETCH_INTERVAL = 60000; // Fetch data every 1 minute

    Handler mHandler = new Handler(Looper.getMainLooper()); // Associates with the main (UI) thread's Looper
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // Perform your task here, e.g., fetch data from API
            fetchDataAndCheckThreshold();

            // Schedule the next execution
            mHandler.postDelayed(this, FETCH_INTERVAL);
        }
    };

    private TextView rpmValueText;

    public class LocalBinder extends Binder {
        MyForegroundService getService() {
            Log.d(TAG, "getService: called getService");
            return MyForegroundService.this;
        }
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // Create a notification channel for Android Oreo and above
        createNotificationChannel();

        // Start the foreground service
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        mHandler.postDelayed(mRunnable, FETCH_INTERVAL); // Start the periodic task
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mHandler.removeCallbacks(mRunnable); // Stop the periodic task
    }

    @SuppressLint("ObsoleteSdkInt")
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Foreground Service Channel";
            String description = "Channel for My Foreground Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Foreground Service")
                .setContentText("Running...")
//                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .build();
    }
    private int fetchDataFromAPI() {
        // Simulate fetching data from API (replace this with your actual API call)
        // For demonstration, returning a random value between 0 and 100
        return new Random().nextInt(101);
    }
    private void fetchDataAndCheckThreshold() {
        // Replace this with your logic to fetch data from API and check threshold
        Log.d(TAG, "Fetching data from API and checking threshold...");
        // Fetch data from API (replace this with your actual API call)
        final int fetchedValue = fetchDataFromAPI();
        Log.d(TAG, "fetched value : "+fetchedValue);
        // Update UI on the main thread using Handler
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Update UI with fetched value
                updateUI(fetchedValue);
            }
        });
    }
    private void updateUI(int value) {
        // Update UI with fetched value
        // For demonstration, updating a TextView named "textViewValue"
        if (rpmValueText != null) {
            Log.d(TAG, "updateUI: called update ui");
            rpmValueText.setText(String.valueOf(value));
        }
    }

    public void setTextViewValue(TextView rpmValueText) {
        this.rpmValueText = rpmValueText;
    }

}

