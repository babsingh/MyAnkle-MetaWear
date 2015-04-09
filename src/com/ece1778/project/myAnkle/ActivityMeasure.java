package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.FragmentExerciseDialog.CustomDialogFinishListener;
import com.ece1778.project.myAnkle.Helpers.FragmentHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkle.metawear.ModuleActivity;
import com.ece1778.project.myAnkle.metawear.ScannerFragment;
import com.ece1778.project.myAnkle.threads.PipelineThread;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class ActivityMeasure extends ModuleActivity implements CustomDialogFinishListener {

	private static final String TAG = ActivityMeasure.class.getSimpleName();
	public static PipelineThread mPipelineThread;
	
	//Global FeedBack Variables
	public boolean feedback_status = false;
	public double feedbackUpperBN = -1.0; 
	public double feedbackLowerBN = -1.0;
	
	// the request codes with which to launch child activities
	public static final int CALIBRATION_REQUEST_CODE = 1;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_measure);
		
		// hide the status (notification) bar
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		// get BT device & name 
		Bundle args = getIntent().getExtras();
		deviceName = args.getString("device_name");
		device = (BluetoothDevice) args.getParcelable("ble_device");
		
		// create and maintain a new instance of PipelineThread to be used later
		mPipelineThread = new PipelineThread();
		mPipelineThread.start();
		
		// create a new instance of FragmentExercises and add it to the placeholder
		FragmentExercises newFragment = FragmentExercises.newInstance();
		FragmentHelper.swapFragments(getSupportFragmentManager(), 
				R.id.activity_measure_container, newFragment, 
				true, false, null, FragmentExercises.TAG);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// release the PipelineThread object
		mPipelineThread.requestStop();
	}

	@Override
	public void onBackPressed() {
			
		// if the back button is pressed while on the 'Measure' fragment, do nothing
		if(getCurrentFragmentTag() == FragmentExerciseMeasure.TAG) {

			// create a debug message
			if(BuildConfig.DEBUG) Log.d(TAG, "Blocked back-press!");
			
		// if the back button is pressed while on the 'Result' fragment
		} else if(getCurrentFragmentTag() == FragmentExerciseResults.TAG) {
			
			// navigate up the backstack to the 'Exercises' fragment
			getSupportFragmentManager().popBackStackImmediate(null, 
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
			
		// else, use the default functionality
		} else {
			
			super.onBackPressed();
		}
	}
	
	// if a fragment is currently being rendered in the placeholder, return its tag 
	private String getCurrentFragmentTag() {
		
		String currFragmentTag = null;
		
		// find the fragment currently hosted in the fragment placeholder
		Fragment currFragment = getSupportFragmentManager()
				.findFragmentById(R.id.activity_measure_container);
		
		// if the fragment isn't null, return it's associated tag
		if(currFragment != null) {
			
			currFragmentTag = currFragment.getTag();
			
		} else {
			
			// create a debug message indicating no fragment is currently hosted
			if(BuildConfig.DEBUG) Log.d(TAG, "Current fragment is NULL");
		}
		
		return currFragmentTag;
	}
	
	// creating and manipulating menu elements
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.measure_menu, menu);
		if (mwController.isConnected() || deviceName != null) {
            menu.findItem(R.id.device_select).setVisible(false);
            menu.findItem(R.id.device_change).setVisible(true);
        } else {
        	menu.findItem(R.id.device_select).setVisible(true);
            menu.findItem(R.id.device_change).setVisible(false);
        }
		return true;
	}
	
	// functionality for menu button-press
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
		
		// when the 'Up' button is pressed, use the default back-button press functionality
		case android.R.id.home: 
		
			onBackPressed();
			
			break;
		
		// when the 'Settings' menu-button is selected
		case R.id.measure_menu_settings: 
			
			Intent settingsIntent = new Intent(this, ActivitySettings.class);
			startActivity(settingsIntent);
			
			break;
			
		// when the 'Show User ID' menu-button is pressed
		case R.id.measure_menu_server_id:
			
			int serverId = PrefUtils.getIntPreference(this, PrefUtils.SERVER_ID);
			Toast toast = Toast.makeText(this, "Server ID = " + serverId, Toast.LENGTH_SHORT);
			toast.show();
			break;
			
		case R.id.device_select:
            final FragmentManager fm = getSupportFragmentManager();
            final ScannerFragment dialog = ScannerFragment.getInstance(this, null, true);
            dialog.show(fm, "scan_fragment");
