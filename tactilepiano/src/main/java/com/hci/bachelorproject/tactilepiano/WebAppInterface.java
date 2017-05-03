package com.hci.bachelorproject.tactilepiano;

import android.content.Context;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import de.hpi.hci.bachelorproject2016.bluetoothlib.SVGTransmitter;

/**
 * Created by Julius on 01.03.2017.
 */

public class WebAppInterface {
    Context mContext;
    SVGTransmitter svgTransmitter;
    TextToSpeech tts;
    private SoundPool soundPool;
    private int sound_c,sound_d, sound_e, sound_f, sound_g, sound_a, sound_b, sound_c5;
    private int lastToneVal=0;

    private long lastTimeStamp;

    WebView webView;
    /** Instantiate the interface and set the context */
    WebAppInterface(Context c, WebView webView) {
        mContext = c;
        svgTransmitter = new SVGTransmitter(c, webView);
        this.webView = webView;
        this.tts = new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });

        lastTimeStamp = System.currentTimeMillis();
        soundPool = new SoundPool.Builder().setMaxStreams(5).build();
        sound_c = soundPool.load(mContext,R.raw.c_4,1);
        sound_d = soundPool.load(mContext,R.raw.d_4,1);
        sound_e = soundPool.load(mContext,R.raw.e_4,1);
        sound_f = soundPool.load(mContext,R.raw.f_4,1);
        sound_g = soundPool.load(mContext,R.raw.g_4,1);
        sound_a = soundPool.load(mContext,R.raw.a_4,1);
        sound_b = soundPool.load(mContext,R.raw.b_4,1);
        sound_c5= soundPool.load(mContext,R.raw.c_5,1);
    }

    //framework method
    @JavascriptInterface
    public void sendSVGToLaserPlotter(String svgString, int versionNr){
        Log.i("WebAppInterface", "Sending to plotter " + svgString);
        Toast.makeText(mContext, "Printing SVG", Toast.LENGTH_SHORT).show();
        svgTransmitter.sendToLaserPlotter(versionNr+"");
        svgTransmitter.sendToLaserPlotter(svgString);
    }

    //framework method
    @JavascriptInterface
    public void speak(String text){
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,text);
    }


    @JavascriptInterface
    public void playTone(String tone){
        Log.i("Piano", "received tone " + tone);
        int toneVal =-1;
        switch(tone){
            case "c": toneVal = sound_c; break;
            case "d": toneVal = sound_d; break;
            case "e": toneVal = sound_e; break;
            case "f": toneVal = sound_f; break;
            case "g": toneVal = sound_g; break;
            case "a": toneVal = sound_a; break;
            case "b": toneVal = sound_b; break;
            case "c5": toneVal = sound_c5; break;
        }

        Log.i("Piano",toneVal+"");
        long currentTimeStamp = System.currentTimeMillis();
        //if (toneVal!=lastToneVal || currentTimeStamp - lastTimeStamp>150) {
            soundPool.play(toneVal, 1, 1, 0, 0, 1);
        //}
        lastToneVal = toneVal;
        lastTimeStamp = System.currentTimeMillis();
    }


}