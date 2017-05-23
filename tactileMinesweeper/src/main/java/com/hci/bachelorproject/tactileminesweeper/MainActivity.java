package com.hci.bachelorproject.tactileminesweeper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.hci.bachelorproject.webapplib.JSAppInterface;

import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    JSAppInterface webAppInterface;
    static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1004;
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
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (this.webAppInterface!=null){
            webAppInterface.stopInterface();
        }
    }



    private void checkPermission(String permission, int requestCode) {
        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION){
            if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                webAppInterface = new JSAppInterface(getApplicationContext(),webView,false);
                webView.addJavascriptInterface(webAppInterface, "Android");
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission},
                        requestCode);

            }
        }
    }


}
