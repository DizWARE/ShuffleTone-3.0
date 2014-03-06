package com.DizWARE.ShuffleTone.Others;

import android.telephony.TelephonyManager;


/***
 * Should handle when the call state changes from, calling or in-call to idle
 * 
 * TODO - TEST THIS CLASS BEFORE CALLING IT OFFICIAL
 */
public class CallManager
{
	/***
	 * Checks to see if the current "change" in phone state, is equal the the IDLE state(no phone calls)
	 */
	public static boolean checkState(String state) 
	{						
		if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE))
			return true;
		
		return false;
	}	

}

