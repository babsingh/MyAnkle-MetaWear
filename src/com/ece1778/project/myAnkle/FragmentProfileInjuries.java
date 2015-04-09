package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.DialogStyleHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FragmentProfileInjuries extends Fragment implements OnCheckedChangeListener, 
	OnClickListener {

	public static final String TAG = FragmentProfileInjuries.class.getSimpleName();
	public static final String ARG_PROFILE_MODE = "profile_mode";
	
	// the request codes with which to launch child activities
	public static final int TUTORIAL_REQUEST_CODE = 1;
	
	private String mDateInjuryLeft = "", mDateInjuryRight = "";
	private String mMostRecentInjuryLeft = "", mMostRecentInjuryRight = "";
	
	// argument key-strings for populating the savedInstance bundle
	private static final String SAVED_LEFT_INJURY_DATE = "saved_left_injury_date";
	private static final String SAVED_RIGHT_INJURY_DATE = "saved_right_injury_date";
	private static final String SAVED_DISPLAY_LEFT_STRING = "saved_display_left_string";
	private static final String SAVED_DISPLAY_RIGHT_STRING = "saved_display_right_string";
	
	// user parameters
	private String mProfileMode = null;
	private int mUserId = -1;
	
	// layout objects
	private TextView mLabelTitle, mLabelDesc, mLabelDateInjuryLeft, mLabelDateInjuryRight;
	private CheckBox mCheckboxLeft, mCheckboxRight;
	private Button mButtonDiscardLeft, mButtonDiscardRight, mButtonSubmit;
	
	private DatabaseHelper mDatabaseHelper = null;
	
	// the original date-string format
	SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	
	// instantiate FragmentProfileInjuries
	public static FragmentProfileInjuries newInstance(String profileMode) {
		
		FragmentProfileInjuries myFragment = new FragmentProfileInjuries();
		
		Bundle args = new Bundle();
		args.putString(ARG_PROFILE_MODE, profileMode);
		myFragment.setArguments(args);
		
		return myFragment;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// save the date-string and display labels to the bundle
		outState.putString(SAVED_LEFT_INJURY_DATE, mDateInjuryLeft);
		outState.putString(SAVED_RIGHT_INJURY_DATE, mDateInjuryRight);
		
		outState.putString(SAVED_DISPLAY_LEFT_STRING, mLabelDateInjuryLeft.getText().toString());
		outState.putString(SAVED_DISPLAY_RIGHT_STRING, mLabelDateInjuryRight.getText().toString());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create a new DatabaseHelper object
		mDatabaseHelper = new DatabaseHelper(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// inflate the layout file
		View view = (View) inflater.inflate(R.layout.fragment_profile_injuries, 
				container, false);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// get the userId from SharedPreferences
		mUserId = PrefUtils.getIntPreference(getActivity(), PrefUtils.LOCAL_USER_ID);
						
		// get the profile mode from the arguments
		mProfileMode = getArguments().getString(ARG_PROFILE_MODE);
		
		// link the layout objects to the corresponding UI elements
		mLabelTitle = (TextView) getView().findViewById(R.id.fragment_profile_injuries_label_title);
		mLabelDesc = (TextView) getView().findViewById(R.id.fragment_profile_injuries_label_desc);
		mLabelDateInjuryLeft = (TextView) getView().findViewById(R.id.fragment_profile_injuries_label_left_date);
		mLabelDateInjuryRight = (TextView) getView().findViewById(R.id.fragment_profile_injuries_label_right_date);
		
		mCheckboxLeft = (CheckBox) getView().findViewById(R.id.fragment_profile_injuries_checkbox_left);
		mCheckboxRight = (CheckBox) getView().findViewById(R.id.fragment_profile_injuries_checkbox_right);
		
		mButtonDiscardLeft = (Button) getView().findViewById(R.id.fragment_profile_injuries_button_discard_left);
		mButtonDiscardRight = (Button) getView().findViewById(R.id.fragment_profile_injuries_button_discard_right);
		mButtonSubmit = (Button) getView().findViewById(R.id.fragment_profile_injuries_button_submit);
		
		// register the button listeners
		mButtonDiscardLeft.setOnClickListener(this);
		mButtonDiscardRight.setOnClickListener(this);
		mButtonSubmit.setOnClickListener(this);
		
		// register the check-box listeners
		mCheckboxLeft.setOnCheckedChangeListener(this);
		mCheckboxRight.setOnCheckedChangeListener(this);

		// activity is launched in 'Create' mode
		if(mProfileMode.equals(ActivityProfile.CREATE_MODE)) {
			
			// explicitly set the title label and description
			mLabelTitle.setText(getActivity().getString(R.string.profile_injuries_title_create));
			mLabelDesc.setText(getActivity().getString(R.string.profile_injuries_body_create));
		
		// activity is launched in 'Update' mode
		} else if(mProfileMode.equals(ActivityProfile.UPDATE_MODE)) {
			
			// explicitly set the title label and description
			mLabelTitle.setText(getActivity().getString(R.string.profile_injuries_title_update));
			mLabelDesc.setText(getActivity().getString(R.string.profile_injuries_body_update));
			
			// get the most recent injury dates from the database
			mMostRecentInjuryLeft = getMostRecentInjuryDate("Left");
			mMostRecentInjuryRight = getMostRecentInjuryDate("Right");
			
			// set the default ankle-injury labels
			mLabelDateInjuryLeft.setText(mMostRecentInjuryLeft);
			mLabelDateInjuryRight.setText(mMostRecentInjuryRight);
		}
		
		// if the fragment is being re-created from a saved state
		if(savedInstanceState != null) {
								
			// retrieve the date-strings from the savedInstance bundle
			mDateInjuryLeft = savedInstanceState.getString(SAVED_LEFT_INJURY_DATE, "");
			mDateInjuryRight = savedInstanceState.getString(SAVED_RIGHT_INJURY_DATE, "");
			
			// retrieve the display labels
			String displayStringLeft = savedInstanceState.getString(SAVED_DISPLAY_LEFT_STRING, "");
			String displayStringRight = savedInstanceState.getString(SAVED_DISPLAY_RIGHT_STRING, "");
			
			// if the left injury date-string was not empty
			if(!mDateInjuryLeft.isEmpty()) {
				
				// make the 'Discard Left' button visible
				mButtonDiscardLeft.setVisibility(Button.VISIBLE);
			}
			
			// if the right injury date-string was not empty
			if(!mDateInjuryRight.isEmpty()) {
				
				// make the 'Discard Right' button visible
				mButtonDiscardRight.setVisibility(Button.VISIBLE);
			}
			
			// restore the corresponding text labels
			mLabelDateInjuryLeft.setText(displayStringLeft);
			mLabelDateInjuryRight.setText(displayStringRight);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mDatabaseHelper.close();
	}
	
	@Override
	public void onClick(View view) {
		
		switch(view.getId()) {
		
		// when the 'Discard Left' button is pressed
		case R.id.fragment_profile_injuries_button_discard_left: 
			
			// un-check the 'left' check-box
			mCheckboxLeft.setChecked(false);
			
			break;
			
		// when the 'Discard Right' button is pressed
		case R.id.fragment_profile_injuries_button_discard_right:
			
			// un-check the 'right' check-box
			mCheckboxRight.setChecked(false);
			
			break;
			
		// when the finish button is pressed
		case R.id.fragment_profile_injuries_button_submit:
			
			int personalFragmentPosition = 0;
			
			// in 'update' mode, offset the position (from 0) by the
			// difference in number of fragments displayed in the two modes
			if(mProfileMode.equals(ActivityProfile.UPDATE_MODE)) {
				personalFragmentPosition = ActivityProfile.PERSONAL_STARTS_AT;
			}
			
			FragmentProfilePersonal personalFragment = (FragmentProfilePersonal) 
				ActivityProfile.mSectionsPagerAdapter.getFragment(personalFragmentPosition);
			
			// debug messages
			if(BuildConfig.DEBUG) 
				Log.d(TAG, "Left date : " + mDateInjuryLeft + 
						" and Right date : " + mDateInjuryRight);
			
			// check if the person's details on the previous page are valid
			if(personalFragment.hasFilledOut()) {
				
				// if the activity is launched in 'Create' mode
				if(mProfileMode.equals(ActivityProfile.CREATE_MODE)) {
					
					// if the entered name is available
					if(personalFragment.isAvailableName()) {
						
						// in debug mode, skip the tutorial
						if(BuildConfig.DEBUG) {
							
							// simulate successful completion of the tutorial
							onActivityResult(TUTORIAL_REQUEST_CODE, Activity.RESULT_OK, null);
							
						} else {
							
							// launch ActivityTutorial and listen for a result
							Intent tutorialIntent = new Intent(getActivity(), ActivityTutorial.class);
							startActivityForResult(tutorialIntent, TUTORIAL_REQUEST_CODE);
						}
					
					// else if user name is taken
					} else {
						
						// show an appropriate error
						Toast.makeText(getActivity(), "A user with that name already exists!", 
								Toast.LENGTH_SHORT).show();
						
						// return to the 'Personal' fragment
						ActivityProfile.mViewPager.setCurrentItem(personalFragmentPosition);
					}
					
				// if the activity is launched in 'Update' mode
				} else {
					
					// if the name is either unchanged or is available, update the user's data
					if(personalFragment.isNameUnchanged() || personalFragment.isAvailableName()) {
				
						// update the user in the database
						personalFragment.createOrUpdateUser();
				
						// create a new entry in the 'injuries' table for the new injury
						saveInjuryData();
					
						// create a toast indicating success and finish the current activity
						Toast.makeText(getActivity(), "Profile updated successfully", 
							Toast.LENGTH_SHORT).show();
					
						getActivity().finish();
						
					// user name is changed, and is not available
					} else {
						
						// show an appropriate error
						Toast.makeText(getActivity(), "A user with that name already exists!", 
								Toast.LENGTH_SHORT).show();
						
						// return to the 'Personal' fragment
						ActivityProfile.mViewPager.setCurrentItem(personalFragmentPosition);
					}
				}
				
			} else {
				
				// if the details are invalid, return to the previous page
				ActivityProfile.mViewPager.setCurrentItem(personalFragmentPosition);
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		// retrieve the position of this fragment, and that of the fragment
		// currently being displayed in the ViewPager
		int selectedItemPosition = ActivityProfile.mViewPager.getCurrentItem();
		int thisItemPosition = ActivityProfile.NUM_PERSONAL;
		
		if(mProfileMode.equals(ActivityProfile.UPDATE_MODE)) {
			thisItemPosition = ActivityProfile.INJURIES_STARTS_AT;
		}
		
		// if the check-box was recently checked and this is the
		// currently displayed fragment in the activity's viewPager
		if(isChecked && (thisItemPosition == selectedItemPosition)) {
			
			// get the current date
			Calendar calendar = Calendar.getInstance();
			int thisYear = calendar.get(Calendar.YEAR);
			int thisMonth = calendar.get(Calendar.MONTH);
			int thisDay = calendar.get(Calendar.DAY_OF_MONTH);
			
			// create a new DialogPicker object and register the associated listener
			DatePickerDialog datePickerDialog = DatePickerDialog.newInstance
					(new OnDateSetListener() {
				
				@Override
				public void onDateSet(DatePickerDialog datePickerDialog, int year,
						int month, int day) {
					
					// create and configure a Calendar object with the selected date
					Calendar selectedInjuryCalendar = Calendar.getInstance();
					selectedInjuryCalendar.clear();
					selectedInjuryCalendar.set(year, month, day);
					
					String selectedAnkleSide = null;
					String selectedInjuryDate = "";
					
					// if the left-ankle date-selection was displayed
					if(getFragmentManager().findFragmentByTag("datepicker-left") != null) {

						// generate the selected injury date-string
						mDateInjuryLeft = DateFormat.format("yyyy-MM-dd", 
								selectedInjuryCalendar).toString();
						
						// set the selected ankle-side and injury dates
						selectedAnkleSide = "Left";
						selectedInjuryDate = mDateInjuryLeft;
						
						// update the corresponding (left) text-label
						mLabelDateInjuryLeft.setText(DateFormat.format("dd - MM - yyyy",
								selectedInjuryCalendar).toString());
						
						// make the 'Discard Left' button visible
						mButtonDiscardLeft.setVisibility(Button.VISIBLE);
						
					} else if(getFragmentManager().findFragmentByTag("datepicker-right") != null) {
						
						// generate the selected injury date-string
						mDateInjuryRight = DateFormat.format("yyyy-MM-dd", 
								selectedInjuryCalendar).toString();
						
						// set the selected ankle-side and injury dates
						selectedAnkleSide = "Right";
						selectedInjuryDate = mDateInjuryRight;
						
						// update the corresponding (right) text-label
						mLabelDateInjuryRight.setText(DateFormat.format("dd - MM - yyyy",
								selectedInjuryCalendar).toString());
						
						// make the 'Discard Right' button visible
						mButtonDiscardRight.setVisibility(Button.VISIBLE);
					}
					
					// if the entered injury date is not within the last 180 days
					if(!inDayRange(year, month, day, 180)) {
						
						// un-check the corresponding check-box
						if(selectedAnkleSide.equals("Left")) {
							mCheckboxLeft.setChecked(false);
							
						} else {
							mCheckboxRight.setChecked(false);
						}
						
						// create a toast indicating why the check-box was unchecked
						Toast.makeText(getActivity(), "That date was not in the past 6 months", 
								Toast.LENGTH_SHORT).show();
					
					// if the entered injury date is not the most recent one
					} else if(!isMostRecentInjury(selectedAnkleSide, selectedInjuryDate)) {
						
						// un-check the corresponding check-box
						if(selectedAnkleSide.equals("Left")) {
							mCheckboxLeft.setChecked(false);
							
						} else {
							mCheckboxRight.setChecked(false);
						}
						
						// create a toast indicating why the check-box was unchecked
						Toast.makeText(getActivity(), "Entered injury was not the most recent one", 
								Toast.LENGTH_SHORT).show();
						
					// the recorded injury was in the last 30 days
					} else if(inDayRange(year, month, day, 30)) {
						
						// create an injury warning alert
						createInjuryWarningAlert();
					}
				}
			}, thisYear, thisMonth, thisDay, true);
			
			// set the dialog properties
			datePickerDialog.setVibrate(true);
			datePickerDialog.setCancelable(false);
			datePickerDialog.setYearRange(thisYear - 1, thisYear);
			
			// display the 'left' date-picker dialog
			if(buttonView.getId() == R.id.fragment_profile_injuries_checkbox_left){
				
				datePickerDialog.show(getFragmentManager(), "datepicker-left");			
			}				
			
			// display the 'right' date-picker dialog
			if(buttonView.getId() == R.id.fragment_profile_injuries_checkbox_right){				
				
				datePickerDialog.show(getFragmentManager(), "datepicker-right");		
			}
			
		// if the check-box was recently un-checked, reset the corresponding
		// date-string and associated TextView
		} else if(!isChecked) {
			
			if(buttonView.getId() == R.id.fragment_profile_injuries_checkbox_left) {
				
				// reset the date string and TextView
				mDateInjuryLeft = "";
				mLabelDateInjuryLeft.setText(mMostRecentInjuryLeft);
				mButtonDiscardLeft.setVisibility(Button.INVISIBLE);
				
			} else if(buttonView.getId() == R.id.fragment_profile_injuries_checkbox_right) {
				
				// reset the date string and TextView
				mDateInjuryRight = "";
				mLabelDateInjuryRight.setText(mMostRecentInjuryRight);
				mButtonDiscardRight.setVisibility(Button.INVISIBLE);
			}
		}
	}
	
	// check if the parameterized date is within the valid period 
	private boolean inDayRange(int year, int month, int day, int daysValid) {
		
		// get the current date calendar
		Calendar calCurDate = Calendar.getInstance();		
		
		// make the injury date calendar
		Calendar calInjDate = Calendar.getInstance();
		calInjDate.set(year, month, day);
	
		// get the number of days that have passed since the injury
		int days = Math.round(((calCurDate.getTime().getTime() - calInjDate.getTime().getTime()) / (1000 *60 *60 * 24)));		

		// injury date valid if in the past and less than 6 months
		return ( (days >=0) && (days <= daysValid) );		
	}
	
	// create a new entry in the 'injuries' table corresponding to the current user
	private void saveInjuryData() {
		
		ContentValues args = new ContentValues();
		
		// left ankle
		if(mCheckboxLeft.isChecked() && !mDateInjuryLeft.isEmpty()) {
			
			// clear any existing arguments
			args.clear();
			
			// bundle and insert the entered fields into the database
			args.put("userId", mUserId);
			args.put("ankleSide", "Left");
			args.put("injuryDate", mDateInjuryLeft);
			args.put("severity", 0);
			
			mDatabaseHelper.getWritableDatabase().insert("injuries", null, args);
		}
		
		// right ankle
		if(mCheckboxRight.isChecked() && !mDateInjuryRight.isEmpty()) {
			
			// clear any existing arguments
			args.clear();
			
			// bundle and insert the entered fields into the database
			args.put("userId", mUserId);
			args.put("ankleSide", "Right");
			args.put("injuryDate", mDateInjuryRight);
			args.put("severity", 0);
			
			mDatabaseHelper.getWritableDatabase().insert("injuries", null, args);
		}
	}
	
	// checks if the injury date entered by the user is the most recent one
	// if so, returns true. else, returns false
	private boolean isMostRecentInjury(String enteredAnkleSide, String enteredInjuryDateString) {
		
		boolean isMostRecent = true;
		String lastInjuryDateString = "";
		
		// get the most recent injury date from the database. note: this expects
		// that the dates to be entered in the database in chronological order
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT injuryDate FROM injuries WHERE userId = " + mUserId +
				" AND ankleSide = " + "'" + enteredAnkleSide + "'" + " ORDER BY " +
				" _id DESC", null);
			
		// if the cursor has at least one row entry
		if(cur != null && cur.moveToFirst()) {
			lastInjuryDateString = cur.getString(0);
		}
		
		// close the cursor
		cur.close();
		
		// if the latest injury date-string is not empty
		if(!lastInjuryDateString.isEmpty()) {
			
			try {
				
				/* parse the ankle-injury date-strings as date objects */
				Date enteredInjuryDate = originalFormat.parse(enteredInjuryDateString);
				Date lastInjuryDate = originalFormat.parse(lastInjuryDateString);
				
				// if the entered date is precedes or equals the last injury date
				if(!enteredInjuryDate.after(lastInjuryDate)) {
					isMostRecent = false;
				}
				
			} catch (ParseException e) {
			
				e.printStackTrace();
				isMostRecent = false;
			}
		}
		
		return isMostRecent;
	}
	
	private String getMostRecentInjuryDate(String ankleSide) {
		
		String displayRecentDateString = "";
		
		// fetch the most recent injury date for that particular 
		// ankle from the database
		Cursor cursor = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT injuryDate FROM injuries WHERE userId = ? AND ankleSide = ? " +
				" ORDER BY _id DESC", 
				new String[] {String.valueOf(mUserId), ankleSide});
		
		// if atleast one recorded injury exists
		if(cursor != null && cursor.moveToFirst()) {
			
			try {
				
				// parse the original date string using the specified format
				Date date = originalFormat.parse(cursor.getString(0));
				
				// generate the date-string to be displayed
				displayRecentDateString = "Last Injury\n" + DateFormat.format("dd - MM - yyyy",
						date).toString();
			
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		// close the cursor
		cursor.close();
		
		// return the formatted date string
		return displayRecentDateString;
	}
	
	// called when the child activity finishes with a response to the parent's call
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		int personalFragmentPosition = 0;
		
		// in 'update' mode, offset the position (from 0) by the
		// difference in number of fragments displayed in the two modes
		if(mProfileMode.equals(ActivityProfile.UPDATE_MODE)) {
			personalFragmentPosition = ActivityProfile.PERSONAL_STARTS_AT;
		}
		
		FragmentProfilePersonal personalFragment = (FragmentProfilePersonal) 
				ActivityProfile.mSectionsPagerAdapter.getFragment(personalFragmentPosition);
		
		if(requestCode == TUTORIAL_REQUEST_CODE) {
			
			// tutorial was completed successfully
			if(resultCode == Activity.RESULT_OK) {
				
				// create a new user in the database
				personalFragment.createOrUpdateUser();
			
				// get the created user's local ID from the database
				Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
						"SELECT _id FROM users ORDER BY _id DESC", null);
				
				if(cur != null && cur.moveToFirst()) {
					
					mUserId = cur.getInt(0);
					cur.close();
					
					// create a new entry in the 'injuries' table for the new injury
					saveInjuryData();
				
				// should not reach here
				} else {
					if(BuildConfig.DEBUG) Log.d(TAG, "User creation unsuccessful!");
				}
				
				// create a toast indicating success and finish the current activity
				Toast.makeText(getActivity(), "User created successfully!", 
						Toast.LENGTH_SHORT).show();
				
				getActivity().finish();
			}
		}
	}

	// creates an alert dialog warning the user that the indicated injury was within 30 days
	private void createInjuryWarningAlert(){
		
		// Build a one button dialog and show it
	    AlertDialog.Builder dlgAlert= new AlertDialog.Builder(getActivity())
	    	.setTitle(getResources().getString(R.string.profile_questions_injury_alert_title))
	    	.setMessage(getResources().getString(R.string.profile_questions_injury_alert_messagee))
	    	.setCancelable(false)
	    	.setPositiveButton(getResources().getString(R.string.profile_questions_injury_alert_positive_button), new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int whichButton) {

	    		}
	    	});
	    
	    // create a new instance of DialogHelper, set the parameters and display the dialog
	    DialogStyleHelper box = new DialogStyleHelper(getActivity(), dlgAlert.create());
	    box.setDialogButtonParams(null, -1, getActivity().getResources().getColor(R.color.blue));
	    box.showDialog();	
	}
}
