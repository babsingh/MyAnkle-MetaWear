package com.ece1778.project.myAnkle.Helpers;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

public class SoundPoolHelper {
	public static final String TAG = SoundPoolHelper.class.getSimpleName();
	private final int LAST_TO_LOAD;
	private Context m_context;
	private SoundPool m_sp;
	private boolean loaded = false;
	
	// Sounds
	private int longPingDing;
	private int shortDing;
	
	public SoundPoolHelper(Context context) {
		m_context = context;
		m_sp = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);
	    m_sp.setOnLoadCompleteListener(new OnLoadCompleteListener() {
	    	@Override
	    	public void onLoadComplete(SoundPool sp, int sampleId,
	    			int status) {
	    		if(sampleId == LAST_TO_LOAD) {
	    			loaded = true;
	    		}
	    	}
	    });
	    
	    // Longer sound - signals end (and success) of entire calibration activity
	    longPingDing = m_sp.load(m_context, R.raw.pingding, 1);
	    
	    // Shorter sound - signals end of one axis of calibration
	    shortDing = m_sp.load(m_context, R.raw.lumding, 1);
	    
	    // Feedback Good Sound
	    soundGood1 = m_sp.load(m_context, R.raw.g3string, 1);
	    soundGood2 = m_sp.load(m_context, R.raw.d4string, 1);
	    
	    // Feedback Excellent Sound
	    soundExcellent1 = m_sp.load(m_context, R.raw.e1string, 1);
	    soundExcellent2 = m_sp.load(m_context, R.raw.b2string, 1);
	    
	    // Feedback Fail Sound
	    soundFail1 = m_sp.load(m_context, R.raw.a5string, 1);
	    soundFail2 = m_sp.load(m_context, R.raw.e6string, 1);
	    
