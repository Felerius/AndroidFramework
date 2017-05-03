package com.hci.bachelorproject.webapplib;

import android.content.Context;
import android.media.SoundPool;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import java.nio.ByteBuffer;

import de.hpi.hci.bachelorproject2016.bluetoothlib.SVGTransmitter;

/**
 * Created by Julius on 01.03.2017.
 */

public class JSAppInterface {
    Context mContext;
    SVGTransmitter svgTransmitter;
    TextToSpeech tts;



    WebView webView;
    /** Instantiate the interface and set the context */
    JSAppInterface(Context c, WebView webView) {
        mContext = c;
        svgTransmitter = new SVGTransmitter(c, webView);
        this.webView = webView;
        this.tts = new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });

    }

    //framework method
    @JavascriptInterface
    public void sendSVGToLaserPlotter(String svgString, String printJobUUID){
        Log.i("WebAppInterface", "Sending to plotter " + svgString);
        Toast.makeText(mContext, "Printing SVG", Toast.LENGTH_SHORT).show();
        byte[] uuidBytes = printJobUUID.getBytes();
        byte[] svgBytes = svgString.getBytes();
        int length = svgBytes.length;
        Log.i("SVG Send", "length " + length);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,text);
        } else {
            tts.speak(text,TextToSpeech.QUEUE_FLUSH,null);
        }
    }


}