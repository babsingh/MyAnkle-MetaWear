package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.FileIOHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkle.Http.HttpUpload.httpUploadListener;
import com.ece1778.project.myAnkle.threads.PipelineThread;
import com.ece1778.project.myAnkle.threads.RunnableUploadFile;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Vector;

public class ActivityResearch extends Activity implements OnClickListener {

	private static final String TAG = ActivityResearch.class.getSimpleName();

	// location where data files are on the sd card
	public final static String RAW_FOLDER_PATH = "Raw/";

	// layout objects
	private TextView mTextResearcherName;
	private TextView mTextServerId;
	private TextView mTextTime;
	private Spinner mSpinnerBodyPart;
	private Button mButtonUpdateResearcherName;
	private Button mButtonUpdateServerId;
	private Button mButtonUpdateTime;
	private Button mButtonUpdateBodyPart;
	private Button mButtonSend;

	// user credentials
	private int mUserId = -1;
	private int mServerId = -1;
	private String mExerciseTime = null;
	private String mResearcherName = null;
	private String mBodyPart = null;

	private DatabaseHelper mDatabaseHelper = null;
	private PipelineThread mPipelineThread;
	
	private Vector<File> mSavedFiles;
	private ProgressDialog mUploadPD = null;

	// action to take each time a file has finished being uploaded to server
	private httpUploadListener mHttpUploadListener = new httpUploadListener() {

		@Override
		public void returnResponse(String response) {
			// each response means the completion of one file upload
			mUploadPD.setProgress(mUploadPD.getProgress() + 1);

			// we have completed uploading all the files
			if (mUploadPD.getMax() == mUploadPD.getProgress()) {
				mUploadPD.dismiss();
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mPipelineThread.requestStop();
		mDatabaseHelper.close();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mass_upload);

		// list of body-parts to display in the spinner drop-down
		String[] bodyParts = {"Ankle", "Left Ankle", "Right Ankle", 
				"Knee", "Left Knee", "Right Knee", "Trunk", "Shoulder", "Left Shoulder", 
				"Right Shoulder", "Elbow", "Left Elbow", "Right Elbow", "Wrist", 
				"Left Wrist", "Right Wrist", "Hand", "Left Hand", "Right Hand", "Foot", 
				"Left Foot", "Right Foot", "Neck", "Head"};
		
		// Link UI elements
		mTextResearcherName = (TextView) findViewById(R.id.activity_mass_upload_text_researcher_name);
		mTextServerId = (TextView) findViewById(R.id.activity_mass_upload_text_server_id);
		mTextTime = (TextView) findViewById(R.id.activity_mass_upload_text_time);
		mSpinnerBodyPart = (Spinner) findViewById(R.id.activity_mass_upload_spinner_body_part);
		mButtonUpdateResearcherName = (Button) findViewById(R.id.activity_mass_upload_button_update_name);
		mButtonUpdateServerId = (Button) findViewById(R.id.activity_mass_upload_button_update_server_id);
		mButtonUpdateTime = (Button) findViewById(R.id.activity_mass_upload_button_update_time);
		mButtonUpdateBodyPart = (Button) findViewById(R.id.activity_mass_upload_button_update_part);
		mButtonSend = (Button) findViewById(R.id.activity_mass_upload_button_send);

		// get the userId, exercise time, researcher name and body part from 
		// the shared preferences
		mUserId = PrefUtils.getIntPreference(this, PrefUtils.LOCAL_USER_ID);
		mExerciseTime = PrefUtils.getStringPreference(this, "prefExerciseDuration");
		mResearcherName = PrefUtils.getStringPreference(this, PrefUtils.DEBUG_RESEARCHER_NAME);
		mBodyPart = PrefUtils.getStringPreference(this, PrefUtils.DEBUG_BODY_PART_TESTED);

		// fetch the user's serverId from the database
		mDatabaseHelper = new DatabaseHelper(this);
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT serverId FROM users WHERE _id = " + mUserId, null);
		
		// sanity check
		if(cur != null && cur.moveToFirst()) {
			mServerId = cur.getInt(0);
		}
		cur.close();

		// populate the TextViews with the researcher name, serverId and exercise time
		if(mResearcherName == null) {
			mResearcherName = "";
		}
		
		mTextResearcherName.setText(mResearcherName);
		mTextServerId.setText(String.valueOf(mServerId));
		mTextTime.setText(mExerciseTime);
		
		// initialize an ArrayAdapter and configure the spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item, bodyParts);
		mSpinnerBodyPart.setAdapter(adapter);
		
		// set the default spinner position
		if(mBodyPart != null) {
			
			int position = adapter.getPosition(mBodyPart);
			mSpinnerBodyPart.setSelection(position);
		}

		// register the button-listeners
		mButtonUpdateResearcherName.setOnClickListener(this);
		mButtonUpdateServerId.setOnClickListener(this);
		mButtonUpdateTime.setOnClickListener(this);
		mButtonUpdateBodyPart.setOnClickListener(this);
		mButtonSend.setOnClickListener(this);
		
		// make a pipeline thread to send tasks
		mPipelineThread = new PipelineThread();
		mPipelineThread.start(); 		
	}

	@Override
	public void onClick(View v) {

		// explicitly hide the keyboard when a button-click is registered
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		
		switch (v.getId()) {

		// when the 'Update Name' button is clicked
		case R.id.activity_mass_upload_button_update_name:
			
			String updatedName = mTextResearcherName.getText().toString();
			
			// if the name is not null and has been recently updated
			if(updatedName != null && !updatedName.equals(mResearcherName)) {
				
				// set the local researcher name to the updated one
				mResearcherName = updatedName;
				
				// update the researcher name in the sharedPreferences
				PrefUtils.setStringPreference(this, PrefUtils.DEBUG_RESEARCHER_NAME,
						updatedName);
				
				// display a toast indicating success
				Toast.makeText(this, "Researcher name was updated successfully!", 
						Toast.LENGTH_SHORT).show();
			
			// else, the name was not updated
			} else {
				
				// display an error-level message
				Toast.makeText(this, "Researcher name was not updated!", 
						Toast.LENGTH_SHORT).show();
				
				// reset the corresponding TextView to the original value
				mTextResearcherName.setText(mResearcherName);
			}
			
			break;
		
		// when the 'Update Server ID' button is clicked
		case R.id.activity_mass_upload_button_update_server_id: 
			
			int updatedServerId = mServerId;
			String updatedServerIdString = mTextServerId.getText().toString();
			
			try {
				// try to parse the serverId string as an integer
				updatedServerId = Integer.parseInt(updatedServerIdString);
				
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
			
			// if the serverId has been recently updated
			if(updatedServerId != mServerId) {
				
				// set the local serverId to the updated one
				mServerId = updatedServerId;
				
				// update the Server ID in the shared preferences (temp)
				PrefUtils.setIntPreference(this, PrefUtils.SERVER_ID, 
						updatedServerId);
				
				// create a new ContentValues object and populate it
				ContentValues values = new ContentValues();
				values.put("serverId", updatedServerId);
				
				// update the serverId in the database
				mDatabaseHelper.getWritableDatabase().update(
						"users", values, "_id = " + mUserId, null);
				
				// display a toast indicating success
				Toast.makeText(this, "Server ID was updated successfully!", 
						Toast.LENGTH_SHORT).show();
			
			// else, the serverId was not updated
			} else {
				
				// display an error-level message
				Toast.makeText(this, "Server ID was not updated!", 
						Toast.LENGTH_SHORT).show();
				
				// reset the corresponding TextView to the original value
				mTextServerId.setText(String.valueOf(mServerId));
			}
			
			break;
			
		// when the 'Update Time' button is clicked
		case R.id.activity_mass_upload_button_update_time: 
			
			String updatedTimeString = mTextTime.getText().toString();
			
			// we can assume that updatedTime is a valid integer
			// since it has already been set
			int updatedTime = Integer.parseInt(mExerciseTime);
			
			try {
				
				// try to parse the exerciseTime string as an integer
				updatedTime = Integer.parseInt(updatedTimeString);
				
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
			
			// if the exercise time has been recently updated
			if(updatedTime != Integer.parseInt(mExerciseTime)) {
				
				// set the local exercise time to the updated string
				mExerciseTime = String.valueOf(updatedTime);
				
				// update the exercise time in the shared preferences
				PrefUtils.setStringPreference(this, "prefExerciseDuration", 
						String.valueOf(updatedTime));
				
				// display a toast indicating success
				Toast.makeText(this, "Time was updated successfully!", 
						Toast.LENGTH_SHORT).show();
			
			// else, the exercise time was not updated
			} else {
				
				// display an error-level message
				Toast.makeText(this, "Time was not updated!", 
						Toast.LENGTH_SHORT).show();
				
				// reset the corresponding TextView to the original value
				mTextTime.setText(mExerciseTime);
			}
			
			break;
			
		// when the 'Update Body Part' button is clicked
		case R.id.activity_mass_upload_button_update_part: 
			
			String updatedPart = mSpinnerBodyPart.getSelectedItem().toString();
			
			// set the local body-part to the updated string
			mBodyPart = updatedPart;
			
			// update the body part in the shared preferences
			PrefUtils.setStringPreference(this, PrefUtils.DEBUG_BODY_PART_TESTED, 
					updatedPart);
			
			// display a toast indicating success
			Toast.makeText(this, "Body part was updated successfully!", 
					Toast.LENGTH_SHORT).show();
			
			break;
		
		// when the 'Upload All Files' button is clicked
		case R.id.activity_mass_upload_button_send:
			
			// get a list of all of the saved data files
			FileIOHelper fHelper = new FileIOHelper();
			FileIOHelper.ExternalStorageHelper esHelper = fHelper.new ExternalStorageHelper(
					this, RAW_FOLDER_PATH, "");
			mSavedFiles = esHelper.getAllFiles();

			// get the user id
			int serverId = mServerId;

			// make an upload progress bar
			mUploadPD = makePD(mSavedFiles.size());

			if (BuildConfig.DEBUG)
				Log.i(TAG, "There are " + mSavedFiles.size()
						+ " files on the device");

			for (int x = 0; x < mSavedFiles.size(); x++) {

				File f = mSavedFiles.get(x);

				if (BuildConfig.DEBUG)
					Log.d(TAG, "Uploading File " + f.getName());

				mPipelineThread
						.enqueueNewTask(new RunnableUploadFile(this, serverId,
								f.getName(), f.getPath(), mHttpUploadListener));
			}

			break;
		}
	}

	private ProgressDialog makePD(int maxSize) {
		ProgressDialog pD = new ProgressDialog(this);
		pD.setCancelable(false);
		pD.setMessage("Sending All Files");
		pD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pD.setProgress(0);
		pD.setMax(maxSize);
		pD.show();
		return pD;
	}
}
