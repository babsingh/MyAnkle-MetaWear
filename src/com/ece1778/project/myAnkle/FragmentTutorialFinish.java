package com.ece1778.project.myAnkle;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.ece1778.project.myAnkle.ActivityTutorial.Tutorial_Type;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.R;

public class FragmentTutorialFinish extends Fragment implements OnClickListener {
	
	@SuppressWarnings("unused")
	private static final String TAG = FragmentTutorialFinish.class.getSimpleName();
	
	private Button mButtonRestart;
	private Button mButtonExit;
	
	public static final String ARG_TUTORIAL_TYPE = "tutorial_type";
	private Tutorial_Type mTutorialType;

	// instantiate FragmentTutorialFrinish with the tutorial mode (full or partial)
	public static FragmentTutorialFinish newInstance(Tutorial_Type tutorial_type) {
		FragmentTutorialFinish myFragment = new FragmentTutorialFinish();
		
		Bundle args = new Bundle();
		args.putSerializable(FragmentTutorialFinish.ARG_TUTORIAL_TYPE, tutorial_type);
		
		myFragment.setArguments(args);
	    return myFragment;

	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_tutorial_finish,container, false);
		
		mTutorialType = (Tutorial_Type) getArguments().getSerializable(ARG_TUTORIAL_TYPE);
		
		// link the buttons
		mButtonRestart = (Button)rootView.findViewById(R.id.fragment_tutorial_finish_button_do_not_consent);
		mButtonExit = (Button)rootView.findViewById(R.id.fragment_tutorial_finish_button_consent);
		
		// register listeners
		mButtonRestart.setOnClickListener(this);			
		mButtonExit.setOnClickListener(this);		
		
		return rootView;
	}


	@Override
	public void onClick(View v) {
		// if restart button is pushed
		if(v.getId() == R.id.fragment_tutorial_finish_button_do_not_consent){
			
			// exit activity with a canceled result
			getActivity().setResult(Activity.RESULT_CANCELED);
			getActivity().finish();		

			// if finished is pressed	
		}else if (v.getId() == R.id.fragment_tutorial_finish_button_consent){
			
			if(mTutorialType == Tutorial_Type.FULL){
				
				int privacyAgreementPositon = ActivityTutorial.AGREEMENTS_BEGIN_AT;
				int medicalAgreementPosition = ActivityTutorial.AGREEMENTS_BEGIN_AT + 1;
				int researchAgreementPosition = ActivityTutorial.AGREEMENTS_BEGIN_AT + 2;
				
				// check if privacy agreement is checked
				FragmentAgreement fragmentPrivacyAgreement = 
						(FragmentAgreement) ActivityTutorial.mSectionsPagerAdapter.getFragment(privacyAgreementPositon);
				boolean agreedPrivacy = fragmentPrivacyAgreement.hasAgreed();
						
				// check if medical agreement is checked
				FragmentAgreement fragmentMedicalAgreement = 
						(FragmentAgreement) ActivityTutorial.mSectionsPagerAdapter.getFragment(medicalAgreementPosition);
				boolean agreedMedical = fragmentMedicalAgreement.hasAgreed();	
				
				// check if research Ethics agreement is checked
				FragmentAgreement fragmenResearchAgreement = 
						(FragmentAgreement) ActivityTutorial.mSectionsPagerAdapter.getFragment(researchAgreementPosition);
				boolean agreedResearch = fragmenResearchAgreement.hasAgreed();
				
				if(!agreedPrivacy){
					// set the view pager to the privacy agreement
					// TODO: bad design hardcoding activity pager, means the fragment is not portable
					ActivityTutorial.mViewPager.setCurrentItem(privacyAgreementPositon, true);
				}else if (!agreedMedical){
					// set the view pager to the medical agreement
					// TODO: bad design hardcoding activity pager, means the fragment is not portable
					ActivityTutorial.mViewPager.setCurrentItem(medicalAgreementPosition, true);
				}else if(!agreedResearch){
					// set the view pager to the medical agreement
					// TODO: bad design hardcoding activity pager, means the fragment is not portable
					ActivityTutorial.mViewPager.setCurrentItem(researchAgreementPosition, true);					
				} else {
					
					// default the user to having not opted out of data collection
					PrefUtils.setIntPreference(getActivity(), PrefUtils.OPT_OUT_KEY,0);
					
					// return a success message to the caller and close the activity
					getActivity().setResult(Activity.RESULT_OK);
					getActivity().finish();					
				}
			} else {
				// finish tutorial activity
				getActivity().finish();		
			}
					
		} // if finish button was pressed				
	}
	

}
