package de.hpi.hci.bachelorproject2016.bluetoothlib;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import static android.content.ContentValues.TAG;

//creating activity needs to handle permissions (Manifest.permission.ACCESS_COARSE_LOCATION)

public class PrinterConnector  {


    public BluetoothDevice device;
    public PrinterConnection connection;
    private BluetoothAdapter adapter;
    protected boolean isConnected=false;
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

    BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d("Bluetooth","Bluetooth device found " + d.getName());
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
        context.unregisterReceiver(deviceReceiver);

    }


}
