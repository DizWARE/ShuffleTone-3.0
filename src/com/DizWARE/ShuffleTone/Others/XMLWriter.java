package com.DizWARE.ShuffleTone.Others;

import java.io.File;
import java.io.IOException;

public class XMLWriter 
{
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
