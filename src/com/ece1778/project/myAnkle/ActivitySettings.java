package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.R;
//import com.google.analytics.tracking.android.EasyTracker;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class ActivitySettings extends PreferenceActivity {
	public static final String TAG = ActivitySettings.class.getSimpleName();
	private static ActionBar actionBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
		
		actionBar = getActionBar();
	}
	
	@Override
	  public void onStart() {
	    super.onStart();
	    
	    // enable google analytics
//	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }
	
	@Override
	public void onStop() {
		super.onStop();
		
		// disable google analytics
//	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	
	public static void setActionBarHomeEnabled(boolean enabled) {
		
		// Change functionality
		actionBar.setHomeButtonEnabled(enabled);
		
		// Change visibility
	    actionBar.setDisplayHomeAsUpEnabled(enabled);
	}
		
	// very simple fragment for selecting preferences
	public static class PrefsFragment extends PreferenceFragment {
		
		public static final String TAG = FragmentExerciseInstruction.class.getSimpleName();
       
		@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
        
    	public void onResume() {
    		super.onResume();
    		
    		getActivity().getActionBar().setTitle("Settings");
			ActivitySettings.setActionBarHomeEnabled(false);
    	}
    }
	
}
