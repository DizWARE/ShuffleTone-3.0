package com.DizWARE.ShuffleTone.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.DizWARE.ShuffleTone.Others.CallManager;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Services.ShuffleService;

/***
 * Handles shuffling when user receives a phone call. Checks to make sure that the phone
 * is hanging up...if it did then shuffle the ringtone
 * 
 * @author Tyler Robinson
 */
public class CallReceiver extends BroadcastReceiver {
	@Override public void onReceive(Context context, Intent intent) {
		if(CallManager.checkState(intent.getStringExtra(TelephonyManager.EXTRA_STATE)))
			ShuffleService.startService(context,true, Constants.TYPE_CALLS);
	}

}
