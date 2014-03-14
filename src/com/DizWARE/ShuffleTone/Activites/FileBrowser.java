package com.DizWARE.ShuffleTone.Activites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Activites.ListDialog.OnOkClick;
import com.DizWARE.ShuffleTone.Others.Actions;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.FilterType;
import com.DizWARE.ShuffleTone.Others.Log;
import com.DizWARE.ShuffleTone.Others.PlaylistIO;
import com.DizWARE.ShuffleTone.Others.Ringtone;
import com.DizWARE.ShuffleTone.Others.RingtonePlaylist;
import com.DizWARE.ShuffleTone.Services.ShuffleService;

/***
 * This activity allows the user to pick out ringtones
 * 
 * @author Tyler Robinson
 */
public class FileBrowser extends Activity 
{
	ListView lv_browser;
	BrowserAdapter mAdapter;
	
	RelativeLayout rl_title;
	RelativeLayout rl_content;
	
	ToggleButton tb_selectAll;
	
	SlidingDrawer sd_main;
	
	DrawerLayout drawer;
	RelativeLayout frameLayout;
	
	RelativeLayout defaultContent;
	RelativeLayout sortContent;
	RelativeLayout filterContent;
	
	RingtoneButton btn_media;
	
	RingtonePlaylist mAllRingtones;
	
	List<FilterType> mFilters;
	
	BroadcastReceiver mViewReceiver;
	
	Comparator<Ringtone> artistComparator;
	Comparator<Ringtone> titleComparator;
	Comparator<Ringtone> pathComparator;
	Comparator<Ringtone> durationComparator;
	
	boolean saveToCalls = false;
	boolean saveToTexts = false;
	
	boolean isDrawerOpened = false;
	
	Ringtone currentlyPlaying = null;
	MediaPlayer player = null;
	
	AlertDialog mDialog;
	ViewListDialog mViewListDialog;
	ListDialog<FilterType> mListDialog;
	ListTypeChooser mChooserDialog;
	ProgressDialog saveProgressDialog;
	
	Log log;
	
	/***
	 * Creates the file browser activity
	 */
	@Override protected void onCreate(Bundle savedInstanceState) 
	{
		mAdapter = new BrowserAdapter(this);
		
		if(savedInstanceState != null)
			restoreState(savedInstanceState);
	
		log = new Log(this);
		prepareFrame();
		this.setContentView(frameLayout);	
		
		if(savedInstanceState != null)
			lv_browser.setSelection(savedInstanceState.getInt("listPosition"));		
		
		saveToCalls = this.getIntent().getBooleanExtra("calls", false);
		saveToTexts = this.getIntent().getBooleanExtra("texts", false);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override protected void onDestroy() 
	{
		try
		{
			this.unregisterReceiver(mViewReceiver);
		}catch(IllegalArgumentException e)
		{
			log.e( "Did not correctly register, or unregister this receiver. Fail silently");
		}
		super.onDestroy();
	}
	
	/***
	 * Saves the current status of the file browser, including the ringtones, the adapter list, the filters and the
	 * position in the list
	 */
	@Override protected void onSaveInstanceState(Bundle outState) 
	{
		outState.putSerializable("mAllRingtones", mAllRingtones);
		outState.putSerializable("adapterList", mAdapter.mPlaylist);
		outState.putSerializable("mFilters", (ArrayList<FilterType>)mFilters);
		outState.putInt("listPosition", lv_browser.getFirstVisiblePosition());
	}
	
	/***
	 * Restore the state of the file browser
	 * 
	 * @param savedInstanceState - The saved state from before
	 */
	@SuppressWarnings("unchecked")
	private void restoreState(Bundle savedInstanceState)
	{
		if(savedInstanceState.containsKey("mAllRingtones"))
			mAllRingtones = (RingtonePlaylist)savedInstanceState.getSerializable("mAllRingtones");
		if(savedInstanceState.containsKey("adapterList"))
			mAdapter.mPlaylist = (RingtonePlaylist)savedInstanceState.getSerializable("adapterList");
		if(savedInstanceState.containsKey("mFilters"))
			mFilters = ((ArrayList<FilterType>)savedInstanceState.getSerializable("mFilters"));
		
		mAdapter.notifyDataSetChanged();
	}
	
	/***
	 * Handles stopping the ringtone when screen goes off
	 */
	@Override protected void onPause() {
		btn_media.stopRingtone();
		
		if(mChooserDialog != null && mChooserDialog.isShowing()) mChooserDialog.cancel();
		if(mDialog != null && mDialog.isShowing()) mDialog.cancel();
		if(mViewListDialog != null && mViewListDialog.isShowing()) mViewListDialog.cancel();
		if(saveProgressDialog != null && saveProgressDialog.isShowing()) saveProgressDialog.cancel();
		if(mListDialog != null && mListDialog.isShowing()) mListDialog.cancel();
		
		super.onPause();
	}
	
	/***
	 * Handles when the back button is pressed
	 */
	@Override public void onBackPressed() 
	{
		if(sd_main.isOpened())
		{
			if(drawer.content.getChildAt(0).equals(this.defaultContent))
				sd_main.close();
			else
				drawer.setContent(this.defaultContent);
		}
		else 
		{
			exitFileBrowser();
		}		
	}
	
	/***
	 * Prepares the application frame with all the necessary pieces
	 */
	private void prepareFrame()
	{
		rl_content = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.filebrowser, null);		
		rl_title = new RelativeLayout(this);
		
		if(mFilters == null)
			mFilters = new ArrayList<FilterType>();
		
		prepareContent();
		prepareTitle();
		prepareComparators();
		
		frameLayout = LayoutFrame.createFrame(this.getLayoutInflater(), rl_content, rl_title, R.drawable.slidersort);
		
		sd_main = (SlidingDrawer)frameLayout.findViewById(R.id.sd_options);		
		drawer = new DrawerLayout(this.getLayoutInflater(), (FrameLayout)sd_main.getContent());
		prepareDrawer();
	}
	
