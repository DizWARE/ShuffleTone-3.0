package com.DizWARE.ShuffleTone.Others;

import java.io.Serializable;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

/***
 * This class defines a ringtone. This keeps track of all the 
 * important META data, can play itself, and also is capable 
 * of reading in or parsing itself into XML
 * 
 * @author Tyler Robinson
 *
 */
public class Ringtone implements Comparable<Ringtone>, Serializable
{
	/**
	 * Allows for Serialization to occur
	 */
	private static final long serialVersionUID = -3934096020046550037L;
	
	private String artist;
	private String filePath;
	private String title;
	
	private int fileID;
	private long duration;
	
	private boolean isRingtone;
	private boolean isSelected;
	
	private int location;
	
	private String uri;

	/***
	 * Constructor - Creates a class that allows classification of ringtones
	 * 
	 * @param artist - Music artist of the ringtone
	 * @param filePath - Path in the file system that this ringtone exists
	 * @param title - Name of the ringtone
	 * @param isRingtone - Boolean that expresses if this is a ringtone of a music file
	 */
	public Ringtone(String artist, String filePath, String title, int fileID, boolean isRingtone)
	{
		this.artist = artist;
		this.filePath = filePath;
		this.title = title;
		
		this.fileID = fileID;
		
		this.isRingtone = isRingtone;
		this.isSelected = false;
		
		this.artist = cleanOutString(artist);
		this.filePath = cleanOutString(filePath);
		this.title = cleanOutString(title);
		
		this.location = Constants.LOC_EXTERNAL;
		
		uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
	}
	
	/***
	 * Constructor for a ringtone; takes a location and calls other constructor
	 *
	 * @param artist - Music artist of the ringtone
	 * @param filePath - Path in the file system that this ringtone exists
	 * @param title - Name of the ringtone
	 * @param isRingtone - Boolean that expresses if this is a ringtone of a music file
	  @param location
	 */
	public Ringtone(String artist, String filePath, String title, int fileID, boolean isRingtone, int location)
	{
		this(artist,filePath,title,fileID,isRingtone);
		
		this.location = location;
		if(location == Constants.LOC_INTERNAL)
			uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI.toString();
		else if(location == Constants.LOC_DRM)
			uri = "content://drm/audio";				
	}
	
	/***
	 * Cleans out the string of any illegal letters(for parsing)
	 * 
	 * @param word - Word to be cleaned
	 * @return - The cleaned word
	 */
	private String cleanOutString(String word)
	{
		word = word.replace("<", "");
		word = word.replace("&", "and");
		word = word.replace(">", "");
		
		return word;
	}
	
	/***
	 * Gets the category that this ringtone belongs to based on the sorting that is involved
	 * 
	 * @param category - Category that ringtone is being sorted by(artist/path/title)
	 * @return - The category title for this ringtone
	 */
	public String getCategory(int category)
	{
		if(category == Constants.CAT_PATH)
		{
			return filePath.substring(0, filePath.lastIndexOf("/")+1);
		}
		else if(category == Constants.CAT_DURATION)
		{
			if(this.location == Constants.LOC_DRM)
				return "?";
			long seconds = duration/1000 + 1;
			long minutes = seconds/60;
			
			if(seconds%60 < 10)
				return minutes + ":0" + seconds%60;
			
			return minutes + ":" + (seconds%60);
		}
		else if(category == Constants.CAT_TITLE)
		{
			return title;
		}
		else
		{
			return artist;	
		}
	}
	
	/***
	 * Gets the file path of this ringtone
	 * 
	 * @return - File path of this ringtone
	 */
	public String getPath()
	{
		return filePath;
	}
	
	/***
	 * Gets the directory that contains the ringtone
	 * 
	 * @return The directory path
	 */
	public String getDirectory()
	{
		return filePath.substring(0, filePath.lastIndexOf("/")+1);
	}
	
