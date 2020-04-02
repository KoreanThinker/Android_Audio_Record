package com.koreanthinker.audiorecording;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    public Intent foregroundServiceIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonStartService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });
        findViewById(R.id.buttonStopService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });
        findViewById(R.id.startRecordBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });
        findViewById(R.id.stopRecordBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });
        findViewById(R.id.saveRecordBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecording();
            }
        });

    }
    public void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        }
    }



    public void startService() {
        Intent serviceIntent = new Intent(this, ForeGroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForeGroundService.class);
        stopService(serviceIntent);
    }

    public void startRecording() {
        ForeGroundService.RM.onRecord();
    }
    public void stopRecording() {
        ForeGroundService.RM.onStop();
    }

    public void saveRecording() {
        ForeGroundService.RM.onSave();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != foregroundServiceIntent) {
            stopService(foregroundServiceIntent);
            Log.d(TAG, "DESTORY");
            foregroundServiceIntent = null;
        }
    }

}
