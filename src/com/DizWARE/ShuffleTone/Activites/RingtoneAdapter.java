package com.DizWARE.ShuffleTone.Activites;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Others.Ringtone;
import com.DizWARE.ShuffleTone.Others.RingtonePlaylist;

/***
 * This class represents a viewing list adapter for ringtones using a ringtone playlist
 * 
 * @author Tyler Robinson
 */
public abstract class RingtoneAdapter extends ArrayAdapter<Ringtone> implements SectionIndexer
{
	RingtonePlaylist mPlaylist;
	HashMap<String, Integer> alphaIndexer;
    String[] sections;
    
	/***
	 * Creates an adapter with an empty list
	 * 
	 * @param context
	 */
	public RingtoneAdapter(Context context) {
		super(context, R.id.tv_type);
		mPlaylist = new RingtonePlaylist();
	}
	
	/***
	 * Creates an adapter using an already given playlist
	 * @param context
	 * @param playlist
	 */
	public RingtoneAdapter(Context context, RingtonePlaylist playlist)
	{
		super(context, R.id.tv_type);
		mPlaylist = new RingtonePlaylist();
		mPlaylist.copyPlaylist(playlist);
	}
	
	/***
	 * Adds the ringtone onto the playlist
	 * 
	 * NOTE: Requires a notifyDataSetChanged call before changes will be visible
	 */
	@Override public void add(Ringtone object) {
		mPlaylist.add(object);
	}
	
	/***
	 * Adds all the ringtones onto this adapter
	 * @param playlist - Playlist of ringtones
	 */
	public void addAll(RingtonePlaylist playlist){
		mPlaylist.addAll(playlist);
		this.notifyDataSetChanged();
	}
	
	/***
	 * Adds all the ringtones onto this adapter
	 * @param list - List of ringtones
	 */
	public void addAll(ArrayList<Ringtone> list){
		mPlaylist.addAll(list);
		this.notifyDataSetChanged();
	}
	
	/***
	 * Gets the count of items in the adapter
	 */
	@Override public int getCount() {
		return mPlaylist.size();
	}
	
	/***
	 * Clears the playlist
	 */
	@Override public void clear() {
		mPlaylist.getPlaylist().clear();
		notifyDataSetChanged();
	}
	
	/***
	 * Removes the given ringtone from the playlist
	 * 
	 * NOTE: Requires a notifyDataSetChanged call before changes will be visible
	 */
	@Override public void remove(Ringtone object) {
		mPlaylist.remove(object);
	}
	
	/***
	 * Gets the position of the given ringtone
	 */
	@Override public int getPosition(Ringtone item) {
		return mPlaylist.indexOf(item);
	}
	
	/***
	 * Gets the ringtone at the given position
	 */
	@Override public Ringtone getItem(int position) {
		return mPlaylist.getRingtone(position);
	}
	
	/***
	 * Checks to see if the playlist is empty
	 */
	@Override public boolean isEmpty() {
		return getCount() == 0;
	}
	
	/***
	 * Inserts the ringtone at the given position in the playlist
	 * 
	 * NOTE: Requires a notifyDataSetChanged call before changes will be visible
	 */
	@Override public void insert(Ringtone object, int index) {
		mPlaylist.getPlaylist().add(index, object);
	}
	
	/***
	 * Returns the view that represents the given location in the list
	 */
	@Override abstract public View getView(int position, View convertView, ViewGroup parent);
	
	/***
	 * Gets the playlist that is being used for this adapter
	 */
	public RingtonePlaylist getPlaylist()
	{
		return mPlaylist;
	}
	
	/***
	 * Copies the play list into the adapter
	 * @param playlist
	 */
	public void copy(RingtonePlaylist playlist)
	{
		mPlaylist.copyPlaylist(playlist);
		notifyDataSetChanged();
	}

	/***
	 * Sorts the list using a given comparator
	 */
	public void sort(Comparator<? super Ringtone> comparator) {
		mPlaylist.sort(comparator);
		notifyDataSetChanged();
	}	
	
	/***
	 * Sorts the list in natural order
	 */
	public void sort()
	{
		mPlaylist.sort();
		notifyDataSetChanged();
	}
	
	@Override public int getPositionForSection(int section) {
		return 0;
	}
	
	@Override public int getSectionForPosition(int position) {
		return 1;
	}
	
	@Override public Object[] getSections() {		
		return null;
	}
}