	/***
	 * Gets the artist of this ringtone
	 * 
	 * @return - Artist name of this ringtone
	 */
	public String getArtist()
	{
		return artist;
	}
	
	/***
	 * Gets the ringtone URI for this file
	 * 
	 * @return - The database URI for the ringtone file
	 */
	public Uri getURI()
	{
		return Uri.parse(uri + "/" + fileID);
	}
	
	/***
	 * Gets the title of this ringtone
	 * 
	 * @return - Title of this ringtone
	 */
	public String getTitle()
	{
		return title;
	}
	
	/***
	 * Gets the database ID for this ringtone
	 * 
	 * @return - Database ID of this ringtone
	 */
	public int getFileID()
	{
		return fileID;
	}
	
	/***
	 * Gets where the ringtone came from(sd,phone,drm)
	 * 
	 * @return An integer that represents the database that the file is in
	 */
	public int getLocation()
	{
		return location;
	}
	
	/***
	 * Checks to see if this "ringtone" is labeled as a ringtone or if its a music file
	 * 
	 * @return - true if file is a ringtone
	 */
	public boolean isRingtone()
	{
		return isRingtone;
	}
	
	/***
	 * Gets the file extension of the Ringtone
	 * 
	 * @return - The file extension
	 */
	public String getFileExtension()
	{
		return filePath.substring(filePath.lastIndexOf(".")+1);
	}
	
	/***
	 * Gets the duration of this ringtone
	 * 
	 * @return - The duration of this ringtone in milliseconds
	 */
	public long getDuration()
	{
		return duration;
	}
	
	/***
	 * Sets the duration of this ringtone
	 * 
	 * @param duration - The duration of this ringtone in milliseconds
	 */
	public void setDuration(long duration)
	{
		this.duration = duration;
	}
	
	/***
	 * Converts this ringtone into a brief file summary
	 */
	@Override
	public String toString() 
	{
		String description = artist + " - " + title + 
							"\nFound in " + filePath;
		return description;
	}
	
	/***
	 * Checks to see if the given object is equal to this ringtone
	 * 
	 * @param other - Other ringtone to compare to
	 * @return - If the filepaths are
	 */
	public boolean equals(Object other)
	{
		Ringtone o = (Ringtone)other;
		return this.filePath.equalsIgnoreCase(o.filePath);		
	}
	
	/***
	 * Fixes the ringtone so that it has the correct file id
	 * 
	 * @param correct - The ringtone that has the correct id
	 */
	public void fixRingtone(Ringtone correct)
	{
		if(fileID != correct.fileID)
			fileID = correct.fileID;
	}
	
	public boolean fixRingtone(Activity activity)
	{
		Cursor cursor;
		String[] proj;
		String where;
		
		boolean drm = location == Constants.LOC_DRM;
		
		if(drm)
		{
			where = MediaStore.Audio.Media.DATA + " = \"" + this.filePath + "\"";
			proj = new String[]{
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media._ID};
		}
		else
		{
			where = MediaStore.Audio.AudioColumns.DATA + " = \"" + this.filePath + "\"";
			proj = new String[]{
				 MediaStore.Audio.AudioColumns.DATA,
				 MediaStore.Audio.AudioColumns._ID,
				 MediaStore.Audio.AudioColumns.DURATION};
		}
		
		cursor = activity.managedQuery(Uri.parse(this.uri), proj, where, null, null);
		
		boolean found = cursor.getCount() > 0;
		if(found)
		{
			cursor.moveToFirst();
			
			this.fileID = cursor.getInt(1);
			
			long d = cursor.getLong(2);
			if(drm) d = 15000;
			this.setDuration(d);
		}
	
		return found;
	}
	
	/***
	 * Creates a natural order for ringtones, which is going to be based on file path
	 */
	@Override
	public int compareTo(Ringtone another) 
	{
		int comp = this.getCategory(Constants.CAT_PATH).compareToIgnoreCase(another.getCategory(Constants.CAT_PATH));
		
		if(comp == 0)
			return title.compareToIgnoreCase(another.title);
		
		return comp;
	}
		
