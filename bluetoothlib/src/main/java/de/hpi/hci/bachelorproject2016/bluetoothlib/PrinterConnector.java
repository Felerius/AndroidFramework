package de.hpi.hci.bachelorproject2016.bluetoothlib;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Set;

import static android.content.ContentValues.TAG;

//creating activity needs to handle permissions (Manifest.permission.ACCESS_COARSE_LOCATION)

public class PrinterConnector  {


    public BluetoothDevice device;

    public PrinterConnection getConnection() {
        return connection;
    }

    public void setConnection(PrinterConnection connection) {
        this.connection = connection;
    }

    private PrinterConnection connection;
    private BluetoothAdapter adapter;
    Set<BluetoothDevice> pairedDevices;
    protected boolean isConnected=false;
    private String deviceName;
    private Mode mode;
    private String ip;
    private int port;
    private Context context;
    PrinterConnection.OnConnectionCallBack onConnectionCallBack;
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
        //pairedDevices = adapter.getBondedDevices();


        IntentFilter filter= new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(deviceReceiver,filter);
    }

    BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d("Bluetooth","Bluetooth device found " + d.getName());
                Log.d("Bluetooth", "name to be search for " + deviceName);
                if (d.getName()!=null){
                    if (d.getName().contains(deviceName)) {
                        adapter.cancelDiscovery();
                        device = d;
                        Log.d(TAG, "found device with MAC " + d.getAddress());
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
        } else if (mode == Mode.BLUETOOTH) {
            Log.d("Bluetooth", "initializing connection");
            //http://stackoverflow.com/questions/23076641/ble-device-bonding-remove-automatically-in-android
            // Loop through the Set of paired devices, checking to see
            // if one of the devices is the device you need to unpair
            // from. I use the device name, but I'm sure you can find
            // another way to determine whether or not its your device
            // -- if you need to. :)
            /*for (BluetoothDevice bt : pairedDevices) {
                if (bt.getName().contains(deviceName)) {
                    Log.d("Bluetooth", "unpairing");
                    unpairDevice(bt);
                }
            }*/

            adapter.cancelDiscovery();
            adapter.startDiscovery();
        }
    }

    private void initializeBluetoothConnection(){
        connection=new BluetoothConnection(context,device);
        connection.setOnConnectionCallBack(onConnectionCallBack);
        if(connection!=null)connection.connect();

    }

    public void stopConnection(){
        if (connection!= null) {
            connection.tearDown();
        }

        adapter.cancelDiscovery();
        try{
            context.unregisterReceiver(deviceReceiver);
        } catch (IllegalArgumentException e){

        }

    }



    // Function to unpair from passed in device
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) { Log.e(TAG, e.getMessage()); }
    }


}
