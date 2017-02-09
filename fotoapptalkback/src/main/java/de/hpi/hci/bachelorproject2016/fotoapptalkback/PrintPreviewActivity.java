package de.hpi.hci.bachelorproject2016.fotoapptalkback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Locale;

import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnection;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnector;
import de.hpi.hci.bachelorproject2016.fotoapptalkback.ImageProcessing.ImageTracerAndroid;
import de.hpi.hci.bachelorproject2016.svgparser.Instruction;
import de.hpi.hci.bachelorproject2016.svgparser.SVGParser;

import static android.content.ContentValues.TAG;
import static de.hpi.hci.bachelorproject2016.fotoapptalkback.MainActivity.CONNECTING;

/**
 * Created by Julius on 08.02.2017.
 */

public class PrintPreviewActivity extends Activity {
    private Button printButton;
    private Button goBackButton;
    private WebView wv;
    private TextToSpeech tts;


    private static final String TOOK_PHOTO = "tookPhoto";

    PrinterConnector printerConnector;
    SVGParser parser;

    PrinterConnector.Mode connectionMode = PrinterConnector.Mode.BLUETOOTH;


    String svgString ="";
    String mimeType = "text/html";
    String encoding = "utf-8";

    UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String s) {

        }

        @Override
        public void onDone(String s) {
            if (s.equals(TOOK_PHOTO) || s.equals(getString(R.string.picked_picture))){
                wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");
            }
        }

        @Override
        public void onError(String s) {

        }

        @Override
        public void onStop(String s, boolean interrupted){
            if (s.equals(TOOK_PHOTO)){
                tts.speak(getString(R.string.printing_duration), TextToSpeech.QUEUE_FLUSH, null, s);
            }
        }
    };



    String utteranceId = this.hashCode() + "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview);
        printButton = (Button) findViewById(R.id.btn_print_picture);
        goBackButton = (Button) findViewById(R.id.btn_go_back);
        wv = (WebView) findViewById(R.id.web_view);

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImageToLaserPlotter(svgString);
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PrintPreviewActivity.this,MainActivity.class));
            }
        });

        Bitmap picture = (Bitmap) getIntent().getExtras().get("IMAGE");

        try {
            svgString = ImageTracerAndroid.imageToSVG(picture, null, null);
            wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");

            //int maxLogSize = 1000;
            /*for (int i = 0; i <= svgString.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > svgString.length() ? svgString.length() : end;
                Log.v(TAG, svgString.substring(start, end));
            }*/

            //wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");
            //sendImageToLaserPlotter(svgString);
        } catch (Exception e) {
            Log.e(" Error tracing photo ", e.toString());
        }


        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "successfully set up text to speech");
                    int result = tts.setLanguage(new Locale(Locale.getDefault().getISO3Language(),
                            Locale.getDefault().getISO3Country()));

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    tts.setOnUtteranceProgressListener(utteranceProgressListener);
                    tts.speak(getString(R.string.printing_duration), TextToSpeech.QUEUE_FLUSH, null, TOOK_PHOTO);
                } else {
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });

    }


    private void sendImageToLaserPlotter(String svgString) {
        final String svgData = svgString;
        PrinterConnection.OnConnectionCallBack onConnectionCallBack = new PrinterConnection.OnConnectionCallBack() {
            @Override
            public void connectionEstablished() {
                Log.d(TAG, "connectionEstablished: ");
                tts.speak(getString(R.string.connection_established) + getString(R.string.sending_img_laser_plotter), TextToSpeech.QUEUE_ADD, null, utteranceId);
                sendCommands(printerConnector, svgData);
            }

            @Override
            public void connectionLost() {
                tts.speak(getString(R.string.lost_connection), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                printerConnector = null;
            }

            @Override
            public void connectionRefused() {
                tts.speak(getString(R.string.could_not_connect), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                printerConnector = null;
            }

            @Override
            public void newCharsAvailable(byte[] c, int byteCount) {

            }
        };
        if (printerConnector == null){
            printerConnector = new PrinterConnector(connectionMode, getString(R.string.bluetooth_device_name), "192.168.42.132", 8090,
                    getApplicationContext(), onConnectionCallBack);
        }
        if (printerConnector.device == null || printerConnector.connection == null) {
            tts.speak(getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_ADD, null, CONNECTING);
            Log.d(TAG,"Device or connection = null");
            printerConnector.initializeConnection();
        } else {
            if (!printerConnector.connection.isConnected()) {
                tts.speak(getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, CONNECTING);
                Log.d(TAG,"not connected");
                printerConnector.initializeConnection();
            } else {
                tts.speak(getString(R.string.sending_img_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                Log.d(TAG,"sending commands");
                sendCommands(printerConnector, svgString);
            }
        }
    }


    protected void sendCommands(PrinterConnector printerConnector, String svgString) {
        //parser = new SVGParser(svgString,true);
        parser = new SVGParser(svgString, new PointF(20, 20));
        ArrayList<Instruction> instructions = new ArrayList<>();
        parser.setInstructions(instructions);
        parser.startParsing();

        for (Instruction instruction : instructions) {
            Log.d("Connection", "sending instruction " + instruction.buildInstruction(Instruction.Mode.GPGL));
            printerConnector.connection.write(instruction.buildInstruction(Instruction.Mode.GPGL));
        }
    }


    public void onDestroy() {
        super.onDestroy();
        stopTextToSpeech();
        if (printerConnector !=null){
            printerConnector.stopConnection();
        }
    }

    public void stopTextToSpeech() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
