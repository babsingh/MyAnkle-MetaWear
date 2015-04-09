package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class FragmentExerciseDialog extends DialogFragment implements OnClickListener {
	
	public static final String TAG = FragmentExerciseDialog.class.getSimpleName();
	public static final String ARG_EXERCISE_ID = "exercise_id";
	
	// the call-back ID for this dialog. note: increment in other dialog fragments
	public static final int ID = 0;
	
	// call-back method argument bundle strings
	public static final String RETURN_ANKLE_SIDE = "return_ankle_side";
	public static final String RETURN_EYE_STATE = "return_eye_state";
	
	// layout objects
	private LinearLayout mLayoutAnkleSelection;
	private LinearLayout mLayoutEyeStateSelection;
	private ImageButton mImageButtonLeft;
	private ImageButton mImageButtonRight;
	private ImageButton mImageButtonEyesOpen;
	private ImageButton mImageButtonEyesClosed;
	private Button mButtonDone;
	
	// bundle argument strings
	private static final String ARG_IS_DISPLAY_ANKLE_LAYOUT = "is_display_ankle_layout";
	private static final String ARG_IS_DISPLAY_EYE_LAYOUT = "is_display_eye_layout";
	private static final String ARG_CURRENT_ANKLE = "current_ankle_side";
	private static final String ARG_CURRENT_EYE = "current_eye_state";
	
	// boolean values to decide which layouts to inflate
	private boolean mIsDisplayAnkleSide;
	private boolean mIsDisplayEyeState;
	
	// user-selected parameters
	private String mSelectedAnkleSide = null;
	private String mSelectedEyeState = null;
	
	// create a new instance of the interface
	private CustomDialogFinishListener mDialogFinishListener = null;
	
	// instantiate FragmentExerciseDialog
	public static FragmentExerciseDialog newInstance(boolean isDisplayAnkleSide,
			boolean isDisplayEyeState, String currentAnkleSide, String currentEyeState) {
		
		FragmentExerciseDialog myFragment = new FragmentExerciseDialog();
		
		Bundle args = new Bundle();
		args.putBoolean(ARG_IS_DISPLAY_ANKLE_LAYOUT, isDisplayAnkleSide);
		args.putBoolean(ARG_IS_DISPLAY_EYE_LAYOUT, isDisplayEyeState);
		args.putString(ARG_CURRENT_ANKLE, currentAnkleSide);
		args.putString(ARG_CURRENT_EYE, currentEyeState);
		myFragment.setArguments(args);
		
		return myFragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// get a reference to the created dialog
		Dialog thisDialog = super.onCreateDialog(savedInstanceState);
		
		// set the dialog parameters (windows preferences, animations)
		thisDialog.getWindow().requestFeature(STYLE_NO_TITLE);
		thisDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		
		return thisDialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// ensure that the activity exists and has implemented the
		// fragment's callback interface. else, throw an exception
		if(activity instanceof CustomDialogFinishListener) {
			mDialogFinishListener = (CustomDialogFinishListener) activity;
			
		} else if(BuildConfig.DEBUG) {
			throw new ClassCastException();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// retrieve the bundled values from the arguments
		mIsDisplayAnkleSide = getArguments().getBoolean(ARG_IS_DISPLAY_ANKLE_LAYOUT, true);
		mIsDisplayEyeState = getArguments().getBoolean(ARG_IS_DISPLAY_EYE_LAYOUT, true);
		
		// inflate the fragment's layout file
		View view = inflater.inflate(R.layout.fragment_exercise_dialog, container, false);
		
		// link the layout objects to the corresponding UI elements
		mLayoutAnkleSelection = (LinearLayout) view.findViewById(R.id.fragment_exercise_dialog_layout_ankle_selection);
		mLayoutEyeStateSelection = (LinearLayout) view.findViewById(R.id.fragment_exercise_dialog_layout_eye_state_selection);
		mImageButtonLeft = (ImageButton) view.findViewById(R.id.fragment_exercise_dialog_image_button_left);
		mImageButtonRight = (ImageButton) view.findViewById(R.id.fragment_exercise_dialog_image_button_right);
		mImageButtonEyesOpen = (ImageButton) view.findViewById(R.id.fragment_exercise_dialog_image_button_eyes_open);
		mImageButtonEyesClosed = (ImageButton) view.findViewById(R.id.fragment_exercise_dialog_image_button_eyes_closed);
		mButtonDone = (Button) view.findViewById(R.id.fragment_exercise_dialog_button_done);
		
		// register the button listeners
		mImageButtonLeft.setOnClickListener(this);
		mImageButtonRight.setOnClickListener(this);
		mImageButtonEyesOpen.setOnClickListener(this);
		mImageButtonEyesClosed.setOnClickListener(this);
		mButtonDone.setOnClickListener(this);
		
		// if the ankle-selection layout is not to be displayed, 
		// hide the corresponding UI container
		if(!mIsDisplayAnkleSide) {
			mLayoutAnkleSelection.setVisibility(View.GONE);
			
		} else {
			
			// retrieve the current ankle-side from the arguments
			String ankleSide = getArguments().getString(ARG_CURRENT_ANKLE);
			
			// if the current ankle-side is not null
			if(ankleSide != null) {
				
				// set the corresponding image-button state
				if(ankleSide.equals("Left")) {
					mImageButtonLeft.performClick();
					
				} else {
					mImageButtonRight.performClick();
				}
			} else {
				
				// by default, simulate a click on the left-ankle image-button
				mImageButtonLeft.performClick();
			}
		}
		
		// if the eye-state selection layout is not to be displayed, 
		// hide the corresponding UI container
		if(!mIsDisplayEyeState) {
			mLayoutEyeStateSelection.setVisibility(View.GONE);
			
		} else {
			
			// retrieve the current eye-state from the arguments
			String eyeState = getArguments().getString(ARG_CURRENT_EYE);
			
			// if the current eye-state is not null
			if(eyeState != null) {
				
				// set the corresponding image-button state
				if(eyeState.equals("Open")) {
					mImageButtonEyesOpen.performClick();
					
				} else {
					mImageButtonEyesClosed.performClick();
				}
			} else {
				
				// by default, simulate a click on the eyes-open image-button
				mImageButtonEyesOpen.performClick();
			}
		}
		
		return view;
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		
		// the left-ankle image-button is clicked
		case R.id.fragment_exercise_dialog_image_button_left: 
			
			// set the corresponding (left) ankle-side
			mSelectedAnkleSide = "Left";
			
			// set the corresponding button states
			setImageButtonParameters(mImageButtonRight, Color.TRANSPARENT, 
					R.drawable.menu_right_ankle);
			
			setImageButtonParameters(mImageButtonLeft, 
					getResources().getColor(R.color.blue), 
						R.drawable.menu_left_ankle_pressed);
			
			break;
		
		// the right-ankle image-button is clicked
		case R.id.fragment_exercise_dialog_image_button_right: 
			
			// set the corresponding (right) ankle-side
			mSelectedAnkleSide = "Right";
			
			// set the corresponding button states
			setImageButtonParameters(mImageButtonLeft, Color.TRANSPARENT, 
					R.drawable.menu_left_ankle);
			
			setImageButtonParameters(mImageButtonRight, 
					getResources().getColor(R.color.blue), 
						R.drawable.menu_right_ankle_pressed);
			
			break;
			
		// the eyes-open image-button is clicked
		case R.id.fragment_exercise_dialog_image_button_eyes_open:
			
			// set the corresponding (open) eye-state
			mSelectedEyeState = "Open";
			
			// set the corresponding button states
			setImageButtonParameters(mImageButtonEyesClosed, Color.TRANSPARENT, 
					R.drawable.ic_eyes_closed);
			
			setImageButtonParameters(mImageButtonEyesOpen, 
					getResources().getColor(R.color.blue),
						R.drawable.ic_eyes_open_pressed);
			
			break;
			
		// the eyes-closed image-button is clicked
		case R.id.fragment_exercise_dialog_image_button_eyes_closed:
			
			// set the corresponding (closed) eye-state
			mSelectedEyeState = "Closed";
			
			// set the corresponding button states
			setImageButtonParameters(mImageButtonEyesOpen, Color.TRANSPARENT,
					R.drawable.ic_eyes_open);
			
			setImageButtonParameters(mImageButtonEyesClosed, 
					getResources().getColor(R.color.blue), 
						R.drawable.ic_eyes_closed_pressed);
			
			break;
		
		// the 'Done' button is clicked
		case R.id.fragment_exercise_dialog_button_done: 
			
			// debug messages
			if(BuildConfig.DEBUG) Log.d(TAG, "Ankle: " + mSelectedAnkleSide +
					" & Eye-State: " + mSelectedEyeState);
			
			// create and populate a bundle to store return values
			Bundle bundle = new Bundle();
			bundle.putString(RETURN_ANKLE_SIDE, mSelectedAnkleSide);
			bundle.putString(RETURN_EYE_STATE, mSelectedEyeState);
			
			// if the created instance of the interface exists, initiate the call-back
			if(mDialogFinishListener != null) {
				mDialogFinishListener.onFinish(ID, bundle);
			}
			
			// close the displayed dialog
			getDialog().cancel();
			
			break;
		}
	}
	
	// set the corresponding ImageButton's background-color and image-resource
	// to the parameterized color and image
	private void setImageButtonParameters(ImageButton imageButton, int backgroundColor, 
			int imageResource) {
		
		imageButton.setBackgroundColor(backgroundColor);
		imageButton.setImageResource(imageResource);
	}
	
	// an interface to communicate with the parent activity through a callback
	public interface CustomDialogFinishListener {
		
		public void onFinish(int id, Bundle bundle);
	}
}