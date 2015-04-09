package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.FragmentHelper;
import com.ece1778.project.myAnkle.Helpers.OnSwipeListener;
import com.ece1778.project.myAnkleUser.R;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentProgressGraphPager extends Fragment {
	
	public static final String TAG = FragmentProgressGraphPager.class.getSimpleName();
	private static final String ARG_EXERCISE_ID = "exercise_id";
	
	private int mExerciseId = -1;
	private DatabaseHelper mDatabaseHelper = null;

	// layout objects
	private TextView mTextExerciseName;
	private TextView mTextExerciseSubtitle;
	private Button mButtonSwapFragments;
	
	public SectionsPagerAdapter mSectionsPagerAdapter = null;
	public ViewPager mViewPager = null;
	
	// instantiate FragmentProgessPager
	public static FragmentProgressGraphPager newInstance(int exerciseId) {
		
		FragmentProgressGraphPager myFragment = new FragmentProgressGraphPager();
		
		Bundle args = new Bundle();
		args.putInt(ARG_EXERCISE_ID, exerciseId);
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
		
		View view = inflater.inflate(R.layout.fragment_progress_graph_pager, container, false);
		
		// link the layout objects to the corresponding UI elements
		mTextExerciseName = (TextView) view.findViewById(R.id.fragment_progress_label_title);
		mTextExerciseSubtitle = (TextView) view.findViewById(R.id.fragment_progress_label_subtitle);
		mButtonSwapFragments = (Button) view.findViewById(R.id.fragment_progress_graph_pager_buttonbar);
		mViewPager = (ViewPager) view.findViewById(R.id.fragment_progress_graph_pager);
		
		// register the touch-listener for the fragment button-bar
		mButtonSwapFragments.setOnTouchListener(new OnSwipeListener(getActivity()) {

			// when a swipe-up event is triggered
			@Override
			public void onSwipeUp() {
				
				// instantiate FragmentProgressTableView and add it to the placeholder.
				// display sliding animations during the fragment's entry (up) and exit (down)
				Fragment newFragment = FragmentProgressListView.newInstance(mExerciseId);
				
				FragmentHelper.swapFragments(getActivity().getSupportFragmentManager(), 
						R.id.fragment_progress_inner_container, newFragment, 
						true, true, null, FragmentProgressListView.TAG, true, 
						R.anim.slide_up, R.anim.slide_down);
			}
			
		});
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// get the exerciseId from the arguments
		mExerciseId = getArguments().getInt(ARG_EXERCISE_ID, -1);
		
		// using the exerciseId, fetch the exercise details from the database
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT name, equipment, eyeState FROM exercises WHERE _id = " + mExerciseId, null);
		
		String exerciseName = "Not Found";
		String exerciseEquipment = "Not Found";
		String exerciseEyeState = "Not Found";
		
		// sanity check
		if(cur != null && cur.moveToFirst()) {
			
			// get the exercise parameters from the cursor
			exerciseName = cur.getString(0);
			exerciseEquipment = cur.getString(1);
			exerciseEyeState = cur.getString(2);
		}
		
		// close the cursor
		cur.close();
		
		// set the exercise name and equipment text labels
		mTextExerciseName.setText(exerciseName);
		mTextExerciseSubtitle.setText(exerciseEquipment + " - Eyes " + exerciseEyeState);
		
		// create a new SectionsPagerAdapter object and link it to the ViewPager
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// dim the notification bar and the soft-buttons
		getActivity().getWindow().getDecorView()
			.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mDatabaseHelper.close();
		
		// restore the default UI mode
		getActivity().getWindow().getDecorView()
			.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		// the total number of graph fragments to be rendered
		private static final int GRAPH_FRAGMENT_COUNT = 2;
		private final int[] intervalDaysToDisplay = {5};
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			switch(position) {
			
			// instantiate FragmentProgressGraph in 'Last X Days' mode
			case 0: return FragmentProgressGraph.newInstance
					(mExerciseId, FragmentProgressGraph.MODE_LAST_X_DAYS, 
							intervalDaysToDisplay[0]);
			
			// instantiate FragmentProgressGraph in 'All Results' mode
			case 1: return FragmentProgressGraph.newInstance
					(mExerciseId, FragmentProgressGraph.MODE_ALL_TIME);
			}
			
			return null;
		}

		@Override
		public int getCount() {

			// return the total number of fragments to render
			return GRAPH_FRAGMENT_COUNT;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			
			switch(position) {
			
			case 0: return "Last " + intervalDaysToDisplay[0] + " Days";
			case 1: return "All Time";
			}
			
			return null;
		}
	}
}
