package com.DizWARE.ShuffleTone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

/***
 * This class is the User Interface for the file browser.
 * Handles the list of files on the users card, and the files that they
 * save into their current playlist
 * 
 * 
 * @author Tydiz
 *
 */
public class FileBrowser extends ListActivity 
	implements OnClickListener, OnCheckedChangeListener, Runnable
{
	SlidingDrawer sd_accept;
	SlidingDrawer sd_sort;
	
	Button btn_ok;
	Button btn_cancel;
	
	Button btn_artist;
	Button btn_title;
	Button btn_filepath;
	Button btn_duration;
	Button btn_fArtist;
	Button btn_fFilePath;
	Button btn_fType;
	
	Button btn_check;
	Button btn_uncheck;
	
	TextView tv_counters;
	
	ArrayList<Ringtone> ringtones;
	ArrayList<Ringtone> currentList;
	ArrayList<Ringtone> originalList;
	
	public ArrayList<ToggleButton> boxes;
	
	ListView lv_fileBrowse;
	
	Comparator<Ringtone> artistComparator;
	Comparator<Ringtone> titleComparator;
	Comparator<Ringtone> durationComparator;
	
	OnClickListener sortListener;
	OnClickListener filterListener;
		
	Ringtone currentlyPlaying;
	
	String writeCode;
	
	boolean scanningCard;
	boolean clickable = true;
	boolean isPlaying = false;
	
	int currentSort;	
	
	View rl_row;
	
	ProgressDialog progressDialog;
	
    Handler mHandler = new Handler();
    Runnable reset;
	
	/***
	 * Creates the GUI for the file browser
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.filebrowser);
		

		//Gets the code for what we are writing to(SMS or Calls)
		writeCode = getIntent().getStringExtra("writeCode");
		
		originalList = new ArrayList<Ringtone>();
		currentList = new ArrayList<Ringtone>();
		ringtones = new ArrayList<Ringtone>();
		
		boxes = new ArrayList<ToggleButton>();
		
		//*****Gui Junk****//
		sd_accept = (SlidingDrawer)findViewById(R.id.acceptDrawer);
		sd_sort = (SlidingDrawer)findViewById(R.id.sortDrawer);
		
		sd_accept.bringToFront();
		sd_sort.bringToFront();
		
		btn_ok = (Button)findViewById(R.id.btn_ok);
		btn_cancel = (Button)findViewById(R.id.btn_cancel);
		
		btn_artist = (Button)findViewById(R.id.btn_artist);
		btn_title = (Button)findViewById(R.id.btn_title);
		btn_filepath = (Button)findViewById(R.id.btn_filepath);
		btn_duration = (Button)findViewById(R.id.btn_duration);
		btn_fArtist = (Button)findViewById(R.id.btn_fArtist);
		btn_fFilePath = (Button)findViewById(R.id.btn_fFilepath);
		btn_fType = (Button)findViewById(R.id.btn_fType);
		
		btn_check = (Button)findViewById(R.id.btn_check);
		btn_uncheck = (Button)findViewById(R.id.btn_uncheck);
		
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		
		tv_counters = (TextView)findViewById(R.id.tv_counters);
		
		LayoutInflater inflater = getLayoutInflater();

		rl_row = inflater.inflate(R.layout.row, null);
		
		updateCounter();
		
		lv_fileBrowse = this.getListView();
		
		//********EMBEDED CLASSES************//		
		
		/***
		 * Handles when the play button should stop
		 */
        reset = new Runnable() 
        {
    	   public void run() 
    	   {
    		   //Handles a weird bug that lets .ogg's repeat indefinitely
    		   if(!currentlyPlaying.getFileExtension().equalsIgnoreCase("ogg")&&
    				   currentlyPlaying.isPlaying())
    			   mHandler.postDelayed(this, 1000);
    		   else
    			   stopRingtone();
    	   }
        };
		
		/***
		 * Checks all boxes
		 */
		btn_check.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{				
				check();
			}			
		});
		
		/***
		 * Unchecks all boxes
		 */
		btn_uncheck.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{				
				uncheck();
			}			
		});
		
		/***
		 * Updates the counter view when you open drawer
		 */
		sd_sort.setOnDrawerOpenListener(new OnDrawerOpenListener() 
		{			
			@Override
			public void onDrawerOpened() 
			{ updateCounter(); }
		});
		
		/***
		 * Listener for all the sort buttons
		 */
		sortListener = new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				Button btn = (Button)v;
				
				if("Artist".equalsIgnoreCase(btn.getText().toString()))
						fillList(Ringtone.CAT_ARTIST);
				if("Title".equalsIgnoreCase(btn.getText().toString()))
						fillList(Ringtone.CAT_TITLE);
				if("Filepath".equalsIgnoreCase(btn.getText().toString()))
						fillList(Ringtone.CAT_PATH);
				if("Duration".equalsIgnoreCase(btn.getText().toString()))
						fillList(Ringtone.CAT_DURATION);
			}
		};				
		
		//Set  the sort buttons listen for this listener
		btn_artist.setOnClickListener(sortListener);
		btn_title.setOnClickListener(sortListener);
		btn_filepath.setOnClickListener(sortListener);
		btn_duration.setOnClickListener(sortListener);

		/***
		 * Listens for when the user clicks one of the filter buttosn
		 */
		filterListener = new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				ListDialog ld = null;
				//Shows a dialog of all the artists( so the user can filter )
				if(v.equals(btn_fArtist))
				{
					ld = new ListDialog(FileBrowser.this,
							getCategoryList(Ringtone.CAT_ARTIST),
							Ringtone.CAT_ARTIST);
				}
				
				//Shows a dialog of all the filepath( so the user can filter )
				if(v.equals(btn_fFilePath))
				{
					ld = new ListDialog(FileBrowser.this,
							getCategoryList(Ringtone.CAT_PATH),
							Ringtone.CAT_PATH);
				}
				
				//Shows a dialog of all the types( so the user can filter )
				if(v.equals(btn_fType))
				{
					ArrayList<String> list = new ArrayList<String>();
					list.add("Ringtone Files");
					list.add("Music Files");
					
					ld = new ListDialog(FileBrowser.this,list, -1);
				}
				
				ld.show();
			}			
		};
		
		btn_fArtist.setOnClickListener(filterListener);
		btn_fFilePath.setOnClickListener(filterListener);
		btn_fType.setOnClickListener(filterListener);
		
		/***
		 * Sorts ringtones by artist ( using a comparator )
		 */
		artistComparator = new Comparator<Ringtone>()
		{
			@Override
			public int compare(Ringtone ringtoneA, Ringtone ringtoneB) 
			{
				int comp = ringtoneA.getArtist().compareToIgnoreCase(ringtoneB.getArtist());
				
				if(comp == 0)
					comp = ringtoneA.getTitle().compareToIgnoreCase(ringtoneB.getTitle());
				
				return comp;
			}			
		};
		
		/***
		 * Sorts ringtones by duration ( using a comparator )
		 */
		durationComparator = new Comparator<Ringtone>()
		{
			@Override
			public int compare(Ringtone ringtoneA, Ringtone ringtoneB) 
			{
				if(ringtoneA.getDuration() < ringtoneB.getDuration())
					return -1;
				else if(ringtoneA.getDuration() > ringtoneB.getDuration())
					return 1;
				else
					return ringtoneA.getTitle().compareToIgnoreCase(ringtoneB.getTitle());
			}
			
		};
		
		/***
		 * Sorts ringtones by title ( using a comparator )
		 */
		titleComparator = new Comparator<Ringtone>()
		{
			@Override
			public int compare(Ringtone ringtoneA, Ringtone ringtoneB) 
			{
				int comp = ringtoneA.getTitle().compareToIgnoreCase(ringtoneB.getTitle());
				
				if(comp == 0)
					comp = ringtoneA.getPath().compareToIgnoreCase(ringtoneB.getPath());
				
				return comp;
			}			
		};
	}
	
	/***
	 * Handles when the app starts
	 */
	@Override
	protected void onStart() 
	{
		super.onStart();
		if(checkForCard())
		{		
			scanningCard = true;
						
			//Start reading the .XML file
			Thread thread = new Thread(this);
			thread.start();
			scanCard(this);
			
			//Pause the thread so that the other thread can finish its job
			while(scanningCard)
				try { Thread.sleep(100); } 
				catch (InterruptedException e) {}
				
			//Sets all the ringtones that were selected in file
				//to be selected in the list
			for(Ringtone ringtone : ringtones)
				for(Ringtone current : currentList)
					if(ringtone.equals(current))
						current.setSelect(true);
			
			fillList(Ringtone.CAT_TITLE);
		}
	}
	
	/***
	 * Handles when the activity is left or screen is turned off
	 */
	@Override
	protected void onPause() 
	{
		this.stopRingtone();
		
		//Uhh...I don't know why I had this in...maybe a bad copy and paste
		//this.finish();
		
		
		super.onPause();
	}
	
	/***
	 * When activity ends(for whatever reason), 
	 */
	@Override
	protected void onStop() 
	{
		this.stopRingtone();
		this.finish();
		super.onStop();
	}
	
	/***
	 * Click listener for when Ok, cancel, or play button
	 */
	@Override
	public void onClick(View v) 
	{		
		Button btn = (Button)v;
		
		//Plays the ringtone
		if("Play".equalsIgnoreCase(btn.getText().toString()))
		{
			playRingtone((Ringtone)btn.getTag());						
			return;
		}
		
		//Stops the ringtone
		if("Stop".equalsIgnoreCase(btn.getText().toString()))
		{
			stopRingtone();
			return;
		}
				
		//User presses ok button
		if("OK".equalsIgnoreCase(btn.getText().toString()))
		{
			//Sets up a listener for a dialog that asks the user if the results
				//should be filtered to whats on view or not
			Dialog.OnClickListener clickListener = new Dialog.OnClickListener() 
			{				
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					//If the user accepts the dialog, then fix their ringtone list
					if(which == Dialog.BUTTON_POSITIVE)
						fixRingList();
						
					
					saveList();
					dialog.cancel();
					
				}
			};
			
			
			ringtones = fixIds();
			
			//If there is more ringtones selected than ringtones that are selected in view
			if(getSelectedInView() != ringtones.size())
				UserDialogs.YesNo(this, "I noticed that you had ringtones selected " +
						"that are not in view. Do you want me to filter them out or should I keep them?\n" +
						"(Yes to filter, No to keep)", clickListener);
			else
				saveList();
			
			return;
		}
		
		//Closes the file browser
		this.finish();
	}
	
	/***
	 * Gets the list of files with one specific category
	 * @param category - the category that we want from the files
	 * @return - list of files with the same category
	 */
	public ArrayList<String> getCategoryList(int category)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		//Creates a list of a specific category
		for(Ringtone r : originalList)
			if(!list.contains(r.getCategory(category)))
				list.add(r.getCategory(category));
		
		Collections.sort(list);
		
		return list;
	}
	
	/***
	 * Saves the list to file. If no files are in list; user is notified and 
	 * save is rejected
	 */
	private void saveList()
	{		
		//Lets user know that they didn't pick any files
		if(this.ringtones.isEmpty())
		{
			Toast.makeText(this, "No Files Selected", 
					Toast.LENGTH_LONG).show();
			return;
		}				
		
		//Writes the selected ringtone list to file and creates a sort list
		try 
		{
			XMLWriter.writeFile(ringtones, XMLReader.getDefaultFile(writeCode));
			Shuffler.runShuffle(this, writeCode, ringtones);
		} catch (IOException e) 
		{
			//Notifies user that save has failed
			Toast.makeText(this, "Save has failed...", Toast.LENGTH_LONG);
			return;
		}
		
		this.finish();
	}
	
	
	/***
	 * Makes sure that nothing that is not on the list doesn't get saved(Mostly for
	 * filtering)
	 */
	@SuppressWarnings("unchecked")
	public void fixRingList()
	{
		//Coppies the ringtones into this list
		ArrayList<Ringtone> temp = (ArrayList<Ringtone>)ringtones.clone();
		
		//For every ringtone in them, if it doesn't exist in the view
			//remove it
		for(Ringtone r : temp)
			if(!isRingtoneInList(r,currentList))
				ringtones.remove(r);
		
		//Sort the list
		Collections.sort(ringtones);
		
	}
	
	/***
	 * Updates the counter view so that the user can see their ringtone counts
	 */
	private void updateCounter()
	{
		tv_counters.setText("Number of media files found: " + originalList.size() + "\n" +
							"Number of files currently being viewed: " + currentList.size() + "\n" +
							"Number of files selected: " + ringtones.size() + "\n"+
							"");
	}
	
	/***
	 * Gets the number of selected files that are in the current 'filter'
	 * @return
	 */
	public int getSelectedInView()
	{
		int count = 0;
		for(int i = 0; i < ringtones.size(); i++)
			if(isRingtoneInList(ringtones.get(i),currentList))
				count++;
		
		return count;
	}
	/***
	 * Checks to see if the given ringtone is in the current list
	 * @param ringtone - Ringtone that we are searching for
	 * @return - true if the ringtone is in the list, false otherwise
	 */
	public boolean isRingtoneInList(Ringtone ringtone, ArrayList<Ringtone> list)
	{
		for(Ringtone r : list)
			if(ringtone.equals(r))
				return true;
		
		return false;
	}

	/***
	 * Handles when the select button is pressed
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		Ringtone ringtone = (Ringtone)buttonView.getTag();
		boolean inList = isRingtoneInList(ringtone, ringtones);
		boolean inView = isRingtoneInList(ringtone, currentList);
		
		ringtone.setSelect(isChecked);
		
		//Handles when ringtone is checked
		if(isChecked)
		{
			buttonView.setButtonDrawable(R.drawable.selectbox_checked);
			
			//Checks to make sure this is a valid add
			if(!inList&&inView)
				ringtones.add(ringtone);
		}
		//Handles when ringtone is unchecked
		else if(!isChecked)
		{
			buttonView.setButtonDrawable(R.drawable.selectbox_unchecked);
			
			//Checks to make sure this is a valid remove
			if(inList&&inView)
				ringtones.remove(buttonView.getTag());
		}
	}
	
	/***
	 * Plays the given ringtone
	 * @param ringtone - Ringtone we want to play
	 */
	public void playRingtone(Ringtone ringtone)
	{
		stopRingtone();
		
		currentlyPlaying = ringtone;		
		currentlyPlaying.playRingtone(this);

		//Adds a time handler to the queue
        mHandler.postDelayed(reset, currentlyPlaying.getDuration());
      
	}
	
	
	
	/***
	 * Stops the ringtone from playing if it is not null
	 */
	public void stopRingtone()
	{
		if(currentlyPlaying != null)
		{
			currentlyPlaying.stopRingtone();			
			currentlyPlaying = null;
			
			//Removes the time handler message from the queue
			mHandler.removeCallbacks(reset);
		}
	}
	
	/***
	 * Checks to see if a SD Card exists
	 * @return - True if there is an SD Card, false otherwise
	 */
	public boolean checkForCard()
	{
		//If the external storage is not mounted, display No SD Card
		if(!android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED))
		{
			Toast.makeText(this, "No SD Card Present", Toast.LENGTH_LONG).show();
			this.finish();
			return false;
		}
		
		return true;
	}
	
	/***
	 * Displays a progress box to the user, while it scans the cards media files
	 */
	@SuppressWarnings("unchecked")
	public void scanCard(Activity context)
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
		cursor = context.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
						proj, null, null, null);						
	
		
		//Creates a cursor using the external job and the given projection
	
		try
		{
			runScan(cursor, "e:");	
		}catch(Exception e){Toast.makeText(context, "Failed External scan", Toast.LENGTH_LONG).show();}
	
		
		//Media on phone
		cursor = context.managedQuery(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, 
				proj, null, null, null);
		
		try
		{
			runScan(cursor, "i:");	
		}catch(Exception e){Toast.makeText(context, "Failed internal scan", Toast.LENGTH_LONG).show();}
		
		//New projection for DRM(its special, so this is all that is visible in cursor)
		proj = new String[]{MediaStore.Audio.Media.DATA,
							MediaStore.Audio.Media.TITLE,
							MediaStore.Audio.Media._ID};
		
		//Media in DRM provider
		cursor = context.managedQuery(Uri.parse("content://drm/audio"), 
				proj, null, null, null);
		try
		{
			runDRM(cursor);
		}catch(Exception e){Toast.makeText(context, "Failed DRM scan", Toast.LENGTH_LONG).show();}
			
		currentList = (ArrayList<Ringtone>)originalList.clone();			
	}
	
	/***
	 * Runs the scan on the given cursor
	 * @param cursor
	 */
	
	private void runScan(Cursor cursor, String location)
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
			//if((isRingtone||isMusic)&&(!isAlarm&&!isNotify))
				//Create a ringtone out of the data
			Ringtone ringtone = new Ringtone(artist,
												 cursor.getString(1),
												 cursor.getString(2),
												 cursor.getInt(3),
												 isRingtone,location);
			
			//Sets the tones duration
			ringtone.setDuration(d);
			
			//If the ringtone is of reasonable length(over 5 seconds) then
				//add to list
			if(!isRingtoneInList(ringtone, originalList))	
				originalList.add(ringtone);

			
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
												 true, "d:");
			
			//There is no way to load out duration, so this is an arbitrary number
			ringtone.setDuration(15000);	
				
			
			if(!isRingtoneInList(ringtone, originalList))
				originalList.add(ringtone);
			
			cursor.moveToNext();				
		}	
	}
	
	/***
	 * Reads the .XML file, representing the ringtone list
	 */
	@Override
	public void run() 
	{
		
		//allows us to keep from doing this again, and again
		if(scanningCard)
		{
			//Reads the file from the card(can take a while, unfortunately)
			try 
			{
				ringtones = XMLReader.readFile(
						XMLReader.getDefaultFile(writeCode));
			} catch (Exception e) {
				Toast.makeText(this, "Load has failed...", Toast.LENGTH_LONG).show();
			}
			
			scanningCard = false;		
		}
	}
	
	/***
	 * Unchecks all check boxes in the browser
	 */
	private void uncheck()
	{					
		for(Ringtone ringtone : currentList)
			ringtone.setSelect(false);
		
		//If there is a ringtone in the current list that isn't in ringtones, remove it
		for(int i = 0; i < currentList.size(); i++)
			if(isRingtoneInList(currentList.get(i),ringtones))
				ringtones.remove(currentList.get(i));		
		
		((FileAdapter)this.getListAdapter()).notifyDataSetChanged();
		
		updateCounter();
	}
	
	/***
	 * Checks all check boxes in the browser
	 */
	private void check()
	{				
		for(Ringtone ringtone : currentList)
			ringtone.setSelect(true);
		
		//If there is a ringtone in the current list that isn't in ringtones, add it
		for(int i = 0; i < currentList.size(); i++)
			if(!isRingtoneInList(currentList.get(i),ringtones))
				ringtones.add(currentList.get(i));
		
		((FileAdapter)this.getListAdapter()).notifyDataSetChanged();
		
		updateCounter();
	}
	
	
	
	/***
	 * Resorts the data and pushes it into the display adapter, so that it can
	 * be displayed in the correct order
	 * 
	 * @param sortCategory - Specified way to sort the data
	 */
	private void fillList(int sortCategory)
	{
		if(sortCategory == Ringtone.CAT_PATH)
			Collections.sort(currentList);
		else if(sortCategory == Ringtone.CAT_ARTIST)
			Collections.sort(currentList, artistComparator);
		else if(sortCategory == Ringtone.CAT_DURATION)
			Collections.sort(currentList, durationComparator);
		else
			Collections.sort(currentList, titleComparator);
		
		currentSort = sortCategory;
		
		//Rearrange the list display
		FileAdapter adapter = new FileAdapter(this, currentList, sortCategory);
		
		this.setListAdapter(adapter);
		
		updateCounter();
	}
	
	/***
	 * Filters out results using a restricted list(such as artist or filepath)
	 * @param restrictedList - List of items the user selected to keep in their display list 
	 * @param sortCategory - Category that this list exists in
	 */
	public void filterResults(ArrayList<String> restrictedList, int sortCategory)
	{
		currentList.clear();
		
		//Adds all the files from the given category
		for(int i = 0; i < originalList.size(); i++)
			for(int j = 0; j < restrictedList.size(); j++)
				if(restrictedList.get(j).equalsIgnoreCase(originalList.get(i).getCategory(sortCategory)))
						currentList.add(originalList.get(i));
		
		fillList(currentSort);
	}
	
	/***
	 * Filters out results by type of music file(ringtone or music)
	 * @param isRingtone - The selected type 
	 * @param sortCategory - Category that this list exists in
	 */
	public void filterResults(ArrayList<String> restrictedList, ArrayList<String> original)
	{
		currentList.clear();
		
		//Adds all ringtones to the view list
		if(restrictedList.contains(original.get(0)))
			for(int i = 0; i < originalList.size(); i++)
					if(originalList.get(i).isRingtone())
							currentList.add(originalList.get(i));
		
		//Adds all music files to the view list
		if(restrictedList.contains(original.get(1)))
			for(int i = 0; i < originalList.size(); i++)
				if(!originalList.get(i).isRingtone())
						currentList.add(originalList.get(i));
		
		fillList(currentSort);
		
	}
	
	/***
	 * Array Adapter, that uses the given information to arrange and display
	 * items in a list view
	 * 
	 * @author Tydiz
	 */
	private class FileAdapter extends ArrayAdapter<Ringtone>
	{
		Activity context;
		ArrayList<Ringtone> list;
		int sortCategory;
		
		/***
		 * Constructor - Sets up the adapter
		 * @param context - Activity that called this
		 * @param list - List that will be displayed in the ListView
		 * @param sortCategory - Value representing the way that this list will be sorted
		 */
		public FileAdapter(Activity context, ArrayList<Ringtone> list,int sortCategory) 
		{
			super(context,R.layout.row, list);
			
			this.list = list;
			this.context = context;
			this.sortCategory = sortCategory;
		}
				
		/***
		 * Gets the formatted view representing each row, for display
		 */
    	public View getView(int position, View convertView, ViewGroup parent)
    	{    		
    		LayoutInflater inflater = context.getLayoutInflater();
    		
    		//Breaks the row definition file into pieces so things can be 
    			//give definition
    		View row;
    		if(convertView == null)
    			row = inflater.inflate(R.layout.row, null);
    		else
    			row = convertView;
    		
    		//****GUI JUNK****//
    		Button btn_play = (Button)row.findViewById(R.id.btn_play);
    		
    		ToggleButton btn_select = (ToggleButton)row.findViewById(R.id.btn_select);
    		
    		TextView lbl_title = (TextView)row.findViewById(R.id.tv_name);
    		TextView lbl_artist = (TextView)row.findViewById(R.id.tv_artist);
    		TextView lbl_path = (TextView)row.findViewById(R.id.tv_path);
    		TextView lbl_duration =(TextView)row.findViewById(R.id.tv_duration);
    		//Sets up the labels, so that they display the correct info for their sort
    		lbl_title.setText(list.get(position).getTitle());
    		lbl_artist.setText(list.get(position).getCategory(Ringtone.CAT_ARTIST)); 
    		lbl_path.setText(list.get(position).getCategory(Ringtone.CAT_PATH));
    		
    		//Gets the duration of the corresponding ringtone
    		long duration = (int)list.get(position).getDuration()/1000+1;
    		
    		//Splits the duration into minutes and seconds
    		String minutes = "" + (duration)/60;
    		String seconds = "" + (duration)%60;
    		
    		//If the seconds length is less than 2, add a leading zero
    		if(seconds.length() < 2)
    			seconds = "0" + seconds;
    		
    		//Prints out the duration of the song if it is known. Otherwise
    			//prints a ? 
    		if(!list.get(position).getLocation().equalsIgnoreCase("d:"))
    			lbl_duration.setText(minutes + ":" + seconds);
    		else
    			lbl_duration.setText("?");
    		
    		if(sortCategory == Ringtone.CAT_PATH)
    			lbl_path.setTextColor(Color.WHITE);
    		else
    			lbl_artist.setTextColor(Color.WHITE);
    		
    		
    		//Sets up listeners
    		btn_play.setOnClickListener((FileBrowser)context);
    		btn_select.setOnCheckedChangeListener((FileBrowser)context);    		
    		
    		//Saves the ringtone associated with the buttons, in their tags
    		btn_play.setTag(list.get(position));
    		btn_select.setTag(list.get(position));
    		
    		list.get(position).setPlay(btn_play);
    		
    		btn_select.setChecked(list.get(position).isSelected());
//    		
//    		//If a checkbox isn't being watched yet, add it to the list
//    		if(!boxes.contains(btn_select))
//    			boxes.add(btn_select);
    		
    		
    			
    		//Checks any ringtones that are supposed to be selected
//    		for(Ringtone r : ringtones)
//    			if(list.get(position).equals(r))
//    				btn_select.setChecked(true);
    		
    		
    		return row;
    	}		
	}
	
	/***
	 * This fixes the ringtone lists IDs and removes any files that don't exist anymore
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Ringtone> fixIds()
	{
		ArrayList<Ringtone> temp = (ArrayList<Ringtone>)ringtones.clone();
		
		//If there is a ringtone that isn't in our original list
			//Remove it because its not on the phone anymore
		for(Ringtone ringtone : ringtones)
		{
			boolean found = false;
			for(Ringtone correct : originalList)
			{
				if(ringtone.equals(correct))
				{
					found = true;
					ringtone.fixRingtone(correct);
					break;
				}
			}	
			if(!found)
				temp.remove(ringtone);
		}
		
		return temp;
		
	}
	
	/***
	 * Runs a scan to check if the ringtones in the list are up to date.
	 * If not, the files are fixed
	 * @param ringtones - List that we are fixing
	 * @param context - Context that is running this system
	 */
	public static ArrayList<Ringtone> fixLoadedMedia(ArrayList<Ringtone> ringtones, Activity context)
	{
		FileBrowser fb = new FileBrowser();
		
		fb.originalList = new ArrayList<Ringtone>();
		fb.ringtones = ringtones;
		fb.scanCard(context);
		
		return fb.fixIds();
	}
}
