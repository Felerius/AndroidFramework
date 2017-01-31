package de.hpi.hci.bachelorproject2016.fotoapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

<<<<<<< Updated upstream:fotoapp/src/main/java/de/hpi/hci/bachelorproject2016/fotoapp/FotoAppActivity.java
=======
import com.hci.bachelorproject.bluetoothlib.PrinterConnection;
import com.hci.bachelorproject.bluetoothlib.PrinterConnector;
import com.hci.bachelorproject.fotoapp.ImageProcessing.ImageTracerAndroid;
import com.hci.bachelorproject.fotoapp.ImageProcessing.ImageTransformator;
import com.hci.bachelorproject.speechlib.SpeechRecognitionHandler;

import java.io.File;
import java.io.IOException;
>>>>>>> Stashed changes:fotoapp/src/main/java/com/hci/bachelorproject/fotoapp/FotoAppActivity.java
import java.util.ArrayList;
import java.util.Locale;

import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnection;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnector;
import de.hpi.hci.bachelorproject2016.fotoapp.ImageProcessing.ImageTracerAndroid;
import de.hpi.hci.bachelorproject2016.fotoapp.ImageProcessing.ImageTransformator;
import de.hpi.hci.bachelorproject2016.speechlib.SpeechRecognitionHandler;
import de.hpi.hci.bachelorproject2016.svgparser.Instruction;
import de.hpi.hci.bachelorproject2016.svgparser.SVGParser;

import static android.content.ContentValues.TAG;

public class FotoAppActivity extends Activity implements SpeechRecognitionHandler.OnSpeechRecognizedListener{

    private static final int PICK_IMAGE_REQUEST = 2;
	private static final int CAMERA_REQUEST = 1;
	private static final int MY_PERMISSIONS_CAMERA_REQUEST = 100;
	private static final int MY_PERMISSIONS_EXTERNAL_STORAGE_REQUEST = 101;

	SVGParser parser ;
	String svgString = "";
	PrinterConnector printerConnector;

	public PrinterConnector.Mode getConnectionMode() {
		return connectionMode;
	}

	public void setConnectionMode(PrinterConnector.Mode connectionMode) {
		this.connectionMode = connectionMode;
	}

	PrinterConnector.Mode connectionMode = PrinterConnector.Mode.TCP;

    DisplayMetrics displaymetrics;
	private TextToSpeech tts;
	String utteranceId=this.hashCode() + "";
	SpeechRecognitionHandler audioHandler;
	WebView wv;

	String mimeType = "text/html";
	String encoding = "utf-8";
	String dir;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Window style and metrics
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
		File newdir = new File(dir);
		newdir.mkdirs();
        //checkWriteToStoragePermission();
		//checkPermission();
		audioHandler = new SpeechRecognitionHandler(getApplicationContext(),this);

		initTTS();



		// UI
		ScrollView sv = new ScrollView(getApplicationContext());

		// Layout
		LinearLayout ll = new LinearLayout(getApplicationContext());
		ll.setOrientation(LinearLayout.VERTICAL);
		sv.addView(ll);

		// WebView to show SVG
		wv = new WebView(getApplicationContext());
		ll.addView(wv);

