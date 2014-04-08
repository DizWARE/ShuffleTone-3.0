package com.DizWARE.ShuffleTone.Others;

import java.util.Calendar;
import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

public class Log extends LinkedList<String>
{
	/**
	 * Serialization number
	 */
	private static final long serialVersionUID = 1L;
	
	SharedPreferences settings;
	Context context;
	int sizeLimit = 100;
	
	static Log log;
	
	/***
	 * Creates our logging mechanism
	 * @param context - Context we are logging from
	 */
	public Log(Context context)
	{
		this.context = context;
		this.settings = context.getSharedPreferences("settings", 0);
		
		setUp();			
	}
	
	/***
	 * Set up the log
	 */
	public synchronized void setUp()
	{
		if(Log.log != null)
			this.addAll(log);
		else
		{
			String[] logArray = {""};
			String printOut = settings.getString("log", "");
			
			if(printOut != null && printOut.contains("\n\n"))
				logArray = printOut.split("\n\n");
			
			for(String entry : logArray)
				this.add(entry + "\n\n");
		}
	}
	
	/***
	 * Updates the log before we destroy this object
	 */
	@Override protected void finalize() throws Throwable
	{
		PreferenceWriter.stringWriter(settings, "log", log.toString());
	}
	
	/***
	 * A debug logging method that adds the time/date, adds our logging data, and then adds it to the log
	 * @param whatToLog
	 */
	public synchronized void d(String whatToLog)
	{
		String newString = DateUtils.formatDateTime(context, 
				Calendar.getInstance().getTimeInMillis(), 
				DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_TIME);
		newString += " - " + whatToLog + "\n\n";
		
		this.addFirst(newString);
		if(this.size() > sizeLimit)
			this.removeLast();		
		
		log = this;
		PreferenceWriter.stringWriter(settings, "log", log.toString());
	}
	
	/***
	 * Posts an error in the log
	 * @param whatToLog
	 */
	public void e(String whatToLog)
	{
		d("Error - " + whatToLog);
	}
	
	/***
	 * Starts a log and logs the data
	 * @param context - Context where is being launched
	 * @param whatToLog - information to log
	 * @return - The created log
	 */
	public static Log d(Context context, String whatToLog)
	{
		if(log == null)
			log = new Log(context);
		
		log.d(whatToLog);
		return log;
	}
	
	/***
	 * Starts a log and logs the data with an error post
	 * @param context - Context where is being launched
	 * @param whatToLog - information to log
	 * @return - The created log
	 */
	public static Log e(Context context, String whatToLog)
	{
		if(log == null)
			log = new Log(context);
		
		log.e(whatToLog);
		return log;
	}
	/***
	 * Prints the log out into one string
	 */
	@Override public String toString()
	{
		String printOut = "";
		for(String entry : this)
			printOut += entry;
		
		return printOut;
	}
}
