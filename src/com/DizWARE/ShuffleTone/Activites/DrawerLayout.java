package com.DizWARE.ShuffleTone.Activites;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.DizWARE.ShuffleTone.R;

/***
 * Creates an injectable drawer. 
 * 
 * Requirements: A sliding drawer with a FrameLayout as the content with 2 FrameLayouts
 * within that have the IDs: fl_titlebar and fl_content
 * 
 * TODO - Is this necessarily requiring a drawer? Technically not. Some preconditions
 * should be changed to allow more than just the Drawer content layout
 * 
 * @author Tyler Robinson
 */
public class DrawerLayout 
{
	LayoutInflater inflater;
	FrameLayout title;
	FrameLayout content;
	
	/***
	 * Constructs the drawer
	 * 
	 * @param inflater - Inflates the layout
	 * @param parent - The frame layout that is set to be the content view in 
	 * the drawer
	 */
	public DrawerLayout(LayoutInflater inflater, FrameLayout parent) {
		this.inflater = inflater;
		RelativeLayout rl_frame = (RelativeLayout)this.inflater.inflate(R.layout.drawer_frame, null);
		rl_frame.setBackgroundResource(R.drawable.background);		
		title = (FrameLayout)rl_frame.findViewById(R.id.fl_drawer_titlebar);
		content = (FrameLayout)rl_frame.findViewById(R.id.fl_content);
		parent.addView(rl_frame);
	}
	
	/***
	 * Sets the title bar for the drawer(Not Necessary)
	 * 
	 * ADD ANY LISTENERS TO VIEW BEFORE ADDING INTO CONTENT
	 * 
	 * @param view - View to be put in the title space
	 */
	public void setTitle(View view)
	{
		title.removeAllViews();
		title.addView(view);
	}
	
	/***
	 * Sets the title to a layoutID incase the view hasn't been inflated yet(Not Necessary)
	 * 
	 * NO LISTENERS WILL BE ADDED HERE. INFLATE YOURSELF AND CALL setTitle(View)
	 * TO ADD CUSTOM LISTENERS TO YOUR LAYOUT
	 * 
	 * @param layoutId - ID for the layout that will be used as the title
	 */
	public void setTitle(int layoutId)
	{
		setTitle(inflater.inflate(layoutId, null));
	}
	
	/***
	 * Sets the given view as the content of the slider drawer when it is open
	 * 
	 * ADD ANY LISTENERS TO VIEW BEFORE ADDING INTO CONTENT
	 * 
	 * @param view - View to be used as the content of the drawer
	 */
	public void setContent(View view)
	{
		content.removeAllViews();
		content.addView(view);
	}
	
	/***
	 * Inflates the given layout and sets it as the content view
	 * 
	 * NO LISTENERS WILL BE ADDED HERE. INFLATE YOURSELF AND CALL setContent(View)
	 * TO ADD CUSTOM LISTENERS TO YOUR LAYOUT
	 * 
	 * @param layoutId - Layout to be inflated
	 */
	public void setContent(int layoutId)
	{
		setContent(inflater.inflate(layoutId, null));
	}
}
