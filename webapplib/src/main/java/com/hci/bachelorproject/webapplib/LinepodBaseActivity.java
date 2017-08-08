package com.hci.bachelorproject.webapplib;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.ArrayList;

public class LinepodBaseActivity extends AppCompatActivity implements OnTriggerSpeechCallback{

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private boolean speechRecognitionListening = false;

    protected WebView webView;
    protected String webAppUrl = "file:///android_asset/index.html";
    //this is the key part of every Linepod-application
    protected final String webAppInterfaceName = "Android";
    protected JSAppInterface webAppInterface;

    static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 9999;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }
        //check bluetooth permissions, otherwise the program will not work and you couldn't print
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (this.webAppInterface!=null){
            webAppInterface.stopInterface();
        }
    }


    public void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "listening");
        try {
            if (speechRecognitionListening == false){
                speechRecognitionListening = true;
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            }
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this,
                    "speech not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    speechRecognitionListening = false;
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    webView.loadUrl("javascript:handleSpeech('" + result.get(0) + "');");
                }
                break;
            }
        }
    }



    protected void checkPermission(String permission, int requestCode) {
        //if BT-permission is allowed, create webview
        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION){
            if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                if (webAppInterface==null){
                    webAppInterface = new JSAppInterface(getApplicationContext(),webView,true);
                }

                webView.addJavascriptInterface(webAppInterface, webAppInterfaceName);
                webView.loadDataWithBaseURL("file:///android_asset/", "", "text/html", "utf-8", "");
                webView.loadUrl(webAppUrl);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission},
                        requestCode);

            }
        }
    }
    public JSAppInterface getWebAppInterface() {
        return webAppInterface;
    }

    public void setWebAppInterface(JSAppInterface webAppInterface) {
        this.webAppInterface = webAppInterface;
    }



}
