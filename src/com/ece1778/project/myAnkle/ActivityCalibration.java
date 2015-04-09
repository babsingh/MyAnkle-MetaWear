package com.ece1778.project.myAnkle;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.KeyEvent;

import com.ece1778.project.myAnkle.Helpers.FragmentHelper;
import com.ece1778.project.myAnkle.metawear.ModuleActivity;
import com.ece1778.project.myAnkleUser.R;

public class ActivityCalibration extends ModuleActivity {

	@SuppressWarnings("unused")
	private static final String TAG = ActivityCalibration.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		
		// get BT device & name 
		Bundle args = getIntent().getExtras();
		deviceName = args.getString("device_name");
		device = (BluetoothDevice) args.getParcelable("ble_device");
		
		// create a new instance of FragmentCalibration and add it to the placeholder
		FragmentCalibration newFragment = FragmentCalibration.newInstance();
		FragmentHelper.swapFragments(getSupportFragmentManager(), 
				R.id.activity_calibration_container, newFragment, 
				true, false, null, FragmentCalibration.TAG);
	}

	// on pressing the back button, create a dialog asking whether
	// calibration should be ended
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		// get a reference to the instance of FragmentCalibration in the placeholder
		FragmentCalibration fragment = (FragmentCalibration) getSupportFragmentManager()
				.findFragmentById(R.id.activity_calibration_container);
		
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			
			// if fragment in the placeholder is not null (redundant check), 
			// let the fragment handle the key-press
			if(fragment != null) fragment.onBackPressed();
		}
		
		return false;
	}
}
