package com.hci.bachelorproject.androidjsinteraction;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.api.services.calendar.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Julius on 01.03.2017.
 */

public class WebAppInterface {
    Context mContext;


    public void setGoogleCalendarEvents(List<Event> googleCalendarEvents) {
        this.googleCalendarEvents = googleCalendarEvents;
    }

    List<Event>  googleCalendarEvents;
    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void printSVG(String svg) {
        Toast.makeText(mContext, "Printing SVG", Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getGoogleCalendarEvents() {
        JSONObject obj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Event event: googleCalendarEvents){
            JSONObject jsonEvent = new JSONObject();
            /*try {
                jsonEvent.put("summary", event.getSummary());
                jsonEvent.put("id",event.getId());
                jsonEvent.put("startTime", event.getStart());
                jsonEvent.put("endTime", event.getEnd());
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            Log.i("Event",event.toString());
            jsonArray.put(event.toString());
        }
        Log.i("Array", jsonArray.toString());
        try {
            obj.put("events",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    @JavascriptInterface
    public String getEventProperty(int id, String property) {
        return googleCalendarEvents.get(id).get(property).toString();
    }


}