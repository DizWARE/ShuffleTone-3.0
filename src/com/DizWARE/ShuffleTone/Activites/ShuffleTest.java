package com.DizWARE.ShuffleTone.Activites;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.provider.Settings;
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
	
	Button btn_shuffle;
	Button btn_message;
	
	ToggleButton tb_power;
	ToggleButton tb_notification;
	ToggleButton tb_debugNote;
	
	SharedPreferences settings;
	
	String oldTitle = "";
	String newTitle = "";
	
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
		BroadcastReceiver doneReceiver = new BroadcastReceiver() 
		{
			@Override public void onReceive(Context context, Intent intent) 
			{
				//Retrieve new ringtone info
				Thread t = new Thread(ShuffleTest.this);
				t.run();
				
				while(t.isAlive());
				tv_current.setText(newTitle);
				tv_new.setText(newTitle);
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
		 * Turns on or off the TextTone override feature. 
		 */
		tb_notification.setOnCheckedChangeListener(new OnCheckBoxChangedListener()
		{
			@Override public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
			{
				if(isChecked)	PreferenceWriter.intWriter(settings, "notification", 1);
				else			PreferenceWriter.intWriter(settings, "notification", 0);
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
		tb_notification.setChecked(settings.getInt("notification", 0) == 1||settings.getInt("notification", 0) == 3);
		tb_debugNote.setChecked(settings.getInt("notification", 0) == 2||settings.getInt("notification", 0) == 3);
		tb_power.setChecked(settings.getBoolean(Constants.SETTINGS_TXT_PWR, false));
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
	
	private abstract class OnCheckBoxChangedListener implements OnCheckedChangeListener
	{
		@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if(isChecked) buttonView.setBackgroundResource(R.drawable.selectbox_checked);
			else buttonView.setBackgroundResource(R.drawable.selectbox_unchecked);
			
		}
	}
}
