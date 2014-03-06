package com.DizWARE.ShuffleTone.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.SettingTags;
import com.DizWARE.ShuffleTone.Services.ShuffleService;

/***
 * This class handles creating and handling the alarms(timed shuffles). 
 * 
 * @author Tyler Robinson
 */
public class AlarmReceiver extends BroadcastReceiver 
{
	private static final int ALARM_ID = 0x92;
	
	@Override public void onReceive(Context context, Intent intent) 
	{
		int ringerType = intent.getIntExtra(SettingTags.ringerType.toString(), Constants.TYPE_CALLS);
		
		Log.d("ShuffleTone", "Alarm has gone off for Ringer Type: " + ringerType);//TODO - DEBUG CODE
		
		startAlarm(context,ringerType);
		ShuffleService.startService(context, true, ringerType);
	}
	
	/***
	 * Tells the System to ping the given message after some time
	 * 
	 * @param context - Context that will prepare the message in the system queue
	 */
	public static void startAlarm(Context context, int ringerType) 
	{
		SharedPreferences settings = context.getSharedPreferences("settings", 0);		
		if(!settings.getBoolean(ringerType + SettingTags.useHours.toString(), false)) return;		
		int delay = settings.getInt(ringerType + SettingTags.maxCount.toString(), 1);

		Log.d("ShuffleTone", "Alarm turned on - Goes off in " + delay + " hour(s)");//TODO - DEBUG CODE
		
		AlarmManager alarmManager =	(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() +
				(delay * 3600000), getPendingIntent(context,ringerType));
	}

	/***
	 * Prematurely stops the alarm message that is pending in the system queue
	 * 
	 * @param context - Context that will cancel the message in the system queue
	 * @param pendingIntent - Message that is to be removed from the system queue
	 * (must be exactly the same as what started the alarm message)
	 */
	public static void stopAlarm(Context context, int ringerType) 
	{
		AlarmManager alarmManager =
			(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(getPendingIntent(context, ringerType));
	}

	/***
	 * Gets the pending intent for this class
	 * 
	 * @param context -
	 * @param ringerType -
	 */
	private static PendingIntent getPendingIntent(Context context, int ringerType) 
	{
		Intent broadcast = new Intent();
		broadcast.setClass(context, AlarmReceiver.class);
		broadcast.putExtra(SettingTags.ringerType.toString(), ringerType);
		PendingIntent pendingIntent =
			PendingIntent.getBroadcast(context, ALARM_ID+ringerType, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

}
