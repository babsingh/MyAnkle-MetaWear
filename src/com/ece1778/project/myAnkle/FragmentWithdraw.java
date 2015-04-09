package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FragmentWithdraw extends Fragment implements OnCheckedChangeListener {
	
	@SuppressWarnings("unused")
	private static final String TAG = FragmentWithdraw.class.getSimpleName();

	
	public FragmentWithdraw() {
		
	}
	
	
	public static FragmentWithdraw newInstance() {
		
		// create withdraw fragment
		FragmentWithdraw myFragment = new FragmentWithdraw();	
		Bundle args = new Bundle();
		myFragment.setArguments(args);
	    return myFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_withdraw,container, false);
		
		// link UI elements
		CheckBox cb_accept = (CheckBox)rootView.findViewById(R.id.fragment_withdraw_checkbox_accept);
		
		if ( PrefUtils.getIntPreference(getActivity(), PrefUtils.OPT_OUT_KEY) == 1) cb_accept.setChecked(true);
		
		// associate check listener
		cb_accept.setOnCheckedChangeListener(this);
		
		return rootView;
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// update the global preferences about data collection opt out
		PrefUtils.setIntPreference(getActivity(), PrefUtils.OPT_OUT_KEY, arg1 ? 1 : 0 );		
	}

}
