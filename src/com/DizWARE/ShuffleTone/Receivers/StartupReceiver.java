package com.DizWARE.ShuffleTone.Receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.DizWARE.ShuffleTone.Others.ResetAlarms;

/***
 * Receives the system starting up
 * 
 * Basically resets the alarms if the user has alarms set up.
 * @author diz
 */
public class StartupReceiver extends BroadcastReceiver 
{
	SharedPreferences settings;
	
	/***
	 * Receives the startup
	 */
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		ResetAlarms.SetAlarms(context, intent,3);		
	}
}
