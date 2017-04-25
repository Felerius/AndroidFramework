package de.hpi.hci.bachelorproject2016.bluetoothlib;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.WebView;

import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnection;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnector;

import static android.content.ContentValues.TAG;

/**
 * Created by Julius on 05.04.2017.
 */

public class SVGTransmitter {
    PrinterConnector printerConnector;
    TextToSpeech tts;
    PrinterConnector.Mode connectionMode = PrinterConnector.Mode.BLUETOOTH;
    String utteranceId = "utteranceId";
    WebView webView;
    String svgData="";
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
        initPrinterConnector();
        initTTS(context);
    }

    private void initTTS(Context context){
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });
    }

    private void initPrinterConnector(){


        PrinterConnection.OnConnectionCallBack onConnectionCallBack = new PrinterConnection.OnConnectionCallBack() {

            @Override
            public void connectionEstablished() {
                Log.d(TAG, "connectionEstablished: ");
                speak(context.getString(R.string.connection_established));

                sendCommands(printerConnector, svgData);
            }

            @Override
            public void connectionLost() {
                speak(context.getString(R.string.lost_connection));

                printerConnector = null;
            }

            @Override
            public void connectionRefused() {
                speak(context.getString(R.string.could_not_connect));

                printerConnector = null;
            }

            @Override
            public void newCharsAvailable(byte[] c, int byteCount) {
                final String message=new String(c,0,byteCount);
                String [] xy=message.split(",");
                final int x1=Integer.valueOf(xy[0]);
                final int y1=Integer.valueOf(xy[1]);
                final int x2=Integer.valueOf(xy[2]);
                final int y2=Integer.valueOf(xy[3]);

                Log.d(TAG, "newCharsAvailable: x: "+x1);
                Log.d(TAG, "newCharsAvailable: y: "+y1);

                Runnable sendInputDataOnUiThread = new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:getInputData(" + x1 + "," + y1 + "," + x2 + "," + y2 + ");");
                    }
                };
                webView.post(sendInputDataOnUiThread);
            }
        };
        printerConnector = new PrinterConnector(connectionMode, context.getString(R.string.bluetooth_device_name), "192.168.42.132", 8090,
                context, onConnectionCallBack);
        printerConnector.initializeConnection();
    }


    public void sendToLaserPlotter(String svgString) {
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
    }

    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    public void sendCommands(PrinterConnector printerConnector, String svgString) {
        //parser = new SVGParser(svgString,true);
        Log.d(TAG,"Sending commands");
        printerConnector.getConnection().write(svgString);
        /*parser = new SVGParser(svgString, new PointF(20, 20));
        ArrayList<Instruction> instructions = new ArrayList<>();
        parser.setInstructions(instructions);
        parser.startParsing();

        for (Instruction instruction : instructions) {
            Log.d("Connection", "sending instruction " + instruction.buildInstruction(Instruction.Mode.GPGL));
            printerConnector.connection.write(instruction.buildInstruction(Instruction.Mode.GPGL));
        }*/
    }
}
