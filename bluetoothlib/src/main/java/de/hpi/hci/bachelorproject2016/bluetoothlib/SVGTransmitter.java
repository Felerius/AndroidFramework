package de.hpi.hci.bachelorproject2016.bluetoothlib;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.WebView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Locale;

import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnection;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnector;

import static android.content.ContentValues.TAG;

/**
 * Created by Julius on 05.04.2017.
 */

public class SVGTransmitter {
    public PrinterConnector getPrinterConnector() {
        return printerConnector;
    }

    PrinterConnector printerConnector;
    TextToSpeech tts;
    PrinterConnector.Mode connectionMode = PrinterConnector.Mode.BLUETOOTH;
    String utteranceId = "utteranceId";

    public WebView getWebView() {
        return webView;
    }

    public WebView webView;
    String svgData="";
    byte[] svgBytes;
    Context context;
    public SVGTransmitter(Context context, PrinterConnector printerConnector){
        initTTS(context);
        this.context = context;
        this.printerConnector = printerConnector;
    }

    public SVGTransmitter(Context context){
        initTTS(context);
        this.context = context;
        initPrinterConnector();
    }

    public SVGTransmitter(Context context, WebView webView){
        this.webView = webView;
        this.context = context;
        initTTS(context);
        initPrinterConnector();
    }

    private void initTTS(final Context context){
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                //speak(context.getString(R.string.connecting_laser_plotter));
            }
        });

    }

    private void initPrinterConnector(){


        PrinterConnection.OnConnectionCallBack onConnectionCallBack = new PrinterConnection.OnConnectionCallBack() {

            @Override
            public void connectionEstablished() {
                Log.d(TAG, "connectionEstablished: ");
                speak(context.getString(R.string.connection_established));

                sendCommands(printerConnector, svgBytes);

            }

            @Override
            public void connectionLost() {
                speak(context.getString(R.string.lost_connection));
                stopPrinterConnector();

            }

            @Override
            public void connectionRefused() {
                speak(context.getString(R.string.could_not_connect));
                stopPrinterConnector();
            }

            @Override
            public void newCharsAvailable(byte[] c, int byteCount) {
                Log.d("new chars", "");
                IntBuffer intBuf =
                        ByteBuffer.wrap(c)
                                .order(ByteOrder.BIG_ENDIAN)
                                .asIntBuffer();
                int[] array = new int[intBuf.remaining()];
                intBuf.get(array);
                final int type = Integer.valueOf(array[0]);
                switch (type){
                    case 0: final int x1=Integer.valueOf(array[1]);
                        final int y1=Integer.valueOf(array[2]);
                        final int x2=Integer.valueOf(array[3]);
                        final int y2=Integer.valueOf(array[4]);
                        final int eventType1=Integer.valueOf(array[5]);
                        final int eventType2=Integer.valueOf(array[6]);


                        Log.d(TAG, "newCharsAvailable: x: "+x1);
                        Log.d(TAG, "newCharsAvailable: y: "+y1);
                        Log.d(TAG, "newCharsAvailable: event type 1: "+eventType1);
                        Log.d(TAG, "newCharsAvailable: event type 2: "+eventType2);

                        Runnable sendInputDataOnUiThread = new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("javascript:getInputData(" + x1 + "," + y1 + "," + x2 + "," + y2 + "," + eventType1 + "," + eventType2 + ");");
                            }
                        };
                        webView.post(sendInputDataOnUiThread);
                        break;
                    case 1:
                        final int status = Integer.valueOf(array[1]);
                        Log.d("Status", status + "");
                        Log.d("UUID", "received uuid");
                        byte [] subArray = Arrays.copyOfRange(c, 8, 44);
                        String uuid = new String(subArray);
                        Log.d("received uuid", uuid);
                        switch (status){
                            case 0:
                                String text = "Finished printing";
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                                } else {
                                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                                }
                                webView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        webView.loadUrl("javascript:printStatusChanged(" + status + ");");
                                    }
                                });

                        }
                }

            }
        };
        printerConnector = new PrinterConnector(connectionMode, context.getString(R.string.bluetooth_device_name), "192.168.42.132", 8090,
                context, onConnectionCallBack);

        printerConnector.initializeConnection();
    }


    /*public void sendToLaserPlotter(String svgString) {
        svgData = svgString;
        if (printerConnector.device == null || printerConnector.getConnection() == null) {
            speak(context.getString(R.string.connecting_laser_plotter));
            printerConnector.initializeConnection();
        } else {
            if (!printerConnector.getConnection().isConnected()) {
                speak(context.getString(R.string.connecting_laser_plotter));

                printerConnector.initializeConnection();
            } else {
                speak(context.getString(R.string.sending_img_laser_plotter));
                sendCommands(printerConnector, svgString);
            }
        }
    }*/

    public void sendToLaserPlotter(byte[] svgBytes) {
        this.svgBytes = svgBytes;
        if (printerConnector.device == null || printerConnector.getConnection() == null ) {

            speak(context.getString(R.string.connecting_laser_plotter));
            printerConnector.initializeConnection();
        } else {
            if (!printerConnector.getConnection().isConnected()) {
                speak(context.getString(R.string.connecting_laser_plotter));

                printerConnector.initializeConnection();
            } else {
                speak(context.getString(R.string.sending_img_laser_plotter));
                sendCommands(printerConnector, svgBytes);
            }
        }
    }

    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    /*public void sendCommands(PrinterConnector printerConnector, String svgString) {
        //parser = new SVGParser(svgString,true);
        Log.d(TAG,"Sending commands");
        printerConnector.getConnection().write(svgString);

    }*/
    public void sendCommands(PrinterConnector printerConnector, byte[] svgBytes) {
        //parser = new SVGParser(svgString,true);
        Log.d(TAG,"Sending commands " + new String(svgBytes));
        printerConnector.getConnection().sendData(svgBytes);

    }

    public void stopPrinterConnector(){
        if (printerConnector!= null){
            printerConnector.stopConnection();
            printerConnector = null;
        }
    }
}
