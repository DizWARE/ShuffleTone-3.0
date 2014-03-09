package com.DizWARE.ShuffleTone.Activites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.PreferenceWriter;
import com.DizWARE.ShuffleTone.Others.SettingTags;
import com.DizWARE.ShuffleTone.Receivers.AlarmReceiver;

public class AppSettings 
{
	Activity mContext;
	DrawerLayout mDrawer;
	
	RelativeLayout basicSettings;
	RelativeLayout help;
	RelativeLayout instructions;
	RelativeLayout problem_help;
	
	SharedPreferences settings;
	
	ShuffleTypeDialog textDialog;
	ShuffleTypeDialog callDialog;
	
	/***
	 * Constructs the application settings menu
	 * 
	 * @param context - Reference to the main activity
	 * @param drawer - Reference to the drawer layout of the main activity
	 */
	public AppSettings(Activity context, DrawerLayout drawer)
	{
		mContext = context;
		mDrawer = drawer;
		settings = mContext.getSharedPreferences("settings", 0);

		callDialog = new ShuffleTypeDialog(mContext, true);
		textDialog = new ShuffleTypeDialog(mContext, false);
	}
	
	/***
	 * Prepare the drawer layout with the basic settings
	 * 
	 * @return - The relative layout used for these settings
	 */
	public RelativeLayout prepareSettings()
	{
		if(basicSettings != null) return basicSettings;
		
		basicSettings = (RelativeLayout)mContext.getLayoutInflater().
													inflate(R.layout.settings_basic, null);
		
		TextView tv_wCalls = (TextView)basicSettings.findViewById(R.id.tv_wCalls);
		TextView tv_wTexts = (TextView)basicSettings.findViewById(R.id.tv_wTexts);
		TextView tv_help = (TextView)basicSettings.findViewById(R.id.tv_help);
		TextView tv_contact = (TextView)basicSettings.findViewById(R.id.tv_contact);
		
		tv_wCalls.setTag(true);
		tv_wTexts.setTag(false);
		
		OnClickListener when = new OnClickListener() {			
			@Override public void onClick(View v) {
				if((Boolean)v.getTag()) callDialog.show();
				else textDialog.show();
			}
		};
		
		tv_wCalls.setOnClickListener(when);
		tv_wTexts.setOnClickListener(when);
		tv_help.setOnClickListener(new OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{				
				mDrawer.setContent(prepareHelp());
			}
		});
		tv_contact.setOnClickListener(new OnClickListener() 
		{
			@Override public void onClick(View v) 
			{				
				//TODO Remember to fix this!!!
			   /* Create the Intent */  
			   final Intent emailIntent = new Intent(Intent.ACTION_SEND);  
			     
			   /* Fill it with Data */  
			   emailIntent.setType("plain/text");  
			   emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"tydiz936@gmail.com"});  
			   emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ShuffleTone 3.0");  
			  
			   /* Send it off to the Activity-Chooser */  
			   mContext.startActivity(Intent.createChooser(emailIntent, "Send mail..."));  
			}
		});
		
		return basicSettings;
	}
	
	public RelativeLayout prepareHelp()
	{
		if(help != null) return help;	
		
		help = (RelativeLayout)mContext.getLayoutInflater().
													inflate(R.layout.help_list, null);
		
		TextView tv_setup = (TextView)help.findViewById(R.id.tv_setup);
		TextView tv_problems = (TextView)help.findViewById(R.id.tv_problems);
		TextView tv_faq = (TextView)help.findViewById(R.id.tv_faq);
		TextView tv_contact = (TextView)help.findViewById(R.id.tv_contact);
		
		tv_setup.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v) 
			{
				mDrawer.setContent(prepareInstructions());
			}
		});
		tv_problems.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v) 
			{
				mDrawer.setContent(prepareProblemHelp());
			}
		});
		tv_faq.setOnClickListener(new OnClickListener()
		{	
			@Override public void onClick(View v) 
			{
				String url = "http://dizware.wirenode.mobi/page/40";
				Intent i = new Intent(Intent.ACTION_CHOOSER);
				Intent data = new Intent(Intent.ACTION_VIEW);
				data.setData(Uri.parse(url));
				data.addCategory(Intent.CATEGORY_BROWSABLE);
				i.putExtra(Intent.EXTRA_INTENT,data);
				
				mContext.startActivity(Intent.createChooser(i, "View website..."));
			}
		});
		tv_contact.setOnClickListener(new OnClickListener() 
		{
			@Override public void onClick(View v) 
			{
			    /* Create the Intent */  
			    final Intent emailIntent = new Intent();  
			     
			   /* Fill it with Data */  
			   emailIntent.setType("plain/text");  
			   emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"tydiz936@gmail.com"});  
			   emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ShuffleTone 3.0");  
			  
			   /* Send it off to the Activity-Chooser */  
			   mContext.startActivity(Intent.createChooser(emailIntent, "Send mail..."));  
			}
		});
		
		return help;
	}
	
	public RelativeLayout prepareInstructions()
	{
		if(instructions != null) return instructions;	
		
		instructions = (RelativeLayout)mContext.getLayoutInflater().
													inflate(R.layout.setup_instructions, null);
		return instructions;
	}
	
	public RelativeLayout prepareProblemHelp()
	{
		if(problem_help != null) return problem_help;	
		
		problem_help = (RelativeLayout)mContext.getLayoutInflater().
													inflate(R.layout.problem_help, null);
		return problem_help;
	}
	
	public void closeDialogs()
	{
		if(callDialog != null && callDialog.isShowing()) callDialog.cancel();
		if(textDialog != null && textDialog.isShowing()) textDialog.cancel();
	}
	
	/***
	 * Dialog that allows the user to change the shuffling routines in the for the playlist.
	 * 
	 * TODO - Make this generic to specific playlists
	 * 
	 * @author Tyler Robinson
	 */
	private class ShuffleTypeDialog extends AlertDialog
	{
		RadioButton rb_hours;
		RadioButton rb_input;
		
		EditText et_count;
		
		SeekBar sb_count;
		
		Button btn_ok;
		
		boolean mCalls;
		
		/***
		 * Creates a ShuffleTypeDialog
		 * 
		 * TODO - Right now, this uses only calls and texts. We want it to be open to any playlist
		 */
		protected ShuffleTypeDialog(Context context, boolean calls) 
		{
			super(context);
			
			RelativeLayout rl_layout = (RelativeLayout)this.getLayoutInflater()
											.inflate(R.layout.shuffle_type_dialog, null);
			
			mCalls = calls;
			
			rb_hours = (RadioButton)rl_layout.findViewById(R.id.rb_hours);
			rb_input = (RadioButton)rl_layout.findViewById(R.id.rb_input);
			
			sb_count = (SeekBar) rl_layout.findViewById(R.id.sb_count);
			et_count = (EditText) rl_layout.findViewById(R.id.et_count);
			
			btn_ok = (Button) rl_layout.findViewById(R.id.btn_ok);
			
			prepareRadioButtons();
			prepareEditText();
			prepareSeekBar();
			prepareOkButton();
			
			this.setTitle("Shuffle Settings for "+getString());			
			rb_input.setText(getString());
						
			this.setCurrent(settings.getInt(getSettingString(SettingTags.maxCount), 1));
			
			this.setView(rl_layout);
		}
		
		/***
		 * Sets up the Edit Text listeners
		 */
		private void prepareEditText()
		{
			et_count.addTextChangedListener(new TextWatcher()
			{
				@Override public void afterTextChanged(Editable s) 
				{
					try
					{
						int progress = Integer.parseInt(s.toString()) - 1;
						
						if(progress >= sb_count.getMax())
						{
							progress = sb_count.getMax() - 1;
							et_count.setText("" + (progress + 1));
						}
						else if(progress < 0)
						{
							progress = 0;
							et_count.setText("" + (progress + 1));
						}
						
						sb_count.setProgress(progress);
					}catch(NumberFormatException e)	{ /**Fail Silently**/}
				}
				@Override public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {    }
				@Override public void onTextChanged(CharSequence s, int start,
						int before, int count) {	}				
			});
		}
		
		/***
		 * Sets up the radio button listeners
		 */
		private void prepareRadioButtons()
		{
			boolean useHours = settings.getBoolean(
					getSettingString(SettingTags.useHours), false);
			setMax(99);
			
			rb_hours.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
				@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked)
						setMax(24);
					else
						setMax(99);
					
					setCurrent(sb_count.getProgress()+1);
				}
			});
			
			rb_hours.setChecked(useHours);
		}
		
		/***
		 * Sets up the seekbar listeners
		 */
		private void prepareSeekBar()
		{
			sb_count.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {				
				@Override public void onStopTrackingTouch(SeekBar seekBar) {}
				
				@Override public void onStartTrackingTouch(SeekBar seekBar){}
				
				@Override public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) 
				{
					if(fromUser)
						et_count.setText("" + (seekBar.getProgress() + 1));
				}
			});
		}
		
		/***
		 * Sets up the Ok button listeners
		 */
		private void prepareOkButton()
		{
			/***Ok button. Save all settings***/
			btn_ok.setOnClickListener(new View.OnClickListener() {
				
				@Override public void onClick(View v) {
					PreferenceWriter.booleanWriter(settings, 
							getSettingString(SettingTags.useHours), rb_hours.isChecked());
					PreferenceWriter.intWriter(settings, 
							getSettingString(SettingTags.maxCount), sb_count.getProgress()+1);	
					
					int ringType = Constants.TYPE_CALLS;
					if(!mCalls) ringType = Constants.TYPE_TEXTS;
					
					if(rb_hours.isChecked())
						AlarmReceiver.startAlarm(AppSettings.this.mContext, ringType); 
					cancel();
				}
			});
		}
		
		/***
		 * Gets the string the represents the tag for this item(since it depends
		 * on the type of settings you are changing)
		 * 
		 * @param tag - Setting you want to change
		 * @return - String with ringer type constant in-front
		 */
		public String getSettingString(SettingTags tag)
		{
			int type = Constants.TYPE_CALLS;
			if(!mCalls) type = Constants.TYPE_TEXTS;
			
			return type + tag.toString();
		}
		
		/***
		 * Gets the string that represents the type of dialog this is(Calls, or Texts)
		 * @return Calls if mCalls is true, Texts if otherwise
		 */
		public String getString()
		{
			if(mCalls) return "Calls";
			return "Texts";
		}
		
		/***
		 * Set the maximum the progress bar can be
		 * 
		 * @param max - The maximum number that the sliding bar should reach
		 */
		public void setMax(int max)
		{
			sb_count.setMax(max);
		}
		
		/***
		 * Sets the current text
		 * 
		 * @param current - 
		 */
		public void setCurrent(int current)
		{
			sb_count.setProgress(0);
			et_count.setText("" + current);
		}
	}
}
