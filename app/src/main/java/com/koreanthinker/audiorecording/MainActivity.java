package com.koreanthinker.audiorecording;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    public AudioRecord mAudioRecord = null;

    public Thread mRecordThread = null;
    public boolean isRecording = false;

    public AudioTrack mAudioTrack = null;
    public Thread mPlayThread = null;
    public boolean isPlaying = false;

    public Button mBtRecord = null;
    public Button mBtPlay = null;

    public String mFilePath = null;

    public Intent foregroundServiceIntent = null;
    private Button btnStartService, btnStopService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartService = findViewById(R.id.buttonStartService);
        btnStopService = findViewById(R.id.buttonStopService);
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });



        permissionCheck();
        mBtRecord = (Button)findViewById(R.id.startRecord);
        mBtPlay = (Button)findViewById(R.id.playBtn);

        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecord.startRecording();

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);

        mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                byte[] readData = new byte[mBufferSize];
                mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/record3.pcm";
                FileOutputStream fos = null;

                try {
                    Log.d(TAG, "FOS1");
                    fos = new FileOutputStream(mFilePath);

                } catch(FileNotFoundException e) {
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }

                while(isRecording) {
                    int ret = mAudioRecord.read(readData, 0, mBufferSize);
                    Log.d(TAG, "read bytes is " + ret);
                    Log.d(TAG, ""+ readData);

                    try {
                        fos.write(readData, 0, mBufferSize);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
                long endTime =  System.currentTimeMillis();

                Log.d(TAG, "" + (endTime- startTime));

                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mPlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] writeData = new byte[mBufferSize];
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mFilePath);
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                DataInputStream dis = new DataInputStream(fis);
                mAudioTrack.play();

                while(isPlaying) {
                    try {
                        int ret = dis.read(writeData, 0, mBufferSize);
                        if (ret <= 0) {
                            (MainActivity.this).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isPlaying = false;
                                    mBtPlay.setText("Play");
                                }
                            });

                            break;
                        }
                        mAudioTrack.write(writeData, 0, ret);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;

                try {
                    dis.close();
                    fis.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    public void onRecord(View view) {
        if(isRecording == true) {
            isRecording = false;
            mBtRecord.setText("Record");
        }
        else {
            isRecording = true;
            mBtRecord.setText("Stop");

            if(mAudioRecord == null) {
                mAudioRecord =  new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
                mAudioRecord.startRecording();
            }
            Log.d(TAG, mRecordThread == null ? "널" : "정상");
            mRecordThread.start();
        }

    }

    public void onPlay(View view) {
        if(isPlaying == true) {
            isPlaying = false;
            mBtPlay.setText("Play");
        }
        else {
            isPlaying = true;
            mBtPlay.setText("Stop");

            if(mAudioTrack == null) {
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);
            }
            mPlayThread.start();
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
