package de.hpi.hci.bachelorproject2016.fotoapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnection;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnector;
import de.hpi.hci.bachelorproject2016.fotoapp.ImageProcessing.ImageTracerAndroid;
import de.hpi.hci.bachelorproject2016.fotoapp.ImageProcessing.ImageTransformator;
import de.hpi.hci.bachelorproject2016.speechlib.SingleSpeechRecognitionHandler;
import de.hpi.hci.bachelorproject2016.speechlib.SpeechRecognitionHandler;
import de.hpi.hci.bachelorproject2016.svgparser.Instruction;
import de.hpi.hci.bachelorproject2016.svgparser.SVGParser;

import static android.content.ContentValues.TAG;

public class FotoAppActivity extends Activity implements SpeechRecognitionHandler.OnSpeechRecognizedListener, SensorEventListener {

	private static final int PICK_IMAGE_REQUEST = 2;
	private static final int CAMERA_REQUEST = 1;
	private static final int MY_PERMISSIONS_CAMERA_REQUEST = 100;
	private static final int MY_PERMISSIONS_EXTERNAL_STORAGE_REQUEST = 101;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 102;
	public static final int REPEAT_INSTRUCTIONS_TIME = 30000;
	public static final String FIRST_INSTRUCTIONS = "FirstInstructions";
	public static final String CONNECTING = "connecting";
	private static final String TOOK_PHOTO = "tookPhoto";

	public static boolean isVisible() {
		return isVisible;
	}

	public static void setIsVisible(boolean isVisible) {
		FotoAppActivity.isVisible = isVisible;
	}


	boolean inPrintPreviewMode = false;
	WebView wv;


	boolean takePictureInstantly = false;


	//Shake event handling
	private float mAccel; // acceleration apart from gravity
	private SensorManager mSensorManager;
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity
	private long timeOfLastShakeEvent = System.currentTimeMillis();
	//Volume button event handling
	//VolumeButtonObserver mContentObserver;

	/*private final Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			Log.i("resp", "volume changed");
			if (speechHandler != null) {
				activateSpeechInput();

			}


		}
	};*/

	private void activateSpeechInput() {
		if (speechHandler != null) {
			speechHandler.startSingleSpeechRecognition();
		}
		if (tts!= null){
			tts.stop();
		}
		firstActivatedSpeechInput = true;
	}

	protected static boolean isVisible = false;

	//SVG parsing
	SVGParser parser;
	String svgString = "";
	PrinterConnector printerConnector;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	public PrinterConnector.Mode getConnectionMode() {
		return connectionMode;
	}

	public void setConnectionMode(PrinterConnector.Mode connectionMode) {
		this.connectionMode = connectionMode;
	}

	PrinterConnector.Mode connectionMode = PrinterConnector.Mode.BLUETOOTH;

	DisplayMetrics displaymetrics;
	private TextToSpeech tts;
	String utteranceId = this.hashCode() + "";
	//SpeechRecognitionHandler audioHandler;
	SingleSpeechRecognitionHandler speechHandler;
	private boolean firstActivatedSpeechInput = false;


	UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
		@Override
		public void onStart(String s) {

		}

		@Override
		public void onDone(String s) {
			if (s.equals(getString(R.string.taking_picture_please_wait_a_bit))) {
				openCameraIntent();
			} else if (s.equals(TOOK_PHOTO) || s.equals(getString(R.string.picked_picture))){
				wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");
			}

		}

		@Override
		public void onError(String s) {

		}

