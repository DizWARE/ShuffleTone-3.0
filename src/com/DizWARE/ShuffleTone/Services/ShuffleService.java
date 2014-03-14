package com.DizWARE.ShuffleTone.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Activites.MainActivity;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.PlaylistIO;
import com.DizWARE.ShuffleTone.Others.PreferenceWriter;
import com.DizWARE.ShuffleTone.Others.Ringtone;
import com.DizWARE.ShuffleTone.Others.RingtonePlaylist;
import com.DizWARE.ShuffleTone.Others.SettingTags;



/***
 * This service's task is to set the ringtone based on the current Shuffled ringtone
 * 
 * @author Tyler Robinson
 */
public class ShuffleService extends Service implements Runnable
{
	String directory;
	SharedPreferences settings;
	int ringerType;	
	int notification;
	long duration;
	Intent intent;
	RingtonePlaylist playlist;
	Thread shuffleThread;
	
	private static final int NOTE_ID = 5436;
	private static final int FAILED = -1;
	private static final int SUCCESS = 1;
	
	private static final int NOTE_ON = 1;
	private static final int NONE = 0;
	private static final int DEBUG = 2;
	private static final int DEBUG_SOUND = 3;
	
	
	/***
	 * For all devices 2.0 and above. Starts the service;
	 * 
	 * This will gather all the required data to run and then launch a new thread to run the work
	 */
	@Override public int onStartCommand(Intent intent, int flags, int startId) 
	{		
		this.intent = intent;
		
		Log.d("ShuffleTone","Starting Shuffle Service");
		
		Thread thread = new Thread(this);
		thread.start();		
		
		return Service.START_NOT_STICKY;
	}
	
	/***
	 * Handle the destruction of the service
	 */
	@Override public void onDestroy() {
		Log.d("ShuffleTone","Destroying Shuffle Service");
		
		super.onDestroy();
	}
	
	/***
	 * Runs the work of the shuffle service. This is called by a separate thread; Do not call directly
	 */
	@Override public void run() 
	{
		Log.d("ShuffleTone","Thread Started");
		
		boolean checkSettings = intent.getBooleanExtra(SettingTags.checkSettings.toString(), true);
		boolean hasFilename = intent.getBooleanExtra("hasFilename", false);
		
		ringerType = intent.getIntExtra(SettingTags.ringerType.toString(), Constants.TYPE_CALLS);		
		settings = this.getSharedPreferences("settings", 0);
		
		String powerSettings;
		this.notification = settings.getInt("notification", 0);
		
		//Grabs all the type specific data needed to stay independent
		if(hasFilename)
		{
			directory = intent.getStringExtra("filename");
			if(directory.equalsIgnoreCase(Constants.DEFAULT_CALLS))
			{
				ringerType = Constants.TYPE_CALLS;
				powerSettings = Constants.SETTINGS_CALLS_PWR;
			}
			else if(directory.equalsIgnoreCase(Constants.DEFAULT_TEXTS))
			{
				ringerType = Constants.TYPE_TEXTS;
				powerSettings = Constants.SETTINGS_TXT_PWR;
			}
			else
				powerSettings = "None";
		}
		else if(ringerType == Constants.TYPE_CALLS)
		{ 
			directory = Constants.DEFAULT_CALLS; 
			powerSettings = Constants.SETTINGS_CALLS_PWR;
		}
		else 
		{
			directory = Constants.DEFAULT_TEXTS;
			powerSettings = Constants.SETTINGS_TXT_PWR;
		}
		
		Log.d("ShuffleTone","Ringer Type: " + ringerType + " Power Settings: " + powerSettings + " Directory: " + directory);
		Log.d("ShuffleTone","Check Settings: " + checkSettings);
		
		//Checks to see if we should be running this service right now. This includes evaluating the 
			//power settings and the run count
		if(!settings.getBoolean(powerSettings, false)||
		  (checkSettings && !settings.getBoolean(ringerType + SettingTags.useHours.toString(), false) 
			&& !checkCount()))				
			return;
		
		runShuffle();
		
		if((notification == NOTE_ON) && this.ringerType == Constants.TYPE_TEXTS && checkSettings)
			MessageWatch.startService(this, duration);
		
		Intent done = new Intent("com.DizWARE.ShuffleTone.Done");
		this.sendBroadcast(done);
		
	}
	
