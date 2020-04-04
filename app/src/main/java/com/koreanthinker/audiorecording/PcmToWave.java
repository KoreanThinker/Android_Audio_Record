package com.koreanthinker.audiorecording;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PcmToWave {
    private static final String TAG = "MainActivity";

    PcmToWave(final File rawFile1, final File rawFile2, final File waveFile, int time, int RECORDER_SAMPLERATE, int buffer) throws IOException {
        float unit = 20;
        Log.d(TAG, rawFile1.getName());
        Log.d(TAG, rawFile2.getName());
        int size1 = (int) rawFile1.length();
        int size2 = (int) rawFile2.length();
        Log.d(TAG, "" + size1);
        Log.d(TAG, "" + size2);
        int totalSize = size1 + size2 > time * buffer * unit ? (int) (time * buffer * unit) : size1 + size2;
        Log.d(TAG, "" + totalSize);
        byte[] rawData = new byte[totalSize];
        DataInputStream input = null;
//        try {
//            input = new DataInputStream(new FileInputStream(rawFile1));
//            input.read(rawData);
//        } finally {
//            if (input != null) {
//                input.close();
//            }
//        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 2); // number of channels
            writeInt(output, RECORDER_SAMPLERATE); // sample rate
            writeInt(output, RECORDER_SAMPLERATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile1, rawFile2, size1, size2, totalSize));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private byte[] fullyReadFileToBytes(File f1, File f2, int size1, int size2, int totalSize) throws IOException {
        byte bytes1[] = new byte[size1];
        byte bytes2[] = new byte[size2];
        FileInputStream fis1 = new FileInputStream(f1);
        FileInputStream fis2 = new FileInputStream(f2);
        try {
            fis1.read(bytes1, 0, size1);
            fis2.read(bytes2, 0, size2);
        } catch (IOException e) {
            throw e;
        } finally {
            fis1.close();
            fis2.close();
        }
        byte bytes[] = new byte[totalSize];
        System.arraycopy(bytes1, size1 + size2 - totalSize, bytes, 0, totalSize - size2);
        System.arraycopy(bytes2, 0, bytes, totalSize - size2, size2);

        return bytes;
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
}
