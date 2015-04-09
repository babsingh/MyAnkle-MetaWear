package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkle.Helpers.DatabaseHelper;
import com.ece1778.project.myAnkle.Helpers.DialogStyleHelper;
import com.ece1778.project.myAnkle.Helpers.GraphHelper;
import com.ece1778.project.myAnkle.Helpers.PrefUtils;
import com.ece1778.project.myAnkleUser.R;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class FragmentProgressGraph extends Fragment {

	public static final String TAG = FragmentProgressGraph.class
			.getSimpleName();

	// graph types
	public static final String MODE_LAST_X_DAYS = "last_x_days";
	public static final String MODE_ALL_TIME = "all_time";
	
	// bundled argument keys
	private static final String ARG_EXERCISE_ID = "exercise_id";
	private static final String ARG_GRAPH_TYPE = "graph_type";
	private static final String ARG_NUM_DAYS = "num_days";
	
	// the date-pattern to display the x-axis labels in
	private String mDisplayDateFormat = "dd-MMM";

	// default number of days, months and years to display in 'Last X Days' mode
	private int mTotalDaysOffset = 5;
	public int mXDays = 5;
	public int mXMonths = 0;
	public int mXYears = 0;

	// user and exercise parameters
	private int mUserId = -1;
	private int mExerciseId = -1;
	private String mGraphType = null;

	// AChartEngine graph objects
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private GraphicalView mChartView = null;

	// x- and y-axis range values
	public double maxXCoord = 0, maxYCoord = 0;

	private DatabaseHelper mDatabaseHelper = null;

	// instantiate FragmentProgressGraph
	public static FragmentProgressGraph newInstance(int exerciseId,
			String graphType) {

		FragmentProgressGraph myFragment = new FragmentProgressGraph();

		Bundle args = new Bundle();
		args.putInt(ARG_EXERCISE_ID, exerciseId);
		args.putString(ARG_GRAPH_TYPE, graphType);
		myFragment.setArguments(args);

		return myFragment;
	}

	// instantiate FragmentProgressGraph with the number of days to display
	// results for
	public static FragmentProgressGraph newInstance(int exerciseId,
			String graphType, int numXDays) {

		FragmentProgressGraph myFragment = new FragmentProgressGraph();

		Bundle args = new Bundle();
		args.putInt(ARG_EXERCISE_ID, exerciseId);
		args.putString(ARG_GRAPH_TYPE, graphType);
		args.putInt(ARG_NUM_DAYS, numXDays);
		myFragment.setArguments(args);

		return myFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDatabaseHelper = new DatabaseHelper(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_progress_graph,
				container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// get the userId from preferences
		mUserId = PrefUtils.getIntPreference(getActivity(),
				PrefUtils.LOCAL_USER_ID);

		// retrieve the exerciseId and graph-type from the arguments
		mExerciseId = getArguments().getInt(ARG_EXERCISE_ID);
		mGraphType = getArguments().getString(ARG_GRAPH_TYPE);

		// if the fragment is launched in 'Last X Days' mode
		if (mGraphType.equals(MODE_LAST_X_DAYS)) {
			
			// set the number of days, months and years to offset by
			mTotalDaysOffset = getArguments().getInt(ARG_NUM_DAYS);
			setTimeOffset(mTotalDaysOffset);
		}

		// create the line graph
		createTimeGraph();
	}

	@Override
	public void onResume() {
		super.onResume();

		if(mChartView == null) {
			
			// link the layout object to the UI element
			LinearLayout layout = (LinearLayout) getView().findViewById(
					R.id.fragment_progress_graph_chart);

			// create the chartView and add it to the layout
			mChartView = ChartFactory.getTimeChartView(getActivity(), mDataset,
					mRenderer, mDisplayDateFormat);
			layout.addView(mChartView);
			mChartView.repaint();

			// register the on-click listener for the chartView
			mChartView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// get the currently selected series and point
					SeriesSelection selection = mChartView
							.getCurrentSeriesAndPoint();

					if (selection != null) {

						AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
								getActivity())
								.setTitle("Balance Score")
								.setCancelable(true)
								.setMessage(
										"Balance Number : " + selection.getValue());

						// create a new instance of DialogHelper, set the parameters
						// and display the dialog
						DialogStyleHelper box = new DialogStyleHelper(
								getActivity(), dlgAlert.create());
						box.setDialogButtonParams(null, -1, getActivity()
								.getResources().getColor(R.color.blue));
						box.showDialog();
					}
				}
			});
			
		// if the chartView has already been initialized
		} else {
			
			// re-render the graph
			mChartView.repaint();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mDatabaseHelper.close();
	}

	// creates and renders the time (line) graph (using the AChartEngine library)
	private void createTimeGraph() {

		// remove any existing series and renderers
		mDataset.clear();
		mRenderer.removeAllRenderers();

		/* LEFT ANKLE */

		// create a new series and renderer for left-ankle results
		TimeSeries leftResultsSeries = new TimeSeries(" Left Ankle ");
		XYSeriesRenderer leftSeriesRenderer = new XYSeriesRenderer();

		// set the local renderer properties (such as color and point style)
		leftSeriesRenderer.setColor(getResources().getColor(R.color.blue));
		leftSeriesRenderer.setPointStyle(PointStyle.CIRCLE);

		// populate the series and set common renderer parameters
		setLocalSeriesAndRenderer("Left", leftResultsSeries, leftSeriesRenderer);

		// add the series and renderer
		mDataset.addSeries(leftResultsSeries);
		mRenderer.addSeriesRenderer(leftSeriesRenderer);

		/* RIGHT ANKLE */

		// create a new series and renderer for right-ankle results
		TimeSeries rightResultsSeries = new TimeSeries(" Right Ankle ");
		XYSeriesRenderer rightSeriesRenderer = new XYSeriesRenderer();

		// set the local renderer properties (such as color and point style)
		rightSeriesRenderer.setColor(getResources().getColor(R.color.green));
		rightSeriesRenderer.setPointStyle(PointStyle.DIAMOND);

		// populate the series and set common renderer parameters
		setLocalSeriesAndRenderer("Right", rightResultsSeries,
				rightSeriesRenderer);

		// add the series and renderer
		mDataset.addSeries(rightResultsSeries);
		mRenderer.addSeriesRenderer(rightSeriesRenderer);

		// set the overall renderer parameters
		setOverallRendererParameters();
	}

	// given the ankleSide, local series and renderer, adds the data coordinates
	// and sets renderer parameters (depending on the type of graph)
	private void setLocalSeriesAndRenderer(String ankleSide,
			TimeSeries localSeries, XYSeriesRenderer localRenderer) {

		// iteration counter (x-coordinate)
		int xCoordCount = 1;
		Date xCoordDate = null;

		// y-coordinate
		double yCoord = -1;

		String sessionDateString = null;
		Date sessionDate = null;

		// get the threshold (target) date since when the results should be
		// displayed
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -(mXDays));
		cal.add(Calendar.MONTH, -(mXMonths));
		cal.add(Calendar.YEAR, -(mXYears));
		Date thresholdDate = cal.getTime();

		// the original date-string pattern used to store dates in the database
		String originalPattern = "yyyy-MM-dd";

		// fetch the user's sessions for that particular exercise
		// and ankle from the database.
		Cursor cur = mDatabaseHelper.getReadableDatabase().rawQuery(
				"SELECT meanR, date FROM sessions WHERE userId = ? "
						+ "AND exerciseId = ? AND ankleSide = ?",
				new String[] { String.valueOf(mUserId),
						String.valueOf(mExerciseId), ankleSide });

		if (cur != null && cur.moveToFirst()) {

			// results from the last X days
			if (mGraphType.equals(MODE_LAST_X_DAYS)) {

				// skip all the dates preceding the threshold date. use short-circuit 
				// evaluation to decide whether to increment the cursor position
				do {

					// retrieve the session date-string from the cursor
					sessionDateString = cur.getString(1);
					sessionDate = stringToDate(sessionDateString,
							originalPattern);

				} while (sessionDate.before(thresholdDate) && cur.moveToNext()
						&& !cur.isAfterLast());

				// at this point, the cursor position is either after the last entry or
				// at the entry whose date-stamp is after the threshold date.
				// iterate through the cursor entries, starting with the threshold date
				while (!cur.isAfterLast()) {

					// get the session's date-string from the cursor
					sessionDateString = cur.getString(1);
					xCoordDate = stringToDate(sessionDateString,
							originalPattern);

					/* for several sessions in one day, skip to the most recent
					 * result. note: the implemented logic is such that atleast
					 * one cursor move is guaranteed (in the first iteration,
					 * the date strings will be equal) */
					while(!cur.isAfterLast() && sessionDateString.equals(cur.getString(1))) {
						
						// store the meanR value
						yCoord = Math.round(cur.getDouble(0) * 100) / 100d;
						cur.moveToNext();
					}
					
					// add the x- and y- coordinates to the local series
					localSeries.add(xCoordDate, yCoord);
					
					// if the current y-coordinate is the largest, store it
					if (yCoord > maxYCoord) {
						maxYCoord = yCoord;
					}
				}

				// in 'Last X Days' mode and if the results are sufficiently 
				// spaced out, display the chart (point) values
				if(mTotalDaysOffset <= 6) {
					localRenderer.setDisplayChartValues(true);
				} else {
					localRenderer.setDisplayChartValues(false);
				}

			// results from ALL user sessions
			} else if (mGraphType.equals(MODE_ALL_TIME)) {

				xCoordCount = 1;
				
				// iterate through the entries, adding them to the local series
				while (!cur.isAfterLast()) {

					yCoord = Math.round(cur.getDouble(0) * 100) / 100d;
					localSeries.add(xCoordCount, yCoord);

					// if the current y-coordinate is the largest, store it
					if (yCoord > maxYCoord) {
						maxYCoord = yCoord;
					}

					xCoordCount++;
					cur.moveToNext();
				}

				// in 'Display all results' mode, hide the chart (point) values
				localRenderer.setDisplayChartValues(false);
				mRenderer.setXLabels(0); // do not show the axis labels

				// set the maximum x-coordinate to the integer after the last
				// iterated value (to space out the graph)
				if (xCoordCount > maxXCoord) {
					maxXCoord = xCoordCount;
				}
			}
		}

		// close the cursor
		cur.close();

		// set the local renderer properties
		localRenderer.setDisplayChartValuesDistance(0); // display all chart values
		localRenderer.setFillPoints(true);
		localRenderer.setChartValuesTextAlign(Align.CENTER);
		localRenderer.setChartValuesTextSize(24);
	}

	// sets the basic rendering parameters such as graph color, label size
	private void setOverallRendererParameters() {

		// set the overall rendering parameters
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.WHITE);

		// font size
		mRenderer.setAxisTitleTextSize(24);
		mRenderer.setLegendTextSize(26);
		mRenderer.setLabelsTextSize(26);

		// axes range
		mRenderer.setYAxisMin(0);
		mRenderer.setYAxisMax(GraphHelper.estimateUpperBound(
				new double[] {maxYCoord}));

		// if using a time-based scale
		if (mGraphType.equals(MODE_LAST_X_DAYS)) {
			
			// set the minimum x-value to (X+1) days before today (in ms)
			mRenderer.setXAxisMin(new Date().getTime() - (mTotalDaysOffset + 1) * 24 * 60
					* 60 * 1000l);

			// set the maximum x-value to the current date, plus one day (in ms)
			mRenderer.setXAxisMax(new Date().getTime() + 24 * 60 * 60 * 1000);

		// if not using a time-based scale (x-axis values are numbers as
		// opposed to dates)
		} else {
			mRenderer.setXAxisMin(0);
			mRenderer.setXAxisMax(maxXCoord);
		}

		// margin size and color
		mRenderer.setMargins(new int[] { 40, 60, 50, 10 });
		mRenderer.setMarginsColor(Color.WHITE);

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
		mRenderer.setPointSize(8);
		mRenderer.setShowGrid(true);
		mRenderer.setAxesColor(Color.DKGRAY);
		
		// based on the spacing of the time intervals, set the display pattern
		if(mXYears >= 1) {
			mDisplayDateFormat = "MM-yyyy";
		}
	}

	// given a date-string, parses it using the parameterized date-pattern and
	// returns the corresponding date as a date object.
	private Date stringToDate(String dateString, String originalPattern) {

		Date finalDate = null;

		// create a new simpleDateFormat object using the specified format
		SimpleDateFormat originalFormat = new SimpleDateFormat(originalPattern,
				Locale.ENGLISH);

		try {
			// create the date object
			finalDate = originalFormat.parse(dateString);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return finalDate;
	}
	
	// set the number of days, months and years to offset by (from today's date)
	private void setTimeOffset(int totalDaysOffset) {
		
		mXYears = (int) (totalDaysOffset / 365);
		mXMonths = (int) ((totalDaysOffset - mXYears * 365) / 30);
		mXDays = (int) (totalDaysOffset - (mXMonths * 30) - (mXYears * 365));
	}
}
