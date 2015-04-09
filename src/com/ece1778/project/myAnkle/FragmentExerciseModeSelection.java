package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class FragmentExerciseModeSelection extends Fragment implements OnClickListener {
	
	public static final String TAG = FragmentExerciseModeSelection.class.getSimpleName();
	
	// bundle argument strings
	private static final String ARG_IS_DISPLAY_ANKLE_SIDE = "is_display_ankle_side";
	private static final String ARG_IS_DISPLAY_EYE_STATE = "is_display_eye_state";
	private static final String ARG_CURRENT_ANKLE = "current_ankle_side";
	private static final String ARG_CURRENT_EYE = "current_eye_state";
	
	// layout objects
	private TextView mTextTitle;
	private ImageButton mImageButtonChangeSelection;
	private ImageButton mImageButtonAnkleSide;
	private ImageButton mImageButtonEyeState;
	
	// boolean values to decide which layouts to inflate
	private boolean mIsDisplayAnkleSide;
	private boolean mIsDisplayEyeState;
	
	// current state of ankle-side and eye-state image-buttons
	private String mCurrentAnkleSide = null;
	private String mCurrentEyeState = null;
	
	// instantiate FragmentExerciseModeSelection
	public static FragmentExerciseModeSelection newInstance(boolean isDisplayAnkle, 
			boolean isDisplayEye, String currentAnkleSide, String currentEyeState) {
		
		FragmentExerciseModeSelection myFragment = new FragmentExerciseModeSelection();
		
		Bundle args = new Bundle();
		args.putBoolean(ARG_IS_DISPLAY_ANKLE_SIDE, isDisplayAnkle);
		args.putBoolean(ARG_IS_DISPLAY_EYE_STATE, isDisplayEye);
		args.putString(ARG_CURRENT_ANKLE, currentAnkleSide);
		args.putString(ARG_CURRENT_EYE, currentEyeState);
		myFragment.setArguments(args);
		
		return myFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// retrieve the values from the arguments
		mIsDisplayAnkleSide = getArguments().getBoolean(ARG_IS_DISPLAY_ANKLE_SIDE, true);
		mIsDisplayEyeState = getArguments().getBoolean(ARG_IS_DISPLAY_EYE_STATE, true);
		
		// inflate the fragment's layout file
		View view = inflater.inflate(R.layout.fragment_exercise_mode_selections, container, false);
		
		// link the layout objects to the corresponding UI elements
		mTextTitle = (TextView) view.findViewById(R.id.fragment_exercise_selection_text_current_selection);
		mImageButtonChangeSelection = (ImageButton) view.findViewById(R.id.fragment_exercise_selection_image_button_reselect);
		mImageButtonAnkleSide = (ImageButton) view.findViewById(R.id.fragment_exercise_selection_image_button_ankle);
		mImageButtonEyeState = (ImageButton) view.findViewById(R.id.fragment_exercise_selection_image_button_eye);
		
		// register the on-click listeners
		mTextTitle.setOnClickListener(this);
		mImageButtonChangeSelection.setOnClickListener(this);
		mImageButtonAnkleSide.setOnClickListener(this);
		mImageButtonEyeState.setOnClickListener(this);
		
		/* hide or display the image-buttons based on the corresponding boolean values */
		if(!mIsDisplayAnkleSide) {
			mImageButtonAnkleSide.setVisibility(View.GONE);
		}
		
		if(!mIsDisplayEyeState) {
			mImageButtonEyeState.setVisibility(View.GONE);
		}
		
		// get the current ankle-side and eye-state from the arguments
		String ankleSide = getArguments().getString(ARG_CURRENT_ANKLE);
		String eyeState = getArguments().getString(ARG_CURRENT_EYE);
		
		// update the corresponding image-button states
		updateSelection(ankleSide, eyeState);
		
		return view;
	}
	
	public void createSelectionAlert(FragmentActivity activity, boolean isDisplayAnkle, 
			boolean isDisplayEye) {
		
		FragmentManager manager = activity.getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		
		// create a new instance of FragmentExerciseDialog
		DialogFragment dialogFragment = FragmentExerciseDialog.newInstance(
				isDisplayAnkle, isDisplayEye, mCurrentAnkleSide, mCurrentEyeState);
		
		Fragment previousFragment = manager.findFragmentByTag(FragmentExerciseDialog.TAG);
		
		if(previousFragment != null) {
			transaction.remove(previousFragment);
		}
		
		// set the dialog parameters and display the fragment dialog
		dialogFragment.setCancelable(false);
		dialogFragment.show(transaction, FragmentExerciseDialog.TAG);
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		
		// when the 'Current Selection' text-view is clicked
		case R.id.fragment_exercise_selection_text_current_selection: 
			
			// display the selection dialog alert
			createSelectionAlert(getActivity(), mIsDisplayAnkleSide, mIsDisplayEyeState);
			
			break;
		
		// when the 'Reselect' image-button is clicked
		case R.id.fragment_exercise_selection_image_button_reselect: 
			
			// display the selection dialog alert
			createSelectionAlert(getActivity(), mIsDisplayAnkleSide, mIsDisplayEyeState);
			
			break;
		
		// when the 'Reselect Ankle' image-button is clicked
		case R.id.fragment_exercise_selection_image_button_ankle: 
			
			// create the selection alert, displaying only the ankle-side
			createSelectionAlert(getActivity(), true, false);
			
			break;
		
		// when the 'Reselect Eye-state' image-button is clicked
		case R.id.fragment_exercise_selection_image_button_eye: 
			
			// create the selection alert, displaying only the eye-state
			createSelectionAlert(getActivity(), false, true);
			
			break;
		}
	}
	
	// update the image-buttons to reflect the current ankle-side and eye-state selections
	public void updateSelection(String ankleSide, String eyeState) {
		
		// if the ankle-side is not null and the corresponding view is to be displayed
		if(ankleSide != null && mIsDisplayAnkleSide) {
			
			// update the current ankle-side
			mCurrentAnkleSide = ankleSide;
			
			// update the current image-button selection
			if(ankleSide.equals("Left")) {
				mImageButtonAnkleSide.setImageResource(R.drawable.menu_left_ankle_pressed);
				
			} else {
				mImageButtonAnkleSide.setImageResource(R.drawable.menu_right_ankle_pressed);
			}
		}
		
		// if the eye-state is not null and the corresponding view is to be displayed
		if(eyeState != null && mIsDisplayEyeState) {
			
			// update the current eye-state
			mCurrentEyeState = eyeState;
			
			// update the current image-button selection
			if(eyeState.equals("Open")) {
				mImageButtonEyeState.setImageResource(R.drawable.ic_eyes_open_pressed);
				
			} else {
				mImageButtonEyeState.setImageResource(R.drawable.ic_eyes_closed_pressed);
			}
		}
	}
}
