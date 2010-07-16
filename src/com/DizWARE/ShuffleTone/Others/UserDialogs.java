package com.DizWARE.ShuffleTone.Others;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ScrollView;
import android.widget.TextView;

/***
 * This class creates dialogs, such as the introduction dialogs when app is loaded
 * 
 * Seems like a pretty pointless thing, but this really helps out a lot. Dialogs take
 * quite a bit to set up and format correctly, so using static methods that do the work
 * clears up a lot of confusing code that would otherwise show up in other places
 * 
 * TODO - Fix the tutorial to be a little more clear, or make a video tutorial
 * 
 * @author Tydiz
 */
public class UserDialogs 
{
	Builder builder;
	Activity activity;	

	/***
	 * Dialog series for a new update
	 * @param activity - Context wrapper for these dialogs
	 */
	public static void whatsNew(Activity activity)
	{
		UserDialogs newDialog = new UserDialogs(activity);
		newDialog.createOkDialog("What's New", 
				"Fixed:\n" +
				"-Fixed the file browser bug that caused the system to crash while " +
				"you select ringtones is finally fixed. Thanks goes out to " +
				"\"Ms Tee\" and \"Aubrey\" for helping me get to the bottem of " +
				"that one.\n" +
				"-Fixed the file browser sluggishness. Consider it a 2 birds with" +
				"one stone kinda fix\n" +
				"-Hopefully fixed the early ringtone terminator(ERT :D) for texts. " +
				"Let me know if this didn't fix the problem...I basically put the " +
				"code somewhere where it won't get annihilated instantly by your phone.\n" +
				"Added:\n" +
				"-Different On/Off buttons, to get rid of the confusion. I personally like " +
				"the way it looks in the setting drawer, but not sure if I like the way it " +
				"looks on the main page. Work in progress I guess :)");
		newDialog.builder.show();			
	}
	
	/***
	 * Dialog series for a new user
	 * @param activity - Context wrapper for these dialogs
	 */
	public static void newUser(Activity activity)
	{	
		UserDialogs intro = new UserDialogs(activity);
		intro.welcome();
	}
	
	/***
	 * Dialog series for the tutorial
	 * @param activity - Context wrapper for these dialogs
	 */
	public static void instructions(Activity activity)
	{
		UserDialogs intro = new UserDialogs(activity);
		intro.stepOne();
	}
	
	/***
	 * Creates a dialog that asks the user a Yes/No Question and gives the
	 * user buttons to press
	 * @param activity - Activity that will pop up the dialog
	 * @param message - Message that will be displayed
	 * @param clickListener - Handler for the Yes/No buttons
	 */
	public static void YesNo(Activity activity, String message, OnClickListener clickListener)
	{
		UserDialogs dialog = new UserDialogs(activity);
		dialog.createCustomDialog("Are You Sure", message, "No", "Yes", 
				16, clickListener);
		dialog.builder.show();
		
	}
	
	/***
	 * Creates a dialog for the save/load activities
	 * @param activity - Origin where this was called
	 * @param title - Title for the dialog
	 * @param message - Message you want to say to the user
	 * @return
	 */
	public static Dialog saveLoadDialog(Activity activity, String title, String message)
	{
		UserDialogs dialog = new UserDialogs(activity);
		dialog.createCustomDialog(title, message,16);
		return dialog.builder.show();
	}		
	
	/***
	 * Creates a access point for the user dialogs
	 * @param activity - Activity that will pop up the dialogs
	 */
	private UserDialogs(Activity activity)
	{
		this.activity = activity;
	}
		
