package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.R;
//import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

public class ActivityProfile extends FragmentActivity {
	
	@SuppressWarnings("unused")
	private static final String TAG = ActivityProfile.class.getSimpleName();
	
	private static int TOTAL_PAGES;
	private static final String[] tabs = {"Delete User", "Personal", "Injuries"};
	
	// the number of fragments, of each type, to display
	public static final int NUM_DELETE = 1;
	public static final int NUM_PERSONAL = 1;
	public static final int NUM_INJURIES = 1;
	
	// the array indices from where the individual fragments start
	// in full ('update') mode
	public static final int PERSONAL_STARTS_AT = NUM_DELETE; // = 1
	public static final int INJURIES_STARTS_AT = PERSONAL_STARTS_AT + NUM_PERSONAL; // = 2
	
	public static SectionsPagerAdapter mSectionsPagerAdapter;
	public static ViewPager mViewPager;
	
	public static final String ARG_PROFILE_MODE = "profile_mode";
	public static final String CREATE_MODE = "create_user";
	public static final String UPDATE_MODE = "update_user";
	private String mProfileMode = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		// retrieve the launch mode from the Intent
		mProfileMode = getIntent().getStringExtra(ARG_PROFILE_MODE);
		
		// if the activity is launched in 'update' mode, display the
		// 'Delete User' tab. else, don't display it
		if(mProfileMode.equals(UPDATE_MODE)) {
			TOTAL_PAGES = NUM_DELETE + NUM_PERSONAL + NUM_INJURIES;
			
		} else {
			TOTAL_PAGES = NUM_PERSONAL + NUM_INJURIES;
		}
				
		// create an object of the custom adapter to display the fragments
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		
		// set up the viewpager
		mViewPager = (ViewPager) findViewById(R.id.activity_profile_pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// in 'update' mode, offset (from 0) the default item selection
		if(mProfileMode.equals(UPDATE_MODE)) {
			mViewPager.setCurrentItem(PERSONAL_STARTS_AT);
		}
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
	
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		private Fragment[] fragList = new Fragment[TOTAL_PAGES];
		
		// default constructor
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			Fragment newFragment = null;
			
			// in update mode, display all the fragments
			if(mProfileMode.equals(UPDATE_MODE)) {
				
				switch(position) {
				
				case 0: // display the 'Delete User' fragment
					newFragment = FragmentProfileDelete.newInstance(); 
					break;
					
				case 1: // display the 'Personal' fragment
					newFragment = FragmentProfilePersonal.newInstance(mProfileMode);
					break;
					
				case 2: // display the 'Injuries' fragment
					newFragment = FragmentProfileInjuries.newInstance(mProfileMode);
					break;
				}
				
			// in create mode
			} else {
				
				switch(position) {
				
				case 0: // display the 'Personal' fragment
					newFragment = FragmentProfilePersonal.newInstance(mProfileMode);
					break;
					
				case 1: // display the 'Injuries' fragment
					newFragment = FragmentProfileInjuries.newInstance(mProfileMode);
					break;
				}
			}
			
			// retain a reference to the newly instantiated fragment
			fragList[position] = newFragment;
			return newFragment;
		}

		@Override
		public int getCount() {
			return TOTAL_PAGES;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			
			// in update mode, the index of the corresponding fragment's title
			// in the array is equal to the parameterized position
			if(mProfileMode.equals(UPDATE_MODE)) {
				return tabs[position];
				
			// else, offset the index by the difference in number of 
			// fragments displayed in the two modes
			} else {
				return tabs[position + NUM_DELETE];
			}
		}
		
		public Fragment getFragment(int position) {
			return fragList[position];
		}
	}
}
