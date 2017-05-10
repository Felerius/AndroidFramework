package com.hci.bachelorproject.tactilepiano;

import android.content.Context;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import de.hpi.hci.bachelorproject2016.bluetoothlib.SVGTransmitter;

/**
 * Created by Julius on 01.03.2017.
 */

public class WebAppInterface {
    Context mContext;
    SVGTransmitter svgTransmitter;
    TextToSpeech tts;
    private SoundPool soundPool;
    private int sound_c,sound_d, sound_e, sound_f, sound_g, sound_a, sound_b, sound_c5, sound_cis, sound_dis, sound_fis, sound_gis, sound_ais;
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
        sound_b = soundPool.load(mContext,R.raw.h_4,1);
        sound_c5= soundPool.load(mContext,R.raw.c_5,1);
        sound_cis = soundPool.load(mContext,R.raw.cis_4,1);
        sound_dis = soundPool.load(mContext,R.raw.dis_4,1);
        sound_fis = soundPool.load(mContext,R.raw.fis_4,1);
        sound_gis = soundPool.load(mContext,R.raw.gis_4,1);
        sound_ais= soundPool.load(mContext,R.raw.ais_4,1);


    }

    //framework method
    @JavascriptInterface
    public void sendSVGToLaserPlotter(String svgString, String printJobUUID){
        Log.i("WebAppInterface", "Sending to plotter " + svgString);
        Toast.makeText(mContext, "Printing SVG", Toast.LENGTH_SHORT).show();
        byte[] uuidBytes = printJobUUID.getBytes();
        byte[] svgBytes = svgString.getBytes();
        int length = svgBytes.length;
        ByteBuffer bb = ByteBuffer.wrap(new byte[length + 4 + 36]);
        bb.put(uuidBytes);
        bb.putInt(length);
        bb.put(svgBytes);
        byte[] byteArray = bb.array();
        //svgTransmitter.sendToLaserPlotter(versionNr+"");
        svgTransmitter.sendToLaserPlotter(byteArray);
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
            case "cis": toneVal = sound_cis; break;
            case "dis": toneVal = sound_dis; break;
            case "fis": toneVal = sound_fis; break;
            case "gis": toneVal = sound_gis; break;
            case "ais": toneVal = sound_ais; break;

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