package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentTutorialStep extends Fragment {

	@SuppressWarnings("unused")
	private static final String TAG = FragmentTutorialStep.class.getSimpleName();	
	public static final String ARG_SECTION_NUMBER = "section_number";
	
	private TextView mTextPlaceholder;
	private ImageView mImagePlaceholder;
	
	// instantiate FragmentTutorialStep with the selected step fragment in the viewpager
	public static FragmentTutorialStep newInstance(int position) {
		FragmentTutorialStep myFragment = new FragmentTutorialStep();
		
		Bundle args = new Bundle();
		args.putInt(FragmentTutorialStep.ARG_SECTION_NUMBER, position);
				
		myFragment.setArguments(args);
	    return myFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_tutorial_step,container, false);
		
		// link the layout objects to corresponding UI elements
		mTextPlaceholder = (TextView) rootView.findViewById(R.id.fragment_tutorial_textview_body);
		mImagePlaceholder = (ImageView) rootView.findViewById(R.id.fragment_tutorial_imageview_step);
		
		int section_number = getArguments().getInt(ARG_SECTION_NUMBER);
		
		// set appropriate image and text for this step
		switch (section_number) {
		case 0: mTextPlaceholder.setText(R.string.tutorial_description_step1); break;
		case 1: mTextPlaceholder.setText(R.string.tutorial_description_step2); break;
		case 2:	mTextPlaceholder.setText(R.string.tutorial_description_step3); break;
		case 3:	mTextPlaceholder.setText(R.string.tutorial_description_step4); break;	
		}
		
		switch (section_number) {
		case 0: mImagePlaceholder.setImageResource(R.drawable.tutorial_step_1); break;
		case 1: mImagePlaceholder.setImageResource(R.drawable.tutorial_step_2); break;
		case 2:	mImagePlaceholder.setImageResource(R.drawable.tutorial_step_3); break;
		case 3:	mImagePlaceholder.setImageResource(R.drawable.tutorial_step_4); break;	
		}
		
		return rootView;
	}
}
