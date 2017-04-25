package com.hci.bachelorproject.tactilepiano;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class PianoActivity extends AppCompatActivity {


    WebView webView;
    WebAppInterface webAppInterface;
    static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1000; //for bluetooth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebContentsDebuggingEnabled(true);

        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);

    }


    private void checkPermission(String permission, int requestCode) {
        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION){
            if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                onAccessCoarsePermissionGranted();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PianoActivity.this, new String[]{permission},
                        requestCode);

            } //to abstract a little, one could maybe call onRequestPermissionResult(REQCODE,null,null) if the perm is already granted
        }
    }

    private void onAccessCoarsePermissionGranted(){
        webAppInterface = new WebAppInterface(getApplicationContext(),webView);
        webView.addJavascriptInterface(webAppInterface, "Android");
        webView.loadDataWithBaseURL("file:///android_asset/", "", "text/html", "utf-8", "");
        webView.loadUrl("file:///android_asset/piano.html");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    onAccessCoarsePermissionGranted();
                } else {
                    checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                }
        }
    }

}