	/***
	 * Checks to see if the current count is greater than or equal to the max count. If it is than we know we can
	 * shuffle with these settings. This also updates the current count in the preferences
	 * 
	 * @return - True if current >= max, false otherwise.
	 */
	private boolean checkCount()
	{
		int current = settings.getInt(ringerType + SettingTags.currentCount.toString(), 1);
		int max = settings.getInt(ringerType + SettingTags.maxCount.toString(), 1);		
		
		Log.d("ShuffleTone","Checking Count: Current = " + current + " / Max = " + max);
		if(current >= max) {
			PreferenceWriter.intWriter(settings, ringerType + SettingTags.currentCount.toString(), 1);
			return true;
		} else {		
			PreferenceWriter.intWriter(settings, ringerType + SettingTags.currentCount.toString(), current+1);
			return false;
		}
	}
	
	/***
	 * Open the saved playlist; selects and sets the current ringtone and save the playlist
	 */
	private synchronized void runShuffle()
	{
		playlist = PlaylistIO.loadPlaylist(this.getApplicationContext(), directory);
		boolean note_sound = notification==DEBUG_SOUND;
		
		//Load the playlist and waits registers a wait for it to finish
		if(playlist.size() > 0)
		{	
			Log.d("ShuffleTone", "Load Complete: Playlist size = " + playlist.size());
		}
		else
		{
			Log.e("ShuffleTone", "Load failed");
			if(notification >= DEBUG) postNotification(this, note_sound, FAILED, NONE, NONE);
			return;
		}
		
		//Gets the next ringtone and sets it
		Ringtone next = playlist.getCurrent();
		
		if(next != null)
		{
			RingtoneManager.setActualDefaultRingtoneUri(this, ringerType, next.getURI());
			duration = next.getDuration();
			
			Log.d("ShuffleTone", "Set Ringtone " + next.toString());//TODO - DEBUG CODE
		}
		else
		{
			Log.e("ShuffleTone", "Next Ringtone is null. Load failed.");
			if(notification >= DEBUG) postNotification(this, note_sound, SUCCESS, FAILED, NONE);
			return;
		}
		
		//Save the list again
		if(playlist.size() > 0 && PlaylistIO.savePlaylist(this, directory, playlist))
		{
			Log.d("ShuffleTone", "Save successful");
		}
		else
		{
			Log.e("ShuffleTone", "Playlist is empty or save failed. Skipping save");
			if(notification >= DEBUG) postNotification(this, note_sound, SUCCESS, SUCCESS, FAILED);
		}
		
		if(notification >= DEBUG) postNotification(this, note_sound, SUCCESS, SUCCESS, SUCCESS);
	}
	
	/***
	 * Starts the service running in the background of the phone
	 * 
	 * @param context - Context that the service runs
	 * @param checkSettings - True will check against settings before running shuffle, otherwise just does the shuffle
	 * @param ringerType - Type of ringtone we are shuffling
	 */
	public static void startService(Context context, boolean checkSettings,int ringerType)
	{
		Intent service = new Intent();
		service.setClass(context, ShuffleService.class);
		service.putExtra(SettingTags.checkSettings.toString(), checkSettings);
		service.putExtra(SettingTags.ringerType.toString(), ringerType);
		context.startService(service);
	}
	
	/***
	 * Starts the service running in the background of the phone
	 * 
	 * @param context - Context that the service runs
	 * @param checkSettings - True will check against settings before running shuffle, otherwise just does the shuffle
	 * @param filename - The playlist file
	 */
	public static void startService(Context context, boolean checkSettings, String filename)
	{
		Intent service = new Intent();
		service.setClass(context, ShuffleService.class);
		service.putExtra(SettingTags.checkSettings.toString(), checkSettings);
		service.putExtra("hasFilename", true);
		service.putExtra("filename", filename);
		context.startService(service);
	}
	
