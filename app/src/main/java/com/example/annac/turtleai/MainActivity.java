package com.example.annac.turtleai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Stream;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;
import omrecorder.WriteAction;
import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;
import omrecorder.WriteAction;

public class MainActivity extends AppCompatActivity {

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final short RECORDER_CHANNELSS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    int bufferSize;
    Button recordButton;
    Recorder rdd;
    @NonNull
    private File file() {
        //File file = new File("/sdcard/demo.wav");
       // file.delete();
        return new File(Environment.getExternalStorageDirectory(), "demo1.pcm");
    }

    private void requestRecordAudioPermission() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Activity", "Granted!");

                } else {
                    Log.d("Activity", "Denied!");
                    finish();
                }
                return;
            }
        }
    }
    private void setupRecorder() {
        rdd = OmRecorder.pcm(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                      //  animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file());
    }
    private void setupNoiseRecorder() {
        rdd = OmRecorder.pcm(
                new PullTransport.Noise(mic(),
                        new PullTransport.OnAudioChunkPulledListener() {
                            @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                               // animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                            }
                        },
                        new WriteAction.Default(),
                        new Recorder.OnSilenceListener() {
                            @Override public void onSilence(long silenceTime) {
                                Log.e("silenceTime", String.valueOf(silenceTime));
                            }
                        }, 200
                ), file()
        );
    }
    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestRecordAudioPermission();

        setupRecorder();

        setButtonHandlers();
        enableButtons(false);

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public short[] concat(short[] a, short[] b) {
        int length = a.length+b.length;
        short both [] = new short [length];
        for (int i=0; i<a.length; i++)
        {
            both[i] = a[i];
        }
        for (int i=a.length; i< b.length; i++)
        {
            both[i]= b[i - a.length];
        }
        return both;
    }

    private void setButtonHandlers() {
        ( findViewById(R.id.start)).setOnClickListener(btnClick);
        ( findViewById(R.id.stop)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        (findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.start, !isRecording);
        enableButton(R.id.stop, isRecording);
    }

    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);
       // setupRecorder();
        rdd.startRecording();
        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] & 0x00FF >> 8);
            sData[i] = 0;
        }
        return bytes;



    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void writeAudioDataToFile() {
        // Write the output audio in byte

        String filePath = "/sdcard/rawData.pcm";
        short sData[] = new short[bufferSize];
        short[] wavData = new short[32000];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            int size = 0;
            size  = recorder.read(sData, 0, bufferSize);
            try {
                wavData = concat(wavData, sData);
                byte bData[] = short2byte(sData);
                os.write(bData, 0, size);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
            final short[] array = new short[wavData.length];
            for (int i=0; i<wavData.length; i++)
                array[i] = Short.reverseBytes(wavData[i]);
                     //   (short) wavData[i];
            Wave wave=new Wave(RECORDER_SAMPLERATE,RECORDER_CHANNELSS,array,0,wavData.length-1);
            if(wave.wroteToFile("recording1.wav"))
            {Log.d("ok", "wav file created"); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {
            rdd.stopRecording();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start: {
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.stop: {
                    enableButtons(false);
                    stopRecording();
                    break;
                }
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    //start TurtleAcivity
    public void showTurtle(View view)
    {
        Intent intent = new Intent(this, TurtleActivity.class);
        startActivity(intent);
    }
}
