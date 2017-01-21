package com.hci.bachelorproject.fotoapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.hci.bachelorproject.bluetoothlib.BluetoothConnection;
import com.hci.bachelorproject.bluetoothlib.PrinterConnection;
import com.hci.bachelorproject.bluetoothlib.TcpPrinterConnection;

import java.util.ArrayList;
import java.util.UUID;

import kuchinke.com.svgparser.Instruction;

import static android.content.ContentValues.TAG;

/**
 * Created by Adrian on 28.11.2016.
 */

public class BluetoothActivity extends Activity implements PrinterConnection.OnConnectionCallBack {

    protected BluetoothDevice device;
    protected PrinterConnection connection;
    private BluetoothAdapter adapter;
    protected boolean isConnected=false;
    ArrayList<Instruction> commands = new ArrayList<Instruction>();
    public enum Mode{
        BLUETOOTH,
        TCP
    };


    BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG,"Bluetooth device found " + d.getName());
                //checkTrackingPosition(new Point (202,227));
                if (d.getName()!=null){
                    if (d.getName().contains("raspberrypi")) {
                        device = d;
                        Log.d(TAG, "found device");
                        initializeBluetoothConnection();
                    }

                }
                return;
            }
            if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Log.i("Bluetooth" , "discovery started");
                return;
            }
            if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                return;
            }
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(deviceReceiver);
        if(connection!=null)connection.tearDown();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = BluetoothAdapter.getDefaultAdapter();


        IntentFilter filter= new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(deviceReceiver,filter);
        //checkCoarseLocationPermission();


    }



    protected void checkCoarseLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ActivityCompat.requestPermissions(BluetoothActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            100);      // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    adapter.cancelDiscovery();
                    adapter.startDiscovery();
                    break;
            }
        }
    }

    public void initializeConnection(Mode mode){
        if (mode == Mode.TCP) {
            connection = new TcpPrinterConnection("192.168.42.132", 8090);
            connection.setOnConnectionCallBack(this);
            if(connection!=null)connection.connect();
        } else if (mode == Mode.BLUETOOTH){
            checkCoarseLocationPermission();
        }
    }

    private void initializeBluetoothConnection(){
        connection=new BluetoothConnection(this,device);
        connection.setOnConnectionCallBack(this);
        if(connection!=null)connection.connect();

    }





    @Override
    public void connectionEstablished() {
        Log.d(TAG, "connectionEstablished: ");
    }

    @Override
    public void connectionLost() {
        Log.d(TAG, "connectionLost: ");
    }

    @Override
    public void connectionRefused() {
        Log.d(TAG, "connectionRefused: ");
    }   

    @Override
    public void newCharsAvailable(byte[] c, int byteCount) {
        Log.d(TAG, "newCharsAvailable: ");


    }

    void sendDataWithID(UUID uuid, String data){
        if(connection!=null && connection.isConnected()){
            connection.write(uuid.toString() + ";" + data);
        }

    }


}
