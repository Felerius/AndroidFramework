package com.hci.bachelorproject.tactileCalendar;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.hci.bachelorproject.webapplib.JSAppInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import de.hpi.hci.bachelorproject2016.bluetoothlib.SVGTransmitter;

/**
 * Created by Julius on 01.03.2017.
 */

public class CalendarWebAppInterface extends JSAppInterface {

    GoogleAccountCredential mCredential;
    public void setGoogleCalendarEvents(List<Event> googleCalendarEvents) {
        this.googleCalendarEvents = googleCalendarEvents;
    }

    List<Event>  googleCalendarEvents;
    /** Instantiate the interface and set the context */
    public CalendarWebAppInterface(Context c, WebView webView, GoogleAccountCredential mCredential) {
        super(c,webView,false);
        this.mCredential = mCredential;

    }

    @Override
    protected void setupTTS(){
        this.tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                /*tts.speak("Welcome to your tactile calendar. This app supports speech recognition that you can start by shaking your phone." +
                                "      + \"If you want you can now print your current Google Calendar by saying 'print'. Double check that your Linepod is turned on, a piece of swell paper is inserted " +
                                "      \"and that the lid is closed if you want to print. Also you can say 'options' or 'help' at anytime to hear your current options. "
                        , TextToSpeech.QUEUE_ADD,null);*/
            }
        });
    }

    @JavascriptInterface
    public void startSVGTransmitter(boolean instantPrint){
        this.svgTransmitter = new SVGTransmitter(mContext, webView);
        if (instantPrint){
            webView.post((new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:printSVG();");
                }
            }));

        }
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

    @JavascriptInterface
    public void createEvent(String name, String startTime, String endTime){
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("R_D_Location Callendar")
                .build();


        Event event = new Event().setSummary(name);

        DateTime startDateTime = new DateTime(startTime);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime);
        event.setStart(start);

        DateTime endDateTime = new DateTime(endTime);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime);
        event.setEnd(end);
        Log.i("Starttime", start.toString());
        Log.i("Endtime", end.toString());


        String calendarId = "primary";
        try {
            event = service.events().insert(calendarId, event).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Event created: %s\n", event.getHtmlLink());
        webView.post((new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:getEventsFromAndroid();");
            }
        }));

    }

    //google calendar method
    @JavascriptInterface
    public String getEventProperty(int id, String property) {
        return googleCalendarEvents.get(id).get(property).toString();
    }


}