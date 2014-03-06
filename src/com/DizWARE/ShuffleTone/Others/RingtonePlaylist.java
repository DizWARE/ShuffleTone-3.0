package com.DizWARE.ShuffleTone.Others;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;

/***
 * Interface for a list of ringtones. Wraps around an arraylist of ringtones...Sorts, shuffles, 
 * and is capable of doing most things that an arraylist can do. It is also marked as serializable which
 * makes it capable of being flattened into bytes 
 * 
 * @author Tyler Robinson
 */
public class RingtonePlaylist implements Serializable, Iterable<Ringtone> 
{
	/**
	 * Allows for Serialization
	 */
	private static final long serialVersionUID = 6371341868227206194L;
	
	private ArrayList<Ringtone> playlist;
	private int current;
	
	/***
	 * Default Constructor - Creates an empty playlist
	 */
	public RingtonePlaylist()
	{
		this.playlist = new ArrayList<Ringtone>();
		this.current = 0;		
	}
	
	/***
	 * Creates a playlist from an already created ArrayList
	 * 
	 * @param playlist - Playlist of ringtones that we are trying to create
	 */
	public RingtonePlaylist(ArrayList<Ringtone> playlist)
	{
		this.playlist = new ArrayList<Ringtone>(playlist);
		this.current = 0;
	}
	
	/***
	 * Copies all internal references from the given playlist to this playlist
	 * 
	 * @param playlist - Playlist of ringtones that we are copying
	 */
	public void copyPlaylist(RingtonePlaylist playlist)
	{
		this.playlist.clear();
		addAll(playlist);
		
		this.current = playlist.current;
	}
	
	/***
	 * Adds a ringtone to the playlist
	 * 
	 * @param ringtone - Ringtone we are adding
	 */
	public void add(Ringtone ringtone)
	{
		playlist.add(ringtone);
	}
	
	/***
	 * Performs an union on these two lists
	 * @param playlist - Playlist to merge with 
	 */
	public void addAll(RingtonePlaylist playlist)
	{
		this.playlist.addAll(playlist.getPlaylist());
	}
	
	/***
	 * Adds a full list into this playlist
	 * @param list - list of ringtones to add
	 */
	public void addAll(ArrayList<Ringtone> list)
	{
		this.playlist.addAll(list);
	}
	
	public void fixAll(Activity activity)
	{
		for(Ringtone ringtone : new RingtonePlaylist(playlist))
		{
			if(!ringtone.fixRingtone(activity))
				this.remove(ringtone);
		}
	}
	
	/***
	 * Removes a ringtone from the playlist
	 * 
	 * @param ringtone - Ringtone we are removing
	 */
	public void remove(Ringtone ringtone)
	{
		playlist.remove(ringtone);
	}
	
	/***
	 * Gets the index of the given ringtone within the list of ringtones
	 * 
	 * @param ringtone - Ringtone we are looking for
	 * @return The index of the given ringtone
	 */
	public int indexOf(Ringtone ringtone)
	{
		return playlist.indexOf(ringtone);
	}
	
	/***
	 * Gets the ringtone at the given index
	 * 
	 * @param index - Index of the ringtone we want
	 * @return The ringtone at the given index
	 */
	public Ringtone getRingtone(int index)
	{
		if(index >= size())
			return null;
		
		return playlist.get(index);
	}
	
	/***
	 * Checks to see if the playlist contains the given ringtone
	 * 
	 * @param ringtone - Ringtone we are checking
	 * @return Returns true if the list contains the ringtone, false otherwise
	 */
	public boolean contains(Ringtone ringtone)
	{
		return playlist.contains(ringtone);
	}
	
	/***
	 * Gets the size of the playlist
	 * 
	 * @return The size of the playlist
	 */
	public int size()
	{
		return playlist.size();
	}
	
	
	/***
	 * Gets the current ringtone in the list and increments the pointer
	 * 
	 * @return returns the current ringtone
	 */
	public Ringtone getCurrent()
	{
		if(current >= playlist.size())
			shuffle();
		
		return getRingtone(current++);
	}
	
	/***
	 * Returns a playlist that is filtered based on the filter requirements
	 * @param fliters - List of filters that will restrict our playlist
	 * @return - A restricted playlist containing the given requirements
	 */
	public RingtonePlaylist filter(List<FilterType> filters)
	{
		if(filters.isEmpty()) return this;
		
		RingtonePlaylist playlist = new RingtonePlaylist();
		
		//Finds all the ringtones that matches the filter
		for(Ringtone ringtone : this.playlist)
			for(FilterType filter : filters)
				if(ringtone.getCategory(filter.category).equals(filter.filter))
				{ playlist.add(ringtone); break; }
		
		return playlist;
	}
	
	/***
	 * Gets all the different filters in the given category
	 * @param category - Category of information we want to get filters for
	 * @return - A list of Filters
	 */
	public List<FilterType> getFilters(int category)
	{
		ArrayList<FilterType> filters = new ArrayList<FilterType>();
		
		for(Ringtone ringtone : this.playlist)
		{
			FilterType filter = new FilterType(category, ringtone.getCategory(category));
			if(!filters.contains(filter))
				filters.add(filter);
		}
		
		return filters;
	}
	
	/***
	 * Checks to see if all the ringtones are selected
	 * 
	 * @return - true if they are
	 */
	public boolean isAllSelected()
	{
		return getSelected().size() == this.size();
	}
	
	/***
	 * Gets all the selected ringtones
	 */
	public RingtonePlaylist getSelected()
	{
		RingtonePlaylist selected = new RingtonePlaylist(); 
		for(Ringtone ringtone : this.playlist)
			if(ringtone.isSelected()) selected.add(ringtone);
		
		return selected;
	}
	
	/***
	 * Selects all the ringtones in the playlist
	 */
	public void SelectAll()
	{
		for(Ringtone ringtone : this.playlist)
			ringtone.setSelect(true);
	}
	
	/***
	 * Selects all the ringtones in the given list, if they can be found in this list,
	 * they will be set to selected
	 * 
	 * @param list - List of ringtones we want to select in this list
	 */
	public void SelectRingtones(RingtonePlaylist list)
	{
		for(Ringtone ringtone : list)
		{
			int index = this.indexOf(ringtone);
			
			if(index >= 0)
				this.playlist.get(index).setSelect(true);
		}
	}
	
	/***
	 * Deselects all the ringtones in the playlist
	 */
	public void DeselectAll()
	{
		for(Ringtone ringtone : this.playlist)
			ringtone.setSelect(false);
	}
	
	/***
	 * Shuffles the playlist
	 */
	public void shuffle()
	{
		Collections.shuffle(playlist);
		current = 0;
	}
	
	/***
	 * Sorts the list into natural order
	 */
	public void sort()
	{
		Collections.sort(playlist);
	}
	
	/***
	 * Sorts the list based on the comparator
	 * 
	 * @param comparator - Comparator that will define the order of our sort
	 */
	public void sort(Comparator<? super Ringtone> comparator)
	{
		Collections.sort(playlist, comparator);
	}
	
	/***
	 * Gets the playlist of ringtones as an ArrayList
	 * 
	 * @return - List of ringtones that were in this playlist
	 */
	public ArrayList<Ringtone> getPlaylist()
	{
		return playlist;
	}

	/***
	 * Gets the iterator for the playlist. Allows for easy for-each statements
	 */
	@Override public Iterator<Ringtone> iterator() 
	{
		return playlist.iterator();
	}
}
