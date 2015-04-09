package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.FragmentExerciseDialog.CustomDialogFinishListener;
import com.ece1778.project.myAnkle.Helpers.FragmentHelper;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ActivityProgress extends FragmentActivity implements CustomDialogFinishListener {

	private static final String TAG = ActivityProgress.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_progress);
		
		// set up the actionBar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// create a new instance of FragmentProgress and add it to the placeholder
		FragmentProgressExercises newFragment = FragmentProgressExercises.newInstance();
		FragmentHelper.swapFragments(getSupportFragmentManager(), 
				R.id.activity_progress_container, newFragment, 
				true, false, null, FragmentProgressExercises.TAG);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// inflate the activity's menu layout file
		getMenuInflater().inflate(R.menu.progress_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		// display the menu
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
				
		switch(item.getItemId()) {
		
		// if the 'Up' (Home) button is clicked
		case android.R.id.home: 
			
			// implement the activity's default back-pressed behavior
			onBackPressed();
			return true;
		
		// if the 'Left Ankle' menu button is clicked
		case R.id.progress_menu_button_left_ankle: break;
		
		// if the 'Right Ankle' menu button is clicked
		case R.id.progress_menu_button_right_ankle: break;
		
		default: break;
		}
		
		/* return false to indicate that the click-event is not being
		 * consumed in the activity. the callback propagates to the fragments
		 * hosted by the activity, in the order in which they are attached */
		return false;
	}

	// the callback initiated when a dialog fragment is closed
	@Override
	public void onFinish(int id, Bundle bundle) {
		
		switch(id) {
		
		// the call-back was initiated by FragmentExerciseDialog
		case FragmentExerciseDialog.ID: 
			
			// retrieve the bundled argument string
			String eyeState = bundle.getString(FragmentExerciseDialog.RETURN_EYE_STATE);
			
			// get a reference to the currently displayed FragmentProgressExercises instance
			Fragment exerciseFragment = getSupportFragmentManager().
					findFragmentById(R.id.activity_progress_container);
			
			if(exerciseFragment != null && exerciseFragment instanceof 
					FragmentProgressExercises) {
				
				// initiate the call-back method in the progress exercises fragment
				((FragmentProgressExercises) exerciseFragment).updateSelection(eyeState);
				
			} else {
				
				// display a toast indicating failure to update selection
				Toast.makeText(this, "Selection not updated!", Toast.LENGTH_SHORT).show();
				
				// display an error-level message
				if(BuildConfig.DEBUG) Log.d(TAG, "FragmentProgressExercises is null");
			}
			
			break;
		}
	}
}
