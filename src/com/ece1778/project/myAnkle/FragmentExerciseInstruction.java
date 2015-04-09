package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.FragmentHelper;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentExerciseInstruction extends Fragment implements OnClickListener {
	
	public static final String TAG = FragmentExerciseInstruction.class.getSimpleName();
	public static final String ARG_EXERCISE_ID = "exercise_id"; 
	
	private TextView mTextName;
	private TextView mTextEquipment;
	private TextView mTextDifficulty;
	private TextView mTextExplanation;
	private ImageView mImageIcon;
	private Button mButtonStart;
	
	private DatabaseHelper mDatabaseHelper = null;
	private int mExerciseId = -1;

	private ActivityMeasure parentActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		parentActivity = (ActivityMeasure) getActivity();
		mDatabaseHelper = new DatabaseHelper(getActivity());
	}
	
	// instantiate FragmentExerciseInstruction
	public static FragmentExerciseInstruction newInstance(int exercise_id){
		
		// create fragment
		FragmentExerciseInstruction myFragment = new FragmentExerciseInstruction();
		
		// bundle arguments
		Bundle args = new Bundle();		
		args.putInt(ARG_EXERCISE_ID, exercise_id);		
		
		// set arguments		
		myFragment.setArguments(args);
		
		// return fragment
	    return myFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_exercise_instructions, container, false);
		mButtonStart = (Button) view.findViewById(R.id.fragment_exercise_instructions_button_start);
        mButtonStart.setOnClickListener(this);
 		return view;
	}
	  
	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		
		// Use exercise_id to find the corresponding exercise details
		mExerciseId = getArguments().getInt(ARG_EXERCISE_ID, -1);
		
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
			"SELECT name, instruction, picture, equipment, eyeState, difficulty FROM exercises WHERE _id = " + mExerciseId, null);
		cur.moveToFirst();

		// Load views
		mTextName = (TextView) getView().findViewById(R.id.fragment_exercise_instructions_textview_name);
		mTextName.setText(cur.getString(0));
		
		mTextEquipment = (TextView) getView().findViewById(R.id.fragment_exercise_instructions_textview_equipment);
		mTextEquipment.setText(cur.getString(3) + " - Eyes " + cur.getString(4));
		
		mTextDifficulty = (TextView) getView().findViewById(R.id.fragment_exercise_instructions_textview_difficulty);
		mTextDifficulty.setText("Difficulty : " + cur.getString(5));

		mTextExplanation = (TextView) getView().findViewById(R.id.fragment_exercise_instructions_textview_explination);
		mTextExplanation.setText(cur.getString(1));

		mImageIcon = (ImageView) getView().findViewById(R.id.fragment_exercise_instructions_imageview_icon);
		int id = getStringIdentifier(getActivity(), cur.getString(2));
		mImageIcon.setImageResource(id);
		
		cur.close();
	}

	private int getStringIdentifier(Context context, String name) {
		return context.getResources().getIdentifier(name, "drawable",
				context.getPackageName());
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// set the action bar parameters
		getActivity().getActionBar().setTitle("Instructions");
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// debug messages
		FragmentManager fm = getActivity().getSupportFragmentManager();
		if(BuildConfig.DEBUG) {
			
			Log.i(TAG, "The exercise ID is " + mExerciseId + " & number of"
				+ " backstack entries are " + fm.getBackStackEntryCount());
		
			for(int i=0; i < fm.getBackStackEntryCount(); i++) {
				Log.d(TAG, fm.getBackStackEntryAt(i).getName());
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mDatabaseHelper.close();
	}

	@Override
	public void onClick(View v) {
//		switch (v.getId()) {
//        	
//		case R.id.fragment_exercise_instructions_button_start:
//        		
//			// create a new instance of FragmentExerciseMeasure replace the
//			// fragment in the placeholder
//			Fragment newFragment = FragmentExerciseMeasure.newInstance(mExerciseId);
//			String nextFragmentTag = FragmentExerciseMeasure.TAG;
//			FragmentHelper.swapFragments(getActivity().getSupportFragmentManager(), 
//					R.id.activity_measure_container, newFragment,
//					false, true, TAG, nextFragmentTag);
//			
//        	break;
//        	
//		default: break;
//		}
		
		/* Feedback system input dialog */
		if (mButtonStart == v) {
			final Dialog feedbackSystem = new Dialog(getActivity());
			feedbackSystem.setContentView(R.layout.fragment_feedback_dialog);
			feedbackSystem.setTitle("Feedback System");
			
			Button btnOnFeedback = (Button) feedbackSystem.findViewById(R.id.btnOn);
			Button btnOffFeedback = (Button) feedbackSystem.findViewById(R.id.btnOff);
			
			final EditText targetBN = (EditText) feedbackSystem.findViewById(R.id.feedback_lower);
			final EditText currentBN = (EditText) feedbackSystem.findViewById(R.id.feedback_upper);
			
			btnOnFeedback.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String targetBNStr = targetBN.getText().toString();
					String currentBNStr = currentBN.getText().toString();
					
					if (!isNumeric(targetBNStr) && !isNumeric(currentBNStr)) {
						Toast.makeText(getActivity(), "Please enter a numeric value", Toast.LENGTH_SHORT).show();
						return;
					}
					
					final double targetBNDb = Double.parseDouble(targetBNStr);
					final double currentBNDb = Double.parseDouble(currentBNStr);
					
					if ((targetBNDb >= 0.01 && targetBNDb <= 2) &&
							(currentBNDb >= 0.01 && currentBNDb <= 2)) {
						if ((currentBNDb - targetBNDb) >= 0.049) {
							parentActivity.feedback_status = true;
							parentActivity.feedbackLowerBN = targetBNDb;
							parentActivity.feedbackUpperBN = currentBNDb;
							feedbackSystem.dismiss();
							startExerciseFragment();
						} else {
							Toast.makeText(getActivity(), "Current BN > Target BN\nCurrent BN - Target BN >= 0.05", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getActivity(), "0.01 <= Current BN <= 2.0\n0.01 <= Target BN <= 2.0", Toast.LENGTH_SHORT).show();
					}
				}
			});
			
			btnOffFeedback.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					parentActivity.feedback_status = false;
					feedbackSystem.dismiss();
					startExerciseFragment();
				}
			});
			
			feedbackSystem.show();
		}
	}
	
	public static boolean isNumeric(String string) {
		return string.matches("^\\d+\\.\\d+$") || string.matches("^\\d+$") || string.matches("^\\.\\d+$");
	}

	private void startExerciseFragment () {
		Fragment newFragment = FragmentExerciseMeasure.newInstance(mExerciseId);
		String nextFragmentTag = FragmentExerciseMeasure.TAG;
		FragmentHelper.swapFragments(getActivity().getSupportFragmentManager(), 
				R.id.activity_measure_container, newFragment,
				false, true, TAG, nextFragmentTag);
	}
}