	/***
	 * Starts playing the ringtone file(plays a default ring if ringtone is invalid)
	 * 
	 * @param context - Context that will play the ringtone
	 */
	public MediaPlayer playRingtone(Context context, MediaPlayer player)
	{
		return RingtonePlayer.PlayRingtone(context, this, player);
	}
	
	/***
	 * Play ringtone at the given location
	 * 
	 * @param context - Context that this is running from
	 * @param startLocation - Location to start the ringtone
	 */
	public void playRingtone(Context context, MediaPlayer player, int startLocation)
	{
		player = MediaPlayer.create(context, this.getURI());
		
		if(player == null)
		{
			Log.e(context, "Failed to load ringtone. Ringtone " + this.title + " will not play");
			return;
		}

		player.seekTo(startLocation);
		player.start();
	}
	
	/***
	 * Stops playing the ringtone file
	 */
	public MediaPlayer stopRingtone(MediaPlayer player)
	{
		if(player != null)
		{
			player.stop();
			player.release();
			player = null;		
		}
		
		return player;
	}
	
	/***
	 * Gets whether or not this ringtone is selected
	 * 
	 * @return whether or not this ringtone is selected
	 */
	public boolean isSelected()
	{
		return isSelected;
	}
	
	/***
	 * Set if this ringtone is selected
	 * 
	 * @param isSelected
	 */
	public void setSelect(boolean isSelected)
	{
		this.isSelected = isSelected;
	}
	
	/***
	 * Checks to see if this ringtone is playing
	 * @return - True if it is playing, false otherwise
	 */
	public boolean isPlaying(MediaPlayer player)
	{
		return player != null && player.isPlaying();
	}
	
	/***
	 * Creates a ringtone from this XML Document
	 * @param xml - The XML snippet that describes the ringtone we are making
	 * @return - A new ringtone based on the XML snippet
	 * 
	 * @deprecated - Use the fact that this is serializable
	 */
	public static Ringtone createRingtoneFromXML(StringBuffer xml)
	{
		String title = getXMLSnippet("Title",xml);
		String artist = getXMLSnippet("Artist",xml);
		String filePath = getXMLSnippet("FilePath",xml);
		
		String data = getXMLSnippet("FileID",xml);
		String location = "e:";
		
		int fileID;
		if(!data.equals(""))
			fileID = Integer.parseInt(data);
		else
			fileID = -1;
		
		boolean isRingtone; 
		
		data = getXMLSnippet("isRingtone",xml);
		
		if(!data.equals(""))
			isRingtone = Boolean.parseBoolean(data);
		else
			isRingtone = false;
		
		if(xml.toString().contains("<Location>"))
			location = getXMLSnippet("Location", xml);
		
		int numLocation = Constants.LOC_EXTERNAL;
				
		if(location.equalsIgnoreCase("i:"))
			numLocation = Constants.LOC_INTERNAL;
		else if(location.equalsIgnoreCase("d:"))
			numLocation = Constants.LOC_DRM;
				
		return new Ringtone(artist,filePath,title,fileID,isRingtone,numLocation);
	}
	
	/***
	 * Helper method that gets the piece of data from our XML snippet
	 * @param whichSnippet - The tag that we want to read from
	 * @param xml - The XML snippet we are reading from
	 * @return - Return the value in the given tag
	 * 
	 * @deprecated - Do not scan from xml anymore :/
	 */
	private static String getXMLSnippet(String whichSnippet, StringBuffer xml)
	{		
		String snippet =  xml.substring(
					  xml.indexOf("<" + whichSnippet + ">") + whichSnippet.length() + 2,
					  xml.indexOf("</" + whichSnippet + ">"));				
		
		if(snippet.length() == 0)
			return "";
		
		return snippet;
	}

}
