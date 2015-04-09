package com.ece1778.project.myAnkle;

import com.ece1778.project.myAnkleUser.R;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class ListAdapterExercises extends CursorAdapter {
	public static final String TAG = ListAdapterExercises.class.getSimpleName();
	
	// stores a map between the list row and the database exercise id number
	public SparseIntArray mRowDbMap = new SparseIntArray();
	
	// special cache for fast icon loading
	// make static so same warmed cache is used each time we bring up the exercise list
	private static LruCache<String, Bitmap> mMemoryCache = createCache();
	
	// the parent activity context
	private Context mContext;
	
	ListAdapterExercises(Context context, Cursor cursor) {
		super(context, cursor, false);
		mContext = context;
	}

	private static LruCache<String, Bitmap> createCache() {
		// Setup the Bitmap cache stuff
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 4;
		
		LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
					return bitmap.getByteCount() / 1024;
				}
			};
		return memoryCache;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		// get the layout inflater
		LayoutInflater inflater = LayoutInflater.from(context);
		
		// use it to inflate one row of the exercise list 
		View row = inflater.inflate(R.layout.layout_exercise_list_row, null);
		
		/* create an instance of the ViewHolder and populate it with the
		 * child views. link (cache) the child UI elements to the holder object */
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.imageExerciseIcon = (ImageView) row.findViewById(R.id.layout_exercise_list_row_imageview_icon);
		viewHolder.textExerciseName = (TextView) row.findViewById(R.id.layout_exercise_list_row_textview_name);
		viewHolder.textExerciseEquipment = (TextView) row.findViewById(R.id.layout_exercise_list_row_textview_equipment);
		viewHolder.textExerciseEyeState = (TextView) row.findViewById(R.id.layout_exercise_list_row_textview_eye_state);
		viewHolder.textExerciseDifficulty = (TextView) row.findViewById(R.id.layout_exercise_list_row_textview_difficulty);
		
		row.setTag(viewHolder);
		
		// return the newly inflated row
		return (row);
	}
	
	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		
		// store database results so when a row is "clicked" we know what data to send to next activity	
		mRowDbMap.put(cursor.getPosition(), cursor.getInt(0));
		
		// get the exercise icon label from db and load drawable into icon imageview				
		int id = getStringIdentifier(mContext, cursor.getString(1));
		
		// get the size we want each icon to
		int iconSize = (int) ( context.getResources().getDimension(R.dimen.exercise_icon_size) );
		
		// get the instance of the ViewHolder associated with the parameterized row
		ViewHolder viewHolder = (ViewHolder) row.getTag();
		
		// either load image from cache or insert into and load
		loadBitmap(id, viewHolder.imageExerciseIcon, iconSize);
		
		// load the exercise details into row textviews
		viewHolder.textExerciseName.setText(cursor.getString(2));
		viewHolder.textExerciseEquipment.setText(cursor.getString(3));
		viewHolder.textExerciseEyeState.setText("Eyes " + cursor.getString(4));
		viewHolder.textExerciseDifficulty.setText(cursor.getString(5));
	}

	public int getStringIdentifier(Context context, String name) {
		return context.getResources().getIdentifier(name, "drawable",
				context.getPackageName());
	}
	
	// loads exercise row bitmaps asynchronously, maintains coherence, updates image cache 
	// and cancels outstanding request of the user scrolled past too fast.
	public void loadBitmap(int resId, ImageView imageView,int iconSize) {
	    if (cancelPotentialWork(resId, imageView)) {
	        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	        
	        Bitmap mPlaceHolderBitmap = null;
	        
	        final AsyncDrawable asyncDrawable =
	                new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
	        
	        imageView.setImageDrawable(asyncDrawable);
	        
	        task.execute(resId,iconSize);
	    }
	}
	
	/* the adapter implements the ViewHolder pattern to reduce overheads resulting
	 * from expensive findViewById() calls. the child views (layout objects) are 
	 * cached, resulting in faster rendering of the list */
	private static class ViewHolder
    {
		ImageView imageExerciseIcon;
        TextView textExerciseName;
        TextView textExerciseEquipment;
        TextView textExerciseEyeState;
        TextView textExerciseDifficulty;
    }
	
	/* ************************************************
	 * Bitmap processing methods and cache manipulation  
	 * ************************************************ */
	
	// asynctask checks image caches, decodes image resource if necessary and populates appropriate imageview
	private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap>{

		private final WeakReference<ImageView> imageViewReference;
		private int resId = 0;
		private int iconSize;
		
		
	    public BitmapWorkerTask(ImageView imageView) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
	    }
	    
		@Override
		protected Bitmap doInBackground(Integer... params) {
			resId = params[0];
			iconSize = params[1];
			final String imageKey = String.valueOf(resId);
			
			// if the image is in the cache use it, otherwise decode from resource and append the cache
			final Bitmap bitmap = getBitmapFromMemCache(imageKey);
				if (bitmap != null) {
			        return bitmap;
			    } else {
			    	Bitmap decodedBitmap = decodeSampledBitmapFromResource(mContext.getResources(), resId, iconSize, iconSize);					
			    	addBitmapToMemoryCache(imageKey, decodedBitmap);
					return decodedBitmap;			    	
			    }		
		}
		
		// when decode is complete, if the current view still exists set the image
		@Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (isCancelled()) {
	            bitmap = null;
	        }

	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
	            if (this == bitmapWorkerTask && imageView != null) {
	                imageView.setImageBitmap(bitmap);
	            }
	        }
	    }		
	}
	
	// class used to keep a mapping from an ImageView and the worker thread decoding the image
	static class AsyncDrawable extends BitmapDrawable {
	    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	    public AsyncDrawable(Resources res, Bitmap bitmap,
	            BitmapWorkerTask bitmapWorkerTask) {
	        super(res, bitmap);
	        bitmapWorkerTaskReference =
	            new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	    }

	    public BitmapWorkerTask getBitmapWorkerTask() {
	        return bitmapWorkerTaskReference.get();
	    }
	}
	
	// if an ImageView already has an outstanding request to load the same image don't spawn a new decode process
	public static boolean cancelPotentialWork(int data, ImageView imageView) {
		
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final int bitmapData = bitmapWorkerTask.resId;
	        if (bitmapData != data) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	
	// helper function to return a reference to the aynctask that processed the image for this view
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
		    if (drawable instanceof AsyncDrawable) {
		    	final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
		        return asyncDrawable.getBitmapWorkerTask();
		    }
		}
		return null;
	}
	
	// find the sizes of the scaled smaller bitmap we will use and store in the lruCache 
	public static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	
	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	
	    return inSampleSize;
	}
	
	// decode an image from android resource and scale to our desired icon size for the cache
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	// cache interaction helpers
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if(mMemoryCache != null){
			if(getBitmapFromMemCache(key) == null) {
				mMemoryCache.put(key, bitmap);
			}	
		}else{
			mMemoryCache = createCache();
		}
	}
	public Bitmap getBitmapFromMemCache(String key) {
		if(mMemoryCache != null)
			return mMemoryCache.get(key);
		else
			return null;		
	}
}