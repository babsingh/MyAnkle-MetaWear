package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class FragmentProfilePersonal extends Fragment implements OnClickListener {

	public static final String TAG = FragmentProfilePersonal.class.getSimpleName();
	public static final String ARG_PROFILE_MODE = "profile_mode";
	
	private String mProfileMode = null;
	
	private int mUserId = -1;
	private String mLoggedInUsername = "";
	
	private TextView mLabelTitle, mTextName, mTextAge;
	private RadioButton mRadioButtonMale, mRadioButtonFemale;
	private Button mButtonDiscard, mButtonNext;
	
	private DatabaseHelper mDatabaseHelper = null;
	
	// instantiate FragmentProfilePersonal
	public static FragmentProfilePersonal newInstance(String profileMode) {
		
		FragmentProfilePersonal myFragment = new FragmentProfilePersonal();
		
		Bundle args = new Bundle();
		args.putString(ARG_PROFILE_MODE, profileMode);
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
		
		// inflate the layout file
		View view = (View) inflater.inflate(R.layout.fragment_profile_personal, 
				container, false);
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// get the userId from sharedPreferences
		mUserId = PrefUtils.getIntPreference(getActivity(), PrefUtils.LOCAL_USER_ID);
		
		// retrieve the profile mode from the arguments
		mProfileMode = getArguments().getString(ARG_PROFILE_MODE); 
		
		// link the layout objects to the corresponding UI elements
		mLabelTitle = (TextView) getView().findViewById(R.id.fragment_profile_personal_label_title);
		mTextName = (TextView) getView().findViewById(R.id.fragment_profile_personal_text_name);
		mTextAge = (TextView) getView().findViewById(R.id.fragment_profile_personal_text_age);
		mRadioButtonMale = (RadioButton) getView().findViewById(R.id.fragment_profile_personal_rb_male);
		mRadioButtonFemale = (RadioButton) getView().findViewById(R.id.fragment_profile_personal_rb_female);
		
		mButtonDiscard = (Button) getView().findViewById(R.id.fragment_profile_personal_button_discard);
		mButtonNext = (Button) getView().findViewById(R.id.fragment_profile_personal_button_next);
		
		// register the button-click listeners
		mButtonDiscard.setOnClickListener(this);
		mButtonNext.setOnClickListener(this);
		
		// if the activity is launched in 'Update' mode
		if(mProfileMode.equals(ActivityProfile.UPDATE_MODE)) {
			
			// set the title-label text
			mLabelTitle.setText("Update Profile");
			
			// explicitly display the discard button
			mButtonDiscard.setVisibility(Button.VISIBLE);
			
			// fetch the user's data from the database
			Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
					"SELECT name, age, gender FROM users WHERE _id = " + mUserId, null);
			cur.moveToFirst();
			
			// default the text fields and radio buttons to the 
			// person's data in the database
			mLoggedInUsername = cur.getString(0);
			
			mTextName.setText(mLoggedInUsername);
			mTextAge.setText(String.valueOf(cur.getInt(1)));
			
			if((cur.getString(2)).equals("Male")) {
				mRadioButtonMale.setChecked(true);
			} else {
				mRadioButtonFemale.setChecked(true);
			}
			
			cur.close();
			
		} else {
			
			// set the title-label text
			mLabelTitle.setText("Create New User");
			
			// hide the discard button
			mButtonDiscard.setVisibility(Button.INVISIBLE);
		}
		
		// debug message
		if(BuildConfig.DEBUG) Log.d(TAG, "Local user ID = " + mUserId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mDatabaseHelper.close();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		
		// when the 'Discard' button is pressed
		case R.id.fragment_profile_personal_button_discard: 
			
			// finish the current activity
			getActivity().finish();
			break;
		
		// when the 'Next' button is pressed
		case R.id.fragment_profile_personal_button_next: 
			
			int thisItemPosition = ActivityProfile.mViewPager.getCurrentItem();
			
			// display the 'Injuries' fragment
			ActivityProfile.mViewPager.setCurrentItem(thisItemPosition + 1);
			
			break;
		}
	}
	
	// checks whether all required fields (name, age, gender) are entered by the user
	public boolean hasFilledOut() {
		
		// get the name and age entered by the user
		String nameText = mTextName.getText().toString();
		String ageText = mTextAge.getText().toString();
		
		boolean nameEntered = !nameText.matches("");
		boolean ageEntered = !ageText.matches("");
		boolean genderEntered = (mRadioButtonMale.isChecked() || mRadioButtonFemale.isChecked());
		
		// if all fields are valid (not empty), return true
		return (nameEntered && ageEntered && genderEntered);
	}
	
	// check if the parameterized name is available (no existing users with 
	// the same name). if so, return true. else, returns false
	public boolean isAvailableName() {
			
		String name = mTextName.getText().toString();
		boolean isAvailableName = true;
		
		// query the database for users with the same name as that entered by the user
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery( 
				"SELECT _id FROM users WHERE name = " + "'" + name + "'", null);
		
		if(cur != null && cur.moveToFirst()) {
			
			// a user by the same name exists
			isAvailableName = false;
		}
		
		cur.close();
		
		return isAvailableName;
	}
	
	// check if the entered name is the unchanged since loading the profile activity
	// if so, return true. else, return false
	public boolean isNameUnchanged() {
		
		String name = mTextName.getText().toString();
		
		return (name != null && mLoggedInUsername.equals(name));
	}
	
	// inserts a new user or updates an existing user in the database, depending on the
	// mode in which ActivityProfile is launched
	public void createOrUpdateUser() {
		
		// redundant checking
		if(hasFilledOut()) {
			
			// get the entered name, age and gender
			String name = mTextName.getText().toString();
			String age = mTextAge.getText().toString();
			
			String gender = null;
			if(mRadioButtonMale.isChecked()) {
				gender = "Male";
			} else {
				gender = "Female";
			}
			
			// create a new bundle to insert into the database
			ContentValues args = new ContentValues();
			
			// set the user's basic parameters
			args.put("name", name);
			args.put("age", age);
			args.put("gender", gender);
			
			// the activity is launched in 'Update' mode, update the user's fields
			if(mProfileMode.equals(ActivityProfile.UPDATE_MODE)) {
				
				// update the user
				mDatabaseHelper.getWritableDatabase()
					.update("users", args, "_id = " + mUserId, null);
				
			// the activity is launched in 'Create' mode, create a new user
			} else if(mProfileMode.equals(ActivityProfile.CREATE_MODE)) {
				
				// set additional parameters
				args.put("serverId", -1);
				args.put("consent", -1);
				args.put("level", 0);
				
				// create a row corresponding to the new user
				mDatabaseHelper.getWritableDatabase().insert("users", null, args);
				
			// should not reach here
			} else {
				if(BuildConfig.DEBUG) Log.d(TAG, "Unexpected profile mode");
			}
			
		}
	}
}
