package com.hci.bachelorproject.bluetoothlib;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Adrian on 03.01.2017.
 */

public class TcpPrinterConnection extends PrinterConnection {

    private static final String TAG = "TCP CONNECTION";
    private Socket client;
    private String remoteAdress;
    private boolean isConnected=false;
    private int port;
    private Thread t=new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
                try{
                    if(in==null ||client.isClosed()){
                        isConnected=false;
                        if(onConnectionCallBack!=null)onConnectionCallBack.connectionLost();
                        return;
                    }
                    byte[] buffer=new byte[in.available()];
                    int readB=in.read(buffer);
                    if(onConnectionCallBack!=null==readB>0){
                        onConnectionCallBack.newCharsAvailable(buffer,readB);
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        }
    });
    private InputStream in;
    private OutputStream out;
    private OnConnectionCallBack onConnectionCallBack;
   public TcpPrinterConnection(String addr, int port){
       remoteAdress=addr;
       this.port=port;

   }

    @Override
    public void write(String data) {
        try{
            if(out!=null)out.write(data.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    client=new Socket(InetAddress.getByName(remoteAdress),port);
                    in=new BufferedInputStream(client.getInputStream());
                    out=new PrintStream(client.getOutputStream());
                    if(client.isConnected()){
                        isConnected=true;
                        if(onConnectionCallBack!=null){
                            Log.d(TAG, "run: connected");
                            onConnectionCallBack.connectionEstablished();
                        }
                        t.start();
                    }
                    ;

                }catch (Exception e){
                    if(onConnectionCallBack!=null)onConnectionCallBack.connectionRefused();
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void tearDown() {
        try{
            if(client!=null)client.close();
            t.interrupt();
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void setOnConnectionCallBack(OnConnectionCallBack onConnectionCallBack) {
        this.onConnectionCallBack=onConnectionCallBack;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }
}
