package com.DizWARE.ShuffleTone.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.DizWARE.ShuffleTone.Services.ShuffleService;

/***
 * Handles when a sms is received. 
 * 
 * @author Tydiz
 *
 */
public class SMSReviever extends BroadcastReceiver {

	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		Intent service = new Intent(context,ShuffleService.class);
		service.putExtra("writeCode", "sms");
		context.startService(service);		
	}

}
