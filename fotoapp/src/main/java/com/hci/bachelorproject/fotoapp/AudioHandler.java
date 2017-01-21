package com.hci.bachelorproject.fotoapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Julius on 20.01.2017.
 */

public class AudioHandler {
    Context context;
    public static final String RECEIVED_SPEECH = "received_speech";
    public TextToSpeech tts;
    public AudioHandler(Context context){
        this.context = context;
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS","successfully set up text to speech");
                    int result = tts.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
    }


    // handler for received Intents for the "received speech" event
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            reactOnMessage(message);
            Log.d("receiver", "Got message: " + message);


        }
    };


    public void startSpeechRecognition(){
        context.startService(new Intent(context, VoiceRecogService.class));
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter(AudioHandler.RECEIVED_SPEECH));

    }

    public void stopSpeechRecognition(){
        context.stopService(new Intent(context, VoiceRecogService.class));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
    }

    public void stopTextToSpeech(){
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    private void reactOnMessage(String message) {
        Log.d("Speech recog", "reactOnMessage()");
        //when using this class add a switch case for the speech commands here

    }

    public void initializeBroadcastReceiver(){
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter(RECEIVED_SPEECH));

    }
}