		@Override
		public void onStop(String s, boolean interrupted){
			if (isVisible()) {
				if (s.equals(FIRST_INSTRUCTIONS)) {
					tts.speak(getString(R.string.started_foto_app) + getString(R.string.you_can_always_say_help), TextToSpeech.QUEUE_ADD, null, FIRST_INSTRUCTIONS);
				} else if (s.equals(CONNECTING)) {
					tts.speak(getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_ADD, null, utteranceId);
				} else if (s.equals(getString(R.string.taking_picture_please_wait_a_bit))) {
					tts.speak(s, TextToSpeech.QUEUE_ADD, null, s);
				} else if (s.equals(TOOK_PHOTO) || s.equals(getString(R.string.picked_picture))){
					tts.speak(getString(R.string.printing_duration), TextToSpeech.QUEUE_FLUSH, null, s);
				}
			}
		}
	};



	String mimeType = "text/html";
	String encoding = "utf-8";
	String dir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_foto_app);
		// Window style and metrics
		/*requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);*/
		//displaymetrics = new DisplayMetrics();
		//getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);


		//Shake listener instantiation
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;

		//Volume button listener instantiation
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-5, AudioManager.FLAG_VIBRATE);
		firstActivatedSpeechInput = false;
		//checkWriteToStoragePermission();
		//checkPermission();
		//audioHandler = new SpeechRecognitionHandler(getApplicationContext(),this);

		initTTS();

        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);


        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				if (firstActivatedSpeechInput == false) {
					tts.speak(getString(R.string.started_foto_app) + getString(R.string.available_speech_commands) + getString(R.string.you_can_always_say_help), TextToSpeech.QUEUE_ADD, null, FIRST_INSTRUCTIONS);

				}
			}
		}, REPEAT_INSTRUCTIONS_TIME, TimeUnit.MILLISECONDS);


		wv = (WebView) findViewById(R.id.web_view);


		handleIncomingIntents();


		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

	private void initTTS() {
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
					tts.speak(getString(R.string.started_foto_app) + getString(R.string.available_speech_commands) + getString(R.string.you_can_always_say_help), TextToSpeech.QUEUE_ADD, null, FIRST_INSTRUCTIONS);

					//audioHandler.startSpeechRecognition();

				} else {
					Log.e("TTS", "Initilization Failed!");
				}
			}
		});
	}

	public void onPause() {
		super.onPause();
		setIsVisible(false);
		mSensorManager.unregisterListener(this);
		speechHandler.destroySpeechRecognizer();

	}

	public void onResume() {
		super.onResume();
		setIsVisible(true);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		speechHandler = new SingleSpeechRecognitionHandler(getApplicationContext(), this);

	}

	public void onStart() {
		super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.start(client, getIndexApiAction());
	}

	public void onDestroy() {
		super.onDestroy();
		//getApplicationContext().getContentResolver().unregisterContentObserver(mContentObserver);
		stopTextToSpeech();
	}

	public void stopTextToSpeech() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}

	private void pickPictureIntent() {
		try {
			tts.speak(getString(R.string.on_choose_picture), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
			Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
			getIntent.setType("image/*");

			Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			pickIntent.setType("image/*");

			//Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
			//chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

			startActivityForResult(pickIntent, PICK_IMAGE_REQUEST);
			//startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void takePictureIntent() {
		takePictureInstantly = true;
		openCamera();
	}


	private void handleIncomingIntents() {
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				Log.e("FotoApp", "only accepting single image from other apps");
			} else if (type.startsWith("image/")) {
				Log.i(TAG, "received image");
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
				Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
				Bitmap compressedPicture = ImageTransformator.compressImage(picture);
				compressedPicture = ImageTransformator.applyFilters(compressedPicture);
				svgString = ImageTracerAndroid.imageToSVG(compressedPicture, null, null);
				tts.speak(getString(R.string.picked_picture) + getString(R.string.printing_duration), TextToSpeech.QUEUE_FLUSH,null,getString(R.string.picked_picture));

				wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");
			} catch (Exception e) {
				Log.e(" Error tracing photo ", e.toString());
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {
			tts.speak(getString(R.string.took_picture) + getString(R.string.printing_duration), TextToSpeech.QUEUE_FLUSH, null, TOOK_PHOTO);
			Bitmap picture = null;
			if (data.getData() != null){
				Log.d("FotoAppActivity", "resolving uri");
				Uri uri = data.getData();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				try {
					picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
					picture = ImageTransformator.compressImage(picture);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				picture = (Bitmap) data.getExtras().get("data");
			}

			Log.d("Camera request", "received img from cam");
			Log.d("Camera request", picture.toString());

			picture = ImageTransformator.applyFilters(picture);


			try {
				svgString = ImageTracerAndroid.imageToSVG(picture, null, null);
				int maxLogSize = 1000;
				for (int i = 0; i <= svgString.length() / maxLogSize; i++) {
					int start = i * maxLogSize;
					int end = (i + 1) * maxLogSize;
					end = end > svgString.length() ? svgString.length() : end;
					Log.v(TAG, svgString.substring(start, end));
				}

				//wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");
				startPrintPreviewMode();
				//sendImageToLaserPlotter(svgString);
			} catch (Exception e) {
				Log.e(" Error tracing photo ", e.toString());
			}
		} else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

			Uri uri = data.getData();

			try {
				Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
				Bitmap compressedPicture = ImageTransformator.compressImage(picture);
				compressedPicture = ImageTransformator.applyFilters(compressedPicture);

				svgString = ImageTracerAndroid.imageToSVG(compressedPicture, null, null);

				//wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");
				tts.speak(getString(R.string.picked_picture) + getString(R.string.printing_duration), TextToSpeech.QUEUE_FLUSH, null, getString(R.string.picked_picture));
				startPrintPreviewMode();
				//sendImageToLaserPlotter(svgString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void startPrintPreviewMode() {
		inPrintPreviewMode = true;
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
							Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
							Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
						takePictureIntent();
					} else {
						checkPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_CAMERA_REQUEST);
					}
				}
				return;
			}
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                    }
                }
                return;
            }
		}
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
		if (printerConnector.device == null || printerConnector.getConnection() == null) {
			tts.speak(getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_ADD, null, CONNECTING);
			printerConnector.initializeConnection();
		} else {
			if (!printerConnector.getConnection().isConnected()) {
				tts.speak(getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				printerConnector.initializeConnection();
			} else {
				tts.speak(getString(R.string.sending_img_laser_plotter), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				sendCommands(printerConnector, svgString);
			}
		}
	}


	protected void sendCommands(PrinterConnector printerConnector, String svgString) {
		//parser = new SVGParser(svgString,true);
		Log.d(TAG,"Sending commands");
		parser = new SVGParser(svgString, new PointF(20, 20));
		ArrayList<Instruction> instructions = new ArrayList<>();
		parser.setInstructions(instructions);
		parser.startParsing();

		for (Instruction instruction : instructions) {
			Log.d("Connection", "sending instruction " + instruction.buildInstruction(Instruction.Mode.GPGL));
			printerConnector.getConnection().write(instruction.buildInstruction(Instruction.Mode.GPGL));
		}
	}


	@Override
	public void onSpeechRecognized(String[] messages) {
		parseSpeechInput(messages);
	}

	private void parseSpeechInput(String[] messages) {
		Log.d("receiver", "Got message: " + messages);
		if (!inPrintPreviewMode) {
			for (String message : messages) {
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
					case "Open camera":
					case "Öffne Camera":
						takePictureInstantly = false;
						openCamera();
					case "choose picture":
					case "pick picture":
					case "Foto auswählen":
					case "choose a picture":
					case "pick a picture":
						pickPictureIntent();
						return;
					case "set mode bluetooth":
					case "mode bluetooth":
					case "bluetooth mode":
						setConnectionMode(PrinterConnector.Mode.BLUETOOTH);
						tts.speak("Setting mode to bluetooth", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
						return;
					case "set mode tcp":
					case "set mode debug":
					case "set mode debugging":
						setConnectionMode(PrinterConnector.Mode.TCP);
						tts.speak("Setting mode to debug", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
						return;
					case "help":
					case "options":
					case "hilfe":
					case "Optionen":
						tts.speak(getString(R.string.available_speech_commands), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
						return;
					default:
						Log.d("receiver", "no action recognized");
						break;

				}
			}
		} else {
			for (String message : messages) {
				switch (message) {
					case "ja":
					case "yes":
					case "drucken":
						inPrintPreviewMode = false;
						sendImageToLaserPlotter(svgString);
						return;
					case "nein":
					case "zurück":
					case "back":
					case "no":
						inPrintPreviewMode = false;
						tts.speak(getString(R.string.started_foto_app) + getString(R.string.available_speech_commands), TextToSpeech.QUEUE_ADD, null, FIRST_INSTRUCTIONS);
						return;
					case "help":
					case "options":
					case "hilfe":
					case "Optionen":
						tts.speak("Verfügbare Kommandos sind 'drucken' und 'zurück'", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
						return;
					default:
						Log.d("receiver", "no action recognized");
						break;
				}
			}
		}
		tts.speak("Das wurde nicht richtig verstanden. " + getString(R.string.available_speech_commands) , TextToSpeech.QUEUE_FLUSH, null, utteranceId);


		/*if (message.contains("Foto") || message.contains("picture")){
			tts.speak("Das wurde nicht richtig verstanden", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
		}*/
	}



	private void openCameraIntent(){
		Intent cameraIntent = new Intent(this,CameraActivity.class);
		cameraIntent.putExtra(Constants.TAKE_PICTURE_INSTANTLY, true);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}


	private void openCamera() {
		// Starting an Intent to take a picture
		if (takePictureInstantly==true){

			tts.speak(getString(R.string.taking_picture_please_wait_a_bit), TextToSpeech.QUEUE_FLUSH, null, getString(R.string.taking_picture_please_wait_a_bit));
		} else {

			tts.speak(getString(R.string.on_open_camera), TextToSpeech.QUEUE_FLUSH, null, getString(R.string.on_open_camera));
		}
	}


	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	public Action getIndexApiAction() {
		Thing object = new Thing.Builder()
				.setName("FotoApp Page") // TODO: Define a title for the content shown.
				// TODO: Make sure this auto-generated URL is correct.
				.setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
				.build();
		return new Action.Builder(Action.TYPE_VIEW)
				.setObject(object)
				.setActionStatus(Action.STATUS_TYPE_COMPLETED)
				.build();
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.end(client, getIndexApiAction());
		client.disconnect();
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		float x = se.values[0];
		float y = se.values[1];
		float z = se.values[2];
		mAccelLast = mAccelCurrent;
		mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
		float delta = mAccelCurrent - mAccelLast;
		mAccel = mAccel * 0.9f + delta; // perform low-cut filter
		if (mAccel > 12 && System.currentTimeMillis() - timeOfLastShakeEvent >5000) {
			timeOfLastShakeEvent = System.currentTimeMillis();
				activateSpeechInput();
			}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
}