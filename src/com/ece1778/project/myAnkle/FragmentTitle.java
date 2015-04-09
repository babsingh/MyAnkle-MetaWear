package com.ece1778.project.myAnkle;

import java.util.zip.GZIPOutputStream;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkle.Http.HttpGetRequest.httpGetResponseListener;
import com.ece1778.project.myAnkle.threads.RunnableGetUserId;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class FragmentTitle extends Fragment implements OnClickListener {
	
	private Button mButtonExercises;
	private Button mButtonProgress;
	private Button mButtonTutorial;
	
	public static final String TAG = FragmentTitle.class.getSimpleName();
	
	private DatabaseHelper mDatabaseHelper = null;
	
	private int mUserId = -1;
	private int mServerId = -1;
	
	
	// instantiate FragmentTitle
	public static FragmentTitle newInstance() {
		FragmentTitle myFragment = new FragmentTitle();
		
		Bundle args = new Bundle();		
		myFragment.setArguments(args);
		
	    return myFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDatabaseHelper = new DatabaseHelper(getActivity());
		
		// get the user id from preferences
		mUserId = PrefUtils.getIntPreference(getActivity(), PrefUtils.LOCAL_USER_ID);
		
		// get the server id corresponding to the user from the database
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT serverId FROM users WHERE _id = " + mUserId, null);
		cur.moveToFirst();
		mServerId = cur.getInt(0);
		cur.close();
		
		// Request new serverId from server if serverId is invalid
		if(mServerId < 0 ) {
			
			ActivityMain.mPipelineThread.enqueueNewTask(
					new RunnableGetUserId(
							getActivity(),
							"http://www.myankle.ca:8080",
							mHttpGetResponseListener
					)
				);
			
		// else if serverId is valid, update the preferences
		} else {
			
			// update the serverId in preferences
			PrefUtils.setIntPreference(getActivity(), PrefUtils.SERVER_ID, mServerId);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		// set the title of the action bar
		getActivity().getActionBar().setTitle("myAnkle");
		
		// load the layout elements from an xml file
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		
		// Link layout elements to code
		mButtonExercises = (Button) view.findViewById(R.id.fragment_main_button_exercises);        
        mButtonProgress = (Button) view.findViewById(R.id.fragment_main_button_progress);
        mButtonTutorial = (Button) view.findViewById(R.id.fragment_main_button_tutorial);
        
        // register listeners
        mButtonExercises.setOnClickListener(this);
        mButtonProgress.setOnClickListener(this);
        mButtonTutorial.setOnClickListener(this);
        
        return (view);
	}
	
	@Override
	public void onClick(View v) {
		ActivityMain activity = (ActivityMain) getActivity();
		String name = activity.getDeviceName();
		BluetoothDevice device = activity.getDevice();
		if (device == null && name == null) {
			Toast.makeText(activity.getApplicationContext(), "Please connect a device", Toast.LENGTH_SHORT).show();
		} else {
			switch (v.getId()) {
			
	        	case R.id.fragment_main_button_exercises:
	        		
	        		// Clear the ankle-side and eye-state preferences from previous sessions
	    			PrefUtils.setStringPreference(getActivity(), PrefUtils.ANKLE_SIDE_KEY, null);
	    			PrefUtils.setStringPreference(getActivity(), PrefUtils.EYE_STATE_KEY, null);
	        		
	        		// If device is already calibrated, read the calibration values
	        		// and launch ActivityMeasure
	    			ActivityMain parent = (ActivityMain) getActivity();
	        		if(FragmentCalibration.isCalibrated(getActivity(), parent.getDeviceName(), parent.getDevice().getAddress())) {
	        			
	        			// create an intent to launch a new instance of ActivityMeasure
	        			Intent measureIntent = new Intent(getActivity(), ActivityMeasure.class);
	        			measureIntent.putExtra("device_name", name);
	        			measureIntent.putExtra("ble_device", (Parcelable) device);
	        			activity.startActivity(measureIntent);
	        		
	        		// calibration has not been completed yet
	        		} else {
	        			
	        			// create an intent to launch a new instance of ActivityCalibration
	        			Intent calibrationIntent = new Intent(getActivity(), ActivityCalibration.class);
	        			calibrationIntent.putExtra("device_name", name);
	        			calibrationIntent.putExtra("ble_device", (Parcelable) device);
	        			getActivity().startActivityForResult(calibrationIntent, 
	        					ActivityMain.CALIBRATION_REQUEST_CODE);
	        		}
	        		
	        		break;
	        		
	        	case R.id.fragment_main_button_progress:
	        		
	        		// clear the eye-state preference from previous sessions
	        		PrefUtils.setStringPreference(getActivity(), PrefUtils.EYE_STATE_KEY, null);
	        		
	        		// create an intent to launch a new instance of ActivityProgress
	        		Intent progressIntent = new Intent(getActivity(), ActivityProgress.class);
	        		getActivity().startActivity(progressIntent);
	        		
	        		break;
	        		
	        	case R.id.fragment_main_button_tutorial:
	        		
	        		// start the partial tutorial activity (don't add disclaimers and profile pages)        		
	    			Intent tutorialIntent = new Intent(getActivity(), ActivityTutorial.class);
	    			tutorialIntent.putExtra(ActivityTutorial.ARG_TUTORIAL_TYPE, 
	    					ActivityTutorial.Tutorial_Type.PARTIAL);
	    			getActivity().startActivity(tutorialIntent);
	    			
	    			break;
	        	
	        	default:
	        		
	        		// should not reach here
	        		break;
			}
		}
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// release the database helper object
		mDatabaseHelper.close();
	}


	private httpGetResponseListener mHttpGetResponseListener = new httpGetResponseListener(){

		@Override
		public void returnResponse(String response) {
			
			// Remove all non-numeric characters from response
			String serverId = response.replaceAll("[^\\d.]", "");
			
			try{
				// bundle the arguments to update the user's content fields
				ContentValues args = new ContentValues();
				args.put("serverId", Integer.parseInt(serverId));
				
				// update the serverId in the user's database entry
				mDatabaseHelper.getWritableDatabase().update(
						"users", args, "_id = " + mUserId, null);
				
				// update the serverId in preferences
				PrefUtils.setIntPreference(getActivity(), PrefUtils.SERVER_ID, 
						Integer.parseInt(serverId));
				
				// debug
				if(BuildConfig.DEBUG) Log.i(TAG,"httpGet returned server id = " + serverId);
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}			
		}
		
	};
}
