package com.DizWARE.ShuffleTone.Others;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;

/***
 * This class defines a ringtone. This keeps track of all the 
 * important META data, can play itself, and also is capable 
 * of reading in or parsing itself into XML
 * 
 * @author Tydiz
 *
 */
public class Ringtone implements Comparable<Ringtone>
{
	public static final int CAT_PATH = 0;
	public static final int CAT_ARTIST = 1;
	public static final int CAT_TITLE = 2;
	public static final int CAT_DURATION = 3;
	
	private String artist;
	private String filePath;
	private String title;
	
	private int fileID;
	private long duration;
	
	private Button btn_play;
	
	private boolean isRingtone;
	private boolean isSelected;
	
	private String location;
	
	private Uri uri;
	
	private android.media.Ringtone ringtone;	

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
		
		this.location = "e:";
		
		uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
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
	public Ringtone(String artist, String filePath, String title, int fileID, boolean isRingtone, String location)
	{
		this(artist,filePath,title,fileID,isRingtone);
		
		this.location = location;
		if(location.equalsIgnoreCase("i:"))
			uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
		else if(location.equalsIgnoreCase("d:"))
			uri = Uri.parse("content://drm/audio");
				
	}
	
	/***
	 * Cleans out the string of any illegal letters(for parsing)
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
	 * @param category - Category that ringtone is being sorted by(artist/path/title)
	 * @return - The category title for this ringtone
	 */
	public String getCategory(int category)
	{
		if(category == CAT_PATH)
			return filePath.substring(0, filePath.lastIndexOf("/")+1);

		else 
			return artist;
		
		
	}
	
	/***
	 * Gets the file path of this ringtone
	 * @return - File path of this ringtone
	 */
	public String getPath()
	{
		return filePath;
	}
	
	/***
	 * Gets the artist of this ringtone
	 * @return - Artist name of this ringtone
	 */
	public String getArtist()
	{
		return artist;
	}
	
	/***
	 * Gets the title of this ringtone
	 * @return - Title of this ringtone
	 */
	public String getTitle()
	{
		return title;
	}
	
	/***
	 * Gets the database ID for this ringtone
	 * @return - Database ID of this ringtone
	 */
	public String getFileID()
	{
		return location + fileID;
	}
	
	/***
	 * Gets where the ringtone came from(sd,phone,drm)
	 * @return
	 */
	public String getLocation()
	{
		return location;
	}
	
	/***
	 * Checks to see if this "ringtone" is labeled as a ringtone or if its a music file
	 * @return - true if file is a ringtone
	 */
	public boolean isRingtone()
	{
		return isRingtone;
	}
	
	/***
	 * Creates an XML Snippet that describes this ringtone
	 * @return - XML snippet for this ringtone
	 */
	public String parseRingtone()
	{
		String xml = "<Ringtone>\n" +
					 "<Title>" + title + "</Title>\n" +
					 "<Artist>" + artist + "</Artist>\n" + 
					 "<FilePath>" + filePath + "</FilePath>\n" +
					 "<FileID>" + fileID + "</FileID>\n" + 
					 "<isRingtone>" + isRingtone + "</isRingtone>\n" +
					 "<Location>" + location + "</Location>\n"+
					 "</Ringtone>\n";
		
		return xml;
	}
	
	/***
	 * Creates a ringtone from this XML Document
	 * @param xml - The XML snippet that describes the ringtone we are making
	 * @return - A new ringtone based on the XML snippet
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
					
		return new Ringtone(artist,filePath,title,fileID,isRingtone,location);
	}
	
	/***
	 * Helper method that gets the piece of data from our XML snippet
	 * @param whichSnippet - The tag that we want to read from
	 * @param xml - The XML snippet we are reading from
	 * @return - Return the value in the given tag
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
	
	/***
	 * Gets the file extension of the Ringtone
	 * @return - The file extension
	 */
	public String getFileExtension()
	{
		return filePath.substring(filePath.lastIndexOf(".")+1);
	}
	
	/***
	 * Gets the duration of this ringtone
	 * @return - The duration of this ringtone in milliseconds
	 */
	public long getDuration()
	{
		return duration;
	}
	
	/***
	 * Sets the duration of this ringtone
	 * @param duration - The duration of this ringtone in milliseconds
	 */
	public void setDuration(long duration)
	{
		this.duration = duration;
	}
	
	/***
	 * 
	 * @param play - Button that starts the sound
	 */
	public void setPlay(Button btn_play)
	{
		if(isPlaying())
			btn_play.setText("Stop");
		else
			btn_play.setText("Play");
		
		this.btn_play = btn_play;
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
	 * Defines how to check to see if 
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
	 * @param correct - The ringtone that has the correct id
	 */
	public void fixRingtone(Ringtone correct)
	{
		if(fileID != correct.fileID)
			fileID = correct.fileID;
	}
	
	/***
	 * Creates a natural order for ringtones, which is going to be based on file path
	 */
	@Override
	public int compareTo(Ringtone another) 
	{
		int comp = this.getCategory(CAT_PATH).compareToIgnoreCase(another.getCategory(CAT_PATH));
		
		if(comp == 0)
			return title.compareToIgnoreCase(another.title);
		
		return comp;
	}
		
	/***
	 * Starts playing the ringtone file(plays a default ring if ringtone is invalid)
	 * 
	 * @param context - Context that will play the ringtone
	 */
	public void playRingtone(Context context)
	{
		//TODO - Fix NullPointer bug here. I think this is caused by a null context
		// or possibly a failed grab at getting the ringtone
		
		if(ringtone == null)
			ringtone = RingtoneManager.getRingtone(context, Uri.parse(
					uri + "/" + fileID));

		ringtone.play();
		btn_play.setText("Stop");
	}
	
	/***
	 * Stops playing the ringtone file
	 */
	public void stopRingtone()
	{
		if(ringtone != null)
		{
			ringtone.stop();
			ringtone = null;
			btn_play.setText("Play");			
		}
	}
	
	/***
	 * Gets whether or not this ringtone is selected
	 * @return whether or not this ringtone is selected
	 */
	public boolean isSelected()
	{
		return isSelected;
	}
	
	/***
	 * Set if this ringtone is selected
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
	public boolean isPlaying()
	{
		if(ringtone != null)
			return ringtone.isPlaying();
		
		return false;
	}

}
