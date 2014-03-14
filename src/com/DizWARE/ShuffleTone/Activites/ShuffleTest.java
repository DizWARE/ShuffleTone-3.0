package com.DizWARE.ShuffleTone.Activites;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.PreferenceWriter;
import com.DizWARE.ShuffleTone.Services.ShuffleService;

/***
 * A UI class with options to test the applications ability to handle TextTones
 * 
 * @author Tyler Robinson
 */
public class ShuffleTest extends Activity implements Runnable
{
	TextView tv_current;
	TextView tv_old;
	TextView tv_new;
	TextView tv_log;
	
	Button btn_shuffle;
	Button btn_message;
	
	ToggleButton tb_power;
	ToggleButton tb_notification;
	ToggleButton tb_debugNote;
	
	SharedPreferences settings;
	
	String oldTitle = "";
	String newTitle = "";
	
	BroadcastReceiver doneReceiver;
	BroadcastReceiver logReceiver;
	
	/***
	 * Creates the UI for this activity
	 */
	@Override protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.shuffle_test);
		
		//Initialize TextViews
		tv_current = (TextView)this.findViewById(R.id.tv_current);
		tv_new = (TextView)this.findViewById(R.id.tv_new);
		tv_old = (TextView)this.findViewById(R.id.tv_old);
		tv_log = (TextView)this.findViewById(R.id.tv_log);
		
		//Initialize Buttons
		btn_shuffle = (Button)this.findViewById(R.id.btn_shuffle);
		btn_message = (Button)this.findViewById(R.id.btn_message);
		
		//Initialize Toggle Buttons
		tb_notification = (ToggleButton)this.findViewById(R.id.tb_notification);
		tb_power = (ToggleButton)this.findViewById(R.id.tb_power);
		tb_debugNote = (ToggleButton)this.findViewById(R.id.tb_debugNote);
		
		//Get our application settings
		settings = this.getSharedPreferences("settings", 0);
		
		//Retrieve initial ringtone info
		Thread t = new Thread(this);
		t.run();
		tv_current.setText(oldTitle);
		
		/***
		 * Creates a message receiver that will receive the message from the ShuffleService when it is finished
		 */
		doneReceiver = new BroadcastReceiver() 
		{
			@Override public void onReceive(Context context, Intent intent) 
			{
				//Retrieve new ringtone info
				Thread t = new Thread(ShuffleTest.this);
				t.run();
				
				while(t.isAlive());
				tv_current.setText(newTitle);
				tv_new.setText(newTitle);
				
				Intent logUpdate = new Intent("com.DizWARE.ShuffleTone.Log.Update");
				sendBroadcast(logUpdate);
			}
		};		
		this.registerReceiver(doneReceiver, new IntentFilter("com.DizWARE.ShuffleTone.Done"));
		
		/***
		 * Runs the Shuffle Service when user clicks the button
		 */
		btn_shuffle.setOnClickListener(new OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{
				ShuffleService.startService(ShuffleTest.this, false, Constants.TYPE_TEXTS);
				
				//Switch the new ringtone to current, since it will be the previous ringtone when the new one is fetched
				if(newTitle != "") tv_old.setText(newTitle);
				else tv_old.setText(oldTitle);
				tv_new.setText("Retrieving...");
				
			}
		});	
		
		/***
		 * Sets the click action of the button to open a sms compose window with the users phone number and a test message
		 */
		btn_message.setOnClickListener(new OnClickListener()
		{			
			@Override public void onClick(View v)
			{
				TelephonyManager phoneManager = (TelephonyManager)ShuffleTest.this.getSystemService(Context.TELEPHONY_SERVICE);
				String phoneNumber = phoneManager.getLine1Number();				
				Uri sms_uri = Uri.parse("smsto:"+phoneNumber); 
				
		        Intent composeMessage = new Intent(Intent.ACTION_SENDTO, sms_uri); 
		        composeMessage.putExtra("sms_body", "This is a Test Message from ShuffleTone"); 
		        startActivity(composeMessage); 
			}
		});
		
		/***
		 * Turns on or off the TextTone override feature. 
		 */
		tb_notification.setOnCheckedChangeListener(new OnCheckBoxChangedListener()
		{
			@Override public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
			{
				int value = 0;
				if(isChecked && tb_debugNote.isChecked()) 
					value = 3;
				else if(isChecked) 
					value = 1;
				else if(tb_debugNote.isChecked()) 
					value = 2;
				else 
					value = 0;
				
				PreferenceWriter.intWriter(settings, "notification", value);
				super.onCheckedChanged(buttonView, isChecked);
			}
		});
		
		tb_debugNote.setOnCheckedChangeListener(new OnCheckBoxChangedListener()
		{
			@Override public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
			{
				int value = 0;
				if(isChecked && tb_notification.isChecked()) 
					value = 3;
				else if(isChecked) 
					value = 2;
				else if(tb_notification.isChecked()) 
					value = 1;
				else 
					value = 0;
				
				PreferenceWriter.intWriter(settings, "notification", value);
				super.onCheckedChanged(buttonView, isChecked);
			}
		});
		
		/***
		 * Turns on or off the TextTone override feature. 
		 */
		tb_power.setOnCheckedChangeListener(new OnCheckBoxChangedListener()
		{
			@Override public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
			{
				if(isChecked)	PreferenceWriter.booleanWriter(settings, Constants.SETTINGS_TXT_PWR, true);
				else			PreferenceWriter.booleanWriter(settings, Constants.SETTINGS_TXT_PWR, false);
				super.onCheckedChanged(buttonView, isChecked);
			}
		});
		
		//Set the box based on the setting
		int noteType = settings.getInt("notification", 0);
		
		tb_notification.setChecked(noteType == 1||noteType == 3);
		tb_debugNote.setChecked(noteType == 2||noteType == 3);
		tb_power.setChecked(settings.getBoolean(Constants.SETTINGS_TXT_PWR, false));
		
		logReceiver = new BroadcastReceiver() 
		{
			@Override public void onReceive(Context context, Intent intent) 
			{
				tv_log.setText(settings.getString("log", ""));
			}
		};	
		this.registerReceiver(logReceiver, new IntentFilter("com.DizWARE.ShuffleTone.Log.Update"));
		
		tv_log.setText(settings.getString("log", ""));
	}
	
	/***
	 * Unregisters our broadcast receiver so that it doesn't leak after exiting
	 */
	@Override protected void onDestroy()
	{
		this.unregisterReceiver(doneReceiver);
		this.unregisterReceiver(logReceiver);
		super.onDestroy();
	}

	/***
	 * Background thread that fetches the default Notification ringtone
	 */
	@Override public void run() 
	{
		Ringtone ringtone;
		
		//This will set up for our initial ringtone
		if(oldTitle == "")
		{
			ringtone = RingtoneManager.getRingtone(this, Settings.System.DEFAULT_NOTIFICATION_URI);
			if(ringtone != null) oldTitle = ringtone.getTitle(this);
			return;
		}
		
		ringtone = RingtoneManager.getRingtone(this, Settings.System.DEFAULT_NOTIFICATION_URI);
		if(ringtone != null) newTitle = ringtone.getTitle(this);
	}
	
	/***
	 * Handles the basic operation of the check boxes when clicked
	 * @author Tyler Robinson
	 *
	 */
	private abstract class OnCheckBoxChangedListener implements OnCheckedChangeListener
	{
		@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if(isChecked) buttonView.setBackgroundResource(R.drawable.selectbox_checked);
			else buttonView.setBackgroundResource(R.drawable.selectbox_unchecked);
			
		}
	}
}
