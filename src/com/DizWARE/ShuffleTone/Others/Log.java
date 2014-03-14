package com.DizWARE.ShuffleTone.Others;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.format.DateUtils;

public class Log
{
	SharedPreferences settings;
	Context context;
	public Log(Context context)
	{
		this.context = context;
		settings = context.getSharedPreferences("settings", 0);
		
	}
	
	public void d(String whatToLog)
	{
		String currentLog = settings.getString("log", "");
		String newString = DateUtils.formatDateTime(context, Calendar.getInstance().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_TIME);
		
		newString += " - " + whatToLog + "\n\n" + currentLog;
		
		if(newString.length() > 10000)
			newString = newString.substring(0, 10000);
		
		PreferenceWriter.stringWriter(settings, "log", newString);
	}
	
	public void e(String whatToLog)
	{
		String currentLog = settings.getString("log", "");
		String newString = DateUtils.formatDateTime(context, Calendar.getInstance().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_TIME);
		
		newString += " - Error - " + whatToLog + "\n\n" + currentLog;
		
		if(newString.length() > 10000)
			newString = newString.substring(0, 10000);
		
		PreferenceWriter.stringWriter(settings, "log", newString);
	}
	
	public static Log d(Context context, String whatToLog)
	{
		Log log = new Log(context);
		log.d(whatToLog);
		return log;
	}
	
	public static Log e(Context context, String whatToLog)
	{
		Log log = new Log(context);
		log.e(whatToLog);
		return log;
	}
}
