package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DialogStyleHelper;
import com.ece1778.project.myAnkle.Helpers.FileIOHelper;
import com.ece1778.project.myAnkle.Helpers.SoundPoolHelper;
import com.ece1778.project.myAnkle.metawear.ModuleFragment;
import com.ece1778.project.myAnkleUser.R;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.Accelerometer.Component;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig.FullScaleRange;
import com.mbientlab.metawear.api.controller.Accelerometer.SamplingConfig.OutputDataRate;
import com.mbientlab.metawear.api.util.BytesInterpreter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/** A class to automate calibration data collection and access internal storage.
 * @author Vivian
 */
public class FragmentCalibration extends ModuleFragment implements SensorEventListener{
//	public class FragmentCalibration extends ModuleFragment {
	public static final String TAG = FragmentCalibration.class.getSimpleName();
	
	public static final int CALIBRATION_ACTIVITY_ID = 0;
	public static final String FILENAME = "Calibration_";
	private static final int X = 0, Y = 1, Z = 2, X_NEG = 3, Y_NEG = 4, Z_NEG = 5, DONE = 6;
	public static final String X_MEAN_ID = "X_MEAN_ID", Y_MEAN_ID = "Y_MEAN_ID", Z_MEAN_ID = "Z_MEAN_ID";
	public static final String XNEG_MEAN_ID = "XNEG_MEAN_ID", YNEG_MEAN_ID = "YNEG_MEAN_ID", ZNEG_MEAN_ID = "ZNEG_MEAN_ID";
	private final float LOWEST_ALLOWED_ACCELERATION = (float) 9.000000;
	private final float HIGHEST_ALLOWED_ACCELERATION = (float) 10.600000;
	private final long DCTIMER_DURATION_IN_MILLIS = 5000;
	private final int MAX_SAMPLE_SIZE = 200;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	private ActivityCalibration parent = null;
	
	private TextView mTextInstructions, mTextStatus;
	private ImageView mImageMain;
	private boolean mBeginCalibration;
	private int mCurrAxis;
	private ArrayList<Float> mRawAccel;
	private ArrayList<Float> mMeanAccel;
	private float mSum;

	private MyDataCollectionTimer mDataCollectionTimer;
	
	//MetaWear
	private Accelerometer accelController;
	private SamplingConfig samplingConfig;
	private Handler handler;
	private int dataRange= 2;
	private int samplingRate= 3;
	boolean initCond;
	boolean stop = false;
	byte[] config = new byte[] {0, 0, 0x20, 0, 0};
	
