 package com.ece1778.project.myAnkle.threads;

import android.app.ProgressDialog;
import android.os.Handler;

import com.ece1778.project.myAnkle.Helpers.FileIOHelper;
import com.ece1778.project.myAnkle.Helpers.Samples;
import com.ece1778.project.myAnkle.Helpers.Samples.Sample;

public class RunnableSaveRawDataFile implements Runnable {
	
	@SuppressWarnings("unused")
	private static final String TAG = RunnableSaveRawDataFile.class.getSimpleName();
	
	private FileIOHelper.ExternalStorageHelper mEsHelper;
	private ProgressDialog mProgressDialog;
	private Handler mHandler;
	
	private Samples mSamples;
	private int mUserId;
	private float mMeanR;
	private String mGender;
	private int mAge; 
	private String mInjLeft; 
	private String mInjRight; 
	private int mExercise;
	private String mDate;

	
	public RunnableSaveRawDataFile(FileIOHelper.ExternalStorageHelper esHelper, 
			int userId, String gender, int age, String injLeft, String injRight, int exercise, String date,
			Samples samples, ProgressDialog pd, Handler h) {
		
		mEsHelper = esHelper;
		mSamples = samples;
		mUserId = userId;
		mMeanR = mSamples.get_mean_r();
		mProgressDialog = pd;
		mHandler = h;
		mGender = gender;
		mAge = age; 
		mInjLeft = injLeft; 
		mInjRight = injRight; 
		mExercise = exercise;
		mDate = date;
	}

	@Override
	public void run() {
		mEsHelper.makeWriteFile();
		if(mEsHelper.fileExists()) {
	
			// populate csv file
			int num_samples = mSamples.getNumSamples();
			String lineToWrite;			
			
			// header information
			lineToWrite = "_META_";
			mEsHelper.writeLineToFile(lineToWrite);
			lineToWrite = "userId," + String.valueOf(mUserId);
			mEsHelper.writeLineToFile(lineToWrite);
			lineToWrite = "meanR," + String.valueOf(mMeanR);
			mEsHelper.writeLineToFile(lineToWrite);
			lineToWrite = "gender," + String.valueOf(mGender);
			mEsHelper.writeLineToFile(lineToWrite);
			lineToWrite = "age," + String.valueOf(mAge);
			mEsHelper.writeLineToFile(lineToWrite);
			lineToWrite = "injLeft," + String.valueOf(mInjLeft);
			mEsHelper.writeLineToFile(lineToWrite);
			lineToWrite = "injRight," + String.valueOf(mInjRight);
			mEsHelper.writeLineToFile(lineToWrite);
			lineToWrite = "exercise," + String.valueOf(mExercise);
			mEsHelper.writeLineToFile(lineToWrite);
			lineToWrite = "date," + String.valueOf(mDate);
			mEsHelper.writeLineToFile(lineToWrite);
			
			// add buffer space for extra header information we may want in the future
			for(int i = 0 ; i < 10 ; i++){
				lineToWrite = "";
				mEsHelper.writeLineToFile(lineToWrite);
			}
						
			// main data
			lineToWrite = "_DATA_";
			mEsHelper.writeLineToFile(lineToWrite);
			
			for (int i = 0; i < num_samples; i++) {
				Sample sample = mSamples.getSample(i);
				lineToWrite = 	String.valueOf(sample.t() + ", ") +
								String.valueOf(sample.x_cal() + ", ") + 
								String.valueOf(sample.y_cal() + ", ") + 
								String.valueOf(sample.z_cal());
				
				mEsHelper.writeLineToFile(lineToWrite);
				
				mHandler.post(new Runnable() {
					public void run() {
						int curProgress = mProgressDialog.getProgress();
						if(curProgress <= mProgressDialog.getMax()) 
							mProgressDialog.setProgress(curProgress + 1);
					}
				});
			}
		}
		
		mEsHelper.close();
		mProgressDialog.dismiss();
	}
}
