package com.DizWARE.ShuffleTone.Activites;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Activites.ListTypeChooser.OnOkClick;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.PlaylistIO;
import com.DizWARE.ShuffleTone.Others.PreferenceWriter;
import com.DizWARE.ShuffleTone.Services.ShuffleService;
import com.android.vending.billing.Base64DecoderException;

/***
 * Main Activity of the application. Portal to all the features in ShuffleTone
 * 
 * @author Tyler Robinson
 */
public class MainActivity extends Activity 
{
	TextView tv_power;
	TextView tv_backup;
	TextView tv_browser;
	TextView tv_viewCurrent;
	TextView tv_test;
	TextView tv_donation;
	
	RelativeLayout frameLayout;
	RelativeLayout rl_title;
	ScrollView sv_content;
	
	SlidingDrawer sd_main;	
	DrawerLayout drawer;
	
	ProgressDialog spinnerDialog;
	
	ViewListDialog mViewDialog;
	
	ListTypeChooser mChooser;
	
	AlertDialog mDialog;
	
	ViewListDialog.ViewListAdapter mViewAdapter;
	AppSettings appSettings;
	
	SharedPreferences settings;
	
	/***
	 * Creates an instance of this activity
	 */
	@Override protected void onCreate(Bundle savedInstanceState) {
		prepareFrame();
		this.setContentView(frameLayout);		
		
		//Prepare the spinning dialog box
		spinnerDialog = new ProgressDialog(this);
		spinnerDialog.setTitle("Please wait...");
		spinnerDialog.setMessage("Loading...");
		
		settings = this.getSharedPreferences("settings", 0);
		
		super.onCreate(savedInstanceState);
		
		//Dialog for first time users - Opportunity to learn from tutorial
		if(!settings.contains("ranOnce")||!settings.getBoolean("ranOnce", false))
		{
			PreferenceWriter.booleanWriter(settings, "ranOnce", true);
			
			Builder builder = new Builder(this);
			builder.setTitle("First Time?");
			builder.setMessage("Would you like to read the tutorial?");
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
			{
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					dialog.cancel();
					sd_main.animateOpen();
					drawer.setContent(appSettings.prepareInstructions());
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() 
			{
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					dialog.cancel();
				}
			});
			
			mDialog = builder.create();
			mDialog.show();
		}
	}
	
	/***
	 * Prepares the application frame with all the necessary pieces
	 */
	private void prepareFrame()
	{
		sv_content = (ScrollView)this.getLayoutInflater().inflate(R.layout.main_layout, null);		
		rl_title = new RelativeLayout(this);
		
		prepareContent();
		prepareTitle();
		
		frameLayout = LayoutFrame.createFrame(this.getLayoutInflater(), sv_content, rl_title, R.drawable.slidersettings);
		
		sd_main = (SlidingDrawer)frameLayout.findViewById(R.id.sd_options);		
		drawer = new DrawerLayout(this.getLayoutInflater(), (FrameLayout)sd_main.getContent());
		prepareDrawer();
	}
	
	/***
	 * Prepare this Activities content
	 */
	private void prepareContent()
	{		
		tv_power = (TextView)sv_content.findViewById(R.id.tv_powerStation);
		tv_backup =  (TextView)sv_content.findViewById(R.id.tv_backup);
		tv_browser = (TextView)sv_content.findViewById(R.id.tv_filebrowser);
		tv_viewCurrent = (TextView)sv_content.findViewById(R.id.tv_viewCurrent);
		tv_test = (TextView)sv_content.findViewById(R.id.tv_test);
		tv_donation = (TextView)sv_content.findViewById(R.id.tv_donation);
		
		//Set up and launch the power selection dialog
		tv_power.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View arg0) 
			{
				//Prepare the click handler
				OnOkClick click = new OnOkClick() {					
					@Override public boolean handleClick(boolean calls, boolean texts) {
						boolean runCalls = calls && !settings.getBoolean(Constants.SETTINGS_CALLS_PWR, false);
						boolean runTexts = texts && !settings.getBoolean(Constants.SETTINGS_TXT_PWR, false);
						PreferenceWriter.booleanWriter(settings, 
								Constants.SETTINGS_CALLS_PWR, calls);
						PreferenceWriter.booleanWriter(settings, 
								Constants.SETTINGS_TXT_PWR, texts);
						
						if(runCalls)
							ShuffleService.startService(MainActivity.this, false, Constants.TYPE_CALLS);
						if(runTexts)
							ShuffleService.startService(MainActivity.this, false, Constants.TYPE_TEXTS);
						
						return true;
					}
				};
				
				//Sets up and displays the dialog box that asks for which type of list
				mChooser = ListTypeChooser.create(MainActivity.this, 
						"Turn on/off ShuffleTone", "", click, false);					
				mChooser.setState(settings.getBoolean(Constants.SETTINGS_CALLS_PWR, false), 
						settings.getBoolean(Constants.SETTINGS_TXT_PWR, false));				
				mChooser.show();
			}			
		});
		
		//Launch the file browser
		tv_browser.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View arg0) 
			{
				ListTypeChooser.OnOkClick clickHandler = new OnOkClick() {					
					@Override public boolean handleClick(boolean calls, boolean texts) {
						if(!(calls || texts)) 
						{
							Toast.makeText(MainActivity.this, "Please select a list", Toast.LENGTH_SHORT).show();
							return false;
						}
						
						Intent intent = new Intent(MainActivity.this, FileBrowser.class);
						
						intent.putExtra("calls", calls);
						intent.putExtra("texts", texts);
						
						ArrayList<String> filepaths = new ArrayList<String>();
						if(calls)
							filepaths.add(Constants.DEFAULT_CALLS);
						if(texts)
							filepaths.add(Constants.DEFAULT_TEXTS);
						
						intent.putExtra("filepaths", filepaths);
						
						//TODO - Take this out when editing the list is possible
						if(calls && !texts)
							intent.putExtra("playlist", PlaylistIO.loadPlaylist(MainActivity.this, Constants.DEFAULT_CALLS));
						else if(texts && !calls)
							intent.putExtra("playlist", PlaylistIO.loadPlaylist(MainActivity.this, Constants.DEFAULT_TEXTS));
					
						MainActivity.this.startActivity(intent);
						
						return true;
					}
				};
				
				mChooser = ListTypeChooser.create(MainActivity.this, 
						"Pick Ringtones for which list?", "One or Both:", clickHandler, false);
				
				mChooser.show();
				
			}
		});
		
		//View the currently used playlist. Allows for editing of the current playlist
		tv_viewCurrent.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) 
			{
				mChooser = new ListTypeChooser(MainActivity.this, "View what playlist?", 
						"", new ListTypeChooser.OnOkClick() 
				{	
					String saveLocation = "";
					@Override public boolean handleClick(boolean calls, boolean texts) 
					{				
						if(calls) saveLocation = Constants.DEFAULT_CALLS;
						else saveLocation = Constants.DEFAULT_TEXTS;
						
						mViewDialog = new ViewListDialog(MainActivity.this, saveLocation, "View List", true);
						return true;
					}
				}, true);
				
				mChooser.show();
			}
		});
		
		//Launches the playlist backup activity
		tv_backup.setOnClickListener(new OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{
				File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/ShuffleTone");
				if(!folder.exists())
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Failed...");
					builder.setMessage("You don't have any lists to back up or restore.");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {						
						@Override public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
						}
					});
					
					mDialog = builder.create();
					mDialog.show();
					return;
				}
				
				mChooser = new ListTypeChooser(MainActivity.this, "Backup/Restore What Playlist?", 
						"", new ListTypeChooser.OnOkClick() 
				{	
					String saveLocation = "";
					@Override public boolean handleClick(boolean calls, boolean texts) 
					{				
						if(calls) saveLocation = Constants.DEFAULT_CALLS;
						else saveLocation = Constants.DEFAULT_TEXTS;
						
						Intent intent = new Intent(MainActivity.this, BackupActivity.class);	
						intent.putExtra("filepath", saveLocation);
						MainActivity.this.startActivity(intent);
						
						return true;
					}
				}, true);
				
				mChooser.show();	
			}
		});
		
		tv_test.setOnClickListener(new OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{
				Intent intent = new Intent(MainActivity.this, ShuffleTest.class);	
				MainActivity.this.startActivity(intent);
			}
		});
		
		tv_donation.setOnClickListener(new OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{
				Intent intent = new Intent(MainActivity.this, Donation.class);	
				MainActivity.this.startActivity(intent);
			}
		});
	}
	
	/***
	 * Prepares the Title bar of this activity
	 */
	private void prepareTitle()
	{
		View v = this.getLayoutInflater().inflate(R.layout.title_template, null);
		
		TextView exit = (TextView)v.findViewById(R.id.tv_leftCorner);
		TextView donate = (TextView)v.findViewById(R.id.tv_rightCorner);
		TextView title = (TextView)v.findViewById(R.id.tv_Title);
		
		title.setText("ShuffleTone");
		exit.setText("Exit");
		donate.setText("Donate");
		
		//Quit the program
		exit.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				MainActivity.this.finish();
			}
		});
		
		//Donate button
		donate.setOnClickListener(new OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{
				String url = "http://dizware.wirenode.mobi/page/10";
				Intent i = new Intent(Intent.ACTION_CHOOSER);
				Intent data = new Intent(Intent.ACTION_VIEW);
				data.setData(Uri.parse(url));
				data.addCategory(Intent.CATEGORY_BROWSABLE);
				i.putExtra(Intent.EXTRA_INTENT,data);
				
				startActivity(Intent.createChooser(i, "View website..."));
			}
		});
		rl_title.addView(v);	
	}
	
	/***
	 * Prepares the drawer of this application
	 */
	private void prepareDrawer()
	{
		appSettings = new AppSettings(this, this.drawer);
		this.drawer.setContent(appSettings.prepareSettings());
		
		//When drawer opens, turn off the buttons in the main screen
		sd_main.setOnDrawerOpenListener(new OnDrawerOpenListener() {			
			@Override public void onDrawerOpened() {
				for(int i = 0; i < sv_content.getChildCount(); i++)
					sv_content.getChildAt(i).setEnabled(false);
			}
		});
		
		//When drawer closes, turn on the buttons in the main screen
		sd_main.setOnDrawerCloseListener(new OnDrawerCloseListener() {			
			@Override public void onDrawerClosed() {
				for(int i = 0; i < sv_content.getChildCount(); i++)
					sv_content.getChildAt(i).setEnabled(true);
				
				drawer.setContent(appSettings.prepareSettings());
				drawer.setTitle(new View(MainActivity.this));
			}
		});
		
		//Set up the size of drawer content based on the display metrics
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		for(int i = 0; i < drawer.content.getChildCount(); i++)
			drawer.content.getChildAt(i).measure(metrics.widthPixels, metrics.heightPixels);
		
		drawer.content.measure(metrics.widthPixels, metrics.heightPixels);
		int height = drawer.content.getMeasuredHeight();
		changeViewHeight(this.sd_main, (int)(height + 100*metrics.density));
	}
	
	/***
	 * Changes the height of the given view
	 * @param view - View we are resizing
	 * @param height - The new height for the view
	 */
    private void changeViewHeight(View view, int height)
    {
		//Change content height
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,height);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        view.setLayoutParams(params);
    }
    
    /***
     * Handles when user presses the back button
     */
    @Override public void onBackPressed() 
    {
    	if(sd_main.isOpened())
    		if(drawer.content.getChildAt(0).equals(appSettings.prepareSettings()))
    	    	sd_main.close();
    		else if(drawer.content.getChildAt(0).equals(appSettings.prepareHelp()))
    			drawer.setContent(appSettings.prepareSettings());
    		else
    			drawer.setContent(appSettings.prepareHelp());
    	else
    		super.onBackPressed();
    }
    
	/***
	 * Creates the option menu
	 */
	@Override public boolean onCreateOptionsMenu(Menu menu) 
	{
		sd_main.animateOpen();
		return false;
	}
	
	/***
	 * Closes the option menu when you press menu
	 */
	@Override public void onOptionsMenuClosed(Menu menu) 
	{
		sd_main.animateClose();
	}
	
	/***
	 * Takes care of all the dialogs so that there is no window leaks
	 */
	@Override protected void onPause() 
	{
		if(mChooser != null && mChooser.isShowing()) mChooser.cancel();
		if(mDialog != null && mDialog.isShowing()) mDialog.cancel();
		if(mViewDialog != null && mViewDialog.isShowing()) mViewDialog.cancel();
		if(spinnerDialog != null && spinnerDialog.isShowing()) spinnerDialog.cancel();
		if(appSettings != null) appSettings.closeDialogs();
		super.onPause();
	}
}
