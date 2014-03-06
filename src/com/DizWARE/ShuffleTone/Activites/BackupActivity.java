package com.DizWARE.ShuffleTone.Activites;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ToggleButton;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Others.Actions;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.PlaylistIO;
import com.DizWARE.ShuffleTone.Others.PreferenceWriter;
import com.DizWARE.ShuffleTone.Others.RingtonePlaylist;
import com.DizWARE.ShuffleTone.Others.XMLReader;
import com.DizWARE.ShuffleTone.Services.ShuffleService;

public class BackupActivity extends Activity 
{
	RelativeLayout frameLayout;
	
	RelativeLayout rl_title;
	RelativeLayout rl_content;
	
	ListView lv_restore;
	PlaylistAdapter mAdapter;
	
	SlidingDrawer sd_main;
	DrawerLayout drawer;
	
	ToggleButton tb_calls;
	ToggleButton tb_texts;
	
	RingtonePlaylist mPlaylist;
	
	String mFilepath;
	
	Runnable saveRunnable;
	Runnable loadRunnable;
	
	ProgressDialog progressDialog;
	ViewListDialog viewListDialog;
	AlertDialog mDialog;
	
	Intent saveIntent;
	Intent loadIntent;
	
	BroadcastReceiver mRestoreReceiver;
	ProgressDialog restoreDialog;
	
	/***
	 * Creates this activity.
	 */
	@Override protected void onCreate(Bundle savedInstanceState) 
	{
		mAdapter = new PlaylistAdapter(this);
		
		prepareFrame();
		this.setContentView(frameLayout);
		
		mFilepath = this.getIntent().getStringExtra("filepath");
		
		if(mFilepath.equals(Constants.DEFAULT_CALLS))
			tb_calls.setChecked(true);
		else
			tb_texts.setChecked(true);
		
		askToConvert();
		
		mPlaylist = PlaylistIO.loadPlaylist(this, mFilepath);
				
		saveIntent = new Intent();
		saveIntent.setAction(Actions.SaveComplete.toString());
		
		loadIntent = new Intent();
		loadIntent.setAction(Actions.LoadComplete.toString());
		
		saveRunnable = new Runnable() 
		{					
			@Override public void run() 
			{							
				PlaylistIO.savePlaylist(BackupActivity.this, saveIntent.getStringExtra("filename"), 
						(RingtonePlaylist)saveIntent.getSerializableExtra("playlist"));
				BackupActivity.this.sendBroadcast(saveIntent);
			}
		};
		
		loadRunnable = new Runnable()
		{
			@Override
			public void run() {
				RingtonePlaylist playlist = PlaylistIO.loadPlaylist(BackupActivity.this, loadIntent.getStringExtra("filename"));
				loadIntent.putExtra("playlist", playlist);
				BackupActivity.this.sendBroadcast(loadIntent);				
			}			
		};
		
		super.onCreate(savedInstanceState);
	}
	
	@Override protected void onSaveInstanceState(Bundle outState) 
	{
		if(restoreDialog != null && restoreDialog.isShowing())
		{
			outState.putBoolean("isRestoring", true);
			outState.putInt("progress", restoreDialog.getProgress());
			outState.putInt("max", restoreDialog.getMax());
			outState.putString("message", "Restoring");
		}
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		File folder = new File("/sdcard/ShuffleTone");
		File[] filelist = folder.listFiles(new FilenameFilter() {			
			@Override public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml");
			}
		});
		
