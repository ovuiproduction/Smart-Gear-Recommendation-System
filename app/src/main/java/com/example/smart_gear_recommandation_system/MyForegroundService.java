package com.example.smart_gear_recommandation_system;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class MyForegroundService extends Service {

    private static final String TAG = "MyForegroundService";
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "MyForegroundServiceChannel";
    private static final long FETCH_INTERVAL = 60000; // Fetch data every 1 minute

    public static final String STOP_FOREGROUND_ACTION = "stopForeground";

    private int rpm;
    private int gear;

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
        createNotificationChannel();
        // Start the foreground service
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null && STOP_FOREGROUND_ACTION.equals(intent.getAction())) {
            stopForeground(true); // Remove the service from the foreground state
            stopSelf(); // Stop the service
        }
        mHandler.postDelayed(mRunnable, FETCH_INTERVAL);
        return START_STICKY;
    }

    private void sendVariableToMainActivity() {
        Intent intent = new Intent("custom-event-name");
        Log.d(TAG, "rpm: "+rpm);
        Log.d(TAG, "gear: "+gear);
        intent.putExtra("rpm", rpm);
        intent.putExtra("Gear", gear);
        Log.d(TAG, "sendVariableToMainActivity: Data sent to Main-activity");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager == null) {
                return; // Unable to create notification channel
            }
            CharSequence name = "My Foreground Service Channel";
            String description = "Channel for My Foreground Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Foreground Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .build();
    }
    public interface DataCallback {
        void onDataReceived(int data);
    }

    private void fetchDataFromAPI(final DataCallback callback) {
        String uri = "https://api.thingspeak.com/channels/2473881/feeds.json?api_key=W9WVHO2HLG3529ND&results=1";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("feeds");
                    JSONObject jsonObjectFeeds = jsonArray.getJSONObject(0);
                    String field1 = jsonObjectFeeds.getString("field1");
                    int output = Integer.parseInt(field1);
                    callback.onDataReceived(output); // Pass the fetched data back via callback
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onDataReceived(-1); // Notify callback about error condition
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                callback.onDataReceived(-1); // Notify callback about error condition
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


    @SuppressLint("NotificationPermission")
    private void fetchDataAndCheckThreshold() {
        Log.d(TAG, "Fetching data from API and checking threshold...");
        fetchDataFromAPI(new DataCallback() {
            @Override
            public void onDataReceived(int data) {
                if (data != -1) {
                    // Data fetched successfully, use it here
                    Log.d(TAG, "fetched value : "+data);
                    rpm = data;
                    if(rpm>300 && rpm<700){
                        gear=2;
                    }else if(rpm>=700 && rpm<1500){
                        gear=3;
                    }else if(rpm>=1500){
                        gear=4;
                    }else{
                        gear=1;
                    }
                    Log.d(TAG, "onDataReceived: rpm"+rpm);
                    Log.d(TAG, "onDataReceived: gear"+gear);
                    sendVariableToMainActivity();

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager == null) {
                        return; // Unable to show notification
                    }

                    // Create a notification channel for Android Oreo and above
                    createNotificationChannel();

                    // Create the notification content
                    String notificationContent = "Fetched value : " + data;
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MyForegroundService.this, CHANNEL_ID);
                    builder.setSmallIcon(R.drawable.icon);
                    builder.setContentTitle("Threshold Exceeded");
                    builder.setContentText(notificationContent);
                    builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    // Show the notification
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                    // Do whatever you want with the fetched data
                } else {
                    // Handle error condition
                    Log.e(TAG, "Error fetching data from API");
                    // Display an error message or handle the error condition appropriately
                }
            }
        });
    }
}

