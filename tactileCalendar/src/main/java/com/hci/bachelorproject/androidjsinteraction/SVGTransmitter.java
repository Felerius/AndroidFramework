package com.hci.bachelorproject.androidjsinteraction;

import android.content.Context;
import android.graphics.PointF;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.WebView;

import java.util.ArrayList;

import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnection;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnector;
import de.hpi.hci.bachelorproject2016.svgparser.Instruction;
import de.hpi.hci.bachelorproject2016.svgparser.SVGParser;

import static android.content.ContentValues.TAG;

/**
 * Created by Julius on 05.04.2017.
 */

public class SVGTransmitter {
    PrinterConnector printerConnector;
    TextToSpeech tts;
    PrinterConnector.Mode connectionMode;
    String utteranceId = "utteranceId";
    WebView webView;
    String svgData="";
    Context context;
    public SVGTransmitter(Context context, PrinterConnector printerConnector){
        initTTS(context);
        this.printerConnector = printerConnector;
    }

    public SVGTransmitter(Context context){
        initTTS(context);
        initPrinterConnector();
    }

    public SVGTransmitter(Context context, WebView webView){
        this.webView = webView;
        initPrinterConnector();
        initTTS(context);
    }

    private void initTTS(Context context){
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        this.context = context;
    }

    private void initPrinterConnector(){


        PrinterConnection.OnConnectionCallBack onConnectionCallBack = new PrinterConnection.OnConnectionCallBack() {

            @Override
            public void connectionEstablished() {
                Log.d(TAG, "connectionEstablished: ");
                tts.speak(context.getString(R.string.connection_established) , TextToSpeech.QUEUE_ADD, null, utteranceId);
                sendCommands(printerConnector, svgData);
            }

            @Override
            public void connectionLost() {
                tts.speak(context.getString(R.string.lost_connection), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                printerConnector = null;
            }

            @Override
            public void connectionRefused() {
                tts.speak(context.getString(R.string.could_not_connect), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                printerConnector = null;
            }

            @Override
            public void newCharsAvailable(byte[] c, int byteCount) {
                final String message=new String(c,0,byteCount);
                String [] xy=message.split(",");
                int x1=Integer.valueOf(xy[0]);
                int y1=Integer.valueOf(xy[1]);
                int x2=Integer.valueOf(xy[2]);
                int y2=Integer.valueOf(xy[3]);

                Log.d(TAG, "newCharsAvailable: x: "+x1);
                Log.d(TAG, "newCharsAvailable: y: "+y1);
                webView.loadUrl("javascript:getInputData(" + x1 + "," + y1 + ");");
            }
        };
        printerConnector = new PrinterConnector(connectionMode, context.getString(R.string.bluetooth_device_name), "192.168.42.132", 8090,
                context, onConnectionCallBack);
    }


    public void sendToLaserPlotter(String svgString) {
        svgData = svgString;
        if (printerConnector.device == null || printerConnector.getConnection() == null) {
            tts.speak(context.getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_ADD, null, utteranceId);
            printerConnector.initializeConnection();
        } else {
            if (!printerConnector.getConnection().isConnected()) {
                tts.speak(context.getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                printerConnector.initializeConnection();
            } else {
                tts.speak(context.getString(R.string.sending_img_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                sendCommands(printerConnector, svgString);
            }
        }
    }


    protected void sendCommands(PrinterConnector printerConnector, String svgString) {
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
