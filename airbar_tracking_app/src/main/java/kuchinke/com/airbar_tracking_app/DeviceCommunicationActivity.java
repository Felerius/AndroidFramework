package kuchinke.com.airbar_tracking_app;


import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import de.hpi.hci.bachelorproject2016.bluetoothlib.BluetoothConnection;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnection;
import kuchinke.com.airbar_tracking_app.annotations.Annotation;
import kuchinke.com.airbar_tracking_app.annotations.CircleAnnotation;
import kuchinke.com.airbar_tracking_app.annotations.RectangleAnnotation;


public class DeviceCommunicationActivity extends AppCompatActivity {
    private PrinterConnection.OnConnectionCallBack connectionCallBack;
    private PrinterConnection connection;
    private Annotation lastAnnotation= null;
    ArrayList<Annotation> annotations = new ArrayList<>();;
    int MAX_Y=2000;
    private String TAG="Mainactivity";
    private TrackingSurface trackingSurface;
    int streamType = AudioManager.STREAM_MUSIC;
    int volume = 80;
    ToneGenerator toneGenerator = new ToneGenerator(streamType, volume);

    private TextView messages;
    private EditText commandEdit;
    private CoordinatorLayout mainLayout;
    private Snackbar connectingSnackbar;
    TextToSpeech tts;
    private Queue<UUID> commandsToSend;
        // Register the BroadcastReceiver

        @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "onCreate: ");
            setContentView(R.layout.activity_device_communication);
            Intent intent = getIntent();
            BluetoothDevice device= intent.getParcelableExtra("DEVICE");

            setupUiReferences();
            setupListeners();
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {

                }
            });
            parseJsonContents();

            trackingSurface= (TrackingSurface) findViewById(R.id.trackingSurface);
            trackingSurface.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    float x=motionEvent.getX();
                    float y=motionEvent.getY();
                    trackingSurface.drawTrackingCircle((int)x,(int)y,-1,-1);

                    return false;
                }
            });

            //connection= new BluetoothConnection(this, device);
            connection=new BluetoothConnection(this,device);
            connectingSnackbar= Snackbar.make(mainLayout,"Connecting", Snackbar.LENGTH_INDEFINITE);
            connectingSnackbar.show();
            connection.setOnConnectionCallBack(connectionCallBack);
            connection.connect();

            Log.d(TAG, "onCreate: create finished");
        }



    private void parseJsonContents(){
        try {
            String json = loadJsonAssetFile();
            JSONObject obj = new JSONObject(json);
            addRects(obj);
            addCircles(obj);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addRects(JSONObject obj){
        JSONArray m_jArry = null;
        try {
            m_jArry = obj.getJSONArray("rects");
            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                Log.d("x1-->", jo_inside.getString("x1"));
                int x1 = jo_inside.getInt("x1");
                int y1 = MAX_Y - jo_inside.getInt("y1");
                int x2 = jo_inside.getInt("x2");
                int y2 = MAX_Y - jo_inside.getInt("y2");
                String text = jo_inside.getString("tone");
                //Add your values in your `ArrayList` as below:
                annotations.add(new RectangleAnnotation(x1,y2,x2,y1,text));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addCircles(JSONObject obj){
        JSONArray m_jArry = null;
        try {
            m_jArry = obj.getJSONArray("circles");
            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                int cx = jo_inside.getInt("cx");
                int cy = MAX_Y - jo_inside.getInt("cy");
                int r = jo_inside.getInt("r");
                String text = jo_inside.getString("tone");
                //Add your values in your `ArrayList` as below:
                annotations.add(new CircleAnnotation(cx,cy,r,text));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJsonAssetFile() {
        String json = null;
        try {
            InputStream is = getAssets().open("Keyboard_positional_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void checkTrackingPosition(int x, int y){
        for (Annotation annotation : annotations){

            Log.e("anno", annotation.checkItemIntersection(x,y) + "");
            boolean hovered = false;
            if (annotation.checkItemIntersection(x,y)){
                Log.i("Intersection", "hovered annotation");
                if (lastAnnotation != null ){
                    if (lastAnnotation == annotation){
                        break;
                    }
                }
                lastAnnotation = annotation;

                int toneType = Tones.getTone(annotation.getText());
                int durationMs = 500;
                toneGenerator.startTone(toneType, durationMs);

 ///               tts.speak(annotation.getText(), TextToSpeech.QUEUE_FLUSH, null, "");
                hovered = true;
            }
            if (!hovered){
                lastAnnotation = null;
            }
        }
    }

    @Override
        protected void onDestroy() {
            super.onDestroy();
            if(connection!=null)connection.tearDown();

        }


    void setupUiReferences(){
        mainLayout=(CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        messages=(TextView)findViewById(R.id.textView);
        commandEdit= (EditText)findViewById(R.id.commandEdit);
        commandEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }
    void setupListeners(){
        commandEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId==EditorInfo.IME_ACTION_DONE){
                    if(commandEdit.getText().length()>0){
                        String message=v.getText().toString();
                        connection.write(message);
                    }



                }
                return true;
            }
        });

        connectionCallBack= new PrinterConnection.OnConnectionCallBack() {


            @Override
            public void connectionEstablished() {
                if(connectingSnackbar.isShown())connectingSnackbar.dismiss();
                Log.d(TAG, "connectionEstablished: starting to parse ");
                Snackbar snackbar= Snackbar.make(mainLayout,"Connection established", Snackbar.LENGTH_SHORT);
                snackbar.show();



            }

            @Override
            public void connectionLost() {
                if(connectingSnackbar.isShown())connectingSnackbar.dismiss();
                Snackbar snackbar= Snackbar.make(mainLayout,"Connection Lost", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                snackbar.show();
            }

            @Override
            public void connectionRefused() {
                if(connectingSnackbar.isShown())connectingSnackbar.dismiss();
                Snackbar snackbar= Snackbar.make(mainLayout,"Connection Refused", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                snackbar.show();
            }

            @Override
            public void newCharsAvailable(final byte[] c, final int byteCount) {
                final String message=new String(c,0,byteCount);
                Log.d(TAG, "newCharsAvailable: "+message);
                String [] xy=message.split(",");
                int x1=Integer.valueOf(xy[0]);
                int y1=Integer.valueOf(xy[1]);
                int x2=Integer.valueOf(xy[2]);
                int y2=Integer.valueOf(xy[3]);
                Log.d(TAG, "newCharsAvailable: x: "+x1);
                Log.d(TAG, "newCharsAvailable: y: "+y1);
                trackingSurface.drawTrackingCircle(y1,x1,y2,x2);
                checkTrackingPosition(x1,y1);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messages.append(message+"\n");
                    }
                });
            }
        };
    }









    private List<String> getParts(String string, int partitionSize, char followingIndicator) {
        List<String> parts = new ArrayList<String>();
        int len = string.length();
        for (int i=0; i<len; i+=partitionSize)
        {
            String p=string.substring(i, Math.min(len, i + partitionSize));
            if(i<len-partitionSize)p+=followingIndicator;
            parts.add(p);

        }

        for(String p: parts) Log.d(TAG, "getParts: "+p);
        return parts;
    }

    }