		if(savedInstanceState.getBoolean("isRestoring", false) && filelist.length > 0)
		{
			createProgressDialog(savedInstanceState.getInt("progress"),
					savedInstanceState.getInt("max"), savedInstanceState.getString("message"));
			createConvertReceiver();
		}
		else
		{
			mAdapter.refresh();
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override protected void onPause() 
	{
		if(mRestoreReceiver != null) 
		{
			try
			{
				this.unregisterReceiver(mRestoreReceiver);
			}catch(IllegalArgumentException e)
			{
				Log.e("ShuffleTone", "Did not correctly register, or unregister this receiver. Fail silently");
			}
		}
		if(restoreDialog != null && restoreDialog.isShowing()) restoreDialog.cancel();
		if(progressDialog != null && progressDialog.isShowing()) progressDialog.cancel();
		if(viewListDialog != null && viewListDialog.isShowing()) viewListDialog.cancel();
		if(mDialog != null && mDialog.isShowing()) mDialog.cancel();
		super.onPause();
	}
	
	/***
	 * Prepare the frame for the activity and plug in all the GUI values
	 */
	public void prepareFrame()
	{
		prepareContent();
		prepareTitle();
		
		frameLayout = LayoutFrame.createFrame(this.getLayoutInflater(), rl_content, rl_title, R.drawable.slidersettings);
		
		prepareDrawer();
	}
	
	public void prepareDrawer()
	{
		SharedPreferences settings = this.getSharedPreferences("settings", 0);
		boolean addConvertOption = !settings.getBoolean("show_convert_dialog", true);
		
		sd_main = (SlidingDrawer)frameLayout.findViewById(R.id.sd_options);	

		drawer = new DrawerLayout(this.getLayoutInflater(), (FrameLayout)sd_main.getContent());
		
		RelativeLayout drawerLayout;
		
		if(!addConvertOption)
			drawerLayout =(RelativeLayout) this.getLayoutInflater().inflate(R.layout.backup_drawer_default, null);
		else
			drawerLayout =(RelativeLayout) this.getLayoutInflater().inflate(R.layout.backup_drawer_restore, null);
		
		drawer.setContent(drawerLayout);
		
		tb_calls = (ToggleButton)drawerLayout.findViewById(R.id.tb_calls);
		tb_texts = (ToggleButton)drawerLayout.findViewById(R.id.tb_texts);
		
		TextView tv_calls = (TextView)drawerLayout.findViewById(R.id.tv_calls);
		TextView tv_texts = (TextView)drawerLayout.findViewById(R.id.tv_texts);
		
		if(addConvertOption)
		{
			TextView tv_convert = (TextView)drawerLayout.findViewById(R.id.tv_convert);
			tv_convert.setOnClickListener(new OnClickListener() 
			{
				@Override public void onClick(View v) 
				{
					SharedPreferences settings = BackupActivity.this.getSharedPreferences("settings", 0);
					
					File folder = new File("/sdcard/ShuffleTone");
					final File[] filelist = folder.listFiles(new FilenameFilter() {			
						@Override public boolean accept(File dir, String filename) {
							return filename.endsWith(".xml");
						}
					});
					
					convertFiles(filelist);
					
					PreferenceWriter.booleanWriter(settings, "show_convert_dialog", true);
					prepareDrawer();
				}
			});
		}
		
		tv_calls.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				tb_calls.performClick();
			}
		});
		tv_texts.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				tb_texts.performClick();
			}
		});
		
		tb_calls.setTag(tb_texts);
		tb_texts.setTag(tb_calls);
		
		OnClickListener clickListener = new OnClickListener() 
		{
			@Override public void onClick(View v) 
			{
				ToggleButton clicked = (ToggleButton)v;
				ToggleButton other = (ToggleButton)clicked.getTag();
								
				other.setChecked(!clicked.isChecked());
				
				if(mFilepath.equals(Constants.DEFAULT_CALLS))
					mFilepath = Constants.DEFAULT_TEXTS;
				else
					mFilepath = Constants.DEFAULT_CALLS;
				
				IntentFilter filter = new IntentFilter(Actions.LoadComplete.toString());
				loadIntent.putExtra("filename", mFilepath);
				progressDialog = ProgressDialog.show(BackupActivity.this, "Loading...", "Please Wait...");
				
				new Thread(loadRunnable).start();
				
				BackupActivity.this.registerReceiver(new BroadcastReceiver() 
				{
					@Override public void onReceive(Context context, Intent intent) 
					{
						try
						{
							BackupActivity.this.unregisterReceiver(this);
						}catch(IllegalArgumentException e)
						{
							Log.e("ShuffleTone", "Did not correctly register, or unregister this receiver. Fail silently");
						}
						
						mPlaylist = (RingtonePlaylist)loadIntent.getSerializableExtra("playlist");
						
						if(progressDialog != null)
							progressDialog.cancel();
					}
				},filter);
			}
		};
		
		OnCheckedChangeListener checkListener = new OnCheckedChangeListener() {			
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					buttonView.setBackgroundResource(R.drawable.selectbox_checked);
				else
					buttonView.setBackgroundResource(R.drawable.selectbox_unchecked);
			}
		}; 
		
		tb_calls.setOnClickListener(clickListener);
		tb_calls.setOnCheckedChangeListener(checkListener);
		
		tb_texts.setOnClickListener(clickListener);
		tb_texts.setOnCheckedChangeListener(checkListener);
		
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		for(int i = 0; i < drawerLayout.getChildCount(); i++)
			drawerLayout.getChildAt(i).measure(metrics.widthPixels, metrics.heightPixels);
		drawerLayout.measure(metrics.widthPixels, metrics.heightPixels);
		int height = drawerLayout.getMeasuredHeight();
		changeViewHeight(this.sd_main, (int)(height + 70*metrics.density));
		
	}
	
	/***
	 * Ask the user if it is ok to convert their old files or not
	 */
	public void askToConvert()
	{
		File folder = new File("/sdcard/ShuffleTone");
		final File[] filelist = folder.listFiles(new FilenameFilter() {			
			@Override public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml");
			}
		});
		
		final SharedPreferences settings = this.getSharedPreferences("settings", 0);
				
		//If there are files and the user hasn't chose to ignore the prompt, ask user about conversion
		if(settings.getBoolean("show_convert_dialog", true) && filelist.length > 0)
		{
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("You have old playlists");
			builder.setMessage("Would you like me to convert them?");
			
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() 
			{				
				@Override public void onClick(DialogInterface dialog, int which) {
					switch(which)
					{
					case DialogInterface.BUTTON_POSITIVE:
						convertFiles(filelist);
						dialog.cancel();
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						dialog.cancel();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						PreferenceWriter.booleanWriter(settings, "show_convert_dialog", false);
						prepareDrawer();
						dialog.cancel();
						break;
					}
				}
			};
			builder.setPositiveButton("Yes", listener);
			builder.setNeutralButton("No", listener);
			builder.setNegativeButton("Don't Ask Again", listener);
			mDialog = builder.create();
			mDialog.show();
		}
	}
	
	/***
	 * Create the receiver to catch the convertion progress
	 */
	public void createConvertReceiver()
	{
		mRestoreReceiver = new BroadcastReceiver() 
		{			
			@Override public void onReceive(Context context, Intent intent) 
			{
				int progress = intent.getIntExtra("progress", 0);
				int max = intent.getIntExtra("max", 1);
				String message = intent.getStringExtra("message");
				
				if(progress == max)
				{
					restoreDialog.dismiss();
					BackupActivity.this.mAdapter.refresh();
					return;
				}
				restoreDialog.setProgress(progress);
				restoreDialog.setMax(max);
				restoreDialog.setMessage(message);
			}
		};
		
		IntentFilter filter = new IntentFilter("RestoreComplete");
		this.registerReceiver(mRestoreReceiver, filter);
	}
	
	/***
	 * Runs the converstion process
	 * 
	 * @param filelist - Files that need to be converted
	 */
	public void convertFiles(File[] filelist)
	{
		createProgressDialog(0, filelist.length, "Restoring");
		createConvertReceiver();		
		
		Thread thread = new Thread(new Runnable() 
		{			
			@Override public void run() 
			{
				File folder = new File("/sdcard/ShuffleTone");
				File[] filelist = folder.listFiles(new FilenameFilter() {			
					@Override public boolean accept(File dir, String filename) {
						return filename.endsWith(".xml");
					}
				});
				
				Intent intent = new Intent("RestoreComplete");
				intent.putExtra("max", filelist.length);
				for(int i = 0; i < filelist.length; i++)
				{
					intent.putExtra("progress", i+1);
					intent.putExtra("message", "Restoring");
					
					try {
						RingtonePlaylist playlist = new RingtonePlaylist(XMLReader.readFile(filelist[i]));
						String filename = filelist[i].getName();
						
						if(filename.equals(".call_default.xml"))
							filename = "default_calls_old.xml";
						if(filename.equals(".sms_default.xml"))
							filename = "default_texts_old.xml";
						
						filename = filename.replace(".xml", ".shuffle");
						
						playlist.fixAll(BackupActivity.this);
						
						PlaylistIO.savePlaylist(BackupActivity.this, "/sdcard/ShuffleTone/" + filename , playlist);
						
						filelist[i].delete();
					} catch (Exception e) {
						Log.e("ShuffleTone", "Failed to restore " + filelist[i]);
					}
					
					BackupActivity.this.sendBroadcast(intent);					
				}
			}
		});
		thread.start();
	}
	
	/***
	 * Creates a progress dialog to keep track of how far the conversion is
	 * @param progress - Current progress of the dialog
	 * @param max - Maximum number of files to convert
	 * @param message - Message on the dialog
	 */
	public void createProgressDialog(int progress, int max, String message)
	{
		restoreDialog = new ProgressDialog(this);
		restoreDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		restoreDialog.setProgress(progress);
		restoreDialog.setMax(max);
		restoreDialog.setMessage(message);
		restoreDialog.show();
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
	 * Creates the option menu
	 */
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		sd_main.animateOpen();
		return false;
	}
	
	@Override public void onOptionsMenuClosed(Menu menu) {
		sd_main.animateClose();
	}
	
	/***
	 * Inflate the title to apply the controls for both the left and the right side controls
	 */
	public void prepareTitle()
	{
		rl_title = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.title_template, null);
		
		TextView back = (TextView)rl_title.findViewById(R.id.tv_leftCorner);
		TextView viewList = (TextView)rl_title.findViewById(R.id.tv_rightCorner);
		
		
		//Returns to the main screen
		back.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				finish();
			}
		});
		
		//View the list that is selected when launched
		viewList.setOnClickListener(new OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{				
				viewListDialog = new ViewListDialog(BackupActivity.this, mFilepath, "View List", false);
			}
		});
		back.setText("Go Back");
		viewList.setText("View Current");
	}
	
	public void prepareContent()
	{
		rl_content = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.playlist_backup, null);
		
		lv_restore = (ListView)rl_content.findViewById(R.id.lv_restore);
		lv_restore.setAdapter(mAdapter);
		
		prepareContentHeader();		
	}
	
	public void prepareContentHeader()
	{
		View v = this.getLayoutInflater().inflate(R.layout.playlist_backup_header, null);
		
		FrameLayout fl_backup = (FrameLayout)rl_content.findViewById(R.id.fl_backup);
		TextView tv_backup = (TextView)v.findViewById(R.id.tv_name);
		
		OnClickListener backupListener = new OnClickListener() {			
			@Override public void onClick(View v) {
				View dialog = getLayoutInflater().inflate(R.layout.playlist_backup_filename_dialog, null);
				
				AlertDialog.Builder builder = new Builder(BackupActivity.this);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
				{					
					@Override public void onClick(DialogInterface dialog, int which) 
					{
						AlertDialog filenameDialog = (AlertDialog)dialog;
						EditText et_filename = (EditText)filenameDialog.findViewById(R.id.et_filename);
						String finalText = et_filename.getText().toString();
						finalText = finalText.replace("/"," ").replace("\\", " ").replace(".", " ");
						
						saveIntent.putExtra("filename", Constants.FILE_DIR + finalText + Constants.FILE_EXT);
						saveIntent.putExtra("playlist", mPlaylist);
						
						progressDialog = ProgressDialog.show(BackupActivity.this, "Backing Up...", "Please Wait...");
						
						new Thread(saveRunnable).start();
						
						BackupActivity.this.registerReceiver(new BroadcastReceiver() 
						{							
							@Override public void onReceive(Context context, Intent intent) 
							{
								try
								{
									BackupActivity.this.unregisterReceiver(this);
								}catch(IllegalArgumentException e)
								{
									Log.e("ShuffleTone", "Did not correctly register, or unregister this receiver. Fail silently");
								}
								mAdapter.refresh();
								
								if(progressDialog != null) 
									progressDialog.cancel();
							}
						}, new IntentFilter(Actions.SaveComplete.toString()));
						
					}
				});
				builder.setNegativeButton("Cancel", null);
				builder.setTitle("Name Your Playlist");
				builder.setView(dialog);
				builder.setCancelable(true);
				mDialog = builder.create();
				mDialog.show();
			}
		};
		
		tv_backup.setOnClickListener(backupListener);
		fl_backup.addView(v);
	}
	
	public class PlaylistAdapter extends ArrayAdapter<String>
	{
		public PlaylistAdapter(Context context)
		{
			super(context, R.id.tv_name);
			getFilepaths();
		}
		
		private void getFilepaths()
		{
			File folder = new File("/sdcard/ShuffleTone");
			File[] fileList = folder.listFiles();
			String defaultCalls = this.getContext().getString(R.string.default_calls);
			String defaultTexts = this.getContext().getString(R.string.default_texts);
			
			for(int i = 0; i < fileList.length; i++)
			{
				String filename = fileList[i].getName();
				if(filename.endsWith(".shuffle")&&!filename.startsWith(defaultCalls)&&
						!filename.startsWith(defaultTexts))
					this.add(filename.replace(".shuffle", ""));
			}
		}	
		
		public void refresh()
		{
			this.clear();
			getFilepaths();
			notifyDataSetChanged();
		}
		
		@Override public View getView(int position, View convertView, ViewGroup parent) 
		{
			if(convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.playlist_backup_row, null);
			
			TextView tv_name = (TextView)convertView.findViewById(R.id.tv_name);
			tv_name.setText(this.getItem(position));	
			
			Button btn_view = (Button)convertView.findViewById(R.id.btn_view);
			Button btn_load = (Button)convertView.findViewById(R.id.btn_load);			
			ImageButton ib_remove = (ImageButton)convertView.findViewById(R.id.ib_remove);
			
			ib_remove.setTag(position);
			btn_view.setTag(position);
			btn_load.setTag(position);
			
			ib_remove.setOnClickListener(new OnClickListener() {				
				@Override public void onClick(View v) {
					int position = (Integer)v.getTag();
					File file = new File(Constants.FILE_DIR + getItem(position) + Constants.FILE_EXT);
					file.delete();
					mAdapter.refresh();
				}
			});
			
			btn_view.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					int position = (Integer)v.getTag();
					
					final String filename = Constants.FILE_DIR + getItem(position) + Constants.FILE_EXT;
					
					viewListDialog = new ViewListDialog(BackupActivity.this, filename, "View " + getItem(position), false);
				}
			});
			
			btn_load.setOnClickListener(new OnClickListener() 
			{				
				@Override public void onClick(View v)
				{
					int position = (Integer)v.getTag();
					IntentFilter filter = new IntentFilter(Actions.LoadComplete.toString());
					
					loadIntent.putExtra("filename", Constants.FILE_DIR + getItem(position) + Constants.FILE_EXT);
					progressDialog = ProgressDialog.show(BackupActivity.this, "Loading...", "Please Wait...");
					
					new Thread(loadRunnable).start();
					
					BackupActivity.this.registerReceiver(new BroadcastReceiver() 
					{
						@Override public void onReceive(Context context, Intent intent) 
						{
							try
							{
								BackupActivity.this.unregisterReceiver(this);
							}catch(IllegalArgumentException e)
							{
								Log.e("ShuffleTone", "Did not correctly register, or unregister this receiver. Fail silently");
							}
							IntentFilter filter = new IntentFilter(Actions.SaveComplete.toString());
							mPlaylist = (RingtonePlaylist)loadIntent.getSerializableExtra("playlist");
							
							saveIntent.putExtra("filename", mFilepath);
							saveIntent.putExtra("playlist", mPlaylist);
							
							BackupActivity.this.registerReceiver(new BroadcastReceiver() {
								
								@Override public void onReceive(Context context, Intent intent) 
								{
									try
									{
										BackupActivity.this.unregisterReceiver(this);
									}catch(IllegalArgumentException e)
									{
										Log.e("ShuffleTone", "Did not correctly register, or unregister this receiver. Fail silently");
									}
									
									ShuffleService.startService(BackupActivity.this, false, mFilepath);
									if(progressDialog != null) 
										progressDialog.cancel();
								}
							}, filter);
							
							new Thread(saveRunnable).start();
						}						
					}, filter);

				}
			});
			
			return convertView;
		}
	}
}
