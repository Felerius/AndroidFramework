package com.hci.bachelorproject.tactileCalendar;

import android.content.Context;
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
    WebView webView;
    /** Instantiate the interface and set the context */
    CalendarWebAppInterface(Context c, WebView webView, GoogleAccountCredential mCredential) {
        super(c,webView);
        this.mCredential = mCredential;;
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

        webView.loadUrl("javascript:getEventsFromAndroid();");
    }

    //google calendar method
    @JavascriptInterface
    public String getEventProperty(int id, String property) {
        return googleCalendarEvents.get(id).get(property).toString();
    }


}