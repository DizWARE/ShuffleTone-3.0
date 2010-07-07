package com.DizWARE.ShuffleTone.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.DizWARE.ShuffleTone.Services.ShuffleService;

/***
 * Receiver for MMS Messages. This is a separate intent-filter, hence
 * why we need a separate receiver for it. I'm not sure if you can combine
 * two filters but this works so I kept it.
 * 
 * @author Tydiz
 */
public class MMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		Intent service = new Intent(context,ShuffleService.class);
		service.putExtra("writeCode", "sms");
		context.startService(service);
	}

}
