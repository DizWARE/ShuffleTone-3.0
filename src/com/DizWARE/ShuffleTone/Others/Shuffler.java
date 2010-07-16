package com.DizWARE.ShuffleTone.Others;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.DizWARE.ShuffleTone.Services.ShuffleService;

/***
 * Bunch of methods that allow for shuffling lists and getting a return of
 * file ids
 * 
 * @author Tydiz
 *
 */
public class Shuffler 
{
	/***
	 * Shuffles the given list and gives back a splittable version of whats inside
	 * @param list - List we are shuffling
	 * @return - A string in a format "7/4/6/2" that can be split by the '/'
	 */
	public static String shuffleList(ArrayList<Ringtone> list)
	{
		Collections.shuffle(list);
		String idList = "";
		
		for(int i = 0; i < list.size(); i++)
			idList += list.get(i).getFileID() + "/";
		
		return idList;
	}
	
	/***
	 * Shuffles a list of strings; Used for shuffling a split string of ID numbers
	 * 
	 * @param list - List of ID Numbers
	 * @return - A string in a format "7/4/6/2" that can be split by the '/'
	 */
	public static String shuffleList(String[] list)
	{
		ArrayList<String> l = new ArrayList<String>();
		for(int i = 0; i < list.length; i++)
			l.add(list[i]);
		
		Collections.shuffle(l);
		
		String idList = "";		
		
		for(int i = 0; i < l.size(); i++)
			idList += l.get(i) + "/";
		
		return idList;
	}
	
	/***
	 * Runs the shuffling service
	 * @param context
	 * @param writeCode
	 * @param ringtones
	 */
	public static void runShuffle(Context context, String writeCode, 
			ArrayList<Ringtone> ringtones)
	{
		SharedPreferences settings = context.getSharedPreferences("settings", 0);
		
		PreferenceWriter.stringWriter(settings, 
				writeCode + "list", Shuffler.shuffleList(ringtones));
		PreferenceWriter.intWriter(settings, writeCode + "index", 0);
		
		Intent service = new Intent(context, ShuffleService.class);		
		service.putExtra("writeCode", writeCode);
		context.startService(service);
	}
	
	/***
	 * Runs the shuffling service
	 * @param context
	 * @param writeCode
	 * @param ringtones
	 */
	public static void runShuffle(Context context, String writeCode, 
			String[] ringtones)
	{
		SharedPreferences settings = context.getSharedPreferences("settings", 0);
		
		PreferenceWriter.stringWriter(settings, 
				writeCode + "list", Shuffler.shuffleList(ringtones));
		PreferenceWriter.intWriter(settings, writeCode + "index", 0);
		
		Intent service = new Intent(context, ShuffleService.class);		
		service.putExtra("writeCode", writeCode);
		context.startService(service);
	}
}
