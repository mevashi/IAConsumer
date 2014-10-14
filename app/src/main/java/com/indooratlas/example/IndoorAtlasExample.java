package com.indooratlas.example;

import com.indooratlas.android.*;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import java.util.Vector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/*
 * IndoorAtlas API example
 */
public class IndoorAtlasExample extends Activity implements IndoorAtlasListener {

	private static final String TAG = "IndoorAtlasExample";

	private TextView textView;
	private Handler handler = new Handler();
	private IndoorAtlas indoorAtlas;
	//Fields to save fence info
    private TextView fenceText;
    private TextView radiusText;
    private TextView msgText;
    private Button saveFenceInfoButton;
    private EditText fenceEditTxt;
    private EditText radiusEditTxt;
    private EditText msgEditTxt;
    private Button switchButton;

	private boolean positioningOngoing = false;

  	// Get these from MyAtlas at www.indooratlas.com
    private final String apiKey = "0efd8696-5e2f-423e-8d3a-03dd72fa94d2" ; //"Your Apikey here";
    private final String secretKey = "QlivJz2D)4j((DWT(Non8d21D5SsdyRZW6Bqs51mC!dAbeGXnJhGnXw420buFqodsR0rRFfi6bfgzSOPOdWVUi9(XKvFHBiJ4eYFEHvEaLaGytq0Ojl2Ah1pDMs)%UKE"; //"Your Secret key here";
	
	// Get these from the Floor Plans tool at www.indooratlas.com
    private final String buildingId = "b6f2b066-d5d4-4150-8b9a-4a1abe5817b6"; //"Building Id here";
    private final String levelId = "42ac4d56-3a36-478f-81f4-83c13d3ace7c ";//"Level Id here";
    private final String floorPlanId = "e92020a4-5240-4c02-b5bf-6ed1894afe6b"; // "Floor plan Id here";

	private long lastPositionTimestamp = 0;
    private String appMode = "merchant";
   // Vector<VirtualFence> fences = new Vector<VirtualFence>();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		textView = (TextView) findViewById(R.id.textView1);
        //Save fence info
        /*fenceText = (TextView) findViewById(R.id.fenceTextView);
        radiusText = (TextView) findViewById(R.id.radiusTextView);
        msgText = (TextView) findViewById(R.id.msgTextView);
        fenceEditTxt = (EditText) findViewById(R.id.fenceName);
        radiusEditTxt = (EditText) findViewById(R.id.radius);
        msgEditTxt = (EditText) findViewById(R.id.message);
        saveFenceInfoButton = (Button) findViewById(R.id.saveButton);
        switchButton = (Button) findViewById(R.id.switchButton);
        // Get the message from the intent
        Intent intent = getIntent();
        appMode = intent.getStringExtra("APP_MODE");*/


        try {
			
			// Get handle to the IndoorAtlas API
			// Note that this method should be called as early as possible in the application, because
			// the calibration process starts immediately at API creation and is thus likely to finish
			// by the time positioning is started by the user or the application.
			
			// Throws exception when the cloud service cannot be reached
			// Get your Apikey and SecreSit key from IndoorAtlas My Account
			
			indoorAtlas = IndoorAtlasFactory.createIndoorAtlas(
					this.getApplicationContext(), 
					this, 
					apiKey,
					secretKey);

			Log.d(TAG, "onCreate created IndoorAtlas");
			
			
			
		} catch (IndoorAtlasException ex) {
			showMessageOnUI("Failed to connect to IndoorAtlas. Check your credentials.");
			Toast.makeText(this, "Failed to connect to IndoorAtlas. Check your credentials.", Toast.LENGTH_LONG).show();
			Log.e(TAG, "Failed to connect to IndoorAtlas. Check your credentials.");
			
			// Stop all API processes
			if (indoorAtlas != null) indoorAtlas.tearDown();
			
			// stop application
			this.finish();
		}
		

