package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.DialogStyleHelper;
import com.ece1778.project.myAnkle.Helpers.GraphHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.BuildConfig;
import com.ece1778.project.myAnkleUser.R;
import com.ece1778.project.myAnkleUser.R.color;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class FragmentExerciseResults extends Fragment implements OnClickListener {

	public static final String TAG = FragmentExerciseResults.class.getSimpleName();
	private static final String ARG_SESSION_ID = "session_id";
	
	private DatabaseHelper mDatabaseHelper = null;
	
	// session parameters
	private int mUserId = -1;
	private int mExerciseId = -1;
	private int mSessionId = -1;
	private String mAnkleSide = null;
	
	// mean R
	private double mMeanR = -1;
	private double mBestLeftMeanR = -1;
	private double mBestRightMeanR = -1;
	
	// AChartEngine graph objects
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private GraphicalView mChartView = null;
	
	// layout objects
	private TextView mTextExerciseName; 
	private TextView mTextExerciseEquipment;
	private TextView mTextAnkleSide;
	private TextView mTextMeanR;
	private Button mButtonRetry;
	private Button mButtonNewExercise;
	
	// instantiate FragmentExerciseResults
	public static FragmentExerciseResults newInstance(int sessionId) {
		
		FragmentExerciseResults myFragment = new FragmentExerciseResults();
		
		Bundle args = new Bundle();
		args.putInt(ARG_SESSION_ID, sessionId);
		myFragment.setArguments(args);
		
		return myFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDatabaseHelper = new DatabaseHelper(getActivity());
		
		// indicates that the fragment should receive all menu-related
		// call-backs that are not explicitly consumed in the host activity
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_exercise_results, container, false);
		
		// link the layout objects to the corresponding UI elements
		mTextExerciseName = (TextView) view.findViewById(R.id.fragment_exercise_results_textview_exercise_name);
		mTextExerciseEquipment = (TextView) view.findViewById(R.id.fragment_exercise_results_exercise_textview_equipment);
		mTextAnkleSide = (TextView) view.findViewById(R.id.fragment_exercise_results_textview_ankle_side);
		mTextMeanR = (TextView) view.findViewById(R.id.fragment_exercise_results_textview_meanR);
		
		mButtonRetry = (Button) view.findViewById(R.id.fragment_exercise_results_button_retry);
		mButtonNewExercise = (Button) view.findViewById(R.id.fragment_exercise_results_button_new_exercise);
		
		// register the button listeners
		mButtonRetry.setOnClickListener(this);
		mButtonNewExercise.setOnClickListener(this);
		
		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// get the user id from preferences
		mUserId = PrefUtils.getIntPreference(getActivity(), PrefUtils.LOCAL_USER_ID);
		
		// retrieve the session id from arguments
		mSessionId = getArguments().getInt(ARG_SESSION_ID, -1);
		
		// fetch the session details from the database
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT userId, exerciseId, meanR, ankleSide FROM sessions WHERE _id = " 
						+ mSessionId, null);
		
		// sanity check
		if(cur != null && cur.moveToFirst()) {
			
			// get the session parameters from the cursor
			mExerciseId = cur.getInt(1);
			mMeanR = cur.getDouble(2);
			mAnkleSide = cur.getString(3);
			
		} else {
			if(BuildConfig.DEBUG) Log.d(TAG, "Query to sessions table failed");
		}
		
		// fetch the exercise name from the database
		cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT name, equipment, eyeState FROM exercises WHERE _id = " + mExerciseId, null);
		
		String exerciseName = "Not Found";
		String exerciseEquipment = "Not Found";
		String exerciseEyeState = "Not Found";
		
		// sanity check
		if(cur != null && cur.moveToFirst()) {
			
			// get the exercise parameters from the cursor
			exerciseName = cur.getString(0);
			exerciseEquipment = cur.getString(1);
			exerciseEyeState = cur.getString(2);
		}
		
		// close the cursor
		cur.close();
		
		// calculate rounded mean_r
		double roundedMeanR = ((int) Math.round(mMeanR * 100)) / 100d;
		
		// populate the textViews
		mTextExerciseName.setText(exerciseName);
		mTextExerciseEquipment.setText(exerciseEquipment + " - Eyes " + exerciseEyeState);
		mTextAnkleSide.setText("(" + mAnkleSide + " ankle)");
		mTextMeanR.setText("Balance Number = " + roundedMeanR);
		
		// get the best meanRs from the database
		mBestLeftMeanR = getBestMeanR(mMeanR, "Left");
		mBestRightMeanR = getBestMeanR(mMeanR, "Right");
		
		// create the bar graph
		createBarGraph();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// set the action bar parameters
		getActivity().getActionBar().setTitle("Results");
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// if the ChartView doesn't exist, create it
		if(mChartView == null) {
			
			LinearLayout layout = (LinearLayout) getView().findViewById(R.id.fragment_exercise_result_chart);
			mChartView = ChartFactory.getBarChartView(getActivity(), mDataset, mRenderer, Type.STACKED);
			layout.addView(mChartView);
			
			// register the chartView listener
			mChartView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					SeriesSelection selection = mChartView.getCurrentSeriesAndPoint();

					// if a valid series or point is selected
					if(selection != null) {
						
						// create an alert dialog with the current and best BNs
						createBestScoreAlert();
						
					}
				}
			});
			
		// else, redraw it
		} else {
			mChartView.repaint();
		}
		
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
		
		switch(v.getId()) {
		
		// when the 'Retry' button is clicked
		case R.id.fragment_exercise_results_button_retry: 
			
			if(BuildConfig.DEBUG) Log.d(TAG, "Retrying exercise");
			
			// return to FragmentExerciseInstruction
			String backStackTag = FragmentExercises.TAG;
    		getActivity().getSupportFragmentManager().popBackStackImmediate(backStackTag, 0);
			
			break;
			
		// when the 'New Exercise' button is clicked
		case R.id.fragment_exercise_results_button_new_exercise:
			
			// display the first fragment in the backstack, and pop all remaining ones
    		getActivity().getSupportFragmentManager().popBackStackImmediate(null, 
    				FragmentManager.POP_BACK_STACK_INCLUSIVE);
			
			break;
			
		default: break;
		}
	}
	
	// returns the best meanR for the given ankle from the Database. if the best
	// meanR is from the current session, returns the next best available value.
	// if no other value is available, returns -1
	private double getBestMeanR(double curMeanR, String ankleSide) {
		
		double bestMeanR = -1;
		
		Cursor cur = mDatabaseHelper.getWritableDatabase().query(
				"sessions", 
				new String[] {"meanR"}, "userId = ? AND exerciseId = ? AND ankleSide = ?", 
				new String[] {String.valueOf(mUserId), String.valueOf(mExerciseId), ankleSide}, 
				null, null, 
				"meanR ASC");
		
		if(cur != null && cur.moveToFirst()) {
			
			bestMeanR = cur.getDouble(0);
			
			// the best available meanR is from the current session
			if(bestMeanR == curMeanR) {
				
				// move to the next row of the cursor
				if(cur.moveToNext()) {
					bestMeanR = cur.getDouble(0);
				
				// if not other meanR is available, set the best meanR to -1
				} else {
					bestMeanR = -1;
				}
			}
			
			// debug messages: print all available meanR values
			if(cur.moveToFirst()) {
				while(!cur.isAfterLast()) {
					
					if(BuildConfig.DEBUG) Log.d(ankleSide, "MeanR : " + cur.getDouble(0));
					cur.moveToNext();
				}
			}
		}
		
		// close the cursor
		cur.close();
		
		return bestMeanR;
	}
	
	// creates and renders the bar graph (using the AChartEngine library)
	private void createBarGraph() {
		
		// clear any previously added series and renderers
		mDataset.clear();
		mRenderer.removeAllRenderers();
		
		double currentRoundedBN = ((int) Math.round(mMeanR * 100)) / 100d;
		double bestLeftBN = ((int) Math.round(mBestLeftMeanR * 100)) / 100d;
		double bestRightBN = ((int) Math.round(mBestRightMeanR * 100)) / 100d;
		
		// set the XYMultipleSeriesRenderer parameters
		setOverallRendererParameters();
		
		/* BEST MEAN R */
		
		// create a new Series and Renderer object for the BEST MeanRs
		XYSeries bestMeanSeries = new XYSeries("Best Balance Number  ");
		XYSeriesRenderer bestMeanRenderer = new XYSeriesRenderer();
		
		double[] xBest = {4, 8};
		double[] yBest = {bestLeftBN, bestRightBN};
		
		String[] ankleSides = {"Left", "Right"};
		
		// replace the x-axis number-labels (coordinates) with ankle-sides 
		// ("Left" and "Right")
		for(int i = 0; i < ankleSides.length; i++) {
			
			bestMeanSeries.add(xBest[i], yBest[i]);
			mRenderer.addXTextLabel(xBest[i], ankleSides[i]);
		}
		
		// don't display the x coordinates
		mRenderer.setXLabels(0);
		
		// set the local renderer properties
		bestMeanRenderer.setColor(getResources().getColor(color.blue));
		bestMeanRenderer.setDisplayChartValues(true);
		bestMeanRenderer.setChartValuesTextAlign(Align.CENTER);
		bestMeanRenderer.setChartValuesTextSize(24);
		
		// add the created (best meanR) series and renderer
		mDataset.addSeries(bestMeanSeries);
		mRenderer.addSeriesRenderer(bestMeanRenderer);
		
		/* CURRENT MEAN R */
		
		// create a new Series and Renderer for the CURRENT MeanR
		XYSeries currentMeanSeries = new XYSeries("Current Balance Number");
		XYSeriesRenderer currentMeanRenderer = new XYSeriesRenderer();
		
		double xCurrent = 12;
		double yCurrent = currentRoundedBN;
		
		// replace the x-axis number-labels (coordinates) with the "Current" label
		currentMeanSeries.add(xCurrent, yCurrent);
		mRenderer.addXTextLabel(xCurrent, "Current");
		
		// set the local renderer properties
		currentMeanRenderer.setDisplayChartValues(true);
		currentMeanRenderer.setChartValuesTextAlign(Align.CENTER);
		currentMeanRenderer.setChartValuesTextSize(24);
		
		// if the current meanR is better than the best mean R for that ankle,
		// set the series (bar and legend) color to green
		if(isCurrentBetterThanBest(currentRoundedBN, bestLeftBN, bestRightBN)) {
			currentMeanRenderer.setColor(getResources().getColor(color.green));
			
		// else, set the color to red
		} else {
			currentMeanRenderer.setColor(Color.RED);
		}
		
		// add the created (current meanR) series and renderer
		mDataset.addSeries(currentMeanSeries);
		mRenderer.addSeriesRenderer(currentMeanRenderer);
	}
	
	// sets the basic rendering parameters such as graph color, label size an so on
	private void setOverallRendererParameters() {

		// dynamically set the graph range depending on the values 
		double lowestUpperBound = GraphHelper.estimateUpperBound(
				new double[] {mMeanR, mBestLeftMeanR, mBestRightMeanR});
		
		// set the overall rendering parameters
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.WHITE);
				
		// font size
		mRenderer.setAxisTitleTextSize(24);
		mRenderer.setLegendTextSize(26);
		mRenderer.setLabelsTextSize(26);
				
		// axes range
		mRenderer.setXAxisMin(0);
		mRenderer.setXAxisMax(16);
		mRenderer.setYAxisMin(0);
		mRenderer.setYAxisMax(lowestUpperBound);
		
		// margin size and color
		mRenderer.setMargins(new int[] {40, 60, 50, 10});
		mRenderer.setMarginsColor(Color.WHITE);
		
		// bar element properties
		mRenderer.setBarWidth(40);
		
		// pan and zoom
		mRenderer.setPanEnabled(false);
		mRenderer.setZoomEnabled(false, false);
		mRenderer.setZoomButtonsVisible(false);
		
		// axes labels
		mRenderer.setXLabelsColor(Color.DKGRAY);
		mRenderer.setXLabelsAlign(Align.CENTER);
		mRenderer.setXLabelsPadding(15);
		
		mRenderer.setYLabelsColor(0, Color.DKGRAY);
		mRenderer.setYLabelsAlign(Align.RIGHT);
		mRenderer.setYLabelsPadding(15);
		
		// miscellaneous
		mRenderer.setFitLegend(true);
		mRenderer.setShowGrid(true);
		mRenderer.setAxesColor(Color.DKGRAY);
	}
	
	// returns true if the balance number for the current session is better (lower) than
	// the best score for that ankle. also returns true if the best BN is -1
	private boolean isCurrentBetterThanBest(double curr, double leftBest, double rightBest) {
		
		boolean isBetter = false;
		
		// ankle-side for the current session is "Left"
		if(mAnkleSide.equals("Left")) {
			
			// if current score is better OR exercise is completed for the first time
			if(leftBest == -1 || curr < leftBest) {
				isBetter = true;
			}
		// ankle-side for the current session is "Right"
		} else if(mAnkleSide.equals("Right")){
			
			// if current score is better OR exercise is completed for the first time
			if(rightBest == -1 || curr < rightBest) {
				isBetter = true;
			}
		// sanity check
		} else {
			if(BuildConfig.DEBUG) Log.d(TAG, "Invalid ankle side : " + mAnkleSide);
		}
		
		return isBetter;
	}
	
	// displays an alert dialog with the best and current BNs
	private void createBestScoreAlert() {
		
		String message = "";
		
		// left ankle BN
		message += "Left BN : ";
		
		if(mBestLeftMeanR > 0) {
			message += String.valueOf((double) Math.round(mBestLeftMeanR * 100) / 100d) + "\n";
		} else {
			message += "No best score yet!\n";
		}
		
		// right ankle BN
		message += "Right BN : ";
		
		if(mBestRightMeanR > 0) {
			message += String.valueOf((double) Math.round(mBestRightMeanR * 100) / 100d) + "\n";
		} else {
			message += "No best score yet!\n";
		}
		
		message += "\nCurrent BN : " + String.valueOf((double) Math.round(mMeanR * 100) / 100d);
		
		// create the alert dialog and set the message string
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity())
			.setTitle("Balance Scores")
			.setCancelable(true)
			.setMessage(message)
			.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
		
		// create a new instance of DialogHelper, set the parameters and display the dialog
		DialogStyleHelper box = new DialogStyleHelper(getActivity(), dlgAlert.create());
		box.setDialogButtonParams(null, -1, getActivity().getResources().getColor(R.color.blue));
		box.showDialog();
	}
	
}