		// Button 1
		Button b1 = new Button(getApplicationContext());
		b1.setText(R.string.take_a_picture_button_text);
		b1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				//checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,MY_PERMISSIONS_EXTERNAL_STORAGE_REQUEST);
				//checkPermission(Manifest.permission.CAMERA,MY_PERMISSIONS_CAMERA_REQUEST);
				takePictureIntent();
			}
		});
		ll.addView(b1);

		// Button 2
		Button b2 = new Button(getApplicationContext());
		b2.setText(R.string.pick_a_picture_button_text);
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


	}

	private void checkPermission(String permission, int requestCode) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(getApplicationContext(),
					permission)
					!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(FotoAppActivity.this, new String[]{permission},
							requestCode);

			}
		}
	}

	private void initTTS(){
		tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					Log.d("TTS","successfully set up text to speech");
					int result = tts.setLanguage(new Locale(Locale.getDefault().getISO3Language(),
							Locale.getDefault().getISO3Country()));

					if (result == TextToSpeech.LANG_MISSING_DATA
							|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
						Log.e("TTS", "This Language is not supported");
					}
					tts.speak(getString(R.string.started_foto_app) + getString(R.string.available_speech_commands) + getString(R.string.you_can_always_say_help), TextToSpeech.QUEUE_FLUSH, null,utteranceId);

					audioHandler.startSpeechRecognition();

				} else {
					Log.e("TTS", "Initilization Failed!");
				}
			}
		});
	}

	public void onPause(){
		super.onPause();
		audioHandler.stopSpeechRecognition();
		//tts.speak("Fotoapp wird geschlossen", TextToSpeech.QUEUE_FLUSH,null,utteranceId);

	}

	public void onResume(){
		super.onResume();
		audioHandler.startSpeechRecognition();

	}

	public void onStart(){
		super.onStart();
		initTTS();
	}

	public void onDestroy(){
		super.onDestroy();
		stopTextToSpeech();
	}

	public void stopTextToSpeech(){
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}

	private void pickPictureIntent() {
		try {
			tts.speak(getString(R.string.on_choose_picture), TextToSpeech.QUEUE_FLUSH, null,utteranceId);
			Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            //Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(pickIntent, PICK_IMAGE_REQUEST);
			//startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
        }catch(Exception e){ e.printStackTrace(); }
	}

	private void takePictureIntent() {
		try {
			// Starting an Intent to take a picture
			tts.speak(getString(R.string.on_open_camera), TextToSpeech.QUEUE_FLUSH, null,utteranceId);
			/*String file = dir+"test.jpg";
			File newfile = new File(file);

			try {
				newfile.createNewFile();
			}
			catch (IOException e)
			{
			}

			Uri outputFileUri = Uri.fromFile(newfile);
*/
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
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
                svgString = ImageTracerAndroid.imageToSVG( compressedPicture , null, null);
                wv.loadDataWithBaseURL("", svgString, mimeType, encoding,"");
            } catch (Exception e) { Log.e(" Error tracing photo ", e.toString());}

        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {

			Bitmap picture = (Bitmap) data.getExtras().get("data");
            Log.d("Camera request", "received img from cam");
			Log.d("Camera request", picture.toString());

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
				tts.speak(getString(R.string.took_picture), TextToSpeech.QUEUE_FLUSH, null,utteranceId);
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
				tts.speak(getString(R.string.picked_picture), TextToSpeech.QUEUE_FLUSH, null,utteranceId);
				sendImageToLaserPlotter(svgString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}


	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_CAMERA_REQUEST: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					if (ContextCompat.checkSelfPermission(getApplicationContext(),
							Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
						takePictureIntent();
					} else {
						checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_EXTERNAL_STORAGE_REQUEST);
					}
				}
				return;
			}
			case MY_PERMISSIONS_EXTERNAL_STORAGE_REQUEST: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					if (ContextCompat.checkSelfPermission(getApplicationContext(),
							Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
						takePictureIntent();
					} else {
						checkPermission(Manifest.permission.CAMERA,MY_PERMISSIONS_CAMERA_REQUEST);
					}
				}
				return;
			}
		}
	}


	private void sendImageToLaserPlotter(String svgString){
		final String svgData = svgString;
		PrinterConnection.OnConnectionCallBack onConnectionCallBack = new PrinterConnection.OnConnectionCallBack() {
			@Override
			public void connectionEstablished() {
				Log.d(TAG, "connectionEstablished: ");
				tts.speak(getString(R.string.sending_img_laser_plotter), TextToSpeech.QUEUE_ADD, null,utteranceId);
				sendCommands(printerConnector, svgData);
			}

			@Override
			public void connectionLost() {
				tts.speak(getString(R.string.lost_connection), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
			}

			@Override
			public void connectionRefused() {
				tts.speak(getString(R.string.could_not_connect), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
			}

			@Override
			public void newCharsAvailable(byte[] c, int byteCount) {

			}
		};
		printerConnector = new PrinterConnector(PrinterConnector.Mode.TCP,getString(R.string.bluetooth_device_name),"192.168.42.132", 8090,
				getApplicationContext(),onConnectionCallBack);
		if (printerConnector.device == null || printerConnector.connection == null){
			tts.speak(getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
			printerConnector.initializeConnection();
		} else {
			if (!printerConnector.connection.isConnected()){
				tts.speak(getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				printerConnector.initializeConnection();
			} else {
				tts.speak(getString(R.string.sending_img_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				sendCommands(printerConnector, svgString);
			}
		}
	}


	protected void sendCommands(PrinterConnector printerConnector, String svgString){
		//parser = new SVGParser(svgString,true);
		parser = new SVGParser(svgString, new PointF(20,20));
		ArrayList<Instruction> instructions = new ArrayList<>();
		parser.setInstructions(instructions);
		parser.startParsing();

		for (Instruction instruction : instructions){
			Log.d("Connection", "sending instruction " + instruction.buildInstruction(Instruction.Mode.GPGL));
			printerConnector.connection.write(instruction.buildInstruction(Instruction.Mode.GPGL));
		}
	}


	@Override
	public void onSpeechRecognized(String message) {
		Log.d("receiver", "Got message: " + message);
		switch (message) {
			case "take picture":
			case "Foto machen":
			case "take a picture":
			case "Foto schießen":
			case "Foto aufnehmen":
				//checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,MY_PERMISSIONS_EXTERNAL_STORAGE_REQUEST);
				//checkPermission(Manifest.permission.CAMERA,MY_PERMISSIONS_CAMERA_REQUEST);
				takePictureIntent();
				return;
			case "choose picture":
			case "pick picture":
			case "Foto auswählen":
			case "choose a picture":
			case "pick a picture":
				pickPictureIntent();
				return;
			case "set mode bluetooth":
				setConnectionMode(PrinterConnector.Mode.BLUETOOTH);
				tts.speak("Setting mode to bluetooth", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				break;
			case "set mode tcp":
			case "set mode debug":
			case "set mode debugging":
				setConnectionMode(PrinterConnector.Mode.TCP);
				tts.speak("Setting mode to debug", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				break;
			case "help":
			case "options":
			case "hilfe":
			case "Optionen":
				tts.speak(getString(R.string.available_speech_commands), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				break;
			default:
				//tts.speak("Das wurde nicht richtig verstanden", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				Log.d("receiver", "no action recognized");
				break;

		}
		/*if (message.contains("Foto") || message.contains("picture")){
			tts.speak("Das wurde nicht richtig verstanden", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
		}*/

	}


}