package com.DizWARE.ShuffleTone.Receivers;


import com.DizWARE.ShuffleTone.Others.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/***
 * Receiver that catches when the boot has completed. This will restart the shuffle alarms for calls and texts
 * 
 * @author Tyler Robinson
 */
public class StartupReceiver extends BroadcastReceiver {
	@Override public void onReceive(Context context, Intent intent) {		
		AlarmReceiver.startAlarm(context, Constants.TYPE_CALLS);
		AlarmReceiver.startAlarm(context, Constants.TYPE_TEXTS);
	}
}