	    // LAST_TO_LOAD is the integer ID of the last sound to use m_sp.load(...)
	    LAST_TO_LOAD = soundFail2;
	}
	
	public void pause() {
		m_sp.autoResume();
	}
	public void resume() {
		m_sp.autoResume();
	}
	public void release() {
		m_sp.release();
	}
	
	public void longPingDing() {
		ping(longPingDing);
	}
	public void shortDing() {
		ping(shortDing);
	}
	
	private void ping(int soundID) {
		if(PrefUtils.getPingCheckBox(m_context)) {
			// Getting the user sound settings
		    AudioManager audioManager = (AudioManager) m_context.getSystemService(Context.AUDIO_SERVICE);
		    float actualVolume = (float) audioManager
		        .getStreamVolume(AudioManager.STREAM_MUSIC);
		    float maxVolume = (float) audioManager
		        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		    float volume = actualVolume / maxVolume;
		    // Is the sound loaded already?
		    if (loaded) {
		    	m_sp.play(soundID, volume, volume, 1, 0, 1f);
		      if(BuildConfig.DEBUG) Log.d(TAG, "Played sound");
		    }
		} else {
			if(BuildConfig.DEBUG) Log.d(TAG, "Suppressed sound");
		}
	}

	// Feedback System
	private int currentStreamID = 0;
	private int change = 0;
	private int soundFail1;
	private int soundFail2;
	private int soundGood1;
	private int soundGood2;
	private int soundExcellent1;
	private int soundExcellent2;
	
	// Feedback sound - 3 stages
	public void pingFeedback (double curBN, double targetBN, double oldBN) {
		if (targetBN <= curBN && curBN < (targetBN + ((oldBN - targetBN)/2))) {
			//Between old balance number and target balance number 1
			if (change != 1) {
				if (change == 0) {
					playFeedbackSound(soundGood1);
				} else {
					m_sp.stop(currentStreamID);
					playFeedbackSound(soundGood1);
				}
				change = 1;
			}
		} else if ((targetBN + ((oldBN - targetBN)/2)) <= curBN && curBN < oldBN) {
			//Between old balance number and target balance number 2
			if (change != 2) {
				if (change == 0) {
					playFeedbackSound(soundGood2);
				} else {
					m_sp.stop(currentStreamID);
					playFeedbackSound(soundGood2);
				}
				change = 2;
			}
		} else if (curBN < targetBN/2) {
			//Doing better than expected1
			if (change != 3) {
				if (change == 0) {
					playFeedbackSound(soundExcellent1);
				} else {
					m_sp.stop(currentStreamID);
					playFeedbackSound(soundExcellent1);
				}
				change = 3;
			}
		} else if (curBN >= targetBN/2 && curBN < targetBN) {
			//Doing better than expected2
			if (change != 4) {
				if (change == 0) {
					playFeedbackSound(soundExcellent2);
				} else {
					m_sp.stop(currentStreamID);
					playFeedbackSound(soundExcellent2);
				}
				change = 4;
			}
		} else if (curBN >= oldBN && curBN < (oldBN + ((oldBN - targetBN)/2))) {
			//Doing worse; no improvement 1
			if (change != 5) {
				if (change == 0) {
					playFeedbackSound(soundFail1);
				} else {
					m_sp.stop(currentStreamID);
					playFeedbackSound(soundFail1);
				}
				change = 5;
			}
		} else if (curBN >= (oldBN + ((oldBN - targetBN)/2))) {
			//Doing worse; no improvement 2
			if (change != 6) {
				if (change == 0) {
					playFeedbackSound(soundFail2);
				} else {
					m_sp.stop(currentStreamID);
					playFeedbackSound(soundFail2);
				}
				change = 6;
			}
		} else {
				//Invalid - Out of Scope; Should not be executed
		}
	}
	
	// Play feedback sound in infinite loop
	private void playFeedbackSound(int soundID) {
		if(PrefUtils.getPingCheckBox(m_context)) {
			// Getting the user sound settings
		    AudioManager audioManager = (AudioManager) m_context.getSystemService(Context.AUDIO_SERVICE);
		    float actualVolume = (float) audioManager
		        .getStreamVolume(AudioManager.STREAM_MUSIC);
		    float maxVolume = (float) audioManager
		        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		    float volume = actualVolume / maxVolume;
		    // Is the sound loaded already?
		    if (loaded) {
		    	currentStreamID = m_sp.play(soundID, volume, volume, 1, -1, 1f);
		      if(BuildConfig.DEBUG) Log.d(TAG, "Played sound");
		    }
		} else {
			if(BuildConfig.DEBUG) Log.d(TAG, "Suppressed sound");
		}
	}

	// Stop Feedback sound
	public void stop() {
		change = 0;
		m_sp.stop(currentStreamID);
	}
	
//	//Feedback
//	private double oldBN = 0.05;
//	float volFeed = 0.5f;
//	float rateFeed = 2.0f;
//	float loopFeed = 0.2f;
//	
//	private void reset () {
//		volFeed = 0.5f;
//		rateFeed = 0.1f;
//		loopFeed = 0.1f;
//	}
//	
//	public void pingFeedback (double curBN, double lowerBN, double upperBN) {
//		int soundID = shortDing;
//		if(PrefUtils.getPingCheckBox(m_context)) {
//			if (oldBN == -1) {
//				m_sp.play(soundID, volFeed, volFeed, 1, (int) loopFeed, rateFeed);
//				oldBN = curBN;
//			} else {
//				float change = (float) ((curBN-oldBN)/oldBN);
//				volFeed += change*volFeed;
//				loopFeed += 1.25*change*loopFeed;
//				rateFeed += 0.5*change*rateFeed;
//				
//				m_sp.play(soundID, volFeed, volFeed, 1, (int) loopFeed, rateFeed);
//				oldBN = curBN;
//			}
//		} else {
//			if(BuildConfig.DEBUG) Log.d(TAG, "Suppressed sound");
//		}
//	}
}
