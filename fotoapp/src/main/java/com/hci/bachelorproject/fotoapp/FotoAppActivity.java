package com.hci.bachelorproject.fotoapp;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
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

import com.hci.bachelorproject.fotoapp.ImageProcessing.ImageTracerAndroid;
import com.hci.bachelorproject.fotoapp.ImageProcessing.ImageTransformator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kuchinke.com.svgparser.Instruction;
import kuchinke.com.svgparser.SVGParser;

import static android.content.ContentValues.TAG;


public class FotoAppActivity extends AudioActivity {

    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int SPEECH_INPUT_REQUEST = 100;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;

	protected SpeechRecognizer sr;
	SVGParser parser ;
	String svgString = "";
	protected Context context;

    private String[] storage_permissions =
            {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    File imageTracerAppFolder; // sdcard/ImageTracerApp/ directory
	DisplayMetrics displaymetrics;

	WebView wv;

	private static final int CAMERA_REQUEST = 1888; // field
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

		//context = getApplicationContext(); // Needs to be set
		//VoiceRecognitionListener.getInstance().setListener(this); // Here we set the current listener

        checkWriteToStoragePermission();


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


                /*try {

					// Tracing panda.png to an SVG string
					String svgstring = ImageTracerAndroid.imageToSVG( imageTracerAppFolder.getAbsolutePath()+"/"+"panda.png" , null, null);

					// Saving SVG string as panda.svg
					ImageTracerAndroid.saveString(imageTracerAppFolder.getAbsolutePath()+"/"+"panda.svg", svgstring );

					// Displaying SVG in the WebView
					wv.loadDataWithBaseURL("", svgstring, mimeType, encoding,"");

				}catch(Exception e){ log(" Error tracing panda.png "+e.toString()); e.printStackTrace(); }*/


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


	@Override
	public void connectionEstablished() {
		Log.d(TAG, "connectionEstablished: ");
		sendCommands(svgString);
	}




	private void handleIncomingIntents(){
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendText(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				Log.i(TAG,"received image");
				handleSendImage(intent); // Handle single image being sent
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				handleSendMultipleImages(intent); // Handle multiple images being sent
			}
		} else {
			// Handle other intents, such as being started from the home screen
		}
	}

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
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
            } catch (Exception e) { log(" Error tracing photo "+e.toString()); e.printStackTrace(); }
            // Update UI to reflect image being shared
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }
/*
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Please speak");
        try {
            startActivityForResult(intent, SPEECH_INPUT_REQUEST);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Speech not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }*/


    private void checkWriteToStoragePermission(){
        if ((int) Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.i(TAG, "in if ");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                        builder.setMessage("To get storage access you have to allow us access to your sd card content.");
                        builder.setTitle("Storage");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(FotoAppActivity.this, storage_permissions, 0);

                            }
                        });

                        builder.show();
                    } else {
                        ActivityCompat.requestPermissions(this, storage_permissions, 0);

                    }
                } else {
                    ActivityCompat.requestPermissions(FotoAppActivity.this,
                            storage_permissions,
                            MY_PERMISSIONS_REQUEST_STORAGE);

                }

            } else {
                checkassets();
            }
        }

    }


	public void log(String msg){ System.out.println(msg); }

	private void checkassets(){
		// Creating App folder if it does not
		imageTracerAppFolder = new File((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).getAbsolutePath()+"/ImageTracerApp/");
		if(!imageTracerAppFolder.exists()){
			log("Creating: "+imageTracerAppFolder.getAbsolutePath());
			imageTracerAppFolder.mkdirs();
		}

		AssetManager assetManager = getAssets();
		String[] filelist = {"panda.png","smiley.png"};

		// filelist check then copy
		if (filelist != null){

			byte[] buffer = new byte[16*1024];
			InputStream in = null;
			OutputStream out = null;
			int read;

			// Copying files
			for (String filename : filelist) {
				try {
					File outfile = new File(imageTracerAppFolder, filename);
					if(!outfile.exists()){
						in = assetManager.open(filename);
						out = new FileOutputStream(outfile);
						while((read = in.read(buffer)) != -1){ out.write(buffer, 0, read); }
						log("Successfully copied: "+filename);
					}
				}catch(Exception e){
					log("!!!! ERROR: failed to copy: "+filename+" "+e.toString());
				}finally{
					if(in != null){try{ in.close();}catch(Exception e){}}
					if(out!= null){try{out.close();}catch(Exception e){}}
				}
			}// End of file loop
		}// End of filelist check

	}// End of checkassets()



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {
			Bitmap picture = (Bitmap) data.getExtras().get("data");
            //picture = ImageTransformator.fastblur(picture,1,3);
            //picture = ImageTransformator.toGrayscale(picture);
            picture = ImageTransformator.applyFilters(picture);


            try {
				svgString = ImageTracerAndroid.imageToSVG( picture , null, null);
				//ImageTracerAndroid.saveString(imageTracerAppFolder.getAbsolutePath()+"/"+timestamp()+".svg", svgstring );
                /*String[] svgElements = svgstring.split("><");
                for (String svgElement : svgElements){
                    System.out.println(svgElement);
                }*/
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
			} catch (Exception e) { log(" Error tracing photo "+e.toString()); e.printStackTrace(); }
		} else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                Bitmap compressedPicture =  ImageTransformator.compressImage(picture);
                compressedPicture = ImageTransformator.applyFilters(compressedPicture);

				svgString = ImageTracerAndroid.imageToSVG(compressedPicture, null, null);
                //ImageTracerAndroid.saveString(imageTracerAppFolder.getAbsolutePath()+"/"+timestamp()+".svg", svgstring );
                // Log.d(TAG, String.valueOf(bitmap));

                wv.loadDataWithBaseURL("", svgString, mimeType, encoding,"");
				tts.speak("Sending image to laser plotter", TextToSpeech.QUEUE_FLUSH, null);

				sendImageToLaserPlotter(svgString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } /*else if (requestCode == SPEECH_INPUT_REQUEST){
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.i(TAG, result.get(0));

                } else {
                    promptSpeechInput();
                }
        }*/
	}

	private void sendImageToLaserPlotter(String svgString){
		if (device == null || connection == null){
			initializeConnection(Mode.TCP);
			//checkCoarseLocationPermission();
		} else {
			if (!connection.isConnected()){
				initializeConnection(Mode.TCP);
				//checkCoarseLocationPermission();
			} else {
				sendCommands(svgString);
			}
		}


	}


	protected void sendCommands(String svgString){
		parser = new SVGParser(svgString);
		ArrayList<Instruction> instructions = new ArrayList<>();
		parser.setInstructions(instructions);
		parser.startParsing();

		for (Instruction instruction : instructions){
			Log.d("Connection", "sending instruction " + instruction.buildInstruction(Instruction.Mode.GPGL));
			connection.write(instruction.buildInstruction(Instruction.Mode.GPGL));
		}
	}

    @Override
    public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkassets();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

	public String timestamp(){ return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date()); }




}// End of ImagetracerActivity