package com.DizWARE.ShuffleTone.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.DizWARE.ShuffleTone.Managers.CallManager;

/***
 * Receives all phone states(incoming, off-hook, idle)
 * 
 * Uses the call manager to do all the internal work.
 * @author diz
 */
public class CallReceiver extends BroadcastReceiver 
{
	/***
	 * Receives a phone call and checks for the state
	 */
	@Override
	public void onReceive(Context context, Intent arg1) 
	{	
		CallManager cm = new CallManager(context,"");
		TelephonyManager tele = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		cm.stateChange(tele.getCallState());
	}
}
