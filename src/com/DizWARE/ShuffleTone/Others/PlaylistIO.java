package com.DizWARE.ShuffleTone.Others;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/***
 * This class contains several methods that interacts with the file system, that allows for flattening 
 * and unflattening the RingtonePlaylist object
 * 
 * @author Tyler Robinson
 */
public class PlaylistIO 
{	
	private static boolean locked = false;

	/***
	 * Saves the given playlist to the given filename
	 * None of the given parameters change within the method, so have been marked as final
	 * 
	 * This method starts its own thread and broadcasts an intent:
	 * Action - "saveComplete"
	 * Category - "ShuffleTone"
	 * Intent also puts a boolean under "didSave" if the save was successful
	 *
	 * To know when this method is finished, you must register a receiver for this intent
	 * 
	 * @param context - Context in which the application exists
	 * @param filename - Location of where the file should go
	 * @param playlist - Playlist that is to be saved
	 */
	public static void savePlaylist(final Context context,final String filename,final RingtonePlaylist playlist, final Intent intent)
	{					
		Thread thread = new Thread(new Runnable(){
			@Override public void run() {
				boolean didSave = false;
				while(locked);
				locked = true;
				
				try {
					if(!checkFolder())
						throw new IOException();
					
					Log.d("ShuffleTone", "Save started");//TODO - DEBUG CODE
					
					//Using the magic of serializable objects, flatten the object and save it to our given file
					FileOutputStream out = new FileOutputStream(filename);
					ObjectOutputStream objectStream = new ObjectOutputStream(out);
					objectStream.writeObject(playlist);
					objectStream.close();
					out.close();
					didSave = true;
					locked = false;
				} catch (FileNotFoundException e) {
					Log.e("ShuffleTone", "File location " + filename + " was not found\n" + e.toString());
				} catch (IOException e) {
					Log.e("ShuffleTone", "File " + filename + " could not be saved.\n" + e.toString());
				}

				Log.d("ShuffleTone", "Save complete: " + didSave);//TODO - DEBUG CODE
				
				context.sendBroadcast(intent);
			}});		
		thread.start();
	}
	
	
	/***
	 * Loads a playlist from the file system 
	 * None of the given parameters change within the method, so have been marked as final
	 * 
	 * This method starts its own thread and broadcasts an intent:
	 * Action - "saveComplete"
	 * Category - "ShuffleTone"
	 * The playlist is stored in the intent under "playlist"
	 * Intent also puts a boolean under "didLoad" if the load was successful
	 *
	 * To know when this method is finished, you must register a receiver for this intent
	 * 
	 * @param context - Context in which the application exists
	 * @param filename - Location of where the file is located
	 */
	public static void loadPlaylist(final Context context, final String filename,final RingtonePlaylist playlist, final Intent intent)
	{
		Thread thread = new Thread(new Runnable(){
			@Override public void run() {
				boolean didLoad = false;	
				while(locked);
				locked = true;
				
				try{
					if(!checkFolder())
						throw new IOException();

					Log.d("ShuffleTone", "Preparing to load");//TODO - DEBUG CODE
					
					//With the magic of serializable interface, we unflatten the RingtonePlaylist object
						//at the given file location
					FileInputStream in = new FileInputStream(filename);
					ObjectInputStream objectStream = new ObjectInputStream(in);	
					Log.d("ShuffleTone", "Stream Size: " + in.available() + " bytes");
					playlist.copyPlaylist((RingtonePlaylist)objectStream.readObject()); 
					objectStream.close();
					in.close();
					didLoad = true;
					locked = false;
				} catch (ClassNotFoundException e) { 
					Log.e("ShuffleTone", "Failed to typecast.\n" + e.toString()); 
				} catch (FileNotFoundException e) {
					Log.e("ShuffleTone", "File " + filename + " could not be found.\n" + e.toString()); 
				} catch (StreamCorruptedException e) {
					Log.e("ShuffleTone", "Object Stream is corrupted\n" + e.toString());
				} catch (IOException e) {
					Log.e("ShuffleTone", "Failed to load " + filename +".\n" + e.toString()); 
				}				

				Log.d("ShuffleTone", "Load complete: " + didLoad);//TODO - DEBUG CODE
				intent.putExtra("didLoad", didLoad);
				
				context.sendBroadcast(intent);
			}});		
		thread.start();
	}
	
	public static synchronized boolean savePlaylist(Context context, String filename, RingtonePlaylist playlist)
	{
		boolean didSave = false;
		
		try {
			if(!checkFolder())
				throw new IOException();
			
			Log.d("ShuffleTone", "Save started");//TODO - DEBUG CODE
			
			//Using the magic of serializable objects, flatten the object and save it to our given file
			FileOutputStream out = new FileOutputStream(filename);
			ObjectOutputStream objectStream = new ObjectOutputStream(out);
			objectStream.writeObject(playlist);
			objectStream.close();
			out.close();
			didSave = true;
		} catch (FileNotFoundException e) {
			Log.e("ShuffleTone", "File location " + filename + " was not found\n" + e.toString());
		} catch (IOException e) {
			Log.e("ShuffleTone", "File " + filename + " could not be saved.\n" + e.toString());
		}
		
		Log.d("ShuffleTone", "Save complete: " + didSave);//TODO - DEBUG CODE
		
		return didSave;
	}	
	
	public static synchronized RingtonePlaylist loadPlaylist(Context context, String filename)
	{
		boolean didLoad = false;
		RingtonePlaylist playlist = new RingtonePlaylist();
		
		try{
			if(!checkFolder())
				throw new IOException();

			Log.d("ShuffleTone", "Preparing to load");//TODO - DEBUG CODE
			
			//With the magic of serializable interface, we unflatten the RingtonePlaylist object
				//at the given file location
			FileInputStream in = new FileInputStream(filename);
			ObjectInputStream objectStream = new ObjectInputStream(in);	
			Log.d("ShuffleTone", "Stream Size: " + in.available() + " bytes");
			playlist = (RingtonePlaylist) objectStream.readObject();
			Log.d("ShuffleTone", "Playlist size: " + playlist.size());
			objectStream.close();
			in.close();
			didLoad = true;
		} catch (ClassNotFoundException e) { 
			Log.e("ShuffleTone", "Failed to typecast.\n" + e.toString()); 
		} catch (FileNotFoundException e) {
			Log.e("ShuffleTone", "File " + filename + " could not be found.\n" + e.toString()); 
		} catch (StreamCorruptedException e) {
			Log.e("ShuffleTone", "Object Stream is corrupted\n" + e.toString());
		} catch (IOException e) {
			Log.e("ShuffleTone", "Failed to load " + filename +".\n" + e.toString()); 
		}	
		
		Log.d("ShuffleTone", "Load complete: " + didLoad);//TODO - DEBUG CODE
		return playlist;
	}
	
	/***
	 * Checks to see if the folder is accessible
	 * 
	 * @return True if the file exists and is accessible. False otherwise
	 * @throws IOException - If the file had some problem with saving, this will be thrown
	 */
	private static boolean checkFolder() throws IOException
	{
		//Checks to see if the SD Card is mounted
		if(!android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED))
			return false;		
		
		//Checks if the folder exists
		File folder = new File(Constants.FILE_DIR);
		
		if(!folder.exists())
			folder.mkdir();;
		
		return true;
	}
	
	/***
	 * Checks to see if file access is locked
	 * 
	 * @return - True if the file is being accessed; False otherwise
	 */
	public static boolean isLocked(){	return locked;  }
}