		Log.d(TAG, "onCreate done.");


	}
	
	@Override
	protected void onResume() 
	{
		Log.d(TAG, "onResume() -State- : calibrated = "+indoorAtlas.isCalibrationReady());
		super.onResume();
		
		// After installation of the application, IndoorAtlas API does not have calibration data, and an exception will be thrown
		// if startPositioning() is called at this state. Thus, at first use, the application should guide the user to perform 
		// calibration by moving the device as instructed in the IndoorAtlas Mobile application. Positioning can be started only 
		// after onCalibrationReady() has been called. The calibration will be stored by the API, and on consecutive starts 
		// onCalibrationReady() call immediately follows the call on createIndoorAtlas().
		if(indoorAtlas.isCalibrationReady() == false) {
			// Prompts user to perform calibration motion
			showMessageOnUI("onResume(): Calibrating... Rock your phone gently until onCalibrationReady() is called");
		}
		
		else if(positioningOngoing == false) {			
			showMessageOnUI("onResume(): Starting positioning.");
			
			try {
				positioningOngoing = true;
				
				// Throws an exception if no calibration is done
				indoorAtlas.startPositioning(buildingId, levelId, floorPlanId);
				
			} catch (IndoorAtlasException e) {
				positioningOngoing = false;
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void onStop() {

		Log.d(TAG, "onStop() -State- : calibrated = "+indoorAtlas.isCalibrationReady());

		try {
			//showMessageOnUI("onStop(): Stopping positioning.");

			// Stop positioning when not needed
			indoorAtlas.stopPositioning();
			
			// Stop all API processes
			indoorAtlas.tearDown();

		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onStop();
	}

	@Override
	protected void onPause() 
	{
		Log.d(TAG, "onPause() -State- : calibrated = "+indoorAtlas.isCalibrationReady());
		
		//showMessageOnUI("onPause(): Stopping positioning.");

		// Stop positioning when not needed
		//indoorAtlas.stopPositioning();

		super.onPause();
	}
	
	@Override
	protected void onRestart()
	{
		Log.d(TAG, "onRestart() -State- : calibrated = "+indoorAtlas.isCalibrationReady());

		super.onRestart();
		
		showMessageOnUI("onRestart().");
	}
	
	
	
	// Called on every new location estimate. 
	// Note that when device is not moving, frequency of callbacks may decrease.
	public void onServiceUpdate(final ServiceState state) {

		Log.d(TAG, "onServiceUpdate()");
        //Log.d(TAG, "elements in fences" + fences.isEmpty());
		
		long curTime = SystemClock.elapsedRealtime();
		long diff = curTime - lastPositionTimestamp;
		lastPositionTimestamp = curTime;

        Log.d(TAG, "Customer Mode");
        String msg = "";

        String URL = "content://com.indooratlas.setup.provider/fences";
        Uri fences = Uri.parse(URL);
        Log.d(TAG, "Parsed uri");
        String[] mProjection =
                {
                        "METER_X",    // Contract class constant for the _ID column name
                        "METER_Y",   // Contract class constant for the word column name
                        "RADIUS",
                        "NAME",
                        "MESSAGE"  // Contract class constant for the locale column name
                };
//        String[] mSelectionArgs = {""};
        Log.d(TAG,"fences" + fences);

        /*ContentProviderClient yourCR = getContentResolver().acquireContentProviderClient(fences );
        if(yourCR == null)
            Log.d(TAG, "yourCR null");
        Log.d(TAG, "yourCR not null");*/
        ContentResolver cr = getContentResolver();
        if(cr == null) {
            Log.d(TAG, "yourCR null");
        }
        Log.d(TAG, "yourCR is not null");
        Cursor c = null;

        try {
            c = cr.query(fences, mProjection, null, null, null);
        }
        catch(Exception e) {
           Log.d(TAG,"Exception");
            Log.d(TAG, e.getClass().getName());
            Log.d(TAG, e.getMessage());
            Log.d(TAG, e.getStackTrace().toString());
        }
        if(c != null)
            Log.d(TAG, "Cursor" + c);
        else
            Log.d(TAG, "Null cursor");
        if (!c.moveToFirst()) {
            Log.d(TAG, "No content yet");
            Toast.makeText(this, " no content yet!", Toast.LENGTH_LONG).show();
        }else{
            do{
                Log.d(TAG, "Found content");
                double XVal = Double.valueOf(c.getString(c.getColumnIndex("METER_X"))).doubleValue();
                double YVal = Double.valueOf(c.getString(c.getColumnIndex("METER_Y"))).doubleValue();
                double RadiusVal = Double.valueOf(c.getString(c.getColumnIndex("RADIUS"))).doubleValue();

                Log.d(TAG, "current X and Y " + XVal + " " + YVal);
                double upperX = RadiusVal + XVal;
                double upperY = RadiusVal + YVal;
                double lowerX = XVal - RadiusVal;
                double lowerY = YVal - RadiusVal;

                double currentX =  state.getMetricPoint().getX();
                double currentY =  state.getMetricPoint().getY();
                if(  currentX > lowerX && currentX < upperX &&
                        currentY > lowerY &&  currentY < upperY) {
                    Log.d(TAG, "Int the fence I am");
                    msg = c.getString(c.getColumnIndex("MESSAGE"));
                    Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(2000);
                    showMessageOnUI(msg);
                    break;
                }

            } while (c.moveToNext());
            Log.d(TAG, "Out of the loop");
            //Toast.makeText(this, result, Toast.LENGTH_LONG).show();
         }
        Log.d(TAG, "End");
        //showMessageOnUI(msg);

	}

	// Communication with IndoorAtlas has failed.
	public void onServiceFailure(int errorCode, String reason) {
		Log.d(TAG, "onServiceFailure()");

		switch (errorCode)
		{

		case ErrorCodes.NO_NETWORK:
			showMessageOnUI("onServiceFailure(): No network connections.");
			break;
			
		case ErrorCodes.SENSOR_ERROR:
			showMessageOnUI("onServiceFailure(): Sensor error.");
			break;
			
		case ErrorCodes.LOW_SAMPLING_RATE:
			showMessageOnUI("onServiceFailure(): Too low sampling rate in sensor(s) for reliable positioning.");
			break;
	
		case ErrorCodes.NO_CONNECTION_TO_POSITIONING:
			showMessageOnUI("onServiceFailure(): Connection to positioning service could not be established.");
			break;
			
		case ErrorCodes.INTERNAL_POSITIONING_SERVICE_ERROR:
			showMessageOnUI("onServiceFailure(): Internal positioning service error.");
			break;

		case ErrorCodes.VERSION_MISMATCH:
			showMessageOnUI("onServiceFailure(): API version is not supported.");
			break;

		case ErrorCodes.POSITIONING_SESSION_TIMEOUT:
			showMessageOnUI("onServiceFailure(): Session has timed out.");
			break;
			
		case ErrorCodes.POSITIONING_DENIED:
			showMessageOnUI("onServiceFailure(): Positioning permission denied.");
			break;

		case ErrorCodes.NO_POSITIONING_TIME_LEFT:
			showMessageOnUI("onServiceFailure(): No positioning time left.");
			break;
			
		case ErrorCodes.MAP_NOT_FOUND:
			showMessageOnUI("onServiceFailure(): Positioning service could not retrieve map.");
			break;
			
		case ErrorCodes.NOT_SUPPORTED:
			showMessageOnUI("onServiceFailure(): Selected motion mode is not supported by positioning service.");
			break;

		default:
			showMessageOnUI("onServiceFailure(): Unexpected error: " + reason);
			break;
			
		}
	}

	// Initializing location service
	public void onServiceInitializing() {
		Log.d(TAG, "onServiceInitializing()");
		showMessageOnUI("onServiceInitializing()");
	}

	// Initialization completed
	public void onServiceInitialized() {
		Log.d(TAG, "onServiceInitialized()");
		showMessageOnUI("onServiceInitialized(): Walk to get location fix");
	}

	// Location service initialization failed
	public void onInitializationFailed(String reason) {
		Log.d(TAG, "onInitializationFailed()");
		showMessageOnUI("onInitializationFailed(): "+ reason);
		positioningOngoing = false;
	}

	// Positioning was stopped
	public void onServiceStopped() {
		Log.d(TAG, "onServiceStopped()");
		showMessageOnUI("onServiceStopped(): IndoorAtlas Positioning Service is stopped.");
		positioningOngoing = false;
	}

	// Calibration failed. Called when device is not moved enough during calibration, for example.
	public void onCalibrationFailed(String reason) {
		Log.d(TAG, "onCalibrationFailed(), reason : "+reason);
		
		// Show unrecoverable error to the user. Typically caused by sensor errors in device.
		showMessageOnUI("onCalibrationFailed()");
	}


	public void onCalibrationStatus(CalibrationState calibrationState) {

		Log.d(TAG, "onCalibrationStatus(): calibration event : "+calibrationState.getCalibrationEvent()
												+", percentage : "+calibrationState.getPercentage() 
												+", time "+System.currentTimeMillis());

		if(positioningOngoing == false) {
			showMessageOnUI("onCalibrationStatus(): \ncalibration event : "+calibrationState.getCalibrationEvent()
												+"\npercentage : "+calibrationState.getPercentage()
												+", time "+System.currentTimeMillis());	
		}
	}

	public void onNetworkChangeComplete(boolean success) {
		Log.d(TAG, "onNetworkChangeComplete(), success = "+success);

		showMessageOnUI("onNetworkChangeComplete() success = "+success);
	}

	
	// Calibration ready, positioning can be started
	// This is called once after call to IndoorAtlasFactory.createIndoorAtlas()
	public void onCalibrationReady() {
		Log.d(TAG, "onCalibrationReady(), positioningOngoing : "+positioningOngoing);

		showMessageOnUI("onCalibrationReady()");
		
		// Use Floor Plans tool to get IDs for building, level and floor plan 
		
		if(positioningOngoing == false) {
			try {
				indoorAtlas.startPositioning(buildingId, levelId,
						floorPlanId);
				positioningOngoing = true;
			} catch (IndoorAtlasException e) {
				e.printStackTrace();
			}
		}
	}

	
	public void onCalibrationInvalid() {
		Log.d(TAG, "onCalibrationInvalid()");
		
		showMessageOnUI("onCalibrationInvalid()");		
	}
	
	
	// Helper method
	private void showMessageOnUI(final String message) {
		handler.post(new Runnable() {
			public void run() {
				textView.setText(message);
			}
		});
	}

	


}
