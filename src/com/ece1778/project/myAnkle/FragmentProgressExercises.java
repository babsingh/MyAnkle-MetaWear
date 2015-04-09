package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.FragmentHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class FragmentProgressExercises extends ListFragment {
	public static final String TAG = FragmentProgressExercises.class.getSimpleName();
	
	private DatabaseHelper mDatabaseHelper = null;
	private Cursor mConstantsCursor = null;
	private ListAdapterExercises mAdapter;
	
	// current state of the eye-state image-button
	private String mCurrentEyeState = null;
	
	private Toast mToast = null;
	
	public static FragmentProgressExercises newInstance() {
		FragmentProgressExercises myFragment = new FragmentProgressExercises();
		
		Bundle args = new Bundle();		
		myFragment.setArguments(args);
		
	    return myFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDatabaseHelper = new DatabaseHelper(getActivity());
	}
	  
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// inflate the fragment's layout file
		View view = inflater.inflate(R.layout.fragment_exercises, container, false);
		
		/* get the most recent eye-state from the preferences. note: in case the 
		 * fragment is being created for the first time, the string is guaranteed
		 * to be null */
		mCurrentEyeState = PrefUtils.getStringPreference(getActivity(), 
				PrefUtils.EYE_STATE_KEY);
		
		// get a reference to the existing instance of FragmentExerciseModeSelection
		Fragment previousFragment = getChildFragmentManager().findFragmentById(
				R.id.fragment_exercises_mode_selection_container);
				
		boolean replaceExistingFragment = false;
				
		// if it exists, replace the existing fragment instead of adding a new one
		if(previousFragment != null) {
			replaceExistingFragment = true;
		}
		
		// create a new instance of FragmentExerciseModeSelection and add it to the placeholder
		FragmentExerciseModeSelection selectionFragment = FragmentExerciseModeSelection.
				newInstance(false, true, null, mCurrentEyeState);
		
		/* note: ChildFragmentManager is used to ensure that the life-cycle of
		 * the nested fragment (FragmentExerciseModeSelection) is synchronized
		 * with that of the parent (this) fragment, without additional checks */
		FragmentHelper.swapFragments(getChildFragmentManager(), 
				R.id.fragment_exercises_mode_selection_container, selectionFragment, 
				!replaceExistingFragment, false, null, FragmentExerciseModeSelection.TAG);
		
		// if the eye-state is null
		if(mCurrentEyeState == null) {
			
			// initialize the cursor for the first time
			updateListCursor("Open");
			
			// create the dialog fragment
			selectionFragment.createSelectionAlert(getActivity(), false, true);
			
		} else {
			
			// update the exercises list
			updateListCursor(mCurrentEyeState);
		}
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		
		mAdapter = new ListAdapterExercises(getActivity(), mConstantsCursor);
		setListAdapter(mAdapter);
	}

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
       
		// Get exercise_id
        int exercise_id = mAdapter.mRowDbMap.get(position); 
		
		// instantiate FragmentProgressTable with the selected exercise_id, adding 
        // the current fragment to the back-stack
        Fragment newFragment = FragmentProgressGraphPager.newInstance(exercise_id);
        String nextFragmentTag = FragmentProgressGraphPager.TAG;
        
        FragmentHelper.swapFragments(getActivity().getSupportFragmentManager(), 
        		R.id.activity_progress_container, newFragment, 
        		false, true, TAG, nextFragmentTag);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		// Set action bar title and spinner visibility
		getActivity().getActionBar().setTitle("Progress");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mConstantsCursor.close();
		mDatabaseHelper.close();
	}   
	
	/* the result of the callback initiated in FragmentExerciseDialog 
	 * and propagated through the fragment's host activity */
	public void updateSelection(String eyeState) {
		
		// get a reference to the fragment in the placeholder
		Fragment selectionFragment = getChildFragmentManager().
			findFragmentById(R.id.fragment_exercises_mode_selection_container);
		
		if(selectionFragment != null && selectionFragment instanceof
				FragmentExerciseModeSelection) {
			
			// if the parameterized eye-state is not null
			if(eyeState != null) {
				
				// update the list-view only if the eye-state has recently changed
				if(mCurrentEyeState == null || (mCurrentEyeState != null &&
						!mCurrentEyeState.equals(eyeState))) {
				
					// update the cursor and re-create the displayed exercises list
					updateListCursor(eyeState);
					mAdapter.changeCursor(mConstantsCursor);
				}
				
				// update the currently selected eye-state
				mCurrentEyeState = eyeState;
				
				// update the eye-state in the shared preferences
				PrefUtils.setStringPreference(getActivity(), PrefUtils.EYE_STATE_KEY,
						eyeState);
			}
			
			// update the button state of the image-button fragment
			((FragmentExerciseModeSelection) selectionFragment).updateSelection(
					null, mCurrentEyeState);
			
		} else if(BuildConfig.DEBUG) {
			
			// log a debug-level message indicating failure
			Log.d(TAG, FragmentExerciseModeSelection.TAG + " is null");
		}
		
		// cancel (override) any existing toast messages
		if(mToast != null) {
			mToast.cancel();
		}
		
		// create and display a toast message with the current selection
		mToast = Toast.makeText(getActivity(), "Eyes " + mCurrentEyeState, 
				Toast.LENGTH_SHORT);
		
		mToast.show();
	}
	
	/* populates the exercise-list cursor with the exercises corresponding to the
	 * parameterized eye-state. note: this does not automatically update the list-view.
	 * use changeCursor() on the list-adapter object to redraw the view */
	private void updateListCursor(String eyeState) {
		
		if(eyeState != null) {
			
			// update the SQLite cursor associated with the list-adapter
			mConstantsCursor = mDatabaseHelper.getReadableDatabase().rawQuery(
					"SELECT _id, picture, name, equipment, eyeState, difficulty " +
							" FROM exercises WHERE eyeState = '" + eyeState + "' " +
									" ORDER BY position", null);
		}
	}
}