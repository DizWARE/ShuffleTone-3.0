package com.DizWARE.ShuffleTone;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/***
 * Activity that allows saving playlists(Soon may be merged with LoadActivity)
 * 
 * @author Tydiz
 */
public class SaveActivity extends Activity 
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
		this.setContentView(R.layout.save);
		
		//Checks to see if the SD Card is in
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
		
		et_save.addTextChangedListener(new TextChanged());
		
		writeCode = getIntent().getStringExtra("writeCode");
		
		//Loads the files that current files
		try {
			ringtones = XMLReader.readFile(
					XMLReader.getDefaultFile(writeCode));
		} catch (Exception e) 
		{	
			Toast.makeText(this, "File read failed", Toast.LENGTH_LONG).show();
		}
		
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
		
		//Ok button
		if(btn.getText().toString().equalsIgnoreCase("OK"))
		{
			if(et_save.getText().length() > 0)
				saveList();
			else
				Toast.makeText(this, "No name in text", Toast.LENGTH_SHORT).show();
		}
		//Load button
		else if(btn.getText().toString().equalsIgnoreCase("Load"))
		{			
			if(et_save.getText().length() > 0)
				loadList();
			else
				Toast.makeText(this, "No File to Load", Toast.LENGTH_SHORT).show();			
		}
		
		//Rename button
		else if(btn.getText().toString().equalsIgnoreCase("Rename"))
		{
			renamePlaylist();
		}
		
		//Delete button
		else if(btn.getText().toString().equalsIgnoreCase("Delete"))
		{
			deletePlaylist();
		}
		
		//Cancel button
		else if(btn.getText().toString().equalsIgnoreCase("Exit"))	
			this.finish();
		
		//Resets interface
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
	 * Saves the playlist
	 */
	public void saveList()
	{
		//if there is not a name; exit the save process
		if(et_save.getText().length() == 0)
			return;
		
		String result = "Save Successful";
		try {
			XMLWriter.writeFile(ringtones,
					new File("/sdcard/ShuffleTone/" + 
							et_save.getText().toString() + ".xml"));
		} catch (Exception e) 
		{	
			result = "Save Failed";
		}
		Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
		refresh();
	}
	
	/***
	 * Creates a context menu when the user long presses a menu item
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) 
	{
		menu.add("Load");
		menu.add("Overwrite");
		menu.add("Delete");
		menu.add("Rename");
		
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
		//Overwrite
		if(item.getTitle().toString().equalsIgnoreCase("Overwrite"))
		{
			et_save.setText(selectedView.getText());
			btn_cancel.setText("Cancel");
		}
		else if(item.getTitle().toString().equalsIgnoreCase("Load"))
		{
			et_save.setText("Loading " + selectedView.getText());
			btn_ok.setText("Load");
			btn_cancel.setText("Cancel");
		}
		//Delete
		else if(item.getTitle().toString().equalsIgnoreCase("Delete"))
		{
			//Launch Confirm Dialog
			et_save.setText("");
			et_save.setHint("Delete " + selectedView.getText() + "?");
			btn_ok.setText("Delete");
			btn_cancel.setText("Cancel");
			et_save.setFocusable(false);
			et_save.setHintTextColor(Color.RED);
		}
			
		//Rename
		else if(item.getTitle().toString().equalsIgnoreCase("Rename"))
		{
			ArrayList<Ringtone> list = new ArrayList<Ringtone>(); 
			try {
				list= XMLReader.readFile(
						new File("/sdcard/ShuffleTone/" + selectedView.getText() + ".xml"));
			} catch (Exception e) {	}
						
			btn_ok.setText("Rename");
			btn_ok.setTag(list);		
			btn_cancel.setText("Cancel");
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

		refresh();
	}
	
	/***
	 * Resets the interface to the default setup
	 */
	private void resetButtons()
	{
		btn_ok.setText("OK");
		btn_cancel.setText("Exit");
		et_save.setHint("Give your playlist a name...");
		et_save.setHintTextColor(Color.GRAY);
		et_save.setFocusableInTouchMode(true);
		et_save.setFocusable(true);
		et_save.setText("");
		
		((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(et_save.getWindowToken(), 0);
	}
	
	/***
	 * Renames the selected view
	 */
	@SuppressWarnings("unchecked")
	private void renamePlaylist()
	{
		deletePlaylist();
		
		ArrayList<Ringtone> list = ringtones;
		ringtones = (ArrayList<Ringtone>)btn_ok.getTag();
		
		saveList();
		ringtones = list;	
		
	}
	
	/***
	 * Allows the TextEdits to watch their own text changes
	 * 
	 * @author DizWARE
	 *
	 */
	public class TextChanged implements TextWatcher
	{		
		/***
		 * Prevents invalid information inside the TextEdit
		 */
		@Override
		public void afterTextChanged(Editable text) 
		{
			if(text.length() == 0)
				return;
			
			String word = text.toString();
			char c = text.charAt(text.length()-1);
			
			//If the character is not a letter or a digit
			if(!Character.isLetterOrDigit(c))
			{
				//subtract the last character
				word = word.substring(0, word.length()-1);
				text.clear();
				//and repush it into the text box
				text.append(word);
				
				//If the character is the enter key
				if(Character.valueOf(c) == 10)
				{
					//Save the list if Ok is pressed, or rename if rename is pressed
					if(btn_ok.getText().toString().equalsIgnoreCase("OK"))
						saveList();
					else
						renamePlaylist();
					
					resetButtons();
				}
				else
				{
					//Notifies the user that they put incorrect input
					Toast toast = Toast.makeText(SaveActivity.this,"'" + c + "'" + " is an invalid character", 
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 30);
					toast.show();
				}
			}
					
		}

		//Unused required methods
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {	}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {	}
		
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
		public PlaylistAdapter(Activity context, ArrayList<String> objects) 
		{
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
    		
    		
    		final View row = inflater.inflate(R.layout.saverow, null);
    		TextView tv_saveRow = (TextView)row.findViewById(R.id.tv_saveRow);
    		
    		tv_saveRow.setText(lists.get(position));
    		
    		/***
    		 * Action for that cool background select
    		 */
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
					et_save.setText("Loading " + tv.getText());	
					et_save.setTextColor(Color.RED);
					selectedView = tv;
					btn_ok.setText("Load");
					btn_cancel.setText("Cancel");
				}
    			
    		});
    		
    		context.registerForContextMenu(tv_saveRow);
    		
    		return row;
		}
		
	}

}
