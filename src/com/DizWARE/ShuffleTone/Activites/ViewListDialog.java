package com.DizWARE.ShuffleTone.Activites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Others.Actions;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.PlaylistIO;
import com.DizWARE.ShuffleTone.Others.Ringtone;
import com.DizWARE.ShuffleTone.Others.RingtonePlaylist;

public class ViewListDialog extends ListDialog<Ringtone> 
{
	private static final long serialVersionUID = -4648130473449044372L;
	TextView tv_ringtoneCount;

	/***
	 * Constructor - Creates a dialog that shows the given list of ringtones
	 * 
	 * @param addTitle - Set true if you want the Add more tones button in this dialog
	 * 
	 * Be sure to create a receiver that catches EditComplete action to retrieve 
	 * the changes to the playlist
	 */
	ViewListDialog(Context context, RingtonePlaylist playlist, boolean addTitle)
	{
		super(context, "View List");	
		
		this.setAdapter(new ViewListAdapter(context, playlist));
		this.setOkHandler(new OnOkClick() {
			@Override public boolean handleClick(ListAdapter adapter) {
				ViewListAdapter ringtoneAdapter = (ViewListAdapter)adapter;
				
				Intent intent = new Intent(Actions.EditComplete.name());
				intent.putExtra("playlist", ringtoneAdapter.getPlaylist());				
				getContext().sendBroadcast(intent);
				return true;
			}			
		});	
		
		PrepareTitle(addTitle);
	}
	
	/***
	 * Constructor - Creates a dialog and displays the playlist that gets loaded. Saves the list again once done
	 * 
	 * @param addTitle - Set true if you want the Add more tones button in this dialog
	 */
	ViewListDialog(final Context context, final String filepath, String title, boolean addTitle)
	{		
		super(context, title, new OnOkClick() {
			@Override public boolean handleClick(ListAdapter adapter) {
				ViewListAdapter ringtoneAdapter = (ViewListAdapter)adapter;
				
				PlaylistIO.savePlaylist(context, filepath, ringtoneAdapter.getPlaylist(), new Intent());
				return true;
			}
		});
		
		this.setAdapter(new ViewListAdapter(context));
		LoadPlaylist(filepath);

		PrepareTitle(addTitle);
	}
	
	@Override protected void onStop() {
		ViewListAdapter adapter = (ViewListAdapter)this.getAdapter();
		
		if(adapter.currentlyPlaying != null) 
			adapter.player = adapter.currentlyPlaying.stopRingtone(adapter.player);
		
		super.onStop();
	}
	
	
	/***
	 * Add the title button to the dialog
	 */
	private void PrepareTitle(boolean addTitle)
	{
		int layoutID = R.layout.view_playlist_title_simple;
		//if(addTitle) layoutID = R.layout.view_playlist_title;
		
		View v = this.getLayoutInflater().inflate(layoutID, null);		
		
		tv_ringtoneCount = (TextView)v.findViewById(R.id.tv_ringtoneCount);
		tv_ringtoneCount.setText("Ringtone Count: " + getAdapter().getCount());
		this.addTitleBar(v);
	}
		
	/***
	 * Loads the playlist with the given filepath. Dialog is updated with this information
	 */
	private void LoadPlaylist(final String filepath)
	{				
		IntentFilter filter = new IntentFilter(Actions.LoadComplete.toString());
		
		getContext().registerReceiver(new BroadcastReceiver() 
		{
			@Override public void onReceive(Context context, Intent intent) {
				try
				{
					context.unregisterReceiver(this);
				}catch(IllegalArgumentException e)
				{
					Log.e("ShuffleTone", "Did not correctly register, or unregister this receiver. Fail silently");
				}
				
				if(!intent.getBooleanExtra("success", false))
				{
					Toast.makeText(getContext(), "Playlist is empty or failed to load", Toast.LENGTH_SHORT).show();
					ViewListDialog.this.cancel();
					return;
				}					
				
				getAdapter().notifyDataSetChanged();
				tv_ringtoneCount.setText("Ringtone Count: " + getAdapter().getCount());
				show();
			}
		}, filter);		
		
		Thread loadThread = new Thread(new Runnable() 
		{					
			@Override public void run() 
			{					
				Intent intent = new Intent(Actions.LoadComplete.toString());		
				RingtonePlaylist playlist = PlaylistIO.loadPlaylist(getContext(), filepath);
				ViewListAdapter adapter = (ViewListAdapter) getAdapter();
				adapter.copy(playlist);
				
				intent.putExtra("success", playlist.size() > 0);
				getContext().sendBroadcast(intent);
			}
		});
		
		loadThread.start();
	}
	
