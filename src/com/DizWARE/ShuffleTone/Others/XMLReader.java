package com.DizWARE.ShuffleTone.Others;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Environment;

/***
 * Class that allows reading XML documents that start with <RingtonePlaylist> 
 * 
 * @author Tydiz
 */
public class XMLReader 
{
	private static File SMS_DEFAULT = new File(Environment.getExternalStorageDirectory().getPath()+ 
			"/ShuffleTone/.sms_default.xml");
	private static File CALL_DEFAULT = new File(Environment.getExternalStorageDirectory().getPath()+ 
			"/ShuffleTone/.call_default.xml");
	private static File SMS_CURRENT = new File(Environment.getExternalStorageDirectory().getPath()+ 
			"/ShuffleTone/.sms_current.xml");
	private static File CALL_CURRENT = new File(Environment.getExternalStorageDirectory().getPath()+ 
			"/ShuffleTone/.call_current.xml");
	
	/***
	 * Reads a <RingtonePlaylist> .XML file
	 * 
	 * @param file - File we are reading
	 * @return - List of ringtones that was in the file
	 * @throws Exception - Throws all sorts of exceptions; Just generalized to exception
	 * to avoid 300 catch statements(I hope I have this set up to avoid any of the problems
	 * that the catch statements were required for.
	 */
	@SuppressWarnings("deprecation")
	public static ArrayList<Ringtone> readFile(File file) throws Exception
	{
		if(!android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED))
			throw new IOException();
		
		ArrayList<Ringtone> ringtones = new ArrayList<Ringtone>();
		
		if(!file.exists())
		{
			XMLWriter.createFile(file);
			return ringtones;
		}
		
		//Creates a fast XML reader
		
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xml = factory.newPullParser();
        
        //Reads from the given file
        xml.setInput(new FileReader(file));
         
        int eventType = xml.getEventType();
        StringBuffer currentString = new StringBuffer("");
        
        boolean scanning = false;
        
        //While there is still data
        while(eventType != XmlPullParser.END_DOCUMENT)
        {
        	if(scanning)
        	{
        		//If </Ringtone> or </RingtonePlaylist> is not reached
        		if(!(eventType == XmlPullParser.END_TAG &&
            			(xml.getName().equalsIgnoreCase("Ringtone") ||
            					xml.getName().equalsIgnoreCase("RingtonePlaylist"))))
        		{        			
        			//Gets the current lines beginning tag
        			if(eventType == XmlPullParser.START_TAG)
        				currentString.append("<" + xml.getName() + ">");
        			//Gets the current lines end tag
        			else if(eventType == XmlPullParser.END_TAG)
        				currentString.append("</" + xml.getName() + ">");
        			//Gets the current lines data 
        			else if(eventType == XmlPullParser.TEXT)
        				currentString.append(xml.getText());
        		}
        		else
        		{
        			scanning = false;
                	ringtones.add(Ringtone.createRingtoneFromXML(currentString));
                	currentString = new StringBuffer("");
        		}
        	}
        	//Reading of the start tag <Ringtone>(starts reading to end tag </Ringtone>
        	else if(eventType == XmlPullParser.START_TAG &&
        			xml.getName().equalsIgnoreCase("Ringtone"))
        		scanning = true;       	   	

        	//Go to the next line(try/catch put here so that the app doesn't crash because
        		//of it
        	try{
        		eventType = xml.next();
        	}catch(Exception e)
        	{
        	}
        }
        
		return ringtones;
	}
	
	
	/***
	 * Retrieves the full list of user-selected ringtones
	 * @param writeCode - Tag used to seperate SMS lists and phone call lists
	 * @return - Returns the file that the default list 
	 */
	public static File getDefaultFile(String writeCode)
	{
		if(writeCode.equals(""))
			return CALL_DEFAULT;
		else
			return SMS_DEFAULT;
	}
	
	/***
	 * Retrieves the current list of user-selected ringtones
	 * @param writeCode
	 * @return
	 */
	public static File getCurrentFile(String writeCode)
	{
		if(writeCode.equals(""))
			return CALL_CURRENT;
		else
			return SMS_CURRENT;
	}
}