	/***
	 * Welcome dialog; Shows a yes/no dialog for the user to choose instructions
	 */
	public void welcome()
	{
		OnClickListener btnListener = new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.cancel();
				if(which == Dialog.BUTTON_POSITIVE)
					stepOne();
				else
					warning();
			}			
		};
		
		createYesDialog("Welcome to ShuffleTone 2.0", 
				"I noticed that this is your first time running ShuffleTone 2.0!\n\n" +
				"There is a lot of new things and a lot of changes made here, " +
				"and may take some instructions to learn.\n It is highly recommended " +
				"to go through the tutorial\n\n" +
				"Would you like to go through the tutorial?", btnListener);
		
		builder.show();
	}
	
	/***
	 * First step in the tutorial; Next/Cancel/Back dialog
	 */
	public void stepOne()
	{
		OnClickListener btnListener = new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{				
				dialog.cancel();
				if(which == Dialog.BUTTON_POSITIVE)
					stepTwo();
				else if(which == Dialog.BUTTON_NEGATIVE)
					stepOne();
				else
					warning();
			}			
		};
		
		createNextDialog("First Step", 
				"First thing you need to note, is that this application has two systems to set up: Calls and texts.\n\n" +
				"Setup is exactly the same for both, but knowing that both need to be set up is very important.\n\n" +
				"To switch between modes, swipe in the direction that the arrows point at the bottom of the app. " +
				"Each one has an option to turn on/off that mode, pick ringtones, save/load lists and set up " +
				"shuffle parameters.\n\nClick next for more." 
				, btnListener);
		builder.show();
	}
	
	/***
	 * Second step in the tutorial; Next/Cancel/Back dialog
	 */
	public void stepTwo()
	{
		OnClickListener btnListener = new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if(which == Dialog.BUTTON_POSITIVE)
					stepThree();
				else if(which == Dialog.BUTTON_NEGATIVE)
					stepOne();
				else
					warning();
			}			
		};
		
		createNextDialog("Second Step", 
				"To turn on a mode, click on the On|Off switch at the top of the app. " +
				"This will turn on shuffling for ONLY this mode(i.e. if you turn on " +
				"the Calls mode, Texts mode will not change its current status).\n\n" +
				"Click next for more", btnListener);
		builder.show();
	}
	
	/***
	 * Third step in the tutorial; Next/Cancel/Back dialog
	 */
	public void stepThree()
	{		
		OnClickListener btnListener = new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if(which == Dialog.BUTTON_POSITIVE)
					stepFour();
				else if(which == Dialog.BUTTON_NEGATIVE)
					stepTwo();
				else
					warning();
			}			
		};
		
		createNextDialog("Third Step", 
				"At the bottom of the app, you will see a 'Setting' tab. If you " +
				"swipe this up, you will see options to switch between shuffling " +
				"every # of calls, or a # of hours. Only one of these can be set at " +
				"a time, and you are welcome to move the slider however you choose.\n\n" +
				"Click Next for more."
				, btnListener);
		builder.show();
	}
	
	/***
	 * Forth step in the tutorial; Next/Cancel/Back dialog
	 */
	public void stepFour()
	{
		OnClickListener btnListener = new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if(which == Dialog.BUTTON_POSITIVE)
					stepFive();
				else if(which == Dialog.BUTTON_NEGATIVE)
					stepThree();
				else
					warning();
			}			
		};
		
		createNextDialog("Fourth Step", 
				"Setting up ringtones is easy. Click the 'Pick out ringtones' " +
				"button, which will take you to a list of all media files on your " +
				"phone. Chances are, this is a lot of files, but luckily you can slide" +
				"up the tab at the bottom, to customize the files you can pick in your list.\n\n" +
				"To select a ringtone, just click the black box on the left, which will " +
				"change to a check mark. Select as many as you want, there are no limits." +
				"Slide out the tab on the right when you are done.\n\n" +
				"Click next to continue."
				, btnListener);
		builder.show();
	}
	
	/***
	 * Five step in the tutorial; Next/Cancel/Back dialog
	 */
	public void stepFive()
	{
		OnClickListener btnListener = new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.cancel();
				if(which == Dialog.BUTTON_NEGATIVE)
					stepFour();
				else
					warning();
			}			
		};
		
		createNextDialog("Last Step", 
				"From here, you are ready to go, but there is still one more feature " +
				"feature that hasn't been mentioned...Saving/Loading playlists.\n\n " +
				"Both buttons take you to similar interfaces, but the functionalities " +
				"are different in each.\n\n In the save interface, just type a name for the " +
				"list, and click 'Ok.'\n\n" +
				"In the load interface, just click the list you want to load and click " +
				"'load.'\n\n" +
				"Click next to finish."
				, btnListener);
		builder.show();
	}
	
	/***
	 * User Warning; notifies of the new notification system
	 */
	public void warning()
	{
		createOkDialog("WARNING!!","The old ShuffleTone required to set your messaging app " +
				"to silent. The new ShuffleTone has new requirements!\n\n" +
				"This time you need to go to your messaging app and " +
				"set the ringtone to \'Default.\' " +
				"In your messaging app, press MENU -> Settings(or Preferences). Scroll " +
				"to Notification settings and click 'Change Ringtone.' Set " +
				"ringtone to Default from here.\n\n" +
				"Note, all apps with the default notification set, will play the current" +
				"Text Tone.\n\n" +
				"Thanks for your understanding :)");
		builder.show();
	}
	
	/***
	 * Creates a Ok dialog
	 * @param title - Title for this dialog
	 * @param message - Message for this dialog
	 */
	private void createOkDialog(String title, String message)
	{
		builder = new Builder(activity);
		builder.setTitle(title);
		
		builder.setView(getTextBox(message));
		builder.setCancelable(false);
		
		builder.setNeutralButton("OK", new OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
			}
		});
	}
	
	/***
	 * Creates a Next/Cancel/Back dialog
	 * @param title - Title for dialog
	 * @param message - Message for dialog
	 * @param btnListener - Handler for buttons
	 */
	private void createNextDialog(String title, String message, OnClickListener btnListener)
	{
		builder = new Builder(activity);
		builder.setTitle(title);
		builder.setView(getTextBox(message));	
		builder.setNegativeButton("Back", btnListener);
		builder.setPositiveButton("Next", btnListener);
		builder.setNeutralButton("Close", btnListener);
		builder.setCancelable(false);
	}
	
	/***
	 * Creates a Yes/No dialog
	 * @param title - Title for dialog
	 * @param message - Message for dialog
	 * @param btnListener - Handler for buttons
	 */
	private void createYesDialog(String title, String message, OnClickListener btnListener)
	{
		builder = new Builder(activity);
		builder.setTitle(title);		
		builder.setView(getTextBox(message));
		builder.setNegativeButton("No, thanks", btnListener);
		builder.setPositiveButton("Yes, please", btnListener);
		builder.setCancelable(false);
	}
	
	/***
	 * Creates a completely custom dialog
	 * @param title - Title of dialog
	 * @param message - Message in dialog
	 * @param button1 - Negative button Text
	 * @param button2 - Positive button text
	 * @param size - Size of message text
	 * @param btnListener - Listens for click action
	 */
	private void createCustomDialog(String title, String message, String button1, 
			String button2,int size,OnClickListener btnListener)
	{
		builder = new Builder(activity);
		builder.setTitle(title);		
		builder.setView(getTextBox(message,size));
		builder.setNegativeButton(button1, btnListener);
		builder.setPositiveButton(button2, btnListener);
		builder.setCancelable(false);
	}
	
	/***
	 * Creates a completely custom dialog
	 * @param title - Title of dialog
	 * @param message - Message in dialog
	 * @param button1 - Negative button Text
	 * @param button2 - Positive button text
	 * @param size - Size of message text
	 * @param btnListener - Listens for click action
	 */
	private void createCustomDialog(String title, String message,int size)
	{
		builder = new Builder(activity);
		builder.setTitle(title);		
		builder.setView(getTextBox(message,size));
		builder.setCancelable(false);
		builder.setIcon(android.R.drawable.ic_menu_save);
	}
	
	/***
	 * Gets a TextView to put inside of the dialog
	 * @param message - Message to go in text view
	 * @return - A text View to be put in dialog boxes
	 */
	private ScrollView getTextBox(String message)
	{
		ScrollView sv_message = new ScrollView(activity);
		TextView tv_message = new TextView(activity);
		tv_message.setText(message);
		tv_message.setGravity(Gravity.FILL);
		tv_message.setTextSize(14);
		tv_message.setTextColor(Color.WHITE);
		tv_message.setPadding(10, 0, 10, 5);
		sv_message.addView(tv_message);
		return sv_message;
	}
	
	/***
	 * Gets a TextView to put inside of the dialog
	 * @param message - Message to go in text view
	 * @param size - Size of text
	 * @return - A text View to be put in dialog boxes
	 */
	private ScrollView getTextBox(String message,int size)
	{
		ScrollView sv_message = new ScrollView(activity);
		TextView tv_message = new TextView(activity);
		tv_message.setText(message);
		tv_message.setGravity(Gravity.FILL);
		tv_message.setTextSize(size);
		tv_message.setTextColor(Color.WHITE);
		tv_message.setPadding(10, 0, 10, 5);
		sv_message.addView(tv_message);
		return sv_message;
	}
}
