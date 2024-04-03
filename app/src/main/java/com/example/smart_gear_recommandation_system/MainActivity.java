package com.example.smart_gear_recommandation_system;

import static androidx.core.app.ServiceCompat.stopForeground;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {

    public TextView rpmValueText;
    public TextView recommendedGearText;
    private MyForegroundService mService;
    private boolean mBound = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Main-activity", "onReceive: receive data on main activity");
            int rpmValue = intent.getIntExtra("rpm", 0);
            int recommendedGear = intent.getIntExtra("Gear", 0);
            Log.d("Main-activity", "onReceive: rpm : "+rpmValue);
            String rpm = String.valueOf(rpmValue);
            String recommendedGearStr = String.valueOf(recommendedGear);
            Toast.makeText(MainActivity.this, "Your Bike Running On RPM : " + rpmValue+"\nRecommended Gear : "+recommendedGear, Toast.LENGTH_SHORT).show();
            rpmValueText.setText(rpm);
            recommendedGearText.setText(recommendedGearStr);
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("onServiceConnected", "onServiceConnected: service connecting");
            MyForegroundService.LocalBinder binder = (MyForegroundService.LocalBinder) service;
            mService = binder.getService();
            Log.d("MainActivity", "onServiceConnected : "+mService);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("custom-event-name"));

        Button startServiceButton = findViewById(R.id.startServiceButton);
        Button stopServiceButton = findViewById(R.id.stopServiceButton);
        rpmValueText = findViewById(R.id.rpmValueText);
        recommendedGearText = findViewById(R.id.recommendedGearText);

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click Start","Service started");
                Intent intent = new Intent(MainActivity.this, MyForegroundService.class);
                startForegroundService(intent);
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                Toast.makeText(MainActivity.this, "Service started", Toast.LENGTH_SHORT).show();
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click stop","Service stopped");
                //stopService(new Intent(MainActivity.this, MyForegroundService.class));
                Intent stopIntent = new Intent(MainActivity.this, MyForegroundService.class);
                stopIntent.setAction(MyForegroundService.STOP_FOREGROUND_ACTION);
                startService(stopIntent);
                Toast.makeText(MainActivity.this, "Service stopped", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "onStart: Bind Service");
        Intent intent = new Intent(MainActivity.this, MyForegroundService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from MyForegroundService
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }
    @Override
    protected void onDestroy() {
        // Unregister the receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }
}
