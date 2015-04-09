package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class FragmentInfo extends Fragment{
	public static final String TAG = FragmentTitle.class.getSimpleName();
	public static final String ARG_SECTION_NUMBER = "section_number";
	
	public FragmentInfo(){
		
	}
	
	public static FragmentInfo newInstance(int position) {
		
		// make a new fragment
		FragmentInfo myFragment = new FragmentInfo();
		
		// package and set the arguments
		Bundle args = new Bundle();
		args.putInt(FragmentTutorialStep.ARG_SECTION_NUMBER, position);		
		myFragment.setArguments(args);
		
		// return the fragment so that it can be transacted
	    return myFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		// associate layout view
		View rootView = inflater.inflate(R.layout.fragment_info, container, false);
		TextView bodyTextView = (TextView) rootView.findViewById(R.id.fragment_info_textview_body);
		TextView titleTextView = (TextView) rootView.findViewById(R.id.fragment_info_textview_title);
		
		// pull  fragment variables from the bundle
		int section_number = getArguments().getInt(ARG_SECTION_NUMBER);
		
		// TODO : should send title string as argument to make fragment more generic
		// set title
		switch(section_number){
		case 0 : bodyTextView.setText(R.string.info_content_section1); break;
		case 1 : bodyTextView.setText(R.string.info_content_section2); break;
		case 2 : bodyTextView.setText(R.string.info_content_section3); break;
		case 3 : bodyTextView.setText(R.string.info_content_section4); break;
		}
		
		// TODO : should send body string as argument to make fragment more generic
		// set body text
		switch(section_number){
		case 0 : titleTextView.setText(R.string.info_title_section1); break;
		case 1 : titleTextView.setText(R.string.info_title_section2); break;
		case 2 : titleTextView.setText(R.string.info_title_section3); break;
		case 3 : titleTextView.setText(R.string.info_title_section4); break;
		}
		
		return rootView;
	}

}
