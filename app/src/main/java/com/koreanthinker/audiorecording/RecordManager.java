package com.koreanthinker.audiorecording;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordManager {

    private static final String TAG = "MainActivity";
    private static final long MAX_TIME = 1800;
    private final String FILE_PATH_1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/24hourRecordTemp1.pcm";
    private final String FILE_PATH_2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/24hourRecordTemp2.pcm";
    private final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/24hourRecordSave";

    private Context context = null;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private AudioRecord mAudioRecord = null;
    private Thread mRecordThread = null;
    private boolean isRecording = false;
    private String currentPath = null;

    RecordManager(Context context) {
        this.context = context;

        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecord.startRecording();

        mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                byte[] readData = new byte[mBufferSize];
                FileOutputStream fos = null;
                //fos 선언
                try {
                    currentPath = FILE_PATH_1;
                    fos = new FileOutputStream(FILE_PATH_1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while (isRecording) {
                    //최대 시간 초과시 fos 스위치
                    long currentTime = System.currentTimeMillis() - startTime;
                    if (currentTime > MAX_TIME) {
                        //fos 삭제하기 위해 닫기
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, e.getMessage());
                        }

                        //주소 변경
                        if (currentPath == FILE_PATH_1) {
                            currentPath = FILE_PATH_2;
                        } else {
                            currentPath = FILE_PATH_1;
                        }

                        //앞으로 사용할 주소 초기화
                        File file = new File(currentPath);
                        if (file.exists()) {
                            if (!file.delete()) {
                                ErrorAndStop();
                            }
                        }

                        // 새로운 주소로 fos 등록
                        try {
                            fos = new FileOutputStream(currentPath);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.d(TAG, e.getMessage());
                        }

                        //시간 초기화
                        startTime = System.currentTimeMillis();
                    }
                    // 소리 읽고 pcm에 쓰기
                    int ret = mAudioRecord.read(readData, 0, mBufferSize);
                    Log.d(TAG, currentPath);
                    try {
                        fos.write(readData, 0, mBufferSize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        ErrorAndStop();
                    }

                }

                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;

                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void onRecord() {
        if (isRecording) return;
        isRecording = true;
        if (mAudioRecord == null) {
            mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        }
        mAudioRecord.startRecording();
        mRecordThread.start();
    }

    public void onStop() {
        isRecording = false;
        mAudioRecord.stop();
    }

    public void onSave() {

    }

    public void ErrorAndStop() {
        Toast.makeText(context, "Recording error try again", Toast.LENGTH_SHORT).show();
        onStop();
    }
}