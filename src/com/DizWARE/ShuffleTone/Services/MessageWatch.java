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
import android.util.Log;

/***
 * Watches for a change in the SMS content. If there is, cancel the ringtone
 * 
 * @author Tyler Robinson
 */
public class MessageWatch extends Service
{
	static int unreadCount = 0;
	static int messageCount = 0;
	
	static long duration = 0;
	
	Timer timer;
	SMSObserver observer;
	
	/***
	 * Creates our observer over the sms conversations content. Runs only when the service is recreated
	 */
	@Override public void onCreate()
	{		
		observer = new SMSObserver(new Handler());
        this.getContentResolver().
            registerContentObserver(Uri.parse("content://mms-sms/conversations"), true, observer);
        
		super.onCreate();
	}
	
	/***
	 * Runs everytime the service is started. Counts how our unread messages. Also constrains the service to only run for a certain amount of time
	 */
	@Override public int onStartCommand(Intent intent, int flags, int startId)
	{
		int tempUnread = unreadCount;
		int tempTotal = messageCount;
		ShuffleService.postNotification(this);
		Log.d("ShuffleTone", "Launching Notification");
		//Counts our messages and unread messages. If our count is the same as what we had before the call, we are probably in the 
			//Conversation that this message belongs to; Stop the ringtone and stop this service if wea are in the conversation
		countUnread();			
		if(tempUnread == unreadCount && tempTotal != messageCount)
		{ ShuffleService.cancelNotification(this); this.stopSelf();}
		
		/***
		 * We want to avoid running too long. This will post a timer for the duration of the ringtone, and kill the service when it dings
		 * The cool thing is that we can have multiple calls but only will cancel the service when we are completely done with running it
		 */
		if(timer != null) timer.cancel();
		timer = new Timer("watch");
		timer.schedule(new TimerTask()
		{			
			@Override public void run()
			{
				MessageWatch.this.stopSelf();			
			}
			
		}, duration);		
		return Service.START_NOT_STICKY;
	}
	
	/***
	 * Unregisters the content observer and cancels our timer
	 */
	@Override public void onDestroy() 
	{
		MessageWatch.this.getContentResolver().unregisterContentObserver(observer);
		timer.cancel();
		
		super.onDestroy();
	}
	
	/***
	 * Counts how many unread messages are in the inbox
	 */
	public void countUnread()
	{
		int i = 0;		
		int j = 0;
		
		//Gets all the unread text messages
		final String[] projection = new String[]{"read"}; 		
		Cursor c = getContentResolver().query(Uri.parse("content://sms/inbox"), projection, null, null, null);
		c.moveToFirst();
		
		//Count the unread and total messages in the inbox 		
		while(c != null && !c.isAfterLast()) 
		{
			if(c.getInt(c.getColumnIndex("read")) == 0)
				i++;
			j++;
			c.moveToNext();
		}
		
		//If one of the unread messages has been read; second condition includes the case if you are already inside the receiving conversation to not ring
		if(i < unreadCount || (i == unreadCount && messageCount != j))
		{
			ShuffleService.cancelNotification(MessageWatch.this);
			this.stopSelf();
		}
		
		unreadCount = i; 
		messageCount = j;
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
		 * Constructs an observer that will watch the sms thread
		 * @param handler - Not used
		 */
		public SMSObserver(Handler handler) 
		{	super(handler); 	}
		
		
		/***
		 * Called when changes in the inbox are made; counts the number of messages and the number of unread in the sms content
		 */
		@Override public void onChange(boolean selfChange) 
		{
			super.onChange(selfChange);
			countUnread();
		}
	}
}

