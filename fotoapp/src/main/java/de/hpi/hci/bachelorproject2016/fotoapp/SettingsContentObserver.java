package de.hpi.hci.bachelorproject2016.fotoapp;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnector;

import static android.content.ContentValues.TAG;

/**
 * Created by Julius on 31.01.2017.
 */
public class SettingsContentObserver extends ContentObserver {
    private AudioManager audioManager;
    private Handler handler;
    public SettingsContentObserver(Context context, Handler handler) {
        super(handler);
        this.handler = handler;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "Volume now " + currentVolume);
        handler.sendMessage(handler.obtainMessage());

    }





}
