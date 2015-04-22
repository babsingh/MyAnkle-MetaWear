package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.DialogStyleHelper;
import com.ece1778.project.myAnkle.Helpers.FileIOHelper;
import com.ece1778.project.myAnkle.Helpers.FragmentHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkle.Helpers.Samples;
import com.ece1778.project.myAnkle.Helpers.SoundPoolHelper;
import com.ece1778.project.myAnkle.metawear.ModuleFragment;
import com.ece1778.project.myAnkle.threads.RunnableSaveRawDataFile;
import com.ece1778.project.myAnkle.threads.RunnableUploadFile;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.Debug;
import com.mbientlab.metawear.api.controller.Accelerometer.Axis;
import com.mbientlab.metawear.api.controller.Accelerometer.Component;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig.FullScaleRange;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig.OutputDataRate;
import com.mbientlab.metawear.api.util.BytesInterpreter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import junit.framework.Assert;

public class FragmentExerciseMeasure extends ModuleFragment implements OnSeekBarChangeListener, SensorEventListener {
	
	public static final String TAG = FragmentExerciseMeasure.class.getSimpleName();
	
	public final static String RAW_FOLDER_PATH = "Raw/";
	private static final int STATE_INIT = 1;
	private static final int STATE_COUNTDOWN = 2;
	private static final int STATE_MEASURE = 3;
	private static final int STATE_FINISH = 4;
	
	// Countdown Timer
	private static final int TICK_INTERVAL_IN_MILLIS = 100;
	private static final int SHORT_VIBRATE_DURATION_IN_MILLIS = 50;
	private static final int LONG_VIBRATE_DURATION_IN_MILLIS = 200;
	private MyCounter mTimer = null;
	
	// SoundPool
	private SoundPoolHelper mSoundPoolHelper;

	// Starting measurement state 
	private int mState = STATE_INIT;

	private DatabaseHelper mDatabaseHelper = null;

	// Hardware
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Vibrator mVibrator;

	private Samples mSamples = null;
	
	//Feedback
    private boolean feedbackStatus;
    private double lowerBN, upperBN, curBN, tempBN;
    
	//MetaWear
	ActivityMeasure parent = null;
	private Accelerometer accelController;
	private SamplingConfig samplingConfig;
	private Handler handler = new Handler();
	private int dataRange= 2;
	private int samplingRate= 3;
	boolean initCond;
	byte[] config = new byte[] {0, 0, 0x20, 0, 0};
	boolean updateReadings = false;
	float lastTime = -1;
	boolean setCoverage = true;
	int coverage = 35;
	DecimalFormat df = new DecimalFormat("#.###");
	
