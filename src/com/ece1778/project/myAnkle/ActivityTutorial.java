package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

//import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

public class ActivityTutorial extends FragmentActivity {

	private static final String TAG = ActivityTutorial.class.getSimpleName();

	static SectionsPagerAdapter mSectionsPagerAdapter;
	static ViewPager mViewPager;	
	
	public static final String ARG_TUTORIAL_TYPE = "tutorial_type"; 
	public static enum Tutorial_Type{FULL, PARTIAL};

	// the number of fragments, of each type, to display
	public static final int NUM_START = 1;
	public static final int NUM_STEPS = 4;
	public static final int NUM_AGREEMENT = 3;
	public static final int NUM_FINISH = 1;
	
	// the pages separating different fragment types
	public static final int STEPS_BEGIN_AT = NUM_START; // = 1
	public static final int AGREEMENTS_BEGIN_AT = STEPS_BEGIN_AT + NUM_STEPS; // = 5
	public static final int FINISH_BEGIN_AT = AGREEMENTS_BEGIN_AT + NUM_AGREEMENT; // = 8
	
	private Tutorial_Type tutorial_type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		
		// check if we should create full tutorial with agreements, or just with steps
		tutorial_type = (Tutorial_Type) getIntent().getSerializableExtra(ARG_TUTORIAL_TYPE);
		
		// if tutorial type isn't specified by the caller, default to full. redundancy check
		if(tutorial_type != Tutorial_Type.FULL && tutorial_type != Tutorial_Type.PARTIAL) {
			tutorial_type = Tutorial_Type.FULL;
		}
		
		// Create the adapter that will return a fragment tutorial pages
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.activity_tutorial_pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
	}
	
	@Override
	  public void onStart() {
	    super.onStart();
	    // enable google analytics
//	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }
	
	@Override
	public void onStop() {
		super.onStop();
		// disable google analytics
//	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
		if(tutorial_type == Tutorial_Type.FULL){
			if(keyCode == KeyEvent.KEYCODE_BACK){
				Toast.makeText(this, "Please finish the tutorial", Toast.LENGTH_SHORT).show();
				return false;
			} 
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.	
		// TODO : add menu
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		// the total number of view-pager fragments to render in each mode:
		public static final int NUM_FRAGMENTS_IN_FULL = NUM_START + NUM_STEPS + NUM_AGREEMENT
				+ NUM_FINISH; // = 10
		public static final int NUM_FRAGMENTS_IN_PARTIAL = NUM_START + NUM_STEPS 
				+ NUM_FINISH; // = 6
		
		// a place to store references to fragments that we need to access even when not visible
		private Fragment[] fragList;
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			
			// init the array storing references to each fragment in the viewpager
			if(tutorial_type == Tutorial_Type.FULL) {
				fragList = new Fragment[NUM_FRAGMENTS_IN_FULL];
			} else {
				fragList = new Fragment[NUM_FRAGMENTS_IN_PARTIAL];
			}
		}

		@Override
		public Fragment getItem(int position) {
			if(BuildConfig.DEBUG) Log.i(ActivityTutorial.TAG, "get item called for position " + String.valueOf(position));
			Fragment fragment = null;
			
			if(position == 0) {
				
				// the first fragment is always the start fragment
				fragment = FragmentTutorialStart.newInstance();
				
			} else if(position >= STEPS_BEGIN_AT && position < AGREEMENTS_BEGIN_AT) {
				
				// display the step fragments
				fragment = FragmentTutorialStep.newInstance(position - STEPS_BEGIN_AT);
				
			} else if(tutorial_type == Tutorial_Type.FULL) {
				
				// in the FULL tutorial mode, the next few fragments are rendered differently
				
				if(position >= AGREEMENTS_BEGIN_AT && position < FINISH_BEGIN_AT) {
					
					// display the agreement fragments
					fragment = FragmentAgreement.newInstance(position - AGREEMENTS_BEGIN_AT, position);
					
				} else if(position == FINISH_BEGIN_AT) {
					
					// display the finish fragment
					fragment = FragmentTutorialFinish.newInstance(Tutorial_Type.FULL);
				}
			} else if(tutorial_type == Tutorial_Type.PARTIAL) {
				
				// in PARTIAL tutorial mode, skip the agreement and profile fragments,
				
				// display the finish fragment
				fragment = FragmentTutorialFinish.newInstance(Tutorial_Type.PARTIAL);
				
			} else {
				// should not reach here
				if(BuildConfig.DEBUG) Log.e(TAG, "ViewPager requested for non-existent page");
				finish();
			}
			
			// save a reference to the requested fragment
			fragList[position] = fragment;
						
			// return the created fragment
			return fragment;			
			
		}
		
		public Fragment getFragment(int position) {
		    return fragList[position];
		}
		
		@Override
		public int getCount() {
			
			// the total number of pages to create.
			if(tutorial_type == Tutorial_Type.FULL) {
				return NUM_FRAGMENTS_IN_FULL;
			}else if (tutorial_type == Tutorial_Type.PARTIAL){
				return NUM_FRAGMENTS_IN_PARTIAL;
			}else{
				return 0;
			}
		}
		
		@Override
		public CharSequence getPageTitle(int position) {			
			return null;
		}
	
	}
}