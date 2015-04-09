package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class FragmentAgreement extends Fragment implements OnCheckedChangeListener {
	
	private static final String TAG = FragmentAgreement.class.getSimpleName();
	public static final String ARG_AGREEMENT_NUM = "agreement_number";
	public static final String ARG_RAW_POSITION = "raw_position";
	
	private TextView mTextTitle;
	private TextView mTextBody;
	private CheckBox mCheckboxAccept;
	
	private boolean mHasAgreed = false;
	private int mThisSection = 0;
	
	// instantiate FragmentAgreement with the agreement page number and its absolute
	// position in the viewpager
	public static FragmentAgreement newInstance(int agreement_number, int raw_position) {
		
		// create page fragment
		FragmentAgreement myFragment = new FragmentAgreement();	

		// bundle arguments
		Bundle args = new Bundle();
		args.putInt(FragmentAgreement.ARG_AGREEMENT_NUM, agreement_number);
		args.putInt(FragmentAgreement.ARG_RAW_POSITION, raw_position);
		
		// pass in arguments
		myFragment.setArguments(args);
		
		// return fragment
	    return myFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_agreement,container, false);
		
		// link UI elements
		mTextTitle = (TextView)rootView.findViewById(R.id.fragment_agreement_textview_title);
		mTextBody = (TextView)rootView.findViewById(R.id.fragment_agreement_textview_body);
		mCheckboxAccept = (CheckBox)rootView.findViewById(R.id.fragment_agreement_checkbox_accept);
		
		int agreement_number = getArguments().getInt(ARG_AGREEMENT_NUM);
		if(BuildConfig.DEBUG) Log.i(FragmentAgreement.TAG, "the agreement_number is " + String.valueOf(agreement_number));
		
		mThisSection = getArguments().getInt(ARG_RAW_POSITION);
		
		// set appropriate title and text for this agreement 
		switch (agreement_number) {
			case 0: mTextTitle.setText(R.string.fragment_agreement_title_1); break;
			case 1: mTextTitle.setText(R.string.fragment_agreement_title_2); break;
			case 2: mTextTitle.setText(R.string.fragment_agreement_title_3); break;
		}
		
		switch (agreement_number) {
			case 0: mTextBody.setText(R.string.fragment_agreement_body_1); break;
			case 1: mTextBody.setText(R.string.fragment_agreement_body_2); break;
			case 2: mTextBody.setText(R.string.fragment_agreement_body_3); break;
		}
		
		// associate check listener
		mCheckboxAccept.setOnCheckedChangeListener(this);
		
		return rootView;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		// update public variable to expose value to other fragments in the viewpager	
		mHasAgreed = isChecked;
		
		// debug messages
		if(BuildConfig.DEBUG) Log.d(FragmentAgreement.TAG, "checked = " 
				+ String.valueOf(mHasAgreed));
				
		// if agreement checkbox on the current fragment has been activated, scroll
		// to the next page
		if(isChecked){
			int currentSection = ActivityTutorial.mViewPager.getCurrentItem();
			if(currentSection == mThisSection){
				ActivityTutorial.mViewPager.setCurrentItem(currentSection+1, true);	
			}
		}	
	}	
	
	// returns true if the fragment's checkbox is checked
	public boolean hasAgreed(){
		return mHasAgreed;
	}
}
