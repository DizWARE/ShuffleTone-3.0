package com.DizWARE.ShuffleTone.Others;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class XMLWriter 
{
	/***
	 * Writes a XML file, starting with a <RingtonePlaylist> tag
	 * @param ringtones - List of ringtones to write
	 * @param file - File location that we are writing to
	 * @throws IOException - Throws this exception when there is something wrong with the
	 * file write
	 */
	public static void writeFile(ArrayList<Ringtone> ringtones, File file) 
			throws IOException
	{
		//Handles when there is no SD Card
		if(!android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED))
			throw new IOException();		
		
		//Creates the file if it doesn't exist(or the folders if they doesn't exist either)
		createFile(file);
		
		
		//Writes the data into the file
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("<RingtonePlaylist>\n");
		
		for(int i = 0; i < ringtones.size(); i++)
			writer.write(ringtones.get(i).parseRingtone());
		
		writer.write("</RingtonePlaylist>\n");
		writer.close();
	}
	
	/***
	 * Creates the heirecy for the given file, if it does not exist
	 * @param file - File we are creating
	 */
	public static void createFile(File file)
	{
		while(!file.exists())
		{
			File newFile = new File(file.getAbsolutePath().substring(0, 
					file.getAbsolutePath().lastIndexOf("/")));;
					
			while(!newFile.exists())
				if(!newFile.mkdir())	
					newFile = new File(newFile.getAbsolutePath().substring(0, 
							newFile.getAbsolutePath().lastIndexOf("/")));
			
			try	{ file.createNewFile(); }
			catch(IOException e){}
		}
	}
	

}
