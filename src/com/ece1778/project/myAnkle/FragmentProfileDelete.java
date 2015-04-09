package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.R;
import com.ece1778.project.myAnkleUser.R.color;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class FragmentProfileDelete extends Fragment implements OnClickListener, OnCheckedChangeListener {

	public static final String TAG = FragmentProfileDelete.class
			.getSimpleName();

	// user's credentials
	private int mUserId = -1;
	
	// layout objects
	CheckBox mCheckboxConfirm;
	Button mButtonSubmit;

	private DatabaseHelper mDatabaseHelper = null;

	// instantiate FragmentProfileDelete
	public static FragmentProfileDelete newInstance() {

		FragmentProfileDelete myFragment = new FragmentProfileDelete();

		Bundle args = new Bundle();
		myFragment.setArguments(args);

		return myFragment;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// explicitly un-check the check-box
		mCheckboxConfirm.setChecked(false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDatabaseHelper = new DatabaseHelper(getActivity());
		
		// get the user id from preferences
		mUserId = PrefUtils.getIntPreference(getActivity(), PrefUtils.LOCAL_USER_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_profile_delete,
				container, false);
		
		// link the layout objects to the corresponding UI elements
		mCheckboxConfirm = (CheckBox) view.findViewById(R.id.fragment_profile_delete_checkbox_confirm);
		mButtonSubmit = (Button) view.findViewById(R.id.fragment_profile_delete_buttonbar);
		
		// initially, grey-out the button and disable it
		mButtonSubmit.setBackgroundColor(getResources().getColor(color.light_gray));
		mButtonSubmit.setEnabled(false);
		
		// register the listeners
		mCheckboxConfirm.setOnCheckedChangeListener(this);
		mButtonSubmit.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mDatabaseHelper.close();
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		
		// when the 'Delete User' button is clicked
		case R.id.fragment_profile_delete_buttonbar: 
			
			// delete the user's injury data
			mDatabaseHelper.getWritableDatabase().delete(
					"injuries", "userId = " + mUserId, null);
			
			// delete the user's exercise (session) data
			mDatabaseHelper.getWritableDatabase().delete(
					"sessions", "userId = " + mUserId, null);
			
			// delete the user from the database
			mDatabaseHelper.getWritableDatabase().delete(
					"users", "_id = " + mUserId, null);
			
			// display a toast indicating the user was deleted successfully
			Toast.makeText(getActivity(), "User deleted successfully", 
					Toast.LENGTH_SHORT).show();
			
			// the user profile was deleted. return an appropriate message to 
			// the caller and finish the current activity
			getActivity().setResult(ActivityMain.RESULT_PROFILE_DELETED);
			getActivity().finish();
			
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		switch(buttonView.getId()) {
		
		// when the 'confirm' check-box is clicked
		case R.id.fragment_profile_delete_checkbox_confirm: 
			
			// if the check-box was recently checked
			if(isChecked) {
				
				// set the background color to red and enable the button
				mButtonSubmit.setBackgroundColor(getResources().getColor(R.color.red));
				mButtonSubmit.setEnabled(true);
				
			// if the check-box was recently unchecked
			} else {
				
				// grey-out and disable the button
				mButtonSubmit.setBackgroundColor(getResources().getColor(color.light_gray));
				mButtonSubmit.setEnabled(false);
			}
			
			break;
		}
	}
}