	/***
	 * Prepare this Activities content
	 */
	private void prepareContent()
	{		
		lv_browser = (ListView)rl_content.findViewById(R.id.lv_browser);
		
		prepareContentHeader();
		
		lv_browser.setAdapter(mAdapter);
		
		if(mAllRingtones == null)
		{
			mAllRingtones = new RingtonePlaylist();
			loadList();
			
			//Select the ringtones that were provided in the intent
			RingtonePlaylist selectedList = (RingtonePlaylist)this.getIntent().getSerializableExtra("playlist");
			if(selectedList != null)
			{
				mAllRingtones.SelectRingtones(selectedList);	
				if(mAllRingtones.isAllSelected()) 
					tb_selectAll.setChecked(true);
			}
		}
	}
	
	/***
	 * Prepares the list header.
	 * Includes:
	 * Select All
	 * Media Controls
	 */
	private void prepareContentHeader()
	{
		View v = this.getLayoutInflater().inflate(R.layout.filebrowser_header, null);
		
		tb_selectAll = (ToggleButton)v.findViewById(R.id.tb_add_filter);
		TextView tv_name = (TextView)v.findViewById(R.id.tv_name);
		tv_name.setTag(tb_selectAll);
		
		btn_media = new RingtoneButton((Button)v.findViewById(R.id.btn_media), 
									   (TextView)v.findViewById(R.id.tv_media));
		
		tb_selectAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(buttonView.isChecked())
				{
					buttonView.setButtonDrawable(R.drawable.selectbox_checked);
					FileBrowser.this.mAdapter.mPlaylist.SelectAll();
				}
				else
				{
					buttonView.setButtonDrawable(R.drawable.selectbox_unchecked);
					FileBrowser.this.mAdapter.mPlaylist.DeselectAll();
				}

				mAdapter.notifyDataSetChanged();				
			}
		});	
		
		//If the select all button is pressed
		tv_name.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				ToggleButton tb_selectAll = (ToggleButton)v.getTag();
				tb_selectAll.toggle();			
			}
		});
		
		FrameLayout fl_selectAll = (FrameLayout)rl_content.findViewById(R.id.fl_selectAll);
		fl_selectAll.addView(v);
	}
	
	/***
	 * Loads the phone's entire list of ringtones
	 */
	private void loadList()
	{
		Cursor cursor;		
		//Sets up some columns to read from in the cursor
		String[] proj = new String[]{
				MediaStore.Audio.AudioColumns.ARTIST,
				 MediaStore.Audio.AudioColumns.DATA,
				 MediaStore.Audio.AudioColumns.TITLE,
				 MediaStore.Audio.AudioColumns._ID,
				 MediaStore.Audio.AudioColumns.IS_RINGTONE,
				 MediaStore.Audio.AudioColumns.IS_MUSIC,
				 MediaStore.Audio.AudioColumns.IS_NOTIFICATION,
				 MediaStore.Audio.AudioColumns.IS_ALARM,
				 MediaStore.Audio.AudioColumns.DURATION};
		

		//Media on SD Card
		cursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
						proj, null, null, null);						
	
		//Creates a cursor using the external job and the given projection
	
		try
		{
			runScan(cursor, Constants.LOC_EXTERNAL);	
		}catch(Exception e){log.e("Failed External scan");}
		
		
		//Media on phone
		cursor = managedQuery(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, 
				proj, null, null, null);
		
		try
		{
			runScan(cursor, Constants.LOC_INTERNAL);	
		}catch(Exception e){log.e("Failed Internal scan");}
		
		//New projection for DRM(its special, so this is all that is visible in cursor)
		proj = new String[]{MediaStore.Audio.Media.DATA,
							MediaStore.Audio.Media.TITLE,
							MediaStore.Audio.Media._ID};
				
		
		//Media in DRM provider
		cursor = managedQuery(Uri.parse("content://drm/audio"), 
				proj, null, null, null);
		try
		{
			runDRM(cursor);
		}catch(Exception e){log.e("Failed DRM scan");}
		
		mAdapter.copy(mAllRingtones);		
		
	}
	
	/***
	 * Runs the scan on the given cursor
	 * @param cursor - Type of db access we want
	 * @param location - Location to store in the ringtone
	 */	
	private void runScan(Cursor cursor, int location)
	{
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{				
			boolean isRingtone = cursor.getInt(4) == 1 ||cursor.getInt(6) == 1||cursor.getInt(7) == 1;
			
			String artist = cursor.getString(0);
			
			//Fixes an error when metadata is missing for the artist
			if(artist == null)
				artist = "unknown";
			
			long d = cursor.getLong(8);
			
			//If the file is a ringtone or music, but not a alarm or notification
			Ringtone ringtone = new Ringtone(artist,
												 cursor.getString(1),
												 cursor.getString(2),
												 cursor.getInt(3),
												 isRingtone,location);			
			//Sets the tones duration
			ringtone.setDuration(d);
			
			//If the ringtone is of reasonable length(over 5 seconds) then
				//add to list
			if(!mAllRingtones.contains(ringtone))	
				mAllRingtones.add(ringtone);
			
			cursor.moveToNext();				
		}	
	}
	
	/***
	 * Scans for DRM Content.
	 * @param cursor - Cursor that is progressing through the DRM Provider
	 */
	private void runDRM(Cursor cursor)
	{
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{				
			//If the file is a ringtone or music, but not a alarm or notification
			//if((isRingtone||isMusic)&&(!isAlarm&&!isNotify))
				//Create a ringtone out of the data
			Ringtone ringtone = new Ringtone("DRM Content",
												 cursor.getString(0),
												 cursor.getString(1),
												 cursor.getInt(2),
												 true, Constants.LOC_DRM);
			
			//There is no way to load out duration, so this is an arbitrary number
			ringtone.setDuration(15000);	
				
			if(!mAllRingtones.contains(ringtone))
				mAllRingtones.add(ringtone);
			
			cursor.moveToNext();				
		}	
	}
	
	/***
	 * Creates the option menu
	 */
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		sd_main.animateOpen();
		return false;
	}
	
	@Override public void onOptionsMenuClosed(Menu menu) {
		sd_main.animateClose();
	}
	
	private void exitFileBrowser()
	{
		ListTypeChooser.OnOkClick clickHandler = new ListTypeChooser.OnOkClick() 
		{	
			ArrayList<String> filepaths;
			
			Intent intent;
			
			@Override public boolean handleClick(boolean calls, boolean texts) {											
				intent = new Intent();
				intent.setAction(Actions.SaveComplete.toString());
				
				IntentFilter filter = new IntentFilter(Actions.SaveComplete.toString());
				BroadcastReceiver saveReceiver = new BroadcastReceiver() {
					@Override public void onReceive(Context context, Intent intent) 
					{
						try 
						{
							FileBrowser.this.unregisterReceiver(this);
						} catch(IllegalArgumentException e)
						{
							log.e( "Did not correctly register, or unregister this receiver. Fail silently");
						}
						
						
						if(saveProgressDialog != null) saveProgressDialog.cancel();
						
						if(intent.getBooleanExtra("success", false))
						{
							FileBrowser.this.finish();			
						}
						else
						{										
							mDialog = new AlertDialog.Builder(FileBrowser.this).create();
							mDialog.setTitle("Save Failed");
							mDialog.setMessage("Your lists has failed to save.\nPlease double check your selected ringtones.");
							mDialog.setButton("OK", new DialogInterface.OnClickListener() {
								@Override public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
							mDialog.show();
						}
					}
				};
				FileBrowser.this.registerReceiver(saveReceiver, filter);
				
				//filepaths = FileBrowser.this.getIntent().getStringArrayListExtra("filepaths");
				btn_media.stopRingtone();	
				
				
				if(!calls && !texts)
				{
					try
					{
						FileBrowser.this.unregisterReceiver(saveReceiver);
					}catch(IllegalArgumentException e)
					{
						log.e( "Did not correctly register, or unregister this receiver. Fail silently");
					}
					FileBrowser.this.finish(); 
					return true;					
				}
				
				filepaths = new ArrayList<String>();
				filepaths.clear();
				if(calls)
					filepaths.add(Constants.DEFAULT_CALLS);
				if(texts)
					filepaths.add(Constants.DEFAULT_TEXTS);
				
				saveProgressDialog = ProgressDialog.show(FileBrowser.this, "Saving...", "Saving Your Playlist");				
				
				Thread saveThread = new Thread(new Runnable() 
				{					
					@Override public void run() 
					{						
						for(int i = 0; i < filepaths.size(); i++)
						{							
							mAllRingtones.shuffle();
							if(!PlaylistIO.savePlaylist(FileBrowser.this, filepaths.get(i), mAllRingtones.getSelected()))
							{
								intent.putExtra("success", false);
								FileBrowser.this.sendBroadcast(intent);
								return;
							}
							
							intent.putExtra("success", true);
							ShuffleService.startService(FileBrowser.this, false, filepaths.get(i));
							FileBrowser.this.sendBroadcast(intent);
						}
					}
				});
				
				saveThread.start();
				
				return true;
			}
		};
		mChooserDialog = 
			ListTypeChooser.create(FileBrowser.this, "Save as the default for which of these lists?", "Uncheck all to discard changes", 
					clickHandler, false);
		
		mChooserDialog.setState(saveToCalls, saveToTexts);
		mChooserDialog.show();
	}
	
	/***
	 * Prepares the Title bar of this activity
	 */
	private void prepareTitle()
	{
		View v = this.getLayoutInflater().inflate(R.layout.title_template, null);
		TextView back = (TextView)v.findViewById(R.id.tv_leftCorner);
		TextView viewList = (TextView)v.findViewById(R.id.tv_rightCorner);
		
		back.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				exitFileBrowser();
			}
		});
		back.setText("Back & Save");
		viewList.setText("View Selected");
		
		mViewReceiver = new BroadcastReceiver() {			
			@Override public void onReceive(Context context, Intent intent) {
				RingtonePlaylist playlist = (RingtonePlaylist)intent.getSerializableExtra("playlist");
				mAllRingtones.DeselectAll();
				mAllRingtones.SelectRingtones(playlist);
				mAdapter.notifyDataSetChanged();
			}
		};
		
		this.registerReceiver(mViewReceiver, new IntentFilter(Actions.EditComplete.name()));
		
		viewList.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				RingtonePlaylist playlist = FileBrowser.this.mAllRingtones.getSelected();
				
				if(FileBrowser.this.currentlyPlaying != null)
					player = FileBrowser.this.currentlyPlaying.stopRingtone(player);
				
				mViewListDialog = new ViewListDialog(FileBrowser.this, playlist, false);
				mViewListDialog.show();
				
			}
		});
		
		rl_title.addView(v);
	}
	
	/***
	 * Prepares the drawer of this application
	 */
	private void prepareDrawer()
	{
		defaultContent = prepareDefault();
		sortContent = prepareSort();
		filterContent = prepareFilter();
		drawer.setContent(defaultContent);
		
		sd_main.setOnDrawerCloseListener(new OnDrawerCloseListener() {			
			@Override public void onDrawerClosed() {
				drawer.setContent(defaultContent);
			}
		});
		
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		for(int i = 0; i < defaultContent.getChildCount(); i++)
			defaultContent.getChildAt(i).measure(metrics.widthPixels, metrics.heightPixels);
		defaultContent.measure(metrics.widthPixels, metrics.heightPixels);
		int height = defaultContent.getMeasuredHeight();
		changeViewHeight(this.sd_main, (int)(height + 70*metrics.density));
	}
	
	/***
	 * Prepare the default layout in the sliding drawer
	 * @return - The layout to load up in the drawer
	 */
	private RelativeLayout prepareDefault()
	{
		RelativeLayout content = (RelativeLayout)this.getLayoutInflater().inflate(
				R.layout.sort_drawer_default, null);
		
		Button btn_sort = (Button)content.findViewById(R.id.btn_sort);
		Button btn_filter = (Button)content.findViewById(R.id.btn_filter);
		Button btn_viewFilter = (Button)content.findViewById(R.id.btn_filter_view);
		
		/**Sort button**/
		btn_sort.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				drawer.setContent(sortContent);
			}
		});
		
		/**Filter button**/
		btn_filter.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				drawer.setContent(filterContent);
			}			
		});
		
		/**View Filters button**/
		btn_viewFilter.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				viewFilters();
				sd_main.close();				
			}
		});
		
		return content;
	}
	
	/***
	 * Prepare the sort drawer
	 * @return- The layout for the sort options in the drawer
	 */
	private RelativeLayout prepareSort()
	{
		RelativeLayout content = (RelativeLayout)this.getLayoutInflater().inflate(
				R.layout.sort_drawer_sort, null);
		
		Button btn_artist = (Button)content.findViewById(R.id.btn_artist);
		Button btn_title = (Button)content.findViewById(R.id.btn_title);
		Button btn_path = (Button)content.findViewById(R.id.btn_filepath);
		Button btn_duration = (Button)content.findViewById(R.id.btn_duration);
		
		/***Sort by Artist Button***/
		btn_artist.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				mAdapter.sort(artistComparator);
				lv_browser.setSelection(0);
				mAllRingtones.sort(artistComparator);
				sd_main.close();
			}
		});
		
		/***Sort by Title Button***/
		btn_title.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				mAdapter.sort(titleComparator);
				lv_browser.setSelection(0);
				mAllRingtones.sort(titleComparator);
				sd_main.close();
			}
		});
		
		/***Sort by File Path Button***/
		btn_path.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				mAdapter.sort(pathComparator);
				lv_browser.setSelection(0);
				mAllRingtones.sort(pathComparator);
				sd_main.close();
			}
		});
		
		/***Sort by Duration Button***/
		btn_duration.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				mAdapter.sort(durationComparator);
				lv_browser.setSelection(0);
				mAllRingtones.sort(durationComparator);
				sd_main.close();
			}
		});
		
		return content;
	}
	
	/***
	 * Prepares the filter drawer options
	 * @return - The layout of the filter drawer
	 */
	private RelativeLayout prepareFilter()
	{
		RelativeLayout content = (RelativeLayout)this.getLayoutInflater().inflate(
				R.layout.sort_drawer_filter, null);
		
		Button btn_artist = (Button)content.findViewById(R.id.btn_artist);
		Button btn_path = (Button)content.findViewById(R.id.btn_filepath);
		Button btn_viewFilter = (Button)content.findViewById(R.id.btn_filter_view);
		
		/***Filter by Artist Button***/
		btn_artist.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				FilterAdapter adapter = new FilterAdapter(FileBrowser.this, false);
				List<FilterType> filters = mAllRingtones.getFilters(Constants.CAT_ARTIST);
				Collections.sort(filters);				
				
				filters.removeAll(mFilters);		
				
				adapter.addAll(filters);
				addFilters(adapter);
				sd_main.close();				
			}
		});
		
		/***Filter by File Path Button***/
		btn_path.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				FilterAdapter adapter = new FilterAdapter(FileBrowser.this, false);
				List<FilterType> filters = mAllRingtones.getFilters(Constants.CAT_PATH);
				Collections.sort(filters);
				
				filters.removeAll(mFilters);
				
				adapter.addAll(filters);
				addFilters(adapter);
				sd_main.close();				
			}
		});
		
		/***View Filters Button***/
		btn_viewFilter.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				viewFilters();
				sd_main.close();	
			}
		});
		
		return content;
	}
	
	/***
	 * Launches a dialog for adding filters
	 * @param adapter - Adapter to use in the list dialog. Should already have data in it
	 */
	private void addFilters(FilterAdapter adapter)
	{
		/***Handles the user clicking ok on the selection dialog for filters***/
		OnOkClick clickHandler = new OnOkClick() {
			@Override public boolean handleClick(ListAdapter adapter) {
				FilterAdapter fAdapter = (FilterAdapter)adapter;
				List<FilterType> list = fAdapter.getData();
				
				for(FilterType filter : list)
				{
					int index = mFilters.indexOf(filter);
					if(index < 0)
						mFilters.add(filter);
				}
				
				//TODO - Fix
				mAdapter.copy(mAllRingtones.filter(mFilters));
				
				RingtonePlaylist selectedList = mAdapter.mPlaylist.getSelected();
				FileBrowser.this.tb_selectAll.setChecked(false);
				
				mAdapter.mPlaylist.SelectRingtones(selectedList);
				
				if(mAdapter.mPlaylist.isAllSelected())
					FileBrowser.this.tb_selectAll.setChecked(true);
				
				return true;
			}
		};
		
		mListDialog = new ListDialog<FilterType>(
				FileBrowser.this, "Add Filters", adapter, clickHandler);
		
		View titleBar = this.getLayoutInflater().inflate(R.layout.filter_dialog_title, null);
		ToggleButton selectAll = (ToggleButton)titleBar.findViewById(R.id.tb_add_filter);
		selectAll.setTag(adapter);
		
		/***Adds the select all ability***/
		selectAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					buttonView.setButtonDrawable(R.drawable.selectbox_checked);
				else
					buttonView.setButtonDrawable(R.drawable.selectbox_unchecked);
				
				FilterAdapter adapter = (FilterAdapter)buttonView.getTag();
				for(int i = 0; i < adapter.getCount(); i++)
					adapter.getItem(i).selected = isChecked;
				adapter.notifyDataSetChanged();
			}
		});
		mListDialog.addTitleBar(titleBar);
		
		mListDialog.show();
	}
	
	/***
	 * Launches a dialog with all the selected filters
	 */
	private void viewFilters()
	{
		FilterAdapter adapter = new FilterAdapter(FileBrowser.this, true);
		Collections.sort(mFilters);
		adapter.addAll(mFilters);
		
		/***Refilter on accept***/
		OnOkClick clickHandler = new OnOkClick() {
			@Override public boolean handleClick(ListAdapter adapter) {
				mAdapter.copy(mAllRingtones.filter(mFilters));
				
				RingtonePlaylist selectedList = mAdapter.mPlaylist.getSelected();
				FileBrowser.this.tb_selectAll.setChecked(false);
				
				mAdapter.mPlaylist.SelectRingtones(selectedList);
				
				if(mAdapter.mPlaylist.isAllSelected())
					FileBrowser.this.tb_selectAll.setChecked(true);
				return true;
			}
		};
		mListDialog = ListDialog.create(FileBrowser.this, 
				"Edit Filters", adapter, clickHandler);
		
		
		View titleBar = this.getLayoutInflater().inflate(R.layout.filter_view_title, null);
		ImageButton removeAll = (ImageButton)titleBar.findViewById(R.id.ib_removeAll);
		removeAll.setTag(adapter);
		
		/***Remove all button***/
		removeAll.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				((FilterAdapter)v.getTag()).clear();
				mFilters.clear();
				((FilterAdapter)v.getTag()).notifyDataSetChanged();
			}
		});
		mListDialog.addTitleBar(titleBar);
		mListDialog.show();	
	}
	
	/***
	 * Prepares the comparators for the sorting
	 */
	private void prepareComparators()
	{
		/***Defines an artist sort***/
		artistComparator = new Comparator<Ringtone>() {			
			@Override public int compare(Ringtone object1, Ringtone object2) {
				int compare = object1.getArtist().compareToIgnoreCase(object2.getArtist());
				if(compare == 0)
					compare = object1.getTitle().compareToIgnoreCase(object2.getTitle());
				return compare;
			}
		};
		
		/***Defines a title sort***/
		titleComparator = new Comparator<Ringtone>() {			
			@Override public int compare(Ringtone object1, Ringtone object2) {
				int compare = object1.getTitle().compareToIgnoreCase(object2.getTitle());
				if(compare == 0)
					compare = object1.getArtist().compareToIgnoreCase(object2.getArtist());
				return compare;
			}
		};
		
		/***Defines a file path sort***/
		pathComparator = new Comparator<Ringtone>() {			
			@Override public int compare(Ringtone object1, Ringtone object2) {
				int compare = object1.getDirectory().compareToIgnoreCase(object2.getDirectory());
				if(compare == 0)
					compare = object1.getTitle().compareToIgnoreCase(object2.getTitle());
				return compare;
			}
		};
		
		/***Defines a duration sort***/
		durationComparator = new Comparator<Ringtone>() {			
			@Override public int compare(Ringtone object1, Ringtone object2) {
				/*long diff = object1.getDuration() - object2.getDuration();
				int compare = 0;
				
				if(diff != 0)
					compare = (int)((diff)/(Math.abs(diff)));
				if(compare == 0)
					compare = object1.getTitle().compareToIgnoreCase(object2.getTitle());
					
					
				return compare;*/
				
				if(object1.getDuration() < object2.getDuration())
					return -1;
				else if(object1.getDuration() == object2.getDuration())
					return object1.getTitle().compareToIgnoreCase(object2.getTitle());
				else
					return 1;
				
			}
		};
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
	 * Allows for loading in new data into our listview
	 * 
	 * @author Tyler Robinson
	 */
	private class BrowserAdapter extends RingtoneAdapter
	{
		Activity mContext;
		HashMap<String, Integer> alphaIndexer;
		int sortCategory = Constants.CAT_PATH;
		
		/***
		 * Creates an adapter for this browser to use
		 * @param context - Context in which we are applying this adapter in
		 */
		public BrowserAdapter(Activity context) 
		{
			super(context);
			mContext = context;
			alphaIndexer = new HashMap<String, Integer>();
		}
		
		/***
		 * Creates a browser adapter with an already made playlist
		 * 
		 * @param context - Context in which we are applying this adapter in
		 * @param playlist - Playlist we want added automatically
		 */
		public BrowserAdapter(Activity context, RingtonePlaylist playlist) 
		{
			super(context, playlist);
			mContext = context;
			alphaIndexer = new HashMap<String, Integer>();
		}
		
		
		@Override public void notifyDataSetChanged() 
		{
			if(FileBrowser.this.lv_browser != null)
				FileBrowser.this.lv_browser.setFastScrollEnabled(false);
			
			super.notifyDataSetChanged();
			updateAlphaIndex();
			
			if(FileBrowser.this.lv_browser != null)
			{
				FileBrowser.this.lv_browser.setFastScrollEnabled(true);
				jiggleWidth();
				jiggleWidth();
			}
		}

		/***
		 * Gets the current view from the adapter
		 */
		@Override public View getView(int position, View convertView, ViewGroup parent) 
		{
			if(convertView == null)
				convertView = mContext.getLayoutInflater().inflate(R.layout.filebrowser_row, null);
			
			//GUI Stuff
			CheckBox cb_selected = (CheckBox)convertView.findViewById(R.id.cb_select);
			TextView tv_artist = (TextView)convertView.findViewById(R.id.tv_artist);
			TextView tv_title = (TextView)convertView.findViewById(R.id.tv_title);
			TextView tv_location = (TextView)convertView.findViewById(R.id.tv_location);
			TextView tv_duration = (TextView)convertView.findViewById(R.id.tv_duration);
						
			cb_selected.setTag(this.getItem(position));
			convertView.setTag(this.getItem(position));
			
			//Sets the text of each row
			tv_artist.setText(this.getItem(position).getCategory(Constants.CAT_ARTIST));
			tv_title.setText(this.getItem(position).getCategory(Constants.CAT_TITLE));
			tv_location.setText(this.getItem(position).getCategory(Constants.CAT_PATH));
			tv_duration.setText(this.getItem(position).getCategory(Constants.CAT_DURATION));
			
    		//Handles when the selection button is checked/unchecked
			cb_selected.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
				@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked)
						buttonView.setButtonDrawable(R.drawable.selectbox_checked);
					else
						buttonView.setButtonDrawable(R.drawable.selectbox_unchecked);					
					
					//Changes the overall Select All button to change functionality based on the current status
						//of the checked ringtones
					boolean reselect = BrowserAdapter.this.mPlaylist.isAllSelected() && !isChecked;					
					((Ringtone)buttonView.getTag()).setSelect(isChecked);
					
					RingtonePlaylist selectedList = BrowserAdapter.this.mPlaylist.getSelected();
					
					if(reselect)
					{
						FileBrowser.this.tb_selectAll.setChecked(false);
						BrowserAdapter.this.mPlaylist.SelectRingtones(selectedList);
					}
					else if(BrowserAdapter.this.mPlaylist.isAllSelected())
					{
						FileBrowser.this.tb_selectAll.setChecked(true);
					}					
				}
			});
			
			cb_selected.setChecked(this.getItem(position).isSelected());
			
			//Play ringtone
			convertView.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					Ringtone ringtone = (Ringtone)v.getTag();					
					btn_media.stopRingtone();	
					currentlyPlaying = ringtone;
					btn_media.playRingtone();
				}
			});
						
			return convertView;
		}
		
		@Override public void sort() {
			sortCategory = Constants.CAT_PATH;
			super.sort();
		}
		
		@Override public void sort(Comparator<? super Ringtone> comparator) {
			if(comparator.equals(FileBrowser.this.artistComparator)) sortCategory = Constants.CAT_ARTIST;
			else if(comparator.equals(FileBrowser.this.pathComparator)) sortCategory = Constants.CAT_PATH;
			else if(comparator.equals(FileBrowser.this.titleComparator)) sortCategory = Constants.CAT_TITLE;
			else sortCategory = Constants.CAT_DURATION;

			super.sort(comparator);
		}
		
		private boolean FLAG_THUMB_PLUS = false;		
		/***
		 * Fix for a dumb issue that causes the alpha indexer to disappear after turning off and back on
		 * the fast scroll. 
		 * 
		 * This will move the width 1 pixel to force the relayout
		 */
		private void jiggleWidth() 
		{
		    ListView view = FileBrowser.this.lv_browser;

		    int newWidth = FLAG_THUMB_PLUS ? view.getWidth() - 1 : view.getWidth() + 1;
		    ViewGroup.LayoutParams params = view.getLayoutParams();
		    params.width = newWidth;
		    view.setLayoutParams( params );

		    FLAG_THUMB_PLUS = !FLAG_THUMB_PLUS;
		    FileBrowser.this.lv_browser.forceLayout();
		}
		
		/***
		 * Updates the indexes of the letters that represents each section
		 */
		public void updateAlphaIndex()
		{
			this.alphaIndexer.clear();
			ArrayList<Ringtone> playlist = this.mPlaylist.getPlaylist();
			
			for(int i = 0; i < playlist.size(); i++)
			{
				String alphaIndex = "";

				try
				{
					if(sortCategory == Constants.CAT_DURATION)
					{
						long duration = playlist.get(i).getDuration()/1000 + 1;
						long seconds = ((duration%60)/15) * 15;
						alphaIndex = (duration / 60) + ":" + seconds;
						if(seconds == 0) alphaIndex += "0";
					}
					else if(sortCategory == Constants.CAT_PATH)
					{
						
						alphaIndex = playlist.get(i).getCategory(sortCategory);
						if(alphaIndex.contains(Environment.getExternalStorageDirectory() + "")) 
							alphaIndex = alphaIndex.replace(Environment.getExternalStorageDirectory() + "", "/external/");
						alphaIndex = alphaIndex.split("/")[1].substring(0, 1);
						
					}
					else 
					{
						alphaIndex = playlist.get(i).getCategory(sortCategory);
						alphaIndex = alphaIndex.substring(0, 1).toUpperCase(Locale.ENGLISH);
						
					}
				}
				catch(IndexOutOfBoundsException e)
				{
					alphaIndex = " ";
				}
				if(!this.alphaIndexer.containsKey(alphaIndex))
					this.alphaIndexer.put(alphaIndex, i);
			}		
		}
		
		@Override public int getPositionForSection(int section) 
		{			
			if(section >= getSections().length)
				section = getSections().length-1;
			return this.alphaIndexer.get(getSections()[section]);
		}
		
		@Override public int getSectionForPosition(int position) 
		{
			return 1;
		}
		
		@Override public Object[] getSections() 
		{
			ArrayList<String> keys = new ArrayList<String>(this.alphaIndexer.keySet());
			//If we are sorted by duration, make sure the sort is using numbers rather than alphanumericals
				//Otherwise, sort as usual
			if(sortCategory == Constants.CAT_DURATION)
			{
				Collections.sort(keys,new Comparator<String>()
				{
					@Override public int compare(String arg0, String arg1) {
						String[] split1 = arg0.split(":");
						String[] split2 = arg1.split(":");
						
						if(Integer.parseInt(split1[0]) < Integer.parseInt(split2[0]))
								return -1;
						else if(Integer.parseInt(split1[0]) > Integer.parseInt(split2[0]))
								return 1;
						else if(Integer.parseInt(split1[1]) < Integer.parseInt(split2[1]))
								return -1;
						else if(Integer.parseInt(split1[1]) > Integer.parseInt(split2[1]))
								return 1;						
						return 0;
					}					
				});
			}
			else
				Collections.sort(keys);
			return keys.toArray();
		}
		
	}
	
	
	/***
	 * Handles when user interacts with the media button
	 * 
	 * @author Tyler Robinson
	 */
	private class RingtoneButton implements View.OnClickListener, Runnable
	{
		TextView mTitle;
		
		Drawable playButton;
		Drawable stopButton;
		
		Handler timerHandler;
		
		Button mMedia;
		
		/***
		 * Creates the media button
		 * 
		 * @param media - Button that we will use for the interaction
		 * @param title - Title that we will use to display the media info
		 */
		public RingtoneButton(Button media, TextView title) {
			mTitle = title;			
			mMedia = media;
			
			mMedia.setOnClickListener(this);
			mTitle.setOnClickListener(this);
			
			playButton = mMedia.getContext().getResources().getDrawable(R.drawable.play);
			stopButton = mMedia.getContext().getResources().getDrawable(R.drawable.stop);
			
			timerHandler = new Handler();
		}
		
		/***
		 * Plays the ringtone and changes the media button image
		 */
		public void playRingtone()
		{
			if(currentlyPlaying == null) return;
			
			player = currentlyPlaying.playRingtone(mMedia.getContext(), player);
			
			mTitle.setText(currentlyPlaying.getArtist() + "\n" 
					+ currentlyPlaying.getTitle());
			mMedia.setBackgroundDrawable(stopButton);
			if(currentlyPlaying.getLocation() != Constants.LOC_DRM)
			{
				timerHandler.removeCallbacks(this);
				timerHandler.postDelayed(this, currentlyPlaying.getDuration());
			}
		}
		
		/***
		 * Stops the ringtone from playing and changes the button to a play button
		 */
		public void stopRingtone()
		{
			if(currentlyPlaying != null && currentlyPlaying.isPlaying(player))
				player = currentlyPlaying.stopRingtone(player);
			
			mMedia.setBackgroundDrawable(playButton);
		}
		
		/***
		 * True if the ringtone is playing, false otherwise
		 */
		public boolean isPlaying()
		{
			return currentlyPlaying != null && currentlyPlaying.isPlaying(player);
		}
		
		/***
		 * Handles when the user clicks on the button or media text
		 */
		@Override public void onClick(View v) {
			if(isPlaying())
				stopRingtone();
			else
				playRingtone();
		}

		/***
		 * Stops the ringtone when called by the handle
		 */
		@Override public void run() {
			stopRingtone();			
		}		
	}
}
