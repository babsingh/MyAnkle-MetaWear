package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterResults extends ArrayAdapter<String[]> {
	
	// indices for types of data stored in the string arrays
	public static final int INDEX_DATE = 0;
	public static final int INDEX_RESULT = 1;
	
	public static final String TYPE_INJURY = "Ankle Injury";
	
	private Context mContext;
	private int mLayoutResource;
	private ArrayList<String[]> mResultsList;
	
	public ListAdapterResults(Context context, int resource, ArrayList<String[]> results) {
		super(context, resource, results);
		
		this.mContext = context;
		this.mLayoutResource = resource;
		this.mResultsList = results;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View row = convertView;
		ViewHolder viewHolder = null;
		
		int colorTextResult;
		
		if(row == null) {
			
			// get the layout inflater
			LayoutInflater inflater = LayoutInflater.from(mContext);
			
			// inflate a child container from the resource file
			row = inflater.inflate(mLayoutResource, null);
			
			/* create an instance of the ViewHolder and populate it with the
			 * child views. link (cache) the child UI elements to the holder object */
			viewHolder = new ViewHolder();
			viewHolder.textDate = (TextView) row.findViewById(R.id.layout_results_list_row_text_date);
			viewHolder.textResult = (TextView) row.findViewById(R.id.layout_results_list_row_text_result);
			
			row.setTag(viewHolder);
			
		} else {
			
			// get the cached view associated with the holder object
			viewHolder = (ViewHolder) row.getTag();
		}
		
		// retrieve the the result data
		String[] resultData = mResultsList.get(position);
		
		/* if the row data corresponds to an injury, set the text color to red
		 * else, reset the color to the default value (black) */
		if(resultData[INDEX_RESULT].equals(TYPE_INJURY)) {
			colorTextResult = Color.RED;
		} else {
			colorTextResult = Color.BLACK;
		}
		
		/* set the result (date and balance number/injury) for the 
		 * corresponding text-views */
		viewHolder.textDate.setText(resultData[INDEX_DATE]);
		viewHolder.textDate.setTextColor(colorTextResult);
		
		viewHolder.textResult.setText(resultData[INDEX_RESULT]);
		viewHolder.textResult.setTextColor(colorTextResult);
		
		return row;
	}
	
	/* the adapter implements the ViewHolder pattern to reduce overheads resulting
	 * from expensive findViewById() calls. the child views (layout objects) are 
	 * cached, resulting in faster rendering of the list */
	private static class ViewHolder
    {
        TextView textDate;
        TextView textResult;
    }
}