	private Accelerometer.Callbacks mCallback= new Accelerometer.Callbacks() {
		@Override
        public void receivedDataValue(short x, short y, short z) {
            if (mSamples != null && mState != STATE_FINISH && mTimer != null) {
            	float elapsedTime = mTimer.getElapsedSec();
                float a = (float) (BytesInterpreter.bytesToGs(config, x) * 9.8);
                float b = (float) (BytesInterpreter.bytesToGs(config, y) * 9.8);
                float c = (float) (BytesInterpreter.bytesToGs(config, z) * 9.8);
            	mSamples.add(elapsedTime, a, b, c);
            	if (updateReadings) {
            		
            		/* Set Coverage Scope */
            		if (setCoverage) {
            			coverage = mSamples.getNumSamples();
            			setCoverage = false;
            		}
            		
            		/* Generate a real time balance number */
            		curBN = mSamples.get_mean_r(coverage);
            		presentBN.setText("Aggregate BN = " + df.format(curBN));
            		
            		if (mSamples != null) {
						tempBN = mSamples.get_mean_r_temp();
						shortBN.setText("Transient BN = " + df.format(tempBN));
					}
            		
//            		xAxis.setText("x = " + df.format(a));
//            		yAxis.setText("y = " + df.format(b));
//            		zAxis.setText("z = " + df.format(c));
            		updateReadings = false;
            	}
            	
            	Log.d("ADD_DATA", "FragmentExceriseMeasure: time = " + elapsedTime + " x = " + a + " y = " + b + " z = " + c + ".");
//            	mSamples.add(elapsedTime, x, y, z);
//            	Log.d("ADD DATA", "FragmentExceriseMeasure: x = " + x + " y = " + y + " z = " + z + ".");
            }
        }
    };
    
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
    	super.onServiceConnected(name, service);
        Log.d("DATA_FLOW", "FragmentExceriseMeasure: onServiceConnected; addModuleCallBack");
        accelController= (Accelerometer)this.mwController.getModuleController(Module.ACCELEROMETER);
        this.mwController.addModuleCallback(mCallback);
    };
    
	@Override
	public void onDestroy() {
		Log.d("DATA_FLOW", "FragmentExceriseMeasure: Removing Callback");
        mwController.removeModuleCallback(mCallback);
		super.onDestroy();
		mDatabaseHelper.close();
		mSoundPoolHelper.release();
	}
    
	// Views
	private TextView mTextTitle, mTextTime, mTextSubtitle;
	private TextView xAxis, yAxis, zAxis, presentBN, shortBN;
	
	// User specific data
	private int mUserId = -1;
	private int mServerId = -1;
	private int mExerciseId = -1;
	private String mAnkleSide = null;
	private float mMeanR;
	private int mExerciseDuration;
	private int mCountdownDuration;

	// SeekBar
	private SeekBar mStopSeekBar;
	private int mSeekBarProgress;
	
	// instantiate FragmentExerciseMeasure
	public static FragmentExerciseMeasure newInstance(int exercise_id) {
		FragmentExerciseMeasure myFragment = new FragmentExerciseMeasure();
		
		Bundle args = new Bundle();	
		args.putInt("exercise_id", exercise_id);
		myFragment.setArguments(args);
	
		return myFragment;
	    
	} 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDatabaseHelper = new DatabaseHelper(getActivity());
		
		// indicates that the fragment should receive all menu-related
		// call-backs that are not explicitly consumed in the host activity
		setHasOptionsMenu(true);
		
		// Init hardware
		if (((ActivityMeasure) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
			mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
			mAccelerometer = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		
		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		
		// Init SoundPool
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mSoundPoolHelper = new SoundPoolHelper(getActivity());
		parent = (ActivityMeasure) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_exercise_measure, container, false);
		mStopSeekBar = (SeekBar) view.findViewById(R.id.fragment_exercise_measure_seekbar);
		mStopSeekBar.setOnSeekBarChangeListener(this);
		
		view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		
		view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
		    @Override
		    public void onSystemUiVisibilityChange(int visibility) {
		    }
		});
		
		return (view);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//MetaWear Support
//		accelController.enableXYZSampling().withFullScaleRange(FullScaleRange.values()[dataRange]).withOutputDataRate(OutputDataRate.values()[samplingRate]);
		if (!((ActivityMeasure) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
			initCond = true;
	        new Thread(new Runnable() {
	        	@Override
	        	public void run() {
	        		while (initCond) {
	        			try {
	        				handler.post(new Runnable() {
								@Override
								public void run() {
									collectDataFromMetawear();
									if (mState == STATE_MEASURE) {
										updateReadings = true;
									}
								}
							});
	        				Thread.sleep(500);
	        			} catch(Exception e) {
	        			}
	        		}
	        	}
	        }).start();
		} 
	}
	
	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);

		// Retrieve user id, server id, exercise id, and ankleSide information from 
		// args from FragmentExerciseInstruction and SharedPreferences

		mUserId = PrefUtils.getIntPreference(getActivity(), PrefUtils.LOCAL_USER_ID);
		
		// print debug message
		if(BuildConfig.DEBUG) Log.d(TAG, "User ID is " + mUserId); 
		
		mServerId = PrefUtils.getIntPreference(getActivity(), PrefUtils.SERVER_ID);
		mExerciseId = getArguments().getInt("exercise_id", -1);
		
		mAnkleSide = PrefUtils.getStringPreference(getActivity(), PrefUtils.ANKLE_SIDE_KEY);
		Assert.assertTrue(mExerciseId >= 0);
		Assert.assertTrue(mAnkleSide != null);
		
		// Retrieve duration information from SharedPreferences