//            if(!FragmentCalibration.isCalibrated(this, getDeviceName(), getDevice().getAddress())) {
//    			// create an intent to launch a new instance of ActivityCalibration
//    			Intent calibrationIntent = new Intent(this, ActivityCalibration.class);
//    			calibrationIntent.putExtra("device_name", getDeviceName());
//    			calibrationIntent.putExtra("ble_device", (Parcelable) device);
//    			startActivityForResult(calibrationIntent, 
//    					ActivityMain.CALIBRATION_REQUEST_CODE);
//    		}
            break;
            
        case R.id.device_change:
//        	if(!deviceName.equalsIgnoreCase("inbuilt")) {
        	mwService.close(true);
        	invalidateOptionsMenu();
//    	} 
        	reset_btdevice();
            break;
		
		// shouldn't reach here
		default: 
			if(BuildConfig.DEBUG) Log.d(TAG, "Illegal menu navigation!");
			return false;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override	
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {

		if(requestCode == CALIBRATION_REQUEST_CODE) {
			
			// calibration was successful
			if(resultCode == RESULT_OK) {
				
				// display a toast indicating calibration was successful
				Toast.makeText(this, R.string.calibration_successful_toast, 
						Toast.LENGTH_SHORT).show();
				
				// Clear the ankle-side and eye-state preferences from previous sessions
    			PrefUtils.setStringPreference(this, PrefUtils.ANKLE_SIDE_KEY, null);
    			PrefUtils.setStringPreference(this, PrefUtils.EYE_STATE_KEY, null);
				
    			if (device == null && deviceName == null) {
    				Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_SHORT).show();
    			} else {
					// launch ActivityMeasure
					Intent measureIntent = new Intent(this, ActivityMeasure.class);
					measureIntent.putExtra("device_name", deviceName);
	    			measureIntent.putExtra("ble_device", (Parcelable) device);
					startActivity(measureIntent);
    			}
    			
			// calibration failed
			} else if(resultCode == RESULT_CANCELED) {
				
				// display a toast indicating calibration failure
				Toast.makeText(this, R.string.calibration_failed_toast, 
						Toast.LENGTH_SHORT).show();
			}
			
		}
	}
	
	// the callback initiated when a dialog fragment is closed
	@Override
	public void onFinish(int id, Bundle bundle) {
		
		switch(id) {
		
		// the call-back was initiated by FragmentExerciseDialog
		case FragmentExerciseDialog.ID:
		
			// retrieve the bundled argument strings
			String ankleSide = bundle.getString(FragmentExerciseDialog.RETURN_ANKLE_SIDE);
			String eyeState = bundle.getString(FragmentExerciseDialog.RETURN_EYE_STATE);
			
			// get a reference to the currently displayed FragmentExercises instance
			Fragment exerciseFragment = getSupportFragmentManager().
					findFragmentById(R.id.activity_measure_container);
			
			// sanity check
			if(exerciseFragment != null && exerciseFragment 
					instanceof FragmentExercises) {
				
				// initiate the call-back method in the exercises fragment
				((FragmentExercises) exerciseFragment).
						updateSelection(ankleSide, eyeState);
				
			} else {
			
				// display a toast indicating failure to update selection
				Toast.makeText(this, "Selection not updated!", Toast.LENGTH_SHORT).show();
				
				// display an error-level message
				if(BuildConfig.DEBUG) Log.d(TAG, "FragmentExercises is null");
			}
			
			break;
		}
	}
}
