package de.hpi.hci.bachelorproject2016.fotoapptalkback;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnection;
import de.hpi.hci.bachelorproject2016.bluetoothlib.PrinterConnector;
import de.hpi.hci.bachelorproject2016.fotoapptalkback.ImageProcessing.ImageTracerAndroid;
import de.hpi.hci.bachelorproject2016.fotoapptalkback.ImageProcessing.ImageTransformator;
import de.hpi.hci.bachelorproject2016.speechlib.SingleSpeechRecognitionHandler;
import de.hpi.hci.bachelorproject2016.svgparser.Instruction;
import de.hpi.hci.bachelorproject2016.svgparser.SVGParser;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity { //implements SensorEventListener

    private static final int PICK_IMAGE_REQUEST = 2;
	private static final int CAMERA_REQUEST = 1;
	private static final int MY_PERMISSIONS_CAMERA_REQUEST = 100;
    private static final int MY_PERMISSIONS_EXTERNAL_STORAGE_REQUEST = 101;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 102;
    public static final String FIRST_INSTRUCTIONS = "FirstInstructions";
	public static final String CONNECTING = "connecting";
	public static final String ON_OPEN_CAMERA = "onOpenCamera";
	public static final String ON_OPEN_ALBUM = "onOpenAlbum";
    public static final int SLEEPTIME = 4000;



    public static boolean isVisible() {
		return isVisible;
	}

	public static void setIsVisible(boolean isVisible) {
		MainActivity.isVisible = isVisible;
	}

	protected static boolean isVisible = false;


	//Shake event handling
	/*private SensorManager mSensorManager;
	private float mAccel; // acceleration apart from gravity
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity
	private long timeOfLastShakeEvent = System.currentTimeMillis();
*/

	//UI
	Button takePictureBtn;
	Button pickPictureBtn;

	//SVG parsing
	//SVGParser parser;
//	String svgString = "";
	//PrinterConnector printerConnector;
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


	UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
		@Override
		public void onStart(String s) {

		}

		@Override
		public void onDone(String s) {
			if (s.equals(getString(R.string.taking_picture_please_wait_a_bit))) {
				Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
				cameraIntent.putExtra(Constants.TAKE_PICTURE_INSTANTLY, true);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
			}
		}

		@Override
		public void onError(String s) {

		}

		@Override
		public void onStop(String s, boolean interrupted){
			if (isVisible()) {
				/*if (s.equals(FIRST_INSTRUCTIONS)) {
                    tts.speak(getString(R.string.started_foto_app), TextToSpeech.QUEUE_ADD, null, utteranceId);
                } else*/
                if (s.equals(CONNECTING)) {
                    tts.speak(getString(R.string.connecting_laser_plotter), TextToSpeech.QUEUE_ADD, null, CONNECTING);
                } else if (s.equals(ON_OPEN_CAMERA)){
					tts.speak(getString(R.string.on_open_camera), TextToSpeech.QUEUE_FLUSH, null, ON_OPEN_CAMERA);
				} else if (s.equals(ON_OPEN_ALBUM)) {
                    tts.speak(getString(R.string.on_choose_picture), TextToSpeech.QUEUE_FLUSH, null, ON_OPEN_ALBUM);
                } else if (s.equals(getString(R.string.taking_picture_please_wait_a_bit))){
                    Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
                    cameraIntent.putExtra(Constants.TAKE_PICTURE_INSTANTLY, true);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
			}
		}
	};



	String mimeType = "text/html";
	String encoding = "utf-8";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_foto_app);

		// UI
		pickPictureBtn = (Button) findViewById(R.id.btn_pick_picture);
		takePictureBtn = (Button) findViewById(R.id.btn_take_picture);
		//helpBtn = (Button) findViewById(R.id.btn_help);

		pickPictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				pickPictureIntent();

			}
		});

		takePictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				openCamera();
			}
		});




		initTTS();

        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

		//Shake listener instantiation
		//mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		/*mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;*/




		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}


    private void checkPermission(String permission, int requestCode) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(getApplicationContext(),
					permission)
					!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission},
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
					tts.speak(getString(R.string.started_foto_app), TextToSpeech.QUEUE_ADD, null, FIRST_INSTRUCTIONS);

				} else {
					Log.e("TTS", "Initialization Failed!");
				}
			}
		});
	}

	public void onPause() {
		super.onPause();
		setIsVisible(false);
		//audioHandler.stopSpeechRecognition();
		//tts.speak("Fotoapp wird geschlossen", TextToSpeech.QUEUE_FLUSH,null,utteranceId);

	}

	public void onResume() {
		super.onResume();
		setIsVisible(true);
		//audioHandler.startSpeechRecognition();

	}

	public void onStart() {
		super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
        handleIncomingIntents();

// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.start(client, getIndexApiAction());
	}

	public void onDestroy() {
		super.onDestroy();
		stopTextToSpeech();
/*        if (printerConnector !=null){
            printerConnector.stopConnection();
        }*/
	}

	public void stopTextToSpeech() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}

	private void pickPictureIntent() {
		try {
			tts.speak(getString(R.string.on_choose_picture), TextToSpeech.QUEUE_FLUSH, null, ON_OPEN_ALBUM);
			Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
			getIntent.setType("image/*");

			Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			pickIntent.setType("image/*");

			//Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
			//chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            Thread.sleep(SLEEPTIME);
			startActivityForResult(pickIntent, PICK_IMAGE_REQUEST);
			//startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void takePictureIntent() {
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

                startPrintPreview(compressedPicture);

                //svgString = ImageTracerAndroid.imageToSVG(compressedPicture, null, null);

                //wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");
			} catch (Exception e) {
				Log.e(" Error tracing photo ", e.toString());
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {
			tts.speak(getString(R.string.took_picture), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
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
            startPrintPreview(picture);
		} else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

			Uri uri = data.getData();

			try {
				Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
				Bitmap compressedPicture = ImageTransformator.compressImage(picture);
				compressedPicture = ImageTransformator.applyFilters(compressedPicture);

                startPrintPreview(compressedPicture);
				/*svgString = ImageTracerAndroid.imageToSVG(compressedPicture, null, null);

				wv.loadDataWithBaseURL("", svgString, mimeType, encoding, "");*/
				tts.speak(getString(R.string.picked_picture), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
				//sendImageToLaserPlotter(svgString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    private void startPrintPreview(Bitmap picture) {
        Intent printPreviewIntent = new Intent(this,PrintPreviewActivity.class);
        printPreviewIntent.putExtra("IMAGE",picture);
        startActivity(printPreviewIntent);

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




	private void openCamera() {
		// Starting an Intent to take a picture
		tts.speak(getString(R.string.taking_picture_please_wait_a_bit), TextToSpeech.QUEUE_FLUSH, null, getString(R.string.taking_picture_please_wait_a_bit));

		//startActivity(cameraIntent);

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
/*
	@Override
	public void onSensorChanged(SensorEvent se) {
		float x = se.values[0];
		float y = se.values[1];
		float z = se.values[2];
		mAccelLast = mAccelCurrent;
		mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
		float delta = mAccelCurrent - mAccelLast;
		mAccel = mAccel * 0.9f + delta; // perform low-cut filter
		//Log.d("Shaking", "shaked " + mAccel);
		if (mAccel > 12 && System.currentTimeMillis() - timeOfLastShakeEvent >5000) {
			//Toast toast = Toast.makeText(getApplicationContext(), "Gerät wurde geschaket.", Toast.LENGTH_LONG);
			//toast.show();
			timeOfLastShakeEvent = System.currentTimeMillis();
			//giveHelp();
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
*/
	private void giveHelp() {
		tts.speak(getString(R.string.available_speech_commands), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
	}

}