package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentLogin extends Fragment implements OnClickListener {
	
	public static final String TAG = FragmentLogin.class.getSimpleName();
	
	private ArrayList<String> mUsers = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter;
	
	private DatabaseHelper mDatabaseHelper = null;
	private int mUserId = -1;
	
	private Button mButtonLoginUser;
	private Button mButtonCreateUser;
	private Spinner mSpinner;
	
	// instantiate FragmentLogin
	public static FragmentLogin newInstance() {
		
		FragmentLogin myFragment = new FragmentLogin();
		
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

		// inflate the layout file
		View view = (View) inflater.inflate(R.layout.fragment_login, container, false);
		
		// link the layout objects to the corresponding UI elements
		mButtonLoginUser = (Button) view.findViewById(R.id.fragment_login_button_login);
		mButtonCreateUser = (Button) view.findViewById(R.id.fragment_login_button_create_user);
		mSpinner = (Spinner) view.findViewById(R.id.fragment_login_spinner_username);
		
		// create and link the adapter to the spinner object
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mUsers);
		mSpinner.setAdapter(mAdapter);
		
		// disable the login button by default
		mButtonLoginUser.setEnabled(false);
		
		// register listeners
		mButtonLoginUser.setOnClickListener(this);
		mButtonCreateUser.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
				
		// clear any existing user entries
		mUsers.clear();
				
		// query the database for all users' names
		Cursor cursor = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT name FROM users", null);
		
		if(cursor != null && cursor.moveToFirst()) {
			
			// iterate through all returned entries
			while(!cursor.isAfterLast()) {
				
				// add the user-name to the list of users 
				String username = cursor.getString(0);
				mUsers.add(username);
				
				cursor.moveToNext();
			}
					
			cursor.close();
					
			// enable the login button
			mButtonLoginUser.setEnabled(true);
			
		// no existing users in the database
		} else {
			
			// create a toast indicating there are no existing users
			Toast.makeText(getActivity(), "No users yet. You should create one!", 
				Toast.LENGTH_SHORT).show();
		}
		
		// notify the adapter that the underlying view should be redrawn
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		
		// when the 'Login' button is pressed
		case R.id.fragment_login_button_login: 
			
			String username = mSpinner.getSelectedItem().toString();
			setUserId(username);
			
			// if the user id is valid (greater than -1), store it in the shared preferences
			if(mUserId > -1) {
				
				PrefUtils.setIntPreference(getActivity(), PrefUtils.LOCAL_USER_ID, mUserId);
				
				// launch a new instance of ActivityMain
				Intent mainIntent = new Intent(getActivity(), ActivityMain.class);
				startActivity(mainIntent);
				
				getActivity().finish();
				
			// the user id is invalid
			} else {
				
				// log a debug message
				if(BuildConfig.DEBUG) Log.d(TAG, "Local user ID is invalid. UID = " 
						+ mUserId);
			}
			
			break;
		
		// when the 'Create User' button is pressed
		case R.id.fragment_login_button_create_user: 
			
			// launch a new instance of ActivityProfile in 'Create' mode
			Intent createUserIntent = new Intent(getActivity(), ActivityProfile.class);
			createUserIntent.putExtra(ActivityProfile.ARG_PROFILE_MODE, 
					ActivityProfile.CREATE_MODE);
			
			startActivity(createUserIntent);
			
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// release the database helper object
		mDatabaseHelper.close();
	}
	
	// returns the user id of the user, given their user-name
	private void setUserId(String username) {
		
		// redundant checking
		if(mDatabaseHelper != null) {
			
			// find the person identified by the parameterized user-name
			Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
					"SELECT _id FROM users WHERE name = " + "'" + username + "';", null);
			cur.moveToFirst();
			
			// store the corresponding user id (_id)
			this.mUserId = cur.getInt(0);
			cur.close();
		}
	}

}
