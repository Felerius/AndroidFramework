package de.hpi.hci.bachelorproject2016.speechlib;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Julius on 01.02.2017.
 */

public class SingleSpeechRecognitionHandler extends SpeechRecognitionHandler {
    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;
    public SingleSpeechRecognitionHandler(Context context, OnSpeechRecognizedListener listener) {
        super(context, listener);
        initializeSpeechRecognition();
    }

    public void startSingleSpeechRecognition(){
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    private void initializeSpeechRecognition(){
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d(TAG, "onResults"); //$NON-NLS-1$
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                for (String result : data){
                    if(listener!=null)listener.onSpeechRecognized(result);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.getPackageName());
    }
}
