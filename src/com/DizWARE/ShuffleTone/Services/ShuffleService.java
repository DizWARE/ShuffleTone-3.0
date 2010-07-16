package com.DizWARE.ShuffleTone.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;

import com.DizWARE.ShuffleTone.ShuffleMain;
import com.DizWARE.ShuffleTone.Managers.CallManager;
import com.DizWARE.ShuffleTone.Others.PreferenceWriter;
import com.DizWARE.ShuffleTone.Others.Shuffler;

/***
 * Service that does all the cool shuffling stuff.
 * 
 * When called, this will run in the background of whatever is currently
 * running and quickly pick the next item in the list. Once the end of the
 * list has been reached, the list is shuffled and index is set to 0 
 * 
 * This shuffle is attempted to happen after the current tone is played
 * but it is not a guarantee, due to the systems inability to catch 
 * notifications and the lack of tracking abilities that outside apps have
 * with other apps
 * 
 * @author Tydiz
 *
 */
public class ShuffleService extends Service implements Runnable
{
	String writeCode;
	String[] list;
	int index;	
	
	SharedPreferences settings;
	Handler mHandler;
	
	static int totalRuns = 0;
	static int runs = 0;
	
	boolean running = false;

	
	/***
	 * Called when service is started; Sets up the shuffle service
	 */
	@Override
	public void onStart(Intent intent, int startId) 
	{	
		//This was added to sync the timing of this app with the messaging app
			//Before this, if 5 text messages came in, the shuffling would finish 
			//for all 5 before the first text message would come in
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		writeCode = intent.getStringExtra("writeCode");
		settings = this.getSharedPreferences("settings", 0);
		
		CallManager cm = new CallManager(this,"sms");
		
		if(mHandler != null)
			mHandler.removeCallbacks(this);
		
		mHandler = new Handler();
		
		totalRuns++;		
		
		//Stops the service if it is supposed to be off
		if(!settings.getBoolean(writeCode + "power", false))
		{
			this.stopSelf();
			return;
		}
		
		//Ummm...I don't remember why this was added. TODO - Figure this out :)
		//
		//Ahh, figured it out. Basically sets up the message handler, but does it in a seperate
			//thread so that it doesn't interrupt the shuffle service 
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				if(stopRingtone())
				{
	
				}				
			}					
		});
		//Heck yeah I set this to max priority! :) Actually, I had to...it kept getting killed by the system
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		
		//If the shuffle count hasn't been reached(and we are not using a time based shuffle
		if(!settings.getBoolean(writeCode + "useHours", false)&&
				!cm.countDown())
			return;
		
		//Gets the list of ringtones and the position we are in the list
		list = settings.getString(writeCode + "list", "").split("/");
		index = settings.getInt(writeCode + "index", 0);
		int length = list.length;
		
		//If the list is empty or only has 1, then exit the service
		if(length == 1 && list[0].equalsIgnoreCase(""))
			return;
		
		//Resets the index and reshuffles the list once the index goes past list size
		if(index >= length)
		{
			String unsplit = Shuffler.shuffleList(list);
			PreferenceWriter.stringWriter(settings, writeCode + "list", unsplit);
			list = unsplit.split("/");
			index = 0;
		}		
		
		setRingtone();	
		
	}
	
	/***
	 * Sets the new ringtone
	 */
	public void setRingtone()
	{
		
		String[] currentTone = list[index].split(":");
		Uri location; 
		
		//Since the MediaStore is consisting of 3 different locations
			//(Internal, External, and DRM), this gets the correct location 
			//and tags the ringtone index at the end.
		if(currentTone.length > 1)
			location = Uri.parse(getUri(currentTone[0])
				+ "/" + currentTone[1]);
		else
			location = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
					+ "/" + list[index]);
		
		//If this is an SMS Shuffle, set the Notification ringtone, otherwise
			//set the default ringtone
		if(writeCode.equalsIgnoreCase("sms"))
			RingtoneManager.setActualDefaultRingtoneUri(this, 
					RingtoneManager.TYPE_NOTIFICATION, location);
		else
			RingtoneManager.setActualDefaultRingtoneUri(this, 
					RingtoneManager.TYPE_RINGTONE, location);
		
		//Saves where we are in the list of ringtones
		PreferenceWriter.intWriter(settings,writeCode + "index", index+1);		
	}
	
	/***
	 * Gets the URI for the current location(d for drm, i for internal content, e for external content)
	 * @param location - Location of the ringtone that is playing
	 */
	public Uri getUri(String location)
	{
		Uri uri;
		
		if(location.equalsIgnoreCase("d"))
			uri = Uri.parse("content://drm/audio");
		else if(location.equalsIgnoreCase("i"))
			uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
		else 
			uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		
		return uri;
	}
	
	/***
	 * Stops the ringtone with a nasty little hack. Simply it posts a notification that says it has sound but doens't. It
	 * is then instantly canceled causing the sound to stop
	 */
	private boolean stopRingtone()
	{
		//If this setting isn't on then exit
		if(!settings.getBoolean(writeCode + "stop", false)||writeCode.equalsIgnoreCase("sms"))
		{
			return false;
		}
		running = true;
		
		//This says, Post a message to the message handler after a user defined(set up in settings) time span
		mHandler.postDelayed(this, (settings.getInt("smsdelay", 15))*1000);
		
		return true;
	}

	/***
	 * Runs every time the Thread is called.
	 * This is a "Hacky" way of interrupting the current notification
	 * sound after a certain amount of time
	 * 
	 * Basically what it does is it launches a "null" notification.
	 * One without sound or an image or data. The null sound is ignored
	 * by the system(doesn't stop the current ringtone), but is noted as having
	 * a sound. I immediately cancel the notification, which in turn cancels the 
	 * last played sound(which was my null sound, but because it was null, it falls
	 * back to the sound the preceded it. Yeah I know, complicated :/ but it works, except
	 * its unsynced with everything and sometimes happens before the messaging notification 
	 * is shot. I would like to fix this
	 */
	@Override
	public void run() 
	{
		//In case of multiple restarts(if more than one ringtone come in at once)
			//this will make sure the last ringtone is the one that is canceled early
		if(totalRuns != runs+1)
		{
			runs++;
			return;
		}
		
		Notification note = new Notification(0,"",System.currentTimeMillis());
		NotificationManager noteMan = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		//Creates an intent. The flag tells it that it needs to stop all sounds when a new one comes in
		PendingIntent i = PendingIntent.getActivity(this, 0, 
				new Intent(this, ShuffleMain.class), Notification.FLAG_AUTO_CANCEL);		
		
		//Displays the correct information

		//Makes sure that the notification doesn't actually do anything
		note.setLatestEventInfo(this, "Notification hit","Notification hit" , i);
		
		//Post a fake sound
		note.sound = Uri.parse("");
		
		//Notify and then cancel
		noteMan.notify(3377, note);		
		noteMan.cancel(3377);		
		
		totalRuns = 0;
		runs = 0;
		
		running = false;	
	}
	
	/***
	 * SEE...NOT USED! Yuck!
	 */
	@Override
	public IBinder onBind(Intent arg0) { return null; }



	

}