//		mCountdownDuration = Integer.parseInt(PrefUtils.getStringPreference(getActivity(), "prefCountdownDuration")) * 1000; 
//		mExerciseDuration = Integer.parseInt(PrefUtils.getStringPreference(getActivity(), "prefExerciseDuration")) * 1000; 
		// Changing to 5s count-down and 10s exercise
		mCountdownDuration = 10000; 
		mExerciseDuration = 30000;
		
		// Init views
		mTextTitle = (TextView) getView().findViewById(R.id.fragment_exercise_measure_textview_title);
		mTextTime = (TextView) getView().findViewById(R.id.fragment_exercise_measure_textview_time);
		mTextSubtitle = (TextView) getView().findViewById(R.id.fragment_exercise_measure_textview_subtitle);
		
		presentBN = (TextView) getView().findViewById(R.id.aggregate_balance_number);
		shortBN = (TextView) getView().findViewById(R.id.transient_balance_number);
//		xAxis = (TextView) getView().findViewById(R.id.text_view_x);
//		yAxis = (TextView) getView().findViewById(R.id.text_view_y);
//		zAxis = (TextView) getView().findViewById(R.id.text_view_z);
		
		// Init and clear samples
		mSamples = new Samples(readCalibrationValues());
		mSamples.clear();
	}


	@Override
	public void onResume() {
		super.onResume();
		
		// debug messages
		FragmentManager fm = getActivity().getSupportFragmentManager();
		if(BuildConfig.DEBUG) {
			
			Log.i(TAG, "The exercise ID is " + mExerciseId + " & number of"
				+ " backstack entries are " + fm.getBackStackEntryCount());
		
			for(int i=0; i < fm.getBackStackEntryCount(); i++) {
				Log.d(TAG, fm.getBackStackEntryAt(i).getName());
			}
		}
		
		// set the action bar parameters
		getActivity().getActionBar().setTitle("Measure");
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		
		// Keep screen on
		getActivity().getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// require sensors
		mSoundPoolHelper.resume();
		
		if (((ActivityMeasure) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		// give a countdown before measurements start again
		mState = STATE_INIT;
		
	}

	@Override
	public void onPause() {
		super.onPause();

		// Allow screen to turn off 		
		getActivity().getWindow().clearFlags(android.view.WindowManager
				.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// unregister sensors
		if (((ActivityMeasure) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
			mSensorManager.unregisterListener(this);
		}
			
		mTimer.cancel();
		
		// If it's measuring and paused, go back to instruction fragment
		if(mState != STATE_FINISH) {
			mSoundPoolHelper.pause();
			mSamples.clear();
			backToInstructionFragment();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			
			if (mState == STATE_INIT) {
				
				mTextTitle.setText(R.string.exercise_countdown_title);
				mTextSubtitle.setText(R.string.exercise_countdown_subtitle);

				// Start the countdown
				mTimer = new MyCounter(mCountdownDuration, TICK_INTERVAL_IN_MILLIS, true);
				mTimer.start();
				mState = STATE_COUNTDOWN;
				
			} else if (mState == STATE_COUNTDOWN && mTimer.isTimerDone()) {
				
				
				mTextTitle.setText(R.string.exercise_messure_title);
				mTextSubtitle.setText(R.string.exercise_messure_subtitle);
				
				// Start the measurement timer
				mTimer = new MyCounter(mExerciseDuration, TICK_INTERVAL_IN_MILLIS, true);
				mTimer.start();
				mState = STATE_MEASURE;
				updateReadings = true;
			} else if (mState == STATE_MEASURE) {
				
				if(!mTimer.isTimerDone()) {
					
					// Collect data
					float elapsedTime = mTimer.getElapsedSec();
					float x = event.values[0];
					float y = event.values[1];
					float z = event.values[2];
					mSamples.add(elapsedTime, x, y, z);
	            	if ((elapsedTime - lastTime) >= 0.5) {
	            		lastTime = elapsedTime;
	            		
	            		/* Set Coverage Scope */
	            		if (setCoverage) {
	            			coverage = mSamples.getNumSamples();
	            			setCoverage = false;
	            		}
	            		
	            		/* Generate a real time balance number */
	            		curBN = mSamples.get_mean_r(coverage);
	            		presentBN.setText("Aggregate BN = " + df.format(curBN));
	            		
	            		if (mSamples != null) {
							tempBN = mSamples.get_mean_r_temp();
							shortBN.setText("Transient BN = " + df.format(tempBN));
						}
	            		
//	            		xAxis.setText("x = " + df.format(x));
//	            		yAxis.setText("y = " + df.format(y));
//	            		zAxis.setText("z = " + df.format(z));
	            	}
				} else {
					
					// Finish up collecting data and save and send
					if (mState != STATE_FINISH) {
						mState = STATE_FINISH; // Ensures the proceeding chunk of code for making a dialog and saving results gets executed ONLY ONCE
						vibrate(LONG_VIBRATE_DURATION_IN_MILLIS);
						initCond = false;
						
						// Make a dialog box asking user to Save or Delete 
						AlertDialog.Builder dlgAlert= new AlertDialog.Builder(getActivity())
					    .setTitle("Done measuring!")
					    .setCancelable(false)
					    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int whichButton) {
					        	mSamples.clear();
					        	backToInstructionFragment();
					        }})
					    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
					       	public void onClick(DialogInterface dialog, int whichButton) {
					       		int session_id = saveResults();			    
					       		goToResultFragment(session_id);
					       	}
					    });
						
						// create a new instance of DialogHelper and display the dialog
					    DialogStyleHelper box = new DialogStyleHelper(getActivity(), dlgAlert.create());
					    box.showDialog();
					}
				} 
			}
		}
	}
	
	public void collectDataFromMetawear() {
		if (mState == STATE_INIT) {
			
			mTextTitle.setText(R.string.exercise_countdown_title);
			mTextSubtitle.setText(R.string.exercise_countdown_subtitle);

			// Reset metawear device
			this.debugController.resetDevice();
			
			// Start the countdown
			mTimer = new MyCounter(mCountdownDuration, TICK_INTERVAL_IN_MILLIS, true);
			mTimer.start();
			mState = STATE_COUNTDOWN;
			
		} else if (mState == STATE_COUNTDOWN && mTimer.isTimerDone()) {
			
			
			mTextTitle.setText(R.string.exercise_messure_title);
			mTextSubtitle.setText(R.string.exercise_messure_subtitle);
			
			// Start the measurement timer
			mState = STATE_MEASURE;
			mTimer = new MyCounter(mExerciseDuration, TICK_INTERVAL_IN_MILLIS, true);
			mTimer.start();
			
		} else if (mState == STATE_MEASURE) {
			
			if(!mTimer.isTimerDone()) {
				
				// Collect data
//				accelController.setComponentConfiguration(Component.DATA, config);
//		        accelController.enableNotification(Component.DATA);
//				accelController.enableMotionDetection(Axis.values());
		        SamplingConfig config= accelController.enableXYZSampling();
		        config.withFullScaleRange(FullScaleRange.values()[2])
                	.withOutputDataRate(OutputDataRate.values()[3]);
//		        accelConfig.initialize(config.getBytes());
				accelController.startComponents();
				
			} else {
				
				// Finish up collecting data and save and send
				if (mState != STATE_FINISH) {
//					accelController.disableNotification(Component.DATA);
					accelController.stopComponents();
	                accelController.disableAllDetection(true);
	                
	                initCond = false;
					mState = STATE_FINISH; // Ensures the proceeding chunk of code for making a dialog and saving results gets executed ONLY ONCE
					vibrate(LONG_VIBRATE_DURATION_IN_MILLIS);
					
					// Make a dialog box asking user to Save or Delete 
					AlertDialog.Builder dlgAlert= new AlertDialog.Builder(getActivity())
				    .setTitle("Done measuring!")
				    .setCancelable(false)
				    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				        	mSamples.clear();
				        	backToInstructionFragment();
				        }})
				    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
				       	public void onClick(DialogInterface dialog, int whichButton) {
				       		int session_id = saveResults();			    
				       		goToResultFragment(session_id);
				       	}
				    });
					
					// create a new instance of DialogHelper and display the dialog
				    DialogStyleHelper box = new DialogStyleHelper(getActivity(), dlgAlert.create());
				    box.showDialog();
				}
			} 
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		//noop, but necessary override for implementing SensorEventListener
	}
	
	/*
	 * Saving and Sending data
	 */
	/**
	 * This method manages all the saving of results from this session. 
	 * - calls databaseSaveSummary() to save summary of session into database (ie. userId, exerciseId, meanR, date...)
	 * - calls postSaveThread(...) to save the raw data to a .csv file
	 * - calls postSendThread(...) to send the raw data off to the server
	 * @return
	 */
	private int saveResults() {
		
		if (((ActivityMeasure) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
			mSensorManager.unregisterListener(this);
		}
		
		// Save all samples to file
		int session_id = databaseSaveSummary();
		
		// Setup params for SaveThread and SendThread
		String filename = makeFileName(session_id);
		FileIOHelper fHelper = new FileIOHelper();
		FileIOHelper.ExternalStorageHelper esHelper = fHelper.new ExternalStorageHelper(getActivity(), RAW_FOLDER_PATH, filename);
		
		// Save raw data to a .csv file
		postSaveThread(filename, session_id, esHelper);
		
		// if the user did not opt out of data collection
		if ( PrefUtils.getIntPreference(getActivity(), PrefUtils.OPT_OUT_KEY) != 1){
			// Send the .csv file to the server
			postSendThread(filename, session_id, esHelper.getFullPath());
		}
		
		if(BuildConfig.DEBUG) Log.d("FragmentExerciseMeasure", String.valueOf(session_id));
		return session_id;
	} 

	/**
	 * Saves a summary of the session to the database, in the "sessions" table
	 * The summary includes (userId, exerciseId, meanR, numSamples, ankleSide, date)
	 * @returns the session_id integer autoincremented in the table
	 */
	private int databaseSaveSummary() {
		
		int session_id = -1;
		
		try {
			mMeanR = mSamples.get_mean_r();
			String date = DateFormat.format("yyyy-MM-dd", new Date()).toString();
			mDatabaseHelper.getWritableDatabase().execSQL(
					"INSERT INTO sessions (userId, exerciseId, meanR, ankleSide, date) VALUES ("
					+ mUserId + ", " + mExerciseId + ", " + mMeanR + ", '" + mAnkleSide + "', '" + date +  "');");
			Cursor cursor = mDatabaseHelper.getReadableDatabase().rawQuery(
					"SELECT MAX(_id) FROM sessions", null);
			cursor.moveToFirst();
			Assert.assertTrue(!cursor.isAfterLast());
			session_id = cursor.getInt(0);
			cursor.close();
			mDatabaseHelper.close();
			
		} catch (Throwable t) {
			if(BuildConfig.DEBUG) Log.e(TAG, t.toString());
		}
		return session_id;
	}
	
	/**
	 * Enqueues a new thread to threadManager to save the raw data in a .csv file.
	 * - creates a progress dialog and a UI handler
	 * - passes an ExternalStorageHelper, the Samples object, 
	 * 		the progress dialog and UI handler to the SaveFileRunnable
	 * @param filename
	 * @param session_id
	 * @param esHelper
	 */
	private void postSaveThread(String filename, int session_id, FileIOHelper.ExternalStorageHelper esHelper) {
		if(session_id > 0) {
			ProgressDialog pD = makePD();
			Handler m_pDHandler = new Handler();
			

			String date = DateFormat.format("yyyy-MM-dd", new Date()).toString();			
			// get age and gender about user from db
			Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
					"SELECT age, gender FROM users WHERE _id = " + mUserId, null);

			cur.moveToFirst();
			int age = cur.getInt(0);
			String gender = cur.getString(1);
			
			// get the latest left-ankle injury date
			cur = mDatabaseHelper.getReadableDatabase().rawQuery(
					"SELECT MAX(injuryDate) FROM injuries WHERE ankleSide='Left' " +
					" AND userId = " + mUserId + ";", null);
			cur.moveToFirst();
			String injuryDateLeft = cur.getString(0);
			
			// get the latest right-ankle injury date
			cur = mDatabaseHelper.getReadableDatabase().rawQuery(
					"SELECT MAX(injuryDate) FROM injuries WHERE ankleSide='Right' " +
					" AND userId = " + mUserId + ";", null);
			cur.moveToFirst();
			String injuryDateRight = cur.getString(0);
			
			ActivityMeasure.mPipelineThread.enqueueNewTask(
					new RunnableSaveRawDataFile(esHelper, 
										PrefUtils.getIntPreference(getActivity(), PrefUtils.SERVER_ID),
										gender,age,injuryDateLeft,injuryDateRight,mExerciseId, date,
										mSamples, pD, m_pDHandler));
		}
	}
	
	/**
	 * Enqueues a new thread to threadManager to send the raw data in a .csv file to the server
	 * - creates a List of BasicNameValuePairs containing userId, meanR, and ankleSide info
	 * - passes the context, filename, file's path, the list, 
	 * 		and an httpInteractionResponseListener to the SendFileRunnable
	 * @param filename
	 * @param session_id
	 * @param path
	 */
	private void postSendThread(String filename, int session_id, String path) {
		if(session_id > 0) {
			ActivityMeasure.mPipelineThread.enqueueNewTask(
				new RunnableUploadFile( getActivity(), mServerId, filename, path)
			);
		}
	}

	/**
	 * Creates a filename string of the format
	 * 	gender_age_ankleSide_e#_s#_yyyyMMdd_h_mmaa.csv
	 * ie. male_32_left_e2_s15_20130814_12_08pm.csv
	 * @param session_Id
	 * @returns the compiled string for filename
	 */
	private String makeFileName(int session_Id) {
		
		String exerciseStr = "e" + mExerciseId;
		String sessionStr = "s" + session_Id;
		String date = DateFormat.format("yyyyMMdd", new Date()).toString();
		String curTime = DateFormat.format("h_mmaa", new Date()).toString();
		
		// get age and gender about user from db
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT age, gender FROM users WHERE _id = " + mUserId, null);

		cur.moveToFirst();
		int age = cur.getInt(0);
		String gender = cur.getString(1);
		
		// get the latest left-ankle injury date
		cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT MAX(injuryDate) FROM injuries WHERE ankleSide='Left' " +
				" AND userId = " + mUserId + ";", null);
		cur.moveToFirst();
		String injuryDateLeft = cur.getString(0);
					
		// get the latest right-ankle injury date
		cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT MAX(injuryDate) FROM injuries WHERE ankleSide='Right' " +
				" AND userId = " + mUserId + ";", null);
		cur.moveToFirst();
		String injuryDateRight = cur.getString(0);
		
		String filename = "";
		filename += gender + "_";
		filename += Integer.toString(age) + "_";
		filename += "injLeft-" + injuryDateLeft + "_";
		filename += "injRight-" + injuryDateRight + "_";
		filename += mAnkleSide + "_";
		filename += exerciseStr + "_";
		filename += sessionStr + "_";
		filename += date + "_";
		filename += curTime;
		filename += ".csv";
		return filename;
	}
	
	/**
	 * Initializes a progress dialog for saving raw data to .csv file
	 * @returns the progress dialog
	 */
	private ProgressDialog makePD() {
		
		ProgressDialog pD = new ProgressDialog(getActivity());
		pD.setCancelable(false);
		pD.setMessage("Saving raw data...");
		pD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pD.setProgress(0);
		pD.setMax(mSamples.getNumSamples());
		pD.show();
		return pD;
	}
	/*
	 * User notifications
	 */
	
	/**
	 * Causes the vibrator to vibrate for millis amount of milliseconds.
	 * - checks Preferences first to see if vibrate is enabled
	 * @param millis
	 */
	private void vibrate(int millis) {
		if(PrefUtils.getVibrateCheckBox(getActivity())) {
			if(mVibrator.hasVibrator()) {
				mVibrator.vibrate(millis);
			}
		}
	}

	/*
	 * SeekBar methods
	 * (non-Javadoc)
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		mSeekBarProgress = progress;		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(mSeekBarProgress == seekBar.getMax()) {
			// User has dragged seekBar to the end
			// Cancel the timer,
			if (parent.getDeviceName().equalsIgnoreCase("metawear")
					&& mState == STATE_MEASURE) {
				accelController.stopComponents();
                accelController.disableAllDetection(true);
			}
			
			if(mTimer != null) {
				
				mTimer.cancel();
				
				// set off m_Vibrator
				vibrate(SHORT_VIBRATE_DURATION_IN_MILLIS);
			
				backToInstructionFragment();
			}
			
		} else {
			// Reset the seek bar to the beginning
			seekBar.setProgress(0);
		}	
	}
	

	
	/*
	 * Navigating out of this fragment
	 */
	/**
	 * Launch FragmentExerciseInstruction.
	 */
	private void backToInstructionFragment() {
		
		// display the most recent fragment (ExerciseInstructionFragment) 
		// from the fragment backstack
		String backStackName = FragmentExercises.TAG;
		getActivity().getSupportFragmentManager().popBackStackImmediate(backStackName, 0);
	}
	
	/**
	 * Launch FragmentExerciseResult.
	 * Puts session_id into a bundle and passes it along
	 * @param session_id
	 */
	private void goToResultFragment(int session_id) {
		
		FragmentExerciseResults newFragment = FragmentExerciseResults.newInstance(session_id);
		String nextFragmentTag = FragmentExerciseResults.TAG;
		FragmentHelper.swapFragments(getActivity().getSupportFragmentManager(), 
				R.id.activity_measure_container, newFragment, 
				false, true, TAG, nextFragmentTag);
	}
	
	/**
	 * This class is a CountDownTimer that counts with descending times.
	 * It has additional functionality:
	 * - can play sounds by starting a MyBeepCounter object in the last 5 seconds
	 * 		(enabled by beepEnabled in constructor)
	 * - updates the m_tv_time textView with the remaining time rounded to one decimal place
	 * - updates the isTimerDone boolean flag when the timer finishes
	 * - computes elapsed time (increasing) and returns it using getElapsedSec()
	 * @author Vivian
	 *
	 */
	public class MyCounter extends CountDownTimer {
		
		private final long millisInFuture;
		private long elapsedMilliSec;	// elapsed<Time> means counting up, from 0-max
		private double sec;
		private double roundedSec;
		private boolean beepEnabled;
		private boolean beepCounterStarted;
		private boolean isTimerDone;
		private boolean isRunning;
		
		public MyCounter(long millisInFuture, long countDownInterval, boolean beepEnabled) {
			super(millisInFuture, countDownInterval);
			elapsedMilliSec = 0;
			this.millisInFuture = millisInFuture;
			isTimerDone = false;
			beepCounterStarted = false;
			this.beepEnabled = beepEnabled;
			isTimerDone = false;
			isRunning = false;
		}
		
		@Override
		public void onFinish() {
			isTimerDone = true;
			isRunning = false;
		}
		
		@Override
		public void onTick(long millisUntilFinished) {
			isRunning = true;
			elapsedMilliSec = millisInFuture - millisUntilFinished; 
			sec = (millisUntilFinished/1000.000);
			roundedSec = (double)Math.round(sec * 10) / 10;
			mTextTime.setText((roundedSec) + " seconds");
			
			/* Feedback during exercise */
			if(mState == STATE_MEASURE) {
				feedbackStatus = parent.feedback_status;
				if(beepEnabled && !beepCounterStarted) {
					if (feedbackStatus == true) {
						new FeedbackSystem(30000, 500).start();
						beepCounterStarted = true;
					}
				}
			} else {
				if(millisUntilFinished < 5100 && beepEnabled && !beepCounterStarted) {
					new MyBeepCounter(5100, 1000).start();
					beepCounterStarted = true;
				}
			}
		}
		
		public float getElapsedSec() {
			return (float) (elapsedMilliSec/1000.000);
		}
		
		public boolean isTimerDone() {
			return isTimerDone;
		}
		
		public boolean isTimerRunning(){
			return isRunning;
		}
	}
	
	/**
	 * This class is a CountDownTimer that plays ping(shortDing)
	 * at every tick, and plays ping(longPingDing) when it finishes.
	 * @author Vivian
	 *
	 */
	public final class MyBeepCounter extends CountDownTimer {

		public MyBeepCounter(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		
		@Override
		public void onTick(long millisUntilFinished) {
			mSoundPoolHelper.shortDing();
		}

		@Override
		public void onFinish() {
			mSoundPoolHelper.longPingDing();
		}
	}
	
	/* Feedback counter that executes a sound based upon curBN, lowerBN and upperBN */
	public final class FeedbackSystem extends CountDownTimer {

		public FeedbackSystem(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		
		@Override
		public void onTick(long millisUntilFinished) {
			feedbackTask();
		}

		@Override
		public void onFinish() {
			feedbackTask();
			mSoundPoolHelper.stop();
		}
		
		public void feedbackTask () {
	    	lowerBN = parent.feedbackLowerBN;
	    	upperBN = parent.feedbackUpperBN;
	    	
	    	// Use Aggregate Balance Number 
			//mSoundPoolHelper.pingFeedback(curBN, lowerBN, upperBN);
	    	
	    	// Use Transient Balance Number
	    	mSoundPoolHelper.pingFeedback(tempBN, lowerBN, upperBN);
		}
	}
	
	private ArrayList<Float> readCalibrationValues() {
		Context context = getActivity();
		ArrayList<Float> al = new ArrayList<Float>();
		FileIOHelper m_fHelper = new FileIOHelper();
		String filename = FragmentCalibration.FILENAME + parent.getDeviceName() + parent.getDevice().getAddress() + ".csv"; 
		FileIOHelper.InternalStorageHelper m_isHelper = m_fHelper.new InternalStorageHelper(context, filename);
		
		// Check if FILENAME exists in internal storage:
		// If not, start ActivityCalibration, else just read it
		if(!m_isHelper.fileExists()) {
			
			// Create an intent to launch a new instance of ActivityCalibration
			Intent calibrationIntent = new Intent(getActivity(), ActivityCalibration.class);
			calibrationIntent.putExtra("device_name", parent.getDeviceName());
			calibrationIntent.putExtra("ble_device", (Parcelable) parent.getDevice());
			getActivity().startActivityForResult(calibrationIntent, 
					ActivityMain.CALIBRATION_REQUEST_CODE);
			
		} else {
			// Read from internal storage
			m_isHelper.makeReadFile();
			ArrayList<String> stringAL = m_isHelper.readFromFile();
			for(int i = 0; i < stringAL.size(); i++) {
				al.add(Float.parseFloat(stringAL.get(i)));
			}
			if(stringAL.size() == 6) {
				if(BuildConfig.DEBUG) Log.d("stringAL[0]", stringAL.get(0));
				if(BuildConfig.DEBUG) Log.d("stringAL[1]", stringAL.get(1));
				if(BuildConfig.DEBUG) Log.d("stringAL[2]", stringAL.get(2));
				if(BuildConfig.DEBUG) Log.d("stringAL[3]", stringAL.get(3));
				if(BuildConfig.DEBUG) Log.d("stringAL[4]", stringAL.get(4));
				if(BuildConfig.DEBUG) Log.d("stringAL[5]", stringAL.get(5));
			}
		}
		m_isHelper.close();
		return al;
	}
}