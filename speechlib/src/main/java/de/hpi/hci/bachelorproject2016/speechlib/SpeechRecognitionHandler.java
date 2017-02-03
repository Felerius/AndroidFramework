package de.hpi.hci.bachelorproject2016.speechlib;

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

    public interface OnSpeechRecognizedListener{
        void onSpeechRecognized(String[] messages);
    }
    public static final String RECEIVED_SPEECH = "received_speech";
    protected OnSpeechRecognizedListener listener;
    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(RECEIVED_SPEECH)|| action.equals(context.getPackageName()+"."+RECEIVED_SPEECH)){
                String[] messages=intent.getStringArrayExtra("message");
                if(listener!=null)listener.onSpeechRecognized(messages);
            }
        }
    };
    public SpeechRecognitionHandler(Context context, OnSpeechRecognizedListener listener){
        this.context = context;
        this.listener=listener;
        this.broadcastReceiver = broadcastReceiver;
        initializeBroadcastReceiver();
    }
    public SpeechRecognitionHandler(Context context){
        this.context = context;

        initializeBroadcastReceiver();
    }


    public void startContinuousSpeechRecognition(){
        context.startService(new Intent(context, ContinuousVoiceRecogService.class));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver,
                new IntentFilter(SpeechRecognitionHandler.RECEIVED_SPEECH));

    }

    public void stopContinuousSpeechRecognition(){
        context.stopService(new Intent(context, ContinuousVoiceRecogService.class));
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }




    public void initializeBroadcastReceiver(){
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver,
                new IntentFilter(RECEIVED_SPEECH));



    }

    public void setListener(OnSpeechRecognizedListener listener) {
        this.listener = listener;
    }
}
