package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.OnSwipeListener;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FragmentProgressListView extends Fragment {
	
	public static final String TAG = FragmentProgressListView.class.getSimpleName();
	private static final String ARG_EXERCISE_ID = "exercise_id";
	
	// user and exercise parameters
	private int mUserId = -1;
	private int mExerciseId = -1;
	
	// layout objects
	private TextView mTextAnkleSide;
	private Button mButtonSwapFragments;
	private ListView mListViewResults;
	private ListAdapterResults mAdapter;
	
	// array-list to store result data
	private ArrayList<String[]> mResultsList = new ArrayList<String[]>();
	
	// menu-item objects
	private MenuItem mMenuButtonLeftAnkle = null; 
	private MenuItem mMenuButtonRightAnkle = null;
	
	// argument strings
	private static final String LEFT_ANKLE_RESULTS = "Left";
	private static final String RIGHT_ANKLE_RESULTS = "Right";
	
	private Toast mToast = null;
	private DatabaseHelper mDatabaseHelper;
	private SimpleDateFormat originalDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	
	// instantiate FragmentProgressListView
	public static FragmentProgressListView newInstance(int exerciseId) {
		
		FragmentProgressListView myFragment = new FragmentProgressListView();
		
		Bundle args = new Bundle();
		args.putInt(ARG_EXERCISE_ID, exerciseId);
		myFragment.setArguments(args);
		
		return myFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/* indicate that the fragment should receive any menu-related
		 * call-backs that are not explicitly consumed in the activity */
		setHasOptionsMenu(true);
		
		mDatabaseHelper = new DatabaseHelper(getActivity());
		
		// get the userId from preferences
		mUserId = PrefUtils.getIntPreference(getActivity(), PrefUtils.LOCAL_USER_ID);
						
		// retrieve the exerciseId from the arguments
		mExerciseId = getArguments().getInt(ARG_EXERCISE_ID, -1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// inflate the fragment's layout file
		View view = inflater.inflate(R.layout.fragment_progress_list, container, false);
		
		// link the layout objects to the corresponding UI elements
		mTextAnkleSide = (TextView) view.findViewById(R.id.fragment_progress_list_text_ankle);
		mButtonSwapFragments = (Button) view.findViewById(R.id.fragment_progress_list_buttonbar);
		mListViewResults = (ListView) view.findViewById(R.id.fragment_progress_list_view);
		
		// initialize the adapter and link it to the list-view
		mAdapter = new ListAdapterResults(getActivity(), R.layout.layout_results_list_row, mResultsList);
		mListViewResults.setAdapter(mAdapter);
		
		// register the touch-listener for the button-bar
		mButtonSwapFragments.setOnTouchListener(new OnSwipeListener(getActivity()) {

			// implement the activity's default back-pressed behavior
			@Override
			public void onSwipeDown() {
				getActivity().onBackPressed();
			}
		});
		
		// update the list with the default (Left) ankle-side
		updateResultsList(LEFT_ANKLE_RESULTS);
		
		return view;
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		
		// link the menu objects to the corresponding UI elements
		mMenuButtonLeftAnkle = menu.findItem(R.id.progress_menu_button_left_ankle);
		mMenuButtonRightAnkle = menu.findItem(R.id.progress_menu_button_right_ankle);
		
		// make both the ankle-side buttons visible (not visible by default)
		mMenuButtonLeftAnkle.setVisible(true);
		mMenuButtonRightAnkle.setVisible(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		
		case R.id.progress_menu_button_left_ankle: 
			
			// render the results list for the 'Left' ankle
			updateResultsList(LEFT_ANKLE_RESULTS);
			
			break;
		
		case R.id.progress_menu_button_right_ankle:
			
			// render the results list for the 'Right' ankle
			updateResultsList(RIGHT_ANKLE_RESULTS);
			
			break;
		}
		
		// return false to let the callback propagate to the next fragment
		return false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		mDatabaseHelper.close();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		// set the menu-buttons visibility to invisible
		if(mMenuButtonLeftAnkle != null) {
			mMenuButtonLeftAnkle.setVisible(false);
		}
		
		if(mMenuButtonRightAnkle != null) {
			mMenuButtonRightAnkle.setVisible(false);
		}
	}
	
	/* populates the array-list storing the results with the session values
	 * and injury data for the parameterized ankle-side, for that particular
	 * exercise. the results are displayed in reverse chronological order. 
	 * also calls notifyDataSetChanged() to update the list-view */
	private void updateResultsList(String ankleSide) {
		
		// cancel any pending toast messages
		if(mToast != null) mToast.cancel();
		
		// update the TextView with the ankle-side string
		mTextAnkleSide.setText(ankleSide + " Ankle");
		
		// clear any existing values in the results array-list
		mResultsList.clear();
		
		// fetch the user's sessions for that particular exercise and ankle-side
		Cursor sessionsCur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT date, meanR FROM sessions WHERE userId = ? AND" +
						" exerciseId = ? AND ankleSide = ? ORDER BY _id DESC", 
				new String[] {String.valueOf(mUserId), String.valueOf(mExerciseId),
						ankleSide});
		
		// fetch the user's injuries for that ankle-side
		Cursor injuriesCur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT injuryDate FROM injuries WHERE userId = ? AND" +
						" ankleSide = ? ORDER BY _id DESC", 
				new String[] {String.valueOf(mUserId), ankleSide});
		
		boolean sessionsCurNotEmpty = sessionsCur.moveToFirst();
		injuriesCur.moveToFirst();
		
		/* if at least one user session exists for that particular exercise 
		 * and ankle-side */
		if((sessionsCur != null && injuriesCur != null) && sessionsCurNotEmpty) {
			
			// iterate through all entries of both cursors
			while(!sessionsCur.isAfterLast() || !injuriesCur.isAfterLast()) {
				
				// initialize both date values (in ms) to -1
				long sessionDateVal = -1L;
				long injuryDateVal = -1L;
				
				// if the sessions cursor is not past the last entry
				if(!sessionsCur.isAfterLast()) {
					sessionDateVal = stringToDate(sessionsCur.getString(0)).getTime();
				}
				
				// if the injuries cursor is not past the last entry
				if(!injuriesCur.isAfterLast()) {
					injuryDateVal = stringToDate(injuriesCur.getString(0)).getTime();
				}
				
				/* if the injury date value (in ms) is greater than that of the 
				 * session date value. note: the condition in the while loop 
				 * guarantees that at least one of the two values is valid */
				if(injuryDateVal != -1 && injuryDateVal >= sessionDateVal) {
					
					// add the injury data to the results array-list
					mResultsList.add(new String[] {
							DateFormat.format("dd - MM - yyyy", injuryDateVal).toString(), 
							ListAdapterResults.TYPE_INJURY});
					
					// increment the injury cursor
					injuriesCur.moveToNext();
					
				} else {
					
					// add the session data to the results array-list
					mResultsList.add(new String[] {
							DateFormat.format("dd - MM - yyyy", sessionDateVal).toString(), 
							String.valueOf((double) Math.round(
									sessionsCur.getDouble(1)* 100) / 100d)});
					
					// increment the sessions cursor
					sessionsCur.moveToNext();
				}
			}
			
			// display a toast indicating the selected ankle-side
			mToast = Toast.makeText(getActivity(), ankleSide + " Ankle Results", Toast.LENGTH_SHORT);
			mToast.show();
			
		// the cursors are empty. display an error-level toast
		} else {
			
			mToast = Toast.makeText(getActivity(), "Maybe you didn't do this exercise yet?", Toast.LENGTH_SHORT);
			mToast.show();
		}
		
		// close the cursors
		sessionsCur.close();
		injuriesCur.close();
		
		// notify the adapter that the underlying view is to be redrawn
		mAdapter.notifyDataSetChanged();
	}
	
	/* returns a date object corresponding to the parameterized date-string.
	 * the date is parsed using the default date format of database entries */
	private Date stringToDate(String dateString) {
		
		// initialize a new date object to the minimum value
		Date date = new Date(0);
		
		try {
			
			/* convert the date-string to the corresponding date
			 * object, using the original date-string format */
			date = originalDateFormat.parse(dateString);
			
		} catch (ParseException e) {
			if(BuildConfig.DEBUG) e.printStackTrace();
		}
		
		return date;
	}
}
