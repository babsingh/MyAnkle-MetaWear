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

public class FragmentExercises extends ListFragment {
	
	public static final String TAG = FragmentExercises.class.getSimpleName();
	
	private DatabaseHelper mDatabaseHelper = null;
	private Cursor mConstantsCursor = null;
	private ListAdapterExercises mAdapter;
	
	// current state of ankle-side and eye-state image-buttons
	private String mCurrentAnkleSide = null;
	private String mCurrentEyeState = null;
	
	private Toast mToast = null;
	
	// instantiate FragmentExercises
	public static FragmentExercises newInstance() {
		FragmentExercises myFragment = new FragmentExercises();
		
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
		
		/* get the most recent ankle-side and eye-state selection from the 
		 * preferences. note: in case the fragment is being created for 
		 * the first time, both strings are guaranteed to be null */
		mCurrentAnkleSide = PrefUtils.getStringPreference(getActivity(), 
				PrefUtils.ANKLE_SIDE_KEY);
		
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
				newInstance(true, true, mCurrentAnkleSide, mCurrentEyeState);
		
		/* note: ChildFragmentManager is used to ensure that the life-cycle of
		 * the nested fragment (FragmentExerciseModeSelection) is synchronized
		 * with that of the parent (this) fragment, without additional checks */
		FragmentHelper.swapFragments(getChildFragmentManager(), 
				R.id.fragment_exercises_mode_selection_container, selectionFragment, 
				!replaceExistingFragment, false, null, FragmentExerciseModeSelection.TAG);
		
		// if either the current ankle-side or eye-state are null
		if(mCurrentAnkleSide == null || mCurrentEyeState == null) {
			
			// initialize the cursor for the first time
			updateListCursor("Open");
			
			// create the dialog fragment
			selectionFragment.createSelectionAlert(getActivity(), true, true);
			
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
	public void onResume() {
		super.onResume();
		
		// Set action bar parameters
		getActivity().getActionBar().setTitle("Exercises");
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// debug message
		if(BuildConfig.DEBUG) Log.i(TAG, "Number of backstack entries are : " +
				getActivity().getSupportFragmentManager().getBackStackEntryCount());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mConstantsCursor.close();
		mDatabaseHelper.close();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		// Get exercise_id
		if(BuildConfig.DEBUG) Log.i(TAG, "the count is " + String.valueOf(mAdapter.mRowDbMap.size()));
		int exercise_id = mAdapter.mRowDbMap.get(position); 
		
		// Pack arguments and start FragmentExerciseInstruction
		FragmentExerciseInstruction newFragment = FragmentExerciseInstruction.newInstance(exercise_id);
		String nextFragmentTag = FragmentExerciseInstruction.TAG;
		
		FragmentHelper.swapFragments(getActivity().getSupportFragmentManager(), 
				R.id.activity_measure_container, newFragment, 
				false, true, TAG, nextFragmentTag);
	}
	
	/* the result of the callback initiated in FragmentExerciseDialog 
	 * and propagated through the fragment's host activity */
	public void updateSelection(String ankleSide, String eyeState) {
		
		// get a reference to the fragment in the placeholder
		Fragment selectionFragment = getChildFragmentManager().
				findFragmentById(R.id.fragment_exercises_mode_selection_container);
		
		if(selectionFragment != null && selectionFragment instanceof 
				FragmentExerciseModeSelection) {
			
			// if the parameterized ankle-side is not null
			if(ankleSide != null) {
				
				// update the currently selected ankle-side
				mCurrentAnkleSide = ankleSide;
				
				// update the ankle-side in the shared preferences
				PrefUtils.setStringPreference(getActivity(), 
						PrefUtils.ANKLE_SIDE_KEY, ankleSide);
			}
			
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
				PrefUtils.setStringPreference(getActivity(), 
						PrefUtils.EYE_STATE_KEY, eyeState);
			}
			
			// update the button state of the image-button fragment
			((FragmentExerciseModeSelection) selectionFragment).updateSelection(
					mCurrentAnkleSide, mCurrentEyeState);
			
		} else if(BuildConfig.DEBUG) {
			
			// log a debug-level message indicating failure
			Log.d(TAG, FragmentExerciseModeSelection.TAG + " is null");
		}
		
		// cancel (override) any existing toast messages
		if(mToast != null) {
			mToast.cancel();
		}
		
		// create and display a toast message with the current selection
		mToast = Toast.makeText(getActivity(), mCurrentAnkleSide + " Ankle, Eyes "
				+ mCurrentEyeState, Toast.LENGTH_SHORT);
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