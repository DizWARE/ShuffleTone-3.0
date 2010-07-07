package com.DizWARE.ShuffleTone;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

/***
 * This class handles a left and right swipe. In this case, it actually
 * swipes between Shuffle Modes(Calls/SMS)
 * 
 * @author Tydiz
 *
 */
public class SwipeControls extends SimpleOnGestureListener 
{
	boolean sms;
	ShuffleMain activity;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	/***
	 * Creates a control to swipe between ShuffleTone and TextTone Shuffle
	 * @param activity - the calling activity
	 */
	public SwipeControls(ShuffleMain activity)
	{
		this.activity = activity;
	}
	
	/***
	 * Called when you fling your finger across the calling activity. Switches modes
	 * in the main app when done
	 */
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	{
		//TODO - Fix the NullPointer crash here. May be a result of a null activity
		if((activity.writeCode.equalsIgnoreCase("sms") && e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && 
				Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) || 
		   (activity.writeCode.equalsIgnoreCase("") && e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && 
				Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY))
		{
			activity.switchModes();
		}
		return true;
	}
}