	/***
	 * This class represents a viewing list adapter for ringtones using a ringtone playlist
	 * 
	 * @author Tyler Robinson
	 */
	public class ViewListAdapter extends RingtoneAdapter 
	{
		LayoutInflater mInflater;
		Ringtone currentlyPlaying;
		MediaPlayer player;
		Handler timerHandler;
		Runnable stopRingtone;
		
		public ViewListAdapter(Context context) {
			super(context);
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			timerHandler = new Handler();
			
			stopRingtone = new Runnable() 
			{			
				@Override public void run() 
				{
					if(currentlyPlaying != null && currentlyPlaying.isPlaying(player))
						player = currentlyPlaying.stopRingtone(player);				
				}
			};
		}
		
		/***
		 * Creates an adapter that controls the View List rows
		 * 
		 * @param context - Context that the dialog exists in
		 * @param playlist - Playlist that this dialog represents
		 */
		public ViewListAdapter(Context context, RingtonePlaylist playlist){
			super(context,playlist);
			
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			timerHandler = new Handler();
			
			stopRingtone = new Runnable() 
			{			
				@Override public void run() 
				{
					if(currentlyPlaying != null && currentlyPlaying.isPlaying(player))
						player = currentlyPlaying.stopRingtone(player);				
				}
			};
		}
		
		/***
		 * Removes an item from the list
		 * @param ringtone - Ringtone that is being removed
		 */
		public void removeItem(Ringtone ringtone)
		{
			this.remove(ringtone);
			this.notifyDataSetChanged();
			ViewListDialog.this.tv_ringtoneCount.setText("Ringtone Count: " + getCount());
		}

		/***
		 * Returns the view that represents the given location in the list
		 */
		@Override public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) 
				v = mInflater.inflate(R.layout.view_playlist_row, null);
			
			TextView artist = (TextView)v.findViewById(R.id.tv_artist);
			TextView title = (TextView)v.findViewById(R.id.tv_title);
			TextView duration = (TextView)v.findViewById(R.id.tv_duration);
			TextView location = (TextView)v.findViewById(R.id.tv_location);
			ImageButton remove = (ImageButton)v.findViewById(R.id.ib_remove);		
			
			Ringtone ringtone = this.getItem(position);
			
			artist.setText(ringtone.getCategory(Constants.CAT_ARTIST));
			title.setText(ringtone.getCategory(Constants.CAT_TITLE));
			duration.setText(ringtone.getCategory(Constants.CAT_DURATION));
			location.setText(ringtone.getCategory(Constants.CAT_PATH));
			remove.setTag(ringtone);
			
			v.setTag(ringtone);
			
			remove.setOnClickListener(new View.OnClickListener() 
			{			
				@Override public void onClick(View v) 
				{
					removeItem((Ringtone)v.getTag());
				}
			});
			v.setOnCreateContextMenuListener(new OnCreateContextMenuListener() 
			{			
				@Override public void onCreateContextMenu(ContextMenu menu, View v,
						ContextMenuInfo menuInfo) 
				{
					final Ringtone ringtone = (Ringtone)v.getTag();
					
					menu.setHeaderTitle(ringtone.getTitle());
					MenuItem play = menu.add("Play");
					MenuItem remove = menu.add("Remove");					
					
					play.setOnMenuItemClickListener(new OnMenuItemClickListener() {					
						@Override public boolean onMenuItemClick(MenuItem item) {
							if(currentlyPlaying != null) 
								player = currentlyPlaying.stopRingtone(player);
							currentlyPlaying = ringtone;
							player = currentlyPlaying.playRingtone(getContext(), player);
							
							timerHandler.removeCallbacks(stopRingtone);
							timerHandler.postDelayed(stopRingtone, currentlyPlaying.getDuration());
							
							return true;
						}
					});
					remove.setOnMenuItemClickListener(new OnMenuItemClickListener() {					
						@Override public boolean onMenuItemClick(MenuItem item) {
							removeItem(ringtone);
							return true;
						}
					});
				}
			});
			v.setOnClickListener(new View.OnClickListener() {			
				@Override public void onClick(View v) {
					v.showContextMenu();
				}
			});
			return v;
		}
	}
}
