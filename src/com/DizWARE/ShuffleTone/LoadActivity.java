package com.DizWARE.ShuffleTone;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/***
 * Activity for loading playlists(In future, may be merged with save activity)
 * 
 * Here's some TODO's to remind me:
 * TODO
 * TODO
 * TODO
 * 
 * @author Tydiz
 */
public class LoadActivity extends Activity 
	implements OnClickListener
{	
	Button btn_ok;
	Button btn_cancel;
	
	ListView lv_save;
	EditText et_save;
	
	TextView selectedView;
	TextView tv_empty;
	
	String writeCode;
	
	ArrayList<Ringtone> ringtones;
	
	/***
	 * Creates the activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.load);
		
		//Checks to see if there is a SD Card before starting
		if(!android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED))
		{
			Toast.makeText(this, "No SD Card Present", Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}
		
		//***GUI JUNK ***// 
		btn_ok = (Button)findViewById(R.id.btn_ok);
		btn_cancel = (Button)findViewById(R.id.btn_cancel);
		
		tv_empty = (TextView)findViewById(R.id.tv_empty);
		
		lv_save = (ListView)findViewById(R.id.lv_saveList);
		
		et_save = (EditText)findViewById(R.id.et_saveName);
		
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
				
		writeCode = getIntent().getStringExtra("writeCode");
		
		refresh();
	}
	
	/***
	 * Refreshes the display to show the current folder status
	 */
	private void refresh()
	{
		File folder = new File("/sdcard/ShuffleTone");
		ArrayList<String> fileList = new ArrayList<String>();
		
		if(folder.exists())
		{
			scanFiles(folder.listFiles(), fileList);			
			
			//Shows the empty text if there is no files in the list
			if(fileList.size() > 0)			
				tv_empty.setVisibility(View.INVISIBLE);
			else
				tv_empty.setVisibility(View.VISIBLE);
			
			lv_save.setAdapter(new PlaylistAdapter(this,fileList));
		}
	}
	
	/***
	 * Scans in the files in /sdcard/ShuffleTone
	 * 
	 * @param files - Files in the folder
	 * @param fileList - edited list of the files in the folder
	 */
	private void scanFiles(File[] files, ArrayList<String> fileList)
	{
		for(int i = 0; i < files.length; i++)
		{
			String fileName = files[i].getName();
			
			//If the file is not any of the default files, and ends in .xml
				//add it to the profile list
			if(fileName.endsWith(".xml")&&
					(!XMLReader.getDefaultFile("").equals(files[i])&&
					!XMLReader.getDefaultFile("sms").equals(files[i])))
				fileList.add(fileName.replace(".xml",""));
				
		}
	}

	/***
	 * Handles the button clicks
	 */
	@Override
	public void onClick(View v) 
	{
		Button btn = (Button)v;
		
		//Load button
		if(btn.getText().toString().equalsIgnoreCase("Load"))
		{			
			if(et_save.getText().length() > 0)
				loadList();
			else
				Toast.makeText(this, "No File to Load", Toast.LENGTH_SHORT).show();			
		}
		//Delete Button
		else if(btn.getText().toString().equalsIgnoreCase("Delete"))
		{
			deletePlaylist();
			refresh();
		}
		//Cancel button
		else if(btn.getText().toString().equalsIgnoreCase("Exit"))	
			this.finish();
		
		et_save.setText("");
		resetButtons();
		
	}
	
	/***
	 * Loads the playlist
	 */
	public void loadList()
	{		
		//if there is not a name; exit the load process
		if(et_save.getText().length() == 0)
			return;		

		String loadResults = "Loaded Successfully";	
		
		try {
			ArrayList<Ringtone> ringtones = XMLReader.readFile(
					new File("/sdcard/ShuffleTone/" + 
							selectedView.getText().toString() + ".xml"));
			FileBrowser.fixLoadedMedia(ringtones,this);			
			
			XMLWriter.writeFile(ringtones, XMLReader.getDefaultFile(writeCode));
			Shuffler.runShuffle(this, writeCode, ringtones);
		} catch (Exception e) { loadResults = "Failed Load";	}
		
		
		//Resets the text to be empty		
		Toast.makeText(this, loadResults, Toast.LENGTH_LONG).show();
		et_save.setText("");
		Toast.makeText(this, loadResults, Toast.LENGTH_LONG).show();
		
	}
	
	/***
	 * Creates a context menu when the user long presses a menu item
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) 
	{
		menu.add("Delete");
		
		//resets the button text to OK
		resetButtons();
		
		selectedView = (TextView)v;
		menu.setHeaderTitle("Additional Options");
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	/***
	 * Handles when the user selects a context option
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		//Delete Option selected
		if(item.getTitle().toString().equalsIgnoreCase("Delete"))
		{
			et_save.setText("Delete " + 
					selectedView.getText().toString() + "?");
			
			btn_ok.setText("Delete");
			btn_cancel.setText("Cancel");
			et_save.setTextColor(Color.RED);
		}
		return super.onContextItemSelected(item);
	}
	
	/***
	 * Deletes the selected view's playlist
	 */
	private void deletePlaylist()
	{
		File file = new File("/sdcard/ShuffleTone/" + 
				selectedView.getText().toString() + ".xml");
		file.delete();
	}
	
	/***
	 * Resets the UI back to its original form
	 */
	private void resetButtons()
	{
		btn_ok.setText("Load");
		btn_cancel.setText("Exit");
		et_save.setTextColor(Color.BLACK);
	}
	
	/***
	 * Adapter that to load up a file list
	 * @author Tydiz 
	 *
	 */
	public class PlaylistAdapter extends ArrayAdapter<String>
	{
		ArrayList<String> lists;
		Activity context;
		
		//Constructor - Builds an adapter that will load in all files 
			//in /sdcard/ShuffleTone
		public PlaylistAdapter(Activity context,
				ArrayList<String> objects) {
			super(context, R.id.tv_saveRow, objects);
			
			this.lists = objects;
			this.context = context;
		}
		
		/***
		 * Gets the views that are visible on screen
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			
				
    		LayoutInflater inflater = context.getLayoutInflater();
    		
    		//Declaring as final allowed access through the functor
    		final View row = inflater.inflate(R.layout.saverow, null);
    		TextView tv_saveRow = (TextView)row.findViewById(R.id.tv_saveRow);
    		
    		tv_saveRow.setText(lists.get(position));
    		
    		//Handles when an item is selected in the view( it lays out the blue backgroupnd and back to 
    			//black based on this 
    		tv_saveRow.setOnTouchListener(new OnTouchListener()
    		{
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
						row.setBackgroundResource(R.drawable.selectbg);
					else if(event.getAction() == MotionEvent.ACTION_UP)
						row.setBackgroundResource(R.drawable.unselectbg);
					else if(event.getAction() == MotionEvent.ACTION_CANCEL)
						row.setBackgroundResource(R.drawable.unselectbg);
					
					
					
					return false;
				}   			
    		});
    		
    		/***
    		 * On click listener for the menu items
    		 */
    		tv_saveRow.setOnClickListener(new OnClickListener()
    		{
				@Override
				public void onClick(View v) 
				{
					TextView tv = (TextView)v;
					et_save.setText("Load " + tv.getText() + "?");	
					et_save.setTextColor(Color.RED);
					selectedView = tv;
					btn_cancel.setText("Cancel");
				}
    			
    		});
    		
    		context.registerForContextMenu(tv_saveRow);
    		
    		return row;
		}
		
	}

}
