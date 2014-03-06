package com.DizWARE.ShuffleTone.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

/***
 * Watches for a change in the SMS content. If there is, cancel the ringtone
 * 
 * @author Tyler Robinson
 */
public class MessageWatch extends Service 
{
	SMSObserver observer;
	
	@Override public void onCreate()
	{
		observer = new SMSObserver(new Handler());
        this.getContentResolver().
            registerContentObserver(Uri.parse("content://mms-sms/conversations"), true, observer);
        
		super.onCreate();
	}
	
	/***
	 * Unregisters the content observer
	 */
	@Override public void onDestroy() 
	{
		MessageWatch.this.getContentResolver().unregisterContentObserver(observer);
		super.onDestroy();
	}
	
	
	/*Not used*/
	@Override public IBinder onBind(Intent intent) { return null; }
	
	/***
	 * Starts our message watch service
	 * 
	 * @param context - Context that is launching the service
	 */
	public static void startService(Context context)
	{
		Intent service = new Intent();
		service.setClass(context, MessageWatch.class);
		context.startService(service);
	}

	/***
	 * Observes the SMS inbox for changes
	 * 
	 * @author Tyler Robinson
	 */
	private class SMSObserver extends ContentObserver
	{
		/***
		 * Constructor
		 * @param handler - Not used
		 */
		public SMSObserver(Handler handler) 
		{	super(handler); 	}
		
		
		/***
		 * Called when changes in the inbox are made; Cancels our notification and stops this service
		 */
		@Override public void onChange(boolean selfChange) 
		{
			//getContentResolver().query("content://mms-sms/conversations", projection, selection, selectionArgs, sortOrder)
			ShuffleService.cancelNotification(MessageWatch.this);
			super.onChange(selfChange);
		}
	}
}

