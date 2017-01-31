package de.hpi.hci.bachelorproject2016.bluetoothlib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BluetoothConnection extends PrinterConnection{
    private UUID uuidSecure;
    private UUID uuidInsecure= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OnConnectionCallBack cb;
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private Context context;
    private OutputStream outputStream;
    private InputStream inputStream;
    public boolean isConnected=false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {


            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())){
                isConnected=false;
                outputStream=null;
                inputStream=null;
                cb.connectionLost();

            }
            if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
                isConnected=true;
                cb.connectionEstablished();

            }

            // Add the name and address to an array adapter to show in a ListView




        }
    };
    public BluetoothConnection(Context c, BluetoothDevice d){
        device= d;
        context=c;
        d.fetchUuidsWithSdp();
        IntentFilter filter= new IntentFilter(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(mReceiver,filter);


    }
    public void connect(){
        connectToDevice(null);
    }
    public void connectToDevice(@Nullable UUID uuid){
        //if(uuids.size()<=0)return;
        Log.d("CONNECTION", "trying to connect");
        try{
            //socket=device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            //socket= device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            //device.createBond();
            if(uuid!=null){
                socket=device.createInsecureRfcommSocketToServiceRecord(uuid);
            }
            else{
                socket=device.createInsecureRfcommSocketToServiceRecord(uuidInsecure);
            }




        }catch (Exception e){
            if(cb!=null)cb.connectionRefused();
            return;
        }

        Thread t= new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    Log.d("Thread", "trying to connect");
                    socket.connect();
                   
                    Log.d("Thread", "conect finsished");
                    outputStream= socket.getOutputStream();
                    inputStream= socket.getInputStream();
                    Log.d(TAG, "run: set up io");
                    isConnected=true;
                    startListener();
                    if(cb!=null)cb.connectionEstablished();


                }
                catch (Exception e){
                    if(cb!=null)cb.connectionRefused();
                    isConnected=false;
                }

            }
        });
        t.start();

        return;

    }

    /**
     *
     *
     * if this is supposed to alter the UI, use runOnUiThread(Runnable action) in the callback
     * as other threads cannot do that
     *
     * */
    public void setOnConnectionCallBack( OnConnectionCallBack c){
        cb=c;
    }

    public boolean sendData(byte[] bytes){
        try{
            outputStream.write(bytes);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            Log.d("WRITE", "FAILED");
            return false;
        }


    }
    public void sendData(char[] chars){

    }
    public void write(String data){
        sendData(data);
    }

    public void sendData(String out){
        sendData(out.getBytes());
    }


    public void tearDown(){
        context.unregisterReceiver(mReceiver);
        isConnected=false;
        if(socket.isConnected()){
            try{
                socket.close();
            }catch (Exception e){
                Log.d(TAG, "teardown: coulnt close socket");
            }
        }
    }

    public void startListener(){
        final Thread listener= new Thread(new Runnable() {
            @Override
            public void run() {

                while(inputStream!=null && isConnected){
                    try {
                        byte[] buffer = new byte[inputStream.available()];
                        int readSize=inputStream.read(buffer);

                        if(cb!=null && readSize>0){
                            cb.newCharsAvailable(buffer,readSize);
                            Log.d(TAG, "run: got chars");
                        }

                    }catch (Exception e){
                        Log.d(TAG, "run: Error reading");
                        e.printStackTrace();
                        if(cb!=null)cb.connectionLost();
                        isConnected=false;
                    }

                }
            }
        });
        listener.start();
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }
}
