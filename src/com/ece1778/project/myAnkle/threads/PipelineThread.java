package com.ece1778.project.myAnkle.threads;

import com.ece1778.project.myAnkleUser.BuildConfig;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;


public final class PipelineThread extends Thread {

	private static final String TAG = PipelineThread.class.getSimpleName();

	private Handler mHandler;	
	private int mTotalQueued;	
	private int mTotalCompleted;	
	private PipelineThreadResponseListener mListener = null;
	
	public static interface PipelineThreadResponseListener {
		void handleThreadUpdate();
	}
	
	public PipelineThread(PipelineThreadResponseListener listener) {
		this.mListener = listener;
	}
	
	public PipelineThread() {
	}
	
	@Override
	public void run() {
		try {
			// preparing a looper on current thread			
			// the current thread is being detected implicitly
			Looper.prepare();

			if(BuildConfig.DEBUG) Log.i(TAG, "Pipeline thread entering the loop");

			// now, the handler will automatically bind to the
			// Looper that is attached to the current thread
			// You don't need to specify the Looper explicitly
			mHandler = new Handler();
			
			// After the following line the thread will start
			// running the message loop and will not normally
			// exit the loop unless a problem happens or you
			// quit() the looper (see below)
			Looper.loop();
			
			if(BuildConfig.DEBUG) Log.i(TAG, "Pipeline thread exiting gracefully");
		} catch (Throwable t) {
			if(BuildConfig.DEBUG) Log.e(TAG, "Pipeline thread halted due to an error", t);
		} 
	}
	
	// This method is allowed to be called from any thread
	public synchronized void requestStop() {
		// using the handler, post a Runnable that will quit()
		// the Looper attached to our DownloadThread
		// obviously, all previously queued tasks will be executed
		// before the loop gets the quit Runnable
		try {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					
					// This is guaranteed to run on the DownloadThread
					// so we can use myLooper() to get its looper
					if(BuildConfig.DEBUG) Log.i(TAG, "Pipeline thread loop quitting by request");
				
					Looper.myLooper().quit();
				}
			});
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void enqueueNewTask(final Runnable task) {
		// Wrap DownloadTask into another Runnable to track the statistics
		try {
			
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					
					try {
						task.run();
					} finally {					
						
						// register task completion
						synchronized (PipelineThread.this) {
							mTotalCompleted++;
						}
						
						// tell the listener something has happened
						signalUpdate();
					}				
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mTotalQueued++;
		
		// tell the listeners the queue is now longer
		signalUpdate();
	}
	
	public synchronized int getTotalQueued() {
		return mTotalQueued;
	}
	
	public synchronized int getTotalCompleted() {
		return mTotalCompleted;
	}	
	
	// Please note! This method will normally be called from the pipline thread.
	// Thus, it is up for the listener to deal with it
	private void signalUpdate() {
		if (mListener != null) {
			mListener.handleThreadUpdate();
		}
	}
}
