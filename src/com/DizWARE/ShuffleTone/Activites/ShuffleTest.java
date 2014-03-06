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

public class ShuffleTest extends Activity implements Runnable
{
	TextView tv_current;
	TextView tv_new;
	Button btn_shuffle;
	ToggleButton tb_notification;
	
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
		
		//Initialize UI
		tv_current = (TextView)this.findViewById(R.id.tv_current);
		tv_new = (TextView)this.findViewById(R.id.tv_new);
		btn_shuffle = (Button)this.findViewById(R.id.btn_shuffle);
		tb_notification = (ToggleButton)this.findViewById(R.id.tb_notification);
		
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
				if(newTitle != "") tv_current.setText(newTitle);
				tv_new.setText("Retrieving...");
			}
		});		
		
		/***
		 * Turns on or off the override feature. Demonstrates it visualy with the on/off checkboxes Nate made
		 */
		tb_notification.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				if(isChecked)
				{
					buttonView.setBackgroundResource(R.drawable.selectbox_checked);
					PreferenceWriter.intWriter(settings, "notification", 1);
				}
				else
				{
					buttonView.setBackgroundResource(R.drawable.selectbox_unchecked);
					PreferenceWriter.intWriter(settings, "notification", 0);
				}
				
			}
		});
		
		//Set the box based on the setting
		tb_notification.setChecked(settings.getInt("notification", 0) == 1);
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
}
