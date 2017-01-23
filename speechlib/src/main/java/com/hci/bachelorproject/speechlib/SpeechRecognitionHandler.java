package com.hci.bachelorproject.speechlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Julius on 20.01.2017.
 */

public class SpeechRecognitionHandler {
    Context context;
    public static final String RECEIVED_SPEECH = "received_speech";
    private BroadcastReceiver broadcastReceiver;
    public SpeechRecognitionHandler(Context context, BroadcastReceiver broadcastReceiver){
        this.context = context;
        this.broadcastReceiver = broadcastReceiver;
        initializeBroadcastReceiver();
    }



    public void startSpeechRecognition(){
        context.startService(new Intent(context, VoiceRecogService.class));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver,
                new IntentFilter(SpeechRecognitionHandler.RECEIVED_SPEECH));

    }

    public void stopSpeechRecognition(){
        context.stopService(new Intent(context, VoiceRecogService.class));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }




    public void initializeBroadcastReceiver(){
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver,
                new IntentFilter(RECEIVED_SPEECH));

    }
}
