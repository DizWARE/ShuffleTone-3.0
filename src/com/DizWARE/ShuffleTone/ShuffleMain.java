package com.DizWARE.ShuffleTone;

import com.DizWARE.ShuffleTone.Others.PreferenceWriter;
import com.DizWARE.ShuffleTone.Others.ResetAlarms;
import com.DizWARE.ShuffleTone.Others.Shuffler;
import com.DizWARE.ShuffleTone.Others.SwipeControls;
import com.DizWARE.ShuffleTone.Others.UserDialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

/***
 * This is the main activity of the app. Allows for setting the 
 * settings of the app and gives access to all the other activities
 * @author Tydiz
 *
 */
public class ShuffleMain extends Activity 
	implements OnSeekBarChangeListener {
    /** Called when the activity is first created. */
	
	final int DONATE = 222;
	final int REMOVE_DONATE = 223;
	
	SlidingDrawer sd_settings;
	SharedPreferences settings;
	
	ToggleButton btn_onOff;
	ToggleButton btn_calls;
	ToggleButton btn_hours;
	ToggleButton btn_delay;
	
	Button btn_filebrowse;
	Button btn_save;
	Button btn_instruct;
	Button btn_faq;
	Button btn_email;
	
	SeekBar sb_calls;
	SeekBar sb_hours;
	SeekBar sb_delay;
	
	TextView tv_delay;
	TextView tv_hours;
	TextView tv_calls;
	TextView tv_mode;
	TextView tv_on;
	TextView lbl_calls;
	TextView tv_switcher;
	TextView tv_donate;
	
	RelativeLayout rl_main;
	RelativeLayout rl_delay;
	RelativeLayout rl_settings;
	
	LinearLayout ll_donate;
	
	TextView[] labels;
	
	View[] viewCopy;
	
	public String writeCode;
	String type = "call";
	boolean canSwitch = true;
	
	GestureDetector gestureDetector;
	
	/***
	 * Creates the GUI elements of the main activity
	 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);   
        
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse
				("file://" + Environment.getExternalStorageDirectory())));
       
		
		
        //To differ between saving modes, without control blocks, this will be used
        	//to add a tag to the beginning of any preference saves
        writeCode = "";
        
        /***
         * Event handler for clicking; handles the calls/hours setting controller
         */
        OnClickListener hourControl = new OnClickListener()
        {
        	/***
        	 * Click handler for the calls/hours setting controller
        	 */
			@Override
			public void onClick(View v) 
			{
				boolean current = btn_hours.isChecked();
				if(!v.equals(btn_hours))
					current = !current;
				
				if(current)
					enableSlider(btn_hours);
				else
					enableSlider(btn_calls);
				
				turnOnOff(btn_hours,current);
				turnOnOff(btn_calls,!current);
			}        	
        };
        
        //Gets the settings preference
        settings = this.getSharedPreferences("settings", 0); 
        
        gestureDetector = new GestureDetector(new SwipeControls(this));

        //Opens tutorial and welcome screen
        if(!settings.contains("hasRun"))
        {
        	UserDialogs.newUser(this);
        	PreferenceWriter.booleanWriter(settings, "hasRun", true);	
        }
        
        //Pops up with version update info
        if(!settings.getString("version", "0").equalsIgnoreCase(this.getString(R.string.version)))
        {
        	UserDialogs.whatsNew(this);
        	PreferenceWriter.stringWriter(settings, "version", this.getString(R.string.version));
        }
        	
        
        //****GUI JUNK***\\
        sd_settings = (SlidingDrawer)this.findViewById(R.id.settings);         
        btn_onOff = (ToggleButton)findViewById(R.id.btn_onoff);
        btn_hours = (ToggleButton)findViewById(R.id.btn_hours);
        btn_calls = (ToggleButton)findViewById(R.id.btn_calls);   
        btn_delay = (ToggleButton)findViewById(R.id.btn_delay);
        
        btn_filebrowse = (Button)findViewById(R.id.btn_filebrowse);
        btn_save = (Button)findViewById(R.id.btn_save);
        
        btn_instruct = (Button)findViewById(R.id.btn_instruct); 
        btn_faq = (Button)findViewById(R.id.btn_faq);
        btn_email = (Button)findViewById(R.id.btn_email);
        
        
        sb_calls = (SeekBar)findViewById(R.id.sb_calls);
        sb_hours = (SeekBar)findViewById(R.id.sb_hours);      
        sb_delay = (SeekBar)findViewById(R.id.sb_delay);

        tv_hours = (TextView)findViewById(R.id.tv_numHours);
        tv_calls = (TextView)findViewById(R.id.tv_numCalls);
        tv_mode = (TextView)findViewById(R.id.lbl_mode);
        tv_on = (TextView)findViewById(R.id.lbl_onoff);
        tv_switcher = (TextView)findViewById(R.id.tv_switcher);
        tv_delay = (TextView)findViewById(R.id.tv_delay);
        tv_donate = (TextView)findViewById(R.id.tv_donate);
        
        labels = new TextView[4];
        labels[0] = (TextView)findViewById(R.id.lbl_onoff);
        labels[1] = (TextView)findViewById(R.id.lbl_filebrowse);
        labels[2] = (TextView)findViewById(R.id.lbl_saveload);
        labels[3] = (TextView)findViewById(R.id.lbl_mode);
        
        
        lbl_calls = (TextView)findViewById(R.id.tv_calls);
        
        sb_hours.setOnSeekBarChangeListener(this);
        sb_calls.setOnSeekBarChangeListener(this);       
        sb_delay.setOnSeekBarChangeListener(this);
        
        rl_main = (RelativeLayout)findViewById(R.id.mainLayout);
        rl_delay = (RelativeLayout)findViewById(R.id.rl_delay);
        rl_settings = (RelativeLayout)findViewById(R.id.rl_settings);
        
        ll_donate =(LinearLayout)findViewById(R.id.ll_donate);
        
        if(settings.getBoolean("hideNag", false))
        {
        	removeNag();
        }
               
        viewCopy = new View[rl_delay.getChildCount()];
        
        for(int i = 0; i < viewCopy.length;i++)
        	viewCopy[i] = rl_delay.getChildAt(i);
        
        rl_delay.removeAllViews();
        
        btn_hours.setOnClickListener(hourControl);
        btn_calls.setOnClickListener(hourControl);
        
        tv_switcher.setText("Swipe to the Right\nto Switch Modes");
        
        /*************EMBEDED LISTENER CLASSES**************/
        
        /***
         * Handles when the user turns off text tone playback stopper
         */
        btn_delay.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				ToggleButton tb = (ToggleButton)v;
				turnOnOff(tb, tb.isChecked());				
			}        	
        });
        
        /***
         * Opens instruction dialog
         */
        btn_instruct.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{ UserDialogs.instructions(ShuffleMain.this); }        	
        });
                
        /***
         * Opens the FAQ website
         */
        btn_faq.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				String url = "http://dizware.blogspot.com/2010/01/shuffletone-20-refresh-faq.html";
				Intent i = new Intent(Intent.ACTION_CHOOSER);
				Intent data = new Intent(Intent.ACTION_VIEW);
				data.setData(Uri.parse(url));
				data.addCategory(Intent.CATEGORY_BROWSABLE);
				i.putExtra(Intent.EXTRA_INTENT,data);
				
				startActivity(Intent.createChooser(i, "View website..."));
				
			}
        });
        
        /***
         * Opens a new email to me :)
         */
        btn_email.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				    /* Create the Intent */  
				    final Intent emailIntent = new Intent();  
				     
				   /* Fill it with Data */  
				   emailIntent.setType("plain/text");  
				   emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"tydiz936@gmail.com"});  
				   emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ShuffleTone 2.0 Refresh");  
				  
				   /* Send it off to the Activity-Chooser */  
				   startActivity(Intent.createChooser(emailIntent, "Send mail..."));  
				
			}
        	
        });
        
        /***
         * Opens the saving activity
         */
        btn_save.setOnClickListener(new OnClickListener()
        {

			@Override
			public void onClick(View v) 
			{
				Intent intent = new Intent(ShuffleMain.this,SaveActivity.class);
				intent.putExtra("writeCode", writeCode);
				
				startActivity(intent);
			}
        	
        });
        
    	/***
    	 * Click listener that turns on the service
    	 */
        btn_onOff.setOnClickListener(new OnClickListener()
        {
        	/***
        	 * Click listener that turns on the service
        	 */
			@Override
			public void onClick(View v) 
			{
				ToggleButton btn = (ToggleButton)v;
				ShuffleMain.this.turnOnOff(btn, writeCode + "power");
				if(btn.isChecked())
					Shuffler.runShuffle(ShuffleMain.this, writeCode, settings.getString(writeCode+"list", "").split("/"));
			}        	
        });
        
        /***
         * Handles when the user presses the Browse Files button
         */
        btn_filebrowse.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{				
				Intent intent = new Intent(ShuffleMain.this,FileBrowser.class);
				intent.putExtra("writeCode", writeCode);
				
				startActivity(intent);
			}
        	
        });
        
        /***
    	 * Action that happens when drawer is opened
    	 */
        sd_settings.setOnDrawerOpenListener(new OnDrawerOpenListener()
        {
			@Override
			public void onDrawerOpened() 
			{				
				SharedPreferences prefs = settings;
				
				//Gets all the settings we need for this display
				int numOfCalls = prefs.getInt(writeCode + "numCalls", 0);
				int numOfHours = prefs.getInt(writeCode + "numHours", 0);
				int delay = prefs.getInt("smsdelay", 29);
				
				boolean useHours = prefs.getBoolean(writeCode + "useHours", false);				
				boolean useDelay = prefs.getBoolean("smsstop", false);
				
				canSwitch = false;				

				//If we are in SMS settings and if our time selector
					//isn't up, add it in
				if(writeCode.equalsIgnoreCase("sms")&&rl_delay.getChildCount() == 0)
					for(int i = 0; i < viewCopy.length;i++)
			        	rl_delay.addView(viewCopy[i]);
				
				lbl_calls.setText("Shuffle per " + type);
				
				tv_calls.setText("Number of " + type + "s until I shuffle: " + (numOfCalls +1));
				
				sb_calls.setProgress(numOfCalls);				
				sb_hours.setProgress(numOfHours);				
				
				turnOnOff(btn_calls, !useHours);
				turnOnOff(btn_hours, useHours);		
				
				if(writeCode.equalsIgnoreCase("sms"))
				{
					sb_delay.setProgress(delay);
					turnOnOff(btn_delay, useDelay);
				}
				
				if(useHours)
					enableSlider(btn_hours);
				else
					enableSlider(btn_calls);
				
				for(int i = 0; i < rl_main.getChildCount(); i++)
					rl_main.getChildAt(i).setEnabled(false);			
			}        	
        });
        
        sd_settings.setOnDrawerCloseListener(new OnDrawerCloseListener()
        {
        	/***
        	 * Action that happens when the drawer is closed
        	 */
			@Override
			public void onDrawerClosed() 
			{
				PreferenceWriter.booleanWriter(settings,writeCode + "useHours", btn_hours.isChecked());
				PreferenceWriter.intWriter(settings,writeCode + "numCalls", sb_calls.getProgress());
				PreferenceWriter.intWriter(settings,writeCode + "numHours", sb_hours.getProgress());

				//Saves delay info
				if(writeCode.equalsIgnoreCase("sms"))
				{
					PreferenceWriter.booleanWriter(settings, "smsstop", btn_delay.isChecked());
					PreferenceWriter.intWriter(settings, "smsdelay", sb_delay.getProgress());
				}
				
				//Sets an alarm if the user wants to shuffle by time
				if(btn_hours.isChecked())
					if(writeCode.equals(""))
						ResetAlarms.SetAlarms(ShuffleMain.this, 
								new Intent(), 0);
					else
						ResetAlarms.SetAlarms(ShuffleMain.this, 
								new Intent(), 1);
				//cancels the alarm for when they switch off alarm
				else
					if(writeCode.equals(""))
							ResetAlarms.cancelAlarm(ShuffleMain.this, 
									new Intent(), 0);
					else
						ResetAlarms.cancelAlarm(ShuffleMain.this, 
								new Intent(), 1);
				
				//Removes GUI from delay section so that it doesn't show up in call settings
				if(writeCode.equalsIgnoreCase("sms"))
					rl_delay.removeAllViews();
				
				canSwitch = true;
				
				for(int i = 0; i < rl_main.getChildCount(); i++)
					rl_main.getChildAt(i).setEnabled(true);			
			}
        	
        });
        
        
    }
    
    /***
     * Loads all of the GUI info
     */
    @Override
    protected void onStart() 
    {
    	turnOnOff(btn_onOff, settings.getBoolean(writeCode + "power", false));  
    	super.onStart();
    }
    
    /***
     * Closes the setting pane before pausing the app
     */
    @Override
    protected void onPause() 
    {
    	sd_settings.close();
    	PreferenceWriter.booleanWriter(settings, writeCode + "power", btn_onOff.isChecked());
    	super.onPause();
    }
    
    /***
     * Turns on/off the given toggle button; Stores change in preferences
     * @param btn - The button we are toggling
     * @param valueName - Name of the reference name for saving the switch preference
     */
    public void turnOnOff(ToggleButton btn, String valueName)
    {
		if(!btn.isChecked())
		{
			btn.setBackgroundResource(R.drawable.offswitch);
			PreferenceWriter.booleanWriter(settings,valueName, false);
		}
		else
		{
			btn.setBackgroundResource(R.drawable.onswitch);
			PreferenceWriter.booleanWriter(settings,valueName, true);
		}
    }
    
    /***
     * Turns on/off the given toggle button; Uses a given boolean to set the state
     * @param btn - Button we are toggling
     * @param check - boolean that represents if the button is being turned on or off
     */
    public void turnOnOff(ToggleButton btn, boolean check)
    {
		if(check)
			btn.setBackgroundResource(R.drawable.onswitch);
		else
			btn.setBackgroundResource(R.drawable.offswitch);
			
		btn.setChecked(check);
    } 
    
    /***
     * Switches between the two different setting modes in the app
     */
    public void switchModes()
    {
    	if(!canSwitch)
    		return;
    	
		turnOnOff(btn_onOff,writeCode + "power");
		
		if(writeCode == "")
		{
			//Sets up the Text Setting GUI
			tv_mode.setText("Current Mode: Text Settings");
			tv_switcher.setBackgroundResource(R.drawable.swipeleft);
			
			tv_switcher.setText("Swipe to the Left\nto Switch Modes");
			tv_switcher.setGravity(Gravity.RIGHT);
			tv_switcher.setPadding(30, 5, 10, 0);
			
			type = "text";
			tv_on.setText("Turn On/Off TextTone Shuffle");			
			writeCode = "sms";
		}
		else
		{
			//Sets up the Call Settings GUI
			tv_mode.setText("Current Mode: Call Settings");
			tv_switcher.setBackgroundResource(R.drawable.swiperight);
			writeCode = "";
			
			tv_switcher.setGravity(Gravity.LEFT);
			tv_switcher.setText("Swipe to the Right\nto Switch Modes");
			tv_switcher.setPadding(0, 5, 10, 0);
			
			tv_on.setText("Turn On/Off ShuffleTone");
			type = "call";
		}
		
		turnOnOff(btn_onOff, settings.getBoolean(writeCode + "power", false)); 
    }
    
    /***
     * Handles when the type of shuffle switches(Between hours and calls)
     * 
     * @param v - Button that was clicked
     */
    public void enableSlider(View v)
    {
    	if(v.equals(btn_hours))
    	{
    		sb_hours.setEnabled(true);
    		sb_calls.setEnabled(false);
    		sb_hours.setFocusable(true);
    		sb_calls.setFocusable(false);
    	}
    	else
    	{
    		sb_hours.setEnabled(false);
    		sb_calls.setEnabled(true);
    		sb_hours.setFocusable(false);
    		sb_calls.setFocusable(true);
    	}
    }

    /***
     * Handles when the slider changes progress
     */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) 
	{   	 
		
		//Hours
		if(seekBar.equals(sb_hours))
		{
			String hours = "Number of hours until I shuffle: " + (progress+1);
			tv_hours.setText(hours);
		}
		//Calls
		else if(seekBar.equals(sb_calls))
		{
			String calls = "Number of " + type + "s until I shuffle: " + (progress+1);
			tv_calls.setText(calls);	
		}
		//Delay
		else if(seekBar.equals(sb_delay))
		{
			String delay = "Stop ringtone after " + (progress+1)+ " seconds";
			tv_delay.setText(delay);
		}
				
	}
	
	/***
	 * Creates the MENU Options when the user presses MENU
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuItem mi = menu.add(Menu.NONE, this.DONATE, 1, "DONATE");
		mi.setIcon(android.R.drawable.btn_star_big_on);
			
		if(!settings.getBoolean("hideNag", false))
		{
			mi = menu.add(Menu.NONE, this.REMOVE_DONATE, 0, "Remove NAG");
			mi.setIcon(android.R.drawable.ic_delete);
		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	/***
	 * Handles when the user clicks on the Donate button or the Remove Nag button
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		//Takes user to the website to make a donation
		if(item.getItemId() == this.DONATE)
		{
			String url = "http://dizware.blogspot.com/2010/01/donations.html";
			Intent i = new Intent(Intent.ACTION_CHOOSER);
			Intent data = new Intent(Intent.ACTION_VIEW);
			data.setData(Uri.parse(url));
			data.addCategory(Intent.CATEGORY_BROWSABLE);
			i.putExtra(Intent.EXTRA_INTENT,data);
			
			startActivity(Intent.createChooser(i, "View website..."));
		}
		
		removeNag();
		PreferenceWriter.booleanWriter(settings, "hideNag", true);
		return super.onOptionsItemSelected(item);
	}
	
	/***
	 * Removes the NAG TV and reformats the view
	 */
	public void removeNag()
	{
		ll_donate.removeView(tv_donate);
		ll_donate.setPadding(0, 0, 0, 15);
		
		for(int i = 0; i < labels.length; i++)
		{
			labels[i].setTextSize(20);
			labels[i].setPadding(0, 10, 0, 0);
		}
	}
	
	/***
	 * Handles when the user swipes the screen
	 */
    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
    	if(gestureDetector.onTouchEvent(event))
    		return true;
    	
    	return false;
    }

	//Unneeded Event handlers
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar){}	
}