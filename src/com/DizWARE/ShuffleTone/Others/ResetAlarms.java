package com.DizWARE.ShuffleTone.Others;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.DizWARE.ShuffleTone.Receivers.AlarmReceiver;
import com.DizWARE.ShuffleTone.Receivers.SMSAlarmReceiver;

/***
 * Resets the alarms for the shuffle.
 * @author Tydiz
 *
 */
public class ResetAlarms 
{
	static SharedPreferences settings;
	static int type;
	
	/***
	 * Sets an alarm for either call shuffle, sms shuffle, or both
	 */
	public static void SetAlarms(Context context,Intent intent,int type)
	{
		ResetAlarms.type = type;
		settings = context.getSharedPreferences("settings", 0);	
		if(type == 0||type == 3)
		{		
			if(settings.getBoolean("useHours", false))
				SetAlarm(new Intent(context, AlarmReceiver.class),context,0,"");		
		}
		if(type == 1|| type == 3) 
		{
			type = 1;
				
			if(settings.getBoolean("smsuseHours", false))
				SetAlarm(new Intent(context, SMSAlarmReceiver.class),context,2000,"sms");		
		}
	}
	
	/***
	 * Sets the alarm with the given intent
	 */
	private static void SetAlarm(Intent intent,Context context,int delay,String writeCode)
	{
		//Starts an alarm for ringtone rotation
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9998+type, intent, 0);
		
		int hour = settings.getInt(writeCode + "numHour", 1);				
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (hour * 3600000) + delay, hour * 3600000, pendingIntent);
	
	}
	
	/***
	 * Cancels the alarm for call shuffle, sms shuffle, or both
	 */
	public static void cancelAlarm(Context context,Intent intent,int type)
	{
		ResetAlarms.type = type;
		if(type == 0||type == 3)
			cancelAlarm(new Intent(context, AlarmReceiver.class),context);
		if(type == 1|| type == 3) 
		{
			type = 1;
			cancelAlarm(new Intent(context, SMSAlarmReceiver.class),context);
		}
	}
	
	/***
	 * Cancels the alarm with the given intent
	 */
	private static void cancelAlarm(Intent intent, Context context)
	{
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9998+type, intent, 0);
							
		alarmManager.cancel(pendingIntent);
	
	}

	
}
