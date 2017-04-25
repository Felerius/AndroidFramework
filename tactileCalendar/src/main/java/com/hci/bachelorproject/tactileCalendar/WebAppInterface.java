package com.hci.bachelorproject.tactileCalendar;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.api.services.calendar.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hpi.hci.bachelorproject2016.bluetoothlib.SVGTransmitter;

/**
 * Created by Julius on 01.03.2017.
 */

public class WebAppInterface {
    Context mContext;
    SVGTransmitter svgTransmitter;
    TextToSpeech tts;

    public void setGoogleCalendarEvents(List<Event> googleCalendarEvents) {
        this.googleCalendarEvents = googleCalendarEvents;
    }

    List<Event>  googleCalendarEvents;
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
    }


    //google calendar method
    @JavascriptInterface
    public String getGoogleCalendarEvents() {
        JSONObject obj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Event event: googleCalendarEvents){

            jsonArray.put(event.toString());
        }
        try {
            obj.put("events",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    //google calendar method
    @JavascriptInterface
    public String getEventProperty(int id, String property) {
        return googleCalendarEvents.get(id).get(property).toString();
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



}