	private Accelerometer.Callbacks mCallback= new Accelerometer.Callbacks() {
		@Override
        public void receivedDataValue(short x, short y, short z) {
            if (!stop) {
//            	float elapsedTime = mTimer.getElapsedSec();
                float a = (float) (BytesInterpreter.bytesToGs(config, x) * 9.8);
                float b = (float) (BytesInterpreter.bytesToGs(config, y) * 9.8);
                float c = (float) (BytesInterpreter.bytesToGs(config, z) * 9.8);
                
            	evaluate(a, b, c);
                
//            	mSamples.add(elapsedTime, a, b, c);
            	Log.d("ADD_DATA", "FragmentExceriseMeasure: x = " + a + " y = " + b + " z = " + c + ".");
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
		mwController.removeModuleCallback(mCallback);
		super.onDestroy();
		mSoundPoolHelper.release();
	}
	
	// SoundPool
	private SoundPoolHelper mSoundPoolHelper;
	
	public static FragmentCalibration newInstance() {
		FragmentCalibration myFragment = new FragmentCalibration();
		
		Bundle args = new Bundle();		
		myFragment.setArguments(args);
		
	    return myFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		// pause calibration while user reads calibration alert
		mBeginCalibration = false;
		
		// alert the user that calibration is needed	
		createCalibrationInstructionAlert();
		
		// Initialize accelerometer
		if (((ActivityCalibration) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
			mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
	
		// NOTE: Calibration is first performed on the z-axis because there is minimal user error here. 
		// The app asks the user to put the phone on a flat surface.
		mCurrAxis = Z;
				
		// Initialize variables and arrays
		mSum = 0;
		mRawAccel = new ArrayList<Float>();
		mRawAccel.clear();
		
		// Initialize meanAccel ArrayList to store the 6 means
		// - load them with 0s
		// - set them when values are found
		// - NOTE: Do NOT use meanAccel.add(...) anywhere else! Use meanAccel.set(X or Y or Z or X_NEG or Y_NEG or Z_NEG) instead!
		mMeanAccel = new ArrayList<Float>();
		mMeanAccel.add((float) 0);
		mMeanAccel.add((float) 0);
		mMeanAccel.add((float) 0);
		mMeanAccel.add((float) 0);
		mMeanAccel.add((float) 0);
		mMeanAccel.add((float) 0);
		
		// Initialize SoundPool objects
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC); // Allow volume to be adjusted using hardware buttons
		mSoundPoolHelper = new SoundPoolHelper(getActivity());
        parent = ((ActivityCalibration) getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_calibration, container, false);
		// Initialize text and image views
		mTextInstructions = (TextView)view.findViewById(R.id.fragment_calibration_textview_instructions);
		mImageMain = (ImageView)view.findViewById(R.id.fragment_calibration_imageview_main);
		mTextStatus = (TextView)view.findViewById(R.id.fragment_calibration_textview_status);
		mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_1));
		mImageMain.setImageResource(R.drawable.z);
		return (view);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (!((ActivityCalibration) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
//			this.debugController.resetDevice();
	        handler = new Handler();
	        initCond = true;
	        new Thread(new Runnable() {
	        	@Override
	        	public void run() {
	        		while (initCond) {
	        			try {
	        				handler.post(new Runnable() {
								@Override
								public void run() {
									if(mCurrAxis == DONE){
										stop = true;
										accelController.disableNotification(Component.DATA);
//										accelController.stopComponents();
//						                accelController.disableAllDetection(true);
									} else if (mCurrAxis == Z) { 
										accelController.setComponentConfiguration(Component.DATA, config);
										accelController.enableNotification(Component.DATA);
////										accelController.enableMotionDetection(Axis.values());
//								        SamplingConfig config= accelController.enableXYZSampling();
//								        config.withFullScaleRange(FullScaleRange.values()[2])
//						                	.withOutputDataRate(OutputDataRate.values()[3]);
////								        accelConfig.initialize(config.getBytes());
//										accelController.startComponents();
									}
								}
							});
	        				Thread.sleep(150);
	        			} catch(Exception e) {
	        				
	        			}
	        		}
	        	}
	        }).start();
		}
	}
	
	public void evaluate (float x, float y, float z) {		
		if(mCurrAxis == DONE){
			stop = true;
		}
		
		if(mCurrAxis == Z) {	
			// Verify point z, store it. 
			checkAndStorePoint(z);
			
			// Done collecting z-accelerations
			if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
				mCurrAxis = X;
				// Calculate and store the z-mean
				mMeanAccel.set(Z, (float)mSum/mRawAccel.size());
				reset();
				mSoundPoolHelper.shortDing();
				// Update the view for next axis
				mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_2));
				mImageMain.setImageResource(R.drawable.x);
			}
			
		} else if(mCurrAxis == X) {
			// Verify point x, store it.
			checkAndStorePoint(x);
			
			// Done collecting x-accelerations
			if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
				mCurrAxis = Y;
				// Calculate and store the x-mean
				mMeanAccel.set(X, (float)mSum/mRawAccel.size());
				// Compare the recently calculated x-mean with z-mean
				compareToZMean(mMeanAccel.get(X));
				reset();
				mSoundPoolHelper.shortDing();
				mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_3));
				mImageMain.setImageResource(R.drawable.y);
			}
			
		} else if(mCurrAxis == Y) {	
			// Verify point y, store it.
			checkAndStorePoint(y);
			
			// Done collecting y-accelerations
			if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
				mCurrAxis = X_NEG;
				// Calculate and store the y-Mean
				mMeanAccel.set(Y, (float)mSum/mRawAccel.size());
				// Compare the recently calculated y-mean and compare with z-mean
				compareToZMean(mMeanAccel.get(Y));
				reset();
				mSoundPoolHelper.shortDing();
				mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_4));
				mImageMain.setImageResource(R.drawable.xneg);
			}
			
		} else if(mCurrAxis == X_NEG) {
			float xneg = x;
			
			// Verify point xneg, store it.
			checkAndStorePoint(-1*xneg);
			
			// Done collecting xnegs
			if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
				mCurrAxis = Y_NEG;
				// Calculate and store the xneg-mean
				mMeanAccel.set(X_NEG, (float)mSum/mRawAccel.size());
				// Compare the recently calculated y-mean and compare with z-mean
				compareToZMean(mMeanAccel.get(X_NEG));
				reset();
				mSoundPoolHelper.shortDing();
				mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_5));
				mImageMain.setImageResource(R.drawable.yneg);
			}
			
		} else if(mCurrAxis == Y_NEG) {
				float yneg = y;
				
				// Verify point yneg, store it.
				checkAndStorePoint(-1*yneg);
				
				// Done collecting ynegs
				if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
					mCurrAxis = Z_NEG;
					// Calculate and store the yneg-mean
					mMeanAccel.set(Y_NEG, (float)mSum/mRawAccel.size());
					// Compare the recently calculated yneg-mean and compare with z-mean
					compareToZMean(mMeanAccel.get(Y_NEG));
					reset();
					mSoundPoolHelper.shortDing();
					mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_6));
					mImageMain.setImageResource(R.drawable.zneg);
				}
				
		} else if(mCurrAxis == Z_NEG) {
			float zneg = z;
			
			// Verify point zneg, store it.
			checkAndStorePoint(-1*zneg);
			
			// Done collecting znegs
			if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
				mCurrAxis = DONE;
				// Kill the timer
				mDataCollectionTimer.cancel();
				// Calculate and store the zneg-mean
				mMeanAccel.set(Z_NEG, (float)mSum/mRawAccel.size());
				// Compare the recently calculated zneg-mean and compare with z-mean
				compareToZMean(mMeanAccel.get(Z_NEG));
				mSoundPoolHelper.longPingDing();
				mTextInstructions.setText("Done");
			}
		// Done collecting all six means
		} else if(mCurrAxis == DONE){
			// Increment m_curAxis so onSensorChanged becomes noop (due to all the if-statements)
			mCurrAxis++;
			// Change button and text indicators
			mTextInstructions.setText("Finishing");
			
			// Write the values collected in meanAccel to internal storage
			writeCalibrationValues(getActivity(), 
					mMeanAccel.get(X), 
					mMeanAccel.get(Y),
					mMeanAccel.get(Z), 
					mMeanAccel.get(X_NEG),
					mMeanAccel.get(Y_NEG),
					mMeanAccel.get(Z_NEG));
	      
			// alert the user that calibration is complete
			createCalibrationFinishedAlert();
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {		
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && mBeginCalibration == true) {
			
			if(mCurrAxis == Z) {
				float z = event.values[2];
					
				// Verify point z, store it. 
				checkAndStorePoint(z);
				
				// Done collecting z-accelerations
				if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
					mCurrAxis = X;
					// Calculate and store the z-mean
					mMeanAccel.set(Z, (float)mSum/mRawAccel.size());
					reset();
					mSoundPoolHelper.shortDing();
					// Update the view for next axis
					mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_2));
					mImageMain.setImageResource(R.drawable.x);
				}
				
			} else if(mCurrAxis == X) {
				float x = event.values[0];
				
				// Verify point x, store it.
				checkAndStorePoint(x);
				
				// Done collecting x-accelerations
				if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
					mCurrAxis = Y;
					// Calculate and store the x-mean
					mMeanAccel.set(X, (float)mSum/mRawAccel.size());
					// Compare the recently calculated x-mean with z-mean
					compareToZMean(mMeanAccel.get(X));
					reset();
					mSoundPoolHelper.shortDing();
					mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_3));
					mImageMain.setImageResource(R.drawable.y);
				}
				
			} else if(mCurrAxis == Y) {
				float y = event.values[1];
				
				// Verify point y, store it.
				checkAndStorePoint(y);
				
				// Done collecting y-accelerations
				if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
					mCurrAxis = X_NEG;
					// Calculate and store the y-Mean
					mMeanAccel.set(Y, (float)mSum/mRawAccel.size());
					// Compare the recently calculated y-mean and compare with z-mean
					compareToZMean(mMeanAccel.get(Y));
					reset();
					mSoundPoolHelper.shortDing();
					mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_4));
					mImageMain.setImageResource(R.drawable.xneg);
				}
				
			} else if(mCurrAxis == X_NEG) {
				float xneg = event.values[0];
				
				// Verify point xneg, store it.
				checkAndStorePoint(-1*xneg);
				
				// Done collecting xnegs
				if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
					mCurrAxis = Y_NEG;
					// Calculate and store the xneg-mean
					mMeanAccel.set(X_NEG, (float)mSum/mRawAccel.size());
					// Compare the recently calculated y-mean and compare with z-mean
					compareToZMean(mMeanAccel.get(X_NEG));
					reset();
					mSoundPoolHelper.shortDing();
					mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_5));
					mImageMain.setImageResource(R.drawable.yneg);
				}
				
			} else if(mCurrAxis == Y_NEG) {
					float yneg = event.values[1];
					
					// Verify point yneg, store it.
					checkAndStorePoint(-1*yneg);
					
					// Done collecting ynegs
					if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
						mCurrAxis = Z_NEG;
						// Calculate and store the yneg-mean
						mMeanAccel.set(Y_NEG, (float)mSum/mRawAccel.size());
						// Compare the recently calculated yneg-mean and compare with z-mean
						compareToZMean(mMeanAccel.get(Y_NEG));
						reset();
						mSoundPoolHelper.shortDing();
						mTextInstructions.setText(getResources().getString(R.string.calibration_instruction_6));
						mImageMain.setImageResource(R.drawable.zneg);
					}
					
			} else if(mCurrAxis == Z_NEG) {
				float zneg = event.values[2];
				
				// Verify point zneg, store it.
				checkAndStorePoint(-1*zneg);
				
				// Done collecting znegs
				if(mDataCollectionTimer.isFinished || mRawAccel.size() > MAX_SAMPLE_SIZE) {
					mCurrAxis = DONE;
					// Kill the timer
					mDataCollectionTimer.cancel();
					// Calculate and store the zneg-mean
					mMeanAccel.set(Z_NEG, (float)mSum/mRawAccel.size());
					// Compare the recently calculated zneg-mean and compare with z-mean
					compareToZMean(mMeanAccel.get(Z_NEG));
					mSoundPoolHelper.longPingDing();
					mTextInstructions.setText("Done");
				}
			// Done collecting all six means
			} else if(mCurrAxis == DONE){
				// Increment m_curAxis so onSensorChanged becomes noop (due to all the if-statements)
				mCurrAxis++;
				// Change button and text indicators
				mTextInstructions.setText("Finishing");
				
				// Write the values collected in meanAccel to internal storage
				writeCalibrationValues(getActivity(), 
						mMeanAccel.get(X), 
						mMeanAccel.get(Y),
						mMeanAccel.get(Z), 
						mMeanAccel.get(X_NEG),
						mMeanAccel.get(Y_NEG),
						mMeanAccel.get(Z_NEG));
		      
				// alert the user that calibration is complete
				createCalibrationFinishedAlert();
			}
		}
	}
	
	private void checkAndStorePoint(float point) {
		// Store point if within thresholds
		if(point > LOWEST_ALLOWED_ACCELERATION && point < HIGHEST_ALLOWED_ACCELERATION) {
			// set the status text view
			mTextStatus.setText(getResources().getString(R.string.calibration_status_good));
			mTextStatus.setTextColor(getResources().getColor(R.color.blue));
			
			// add current point to the collected points
			mRawAccel.add(point);
			mSum += point;
			
		// point has taken a strange value, clear raw accelerations and reset sum
		} else {
			
			// set the status textview
			mTextStatus.setText(getResources().getString(R.string.calibration_status_not_good));
			mTextStatus.setTextColor(getResources().getColor(R.color.skin));
			
			// clear previously collected points and reset the timer
			mDataCollectionTimer.restart();
			mRawAccel.clear();
			mSum = 0;
		}
	}
	// Compares mean to z-mean, throws a dialog to finish() if there's a large difference between them
	private void compareToZMean(float mean) {
		if(Math.abs(mean - mMeanAccel.get(Z)) > 0.1*LOWEST_ALLOWED_ACCELERATION) {					
			createCalibrationErrorAlert();		
		}		
	}
	
	private void reset() {
		// Restart timer
		mDataCollectionTimer.restart();
		// Clear sum and rawAccel ArrayList to be reused
		mSum = 0;
		mRawAccel.clear();
	}

	@Override
	public void onResume() {
		super.onResume();
		// disable the action bar home button (so user can't accidentally go back)
		getActivity().getActionBar().setTitle("Calibrating");		
		//ActivityMain.setActionBarHomeEnabled(false);
		
		if (((ActivityCalibration) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
			mSensorManager.registerListener(this, mAccelerometer,
			SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		mDataCollectionTimer = new MyDataCollectionTimer(DCTIMER_DURATION_IN_MILLIS, 1000);
		mDataCollectionTimer.start();
		
		mSoundPoolHelper.resume();
		
	}

	@Override
	public void onPause() {
		//enable the action bar home button (so user can't accidentally go back)	
		//ActivityMain.setActionBarHomeEnabled(true);
		super.onPause();
		
		if (((ActivityCalibration) getActivity()).getDeviceName().equalsIgnoreCase("inbuilt")) {
			mSensorManager.unregisterListener(this);
		}
		
		if(!getActivity().isFinishing()) {
			mSoundPoolHelper.pause();
		}
	}
	
	// called in ActivityCalibration when the back button is pressed
	// create an alert dialog asking whether calibration should be ended
	public void onBackPressed() {
		onPause();
		
		createCalibrationExitEarlyAlert();

	}
	
	public class MyDataCollectionTimer extends CountDownTimer {
		public boolean isFinished;
		public MyDataCollectionTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			isFinished = false;
		}
		
		@Override
		public void onFinish() {
			isFinished = true;
		}

		@Override
		public void onTick(long millisUntilFinished) {		
		}
		
		public void restart() {
			isFinished = false;
			cancel();
			start();
		}
	}
	
	public static boolean isCalibrated(Context context, String name, String address) {
		FileIOHelper m_fHelper = new FileIOHelper();
		String fileName = FILENAME + name + address + ".csv"; 
		FileIOHelper.InternalStorageHelper m_isHelper = m_fHelper.new InternalStorageHelper(context, fileName);
		return m_isHelper.fileExists();
	}
		
	public void writeCalibrationValues(Context context, float x, float y, float z, float xneg, float yneg, float zneg) {
		FileIOHelper m_fHelper = new FileIOHelper();
		String filename = FILENAME + parent.getDeviceName() + parent.getDevice().getAddress() + ".csv"; 
		FileIOHelper.InternalStorageHelper m_isHelper = m_fHelper.new InternalStorageHelper(context, filename);
		if(x == 0) {
			Toast.makeText(context, "Error: calibration values collected were weird.", Toast.LENGTH_SHORT).show();
		return;
		}
		m_isHelper.makeWriteFile();
		m_isHelper.buildWriteString(String.valueOf(x) + "\n");
		m_isHelper.buildWriteString(String.valueOf(y) + "\n");
		m_isHelper.buildWriteString(String.valueOf(z) + "\n");
		m_isHelper.buildWriteString(String.valueOf(xneg) + "\n");
		m_isHelper.buildWriteString(String.valueOf(yneg) + "\n");
		m_isHelper.buildWriteString(String.valueOf(zneg) + "\n");
		m_isHelper.writeToFile();
		m_isHelper.close();
	}
	
	
	private void createCalibrationExitEarlyAlert(){
		
		AlertDialog.Builder dlgAlert= new AlertDialog.Builder(getActivity())
			.setTitle(getResources().getString(R.string.calibration_back_alert_title))
			.setCancelable(false)
			.setNegativeButton(getResources().getString(R.string.calibration_back_alert_negitive_button), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					onResume();
				}
			})
			.setPositiveButton(getResources().getString(R.string.calibration_back_alert_positive_button), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					// calibration didn't complete successfully. return error-level message
					// to the caller and finish the current activity
					Intent returnIntent = new Intent();
					getActivity().setResult(Activity.RESULT_CANCELED, returnIntent);
					getActivity().finish();
				}
			});
		
		// create a new instance of DialogHelper, set parameters and display the dialog
		DialogStyleHelper box = new DialogStyleHelper(getActivity(), dlgAlert.create());
		box.showDialog();
	}
	
	
	private void createCalibrationErrorAlert(){
		AlertDialog.Builder dlgAlert= new AlertDialog.Builder(getActivity())
	    	.setTitle(getResources().getString(R.string.calibration_error_alert_title))
	    	.setCancelable(false)
	        .setPositiveButton(getResources().getString(R.string.calibration_error_alert_positive_button),
	        	new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {

	                	// calibration didn't complete successfully. return error-level message
						// to the caller and finish the current activity
	                	Intent returnIntent = new Intent();
						getActivity().setResult(Activity.RESULT_CANCELED, returnIntent);
						getActivity().finish();
	                }
	        });
		
		// create a new instance of DialogHelper, set parameters and display the dialog
		DialogStyleHelper box = new DialogStyleHelper(getActivity(), dlgAlert.create());
		box.setDialogButtonParams(null, -1, getActivity().getResources().getColor(R.color.blue));
		box.showDialog();
	}
	
	private void createCalibrationInstructionAlert(){
		// Fix Portrait orientation
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// Build a one button dialog and show it
		AlertDialog.Builder dlgAlert= new AlertDialog.Builder(getActivity())
			.setTitle(getResources().getString(R.string.calibration_initial_alert_title))
		    .setMessage(getResources().getString(R.string.calibration_initial_alert_message))
		    .setCancelable(false)
		    .setPositiveButton(getResources().getString(R.string.calibration_initial_alert_positive_button), new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int whichButton) {
		    		mBeginCalibration = true;
		    	}
		    });
		
		// create a new instance of DialogHelper, set parameters and display the dialog
		DialogStyleHelper box = new DialogStyleHelper(getActivity(), dlgAlert.create());
		box.setDialogButtonParams(null, -1, getActivity().getResources().getColor(R.color.blue));
		box.showDialog();
	}
	
	private void createCalibrationFinishedAlert(){
		
	    AlertDialog.Builder dlgAlert= new AlertDialog.Builder(getActivity())
	    	.setTitle(getResources().getString(R.string.calibration_done_alert_title))
	    	.setCancelable(false)
	    	.setPositiveButton(getResources().getString(R.string.calibration_done_alert_positive_button), new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int whichButton) {
	    			
	    			// calibration completed successfully. return a success message to the 
	    			// caller and finish the current activity
      		  		Intent returnIntent = new Intent();
      		  		getActivity().setResult(Activity.RESULT_OK, returnIntent);
      		  		getActivity().finish();
	    		}
	    	});

	    // create a new instance of DialogHelper, set parameters and display the dialog
	    DialogStyleHelper box = new DialogStyleHelper(getActivity(), dlgAlert.create());
	    box.showDialog();
	}
}
