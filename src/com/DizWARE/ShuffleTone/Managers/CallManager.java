package com.DizWARE.ShuffleTone.Managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.DizWARE.ShuffleTone.PreferenceWriter;
import com.DizWARE.ShuffleTone.Services.ShuffleService;

/***
 * Keeps track of current states of calls/sms. This includes
 * counting the number of calls/sms until the next shuffle, 
 * among other things.
 * 
 * Don't let the name deceive you...it isn't for just calls, but the reason
 * it is still named as such, is that it handles the call state, entirely.
 * 
 * @author diz
 */
public class CallManager
{
	Context context;
	SharedPreferences settings;
	String writeCode;
	Intent service;
	
	
	/***
	 * Constructor - Sets up the manager
	 * @param context - Application/context that called this
	 * @param writeCode - Represents which shuffle service we are using
	 */
	public CallManager(Context context, String writeCode)
	{
		service = new Intent(context,ShuffleService.class);
		context.stopService(service);
		
		this.writeCode = writeCode;
		this.context = context;
		settings = context.getSharedPreferences("settings", 0);
	}
	
	/***
	 * Checks to see if the phone was ringing or offhook(talking on the phone) and has just switched to idle
	 * if it has, rotate the ringtone
	 */
	public void stateChange(int state) 
	{						
		//If the ringing state has passed and is returning to idle
		if(settings.getBoolean(writeCode + "calling", false) && 
				state == TelephonyManager.CALL_STATE_IDLE)
		{
			PreferenceWriter.booleanWriter(settings, writeCode + "calling", false);
			//countDown();
			
    		service.putExtra("writeCode", writeCode);
    		context.startService(service);
		}
		//If the phone is ringing, then save that it is
		else if((state == TelephonyManager.CALL_STATE_RINGING))
			PreferenceWriter.booleanWriter(settings, writeCode + "calling", true);				
	}
	
	/***
	 * Checks to see if the shuffle is valid by checking to see if it is shuffling per call
	 * and if the current call is equal to the number of calls the user set up.
	 * @return - True if the calls/sms has reached its max count
	 * 
	 * Note: I screwed up when I added this apparantly. This will fail a shuffle 
	 * any time that calls != numCalls. This is a problem that needs to be addressed
	 * Here's a couple TODO's to remind me to not forget to fix
	 * TODO 
	 * TODO 
	 * TODO
	 */
	public boolean countDown()
	{
		//If the shuffle isn't supposed to be a call shuffle, the return
		if(settings.getBoolean(writeCode + "useHours", false))
			return false;
		
		//Get the current call information
		int calls = settings.getInt(writeCode + "calls", 0);
		int maxCalls = settings.getInt(writeCode + "numCalls", 0);		
		
		//if the call limit has been reached, shuffle the ringtone
		if(calls >= maxCalls)
		{
			PreferenceWriter.intWriter(settings, writeCode + "calls", 0);;
    		
    		//service.putExtra("writeCode", writeCode);
    		//context.startService(service);
			return true;
		}
		//else add one to the calls
		else
		{					
			PreferenceWriter.intWriter(settings, writeCode + "calls", calls+1);
			return false;
		}
	}
	

}

