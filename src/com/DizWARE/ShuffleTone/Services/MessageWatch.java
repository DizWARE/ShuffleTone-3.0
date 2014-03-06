package com.DizWARE.ShuffleTone.Services;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
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
	static int unreadCount = 0;
	static long duration = 0;
	SMSObserver observer;
	
	/***
	 * Creates our observer over the sms conversations content. Gets our initial unread count
	 */
	@Override public void onCreate()
	{		
		observer = new SMSObserver(new Handler());
        this.getContentResolver().
            registerContentObserver(Uri.parse("content://mms-sms/conversations"), true, observer);
        countUnread();
        
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
	
	/***
	 * Counts how many unread messages are in the inbox
	 */
	public void countUnread()
	{
		int i = 0;		
		
		//Gets all the unread text messages
		final String[] projection = new String[]{"read"}; 		
		Cursor c = getContentResolver().query(Uri.parse("content://mms-sms/conversations?simple=true"), projection, "read = 0", null, null);
		c.moveToFirst();
		
		//Count the unread messages. 		
		while(c !=null && !c.isAfterLast()) 
		{
			i++;
			c.moveToNext();
		}
		
		//If one of the unread messages has been read, then cancel our notification. Also if the notification is 0
		if(i < unreadCount || i == 0)
		{
			ShuffleService.cancelNotification(MessageWatch.this);
			this.stopSelf();
		}
		unreadCount = i;
	}
	
	
	/*Not used*/
	@Override public IBinder onBind(Intent intent) { return null; }
	
	/***
	 * Starts our message watch service
	 * 
	 * @param context - Context that is launching the service
	 */
	public static void startService(Context context, long duration)
	{
		Intent service = new Intent();
		MessageWatch.duration = duration;
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
			super.onChange(selfChange);
			countUnread();
		}
	}
}

