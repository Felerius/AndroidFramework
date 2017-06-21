package com.hci.bachelorproject.webapplib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class LinepodBaseActivity extends AppCompatActivity {

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



    protected void checkPermission(String permission, int requestCode) {
        //if BT-permission is allowed, create webview
        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION){
            if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                if (webAppInterface==null){
                    webAppInterface = new JSAppInterface(getApplicationContext(),webView,false);
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
                ActivityCompat.requestPermissions(LinepodBaseActivity.this, new String[]{permission},
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
