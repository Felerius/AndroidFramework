package com.hci.bachelorproject.fotoapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.hci.bachelorproject.bluetoothlib.PrinterConnection;
import com.hci.bachelorproject.bluetoothlib.PrinterConnector;
import com.hci.bachelorproject.fotoapp.ImageProcessing.ImageTracerAndroid;
import com.hci.bachelorproject.fotoapp.ImageProcessing.ImageTransformator;

import java.util.ArrayList;

import kuchinke.com.svgparser.Instruction;
import kuchinke.com.svgparser.SVGParser;

import static android.content.ContentValues.TAG;


public class FotoAppActivity extends AudioActivity {

    private static final int PICK_IMAGE_REQUEST = 2;
	private static final int CAMERA_REQUEST = 1;


	SVGParser parser ;
	String svgString = "";
	PrinterConnector printerConnector;

    DisplayMetrics displaymetrics;

	WebView wv;

	String mimeType = "text/html";
	String encoding = "utf-8";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Window style and metrics
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        //checkWriteToStoragePermission();


		mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Extract data included in the Intent
				String message = intent.getStringExtra("message");
				Log.d("receiver", "Got message: " + message);
				switch (message){
					case "take":
						tts.speak("Opening camera", TextToSpeech.QUEUE_FLUSH, null);
						takePictureIntent();
						break;
					case "choose":
						tts.speak("Opening album", TextToSpeech.QUEUE_FLUSH, null);
						pickPictureIntent();
						break;
					default:
						break;
				}

			}
		};



		// UI
		ScrollView sv = new ScrollView(getApplicationContext());

		// Layout
		LinearLayout ll = new LinearLayout(getApplicationContext());
		ll.setOrientation(LinearLayout.VERTICAL);
		sv.addView(ll);

		// TextView for log
		// tv = new TextView(getApplicationContext()); tv.setText("Hi! ☺");ll.addView(tv);

		// WebView to show SVG
		wv = new WebView(getApplicationContext());
		ll.addView(wv);

		// Button 1
		Button b1 = new Button(getApplicationContext());
		b1.setText("Take a picture");
		b1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				takePictureIntent();
			}
		});
		ll.addView(b1);

		// Button 2
		Button b2 = new Button(getApplicationContext());
		b2.setText("Pick a picture");
		b2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				pickPictureIntent();

			}
		});
		ll.addView(b2);


        // Displaying UI
		setContentView(sv);
		handleIncomingIntents();


	}// End of onCreate()

	private void pickPictureIntent() {
		try {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
        }catch(Exception e){ e.printStackTrace(); }
	}

	private void takePictureIntent() {
		try {
			// Starting an Intent to take a picture
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, CAMERA_REQUEST);
		}catch(Exception e){ e.printStackTrace(); }
	}



	private void handleIncomingIntents(){
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				Log.e("FotoApp", "only accepting single image from other apps");
			} else if (type.startsWith("image/")) {
				Log.i(TAG,"received image");
				handleSendImage(intent); // Handle single image being sent
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				Log.e("FotoApp", "only accepting single image from other apps");
			}
		} else {
			// Handle other intents, such as being started from the home screen
		}
	}


    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if (imageUri != null) {
            try {
				Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
				Bitmap compressedPicture =  ImageTransformator.compressImage(picture);
				compressedPicture = ImageTransformator.applyFilters(compressedPicture);
                //String svgstring = ImageTracerAndroid.imageToSVG( compressedPicture , os.getvals(), null);
				svgString = ImageTracerAndroid.imageToSVG( compressedPicture , null, null);
                //ImageTracerAndroid.saveString(imageTracerAppFolder.getAbsolutePath()+"/"+timestamp()+".svg", svgstring );
                wv.loadDataWithBaseURL("", svgString, mimeType, encoding,"");
            } catch (Exception e) { Log.e(" Error tracing photo ", e.toString());}
            // Update UI to reflect image being shared
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {
			Bitmap picture = (Bitmap) data.getExtras().get("data");
            picture = ImageTransformator.applyFilters(picture);


            try {
				svgString = ImageTracerAndroid.imageToSVG( picture , null, null);
				int maxLogSize = 1000;
                for(int i = 0; i <= svgString.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i+1) * maxLogSize;
                    end = end > svgString.length() ? svgString.length() : end;
                    Log.v(TAG, svgString.substring(start, end));
                }

				wv.loadDataWithBaseURL("", svgString, mimeType, encoding,"");
				tts.speak("Sending image to laser plotter", TextToSpeech.QUEUE_FLUSH, null);

				sendImageToLaserPlotter(svgString);
			} catch (Exception e) { Log.e(" Error tracing photo ",e.toString());}
		} else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                Bitmap compressedPicture =  ImageTransformator.compressImage(picture);
                compressedPicture = ImageTransformator.applyFilters(compressedPicture);

				svgString = ImageTracerAndroid.imageToSVG(compressedPicture, null, null);

                wv.loadDataWithBaseURL("", svgString, mimeType, encoding,"");
				tts.speak("Sending image to laser plotter", TextToSpeech.QUEUE_FLUSH, null);

				sendImageToLaserPlotter(svgString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

	private void sendImageToLaserPlotter(String svgString){
		final String svgData = svgString;
		PrinterConnection.OnConnectionCallBack onConnectionCallBack = new PrinterConnection.OnConnectionCallBack() {
			@Override
			public void connectionEstablished() {
				Log.d(TAG, "connectionEstablished: ");
				sendCommands(printerConnector, svgData);
			}

			@Override
			public void connectionLost() {

			}

			@Override
			public void connectionRefused() {

			}

			@Override
			public void newCharsAvailable(byte[] c, int byteCount) {

			}
		};
		printerConnector = new PrinterConnector(PrinterConnector.Mode.TCP,"","192.168.42.132", 8090,
				getApplicationContext(),onConnectionCallBack);
		if (printerConnector.device == null || printerConnector.connection == null){
			printerConnector.initializeConnection();
		} else {
			if (!printerConnector.connection.isConnected()){
				printerConnector.initializeConnection();
			} else {
				sendCommands(printerConnector, svgString);
			}
		}


	}


	protected void sendCommands(PrinterConnector printerConnector, String svgString){
		parser = new SVGParser(svgString);
		ArrayList<Instruction> instructions = new ArrayList<>();
		parser.setInstructions(instructions);
		parser.startParsing();

		for (Instruction instruction : instructions){
			Log.d("Connection", "sending instruction " + instruction.buildInstruction(Instruction.Mode.GPGL));
			printerConnector.connection.write(instruction.buildInstruction(Instruction.Mode.GPGL));
		}
	}

    @Override
    public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		super.onRequestPermissionsResult(requestCode,permissions,grantResults);
//        switch (requestCode) {

            // other 'case' lines to check for other
            // permissions this app might request
        //}
    }




}