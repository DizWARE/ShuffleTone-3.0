package com.DizWARE.ShuffleTone.Receivers;

import com.DizWARE.ShuffleTone.Services.ShuffleService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/***
 * Receives alarm declarations for sms.
 * 
 * This is a separate class from AlarmReceiver because of the 
 * write-code. There might be a better way to do this, but
 * this works.
 * 
 * @author diz
 */
public class SMSAlarmReceiver extends BroadcastReceiver 
{
	/***
	 * Called when an alarm goes off
	 */
	@Override
	public void onReceive(Context context, Intent intent) 
	{		
		Intent service;
		service = new Intent(context, ShuffleService.class);
		service.putExtra("writeCode", "sms");
		context.startService(service);
	}

}
