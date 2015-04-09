package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentTutorialStart extends Fragment{
	
	@SuppressWarnings("unused")
	private static final String TAG = FragmentTutorialStart.class.getSimpleName();
	
	// instantiate FragmentTutorialStart
	public static FragmentTutorialStart newInstance() {
		FragmentTutorialStart myFragment = new FragmentTutorialStart();
		Bundle args = new Bundle();		
		myFragment.setArguments(args);
	    return myFragment;
	} 
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_tutorial_start,container, false);		
		return rootView;
	}

}
