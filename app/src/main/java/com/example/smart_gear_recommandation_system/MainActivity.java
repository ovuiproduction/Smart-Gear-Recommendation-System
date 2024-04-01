package com.example.smart_gear_recommandation_system;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public TextView rpmValueText;
    private MyForegroundService mService;
    private boolean mBound = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyForegroundService.LocalBinder binder = (MyForegroundService.LocalBinder) service;
            mService = binder.getService();
            Log.d("MainActivity", "onServiceConnected : "+mService);
            // Set TextView reference to the service
            if (mService != null) {
                Log.d("MainActivity", "onServiceConnected:  Set TextView reference to the service");
                mService.setTextViewValue(rpmValueText);
            }
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

        Button startServiceButton = findViewById(R.id.startServiceButton);
        Button stopServiceButton = findViewById(R.id.stopServiceButton);
        rpmValueText = findViewById(R.id.rpmValueText);

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startForegroundService(new Intent(MainActivity.this, MyForegroundService.class));
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, MyForegroundService.class));
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "onStart: Bind Service");
        // Bind to MyForegroundService
        Intent intent = new Intent(this, MyForegroundService.class);
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
}
