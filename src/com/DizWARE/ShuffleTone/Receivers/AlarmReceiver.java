package com.DizWARE.ShuffleTone.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.DizWARE.ShuffleTone.Services.ShuffleService;



/***
 * Receives alarm declarations; 
 * 
 * For example: if I say, I set up a system message to happen in 
 * 1 hour(3600 seconds); this will catch the message that is sent in 1 hour
 * @author diz
 */
public class AlarmReceiver extends BroadcastReceiver 
{
	/***
	 * Called when an alarm goes off
	 */
	@Override
	public void onReceive(Context context, Intent intent) 
	{		
		Intent service;
		service = new Intent(context, ShuffleService.class);		
		service.putExtra("writeCode", "");
		context.startService(service);
	}

}
