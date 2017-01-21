package com.hci.bachelorproject.bluetoothlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


import java.util.ArrayList;

import kuchinke.com.svgparser.Instruction;

import static android.content.ContentValues.TAG;

/**
 * Created by Julius on 20.01.2017.
 */

//creating activity needs to handle permissions (Manifest.permission.ACCESS_COARSE_LOCATION)

public class PrinterConnector  {


    public BluetoothDevice device;
    public PrinterConnection connection;
    private BluetoothAdapter adapter;
    protected boolean isConnected=false;
    ArrayList<Instruction> commands = new ArrayList<Instruction>();
    private String deviceName;
    private Mode mode;
    private String ip;
    private int port;
    private Context context;
    private PrinterConnection.OnConnectionCallBack onConnectionCallBack;
    public enum Mode{
        BLUETOOTH,
        TCP
    };

    public PrinterConnector(Mode mode, String deviceName, String ip, int port, Context context, PrinterConnection.OnConnectionCallBack onConnectionCallBack){
        this.mode = mode;
        this.deviceName = deviceName;
        this.ip = ip;
        this.port = port;
        this.context = context;
        this.onConnectionCallBack = onConnectionCallBack;
        adapter = BluetoothAdapter.getDefaultAdapter();


        IntentFilter filter= new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(deviceReceiver,filter);
    }

    public BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG,"Bluetooth device found " + d.getName());
                if (d.getName()!=null){
                    if (d.getName().contains(deviceName)) {
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


    public void initializeConnection(){
        if (mode == Mode.TCP) {
            connection = new TcpPrinterConnection(ip, port);
            connection.setOnConnectionCallBack(onConnectionCallBack);
            if(connection!=null)connection.connect();
        } else if (mode == Mode.BLUETOOTH){
            adapter.cancelDiscovery();
            adapter.startDiscovery();
        }
    }

    private void initializeBluetoothConnection(){
        connection=new BluetoothConnection(context,device);
        connection.setOnConnectionCallBack(onConnectionCallBack);
        if(connection!=null)connection.connect();

    }



}
