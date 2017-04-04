package com.hci.bachelorproject.androidjsinteraction;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    Button trigger;
    SimpleWebServer webServer;
    //WebViewLocalServer assetServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*assetServer = new WebViewLocalServer(getApplicationContext());
        WebViewLocalServer.AssetHostingDetails details =
                assetServer.hostAssets("http://localhost/");
*/
        webServer = new SimpleWebServer(8000,getAssets());
        webServer.start();
        webView = (WebView)findViewById(R.id.webView);

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        //http://stackoverflow.com/questions/9414312/android-webview-javascript-from-assets
        //String html = "file:///android_asset/d3test.html";
        //String html = "http://localhost:8000/TactileCalendar/calendar.html"; //parse the content of this url
        //String html = "calendar.html"; //parse the content of this url
        //webView.loadDataWithBaseURL("http://localhost:8000/TactileCalendar", html, "text/html", "utf-8", ""); //if no WebServer needed and file server is enough
        webView.setWebViewClient(new WebViewClient());
        webView.setWebContentsDebuggingEnabled(true);

        webView.setWebChromeClient(new WebChromeClient(){

            public void onCloseWindow(WebView w){
                super.onCloseWindow(w);
                Log.d("WebChromeClient", "Window close");
                w.destroy();
            }
        });

        webView.getSettings().setDomStorageEnabled(true);
        try {
            webView.loadUrl("http://localhost:8000/TactileCalendar/test.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
        //webView.loadUrl("file:///android_asset/d3test.html");
        trigger = (Button)findViewById(R.id.button);
        trigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl("javascript:showAndroidToast('Toast');");
            }
        });


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        webServer.stop();
    }

    class MyWebViewClient extends WebViewClient {
        // For KitKat and earlier.
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!Uri.parse(url).getHost().contains("google.com")) {

                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }


}
