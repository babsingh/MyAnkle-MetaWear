package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.FragmentHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkle.metawear.ModuleActivity;
import com.ece1778.project.myAnkle.metawear.ScannerFragment;
import com.ece1778.project.myAnkle.threads.PipelineThread;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;
//import com.google.analytics.tracking.android.EasyTracker;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ActivityMain extends ModuleActivity {
	
	private static final String TAG = ActivityMain.class.getSimpleName();
	
	// the request codes with which to launch child activities
	public static final int CALIBRATION_REQUEST_CODE = 1;
	public static final int PROFILE_REQUEST_CODE = 2;
	public static final int TUTORIAL_REQUEST_CODE = 3;
	
	// custom return codes (which the children return to the caller)
	public static final int RESULT_PROFILE_DELETED = RESULT_FIRST_USER + 1;
	
	public static PipelineThread mPipelineThread;
	
	private DatabaseHelper mDatabaseHelper = null;
	private int mUserId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDatabaseHelper = new DatabaseHelper(this);
		mUserId = PrefUtils.getIntPreference(this, PrefUtils.LOCAL_USER_ID);
		
		// Create and launch a thread which consume issued jobs sequentially off of the main UI thread
        mPipelineThread = new PipelineThread();
        mPipelineThread.start();
		
		// Start FragmentTitle
	    FragmentHelper.swapFragments(getSupportFragmentManager(), 
	    		R.id.activity_main_container, FragmentTitle.newInstance(), 
	    		true, false, null, FragmentTitle.TAG);
	    
	    
	    // if the user hasn't consented yet make them do the full tutorial
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT consent FROM users WHERE _id = " + mUserId, null);
		cur.moveToFirst();
	   if(cur.getInt(0) == -1){
		   // launch an instance of the tutorial in "full" mode
		   Intent intent = new Intent(this, ActivityTutorial.class);
		   intent.putExtra(ActivityTutorial.ARG_TUTORIAL_TYPE,
				ActivityTutorial.Tutorial_Type.FULL);
		   
		   startActivityForResult(intent, TUTORIAL_REQUEST_CODE);
	   }
	   
		
	}
	
	@Override
	  public void onStart() {
	    super.onStart();
	    // enable google analytics
//	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }
	
	@Override 
	public boolean onCreateOptionsMenu(Menu menu) {		
		getMenuInflater().inflate(R.menu.main_menu, menu);
		if (mwController.isConnected() || deviceName != null) {
            menu.findItem(R.id.device_select).setVisible(false);
            menu.findItem(R.id.device_change).setVisible(true);
        } else {
        	menu.findItem(R.id.device_select).setVisible(true);
            menu.findItem(R.id.device_change).setVisible(false);
        }
        return true;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// disable google analytics
//	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mPipelineThread.requestStop();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		// link the layout objects to the corresponding menu-items
		MenuItem menuItemResearch = menu.findItem(R.id.menu_research);
		
		// if the application is launched in debug mode
		if(BuildConfig.DEBUG) {
			
			// enable the 'Mass Upload' menu-item (not visible by default)
			menuItemResearch.setVisible(true);
			menuItemResearch.setEnabled(true);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(BuildConfig.DEBUG) Log.i(TAG,(String) item.getTitle());
		switch(item.getItemId())
		{
				
			case R.id.menu_logout:
				
				// create an intent to launch a new instance of ActivityLogin
				Intent loginIntent = new Intent(this, ActivityLogin.class);
				startActivity(loginIntent);
				
				// close the current activity
				this.finish();
				break;
				
			case R.id.menu_profile:
				
				// create an intent to launch a new instance of ActivityProfile
				// in 'Update Profile' mode
				Intent profileIntent = new Intent(this, ActivityProfile.class);
				profileIntent.putExtra(ActivityProfile.ARG_PROFILE_MODE,
						ActivityProfile.UPDATE_MODE);
				
				// listen for a return call, in case the user deletes the profile
				startActivityForResult(profileIntent, PROFILE_REQUEST_CODE);
				break;
				
			case R.id.menu_calibration:
				
				// create an intent to launch a new instance of ActivityCalibration
				if (device == null && deviceName == null) {
    				Toast.makeText(getApplicationContext(), "Please connect a device", Toast.LENGTH_SHORT).show();
    			} else {
					Intent calibrationIntent = new Intent(this, ActivityCalibration.class);
					calibrationIntent.putExtra("device_name", deviceName);
					calibrationIntent.putExtra("ble_device", (Parcelable) device);
					startActivityForResult(calibrationIntent, CALIBRATION_REQUEST_CODE);
    			}
				break;
				
			case R.id.menu_settings:
				Intent i = new Intent(this, ActivitySettings.class);
				startActivity(i);
				break;
				
			case R.id.menu_info:
				Intent j = new Intent(this, ActivityInformation.class);
				startActivity(j);
				break;
				
			case R.id.menu_server_id:
				int serverId = PrefUtils.getIntPreference(this, PrefUtils.SERVER_ID);
				Toast toast = Toast.makeText(this, "server ID = " + serverId , Toast.LENGTH_LONG);
				toast.show();
				break;
				
			case R.id.menu_research:
				Intent l = new Intent(this, ActivityResearch.class);
				this.startActivity(l);				
				break;
				
			case R.id.device_select:
	            final FragmentManager fm = getSupportFragmentManager();
	            final ScannerFragment dialog = ScannerFragment.getInstance(this, null, true);
	            dialog.show(fm, "scan_fragment");
//	            if(!FragmentCalibration.isCalibrated(this, getDeviceName(), getDevice().getAddress())) {
//	    			// create an intent to launch a new instance of ActivityCalibration
//	    			Intent calibrationIntent = new Intent(this, ActivityCalibration.class);
//	    			calibrationIntent.putExtra("device_name", getDeviceName());
//	    			calibrationIntent.putExtra("ble_device", (Parcelable) device);
//	    			startActivityForResult(calibrationIntent, 
//	    					ActivityMain.CALIBRATION_REQUEST_CODE);
//	    		}
	            break;
	            
	        case R.id.device_change:
//	        	if(!deviceName.equalsIgnoreCase("inbuilt")) {
	            	mwService.close(true);
	            	invalidateOptionsMenu();
//	        	} 
	        	reset_btdevice();
	            break;
				
			default: // shouldn't reach this
				Toast.makeText(this, "Illegal navigation", Toast.LENGTH_LONG).show();
				return false;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// called when the child activity finishes with a response to 
	// the parent's intent call
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
			
		} else if(requestCode == PROFILE_REQUEST_CODE) {
			
			// the profile was deleted
			if(resultCode == RESULT_PROFILE_DELETED) {
				
				// debug message
				if(BuildConfig.DEBUG) Log.d(TAG, "User profile deleted");
				
				// launch a new instance of ActivityLogin
				Intent loginIntent = new Intent(this, ActivityLogin.class);
				startActivity(loginIntent);
				
				// finish the current instance of ActivityMain
				this.finish();
			}
		} else if (requestCode == TUTORIAL_REQUEST_CODE){
			// the full tutorial has been complete mark this user as having consented
			if(resultCode == RESULT_OK){
				int userId = PrefUtils.getIntPreference(this, PrefUtils.LOCAL_USER_ID);
				
				ContentValues args = new ContentValues();
				args.put("consent", 1);
				
				// update the user consent value
				mDatabaseHelper.getWritableDatabase()
						.update("users", args, "_id = " + userId, null);
			} else if (resultCode == RESULT_CANCELED){
				// dont let the user use the app
				this.finish();	
			}
		}
	}
}