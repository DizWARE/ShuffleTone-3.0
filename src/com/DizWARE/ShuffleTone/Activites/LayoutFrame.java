package com.DizWARE.ShuffleTone.Activites;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;

import com.DizWARE.ShuffleTone.R;

/***
 * Layout frame that is specifically made for the main activities of this application
 * 
 * This injects the layout, gui_frame, with a "content" view, "title" view, and a drawable for the 
 * drawer handle
 * 
 * @author Tyler Robinson
 *
 */
public class LayoutFrame 
{
	/***
	 * Creates a layout using the "gui_frame" layout, which has a title bar layout, a content layout, and 
	 * a sliding drawer
	 * 
	 * @param inflater - Just a LayoutInflater that will inflate the "gui_frame" layout
	 * @param content - View that should represent the look and feel of our activity
	 * @param title - View that should represent the look and feel of the title bar
	 * @param handleId - Drawable ID for the handle of our sliding drawer
	 * 
	 * @return -  The "gui_frame" layout after all the injections
	 */
	public static RelativeLayout createFrame(LayoutInflater inflater, View content, View title, int handleId)
	{
		RelativeLayout rl_frame = (RelativeLayout)inflater.inflate(R.layout.gui_frame, null);
		
		ImageButton ib_handle = (ImageButton)((SlidingDrawer)rl_frame.findViewById(R.id.sd_options)).getHandle();
		ib_handle.setBackgroundResource(handleId);
		
		addView((FrameLayout)rl_frame.findViewById(R.id.fl_content), content);
		addView((FrameLayout)rl_frame.findViewById(R.id.fl_titlebar), title);
		
		return rl_frame;
	}
	
	
	/***
	 * TODO - Do I need this???????????
	 */
	public static RelativeLayout createFrame(LayoutInflater inflater, int contentId, int titleId, int handleId)
	{
		RelativeLayout rl_frame = (RelativeLayout)inflater.inflate(R.layout.gui_frame, null);
		RelativeLayout rl_content = (RelativeLayout)inflater.inflate(contentId, null);
		RelativeLayout rl_title = (RelativeLayout)inflater.inflate(titleId, null);
		
		ImageButton ib_handle = (ImageButton)((SlidingDrawer)rl_frame.findViewById(R.id.sd_options)).getContent();
		ib_handle.setBackgroundResource(handleId);
		
		addView((FrameLayout)rl_frame.findViewById(R.id.fl_content), rl_content);
		addView((FrameLayout)rl_frame.findViewById(R.id.fl_titlebar), rl_title);
		
		return rl_frame;
	}
	
	/***
	 * Replaces the stuff that is in the current frame layout with the given content
	 * 
	 * ***Basically this is the Injection process***
	 * 
	 * @param contentPosition - Where the content should be injected
	 * @param content - View we want in this location
	 */
	private static void addView(FrameLayout contentPosition, View content)
	{
		contentPosition.removeAllViews();
		contentPosition.addView(content);
	}
}
