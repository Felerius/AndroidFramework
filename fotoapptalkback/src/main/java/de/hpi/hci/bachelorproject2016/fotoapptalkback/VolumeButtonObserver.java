package de.hpi.hci.bachelorproject2016.fotoapptalkback;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Julius on 31.01.2017.
 */
public class VolumeButtonObserver extends ContentObserver {
    private AudioManager audioManager;
    private Handler handler;
    int previousVolume;
    boolean isChangable = true;
    public VolumeButtonObserver(Context context, Handler handler) {
        super(handler);
        this.handler = handler;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {

        if (isChangable) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            Log.d(TAG, "Volume now " + currentVolume);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, AudioManager.FLAG_VIBRATE);
            handler.sendMessage(handler.obtainMessage());
        }

        isChangable = !isChangable;

    }





}