	/***
	 * Posts a notification for receiving a text message as a fallback when user opts into it
	 * @param context
	 */
	public static void postNotification(Context context)
	{
		Notification notification = new Notification();	
		notification.sound = Settings.System.DEFAULT_NOTIFICATION_URI;
		sendNotification(context, notification, 0);
	}
	
	/***
	 * Post a debugging notification with indicators of ShuffleTone working
	 * @param context - The service context to post the notification
	 * @param loaded - Has the playlist loaded; 0 - no, 1 yes, -1 failed
	 * @param shuffled - Has the playlist updated to the new pointer; 0 - no, 1 yes, -1 failed
	 * @param saved - Has the playlist saved; 0 - no, 1 yes, -1 failed
	 */
	public static void postNotification(Context context, boolean sound, int loaded, int shuffled, int saved)
	{
		Notification notification = new Notification();
		
		//Prepare a notification with the given icon and tucjer text
		notification.icon = R.drawable.ic_launcher;	
		notification.tickerText = "Playlist Loaded: " + getStatusString(loaded) + 
								  " Playlist Shuffled: " + getStatusString(shuffled) +
								  " Playlist saved" + getStatusString(saved);
		
		//Gets our custom layout from the resources
		RemoteViews layout = new RemoteViews(context.getPackageName(), R.layout.debug_notification);
		
		//Sets the image in the given image views
		layout.setImageViewResource(R.id.iv_loaded, getStatusImage(loaded));
		layout.setImageViewResource(R.id.iv_shuffled, getStatusImage(shuffled));
		layout.setImageViewResource(R.id.iv_saved, getStatusImage(saved));
		android.media.Ringtone current = RingtoneManager.getRingtone(context, Settings.System.DEFAULT_NOTIFICATION_URI);
		if(current != null)		
			//Set the text for the ringtone that was sest
			layout.setTextViewText(R.id.tv_current, "Current: " + current.getTitle(context));
		else
			layout.setTextViewText(R.id.tv_current, "Failed to Load");
		
		if(sound)
			notification.sound = Settings.System.DEFAULT_NOTIFICATION_URI;
		//Set our custom layout into the notification
		notification.contentView = layout;
		
		//Create an intent that will open ShuffleTone when the Notification is clicked
		Intent intent = new Intent(context, MainActivity.class);
		notification.contentIntent = PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		//Send the notification
		sendNotification(context, notification, 1);
	}
	
	/***
	 * 
	 * @param status
	 * @return
	 */
	private static int getStatusImage(int status)
	{
		switch(status)
		{
		case FAILED:
			return R.drawable.remove;
		case SUCCESS:
			return R.drawable.selectbox_checked;
		default:
			return R.drawable.selectbox_unchecked;
		}
	}
	
	/***
	 * Get the text status for the shuffle
	 * @param status - a -1, 0, or 1 representing the status of certain 
	 * @return
	 */
	private static String getStatusString(int status)
	{
		switch(status)
		{
		case FAILED:
			return "Failed";
		case SUCCESS:
			return "Success";
		default:
			return "Wait";
		}
	}
	
	/***
	 * Sends a notification to the status bar. This will update a notification if it is already posted
	 * @param context - Context launching this notification
	 * @param notification - The notification to launch or update
	 */
	private static void sendNotification(Context context, Notification notification, int noteType)
	{		
		NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notification.vibrate = new long[]{0};
		manager.notify(NOTE_ID + noteType, notification);
	}
	
	/***
	 * Cancels a posted notification. Does nothing if there isn't one posted
	 * @param context - Context canceling this notification
	 */
	public static void cancelNotification(Context context)
	{
		NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTE_ID);
	}
	
	/***UNUSED REQUIRED METHOD***/
	@Override public IBinder onBind(Intent arg0) { return null; }
}
