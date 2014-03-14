package com.DizWARE.ShuffleTone.Others;

import android.content.Context;
import android.media.MediaPlayer;

public class RingtonePlayer 
{
	/***
	 * 
	 * @param context
	 * @param ringtone
	 * @param ringtonePlayer
	 * @return
	 */
	public static MediaPlayer PlayRingtone(Context context, Ringtone ringtone, MediaPlayer ringtonePlayer)
	{
		if(ringtonePlayer != null)
			ringtonePlayer.release();
		
		ringtonePlayer = MediaPlayer.create(context, ringtone.getURI());
		
		if(ringtonePlayer == null)
		{
			Log.e(context, "Failed to load ringtone. Ringtone " + ringtone.getTitle() + " will not play");
			return null;
		}
		
		ringtonePlayer.start();
			
		return ringtonePlayer;		
	}
	
	/***
	 * 
	 * @param context
	 * @param ringtonePlayer
	 */
	public static void StopRingtone(Context context, MediaPlayer ringtonePlayer)
	{
		if(ringtonePlayer == null)
			return;
			
		ringtonePlayer.stop();
		ringtonePlayer.release();
	}
}
