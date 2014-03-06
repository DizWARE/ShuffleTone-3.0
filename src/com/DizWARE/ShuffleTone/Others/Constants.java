package com.DizWARE.ShuffleTone.Others;

import android.media.RingtoneManager;

public final class Constants 
{
	//Ringtone types
	public final static int TYPE_CALLS = RingtoneManager.TYPE_RINGTONE;
	public final static int TYPE_TEXTS = RingtoneManager.TYPE_NOTIFICATION;
	
	//Incase ShuffleTone does its own notification
	public final static int NOTIFICATION_ID = 0x092;
	
	//File locations of the default files
	public final static String DEFAULT_CALLS = "/sdcard/ShuffleTone/.calls_default.shuffle";
	public final static String DEFAULT_TEXTS = "/sdcard/ShuffleTone/.texts_default.shuffle";
	
	//Categories for sorting and filtering ringtones
	public static final int CAT_PATH = 0;
	public static final int CAT_ARTIST = 1;
	public static final int CAT_TITLE = 2;
	public static final int CAT_DURATION = 3;
	
	//Locations of where the media files came from
	public static final int LOC_EXTERNAL = 0;
	public static final int LOC_INTERNAL = 1;
	public static final int LOC_DRM = 2;
	
	//Constants containing the data for saving files
	public static final String FILE_EXT = ".shuffle";
	public static final String FILE_DIR = "/sdcard/ShuffleTone/";
	
	//Constants for settings
	public static final String SETTINGS_CALLS_PWR = "calls_power";
	public static final String SETTINGS_TXT_PWR = "text_power";
	
}
