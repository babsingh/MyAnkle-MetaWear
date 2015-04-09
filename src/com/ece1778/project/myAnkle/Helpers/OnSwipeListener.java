package com.ece1778.project.myAnkle.Helpers;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeListener implements OnTouchListener {

	private final GestureDetector gestureDetector;

	public OnSwipeListener(Context context) {
		
		// initialize the GestureDetector object
		gestureDetector = new GestureDetector(context, new GestureListener());
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return gestureDetector.onTouchEvent(event);
	}

	private final class GestureListener extends SimpleOnGestureListener {

		// the minimum distance and velocity values for an event 
		// callback to be triggered
		private static final int SWIPE_DISTANCE_THRESHOLD = 40;
		private static final int SWIPE_VELOCITY_THRESHOLD = 100;

		// return true to indicate that the gesture (event) is to be consumed.
		// note: do not override the implemented functionality unless necessary
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			try {
				float distanceX = e2.getX() - e1.getX();
				float distanceY = e2.getY() - e1.getY();

				// swipe event is more prominent in the x-direction
				if (Math.abs(distanceX) > Math.abs(distanceY)) {

					if (Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD
							&& Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

						// swipe was from left to right
						if (distanceX > 0) {
							onSwipeRight();

						// swipe was from right to left
						} else {
							onSwipeLeft();
						}
					}
				
				// swipe event is more prominent in the y-direction
				} else {

					if (Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD
							&& Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {

						// swipe was from top to bottom
						if (distanceY > 0) {
							onSwipeDown();

						// swipe was from bottom to top
						} else {
							onSwipeUp();
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return false;
		}
	}
	
	/* methods called when the corresponding event is triggered. implement the 
	   required functionality by overriding these methods in the desired classes */
	
	public void onSwipeRight() {
	}
	
	public void onSwipeLeft() {
	}
	
	public void onSwipeDown() {
	}
	
	public void onSwipeUp() {
	}
}
