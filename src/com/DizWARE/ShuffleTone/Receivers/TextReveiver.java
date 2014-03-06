package com.DizWARE.ShuffleTone.Receivers;

import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Services.ShuffleService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/***
 * Receiver for when a text message or an MMS is received
 * 
 * @author Tyler Robinson
 */
public class TextReveiver extends BroadcastReceiver {
	@Override public void onReceive(Context context, Intent intent) {
		Log.d("ShuffleTone","Text Recieved");
		ShuffleService.startService(context, true, Constants.TYPE_TEXTS);
	}
}
