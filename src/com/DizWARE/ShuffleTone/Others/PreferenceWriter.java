package com.DizWARE.ShuffleTone.Others;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/***
 * This class basically shortens the amount of work needed to store stuff
 * within a SharedPreference. When you use SharedPrferences all the time,
 * this is a huge time/code saver
 * 
 * @author Tydiz
 *
 */
public class PreferenceWriter 
{
	/***
	 * Writes the data, whatToStore, under the reference, valueName, into the given
	 * preference; Specifically strings
	 * 
	 * @param pref - Settings file we are trying to add data to
	 * @param valueName - Reference name of the value we are storing
	 * @param whatToStore - Data we are putting in the preference file
	 */
	public static void stringWriter(SharedPreferences pref, String valueName, 
			String whatToStore)
	{
		Editor e = pref.edit();
		e.putString(valueName, whatToStore);
		e.commit();
	}
	
	/***
	 * Writes the data, whatToStore, under the reference, valueName, into the given
	 * preference; Specifically int
	 * 
	 * <version>2</version>
	 * 
	 * @param pref - Settings file we are trying to add data to
	 * @param valueName - Reference name of the value we are storing
	 * @param whatToStore - Data we are putting in the preference file
	 */
	public static void intWriter(SharedPreferences pref, String valueName, 
			int whatToStore)
	{
		Editor e = pref.edit();
		e.putInt(valueName, whatToStore);
		e.commit();
	}
	
	/***
	 * Writes the data, whatToStore, under the reference, valueName, into the given
	 * preference; Specifically float
	 * 
	 * @param pref - Settings file we are trying to add data to
	 * @param valueName - Reference name of the value we are storing
	 * @param whatToStore - Data we are putting in the preference file
	 */
	public static void floatWriter(SharedPreferences pref, String valueName,
			float whatToStore)
	{
		Editor e = pref.edit();
		e.putFloat(valueName, whatToStore);
		e.commit();
	}
	
	/***
	 * Writes the data, whatToStore, under the reference, valueName, into the given
	 * preference; Specifically booleans
	 * 
	 * @param pref - Settings file we are trying to add data to
	 * @param valueName - Reference name of the value we are storing
	 * @param whatToStore - Data we are putting in the preference file
	 */
	public static void booleanWriter(SharedPreferences pref, String valueName, 
			boolean whatToStore)
	{
		Editor e = pref.edit();
		e.putBoolean(valueName, whatToStore);
		e.commit();
	}
	
	/***
	 * Clears the given prefrerence of all its values
	 * 
	 * @param pref - Preference that we want cleared
	 */
	public static void clear(SharedPreferences pref)
	{
		Editor e = pref.edit();
		e.clear();
		e.commit();
	}
}
